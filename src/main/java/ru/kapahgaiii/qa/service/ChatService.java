package ru.kapahgaiii.qa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.kapahgaiii.qa.controller.SessionController;
import ru.kapahgaiii.qa.core.objects.Subscriber;
import ru.kapahgaiii.qa.domain.Message;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.domain.User;
import ru.kapahgaiii.qa.domain.Vote;
import ru.kapahgaiii.qa.dto.MessageDTO;
import ru.kapahgaiii.qa.dto.QuestionDTO;
import ru.kapahgaiii.qa.other.VoteType;
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
        message.getQuestion().incrementMessages();
        chatDAO.updateQuestion(message.getQuestion());
        chatDAO.saveMessage(message);
    }

    public Question getQuestionById(Integer id) {
        return chatDAO.getQuestionById(id);
    }

    public List<MessageDTO> getMessageDTOsList(Question question) {
        return chatDAO.getMessageDTOs(question);
    }

    public MessageDTO getMessageDTO(Question question, Integer number) {
        return chatDAO.getMessageDTO(question, number);
    }

    public Message getMessage(Question question, Integer number) {
        return chatDAO.getMessage(question, number);
    }

    public Set<Integer> getVotes(Question question, User user) {
        return chatDAO.getMessagesUserVotes(question, user);

    }

    @Transactional
    public boolean vote(User user, Message message) {
        Vote vote = chatDAO.getMessageVote(user, message);
        if (vote == null) {
            vote = new Vote();
            vote.setUser(user);
            vote.setMessage(message);
            vote.setVoteType(VoteType.MESSAGE);

            User author = message.getUser();
            author.addReputation(2);
            message.incrementVotes();

            chatDAO.updateMessage(message);
            chatDAO.saveVote(vote);
            userDAO.updateUser(author);
            return true;
        } else {
            message.decrementVotes();
            User author = message.getUser();
            author.addReputation(-2);
            chatDAO.updateMessage(message);
            chatDAO.deleteVote(vote);
            userDAO.updateUser(author);
            return false;
        }
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
        Vote vote = chatDAO.getQuestionVote(user, question);
        User author = question.getUser();
        if (vote == null) { //adding vote
            vote = new Vote();
            vote.setVoteType(VoteType.QUESTION);
            vote.setQuestion(question);
            vote.setSign(sign);
            vote.setUser(user);

            author.addReputation(10 * sign);

            if (sign > 0) {
                question.incrementVotes();
            } else {
                question.decrementVotes();
            }

            chatDAO.saveVote(vote);
        } else if (vote.getSign() != sign) { //changing vote
            if (sign > 0) {
                question.incrementVotes();
                question.incrementVotes();
            } else {
                question.decrementVotes();
                question.decrementVotes();
            }
            vote.setSign(sign);
            author.addReputation(20 * sign);
            chatDAO.updateVote(vote);
        } else { //deleting vote
            if (vote.getSign() > 0) {
                question.decrementVotes();
            } else {
                question.incrementVotes();
            }
            author.addReputation(-10 * vote.getSign());
            chatDAO.deleteVote(vote);
            vote = null;
        }
        chatDAO.updateQuestion(question);
        userDAO.updateUser(author);
        return vote != null;
    }

    public Vote getQuestionVote(User user, Question question) {
        return chatDAO.getQuestionVote(user, question);
    }

}
