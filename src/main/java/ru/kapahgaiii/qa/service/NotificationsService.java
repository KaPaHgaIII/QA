package ru.kapahgaiii.qa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.kapahgaiii.qa.domain.Message;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.domain.Tag;
import ru.kapahgaiii.qa.domain.User;
import ru.kapahgaiii.qa.dto.Notification;
import ru.kapahgaiii.qa.repository.interfaces.NotificationsDAO;

import java.util.ArrayList;
import java.util.List;

@Service("NotificationsService")
public class NotificationsService {

    @Autowired
    private NotificationsDAO notificationsDAO;


    public Notification createNewQuestionNotification(Question question) {
        Notification notification = new Notification();
        notification.setType("new_question");
        notification.setParam(question.getQuestionId().toString());
        notification.setText(question.getTitle());
        notification.setTime(question.getAskedTime().getTime());
        List<String> tags = new ArrayList<String>();
        for (Tag tag : question.getTags()) {
            tags.add(tag.getName());
        }
        notification.getNames().addAll(tags);
        return notification;
    }

    public Notification createNewMessageNotification(Message message) {
        Notification notification = new Notification();
        notification.setType("new_message");
        notification.setParam(message.getQuestion().getQuestionId().toString());
        notification.setText(message.getQuestion().getTitle());
        notification.setTime(message.getTime().getTime());
        return notification;
    }

    public Notification createAddressedMessageNotification(Message message) {
        if (message.getAddressee() == null) {
            return null;
        }
        Notification notification = new Notification();
        notification.setType("addressed_message");
        notification.setParam(message.getQuestion().getQuestionId().toString());
        notification.setText(message.getQuestion().getTitle());
        notification.setTime(message.getTime().getTime());
        return notification;
    }

    public List<Notification> getNotifications(User user) {
        return notificationsDAO.getNotifications(user);
    }

}
