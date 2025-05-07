package com.school.management.service;

import com.school.management.model.dao.UserDAO;
import com.school.management.model.entities.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class.getName());
    private final UserDAO userDAO;

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public Map<Integer, String> getUserDisplayNames(List<Integer> userIds) {
        Map<Integer, String> userNames = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return userNames;
        }

        try {
            for (int userId : userIds) {
                User user = userDAO.getUserById(userId);
                if (user != null) {
                    userNames.put(userId, user.getFirstName() + " " + user.getLastName());
                } else {
                    LOGGER.log(Level.WARNING, "User not found for ID: {0}", userId);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching user display names", e);
            throw new RuntimeException("Error retrieving user names: " + e.getMessage(), e);
        }
        return userNames;
    }
} 