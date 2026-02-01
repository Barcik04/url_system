//package com.example.url_system.utils.config;
//
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.boot.mail.autoconfigure.MailProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.JavaMailSenderImpl;
//
//import java.util.Properties;
//
//@Configuration
//@EnableConfigurationProperties(MailProperties.class)
//public class MailConfig {
//
//    @Bean
//    public JavaMailSender javaMailSender(MailProperties mailProperties) {
//        JavaMailSenderImpl sender = new JavaMailSenderImpl();
//        sender.setHost(mailProperties.getHost());
//        sender.setPort(mailProperties.getPort());
//        sender.setUsername(mailProperties.getUsername());
//        sender.setPassword(mailProperties.getPassword());
//        sender.setProtocol(mailProperties.getProtocol());
//        sender.setDefaultEncoding(mailProperties.getDefaultEncoding().name());
//
//        Properties javaMailProperties = sender.getJavaMailProperties();
//        javaMailProperties.putAll(mailProperties.getProperties());
//
//        return sender;
//    }
//}
//
