package ru.kapahgaiii.qa.repository.interfaces;

import ru.kapahgaiii.qa.domain.RestorePassword;
import ru.kapahgaiii.qa.domain.User;

public interface UserDAO {

    public User findByUsernameOrEmail(String value);

    public User findByUsername(String username);

    public User findByEmail(String email);

    public void updateUser(User user);

    public User findByUid(Integer uid);

    public User findByVkUid(Integer uid);

    public void saveUser(User user);

    public RestorePassword findRestorePasswordByHash(String hash);

    public void saveRestorePassword(RestorePassword restorePassword);

    public void deleteRestorePassword(RestorePassword restorePassword);

}
