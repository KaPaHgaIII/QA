package ru.kapahgaiii.qa.core.objects;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Session {

    private String sessionId;

    private Map<String, Subscription> subscriptions = new ConcurrentHashMap<String, Subscription>();

    private Subscriber subscriber;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void addSubscription(Subscription subscription) {
        subscriptions.put(subscription.getSubscriptionId(), subscription);
    }

    public void removeSubscription(Subscription subscription) {
        subscriptions.remove(subscription.getSubscriptionId());
    }

    public Map<String, Subscription> getSubscriptions() {
        return subscriptions;
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber) {
        subscriber.setSessionId(this.sessionId);
        this.subscriber = subscriber;
    }
}
