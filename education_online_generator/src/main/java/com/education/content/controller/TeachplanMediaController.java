package com.education.content.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.education.content.service.TeachplanMediaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yq
 */
@Slf4j
@RestController
@RequestMapping("teachplanMedia")
public class TeachplanMediaController {

    @Autowired
    private TeachplanMediaService  teachplanMediaService;
}
