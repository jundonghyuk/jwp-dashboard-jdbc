package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.support.Transaction;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionManager transactionManager;

    public UserService(final UserDao userDao,
                       final UserHistoryDao userHistoryDao,
                       final TransactionManager transactionManager) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionManager = transactionManager;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        Transaction transaction = transactionManager.getTransaction();
        transaction.start();

        final var user = findById(id);
        user.changePassword(newPassword);
        try {
            userDao.update(transaction.getConnection(), user);
            userHistoryDao.log(transaction.getConnection(), new UserHistory(user, createBy));
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new DataAccessException("data access error.", e);
        }
    }
}
