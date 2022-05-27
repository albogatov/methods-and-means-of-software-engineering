package management;

import data.ResultBean;

import javax.management.*;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.management.ManagementFactory;

public class MXBeanContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ResultBean bean = new ResultBean();
            ObjectName beanObjName = new ObjectName("data:type=mbeans,name=result");
            mBeanServer.registerMBean(bean, beanObjName);
            MXBeanListener beanListener = new MXBeanListener();
            mBeanServer.addNotificationListener(beanObjName, beanListener, beanListener.getNotificationFilter(), null);
        } catch (InstanceAlreadyExistsException | MalformedObjectNameException | MBeanRegistrationException | NotCompliantMBeanException e) {
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName("data:type=mbeans,name=result"));
        } catch (MBeanRegistrationException | InstanceNotFoundException | MalformedObjectNameException e) {
            e.printStackTrace();
        }
    }
}
