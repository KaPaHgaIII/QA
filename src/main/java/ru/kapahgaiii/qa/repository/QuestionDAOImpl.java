package ru.kapahgaiii.qa.repository;

import org.springframework.stereotype.Repository;
import ru.kapahgaiii.qa.domain.Question;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class QuestionDAOImpl implements QuestionDAO {
    private Map<Integer, Question> questions = new HashMap<Integer, Question>();

    public QuestionDAOImpl() {
        Question question = new Question();
        question.setId(1);
        question.setText("Обсуждаем приложение");
        questions.put(question.getId(), question);
        Question q2 = new Question();
        q2.setId(2);
        q2.setText("Флудильня");
        questions.put(q2.getId(), q2);
    }

    @Override
    public List<Question> getQuestionsList() {
        return new ArrayList<Question>(questions.values());
    }

    @Override
    public Question getQuestionById(Integer id) {
        return questions.get(id);
    }
}
