package com.school.management.service;

import com.school.management.model.dao.CourseDAO;
import com.school.management.model.entities.Course;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CourseServiceImpl implements CourseService {

    private static final Logger LOGGER = Logger.getLogger(CourseServiceImpl.class.getName());
    private final CourseDAO courseDAO;

    public CourseServiceImpl(CourseDAO courseDAO) {
        this.courseDAO = courseDAO;
    }

    @Override
    public Map<Integer, String> getCourseNames(List<Integer> courseIds) {
        Map<Integer, String> courseNames = new HashMap<>();
        if (courseIds == null || courseIds.isEmpty()) {
            return courseNames;
        }

        try {
            for (int courseId : courseIds) {
                Course course = courseDAO.getCourseById(courseId);
                if (course != null) {
                    // Use Course Code as the display name for brevity, could also use Name
                    courseNames.put(courseId, course.getCourseCode()); 
                } else {
                    LOGGER.log(Level.WARNING, "Course not found for ID: {0}", courseId);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching course names", e);
            throw new RuntimeException("Error retrieving course names: " + e.getMessage(), e);
        }
        return courseNames;
    }

    @Override
    public List<Course> getCoursesByTeacher(int teacherId) {
         try {
            return courseDAO.getCoursesByTeacher(teacherId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching courses for teacher ID: " + teacherId, e);
            throw new RuntimeException("Error retrieving courses by teacher: " + e.getMessage(), e);
        }
    }

    @Override
    public Course getCourseById(int courseId) {
        try {
            return courseDAO.getCourseById(courseId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching course by ID: " + courseId, e);
            throw new RuntimeException("Error retrieving course by ID: " + e.getMessage(), e);
        }
    }
} 