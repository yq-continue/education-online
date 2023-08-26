package com.education.content.service;

import com.education.content.model.dto.CoursePreviewDto;
import com.education.content.model.po.CoursePublish;

import java.io.File;

/**
 * @author yang
 * @create 2023-08-07 22:09
 */
public interface CoursePublishService {

    /**
     * 通过 courseid 查询需要的相关信息
     * @param courseId
     * @return
     */
     public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 提交审核
     * @param companyId 公司id
     * @param courseId  课程id
     */
    public void commitAudit(Long companyId,Long courseId);

    /**
     * 课程发布
     * @param companyId
     * @param courseId
     */
    public void publish(Long companyId,Long courseId);

    /**
     * 生成课程静态化文件
     * @param courseId 课程 id
     * @return
     */
    public File generateCourseHtml(Long courseId);

    /**
     * 将课程静态化文件上传到 minio
     * @param courseId 课程 id
     * @param file  文件
     */
    public void  uploadCourseHtml(Long courseId,File file);

    /**
     * 根据 id 查询课程发布信息
     * @param courseId
     * @return
     */
    public CoursePublish getCoursePublish(Long courseId);



}
