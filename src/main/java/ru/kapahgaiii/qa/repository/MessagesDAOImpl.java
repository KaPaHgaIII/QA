package ru.kapahgaiii.qa.repository;

import org.springframework.stereotype.Repository;
import ru.kapahgaiii.qa.domain.ChatMessage;
import ru.kapahgaiii.qa.domain.Question;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MessagesDAOImpl implements MessagesDAO {
    private Map<Question, List<ChatMessage>> messages = new HashMap<Question, List<ChatMessage>>();

    public void addMessage(ChatMessage message) {
        Question question = message.getQuestion();
        if (!messages.containsKey(question)) {
            messages.put(question, new ArrayList<ChatMessage>());
        }
        messages.get(question).add(message);
    }

    public List<ChatMessage> getMessages(Question question) {
        if (!messages.containsKey(question)) {
            messages.put(question, new ArrayList<ChatMessage>());
        }
        return messages.get(question);
    }
}
