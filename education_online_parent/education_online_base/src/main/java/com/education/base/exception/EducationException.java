package com.education.base.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yang
 * @create 2023-07-28 16:42
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EducationException extends RuntimeException {

    private String message;

    public static void cast(CommonError commonError){
        throw new EducationException(commonError.getErrMessage());
    }
    public static void cast(String errMessage){
        throw new EducationException(errMessage);
    }


}
