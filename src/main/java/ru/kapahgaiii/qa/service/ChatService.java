package ru.kapahgaiii.qa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.kapahgaiii.qa.controller.SessionController;
import ru.kapahgaiii.qa.domain.Message;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.domain.User;
import ru.kapahgaiii.qa.domain.Vote;
import ru.kapahgaiii.qa.dto.ChatMessage;
import ru.kapahgaiii.qa.core.objects.Subscriber;
import ru.kapahgaiii.qa.other.VoteType;
import ru.kapahgaiii.qa.repository.ChatDAO;
import ru.kapahgaiii.qa.repository.QuestionDAO;
import ru.kapahgaiii.qa.repository.UserDAO;

import javax.transaction.Transactional;
import java.util.*;

@Service("ChatService")
public class ChatService {

    @Autowired
    private QuestionDAO questionDAO;

    @Autowired
    private ChatDAO chatDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    SessionController sessionController;


    public ChatMessage addMessage(Message message) {
        return chatDAO.addMessage(message);
    }

    public Question getQuestionById(Integer id) {
        return questionDAO.getQuestionById(id);
    }

    public List<Question> getQuestionsList() {
        return questionDAO.getQuestionsList();
    }

    public List<ChatMessage> getMessageDTOsList(Question question) {
        return chatDAO.getMessageDTOs(question);
    }

    public ChatMessage getMessageDTO(Question question, Integer number) {
        return chatDAO.getMessageDTO(question, number);
    }

    public Message getMessage(Question question, Integer number) {
        return chatDAO.getMessage(question, number);
    }

    public Set<Integer> getVotes(Question question, User user) {
        return chatDAO.getVotes(question, user);

    }

    @Transactional
    public boolean vote(User user, Message message) {
        Vote vote = chatDAO.getUserVote(user, message);
        if (vote == null) {
            vote = new Vote();
            vote.setUser(user);
            vote.setMessage(message);
            vote.setVoteType(VoteType.MESSAGE);

            user.addReputation(1);
            message.addVotes(1);

            chatDAO.updateMessage(message);
            chatDAO.saveVote(vote);
            userDAO.updateUser(user);
            return true;
        } else {
            message.addVotes(-1);
            user.addReputation(-1);
            chatDAO.updateMessage(message);
            chatDAO.deleteVote(vote);
            userDAO.updateUser(user);
            return false;
        }
    }

    public Set<Subscriber> getChatSubscribers(Question question) {
        return sessionController.getChatSubscribers(question);
    }

}
