package com.education.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.education.ucenter.mapper.XcUserMapper;
import com.education.ucenter.model.dto.AuthParamsDto;
import com.education.ucenter.model.dto.XcUserExt;
import com.education.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author yang
 * @create 2023-08-10 17:26
 */
@Service
@Slf4j
public class UserServiceImpl implements UserDetailsService {
    @Autowired
    private XcUserMapper xcUserMapper;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 根据账号查询用户信息
     *
     * @param s the username identifying the user whose data is required.
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求不符合项目要求:{}",s);
            throw new RuntimeException("认证请求数据格式不对");
        }

        //开始认证
        //认证类型
        String authType = authParamsDto.getAuthType();

        //根据认证类型从 spring 容器取出指定的 bean
        String beanName = authType + "_auth";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        //调用统一execute方法完成认证
        XcUserExt xcUserExt = authService.execute(authParamsDto);
        UserDetails userDetails = getUserPrincipal(xcUserExt);
        return userDetails;
    }

    private UserDetails getUserPrincipal(XcUserExt xcUserExt){
        String password = xcUserExt.getPassword();
        xcUserExt.setPassword(null);
        String jsonString = JSON.toJSONString(xcUserExt);
        //用户权限,如果不加报 Cannot pass a null GrantedAuthority collection
        String[] authorities = {"test"};
        //创建UserDetails对象,权限信息待实现授权功能时再向UserDetail中加入
        UserDetails userDetails = User.withUsername(jsonString).password(password).authorities(authorities).build();
        return userDetails;
    }


}
