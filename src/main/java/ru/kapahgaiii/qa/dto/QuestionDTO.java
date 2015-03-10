package ru.kapahgaiii.qa.dto;

import ru.kapahgaiii.qa.core.tools.StringMaster;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.domain.Tag;

import java.sql.Timestamp;
import java.util.Set;

public class QuestionDTO {

    private Integer id;

    private String username;

    private String title;

    private String text;

    private Integer votes;

    private Integer messages;

    private Integer subscribers;

    private Timestamp updatedTime;

    private Set<Tag> tags;

    private String tagsString;

    public QuestionDTO() {
    }

    public QuestionDTO(Question question, boolean text, boolean nl2br) {
        this.id = question.getQuestionId();
        this.username = question.getUser().getUsername();
        this.title = question.getTitle();

        if (text) {
            this.text = nl2br ? question.getText().replace("\n", "<br>") : question.getText();
        }
        this.votes = question.getVotes();
        this.messages = question.getMessages();
        this.updatedTime = question.getUpdatedTime();
        this.tags = question.getTags();

        this.tagsString = StringMaster.join(this.tags, ", ");

    }

    public QuestionDTO(Question question, boolean text) {
        this(question, text, false);
    }

    public QuestionDTO(Question question) {
        this(question, false, false);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Integer getVotes() {
        return votes;
    }

    public void setVotes(Integer votes) {
        this.votes = votes;
    }

    public Integer getMessages() {
        return messages;
    }

    public void setMessages(Integer messages) {
        this.messages = messages;
    }

    public Integer getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(Integer subscribers) {
        this.subscribers = subscribers;
    }

    public Timestamp getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Timestamp updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public String getTagsString() {
        return tagsString;
    }

    public void setTagsString(String tagsString) {
        this.tagsString = tagsString;
    }
}
