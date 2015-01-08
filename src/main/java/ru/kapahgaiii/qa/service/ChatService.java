package ru.kapahgaiii.qa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.kapahgaiii.qa.domain.ChatMessage;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.repository.MessagesDAO;
import ru.kapahgaiii.qa.repository.QuestionDAO;

import java.util.List;

@Service("ChatService")
public class ChatService {

    @Autowired
    private QuestionDAO questionDAO;

    @Autowired
    private MessagesDAO messagesDAO;

    public void addMessage(ChatMessage message) {
        messagesDAO.addMessage(message);
    }

    public Question getQuestionById(Integer id) {
        return questionDAO.getQuestionById(id);
    }

    public List<Question> getQuestionsList() {
        return questionDAO.getQuestionsList();
    }

    public List<ChatMessage> getMessagesList(Question question) {
        return messagesDAO.getMessages(question);
    }
}
