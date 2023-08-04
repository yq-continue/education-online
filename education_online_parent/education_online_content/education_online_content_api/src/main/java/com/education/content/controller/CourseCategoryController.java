package com.education.content.controller;

import com.education.content.model.dto.CourseCategoryTreeDto;
import com.education.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yang
 * @create 2023-07-27 21:36
 */
@RestController
@Slf4j
@Api(tags = {"课程分类编辑接口"})
public class CourseCategoryController {

    @Autowired
    private CourseCategoryService courseCategoryService;

    @ApiOperation("添加课程界面课程分类显示")
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes(){
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.queryTreeNodes("1");
        return courseCategoryTreeDtos;
    }
}
