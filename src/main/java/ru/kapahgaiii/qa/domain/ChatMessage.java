package ru.kapahgaiii.qa.domain;

import javax.persistence.Entity;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
public class ChatMessage {

    private static ConcurrentHashMap<Question, AtomicInteger> numbers = new ConcurrentHashMap<Question, AtomicInteger>();

    private Question question;

    private Integer number;

    private String text;

    private String username;

    private long time = new Date().getTime();

    public ChatMessage(Question question) {
        numbers.putIfAbsent(question, new AtomicInteger(0));
        this.number = numbers.get(question).getAndIncrement();
    }

    public ChatMessage(Question question, String username, String text) {
        this(question);
        this.question = question;
        this.username = username;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}