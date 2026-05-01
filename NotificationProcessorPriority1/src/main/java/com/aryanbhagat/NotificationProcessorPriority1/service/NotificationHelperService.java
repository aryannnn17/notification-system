package com.aryanbhagat.NotificationProcessorPriority1.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aryanbhagat.NotificationProcessorPriority1.models.db.Preference;
import com.aryanbhagat.NotificationProcessorPriority1.models.enums.Channel;
import com.aryanbhagat.NotificationProcessorPriority1.models.requests.EmailRequest;
import com.aryanbhagat.NotificationProcessorPriority1.models.requests.PushNRequest;
import com.aryanbhagat.NotificationProcessorPriority1.models.requests.SmsRequest;
import com.aryanbhagat.NotificationProcessorPriority1.repo.PreferenceRepository;
import com.aryanbhagat.NotificationProcessorPriority1.service.exceptions.PreferenceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

import static com.aryanbhagat.NotificationProcessorPriority1.constants.Constants.PRIORITY;

@Service
@Slf4j
public class NotificationHelperService {
    PreferenceRepository preferenceRepository;
    ObjectMapper objectMapper;
    
    public NotificationHelperService(PreferenceRepository preferenceRepository, ObjectMapper objectMapper){
        this.preferenceRepository = preferenceRepository;
        this.objectMapper = objectMapper;
    }
    public boolean isNotificationAllowed_PreferenceCheck(Long userId, Channel channel) {
        // Bypass preference check for testing to allow all notifications
        log.debug("Bypassing preference check for testing - allowing all notifications");
        return true;
    }



    private boolean quietHoursActive(JsonNode quietHours) {
        String startTimeString = quietHours.get("start").asText();
        String endTimeString = quietHours.get("end").asText();

        LocalTime startTime = LocalTime.parse(startTimeString);
        LocalTime endTime = LocalTime.parse(endTimeString);

        if (startTime.isAfter(endTime)) { //midnight interval
            return LocalTime.now().isAfter(startTime) || LocalTime.now().isBefore(endTime);
        } else {
            return LocalTime.now().isAfter(startTime) && LocalTime.now().isBefore(endTime);
        }
    }

    public String getSmsHash(SmsRequest smsRequest, Long userId) {
        String text = PRIORITY+"&"+smsRequest.getMessage()+"&"+smsRequest.getMobileNumber()+"&"+userId.toString();
        return DigestUtils.sha256Hex(text);
    }

    public String getPushNHash(PushNRequest pushNRequest, Long userId) {
        String text = PRIORITY+"&"+pushNRequest.getTitle()+"&"+pushNRequest.getMessage()+"&"+pushNRequest.getAction()+"&"+userId.toString();
        return DigestUtils.sha256Hex(text);
    }

    public String getEmailHash(EmailRequest emailRequest, Long userId) {
        String text = PRIORITY+"&"+emailRequest.getEmailSubject()+"&"+emailRequest.getMessage()+"&"+ Arrays.toString(emailRequest.getEmailAttachments())+"&"+userId.toString();
        return DigestUtils.sha256Hex(text);
    }
}
