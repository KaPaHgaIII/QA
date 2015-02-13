package ru.kapahgaiii.qa.repository.implementations;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.kapahgaiii.qa.domain.Message;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.domain.User;
import ru.kapahgaiii.qa.domain.Vote;
import ru.kapahgaiii.qa.dto.MessageDTO;
import ru.kapahgaiii.qa.other.VoteType;
import ru.kapahgaiii.qa.repository.interfaces.ChatDAO;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Transactional
public class ChatDAOImpl implements ChatDAO {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void saveMessage(Message message) {
        sessionFactory.getCurrentSession().save(message);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MessageDTO> getMessageDTOs(Question question) {
        List<Message> messagesList = sessionFactory.getCurrentSession()
                .createQuery("from Message where question=:question")
                .setParameter("question", question)
                .list();

        List<MessageDTO> messageDTOs = new ArrayList<MessageDTO>();

        for (Message message : messagesList) {
            messageDTOs.add(message.getNumber(), new MessageDTO(message));
        }

        Message.setLastNumber(question, messageDTOs.size());

        return messageDTOs;
    }

    @Override
    public MessageDTO getMessageDTO(Question question, Integer number) {
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
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<Question> getQuestionsList(Timestamp time, Integer[] exclude) {
        int limit = 15;
        if (exclude == null){
            return sessionFactory.getCurrentSession()
                    .createQuery("from Question where updatedTime <= :time order by updatedTime desc")
                    .setParameter("time", time)
                    .setMaxResults(limit)
                    .list();
        }
        return sessionFactory.getCurrentSession()
                .createQuery("from Question where updatedTime <= :time and id not in (:ids) order by updatedTime desc")
                .setParameter("time", time)
                .setParameterList("ids", exclude)
                .setMaxResults(limit)
                .list();
    }

    @Override
    public Question getQuestionById(Integer id) {
        return (Question) sessionFactory.getCurrentSession().get(Question.class, id);
    }

    @Override
    public void saveQuestion(Question question) {
        sessionFactory.getCurrentSession().save(question);
    }

    @Override
    public void updateQuestion(Question question) {
        sessionFactory.getCurrentSession().merge(question);
    }


    @Override
    @SuppressWarnings("unchecked")
    public Vote getMessageVote(User user, Message message) {
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
    @SuppressWarnings("unchecked")
    public Vote getQuestionVote(User user, Question question) {
        List<Vote> votes = sessionFactory.getCurrentSession()
                .createQuery("from Vote where user=:user and question=:question")
                .setParameter("user", user).setParameter("question", question)
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
    public void updateVote(Vote vote) {
        sessionFactory.getCurrentSession().merge(vote);
    }

    @Override
    public void deleteVote(Vote vote) {
        sessionFactory.getCurrentSession().delete(vote);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Integer> getMessagesUserVotes(Question question, User user) {
        List<Integer> numbers = sessionFactory.getCurrentSession()
                .createQuery("select m.number from Vote as v left join v.message as m " +
                        "where m.question=:question and v.user=:user and v.voteType=:voteType")
                .setParameter("question", question)
                .setParameter("user", user)
                .setParameter("voteType", VoteType.MESSAGE)
                .list();
        return new HashSet<Integer>(numbers);

    }


}
