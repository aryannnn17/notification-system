package com.aryanbhagat.EmailConsumer.consumer;

import com.aryanbhagat.EmailConsumer.service.MessageHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

import static com.aryanbhagat.EmailConsumer.constants.Constants.TOPIC;
import static com.aryanbhagat.EmailConsumer.constants.Constants.GROUP_ID;

// This class has been disabled to prevent Kafka consumer conflicts
// The EmailKafkaConsumer class handles email message consumption using @KafkaListener
// @Component
// @Slf4j
// public class PriorityAwarePartitionConsumer {
//     // ... original implementation removed
// }
