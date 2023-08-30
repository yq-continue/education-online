package com.education.learning.model.dto;


import lombok.Data;
import lombok.ToString;

/**
 * 我的课程查询条件
 */
@Data
@ToString
public class MyCourseTableParams {

    String userId;

    //课程类型  [{"code":"700001","desc":"免费课程"},{"code":"700002","desc":"收费课程"}]
    private String courseType;

    //排序 直接按照加入课程的顺序进行排序
    private String sortType;

    //1即将过期、2已经过期
    private String expiresType;

    int pageNo = 1;

    int startIndex;

    int pageSize = 10;

}
