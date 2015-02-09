package ru.kapahgaiii.qa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.kapahgaiii.qa.core.objects.Subscriber;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.dto.ChatInitial;
import ru.kapahgaiii.qa.dto.MessageDTO;
import ru.kapahgaiii.qa.dto.Online;
import ru.kapahgaiii.qa.dto.QuestionDTO;
import ru.kapahgaiii.qa.service.ChatService;
import ru.kapahgaiii.qa.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Set;


@Controller
public class AjaxController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Autowired
    private SessionController sessionController;

    private String template(Model model) {
//        model.addAttribute("guestsOnline", subscribeController.getGuestsOnline());
//        model.addAttribute("usersOnline", subscribeController.getUsersOnline());
        return "template";
    }

    @RequestMapping({"/", "/index"})
    public String index(HttpServletRequest request, Model model) {
        if (!isAjax(request)) {
            return template(model);
        }
//        model.addAttribute("questions", chatService.getQuestionDTOsList());
        return "index :: content";
    }

    @RequestMapping("/question")
    public String content(HttpServletRequest request, @RequestParam int id, Model model, Principal principal) {
        if (!isAjax(request)) {
            return template(model);
        }
        Question question = chatService.getQuestionById(id);
        model.addAttribute("question", new QuestionDTO(question, true));
        if (principal != null) {
            model.addAttribute("vote",
                    chatService.getQuestionVote(userService.findByUsername(principal.getName()),question));
        }
        return "question :: content";
    }

    @RequestMapping("/login")
    public String login(HttpServletRequest request,
                        @RequestParam(value = "error", required = false) String error, Model model) {
        if (!isAjax(request)) {
            return template(model);
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "login :: content";
    }

    @RequestMapping("/loadChat/{chatId}")
    public
    @ResponseBody
    ChatInitial getChatInfo(@PathVariable String chatId, Principal principal) {
        Question question = chatService.getQuestionById(Integer.parseInt(chatId));
        ChatInitial response = new ChatInitial();

        List<MessageDTO> messagesList = chatService.getMessageDTOsList(question);
        response.setMessages(messagesList.toArray(new MessageDTO[messagesList.size()]));

        if (principal != null) {
            Set<Integer> numbers = chatService.getVotes(question, userService.findByUsername(principal.getName()));
            response.setVotedNumbers(numbers.toArray(new Integer[numbers.size()]));
        }

        Set<Subscriber> subscribers = chatService.getChatSubscribers(question);
        if (subscribers != null) {
            response.setSubscribers(subscribers.toArray(new Subscriber[subscribers.size()]));
        } else {
            response.setSubscribers(new Subscriber[0]);
        }

        return response;
    }

    @RequestMapping("/getOnline")
    public
    @ResponseBody
    Online getOnline() {
        return new Online(sessionController.getUsersOnline(), sessionController.getGuestsOnline());
    }

    @RequestMapping("/loadQuestions/{time}")
    public
    @ResponseBody
    QuestionDTO[] getQuestions(@PathVariable(value = "time") Long time) {
        time = time < 0 ? (new Date()).getTime() : time;
        List<QuestionDTO> questions = chatService.getQuestionDTOsList(new Timestamp(time));
        return questions.toArray(new QuestionDTO[questions.size()]);
//        return new Question[0];
    }

    private boolean isAjax(HttpServletRequest request) {
        String xRequestedWith = request.getHeader("x-requested-with");
        return xRequestedWith != null && xRequestedWith.equals("XMLHttpRequest");
    }
}
