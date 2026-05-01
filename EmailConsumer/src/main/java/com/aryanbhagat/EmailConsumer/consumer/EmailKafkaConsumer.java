package com.aryanbhagat.EmailConsumer.consumer;

import com.aryanbhagat.EmailConsumer.service.MessageHandlerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aryanbhagat.EmailConsumer.models.EmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailKafkaConsumer {
    
    private final MessageHandlerService messageHandlerService;
    private final ObjectMapper objectMapper;

    public EmailKafkaConsumer(MessageHandlerService messageHandlerService, ObjectMapper objectMapper) {
        this.messageHandlerService = messageHandlerService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "email-topic", groupId = "email-consumer")
    public void consumeEmailRequest(String message) {
        log.info("Received email request: {}", message);
        
        try {
            EmailRequest emailRequest = objectMapper.readValue(message, EmailRequest.class);
            log.info("Successfully parsed EmailRequest: {}", emailRequest);
            messageHandlerService.handleEmailRequest(message);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse email request: {}", message, e);
        } catch (Exception e) {
            log.error("Unexpected error processing email request: {}", message, e);
        }
    }
}
