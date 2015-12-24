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
import ru.kapahgaiii.qa.dto.*;
import ru.kapahgaiii.qa.service.ChatService;
import ru.kapahgaiii.qa.service.NotificationsService;
import ru.kapahgaiii.qa.service.SocketService;
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
    private NotificationsService notificationsService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Autowired
    private SessionController sessionController;

    @Autowired
    private SocketService socketService;

    private String template(Model model, Principal principal) {
        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            List<String> tags = new ArrayList<String>();
            for (Tag tag : user.getInterestingTags()) {
                tags.add(tag.getName());
            }
            List<Integer> questions = new ArrayList<Integer>();
            for (Question question : user.getFavouriteQuestions()) {
                questions.add(question.getQuestionId());
            }
            model.addAttribute("interestingTags", tags);
            model.addAttribute("favouriteQuestions", questions);
        }
        return "template";
    }

    @RequestMapping("/not_found")
    public String notFound(HttpServletRequest request, Model model, Principal principal) {
        if (!isAjax(request)) {
            return template(model, principal);
        }
        return "not_found :: content";
    }

    @RequestMapping({"/", "/index"})
    public String index(HttpServletRequest request, Model model, Principal principal,
                        @RequestParam(required = false) String searchQuery) {
        if (!isAjax(request)) {
            return template(model, principal);
        }
        model.addAttribute("searchQuery", searchQuery);
        return "index :: content";
    }

    @RequestMapping("/question")
    public String question(HttpServletRequest request, @RequestParam Integer id, Model model, Principal principal) {
        if (!isAjax(request)) {
            return template(model, principal);
        }
        Question question = chatService.getQuestionById(id);
        model.addAttribute("question", new QuestionDTO(question, true, true));
        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("vote", chatService.getQuestionVote(user, question));
            model.addAttribute("isFavourite", chatService.isFavouriteQuestion(question, user));
        }
        return "question :: content";
    }

    @RequestMapping("/edit_question")
    public String editQuestion(HttpServletRequest request, @RequestParam Integer id, Model model, Principal principal) {
        Question question = chatService.getQuestionById(id);
        if (principal == null || !question.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/question?id=" + id;
        }
        if (!isAjax(request)) {
            return template(model, principal);
        }
        model.addAttribute("question", new QuestionDTO(question, true));
        return "edit_question :: content";
    }

    @RequestMapping("/add_to_favourite")
    public
    @ResponseBody
    boolean restorePassword(@RequestParam(value = "questionId") Integer questionId, Principal principal) {
        if (principal == null) {
            return false;
        }
        Question question = chatService.getQuestionById(questionId);
        User user = userService.findByUsername(principal.getName());
        return chatService.addToFavourite(question, user);
    }

    @RequestMapping("/cp")
    public String cp(HttpServletRequest request, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/";
        }
        if (!isAjax(request)) {
            return template(model, principal);
        }
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("user", user);
        return "cp :: content";
    }

    @RequestMapping("/new_question")
    public String newQuestion(HttpServletRequest request, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/";
        }
        if (!isAjax(request)) {
            return template(model, principal);
        }
        return "new_question :: content";
    }

    @RequestMapping("/save_new_question")
    public
    @ResponseBody
    String saveNewQuestion(@RequestParam(value = "title") String title,
                           @RequestParam(value = "text") String text,
                           @RequestParam(value = "tags") String tagsString,
                           Principal principal) {
        if (principal == null) {
            return "not_logined";
        }
        if (title.length() < 4) {
            return "short_title";
        }
        if (text.length() < 10) {
            return "short_text";
        }
        Set<Tag> tags = chatService.parseTagsString(tagsString);
        if (tags.size() < 1 || tags.size() > 5) {
            return "invalid_tags_count";
        }
        User user = userService.findByUsername(principal.getName());
        Question question = chatService.createQuestion(user, title, text, tags);
        return question.getQuestionId().toString();
    }


    @RequestMapping("/save_edited_question")
    public
    @ResponseBody
    String saveEditedQuestion(Principal principal, @RequestParam(value = "id") Integer id,
                              @RequestParam(value = "title") String title,
                              @RequestParam(value = "text") String text,
                              @RequestParam(value = "tags") String tagsString) {
        Question question = chatService.getQuestionById(id);
        if (principal == null || !question.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/question?id=" + id;
        }
        if (title.length() < 4) {
            return "short_title";
        }
        if (text.length() < 10) {
            return "short_text";
        }
        Set<Tag> tags = chatService.parseTagsString(tagsString);
        if (tags.size() < 1 || tags.size() > 5) {
            return "invalid_tags_count";
        }
        chatService.editQuestion(question, title, text, tags);
        return "success";
    }

    @RequestMapping("/login")
    public String login(HttpServletRequest request, Model model, Principal principal) {
        if (principal != null) {
            return "redirect:/";
        }
        if (!isAjax(request)) {
            return template(model, principal);
        }
        return "login :: right_block";
    }

    @RequestMapping("/login_error")
    public String loginError(HttpServletRequest request, Model model, Principal principal) {
        if (!isAjax(request)) {
            return template(model, principal);
        }
        return "login_error :: content";
    }

    @RequestMapping("/lost_password")
    public String lostPassword(HttpServletRequest request, Model model, Principal principal) {
        if (principal != null) {
            return "redirect:/";
        }
        if (!isAjax(request)) {
            return template(model, principal);
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
        if (principal != null) {
            return "redirect:/";
        }
        if (!isAjax(request)) {
            return template(model, principal);
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


    @RequestMapping("/add_interesting_tags")
    public
    @ResponseBody
    String addInterestingTags(@RequestParam(value = "tags") String tagsString, Principal principal) {
        if (principal == null) {
            return "not_logined";
        }
        User user = userService.findByUsername(principal.getName());
        Set<Tag> tags = chatService.parseTagsString(tagsString);
        userService.addInterestingTags(user, tags);
        return "success";
    }

    @RequestMapping("/delete_interesting_tag")
    public
    @ResponseBody
    String deleteInterestingTag(@RequestParam(value = "name") String name, Principal principal) {
        if (principal == null) {
            return "not_logined";
        }
        User user = userService.findByUsername(principal.getName());
        Tag tag = chatService.getTagByName(name);
        userService.deleteInterestingTag(user, tag);
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
                               @RequestParam(value = "exclude[]", required = false) Integer[] exclude,
                               @RequestParam(value = "searchQuery", required = false) String searchQuery,
                               @RequestParam(value = "tags[]", required = false) String[] tags) {
        List<QuestionDTO> questions = chatService.getQuestionDTOsList(new Timestamp(time), exclude, searchQuery, tags);
        return questions.toArray(new QuestionDTO[questions.size()]);
    }

    @RequestMapping("/get_tags")
    public
    @ResponseBody
    List<Tag> getTags(@RequestParam(value = "s") String s) {
        return chatService.getTags(s);
    }

    @RequestMapping("/get_notifications")
    public
    @ResponseBody
    List<Notification> getNotifications(Principal principal) {
        if (principal == null) {
            return null;
        }
        return notificationsService.getNotifications(userService.findByUsername(principal.getName()));
    }

    private boolean isAjax(HttpServletRequest request) {
        String xRequestedWith = request.getHeader("x-requested-with");
        return xRequestedWith != null && xRequestedWith.equals("XMLHttpRequest");
    }
}
