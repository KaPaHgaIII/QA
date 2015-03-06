package ru.kapahgaiii.qa.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "restore_password")
public class RestorePassword implements Serializable {

    @Id
    @OneToOne
    @JoinColumn(name = "uid")
    private User user;

    @Column(name = "hash")
    private String hash;

    @Column(name = "time")
    private Timestamp time = new Timestamp(new Date().getTime());


    public RestorePassword() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }
}
