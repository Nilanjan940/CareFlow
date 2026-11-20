package com.careflow.backend.modules.notification.service;

import com.careflow.backend.modules.notification.entity.Notification;

import org.springframework.stereotype.Service;

@Service
public class UnavailableNotificationDeliveryService
    implements NotificationDeliveryService {

    @Override
    public void deliver(Notification notification) {
        throw new UnsupportedOperationException(
            "Notification delivery is not configured for channel: "
                + notification.getChannel()
        );
    }
}