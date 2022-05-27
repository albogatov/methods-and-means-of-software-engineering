package data;

import management.MXBean;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import utils.HibernateUtility;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ResultBean extends NotificationBroadcasterSupport implements Serializable, MXBean {
    private long sequenceNumber = 0;
    private long resultAmount = 0;
    private long missAmount = 0;
    private long svgAmount = 0;
    private double missPercentage = 0;
    private Result newResult = new Result();
    private String persistenceUnitName = "result";
    private List<Result> results = new ArrayList<Result>();

    private SessionFactory hibernateSessionFactory;
    private Session session;
    private Transaction transaction;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");

    public ResultBean() {
        newResult = new Result(0, 0, 1, OffsetDateTime.now(), "btn");
        results = new ArrayList<Result>();
        hibernateSessionFactory = HibernateUtility.getSessionFactory();
        session = hibernateSessionFactory.openSession();
        loadResults();
        session.close();
    }

    public void loadResults() {
        try {
            session = hibernateSessionFactory.openSession();
            transaction = session.beginTransaction();
            results = (ArrayList<Result>) session.createSQLQuery("SELECT * FROM RESULT_TBL").addEntity(Result.class).list();
            transaction.commit();
            session.close();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            session.close();
        }
    }

    public Result getNewResult() {
        return this.newResult;
    }

    public void setNewResult(Result newResult) {
        this.newResult = newResult;
    }

    public List<Result> getResults() {
        return this.results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public String addResult() {
        try {
            long begin = System.nanoTime();
            String currentTime = formatter.format(new Date(System.currentTimeMillis()));
            session = hibernateSessionFactory.openSession();
            transaction = session.beginTransaction();
            newResult.setCurrTime(currentTime);
            newResult.checkHit();
            double exeTime = (System.nanoTime() - begin) * Math.pow(10, -9);
            newResult.setExecutionTime(exeTime);
            results.add(newResult);
            session.save(newResult);
            transaction.commit();
            newResult = new Result(0, 0, 1, OffsetDateTime.now(), "");
            session.close();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            session.close();
        }
        checkPointAmountDivisor();
        getMissPercentage();
        return "update";
    }

    public void deleteResult(Result result) {
        try {
            session.delete(result);
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            session.close();
        }
        checkPointAmountDivisor();
        getMissPercentage();
    }

    public String eraseResults() {
        try {
            session = hibernateSessionFactory.openSession();
            transaction = session.beginTransaction();
            results.clear();
            List<Result> toDelete = (ArrayList<Result>) session.createSQLQuery("SELECT * FROM RESULT_TBL").addEntity(Result.class).list();
            for (Result erased : toDelete) {
                deleteResult(erased);
            }
            transaction.commit();
            session.close();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            session.close();
        }
        checkPointAmountDivisor();
        getMissPercentage();
        return "update";
    }

    @Override
    public long getResultAmount() {
        loadResults();
        return results.size();
    }

    @Override
    public long getSVGResultAmount() {
        loadResults();
        return results.stream().filter(result -> result.getType().equals("fromSVG")).count();
    }


    @Override
    public long getMissAmount() {
        loadResults();
        return results.stream().filter(result -> result.getHit() == false).count();
    }

    @Override
    public long checkPointAmountDivisor() {
        long amount = getResultAmount();
        long misses = getMissAmount();
        resultAmount = amount;
        missAmount = misses;
        if (amount % 15 == 0) {
            sendNotification(new Notification("Result amount can be divided by 15", this.getClass().getName(), sequenceNumber++, "Overall number of results: " + results.size() + "\n Missed results: " + misses));
        }
        return amount;
    }

    @Override
    public double getMissPercentage() {
        long amount = getSVGResultAmount();
        long misses = getMissAmount();
        svgAmount = amount;
        missAmount = misses;
        if (amount != 0) {
            missPercentage = misses * 100 / amount;
            return misses * 100 / amount;
        }
        missPercentage = 0;
        return 0;
    }

//    @Override
//    public void setMissPercentage(double missPercentage) {
//        this.missPercentage = missPercentage;
//    }
//
//    @Override
//    public void setResultAmount(long amount) {
//        this.resultAmount = amount;
//    }
//
//    @Override
//    public void setMissAmount(long amount) {
//        this.missAmount = amount;
//    }
//
//    @Override
//    public void setSVGResultAmount(long amount) {
//        this.svgAmount = amount;
//    }

}
