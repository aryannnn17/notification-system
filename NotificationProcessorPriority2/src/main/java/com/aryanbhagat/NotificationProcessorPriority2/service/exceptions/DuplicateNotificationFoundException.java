package com.aryanbhagat.NotificationProcessorPriority2.service.exceptions;

import com.aryanbhagat.NotificationProcessorPriority2.models.db.Notification;

public class DuplicateNotificationFoundException extends RuntimeException {
    public DuplicateNotificationFoundException(String message) {
        super(message);
    }
}

