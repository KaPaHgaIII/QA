package ru.kapahgaiii.qa.repository.interfaces;

import ru.kapahgaiii.qa.domain.Message;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.domain.User;
import ru.kapahgaiii.qa.domain.Vote;

import java.util.Set;

public interface VoteDAO {

    public Vote getMessageVote(User user, Message message);

    public Vote getQuestionVote(User user, Question question);

    public Set<Integer> getMessagesUserVotes(Question question, User user);

    public boolean voteMessage(User user, Message message);

    public boolean voteQuestion(User user, Question question, int sign);

}
