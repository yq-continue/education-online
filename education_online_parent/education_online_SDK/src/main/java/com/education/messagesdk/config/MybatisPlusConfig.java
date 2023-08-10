package com.education.messagesdk.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author yang
 * @create 2023-08-08 17:24
 */
@Configuration("messagesdk_mpconfig")
@MapperScan("com.education.messagesdk.mapper")
@ComponentScan("com.education.messagesdk")
public class MybatisPlusConfig {


}
