package com.education.learning.service;

import com.education.base.model.PageResult;
import com.education.learning.model.dto.MyCourseTableParams;
import com.education.learning.model.dto.XcChooseCourseDto;
import com.education.learning.model.dto.XcCourseTablesDto;
import com.education.learning.model.po.XcCourseTables;

/**
 * @author yang
 * @create 2023-08-25 15:25
 */
public interface MyCourseTablesService {
    /**
     * 添加选课
     * @param userId
     * @param courseId
     * @return
     */
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    /**
     * 获取学习资格
     * @param userId
     * @param courseId
     * @return
     */
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId);

    /**
     * 保存选课成功状态
     * @param chooseCourseId
     * @return
     */
    public boolean saveChooseCourseSuccess(String chooseCourseId);

    /**
     * 获取课程表信息
     * @param params
     * @return
     */
    public PageResult<XcCourseTables> getCourseTable(MyCourseTableParams params);
}
