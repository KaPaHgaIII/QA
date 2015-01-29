package ru.kapahgaiii.qa.core.objects;

public class Subscription {

    private String sessionId;

    private String destination;

    private String subscriptionId;


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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subscription that = (Subscription) o;

        if (!destination.equals(that.destination)) return false;
        if (!sessionId.equals(that.sessionId)) return false;
        if (!subscriptionId.equals(that.subscriptionId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sessionId.hashCode();
        result = 31 * result + destination.hashCode();
        result = 31 * result + subscriptionId.hashCode();
        return result;
    }
}
