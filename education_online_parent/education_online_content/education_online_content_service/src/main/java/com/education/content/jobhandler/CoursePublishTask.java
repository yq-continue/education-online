package com.education.content.jobhandler;

import com.education.base.exception.EducationException;
import com.education.content.service.CoursePublishService;
import com.education.messagesdk.model.po.MqMessage;
import com.education.messagesdk.service.MessageProcessAbstract;
import com.education.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author yang
 * @create 2023-08-08 20:40
 */
@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    private CoursePublishService coursePublishService;

    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex,shardTotal,"course_publish",30,60);
    }



    @Override
    public boolean execute(MqMessage mqMessage) {
        //获取 courseId
        long courseId = Long.parseLong(mqMessage.getBusinessKey1());
        // 页面静态化并传输到 minio 中  阶段一
        generateCourseHtml(mqMessage,courseId);
        // 添加课程索引       阶段二
        saveCourseIndex(mqMessage,courseId);
        // 写入 redis         阶段三
        saveCourseCache(mqMessage,courseId);
        // 返回 true 说明执行成功
        return true;
    }

    /**
     * 页面静态化并将文件传输到 minio 中  第一阶段
     * @param mqMessage
     * @param courseId
     */
    public void generateCourseHtml(MqMessage mqMessage,long courseId){
        log.debug("开始进行课程静态化,课程id:{}",courseId);
        //消息id
        Long id = mqMessage.getId();
        //消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int stageOne = mqMessageService.getStageOne(id);
        if(stageOne >0){
            log.debug("课程静态化已处理直接返回，课程id:{}",courseId);
            return ;
        }
        // 生成静态文件
        File file = coursePublishService.generateCourseHtml(courseId);
        if (file == null){
            EducationException.cast("生成页面静态文件失败");
        }
        //将文件上传到 minio 中
        coursePublishService.uploadCourseHtml(courseId,file);
        //保存第一阶段状态
        mqMessageService.completedStageOne(id);

    }

    /**
     * 保存课程索引信息       第二阶段
     * @param mqMessage
     * @param courseId
     */
    public void saveCourseIndex(MqMessage mqMessage,long courseId){
        log.debug("开始保存索引,课程id:{}",courseId);
        //消息id
        Long id = mqMessage.getId();
        //消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int stageThree = mqMessageService.getStageThree(id);
        if (stageThree > 0){
            log.debug("索引已经保存完毕，无需再次保存，课程id:{}",courseId);
            return ;
        }

        //todo:保存索引

        mqMessageService.completedStageThree(id);
    }

    /**
     * 将课程信息缓存至redis    第三阶段
     * @param mqMessage
     * @param courseId
     */
    public void saveCourseCache(MqMessage mqMessage,long courseId){
        log.debug("开始将数据缓存进redis,课程id:{}",courseId);
        //消息id
        Long id = mqMessage.getId();
        //消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int stageTwo = mqMessageService.getStageTwo(id);
        if(stageTwo >0){
            log.debug("课程已经缓存进redis中，课程id:{}",courseId);
            return ;
        }
        // todo:将数据缓存进redis中

        mqMessageService.completedStageTwo(id);

    }





}
