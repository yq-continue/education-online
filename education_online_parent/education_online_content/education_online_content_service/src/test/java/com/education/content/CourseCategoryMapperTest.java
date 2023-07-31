package com.education.content;

import com.education.content.mapper.CourseCategoryMapper;
import com.education.content.model.dto.CourseCategoryTreeDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author yang
 * @create 2023-07-27 22:42
 */
@SpringBootTest
public class CourseCategoryMapperTest {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Test
    public void testSelectNodes(){
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes("1");
        Assertions.assertNotNull(courseCategoryTreeDtos);
        System.out.println(courseCategoryTreeDtos);
    }
}
