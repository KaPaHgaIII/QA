package ru.kapahgaiii.qa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kapahgaiii.qa.core.objects.Subscriber;
import ru.kapahgaiii.qa.domain.Question;
import ru.kapahgaiii.qa.domain.RestorePassword;
import ru.kapahgaiii.qa.domain.Tag;
import ru.kapahgaiii.qa.domain.User;
import ru.kapahgaiii.qa.dto.ChatInitial;
import ru.kapahgaiii.qa.dto.MessageDTO;
import ru.kapahgaiii.qa.dto.Online;
import ru.kapahgaiii.qa.dto.QuestionDTO;
import ru.kapahgaiii.qa.service.ChatService;
import ru.kapahgaiii.qa.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.ArrayList;
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

    @RequestMapping("/not_found")
    public String notFound(HttpServletRequest request, Model model) {
        if (!isAjax(request)) {
            return template(model);
        }
        return "not_found :: content";
    }

    @RequestMapping({"/", "/index"})
    public String index(HttpServletRequest request, Model model) {
        if (!isAjax(request)) {
            return template(model);
        }
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
                    chatService.getQuestionVote(userService.findByUsername(principal.getName()), question));
        }
        return "question :: content";
    }

    @RequestMapping("/cp")
    public String cp(HttpServletRequest request, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/";
        }
        if (!isAjax(request)) {
            return template(model);
        }
        model.addAttribute("user", userService.findByUsername(principal.getName()));
        return "cp :: content";
    }

    @RequestMapping("/login")
    public String login(HttpServletRequest request, Model model, Principal principal) {
        if (principal != null) {
            return "redirect:/";
        }
        if (!isAjax(request)) {
            return template(model);
        }
        return "login :: right_block";
    }

    @RequestMapping("/login_error")
    public String loginError(HttpServletRequest request, Model model) {
        if (!isAjax(request)) {
            return template(model);
        }
        return "login_error :: content";
    }

    @RequestMapping("/lost_password")
    public String lostPassword(HttpServletRequest request, Model model, Principal principal) {
        if (principal != null) {
            return "redirect:/";
        }
        if (!isAjax(request)) {
            return template(model);
        }
        return "lost_password :: content";
    }

    @RequestMapping("/restore_password")
    public
    @ResponseBody
    String restorePassword(@RequestParam(value = "email") String email) {
        User user = userService.findByEmail(email);
        if (user != null) {
            if (user.getEmail() != null) {
                userService.restorePassword(user);
                return "success";
            } else {
                return "no_email";
            }
        } else {
            return "user_not_found";
        }
    }

    @RequestMapping("/restore_password_login")
    public String restorePasswordLogin(@RequestParam(value = "hash") String hash, RedirectAttributes redirectAttributes) {
        RestorePassword restorePassword = userService.findRestorePasswordByHash(hash);
        if (restorePassword != null &&
                (new Date()).getTime() - restorePassword.getTime().getTime() < 3600000) {
            userService.deleteRestorePassword(restorePassword);
            userService.login(restorePassword.getUser());
        } else {
            redirectAttributes.addFlashAttribute("message", "Ссылка, по которой Вы перешли, устарела.");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/";
    }


    @RequestMapping("/need_username")
    public String needUsername() {
        return "need_username :: content";
    }

    @RequestMapping("/register")
    public String register(HttpServletRequest request, Model model, Principal principal) {
        if (!isAjax(request)) {
            return template(model);
        }
        return "register :: content";
    }

    @RequestMapping("/private_zone")
    public String privateZone(Model model) {
        return "private_zone :: right_block";
    }

    @RequestMapping("/vk_login")
    public
    @ResponseBody
    String vkLogin(@RequestParam(value = "vkUid") Integer vkUid, @RequestParam(value = "hash") String hash) {
        if (userService.isVkLoginCorrect(vkUid, hash)) {
            User user = userService.findByVkUid(vkUid);
            if (user != null) {
                userService.login(user);
                return "success";
            } else {
                return "need_username";
            }
        } else {
            return "failed";
        }
    }

    @RequestMapping("/vk_register_done")
    public
    @ResponseBody
    String vkRegisterDone(@RequestParam(value = "vkUid") Integer vkUid, @RequestParam(value = "hash") String hash,
                          @RequestParam(value = "username") String username) {
        if (userService.isVkLoginCorrect(vkUid, hash)) {
            if (userService.isUsernameValid(username)) {
                if (userService.isUsernameFree(username)) {
                    User user = userService.vkRegister(vkUid, username);
                    userService.login(user);
                    return "success";
                } else {
                    return "busy_username";
                }
            } else {
                return "incorrect_username";
            }
        } else {
            return "failed";
        }
    }

    @RequestMapping("/register_done")
    public
    @ResponseBody
    String[] registerDone(@RequestParam(value = "username") String username,
                          @RequestParam(value = "email") String email,
                          @RequestParam(value = "password") String password,
                          @RequestParam(value = "password2") String password2) {
        List<String> result = new ArrayList<String>();
        if (!userService.isUsernameValid(username)) {
            result.add("incorrect_username");
        }
        if (!userService.isUsernameFree(username)) {
            result.add("busy_username");
        }
        if (!userService.isEmailValid(email)) {
            result.add("incorrect_email");
        }
        if (!userService.isEmailFree(email)) {
            result.add("busy_email");
        }
        if (!password.equals(password2)) {
            result.add("passwords_not_equal");
        }
        if (result.size() == 0) {
            User user = userService.register(username, email, password);
            userService.login(user);
            result.add("success");
        }
        return result.toArray(new String[result.size()]);
    }


    @RequestMapping("/change_email")
    public
    @ResponseBody
    String changeEmail(@RequestParam(value = "email") String email, Principal principal) {
        if (principal == null) {
            return "not_logined";
        }
        User user = userService.findByUsername(principal.getName());
        if (!userService.isEmailValid(email)) {
            return "incorrect_email";
        }
        if (!email.equals(user.getEmail()) && !userService.isEmailFree(email)) {
            return "busy_email";
        }
        user.setEmail(email);
        userService.updateUser(user);
        return "success";
    }

    @RequestMapping("/change_password")
    public
    @ResponseBody
    String changePassword(Principal principal, @RequestParam(value = "password") String password,
                          @RequestParam(value = "password2") String password2) {
        if (principal == null) {
            return "not_logined";
        }
        User user = userService.findByUsername(principal.getName());
        if (!password.equals(password2)) {
            return "passwords_not_equal";
        }
        user.encodeAndSetPassword(password);
        userService.updateUser(user);
        return "success";
    }

    @RequestMapping("/vk_attach")
    public
    @ResponseBody
    String vkAttach(@RequestParam(value = "vkUid") Integer vkUid, @RequestParam(value = "hash") String hash,
                    Principal principal) {
        if (principal == null) {
            return "not_logined";
        }
        if (userService.isVkLoginCorrect(vkUid, hash)) {
            if (userService.findByVkUid(vkUid) == null) {
                User user = userService.findByUsername(principal.getName());
                user.setVkUid(vkUid);
                userService.updateUser(user);
                return "success";
            } else {
                return "vkUid_busy";
            }
        } else {
            return "failed";
        }
    }


    @RequestMapping("/vk_detach")
    public
    @ResponseBody
    String vkDetach(Principal principal) {
        if (principal == null) {
            return "not_logined";
        }
        User user = userService.findByUsername(principal.getName());
        user.setVkUid(null);
        userService.updateUser(user);
        return "success";
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
    QuestionDTO[] getQuestions(@PathVariable(value = "time") Long time,
                               @RequestParam(value = "exclude[]", required = false) Integer[] exclude) {
        List<QuestionDTO> questions = chatService.getQuestionDTOsList(new Timestamp(time), exclude);
        return questions.toArray(new QuestionDTO[questions.size()]);
    }

    @RequestMapping("/get_tags")
    public
    @ResponseBody
    List<Tag> getTags(@RequestParam(value = "s") String s) {
        return chatService.getTags(s);
    }

    private boolean isAjax(HttpServletRequest request) {
        String xRequestedWith = request.getHeader("x-requested-with");
        return xRequestedWith != null && xRequestedWith.equals("XMLHttpRequest");
    }
}
