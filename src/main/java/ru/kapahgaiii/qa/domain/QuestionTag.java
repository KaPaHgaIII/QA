package ru.kapahgaiii.qa.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "question_tags")
@IdClass(QuestionTag.QTPK.class)
public class QuestionTag {

    @Id
    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @Id
    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag;

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

    public static class QTPK implements Serializable {
        private Question question;
        private Tag tag;

        public QTPK() {
        }

        public QTPK(Question question, Tag tag) {
            this.question = question;
            this.tag = tag;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

}
