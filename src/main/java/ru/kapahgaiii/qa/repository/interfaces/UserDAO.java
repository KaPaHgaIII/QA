package ru.kapahgaiii.qa.repository.interfaces;

import ru.kapahgaiii.qa.domain.User;

public interface UserDAO {

    public User findByUsername(String username);

    public User findByEmail(String email);

    public void updateUser(User user);

}
