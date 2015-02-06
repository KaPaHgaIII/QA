package ru.kapahgaiii.qa.dto;

import ru.kapahgaiii.qa.core.objects.Subscriber;

public class ChatInitial {
    private MessageDTO[] messages;
    
    private Integer[] votedNumbers;

    private Subscriber[] subscribers;
    

    public MessageDTO[] getMessages() {
        return messages;
    }

    public void setMessages(MessageDTO[] messages) {
        this.messages = messages;
    }

    public Integer[] getVotedNumbers() {
        return votedNumbers;
    }

    public void setVotedNumbers(Integer[] votedNumbers) {
        this.votedNumbers = votedNumbers;
    }

    public Subscriber[] getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(Subscriber[] subscribers) {
        this.subscribers = subscribers;
    }

}
