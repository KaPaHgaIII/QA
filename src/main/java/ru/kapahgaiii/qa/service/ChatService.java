package ru.kapahgaiii.qa.service;

import org.apache.commons.lang3.StringEscapeUtils;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service("ChatService")
public class ChatService {

    @Autowired
    private SocketService socketService;

    @Autowired
    private ChatDAO chatDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserService userService;

    @Autowired
    private SessionController sessionController;

    @Autowired
    private NotificationsService notificationsService;


    public Message addMessage(Question question, User user, MessageDTO messageDTO) {
        Message message = new Message(question, user, StringEscapeUtils.escapeHtml4(messageDTO.getText()));
        if (messageDTO.getAddressee() != null) {
            message.setAddressee(userService.findByUsername(messageDTO.getAddressee()));
        }
        chatDAO.saveMessage(message);
        message.getQuestion().incrementMessages();

        socketService.sendChatMessage(message);
        socketService.sendQuestionInfo("messages", question, question.getMessages());
        socketService.sendNotification(notificationsService.createNewMessageNotification(message));
        if (message.getAddressee() != null) {
            socketService.sendNotification(notificationsService.createAddressedMessageNotification(message),
                    message.getAddressee().getUsername());
        }

        return message;
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

    public List<QuestionDTO> getQuestionDTOsList(Timestamp time, Integer[] exclude, String searchQuery, String[] tags) {
        List<Question> questions = chatDAO.getQuestionsList(time, exclude, searchQuery, tags);
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

    public Tag getTagByName(String name) {
        return chatDAO.getTagByName(name);
    }

    public boolean isFavouriteQuestion(Question question, User user) {
        return user.getFavouriteQuestions().contains(question);
    }

    public boolean addToFavourite(Question question, User user) {
        if (!isFavouriteQuestion(question, user)) {
            user.getFavouriteQuestions().add(question);
            userDAO.updateUser(user);
            return true;
        } else {
            user.getFavouriteQuestions().remove(question);
            userDAO.updateUser(user);
            return false;
        }
    }

    public Set<Tag> parseTagsString(String s) {
        String[] names = s.split(",");
        Set<Tag> result = new HashSet<Tag>();
        for (String name : names) {
            String correctName = StringEscapeUtils.escapeHtml4(name).trim();
            if (!correctName.equals("")) {
                Tag tag = chatDAO.getTagByName(correctName);
                if (tag == null) {
                    tag = new Tag(correctName);
                    chatDAO.addTag(tag);
                }
                result.add(tag);
            }
        }
        return result;
    }

    public Question createQuestion(User user, String title, String text, Set<Tag> tags) {
        Question question = new Question();
        question.setUser(user);
        question.setTitle(StringEscapeUtils.escapeHtml4(title));
        question.setText(StringEscapeUtils.escapeHtml4(text));
        question.getTags().addAll(tags);
        chatDAO.saveQuestion(question);

        socketService.sendNotification(notificationsService.createNewQuestionNotification(question));

        return question;
    }

    public void editQuestion(Question question, String title, String text, Set<Tag> tags) {
        question.setTitle(StringEscapeUtils.escapeHtml4(title));
        question.setText(StringEscapeUtils.escapeHtml4(text));
        question.getTags().clear();
        question.getTags().addAll(tags);
        chatDAO.updateQuestion(question);
    }

}
