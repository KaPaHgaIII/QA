package ru.kapahgaiii.qa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.kapahgaiii.qa.core.objects.Subscriber;
import ru.kapahgaiii.qa.domain.Message;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.dto.*;

@Service("SocketService")
public class SocketService {

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    public void sendChatMessage(Message message) {
        MessageDTO[] answer = {new MessageDTO(message)}; // client requires array
        messagingTemplate.convertAndSend("/chat/messages/" + message.getQuestion().getQuestionId(), answer);
    }

    public void sendChatEvent(String chatId, ChatEvent event) {
        messagingTemplate.convertAndSend("/chat/events/" + chatId, event);
    }

    public void sendUserSubscribedToChat(Question question, Subscriber subscriber) {
        ChatEvent chatEvent = new ChatEvent("subscribe");
        chatEvent.setSubscriber(subscriber);
        messagingTemplate.convertAndSend("/chat/events/" + question.getQuestionId(), chatEvent);
    }

    public void sendUserUnsubscribedFromChat(Question question, Subscriber subscriber) {
        ChatEvent chatEvent = new ChatEvent("unsubscribe");
        chatEvent.setSubscriber(subscriber);
        messagingTemplate.convertAndSend("/chat/events/" + question.getQuestionId(), chatEvent);
    }

    public void sendOnline(int users, int guests) {
        messagingTemplate.convertAndSend("/online", new Online(users, guests));
    }

    public void sendQuestionInfo(String type, Question question, int value) {
        messagingTemplate.convertAndSend("/questions", new QuestionInfo(question.getQuestionId(), type, value));
    }

    public void sendNotification(Notification notification, String username) {
        if (notification.getType().equals("addressed_message") && username != null) {
            messagingTemplate.convertAndSendToUser(username, "/notifications", notification);
            return;
        }
        messagingTemplate.convertAndSend("/notifications", notification);
    }

    public void sendNotification(Notification notification) {
        sendNotification(notification, null);
    }

}
