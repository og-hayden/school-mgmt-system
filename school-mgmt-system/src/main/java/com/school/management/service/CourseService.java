package com.school.management.service;

import com.school.management.model.entities.Course;

import java.util.List;
import java.util.Map;

/**
 * Service layer interface for Course related operations.
 */
public interface CourseService {

    /**
     * Retrieves a map of Course IDs to Course names (or codes) for a given list of IDs.
     * This is useful for displaying course context in lists (like messages).
     *
     * @param courseIds A list of Course IDs.
     * @return A Map where the key is the Course ID and the value is the course name/code string.
     *         If an ID is not found, it might be omitted from the map.
     * @throws RuntimeException if there is an error retrieving course data.
     */
    Map<Integer, String> getCourseNames(List<Integer> courseIds);

    /**
     * Retrieves all courses taught by a specific teacher.
     *
     * @param teacherId The User ID of the teacher.
     * @return A list of Course objects. Returns an empty list if the teacher teaches no courses.
     * @throws RuntimeException if there is an error retrieving courses.
     */
    List<Course> getCoursesByTeacher(int teacherId);

    /**
     * Retrieves a specific course by its ID.
     *
     * @param courseId The ID of the course.
     * @return The Course object, or null if not found.
     * @throws RuntimeException if there is an error retrieving the course.
     */
    Course getCourseById(int courseId);

} 