package ru.kapahgaiii.qa.domain;


import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "interesting_tags")
@IdClass(InterestingTag.ITPK.class)
public class InterestingTag {

    @Id
    @ManyToOne
    @JoinColumn(name = "uid")
    private User user;

    @Id
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    public InterestingTag() {
    }

    public InterestingTag(User user, Tag tag) {
        this.user = user;
        this.tag = tag;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InterestingTag that = (InterestingTag) o;

        if (!tag.equals(that.tag)) return false;
        if (!user.equals(that.user)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = user.hashCode();
        result = 31 * result + tag.hashCode();
        return result;
    }

    public static class ITPK implements Serializable {
        private User user;
        private Tag tag;

        public ITPK() {
        }

        public ITPK(User user, Tag tag) {
            this.user = user;
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


