package com.education.content.service;

import com.education.content.model.dto.BindTeachplanMediaDto;
import com.education.content.model.dto.SaveTeachplanDto;
import com.education.content.model.dto.TeachplanDto;
import com.education.content.model.po.Teachplan;
import org.springframework.web.bind.annotation.PathVariable;

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

    /**
     * 绑定课程计划与媒资信息
     * @param bindTeachplanMediaDto
     * @return
     */
    public void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    /**
     * 解绑媒体资源
     * @param teachPlanId 课程计划 id
     * @param mediaId 媒体资源 id
     */
    public void unbindMedia(Long teachPlanId, String mediaId);

    /**
     * 根据课程计划 id 查询课程计划相关信息
     * @param teachplanId
     * @return
     */
    public Teachplan queryCourseplan(@PathVariable("teachplan")Long teachplanId);

    /**
     * 查询媒体资源是否绑定课程计划
     * @return true:绑定了课程计划   false：没有绑定课程计划
     */
    public boolean isBinding(String mediaId);

}
