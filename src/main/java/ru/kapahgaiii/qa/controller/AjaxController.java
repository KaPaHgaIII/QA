package ru.kapahgaiii.qa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.dto.ChatMessage;
import ru.kapahgaiii.qa.service.ChatService;
import ru.kapahgaiii.qa.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;
import java.util.Set;


@Controller
public class AjaxController {

    @Autowired
    private ChatService chatService;
    
    @Autowired
    private UserService userService;

    @RequestMapping({"/", "/index"})
    public String index(HttpServletRequest request, Model model) {
        if (!isAjax(request)) {
            return "template";
        }
        model.addAttribute("questions", chatService.getQuestionsList());
        return "index :: content";
    }

    @RequestMapping("/question")
    public String content(HttpServletRequest request, @RequestParam int id, Model model) {
        if (!isAjax(request)) {
            return "template";
        }
        model.addAttribute("question", chatService.getQuestionById(id));
        return "question :: content";
    }

    @RequestMapping("/login")
    public String login(HttpServletRequest request,
                        @RequestParam(value = "error", required = false) String error, Model model) {
        if (!isAjax(request)) {
            return "template";
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "login :: content";
    }

    @RequestMapping("/loadChatMessages/{chatId}")
    public
    @ResponseBody
    ChatMessage[] getChatMessages(@PathVariable String chatId) {
        Question question = chatService.getQuestionById(Integer.parseInt(chatId));
        List<ChatMessage> messagesList = chatService.getMessageDTOsList(question);
        return messagesList.toArray(new ChatMessage[messagesList.size()]);
    }
    
    @RequestMapping("/loadChatVotes/{chatId}")
    public
    @ResponseBody
    Integer[] getChatVotes(@PathVariable String chatId, Principal principal) {
        if (principal != null) {
            Question question = chatService.getQuestionById(Integer.parseInt(chatId));
            Set<Integer> numbers = chatService.getVotes(question, userService.findByUsername(principal.getName()));
            return numbers.toArray(new Integer[numbers.size()]);
        }
        return null;
    }

    private boolean isAjax(HttpServletRequest request) {
        String xRequestedWith = request.getHeader("x-requested-with");
        return xRequestedWith != null && xRequestedWith.equals("XMLHttpRequest");
    }
}
