package com.education.content.service;

import com.education.base.model.PageParams;
import com.education.base.model.PageResult;
import com.education.content.model.dto.AddCourseDto;
import com.education.content.model.dto.CourseBaseInfoDto;
import com.education.content.model.dto.QueryCourseParamsDto;
import com.education.content.model.po.CourseBase;

/**
 * 课程基本信息业务管理接口
 * @author yang
 * @create 2023-07-27 14:12
 */
public interface CourseBaseInfoService {
    /**
     * 课程管理界面分页查询
     * @param pageParams 分页数据
     * @param dto   封装查询数据
     * @return
     */
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto dto);

    /**
     * 新增课程
     * @param compantId 机构id
     * @param addCourseDto 数据
     * @return
     */
    public CourseBaseInfoDto addCourseOfService(Long compantId,AddCourseDto addCourseDto);

    /**
     * 修改课程界面表单回显
     * @param id    课程id
     * @return
     */
    public CourseBaseInfoDto getCourseByIdOfService(Long id);

    /**
     * 修改课程信息
     * @param companyId  机构id
     * @param courseBaseInfoDto  数据
     * @return
     */
    public CourseBaseInfoDto updateCourseById(Long companyId,CourseBaseInfoDto courseBaseInfoDto);

    /**
     * 删除课程
     * @param courseId 课程id
     */
    public void deleteCourse(Long courseId);

}
