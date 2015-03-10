package ru.kapahgaiii.qa.repository.interfaces;

import ru.kapahgaiii.qa.domain.User;
import ru.kapahgaiii.qa.dto.Notification;

import java.util.List;

public interface NotificationsDAO {
    public List<Notification> getNotifications(User user);
}
