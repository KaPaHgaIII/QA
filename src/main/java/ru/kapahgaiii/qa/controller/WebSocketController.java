package ru.kapahgaiii.qa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import ru.kapahgaiii.qa.domain.Message;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.domain.User;
import ru.kapahgaiii.qa.dto.ChatEvent;
import ru.kapahgaiii.qa.dto.MessageDTO;
import ru.kapahgaiii.qa.service.ChatService;
import ru.kapahgaiii.qa.service.SocketService;
import ru.kapahgaiii.qa.service.UserService;

import java.security.Principal;

@Controller
public class WebSocketController {


    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private SocketService socketService;

    @MessageMapping("/chat/messages/{chatId}")
    public void chatMessage(MessageDTO messageDTO, @DestinationVariable String chatId, Principal principal) {
        if (principal != null) {
            Question question = chatService.getQuestionById(Integer.parseInt(chatId));

            Message message = chatService.addMessage(question,
                    userService.findByUsername(principal.getName()), messageDTO);

            socketService.sendChatMessage(message);
            socketService.sendQuestionInfo("messages", question, question.getMessages());
        }
    }

    @MessageMapping("/chat/events/{chatId}")
    public void chatEvent(ChatEvent event, @DestinationVariable String chatId, Principal principal) {
        if (principal != null) {
            Question question = chatService.getQuestionById(Integer.parseInt(chatId));

            if (event.getAction().equals("vote")) {
                Message message = chatService.getMessage(question, event.getNumber());
                User user = userService.findByUsername(principal.getName());
                if (!message.getUser().equals(user)) {
                    boolean vote = chatService.vote(user, message);
                    event.setResult(vote);
                    event.setUsername(user.getUsername());
                    event.setValue(message.getVotes());
                    socketService.sendChatEvent(chatId, event);
                }
            } else if (event.getAction().equals("questionVote")) {
                User user = userService.findByUsername(principal.getName());
                if (!question.getUser().equals(user)) {
                    boolean vote = chatService.vote(user, question, event.getValue());
                    event.setResult(vote);
                    event.setUsername(user.getUsername());
                    event.setValue(question.getVotes());
                    socketService.sendChatEvent(chatId, event);
                    socketService.sendQuestionInfo("votes", question, question.getVotes());
                }
            }

        }
    }
}
