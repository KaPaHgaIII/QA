package ru.kapahgaiii.qa.repository;

import ru.kapahgaiii.qa.domain.Message;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.domain.User;
import ru.kapahgaiii.qa.domain.Vote;
import ru.kapahgaiii.qa.dto.MessageDTO;

import java.util.List;
import java.util.Set;

public interface ChatDAO {

    public void saveMessage(Message message);

    public List<MessageDTO> getMessageDTOs(Question question);

    public MessageDTO getMessageDTO(Question question, Integer number);

    public Message getMessage(Question question, Integer number);

    public void updateMessage(Message message);

    public Vote getUserVote(User user, Message message);

    public void saveVote(Vote vote);

    public void deleteVote(Vote vote);

    public Set<Integer> getVotes(Question question, User user);

}
