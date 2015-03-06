package ru.kapahgaiii.qa.repository.interfaces;

import ru.kapahgaiii.qa.domain.FavouriteQuestion;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.domain.User;

public interface ChatDAO extends MessageDAO, QuestionDAO, VoteDAO, TagDAO {

    public FavouriteQuestion getFavouriteQuestion(Question question, User user);

    public void saveFavouriteQuestion(FavouriteQuestion favouriteQuestion);

    public void deleteFavouriteQuestion(FavouriteQuestion favouriteQuestion);

}
