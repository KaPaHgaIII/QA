package ru.kapahgaiii.qa.repository.implementations;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.kapahgaiii.qa.domain.RestorePassword;
import ru.kapahgaiii.qa.domain.User;
import ru.kapahgaiii.qa.repository.interfaces.UserDAO;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class UserDAOImpl implements UserDAO {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    @SuppressWarnings("unchecked")
    public User findByUsernameOrEmail(String value) {
        List<User> users = sessionFactory.getCurrentSession()
                .createQuery("from User where username=:username or email=:email")
                .setParameter("username", value)
                .setParameter("email", value)
                .list();
        if (users.isEmpty()) {
            return null;
        } else {
            return users.get(0);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public User findByUsername(String username) {
        List<User> users = sessionFactory.getCurrentSession()
                .createQuery("from User where username=:username")
                .setParameter("username", username)
                .list();
        if (users.isEmpty()) {
            return null;
        } else {
            return users.get(0);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public User findByEmail(String email) {
        List<User> users = sessionFactory.getCurrentSession()
                .createQuery("from User where email=:email")
                .setParameter("email", email)
                .list();
        if (users.isEmpty()) {
            return null;
        } else {
            return users.get(0);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public User findByUid(Integer uid) {
        List<User> users = sessionFactory.getCurrentSession()
                .createQuery("from User where uid=:uid")
                .setParameter("uid", uid)
                .list();
        if (users.isEmpty()) {
            return null;
        } else {
            return users.get(0);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public User findByVkUid(Integer vkUid) {
        List<User> users = sessionFactory.getCurrentSession()
                .createQuery("from User where vkUid=:vkUid")
                .setParameter("vkUid", vkUid)
                .list();
        if (users.isEmpty()) {
            return null;
        } else {
            return users.get(0);
        }
    }

    @Override
    public void updateUser(User user) {
        sessionFactory.getCurrentSession().merge(user);
    }

    @Override
    public void saveUser(User user) {
        sessionFactory.getCurrentSession().save(user);
    }

    @Override
    @SuppressWarnings("unchecked")
    public RestorePassword findRestorePasswordByHash(String hash) {
        List<RestorePassword> list = sessionFactory.getCurrentSession()
                .createQuery("from RestorePassword where hash=:hash")
                .setParameter("hash", hash)
                .list();
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    @Override
    public void saveRestorePassword(RestorePassword restorePassword) {
        sessionFactory.getCurrentSession().saveOrUpdate(restorePassword);
    }

    @Override
    public void deleteRestorePassword(RestorePassword restorePassword) {
        sessionFactory.getCurrentSession().delete(restorePassword);
    }
}
