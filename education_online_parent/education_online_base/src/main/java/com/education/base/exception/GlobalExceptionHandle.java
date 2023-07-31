package com.education.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yang
 * @create 2023-07-28 16:50
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandle {
    /**
     * 处理自定义异常
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(EducationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customException(EducationException e) {

        return new RestErrorResponse(e.getMessage());

    }

    /**
     * 处理系统异常
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse exceptionHandle(Exception e) {

        log.error("【系统异常】{}",e.getMessage());

        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());

    }

    /**
     * 处理 JSR303 校验系统异常
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse MethodArgumentNotValidExceptionHandle(MethodArgumentNotValidException  e) {
        BindingResult bindingResult = e.getBindingResult();
        List<String> msgList = new ArrayList<>();
        //将错误信息放在msgList
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        for (int i = 0;i < fieldErrors.size();i++){
            msgList.add(fieldErrors.get(i).getDefaultMessage());
        }
        //拼接错误信息
        String msg = StringUtils.join(msgList, ",");
        log.error("【系统异常】{}",msg);
        return new RestErrorResponse(msg);
    }

}
