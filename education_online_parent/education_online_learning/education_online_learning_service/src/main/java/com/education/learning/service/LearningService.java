package com.education.learning.service;

import com.education.base.model.RestResponse;

/**
 * @author yang
 * @create 2023-08-29 20:08
 */
public interface LearningService {

    /**
     *  获取教学视频
     * @param userId 用户 id
     * @param courseId 课程 id
     * @param teachplanId 课程计划 id
     * @param mediaId 媒资文件 id
     * @return
     */
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId);

}
