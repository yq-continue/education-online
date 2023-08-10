package com.education.content.model.dto;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author yang
 * @create 2023-08-07 22:07
 */
@Data
@ToString
public class CoursePreviewDto {
    //课程基本信息,课程营销信息
    CourseBaseInfoDto courseBase;

    //课程计划信息
    List<TeachplanDto> teachplans;

    //师资信息 todo：稍后添加师资信息

}
