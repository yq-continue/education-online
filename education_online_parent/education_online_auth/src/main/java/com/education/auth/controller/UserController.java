package com.education.auth.controller;

import com.education.ucenter.model.po.Message;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yang
 * @create 2023-08-30 15:25
 */
@RestController
@Slf4j
public class UserController {

    @ApiOperation("找回密码接口")
    @PostMapping("/findpassword")
    public void updateMessage(Message message){
        System.out.println("hello");
    }

}
