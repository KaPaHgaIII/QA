package ru.kapahgaiii.qa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.dto.ChatEvent;
import ru.kapahgaiii.qa.core.objects.Subscriber;
import ru.kapahgaiii.qa.dto.Online;

@Service("SocketService")
public class SocketService {

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    public void sendUserConnected(){

    }

    public void sendUserSubscribedToChat(Question question, Subscriber subscriber) {
        ChatEvent chatEvent = new ChatEvent("subscribe");
        chatEvent.setSubscriber(subscriber);
        messagingTemplate.convertAndSend("/chat/events/" + question.getId(), chatEvent);
    }

    public void sendUserUnsubscribedFromChat(Question question, Subscriber subscriber){
        ChatEvent chatEvent = new ChatEvent("unsubscribe");
        chatEvent.setSubscriber(subscriber);
        messagingTemplate.convertAndSend("/chat/events/" + question.getId(), chatEvent);
    }

    public void sendOnline(int users, int guests){
        messagingTemplate.convertAndSend("/online", new Online(users, guests));
    }

}
