package com.school.management.service;

import java.util.List;
import java.util.Map;

/**
 * Service layer interface for User related operations.
 */
public interface UserService {

    /**
     * Retrieves a map of User IDs to user display names (e.g., "FirstName LastName")
     * for a given list of IDs. Useful for showing sender/recipient names.
     *
     * @param userIds A list of User IDs.
     * @return A Map where the key is the User ID and the value is the display name string.
     *         If an ID is not found, it might be omitted from the map.
     * @throws RuntimeException if there is an error retrieving user data.
     */
    Map<Integer, String> getUserDisplayNames(List<Integer> userIds);
    
} 