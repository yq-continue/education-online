package com.education.content.controller;

import com.education.content.model.po.CourseTeacher;
import com.education.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yang
 * @create 2023-07-31 17:33
 */
@RestController
@Slf4j
@Api(tags = {"教师信息编辑接口"})
public class CourseTeacherController {

    @Autowired
    private CourseTeacherService courseTeacherService;

    @ApiOperation("添加、修改教师信息")
    @PostMapping("/courseTeacher")
    public CourseTeacher addTeacher(@RequestBody CourseTeacher courseTeacher){
        return courseTeacherService.addTeacher(courseTeacher);
    }

    @ApiOperation("教师信息查询")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> queryCourseTeachers(@PathVariable("courseId")Long courseId){
        List<CourseTeacher> courseTeachers = courseTeacherService.getCourseTeachers(courseId);
        return courseTeachers;
    }

    @ApiOperation("删除教师信息")
    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    public void deleteTeacher(@PathVariable("courseId")Long courseId, @PathVariable("teacherId")Long teacherId){
        courseTeacherService.deleteTeacher(courseId,teacherId);
    }


}
