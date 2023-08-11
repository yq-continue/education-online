package com.education.ucenter.service;

import com.education.ucenter.model.dto.AuthParamsDto;
import com.education.ucenter.model.dto.XcUserExt;

/**
 * @author yang
 * @create 2023-08-10 20:34
 */
public interface AuthService {

    public XcUserExt execute(AuthParamsDto authParamsDto);

}
