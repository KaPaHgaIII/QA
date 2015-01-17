package ru.kapahgaiii.qa.repository;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.kapahgaiii.qa.domain.Message;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.domain.User;
import ru.kapahgaiii.qa.domain.Vote;
import ru.kapahgaiii.qa.dto.ChatMessage;
import ru.kapahgaiii.qa.other.VoteType;

import javax.transaction.Transactional;
import java.util.*;

@Repository
@Transactional
public class ChatDAOImpl implements ChatDAO {

    @Autowired
    private SessionFactory sessionFactory;

    private Map<Question, List<ChatMessage>> messages = new HashMap<Question, List<ChatMessage>>();

    @Override
    public ChatMessage addMessage(Message message) {
        Question question = message.getQuestion();
        if (!messages.containsKey(question)) {
            loadMessagesFromDB(question);
        }
        sessionFactory.getCurrentSession().save(message);

        ChatMessage messageDTO = new ChatMessage(message);

        messages.get(question).add(messageDTO);

        return messageDTO;
    }

    @Override
    public List<ChatMessage> getMessageDTOs(Question question) {
        if (!messages.containsKey(question)) {
            loadMessagesFromDB(question);
        }
        return messages.get(question);
    }

    @Override
    public ChatMessage getMessageDTO(Question question, Integer number) {
        return getMessageDTOs(question).get(number);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Message getMessage(Question question, Integer number) {
        List<Message> votes = sessionFactory.getCurrentSession()
                .createQuery("from Message where question=:question and number=:number")
                .setParameter("question", question).setParameter("number", number)
                .list();

        if (votes.isEmpty()) {
            return null;
        } else {
            return votes.get(0);
        }
    }

    @Override
    public void updateMessage(Message message) {
        sessionFactory.getCurrentSession().merge(message);
        messages.get(message.getQuestion()).set(message.getNumber(), new ChatMessage(message));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Vote getUserVote(User user, Message message) {
        List<Vote> votes = sessionFactory.getCurrentSession()
                .createQuery("from Vote where user=:user and message=:message")
                .setParameter("user", user).setParameter("message", message)
                .list();

        if (votes.isEmpty()) {
            return null;
        } else {
            return votes.get(0);
        }

    }

    @Override
    public void saveVote(Vote vote) {
        sessionFactory.getCurrentSession().save(vote);
    }

    @Override
    public void deleteVote(Vote vote) {
        sessionFactory.getCurrentSession().delete(vote);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Integer> getVotes(Question question, User user) {
        List<Integer> numbers = sessionFactory.getCurrentSession()
                .createQuery("select m.number from Vote as v left join v.message as m " +
                        "where m.question=:question and v.user=:user and v.voteType=:voteType")
                .setParameter("question", question)
                .setParameter("user", user)
                .setParameter("voteType", VoteType.MESSAGE)
                .list();
        return new HashSet<Integer>(numbers);

    }

    @SuppressWarnings("unchecked")
    private void loadMessagesFromDB(Question question) {
        List<Message> messagesList = sessionFactory.getCurrentSession()
                .createQuery("from Message where question=:question")
                .setParameter("question", question)
                .list();

        List<ChatMessage> messageDTOs = new ArrayList<ChatMessage>();

        for (Message message : messagesList) {
            messageDTOs.add(message.getNumber(), new ChatMessage(message));
        }

        Message.setLastNumber(question, messageDTOs.size());

        messages.put(question, messageDTOs);
    }
}
