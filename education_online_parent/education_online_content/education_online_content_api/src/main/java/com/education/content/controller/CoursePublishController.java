package com.education.content.controller;

import com.alibaba.fastjson.JSON;
import com.education.content.model.dto.CourseBaseInfoDto;
import com.education.content.model.dto.CoursePreviewDto;
import com.education.content.model.dto.TeachplanDto;
import com.education.content.model.po.CoursePublish;
import com.education.content.service.CoursePublishService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author yang
 * @create 2023-08-07 21:18
 */
@Controller
@Slf4j
public class CoursePublishController {

    @Autowired
    private CoursePublishService coursePublishService;

    /**
     * 课程预览接口
     *
     * @param courseId 课程 id
     * @return
     */
    @ApiOperation("课程预览接口")
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId) {
        ModelAndView modelAndView = new ModelAndView();
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        modelAndView.addObject("model", coursePreviewInfo);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }

    /**
     * 课程审核接口
     *
     * @param courseId
     */
    @ApiOperation("课程审核接口")
    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId) {
        Long companyId = 1232141425L;
        coursePublishService.commitAudit(companyId, courseId);
    }

    /**
     * 课程发布接口
     *
     * @param courseId 课程 id
     */
    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void coursePublish(@PathVariable("courseId") Long courseId) {
        Long companyId = 1232141425L;
        coursePublishService.publish(companyId, courseId);
    }

    @ApiOperation("查询课程发布信息")
    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId) {
        CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
        return coursePublish;
    }

    @ApiOperation("获取课程发布信息")
    @ResponseBody
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getCoursePublish(@PathVariable("courseId") Long courseId) {
        //查询课程发布信息
        CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
        if (coursePublish == null) {
            return new CoursePreviewDto();
        }
        //课程基本信息
        CourseBaseInfoDto courseBase = new CourseBaseInfoDto();
        BeanUtils.copyProperties(coursePublish, courseBase);
        //查询课程计划
        List<TeachplanDto> teachplans = JSON.parseArray(coursePublish.getTeachplan(), TeachplanDto.class);
        CoursePreviewDto coursePreviewInfo = new CoursePreviewDto();
        //封装返回数据
        coursePreviewInfo.setCourseBase(courseBase);
        coursePreviewInfo.setTeachplans(teachplans);
        return coursePreviewInfo;
    }


}
