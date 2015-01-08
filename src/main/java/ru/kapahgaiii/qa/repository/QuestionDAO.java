package ru.kapahgaiii.qa.repository;

import ru.kapahgaiii.qa.domain.Question;

import java.util.List;

public interface QuestionDAO {

    public Question getQuestionById(Integer id);

    public List<Question> getQuestionsList();

}
