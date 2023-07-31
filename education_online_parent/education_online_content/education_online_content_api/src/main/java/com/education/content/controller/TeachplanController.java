package com.education.content.controller;

import com.education.content.model.dto.SaveTeachplanDto;
import com.education.content.model.dto.TeachplanDto;
import com.education.content.service.TeachPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yang
 * @create 2023-07-28 23:25
 */
@Slf4j
@RestController
@Api(tags = {"课程计划编辑接口"})
public class TeachplanController {

    @Autowired
    private TeachPlanService teachPlanService;

    @GetMapping("/teachplan/{courseId}/tree-nodes")
    @ApiOperation("查询课程计划")
    public List<TeachplanDto> getTreeNodes(@PathVariable("courseId") Long courseId) {
        List<TeachplanDto> treeNodes = teachPlanService.getTreeNodes(courseId);
        return treeNodes;
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto teachplan) {
        teachPlanService.saveTeachPlanOfService(teachplan);
    }

    @ApiOperation("删除课程计划")
    @DeleteMapping("/teachplan/{planId}")
    public void deleteTeachplan(@PathVariable("planId") Long planId){
        teachPlanService.deleteTeachPlanOfService(planId);
    }

    @ApiOperation("课程计划排序修改")
    @PostMapping("/teachplan/{type}/{courseId}")
    public void changeOrder(@PathVariable("type") String type,@PathVariable("courseId") Long teachPlanId){
        teachPlanService.move(type,teachPlanId);
    }
}
