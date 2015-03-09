package ru.kapahgaiii.qa.domain;


import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "favourite_questions")
@IdClass(FavouriteQuestion.FQPK.class)
public class FavouriteQuestion {

    @Id
    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @Id
    @ManyToOne
    @JoinColumn(name = "uid")
    private User user;

    public FavouriteQuestion() {
    }

    public FavouriteQuestion(Question question, User user) {
        this.question = question;
        this.user = user;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static class FQPK implements Serializable {
        private Question question;
        private User user;

        public FQPK() {
        }

        public FQPK(Question question, User user) {
            this.question = question;
            this.user = user;
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


