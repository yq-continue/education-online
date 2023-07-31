package com.education.content.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @author yang
 * @create 2023-07-26 15:13
 */
@Data
@ToString
public class QueryCourseParamsDto {

    //审核状态
    private String auditStatus;
    //课程名称
    private String courseName;
    //发布状态
    private String publishStatus;


}
