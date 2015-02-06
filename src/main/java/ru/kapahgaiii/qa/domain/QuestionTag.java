package ru.kapahgaiii.qa.domain;

import javax.persistence.*;

@Entity
@Table(name = "question_tags")
public class QuestionTag {

    @Id
    @Column(name = "question_tag_id")
    private Integer questionTagId;

    @ManyToOne
    private Question question;

    @ManyToOne
    private Tag tag;

    public Integer getQuestionTagId() {
        return questionTagId;
    }

    public void setQuestionTagId(Integer questionTagId) {
        this.questionTagId = questionTagId;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
}
