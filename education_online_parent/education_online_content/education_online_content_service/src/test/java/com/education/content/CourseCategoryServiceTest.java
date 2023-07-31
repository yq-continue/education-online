package com.education.content;

import com.education.content.model.dto.CourseCategoryTreeDto;
import com.education.content.service.CourseCategoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author yang
 * @create 2023-07-27 22:50
 */
@SpringBootTest
public class CourseCategoryServiceTest {

    @Autowired
    private CourseCategoryService service;

    @Test
    public void queryTreeNodesTest(){
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = service.queryTreeNodes("1");
        Assertions.assertNotNull(courseCategoryTreeDtos);
        System.out.println(courseCategoryTreeDtos);
    }


}
