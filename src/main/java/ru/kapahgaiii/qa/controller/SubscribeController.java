package ru.kapahgaiii.qa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.dto.ChatEvent;
import ru.kapahgaiii.qa.dto.Subscriber;
import ru.kapahgaiii.qa.service.ChatService;
import ru.kapahgaiii.qa.service.UserService;

import java.util.HashMap;
import java.util.Map;

@Controller
public class SubscribeController implements ApplicationListener<ApplicationEvent> {

    //track only chat messages subscription
    private Map<String, Subscription> subscriptions = new HashMap<String, Subscription>();

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    UserService userService;

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent.getClass() == SessionSubscribeEvent.class) {

            //getting event headers
            SessionSubscribeEvent subscribeEvent = (SessionSubscribeEvent) applicationEvent;
            StompHeaderAccessor headers = StompHeaderAccessor.wrap(subscribeEvent.getMessage());

            String destination = headers.getDestination();
            String sessionId = headers.getSessionId();

            //regex isn't the quickest thing in the world
            if (destination.substring(0, 8).equals("/chat/me")) { //subscribed to chat messages

                //get question
                Integer questionId = Integer.parseInt(destination.substring(15));
                Question question = chatService.getQuestionById(questionId);

                //create Subscription
                Subscription subscription = new Subscription();
                subscription.setDestination(destination);
                subscription.setSessionId(sessionId);
                subscription.setSubscriptionId(headers.getSubscriptionId());

                //create and save Subscriber
                Subscriber subscriber = new Subscriber(sessionId);
                if (headers.getUser() != null) {
                    subscriber.setUser(userService.findByUsername(headers.getUser().getName()));
                }
                chatService.addChatSubscriber(question, subscriber);

                //save subscription
                subscription.setSubscriber(subscriber);
                subscriptions.put(sessionId, subscription);

                //sending event
                ChatEvent chatEvent = new ChatEvent("subscribe");
                chatEvent.setSubscriber(subscriber);
                messagingTemplate.convertAndSend("/chat/events/" + questionId, chatEvent);
            }
        } else if (applicationEvent.getClass() == SessionUnsubscribeEvent.class) {
            unsubscribeFromChat((SessionUnsubscribeEvent) applicationEvent);
        } else if (applicationEvent.getClass() == SessionDisconnectEvent.class) {
            unsubscribeFromChat((SessionDisconnectEvent) applicationEvent);
        }
    }

    private void unsubscribeFromChat(AbstractSubProtocolEvent event) {

        //getting headers
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        //getting subscription
        Subscription subscription = subscriptions.get(headers.getSessionId());

        //unsubscribed from messages subscription || disconnect
        if (subscription != null && (subscription.getSubscriptionId().equals(headers.getSubscriptionId()) ||
                event.getClass() == SessionDisconnectEvent.class)) {

            //getting question
            Integer questionId = Integer.parseInt(subscription.getDestination().substring(15));
            Question question = chatService.getQuestionById(questionId);

            //remove subscriber
            chatService.removeChatSubscriber(question, subscription.getSubscriber());

            //sending event
            ChatEvent chatEvent = new ChatEvent("unsubscribe");
            chatEvent.setSubscriber(subscription.getSubscriber());
            messagingTemplate.convertAndSend("/chat/events/" + questionId, chatEvent);
//            System.out.println("sending");

            //remove subscription from map
            subscriptions.remove(subscription.getSessionId());
        }

    }

    private class Subscription {
        private String sessionId;
        private String destination;
        private String subscriptionId;
        private Subscriber subscriber;


        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public String getSubscriptionId() {
            return subscriptionId;
        }

        public void setSubscriptionId(String subscriptionId) {
            this.subscriptionId = subscriptionId;
        }

        public Subscriber getSubscriber() {
            return subscriber;
        }

        public void setSubscriber(Subscriber subscriber) {
            this.subscriber = subscriber;
        }
    }
}