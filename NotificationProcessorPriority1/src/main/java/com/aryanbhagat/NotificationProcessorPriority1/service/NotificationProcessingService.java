package com.aryanbhagat.NotificationProcessorPriority1.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aryanbhagat.NotificationProcessorPriority1.models.Content;
import com.aryanbhagat.NotificationProcessorPriority1.models.NotificationRequest;
import com.aryanbhagat.NotificationProcessorPriority1.models.PushNotification;
import com.aryanbhagat.NotificationProcessorPriority1.models.enums.Channel;
import com.aryanbhagat.NotificationProcessorPriority1.models.requests.EmailRequest;
import com.aryanbhagat.NotificationProcessorPriority1.models.requests.PushNRequest;
import com.aryanbhagat.NotificationProcessorPriority1.models.requests.SmsRequest;
import com.aryanbhagat.NotificationProcessorPriority1.models.db.Notification;
import com.aryanbhagat.NotificationProcessorPriority1.models.db.Template;
import com.aryanbhagat.NotificationProcessorPriority1.models.db.User;
import com.aryanbhagat.NotificationProcessorPriority1.repo.TemplateRepository;
import com.aryanbhagat.NotificationProcessorPriority1.repo.UserRepository;
import com.aryanbhagat.NotificationProcessorPriority1.service.exceptions.DuplicateNotificationFoundException;
import com.aryanbhagat.NotificationProcessorPriority1.service.exceptions.PlaceholderNotFoundInRequestException;
import com.aryanbhagat.NotificationProcessorPriority1.service.exceptions.TemplateNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.ArrayList;
import java.util.Map;

@Service
@Slf4j
public class NotificationProcessingService {
    ObjectMapper objectMapper;
    TemplateRepository templateRepository;
    UserRepository userRepository;
    SendNotificationService sendNotificationService;

    public NotificationProcessingService(ObjectMapper objectMapper,TemplateRepository templateRepository, UserRepository userRepository, SendNotificationService sendNotificationService){
        this.objectMapper = objectMapper;
        this.templateRepository = templateRepository;
        this.userRepository = userRepository;
        this.sendNotificationService = sendNotificationService;
    }

    public void processNotification(NotificationRequest notificationRequest) {
        try {
            if (notificationRequest.getContent().isUsingTemplates()){
                prepareMessageFromTemplate(notificationRequest);
            }

            //Channel validation done at Notification Service
            ArrayList<Channel> channels = getChannels(notificationRequest.getChannels());
            
            // Use email directly from request to bypass database lookup issues
            String userEmail = notificationRequest.getRecipient().getUserEmail();
            Long userId = Long.parseLong(notificationRequest.getRecipient().getUserId());
            log.debug("Using email from request: {}", userEmail);
            
            // Create a simple user object for the notification
            User user = new User();
            user.setId(userId);
            user.setEmail(userEmail);
            user.setName("Test User");
            user.setPhone("15104537204"); // Set phone number for SMS
            log.debug("User created from request: {}", user);

            if(channels.contains(Channel.email)){
                try {
                    prepareAndSendEmailNotification(notificationRequest, user.getEmail(), user);
                } catch (Exception exception){
                    log.error("Unexpected Exception while processing Email Notification Request: {}", notificationRequest);
                    log.error("Exception: {}", exception.toString());
                }
            }
            if(channels.contains(Channel.sms)){
                try{
                    prepareAndSendSMSNotification(notificationRequest.getContent().getMessage(),user.getPhone(),user);
                }catch (Exception exception){
                    log.error("Unexpected Exception while processing SMS Notification Request: {}", notificationRequest);
                    log.error("Exception: {}", exception.toString());
                }

            }
            // Push notifications removed - only email and SMS supported
        } catch (Exception e) {
            log.error("Error processing notification: " + e.getMessage());
        }
    }

    private void prepareMessageFromTemplate(NotificationRequest notificationRequest) {
        String templateName = notificationRequest.getContent().getTemplateName();
        try{
            Template usedTemplate = templateRepository.findByName(templateName)
                    .orElseThrow(() -> {
                        log.error("Template with name: " + templateName + " Not found");
                        return new TemplateNotFoundException("Template with name: " + templateName + " Not found");
                    });
            System.out.println("Used Template: "+usedTemplate.toString());
            Map<String,String> placeholdersInRequest = notificationRequest.getContent().getPlaceholders();
            String[] requiredPlaceholders = objectMapper.readValue(usedTemplate.getPlaceholders(),String[].class);

            String updatedMessage = replacePlaceholdersInMessageContent(usedTemplate.getContent(),placeholdersInRequest,requiredPlaceholders);
            notificationRequest.getContent().setMessage(updatedMessage);
            System.out.println("Updated message: "+updatedMessage);
        } catch (TemplateNotFoundException | PlaceholderNotFoundInRequestException e){
            log.error(e+" For notificationRequest: "+notificationRequest);
        } catch (JsonProcessingException e) {
            log.error("Error parsing String placeholders to Json from usedTemplate. "+e+" For notificationRequest: "+notificationRequest);
        }
    }

    private String replacePlaceholdersInMessageContent(String content, Map<String, String> placeholdersInRequest, String[] requiredPlaceholders) throws PlaceholderNotFoundInRequestException {
        for(String s: requiredPlaceholders){ //check all req. placeholders exist in request
            if(!placeholdersInRequest.containsKey(s)){
                throw new PlaceholderNotFoundInRequestException("Value for "+s+" not found in the request for using content template");
            }
        }
        for(String s: requiredPlaceholders){
            content = content.replace("{"+s+"}",placeholdersInRequest.get(s));
        }
        return content;
    }

    // Push notification method removed - only email and SMS supported

    private void prepareAndSendSMSNotification(String message, String phone, User user) {
        SmsRequest smsRequest = new SmsRequest(phone,message);
        try{
            sendNotificationService.sendSmsRequest(smsRequest, user);
        }catch (DuplicateNotificationFoundException duplicateNotificationFoundException){
            log.error("Duplicate SMS Request. "+duplicateNotificationFoundException.toString());
        }
    }

    private void prepareAndSendEmailNotification(NotificationRequest notificationRequest, String email, User user) {
        Content content = notificationRequest.getContent();

        EmailRequest emailRequest = new EmailRequest(email,content.getMessage(),content.getEmailSubject(),content.getEmailAttachments());
        try{
            sendNotificationService.sendEmailRequest(emailRequest,user);
        } catch (DuplicateNotificationFoundException duplicateNotificationFoundException){
            log.error("Duplicate Email Request. "+duplicateNotificationFoundException.toString());
        }
    }

    private ArrayList<Channel> getChannels(String[] channels) {
        ArrayList<Channel> channelList = new ArrayList<>();
        for (String s: channels){
            if(s.equals("email")){
                channelList.add(Channel.email);
            } else if(s.equals("sms")){
                channelList.add(Channel.sms);
            }
            // Push channel removed - only email and SMS supported
        }
        return channelList;
    }
}
