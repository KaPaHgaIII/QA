package ru.kapahgaiii.qa.repository.interfaces;

import ru.kapahgaiii.qa.domain.Tag;

import java.util.List;

public interface TagDAO {

    public List<Tag> getTags(String s);

    public Tag getTagByName(String name);

    public void addTag(Tag tag);
}
