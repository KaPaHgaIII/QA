package ru.kapahgaiii.qa.core.objects;

import ru.kapahgaiii.qa.domain.User;

// class is used to describe user, connected to the site
// for anonymous we keep only sessionId
// equals and hashcode use only sessionId
// so, we can get multiple Subscribers for one User
// it should be checked in the client side and anywhere else

public class Subscriber {

    private String username;
    
    private String sessionId;

    public Subscriber() {
    }

    public Subscriber(String sessionId) {
        this.sessionId = sessionId;
    }

    public Subscriber(User user, String sessionId) {
        this.username = user.getUsername();
        this.sessionId = sessionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subscriber that = (Subscriber) o;

        if (!sessionId.equals(that.sessionId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return sessionId.hashCode();
    }
}
