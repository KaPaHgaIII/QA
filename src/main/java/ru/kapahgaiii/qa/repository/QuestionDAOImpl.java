package ru.kapahgaiii.qa.repository;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.kapahgaiii.qa.domain.Question;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;

@Repository
@Transactional
public class QuestionDAOImpl implements QuestionDAO {

    @Autowired
    SessionFactory sessionFactory;

    @Override
    @SuppressWarnings("unchecked")
    public List<Question> getQuestionsList(Timestamp time) {
//        return sessionFactory.getCurrentSession().createQuery("from Question")
        return sessionFactory.getCurrentSession().createQuery("from Question where updatedTime <= :time")
                .setParameter("time", time)
                .setMaxResults(20)
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
}
