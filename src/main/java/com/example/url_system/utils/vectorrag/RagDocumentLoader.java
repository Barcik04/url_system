package com.example.url_system.utils.vectorrag;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Profile("!test")
@Component
public class RagDocumentLoader implements CommandLineRunner {

    private final VectorStore vectorStore;

    public RagDocumentLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) throws Exception {
        PathMatchingResourcePatternResolver resolver =
                new PathMatchingResourcePatternResolver();

        Resource[] resources = resolver.getResources("classpath:rag/*.md");

        if (resources.length == 0) {
            System.out.println("No RAG documents found in classpath:rag/");
            return;
        }

        List<Document> documents = new ArrayList<>();

        for (Resource resource : resources) {
            String content = new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            Document doc = new Document(content);
            doc.getMetadata().put("source", resource.getFilename());

            documents.add(doc);
        }

        vectorStore.add(documents);
        System.out.println("Loaded " + documents.size() + " RAG documents into vector store.");
    }
}