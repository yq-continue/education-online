package com.education.content;

import com.education.base.model.PageParams;
import com.education.base.model.PageResult;
import com.education.content.model.dto.AddCourseDto;
import com.education.content.model.dto.CourseBaseInfoDto;
import com.education.content.model.dto.QueryCourseParamsDto;
import com.education.content.model.po.CourseBase;
import com.education.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author yang
 * @create 2023-07-27 14:38
 */
@SpringBootTest
public class CourseBaseServiceTest {

    @Autowired
    private CourseBaseInfoService service;

    @Test
    public void testQueryCourseBaseList() {
        //PageParams pageParams, QueryCourseParamsDto dto
        PageParams pageParams = new PageParams(10l, 1l);
        QueryCourseParamsDto dto = new QueryCourseParamsDto();
//        dto.setAuditStatus("002003");
//        dto.setPublishStatus("203002");
        dto.setCourseName("java");
        PageResult<CourseBase> pageResult = service.queryCourseBaseList(pageParams, dto);
        Assertions.assertNotNull(pageResult);
    }

    @Test
    public void testAddCourse() {
        AddCourseDto addCourseDto = new AddCourseDto("java基础", "java初学者",
                "小白", "1-1", "1-1-1", "123", "12345", "快来玩玩把", "64873247.img",
                "123456", 100f, 10f, "1760", "wechat", "152", new Integer(365));
        CourseBaseInfoDto courseBaseInfoDto = service.addCourseOfService(123l, addCourseDto);
        Assertions.assertNotNull(courseBaseInfoDto);

    }
}
