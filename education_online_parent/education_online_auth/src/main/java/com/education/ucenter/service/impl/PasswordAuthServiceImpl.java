package com.education.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.ucenter.feiclient.CheckCodeClient;
import com.education.ucenter.mapper.XcUserMapper;
import com.education.ucenter.model.dto.AuthParamsDto;
import com.education.ucenter.model.dto.XcUserExt;
import com.education.ucenter.model.po.XcUser;
import com.education.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author yang
 * @create 2023-08-10 20:35
 */
@Service("password_auth")
@Slf4j
public class PasswordAuthServiceImpl implements AuthService {

    @Autowired
    private XcUserMapper xcUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CheckCodeClient checkCodeClient;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        //对比验证码
        String checkcode = authParamsDto.getCheckcode();
        String checkcodekey = authParamsDto.getCheckcodekey();
        if(StringUtils.isBlank(checkcodekey) || StringUtils.isBlank(checkcode)){
            throw new RuntimeException("验证码为空");
        }

        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if (!verify || verify == null){
            log.info("验证码错误");
            throw new RuntimeException("验证码错误");
        }
        // 查询用户是是否存在
        String userName = authParamsDto.getUsername();
        LambdaQueryWrapper<XcUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(XcUser::getUsername,userName);
        XcUser user = xcUserMapper.selectOne(wrapper);
        if (user == null){
            log.info("用户不存在");
            throw new RuntimeException("账号不存在");
        }
        //判断密码是否正确
        String passwordOfInput = authParamsDto.getPassword();
        String passwordOfDb = user.getPassword();
        boolean matches = passwordEncoder.matches(passwordOfInput, passwordOfDb);
        if (!matches){
            log.info("密码错误");
            throw new RuntimeException("密码错误");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user,xcUserExt);
        return xcUserExt;
    }
}
