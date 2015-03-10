package ru.kapahgaiii.qa.repository.implementations;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.kapahgaiii.qa.domain.*;
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
        if (exclude == null) {
            return sessionFactory.getCurrentSession()
                    .createQuery("from Question where updatedTime <= :time order by updatedTime desc")
                    .setParameter("time", time)
                    .setMaxResults(limit)
                    .list();
        }
        return sessionFactory.getCurrentSession()
                .createQuery("from Question where updatedTime <= :time and questionId not in (:ids) order by updatedTime desc")
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

    @Override
    @SuppressWarnings("unchecked")
    public boolean voteMessage(User user, Message message) {
        List<Object[]> list = sessionFactory.getCurrentSession()
                .createSQLQuery("dbo.vote_message :uid, :message_id")
                .setParameter("uid", user.getUid())
                .setParameter("message_id", message.getMessageId())
                .list();
        message.setVotes((Integer) list.get(0)[1]);
        return (Integer) list.get(0)[0] == 1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean voteQuestion(User user, Question question, int sign) {
        List<Object[]> list = sessionFactory.getCurrentSession()
                .createSQLQuery("dbo.vote_question :uid, :question_id, :sign")
                .setParameter("uid", user.getUid())
                .setParameter("question_id", question.getQuestionId())
                .setParameter("sign", sign)
                .list();
        question.setVotes((Integer) list.get(0)[1]);
        return (Integer) list.get(0)[0] == 1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Tag> getTags(String s) {
        List<Tag> tags = sessionFactory.getCurrentSession()
                .createQuery("from Tag where name like :s ORDER BY usage DESC")
                .setParameter("s", s + "%")
                .setMaxResults(15)
                .list();

        return tags;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Tag getTagByName(String name) {
        List<Tag> tags = sessionFactory.getCurrentSession()
                .createQuery("from Tag where name=:name")
                .setParameter("name", name)
                .list();
        if (tags.isEmpty()) {
            return null;
        } else {
            return tags.get(0);
        }
    }

    @Override
    public void addTag(Tag tag) {
        sessionFactory.getCurrentSession().save(tag);
    }

    /*@Override
    @SuppressWarnings("unchecked")
    public FavouriteQuestion getFavouriteQuestion(Question question, User user) {
        List<FavouriteQuestion> list = sessionFactory.getCurrentSession()
                .createQuery("from FavouriteQuestion where question=:question and user=:user")
                .setParameter("question", question)
                .setParameter("user", user)
                .list();
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    @Override
    public void saveFavouriteQuestion(FavouriteQuestion favouriteQuestion) {
        sessionFactory.getCurrentSession().save(favouriteQuestion);
    }

    @Override
    public void deleteFavouriteQuestion(FavouriteQuestion favouriteQuestion) {
        sessionFactory.getCurrentSession().delete(favouriteQuestion);
    }*/
}
