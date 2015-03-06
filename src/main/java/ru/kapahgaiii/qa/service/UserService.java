package ru.kapahgaiii.qa.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.kapahgaiii.qa.core.objects.MyPrincipal;
import ru.kapahgaiii.qa.core.tools.StringEncoder;
import ru.kapahgaiii.qa.domain.RestorePassword;
import ru.kapahgaiii.qa.domain.Tag;
import ru.kapahgaiii.qa.domain.User;
import ru.kapahgaiii.qa.domain.UserRole;
import ru.kapahgaiii.qa.repository.interfaces.UserDAO;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.*;
import java.util.regex.Pattern;

@Service("UserService")
public class UserService implements org.springframework.security.core.userdetails.UserDetailsService {


    @Autowired
    private EmailService emailService;

    @Autowired
    private UserDAO userDAO;

    @Value("${vkAppId}")
    private String vkAppId;

    @Value("${vkSecurityKey}")
    private String vkSecurityKey;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = userDAO.findByUsernameOrEmail(s);
        if (user == null) {
            throw new UsernameNotFoundException("");
        }
        List<GrantedAuthority> authorities = buildUserAuthority(user.getUserRoles());

        return buildUserForAuthentication(user, authorities);
    }

    private org.springframework.security.core.userdetails.User buildUserForAuthentication(
            User user, List<GrantedAuthority> authorities) {
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                true, true, true, true, authorities);
    }

    private List<GrantedAuthority> buildUserAuthority(Set<UserRole> userRoles) {

        Set<GrantedAuthority> setAuths = new HashSet<GrantedAuthority>();

        // Build user's authorities
        for (UserRole userRole : userRoles) {
            setAuths.add(new SimpleGrantedAuthority(userRole.getRole()));
        }

        return new ArrayList<GrantedAuthority>(setAuths);
    }

    public User findByUsernameOrEmail(String value) {
        return userDAO.findByUsernameOrEmail(value);
    }

    public User findByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    public User findByEmail(String email) {
        return userDAO.findByEmail(email);
    }

    public void updateUser(User user){
        userDAO.updateUser(user);
    }

    public boolean isVkLoginCorrect(Integer vkUid, String hash) {
        return DigestUtils.md5Hex(vkAppId + vkUid + vkSecurityKey).equals(hash);
    }

    public User findByVkUid(Integer vkUid) {
        return userDAO.findByVkUid(vkUid);
    }

    public void login(User user) {
        MyPrincipal principal = new MyPrincipal(user.getUsername());
        List<GrantedAuthority> list = buildUserAuthority(user.getUserRoles());
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(principal,
                user.getPassword(), list);
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    public boolean isUsernameValid(String username) {
        return Pattern.matches("[a-zA-Zа-яёА-ЯЁ0-9_!&\\^\\-\\*]{2,20}", username);
    }

    public boolean isUsernameFree(String username) {
        return userDAO.findByUsername(username) == null;
    }

    public boolean isEmailValid(String email) {
        boolean result = true;
        try {
            (new InternetAddress(email)).validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    public boolean isEmailFree(String email) {
        return userDAO.findByEmail(email) == null;
    }

    public User vkRegister(Integer vkUid, String username) {
        UserRole role = new UserRole();
        role.setRole("ROLE_USER");

        User user = new User();
        user.setVkUid(vkUid);
        user.setUsername(username);
        user.getUserRoles().add(role);
        role.setUser(user);

        userDAO.saveUser(user);

        return user;
    }

    public User register(String username, String email, String password) {
        UserRole role = new UserRole();
        role.setRole("ROLE_USER");

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.encodeAndSetPassword(password);
        user.getUserRoles().add(role);
        role.setUser(user);

        userDAO.saveUser(user);

        return user;
    }

    public void restorePassword(User user) {

        RestorePassword restorePassword = new RestorePassword();
        restorePassword.setUser(user);

        String hash = StringEncoder.sha256(user.getUsername() + restorePassword.getTime().getTime());

        restorePassword.setHash(hash);

        userDAO.saveRestorePassword(restorePassword);

        emailService.sendPasswordHash(user, restorePassword);

    }

    public RestorePassword findRestorePasswordByHash(String hash) {
        return userDAO.findRestorePasswordByHash(hash);
    }

    public void deleteRestorePassword(RestorePassword restorePassword) {
        userDAO.deleteRestorePassword(restorePassword);
    }

    public void deleteInterestingTag(User user, Tag tag) {
        user.getInterestingTags().remove(tag);
        userDAO.updateUser(user);
    }
    public void addInterestingTags(User user, Collection<Tag> tags) {
        user.getInterestingTags().addAll(tags);
        userDAO.updateUser(user);
    }


}
