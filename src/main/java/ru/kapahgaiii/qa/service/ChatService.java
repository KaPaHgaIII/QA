package ru.kapahgaiii.qa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.kapahgaiii.qa.controller.SessionController;
import ru.kapahgaiii.qa.core.objects.Subscriber;
import ru.kapahgaiii.qa.domain.*;
import ru.kapahgaiii.qa.dto.MessageDTO;
import ru.kapahgaiii.qa.dto.QuestionDTO;
import ru.kapahgaiii.qa.repository.interfaces.ChatDAO;
import ru.kapahgaiii.qa.repository.interfaces.UserDAO;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service("ChatService")
public class ChatService {

    @Autowired
    private ChatDAO chatDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    SessionController sessionController;


    public void addMessage(Message message) {
        chatDAO.saveMessage(message);
        message.getQuestion().incrementMessages();
    }

    public Question getQuestionById(Integer id) {
        return chatDAO.getQuestionById(id);
    }

    public List<MessageDTO> getMessageDTOsList(Question question) {
        return chatDAO.getMessageDTOs(question);
    }

    public Message getMessage(Question question, Integer number) {
        return chatDAO.getMessage(question, number);
    }

    public Set<Integer> getVotes(Question question, User user) {
        return chatDAO.getMessagesUserVotes(question, user);

    }

    @Transactional
    public boolean vote(User user, Message message) {
        return chatDAO.voteMessage(user, message);
    }

    public Set<Subscriber> getChatSubscribers(Question question) {
        return sessionController.getChatSubscribers(question);
    }

    public List<QuestionDTO> getQuestionDTOsList(Timestamp time, Integer[] exclude) {
        List<Question> questions = chatDAO.getQuestionsList(time, exclude);
        List<QuestionDTO> questionDTOs = new ArrayList<QuestionDTO>();
        for (Question question : questions) {
            QuestionDTO questionDTO = new QuestionDTO(question);
            questionDTO.setSubscribers(sessionController.getChatSubscribersCount(question));
            questionDTOs.add(questionDTO);
        }
        return questionDTOs;
    }

    @Transactional
    public boolean vote(User user, Question question, int sign) {
        return chatDAO.voteQuestion(user, question, sign);
    }

    public Vote getQuestionVote(User user, Question question) {
        return chatDAO.getQuestionVote(user, question);
    }

    public List<Tag> getTags(String s) {
        return chatDAO.getTags(s);
    }

}
