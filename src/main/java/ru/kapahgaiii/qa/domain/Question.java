package ru.kapahgaiii.qa.domain;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue
    @Column(name = "question_id")
    private Integer questionId;

    @ManyToOne
    @JoinColumn(name = "uid")
    private User user;


    @Column(name = "title")
    private String title;

    @Column(name = "text")
    private String text;

    @Column(name = "votes")
    private Integer votes = 0;

    @Column(name = "messages")
    private Integer messages = 0;

    @Column(name = "updated_time")
    private Timestamp updatedTime;

    @Column(name = "asked_time")
    private Timestamp askedTime;


    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "question_tags",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<Tag>();

    public Question() {
        this.askedTime = new Timestamp((new Date()).getTime());
        this.updatedTime = this.askedTime;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer id) {
        this.questionId = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getVotes() {
        return votes;
    }

    public void setVotes(Integer votes) {
        this.votes = votes;
    }

    public void incrementVotes() {
        this.votes++;
    }

    public void decrementVotes() {
        this.votes--;
    }

    public Integer getMessages() {
        return messages;
    }

    public void incrementMessages(){
        messages++;
    }

    public void setMessages(Integer messages) {
        this.messages = messages;
    }

    public Timestamp getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Timestamp updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Timestamp getAskedTime() {
        return askedTime;
    }

    public void setAskedTime(Timestamp askedTime) {
        this.askedTime = askedTime;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Question question = (Question) o;

        if (questionId != null ? !questionId.equals(question.questionId) : question.questionId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return questionId != null ? questionId.hashCode() : 0;
    }

}
