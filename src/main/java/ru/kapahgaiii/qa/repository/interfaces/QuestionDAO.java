package ru.kapahgaiii.qa.repository.interfaces;

import ru.kapahgaiii.qa.domain.Question;

import java.sql.Timestamp;
import java.util.List;

public interface QuestionDAO {

    public Question getQuestionById(Integer id);

    public List<Question> getQuestionsList(Timestamp time);

    public void saveQuestion(Question question);

    public void updateQuestion(Question question);

}
