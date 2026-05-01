package com.aryanbhagat.NotificationProcessorPriority1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class NotificationProcessorPriority1Application {

	public static void main(String[] args) {
		SpringApplication.run(NotificationProcessorPriority1Application.class, args);
	}

}
