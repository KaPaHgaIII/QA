package ru.kapahgaiii.qa.repository.implementations;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.kapahgaiii.qa.domain.User;
import ru.kapahgaiii.qa.dto.Notification;
import ru.kapahgaiii.qa.repository.interfaces.NotificationsDAO;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional
public class NotificationsDAOImpl implements NotificationsDAO {

    @Autowired
    private SessionFactory sessionFactory;
    
    @SuppressWarnings("unchecked")
    public List<Notification> getNotifications(User user) {
        List<Object[]> list = sessionFactory.getCurrentSession()
                .createSQLQuery("dbo.select_notifications :uid")
                .setParameter("uid", user.getUid())
                .list();
        List<Notification> notifications = new ArrayList<Notification>();
        for (Object[] objects : list) {
            Notification notification = new Notification();
            notification.setText((String)objects[0]);
            notification.setParam((String) objects[1]);
            notification.setTime(((Timestamp) objects[2]).getTime());
            notification.setType((String) objects[3]);
            notifications.add(notification);
        }
        return notifications;
    }
}
