package com.education.content.service;

import com.education.content.model.dto.SaveTeachplanDto;
import com.education.content.model.dto.TeachplanDto;

import java.util.List;

/**
 * @author yang
 * @create 2023-07-29 17:08
 */
public interface TeachPlanService {
    /**
     * 树形结构查询课程计划
     * @param courseId 课程 id
     * @return
     */
    public List<TeachplanDto> getTreeNodes(Long courseId);

    /**
     * 课程计划创建或修改
     * @param saveTeachplanDto 数据
     */
    public void saveTeachPlanOfService(SaveTeachplanDto saveTeachplanDto);

    /**
     * 删除课程计划
     * @param teachPlanId 课程计划 id
     */
    public void deleteTeachPlanOfService(Long teachPlanId);

    /**
     * 课程计划排序修改
     * @param type 分类，moveUp、moveDown
     * @param teachPlanId 课程计划 id
     */
    public void move(String type,Long teachPlanId);

}
