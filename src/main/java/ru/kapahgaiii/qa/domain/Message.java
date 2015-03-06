package ru.kapahgaiii.qa.domain;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@Table(name = "messages")
public class Message {

    private static ConcurrentHashMap<Question, AtomicInteger> numbers = new ConcurrentHashMap<Question, AtomicInteger>();

    @Id
    @GeneratedValue
    @Column(name = "message_id")
    private Integer messageId;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(name = "number")
    private Integer number;

    @Column(name = "text")
    private String text;

    @ManyToOne
    @JoinColumn(name = "uid")
    private User user;

    @ManyToOne
    @JoinColumn(name = "addressee")
    private User addressee;

    @Column(name = "time")
    private Timestamp time = new Timestamp(new Date().getTime());

    @Column(name = "votes")
    private Integer votes = 0;

    public Message() {
    }

    public Message(Question question) {
        numbers.putIfAbsent(question, new AtomicInteger(0));
        this.number = numbers.get(question).getAndIncrement();
    }

    public Message(Question question, User user, String text) {
        this(question);
        this.question = question;
        this.user = user;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getAddressee() {
        return addressee;
    }

    public void setAddressee(User addressee) {
        this.addressee = addressee;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Integer getNumber() {
        return number;
    }

    public Integer getVotes() {
        return votes;
    }

    public void setVotes(Integer votes) {
        this.votes = votes;
    }

    public void incrementVotes() {
        votes++;
    }

    public void decrementVotes() {
        votes--;
    }

    public static void setLastNumber(Question question, Integer number) {
        numbers.putIfAbsent(question, new AtomicInteger(0));
        numbers.get(question).set(number);
    }
}