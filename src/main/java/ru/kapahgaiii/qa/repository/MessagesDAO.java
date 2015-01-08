package ru.kapahgaiii.qa.repository;

import ru.kapahgaiii.qa.domain.ChatMessage;
import ru.kapahgaiii.qa.domain.Question;

import java.util.List;

public interface MessagesDAO {

    public void addMessage(ChatMessage message);

    public List<ChatMessage> getMessages(Question question);

}
