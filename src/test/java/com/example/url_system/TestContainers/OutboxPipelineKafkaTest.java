package com.example.url_system.TestContainers;

import com.example.url_system.dtos.CreateUrlRequest;
import com.example.url_system.models.OutboxEvent;
import com.example.url_system.models.User;
import com.example.url_system.repositories.OutboxEventRepository;
import com.example.url_system.repositories.UserRepository;
import com.example.url_system.services.UrlService;
import com.example.url_system.utils.emailSender.EmailSender;
import com.example.url_system.utils.emailSender.OutboxDispatcher;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.util.*;
import java.util.concurrent.TimeUnit;


@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class OutboxPipelineKafkaTest {
    @MockitoBean
    private Clock clock;

    private static final String TOPIC = "email.send.requested";


    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1")
    );

    @DynamicPropertySource
    static void kafkaProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

        // Optional but helpful: producer reliability in tests
        registry.add("spring.kafka.producer.acks", () -> "all");
        registry.add("spring.kafka.producer.properties.delivery.timeout.ms", () -> "30000");
        registry.add("spring.kafka.producer.properties.request.timeout.ms", () -> "10000");
    }

    @Autowired PasswordEncoder passwordEncoder;
    @Autowired UserRepository userRepository;
    @Autowired UrlService urlService;
    @Autowired OutboxEventRepository outboxEventRepository;
    @Autowired JdbcTemplate jdbc;
    @Autowired OutboxDispatcher outboxDispatcher;

    @MockitoBean
    EmailSender emailSender;


    @BeforeAll
    static void createTopic() throws Exception {
        // Create topic explicitly => no dependency on auto-create-topics config
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());

        try (AdminClient admin = AdminClient.create(props)) {
            NewTopic topic = new NewTopic(TOPIC, 1, (short) 1);
            admin.createTopics(List.of(topic)).all().get(10, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // If it already exists, fine.
        }
    }

    @BeforeEach
    void setup() {
        jdbc.execute("TRUNCATE TABLE urls, users, idempotency_keys, outbox_events RESTART IDENTITY CASCADE");
        userRepository.save(new User("igor.bb00@gmail.com", passwordEncoder.encode("12345678")));
    }

    @Test
    void shouldClaimPublishAndMarkDone() throws Exception {
        User user = userRepository.findByUsername("igor.bb00@gmail.com").orElseThrow();

        Instant fixedInstant = LocalDate.of(2082, 12, 13)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());


        urlService.create(
                new CreateUrlRequest("https://12dwkdaowdko.com", Instant.now().plusSeconds(120)),
                user.getId(),
                "123"
        );

        OutboxEvent event = outboxEventRepository.findAll().getFirst();
        assertEquals(OutboxEvent.Status.NEW, event.getStatus());

        List<Long> ids = outboxDispatcher.claimBatch(50);
        assertTrue(ids.contains(event.getId()));

        OutboxEvent processing = outboxEventRepository.findById(event.getId()).orElseThrow();
        assertEquals(OutboxEvent.Status.PROCESSING, processing.getStatus());

        try (KafkaConsumer<String, String> consumer = newConsumer(kafka.getBootstrapServers())) {
            consumer.subscribe(List.of(TOPIC));
            consumer.poll(Duration.ofMillis(200));

            outboxDispatcher.publishOne(event.getId());

            OutboxEvent done = outboxEventRepository.findById(event.getId()).orElseThrow();
            assertEquals(OutboxEvent.Status.DONE, done.getStatus());
            assertNull(done.getLastError());
            assertNull(done.getNextAttemptAt());

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
            assertFalse(records.isEmpty(), "Expected at least 1 kafka message");

            boolean found = false;

            for (ConsumerRecord<String, String> r : records.records(TOPIC)) {
                if (Objects.equals(r.key(), String.valueOf(event.getId()))
                        && Objects.equals(r.value(), done.getPayload().toString())) {
                    found = true;
                    break;
                }
            }

            assertTrue(found, "Expected message with key=eventId and value=payloadJson");

        }
    }

    private KafkaConsumer<String, String> newConsumer(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        return new KafkaConsumer<>(props, new StringDeserializer(), new StringDeserializer());
    }
}
