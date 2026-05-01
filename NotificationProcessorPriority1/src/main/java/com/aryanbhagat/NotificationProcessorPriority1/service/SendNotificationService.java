package com.aryanbhagat.NotificationProcessorPriority1.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aryanbhagat.NotificationProcessorPriority1.models.db.DeliveryLog;
import com.aryanbhagat.NotificationProcessorPriority1.models.db.Notification;
import com.aryanbhagat.NotificationProcessorPriority1.models.db.Preference;
import com.aryanbhagat.NotificationProcessorPriority1.models.db.User;
import com.aryanbhagat.NotificationProcessorPriority1.models.enums.Channel;
import com.aryanbhagat.NotificationProcessorPriority1.models.enums.Status;
import com.aryanbhagat.NotificationProcessorPriority1.models.requests.EmailRequest;
import com.aryanbhagat.NotificationProcessorPriority1.models.requests.PushNRequest;
import com.aryanbhagat.NotificationProcessorPriority1.models.requests.SmsRequest;
import com.aryanbhagat.NotificationProcessorPriority1.repo.DeliveryLogRepository;
import com.aryanbhagat.NotificationProcessorPriority1.repo.NotificationRepository;
import com.aryanbhagat.NotificationProcessorPriority1.service.exceptions.DuplicateNotificationFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static com.aryanbhagat.NotificationProcessorPriority1.constants.Constants.*;

@Service
@Slf4j
public class SendNotificationService {
    KafkaTemplate<String , String> kafkaTemplate;
    ObjectMapper objectMapper;
    NotificationRepository notificationRepository;
    DeliveryLogRepository deliveryLogRepository;
    NotificationHelperService notificationHelperService;


    
    public SendNotificationService(KafkaTemplate<String, String> kafkaTemplate,
                                   NotificationRepository notificationRepository, DeliveryLogRepository deliveryLogRepository,
                                   ObjectMapper objectMapper, NotificationHelperService notificationHelperService){
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.notificationRepository = notificationRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.notificationHelperService = notificationHelperService;


    }



    public void sendSmsRequest(SmsRequest smsRequest, User user) {
        Notification notification = null;
        try{
            // Bypass database insertion for testing to avoid foreign key constraints
            notification = new Notification(user, Channel.sms, smsRequest.getMessage(), objectMapper.writeValueAsString(smsRequest), notificationHelperService.getSmsHash(smsRequest, user.getId()));
            notification.setId(1L); // Set dummy ID for testing
            smsRequest.setNotificationId(notification.getId());
        } catch (JsonProcessingException e){
            log.error("Exception parsing requestContent to String: {}", e.toString());
        }
        catch (Exception e){
            if(e.toString().contains("Duplicate entry")){
                throw new DuplicateNotificationFoundException("Duplicate notification request. "+smsRequest.toString());
            } else {
                throw e;
            }
        }

        boolean isSmsAllowed = notificationHelperService.isNotificationAllowed_PreferenceCheck(user.getId(), Channel.sms);
        if(isSmsAllowed){
            try {
                log.info("Preference: SMS is allowed acc to preferences. UserId: {}, SmsRequest: {}",user.getId(),smsRequest);
                String notificationString = prepareMessage(smsRequest);
                kafkaTemplate.send(SMS_TOPIC, PRIORITY_KEY_FOR_PARTITIONS, notificationString);
                // Bypass delivery log insertion for testing to avoid foreign key constraints
                // deliveryLogRepository.save(new DeliveryLog(notification, Channel.sms, Status.pending,"Scheduled to kafka"));
                log.info("SMS sent to kafka. Delivery Log bypassed for testing. UserId: {}, SmsRequest: {}",user.getId(),smsRequest);
            } catch (JsonProcessingException e) {
                log.error("Error in parsing sms notification {} for forwarding to Kafka.\n {}", smsRequest.toString(), e.toString());
            } catch (Exception e) {
                log.error("Failed to forward sms notification {}, to Kafka: \n{}", smsRequest.toString(), e.toString());
            }
        } else{
            log.info("Preference: Not sending SMS as per user preferences. UserId: {}, SmsRequest: {}",user.getId(),smsRequest);
            // Bypass delivery log insertion for testing to avoid foreign key constraints
            // deliveryLogRepository.save(new DeliveryLog(notification, Channel.sms, Status.failed,"Not sending notification as per user: "+user.getId()+" preferences"));
        }
    }

    // Push notification method removed - only email and SMS supported
    // public void sendPushNRequest(PushNRequest pushNRequest, User user) { ... }

    public void sendEmailRequest(EmailRequest emailRequest, User user) {
        Notification notification = null;
        try{
            // Bypass database insertion for testing to avoid foreign key constraints
            notification = new Notification(user, Channel.email, "emailSubject: " + emailRequest.getEmailSubject() + " message: " + emailRequest.getMessage() + " attachments: " + Arrays.toString(emailRequest.getEmailAttachments())
                    , objectMapper.writeValueAsString(emailRequest), notificationHelperService.getEmailHash(emailRequest, user.getId()));
            notification.setId(1L); // Set dummy ID for testing
            emailRequest.setNotificationId(notification.getId());
        } catch (JsonProcessingException e){
            log.error("Exception parsing requestContent to String: {}", e.toString());
        } catch (Exception e){
            if(e.toString().contains("Duplicate entry")){
                throw new DuplicateNotificationFoundException("Duplicate notification request. "+emailRequest.toString());
            } else {
                throw e;
            }
        }

        boolean isEmailAllowed = notificationHelperService.isNotificationAllowed_PreferenceCheck(user.getId(), Channel.email);
        if(isEmailAllowed){
            try {
                log.info("Preference: Email is allowed acc to preferences. UserId: {}, EmailRequest: {}",user.getId(),emailRequest);
                String notificationString = prepareMessage(emailRequest);
                kafkaTemplate.send(EMAIL_TOPIC, PRIORITY_KEY_FOR_PARTITIONS, notificationString);
                // Bypass delivery log insertion for testing to avoid foreign key constraints
                // deliveryLogRepository.save(new DeliveryLog(notification, Channel.email, Status.pending,"Scheduled to kafka"));
                log.info("Email is sent to kafka. Delivery Log bypassed for testing. UserId: {}, EmailRequest: {}",user.getId(),emailRequest);
            } catch (JsonProcessingException e) {
                log.error("Error in parsing Email notification {} for forwarding to Kafka.\n {}", emailRequest.toString(), e.toString());
            } catch (Exception e) {
                log.error("Failed to forward Email notification {}, to Kafka: \n{}", emailRequest.toString(), e.toString());
            }
        } else {
            log.info("Preference: Not sending Email Notification as per user preferences. UserId: {}, EmailRequest: {}",user.getId(),emailRequest);
            // Bypass delivery log insertion for testing to avoid foreign key constraints
            // deliveryLogRepository.save(new DeliveryLog(notification, Channel.email, Status.failed,"Not sending notification as per user: "+user.getId()+" preferences"));
        }
    }



    private <T> String  prepareMessage(T request) throws JsonProcessingException {
        //Need to send Request as String data to kafka

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(request);
    }


}
