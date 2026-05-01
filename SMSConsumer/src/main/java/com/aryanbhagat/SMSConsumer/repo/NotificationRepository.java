package com.aryanbhagat.SMSConsumer.repo;

import com.aryanbhagat.SMSConsumer.models.db.Notification;
import com.aryanbhagat.SMSConsumer.models.enums.Channel;
import com.aryanbhagat.SMSConsumer.models.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Find notifications for a specific user
    List<Notification> findByUserId(Long userId);

    // Find notifications by user and channel
    List<Notification> findByUserIdAndChannel(Long userId, Channel channel);

    // Find notifications by status
    List<Notification> findByStatus(Status status);
}

