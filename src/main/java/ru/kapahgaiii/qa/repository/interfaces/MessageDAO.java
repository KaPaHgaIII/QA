package ru.kapahgaiii.qa.repository.interfaces;

import ru.kapahgaiii.qa.domain.Message;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.dto.MessageDTO;

import java.util.List;

public interface MessageDAO {

    public void saveMessage(Message message);

    public List<MessageDTO> getMessageDTOs(Question question);

    public MessageDTO getMessageDTO(Question question, Integer number);

    public Message getMessage(Question question, Integer number);

    public void updateMessage(Message message);


}
