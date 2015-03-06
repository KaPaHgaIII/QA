package ru.kapahgaiii.qa.dto;

import ru.kapahgaiii.qa.domain.Message;

import java.sql.Timestamp;
import java.util.Date;

public class MessageDTO {

    private Integer message_id;

    private Integer number;

    private String text;

    private String username;

    private String addressee;

    private Integer votes;

    private Timestamp time = new Timestamp(new Date().getTime());

    public MessageDTO() {
    }

    public MessageDTO(Message message) {
        this.message_id = message.getMessageId();
        this.number = message.getNumber();
        this.text = message.getText();
        this.username = message.getUser().getUsername();
        this.votes = message.getVotes();
        this.time = message.getTime();
        this.addressee = message.getAddressee()!= null ? message.getAddressee().getUsername() : null;
    }

    public Integer getMessage_id() {
        return message_id;
    }

    public void setMessage_id(Integer message_id) {
        this.message_id = message_id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
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

    public String getAddressee() {
        return addressee;
    }

    public void setAddressee(String addressee) {
        this.addressee = addressee;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public Integer getVotes() {
        return votes;
    }

    public void setVotes(Integer votes) {
        this.votes = votes;
    }
}
