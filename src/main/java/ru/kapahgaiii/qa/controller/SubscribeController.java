package ru.kapahgaiii.qa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import ru.kapahgaiii.qa.service.ChatService;

//@Controller
public class SubscribeController implements ApplicationListener<ApplicationEvent> {


    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent.getClass() == SessionSubscribeEvent.class) {
            System.out.println("SUBSCRIBED");
            SessionSubscribeEvent subscribeEvent = (SessionSubscribeEvent) applicationEvent;
            MessageHeaders headers = subscribeEvent.getMessage().getHeaders();
            System.out.println("Subscribed: " + headers.get("simpSessionId"));
            /*String destination = subscribeEvent.getMessage().getHeaders().get("simpDestination").toString();
            Integer questionId = Integer.parseInt(destination.substring(6));
            messagingTemplate.convertAndSend(destination, chatService.getMessages(questionId).toArray());
            System.out.println("SENDING MESSAGES: " + chatService.getMessages(questionId).size());*/
        }
        if (applicationEvent.getClass() == SessionUnsubscribeEvent.class) {
            System.out.println("UNSUBSCRIBED");
        }
    }
}