package com.aryanbhagat.NotificationProcessorPriority1.service.exceptions;

import com.aryanbhagat.NotificationProcessorPriority1.models.db.Notification;

public class DuplicateNotificationFoundException extends RuntimeException {
    public DuplicateNotificationFoundException(String message) {
        super(message);
    }
}

