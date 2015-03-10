package ru.kapahgaiii.qa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import ru.kapahgaiii.qa.core.objects.Session;
import ru.kapahgaiii.qa.core.objects.Subscriber;
import ru.kapahgaiii.qa.core.objects.Subscription;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.service.ChatService;
import ru.kapahgaiii.qa.service.SocketService;
import ru.kapahgaiii.qa.service.UserService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class SessionController implements ApplicationListener<ApplicationEvent> {

    private Map<String, AtomicInteger> usersOnline = new ConcurrentHashMap<String, AtomicInteger>();
    private AtomicInteger guestsOnline = new AtomicInteger(0);

    private Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();

    private Map<Question, Set<Session>> chatSessions = new HashMap<Question, Set<Session>>();

    @Autowired
    private ChatService chatService;

    @Autowired
    SocketService socketService;

    @Autowired
    UserService userService;

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {

        if (applicationEvent.getClass() == SessionConnectEvent.class) {
            SessionConnectEvent event = (SessionConnectEvent) applicationEvent;
            StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
            incrementOnline(headers);
            addSession(headers);
        } else if (applicationEvent.getClass() == SessionSubscribeEvent.class) {
            SessionSubscribeEvent event = (SessionSubscribeEvent) applicationEvent;
            StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
            try {
                if (headers.getDestination().substring(0, 15).equals("/chat/messages/")) { //subscribed to chat messages
                    subscribeToChat(headers);
                }
            } catch (IndexOutOfBoundsException ignored) {}
        } else if (applicationEvent.getClass() == SessionUnsubscribeEvent.class) {
            SessionUnsubscribeEvent event = (SessionUnsubscribeEvent) applicationEvent;
            StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());

            Subscription subscription = sessions.get(headers.getSessionId()).getSubscriptions()
                    .get(headers.getSubscriptionId());

            if (subscription != null) {
                try {
                    if (subscription.getDestination().substring(0, 15).equals("/chat/messages/")) {
                        unsubscribeFromChat(headers);
                    }
                } catch (IndexOutOfBoundsException ignored) {}
            }

        } else if (applicationEvent.getClass() == SessionDisconnectEvent.class) {
            SessionDisconnectEvent event = (SessionDisconnectEvent) applicationEvent;
            StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
            decrementOnline(headers);

            //search for chat messages subscription
            for (Subscription subscription : sessions.get(headers.getSessionId()).getSubscriptions().values()) {
                try {
                    if (subscription.getDestination().substring(0, 15).equals("/chat/messages/")) {
                        headers.setSubscriptionId(subscription.getSubscriptionId());
                        unsubscribeFromChat(headers);
                    }
                } catch (IndexOutOfBoundsException ignored) {}
            }

            removeSession(headers);
        }
    }

    private void addSession(StompHeaderAccessor headers) {
        //create Subscriber
        //sessionId is automatically set in Session.setSubscriber method.
        Subscriber subscriber = new Subscriber();
        if (headers.getUser() != null) {
            subscriber.setUsername(headers.getUser().getName());
        }
        //create session object
        Session session = new Session();
        session.setSessionId(headers.getSessionId());
        session.setSubscriber(subscriber);
        //save session
        sessions.put(session.getSessionId(), session);
    }

    private void subscribeToChat(StompHeaderAccessor headers) {
        String destination = headers.getDestination();
        String sessionId = headers.getSessionId();

        //get question
        Integer questionId = Integer.parseInt(destination.substring(15));
        Question question = chatService.getQuestionById(questionId);

        //create Subscription
        Subscription subscription = new Subscription();
        subscription.setDestination(destination);
        subscription.setSessionId(sessionId);
        subscription.setSubscriptionId(headers.getSubscriptionId());

        Session session = sessions.get(headers.getSessionId());
        session.addSubscription(subscription);

        //save basicSubscription
        chatSessions.putIfAbsent(question, new HashSet<Session>());
        chatSessions.get(question).add(session);

        //send event
        socketService.sendUserSubscribedToChat(question, session.getSubscriber());
        socketService.sendQuestionInfo("subscribers", question, getChatSubscribersCount(question));
    }

    private void unsubscribeFromChat(StompHeaderAccessor headers) {

        //getting session
        Session session = sessions.get(headers.getSessionId());

        //getting basicSubscription
        Subscription subscription = session.getSubscriptions().get(headers.getSubscriptionId());

        //getting question
        Integer questionId = Integer.parseInt(subscription.getDestination().substring(15));
        Question question = chatService.getQuestionById(questionId);

        //remove chat basicSubscription
        session.removeSubscription(subscription);
        chatSessions.get(question).remove(session);

        //clear
        if (chatSessions.get(question).isEmpty()) {
            chatSessions.remove(question);
        }

        //send event
        socketService.sendUserUnsubscribedFromChat(question, session.getSubscriber());
        socketService.sendQuestionInfo("subscribers", question, getChatSubscribersCount(question));
    }

    private void removeSession(StompHeaderAccessor headers) {
        sessions.remove(headers.getSessionId());
    }

    public Set<Subscriber> getChatSubscribers(Question question) {
        chatSessions.putIfAbsent(question, new HashSet<Session>());
        Set<Subscriber> subscribers = new HashSet<Subscriber>();
        for (Session session : chatSessions.get(question)) {
            subscribers.add(session.getSubscriber());
        }
        return subscribers;
    }

    //could be slow when many people online
    public int getChatSubscribersCount(Question question) {
        if (chatSessions.get(question) == null) {
            return 0;
        }
        int count = 0;
        Set<String> authorized = new HashSet<String>();
        for (Session session : chatSessions.get(question)) {
            Subscriber subscriber = session.getSubscriber();
            if (subscriber.getUsername() != null) {
                authorized.add(subscriber.getUsername());
            } else {
                count++; //anonymous
            }
        }
        return count + authorized.size();

    }

    private void incrementOnline(StompHeaderAccessor headers) {
        if (headers.getUser() != null) {
            usersOnline.putIfAbsent(headers.getUser().getName(), new AtomicInteger(0));
            usersOnline.get(headers.getUser().getName()).incrementAndGet();
        } else {
            guestsOnline.incrementAndGet();
        }
        socketService.sendOnline(getUsersOnline(), getGuestsOnline());
    }

    private void decrementOnline(StompHeaderAccessor headers) {
        if (sessions.get(headers.getSessionId()).getSubscriber().getUsername() != null) {
            if (usersOnline.get(headers.getUser().getName()).decrementAndGet() == 0) {
                usersOnline.remove(headers.getUser().getName());
            }
        } else {
            guestsOnline.decrementAndGet();
        }
        socketService.sendOnline(getUsersOnline(), getGuestsOnline());
    }

    public int getGuestsOnline() {
        return guestsOnline.get();
    }

    public int getUsersOnline() {
        return usersOnline.size();
    }
}