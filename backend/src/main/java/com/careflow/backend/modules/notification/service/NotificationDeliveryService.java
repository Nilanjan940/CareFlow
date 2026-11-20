package com.careflow.backend.modules.notification.service;

import com.careflow.backend.modules.notification.entity.Notification;

public interface NotificationDeliveryService {

    void deliver(Notification notification);
}