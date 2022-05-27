package management;

import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;

public class MXBeanListener implements NotificationListener {

    @Override
    public void handleNotification(Notification notification, Object handback) {
        System.out.println(notification.getMessage());
    }

    public NotificationFilter getNotificationFilter() {
        NotificationFilterSupport nfs = new NotificationFilterSupport();
        nfs.enableType("resultBeanNotification");
        return nfs;
    }
}
