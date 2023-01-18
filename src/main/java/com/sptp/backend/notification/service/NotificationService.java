package com.sptp.backend.notification.service;

import com.sptp.backend.notification.repository.Notification;
import com.sptp.backend.notification.repository.NotificationRepository;
import com.sptp.backend.notification.web.dto.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationList(Long loginMember){

        List<Notification> findNotificationList = notificationRepository.findByMemberId(loginMember);

        List<NotificationResponse> notificationResponses =findNotificationList.stream()
                .map(m -> new NotificationResponse(m.getTitle(), m.getMessage(), m.getLink(), m.getCreatedDate()))
                .collect(Collectors.toList());

        return notificationResponses;
    }
}
