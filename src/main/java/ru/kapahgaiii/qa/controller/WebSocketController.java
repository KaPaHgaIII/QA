package ru.kapahgaiii.qa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import ru.kapahgaiii.qa.domain.ChatMessage;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.dto.IncomingChatMessage;
import ru.kapahgaiii.qa.service.ChatService;

import java.security.Principal;

@Controller
public class WebSocketController {


    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    @MessageMapping("/chat/{chatId}")
    public void chat(IncomingChatMessage incomingMessage, @DestinationVariable String chatId, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        if (principal != null) {
            Question question = chatService.getQuestionById(Integer.parseInt(chatId));

            ChatMessage answer = new ChatMessage(question, principal.getName(), incomingMessage.getText());

            chatService.addMessage(answer);

            ChatMessage[] messages = {answer};

            messagingTemplate.convertAndSend("/chat/" + chatId, messages);
        }
    }
    /*@MessageMapping("/other")
    public void pm(Principal principal, HelloMessage message) {
        messagingTemplate.convertAndSendToUser(principal.getName(), "/pm", new Greeting(message.getName()));
    }*/
}
