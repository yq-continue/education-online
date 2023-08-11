package com.education.ucenter.service.impl;

import com.education.ucenter.model.dto.AuthParamsDto;
import com.education.ucenter.model.dto.XcUserExt;
import com.education.ucenter.service.AuthService;
import org.springframework.stereotype.Service;

/**
 * @author yang
 * @create 2023-08-10 20:37
 */
@Service("wechat_auth")
public class WechatAuthServiceImpl implements AuthService {
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {

        return null;
    }
}
