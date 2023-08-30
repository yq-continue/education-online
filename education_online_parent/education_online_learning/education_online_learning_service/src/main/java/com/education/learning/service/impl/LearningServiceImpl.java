package com.education.learning.service.impl;

import com.education.base.model.RestResponse;
import com.education.content.model.po.CoursePublish;
import com.education.content.model.po.Teachplan;
import com.education.learning.feignclient.ContentServiceClient;
import com.education.learning.feignclient.MediaServiceClient;
import com.education.learning.model.dto.XcCourseTablesDto;
import com.education.learning.service.LearningService;
import com.education.learning.service.MyCourseTablesService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author yang
 * @create 2023-08-29 20:08
 */
@Service
public class LearningServiceImpl implements LearningService {

    @Autowired
    MyCourseTablesService myCourseTablesService;

    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {

        //查询课程发布信息信息
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        //判断如果为 null不再继续
        if(coursepublish == null){
            return RestResponse.validfail("课程还未发布");
        }

        //远程调用内容管理服务根据课程计划 id（teachplanId）去查询课程计划信息，如果is_preview的值为1表示支持试学
        //若为试学课程则直接返回视频 url
        Teachplan courseplan = contentServiceClient.getCourseplan(teachplanId);
        if (courseplan != null){
            String isPreview = courseplan.getIsPreview();
            if ("1".equals(isPreview)){
                //返回视频的播放地址
                RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
                return playUrlByMediaId;
            }
        }

        //用户已登录
        if(StringUtils.isNotEmpty(userId)){
            //获取学习资格
            XcCourseTablesDto learningStatus = myCourseTablesService.getLearningStatus(userId, courseId);
            //学习资格，[{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"}
            // {"code":"702003","desc":"已过期需要申请续期或重新支付"}]
            String learnStatus = learningStatus.getLearnStatus();
            if("702002".equals(learnStatus)){
                return RestResponse.validfail("无法学习，因为没有选课或选课后没有支付");
            }else if("702003".equals(learnStatus)){
                return RestResponse.validfail("已过期需要申请续期或重新支付");
            }else{
                //返回视频的播放地址
                RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
                return playUrlByMediaId;
            }
        }
        //如果用户没有登录
        //取出课程的收费规则 若为免费则获取视频地址
        String charge = coursepublish.getCharge();
        if("201000".equals(charge)){
            //返回视频的播放地址
            RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
            return playUrlByMediaId;
        }
        return RestResponse.validfail("课程需要购买");
    }

}
