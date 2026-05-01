package com.aryanbhagat.EmailConsumer.service;

import com.aryanbhagat.EmailConsumer.models.EmailRequest;
import org.springframework.stereotype.Service;

@Service
public class FailedNotificationsHandlerService {
    public void handleFailedRequest(EmailRequest emailRequest){
        //implement retry strategy or logging for failed notifications
    }
}
