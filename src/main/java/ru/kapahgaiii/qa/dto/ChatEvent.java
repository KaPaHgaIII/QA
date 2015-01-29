package ru.kapahgaiii.qa.dto;

import ru.kapahgaiii.qa.core.objects.Subscriber;

public class ChatEvent {

    private String action;
    
    private Subscriber subscriber;

    private String username;

    private Integer number;

    private boolean result;

    public ChatEvent() {
    }

    public ChatEvent(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
