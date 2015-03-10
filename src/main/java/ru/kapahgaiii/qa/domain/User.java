package ru.kapahgaiii.qa.domain;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    private static PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // for password encoding

    @Id
    @GeneratedValue
    @Column(name = "uid")
    private Integer uid;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "vk_uid")
    private Integer vkUid;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", cascade = CascadeType.ALL)
    private Set<UserRole> userRoles = new HashSet<UserRole>();

    @Column(name = "reputation")
    private Integer reputation = 0;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "interesting_tags",
            joinColumns = @JoinColumn(name = "uid"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> interestingTags = new HashSet<Tag>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "favourite_questions",
            joinColumns = @JoinColumn(name = "uid"),
            inverseJoinColumns = @JoinColumn(name = "question_id"))
    private Set<Question> favouriteQuestions = new HashSet<Question>();

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void encodeAndSetPassword(String password) {
        this.password = passwordEncoder.encode(password);
    }

    public Integer getVkUid() {
        return vkUid;
    }

    public void setVkUid(Integer vkUid) {
        this.vkUid = vkUid;
    }

    public void setUserRoles(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public Set<UserRole> getUserRoles() {
        return userRoles;
    }

    public void addRole(UserRole role) {
        userRoles.add(role);
    }

    public Integer getReputation() {
        return reputation;
    }

    public void setReputation(Integer reputation) {
        this.reputation = reputation;
    }

    public Set<Tag> getInterestingTags() {
        return interestingTags;
    }

    public void setInterestingTags(Set<Tag> interestingTags) {
        this.interestingTags = interestingTags;
    }

    public Set<Question> getFavouriteQuestions() {
        return favouriteQuestions;
    }

    public void setFavouriteQuestions(Set<Question> favouriteQuesitons) {
        this.favouriteQuestions = favouriteQuesitons;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!uid.equals(user.uid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }
}
