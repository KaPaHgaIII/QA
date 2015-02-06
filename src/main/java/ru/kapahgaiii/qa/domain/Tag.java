package ru.kapahgaiii.qa.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @Column(name = "tag_id")
    private Integer tagId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "real_id")
//    private Tag realTag;
//
//    @OneToMany(mappedBy = "realTag")
//    private Set<Tag> aliases = new HashSet<Tag>();

    @Column(name = "value")
    private String value;

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    /*public Tag getRealTag() {
        return realTag;
    }

    public void setRealTag(Tag realTag) {
        this.realTag = realTag;
    }

    public Set<Tag> getAliases() {
        return aliases;
    }

    public void setAliases(Set<Tag> aliases) {
        this.aliases = aliases;
    }*/

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
