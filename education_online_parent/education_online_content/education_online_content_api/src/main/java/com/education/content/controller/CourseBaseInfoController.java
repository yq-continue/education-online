package com.education.content.controller;

import com.education.base.model.PageParams;
import com.education.base.model.PageResult;
import com.education.content.model.dto.AddCourseDto;
import com.education.content.model.dto.CourseBaseInfoDto;
import com.education.content.model.dto.QueryCourseParamsDto;
import com.education.content.model.po.CourseBase;
import com.education.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author yang
 * @create 2023-07-26 15:20
 */
@RestController
@Slf4j
@Api(tags = {"课程编辑接口"})
public class CourseBaseInfoController {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;


    @ApiOperation("课程管理界面分页查询")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto){
        PageResult<CourseBase> pageResult = courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParamsDto);
        return pageResult;
    }

    @ApiOperation("新增课程")
    @PostMapping("/course")
    public CourseBaseInfoDto addCourse(@RequestBody @Validated AddCourseDto addCourseDto){
        Long companyId = 123l;
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.addCourseOfService(companyId,addCourseDto);
        return courseBaseInfoDto;
    }

    @ApiOperation("修改课程表单回显")
    @GetMapping("/course/{id}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable("id") Long id){
        CourseBaseInfoDto courseByIdOfService = courseBaseInfoService.getCourseByIdOfService(id);
        return courseByIdOfService;
    }

    @ApiOperation("修改课程信息")
    @PutMapping("/course")
    public CourseBaseInfoDto updateCourseById(@RequestBody @Validated CourseBaseInfoDto courseBaseInfoDto){
        Long companyId = 1232141425l;
        courseBaseInfoService.updateCourseById(companyId,courseBaseInfoDto);
        return courseBaseInfoDto;
    }

    @ApiOperation("删除课程")
    @DeleteMapping("/course/{courseId}")
    public void deleteCourse(@PathVariable("courseId")Long courseId){
        courseBaseInfoService.deleteCourse(courseId);
    }




}
