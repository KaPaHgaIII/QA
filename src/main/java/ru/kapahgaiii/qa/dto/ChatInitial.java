package ru.kapahgaiii.qa.dto;

import ru.kapahgaiii.qa.core.objects.Subscriber;

public class ChatInitial {
    private ChatMessage[] messages;
    
    private Integer[] votedNumbers;

    private Subscriber[] subscribers;
    

    public ChatMessage[] getMessages() {
        return messages;
    }

    public void setMessages(ChatMessage[] messages) {
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
