package com.eduaction.learning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author yang
 * @create 2023-08-25 14:59
 */
@SpringBootApplication(scanBasePackageClasses = TestDemo.class)
@EnableFeignClients
public class LearningApplication {
    public static void main(String[] args) {
        SpringApplication.run(LearningApplication.class,args);
    }
}
