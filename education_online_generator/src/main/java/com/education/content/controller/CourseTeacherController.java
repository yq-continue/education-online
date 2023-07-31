package com.education.content.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.education.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * 课程-教师关系表 前端控制器
 * </p>
 *
 * @author yq
 */
@Slf4j
@RestController
@RequestMapping("courseTeacher")
public class CourseTeacherController {

    @Autowired
    private CourseTeacherService  courseTeacherService;
}
