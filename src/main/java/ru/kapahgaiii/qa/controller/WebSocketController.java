package ru.kapahgaiii.qa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import ru.kapahgaiii.qa.domain.Message;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.domain.User;
import ru.kapahgaiii.qa.dto.ChatEvent;
import ru.kapahgaiii.qa.dto.ChatMessage;
import ru.kapahgaiii.qa.service.ChatService;
import ru.kapahgaiii.qa.service.UserService;

import java.security.Principal;

@Controller
public class WebSocketController {


    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    @MessageMapping("/chat/messages/{chatId}")
    public void chatMessage(ChatMessage messageDTO, @DestinationVariable String chatId, Principal principal) {
        if (principal != null) {
            Question question = chatService.getQuestionById(Integer.parseInt(chatId));

            Message message = new Message(question, userService.findByUsername(principal.getName()), messageDTO.getText());

            messageDTO = chatService.addMessage(message);

            ChatMessage[] answer = {messageDTO};

            messagingTemplate.convertAndSend("/chat/messages/" + chatId, answer);
        }
    }

    @MessageMapping("/chat/events/{chatId}")
    public void chatEvent(ChatEvent event, @DestinationVariable String chatId, Principal principal) {
        if (principal != null) {
            Question question = chatService.getQuestionById(Integer.parseInt(chatId));

            if (event.getAction().equals("vote")) {
                Message message = chatService.getMessage(question, event.getNumber());
                User user = userService.findByUsername(principal.getName());
                if(!message.getUser().equals(user)) {
                    boolean vote = chatService.vote(user, message);
                    event.setResult(vote);
                    event.setUsername(user.getUsername());
                    messagingTemplate.convertAndSend("/chat/events/" + chatId, event);
                }
            }

        }
    }
    /*@MessageMapping("/other")
    public void pm(Principal principal, HelloMessage message) {
        messagingTemplate.convertAndSendToUser(principal.getName(), "/pm", new Greeting(message.getName()));
    }*/
}
