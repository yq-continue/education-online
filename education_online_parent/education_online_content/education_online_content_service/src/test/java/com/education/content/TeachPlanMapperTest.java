package com.education.content;

import com.education.content.mapper.TeachplanMapper;
import com.education.content.model.dto.TeachplanDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author yang
 * @create 2023-07-29 17:06
 */
@SpringBootTest
public class TeachPlanMapperTest {

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Test
    public void testselectTreeNodes(){
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(117l);
        System.out.println(teachplanDtos);

    }

}
