package com.eduaction.learning;

import com.education.learning.feignclient.ContentServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author yang
 * @create 2023-08-25 15:00
 */
@SpringBootTest(classes = LearningApplication.class)
public class TestDemo {

    @Autowired
    private ContentServiceClient contentServiceClient;

    @Test
    public void test1(){
        System.out.println("das");
    }

}
