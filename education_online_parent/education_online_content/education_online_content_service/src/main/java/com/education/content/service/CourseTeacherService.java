package com.education.content.service;

import com.education.content.model.po.CourseTeacher;

import java.util.List;

/**
 * @author yang
 * @create 2023-07-31 17:37
 */
public interface CourseTeacherService {
    /**
     * 添加、修改教师信息
     * @param courseTeacher
     * @return
     */
    public CourseTeacher addTeacher(CourseTeacher courseTeacher);

    /**
     * 查询 courseid 下的教师信息
     * @param courseId  课程 id
     * @return
     */
    public List<CourseTeacher> getCourseTeachers(Long courseId);

    /**
     * 删除教师信息
     * @param courseId 课程 id
     * @param teacherId 教师 id
     */
    public void deleteTeacher(Long courseId,Long teacherId);

}
