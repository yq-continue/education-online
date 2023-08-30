package com.education.ucenter.model.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author yang
 * @create 2023-08-30 15:31
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    //密码
    private String cellphone;

    //邮箱
    private String email;

    private String checkcodekey;

    //验证码
    private String checkcode;

    //确认密码
    private String confirmpwd;

    //密码
    private String password;

}
