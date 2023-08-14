package com.education.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.education.base.exception.CommonError;
import com.education.base.exception.EducationException;
import com.education.content.config.MultipartSupportConfig;
import com.education.content.feignclient.MediaServiceClient;
import com.education.content.mapper.*;
import com.education.content.model.dto.CourseBaseInfoDto;
import com.education.content.model.dto.CoursePreviewDto;
import com.education.content.model.dto.TeachplanDto;
import com.education.content.model.po.*;
import com.education.content.service.CourseBaseInfoService;
import com.education.content.service.CoursePublishService;
import com.education.content.service.TeachPlanService;
import com.education.messagesdk.model.po.MqMessage;
import com.education.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yang
 * @create 2023-08-07 22:13
 */
@Service
@Slf4j
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @Autowired
    private TeachPlanService teachPlanService;

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    private CoursePublishMapper coursePublishMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Autowired
    private MqMessageService mqMessageService;

    @Autowired
    private MediaServiceClient mediaServiceClient;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        //查询课程信息与营销信息
        CourseBaseInfoDto courseByIdOfService = courseBaseInfoService.getCourseByIdOfService(courseId);
        coursePreviewDto.setCourseBase(courseByIdOfService);
        //查询课程计划
        List<TeachplanDto> treeNodes = teachPlanService.getTreeNodes(courseId);
        coursePreviewDto.setTeachplans(treeNodes);
        return coursePreviewDto;
    }

    @Override
    public void commitAudit(Long companyId, Long courseId) {
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseByIdOfService(courseId);
        if (courseBaseInfo == null) {
            EducationException.cast("课程找不到");
        }
        //审核状态
        String auditStatus = courseBaseInfo.getAuditStatus();

        //如果课程的审核状态为已提交则不允许提交
        if (auditStatus.equals("202003")) {
            EducationException.cast("课程已提交请等待审核");
        }
        //todo:本机构只能提交本机构的课程
        Long companyId1 = courseBaseInfo.getCompanyId();
        /*
        if (!companyId1.equals(companyId)){
            EducationException.cast("不能修改非本机构的课程");
        }*/
        //课程的图片、计划信息没有填写也不允许提交
        String pic = courseBaseInfo.getPic();
        if (StringUtils.isEmpty(pic)) {
            EducationException.cast("请求上传课程图片");
        }
        //查询课程计划
        //课程计划信息
        List<TeachplanDto> teachplanTree = teachPlanService.getTreeNodes(courseId);
        if (teachplanTree == null || teachplanTree.size() == 0) {
            EducationException.cast("请编写课程计划");
        }

        //查询到课程基本信息、营销信息、计划等信息插入到课程预发布表
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        //设置机构id
        coursePublishPre.setCompanyId(companyId1);
        //营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //转json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);
        //计划信息
        //转json
        String teachplanTreeJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeJson);
        //查询师资信息 转 JSON
        LambdaQueryWrapper<CourseTeacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseTeacher::getCourseId, courseId);
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(wrapper);
        String courseTeacherJson = JSON.toJSONString(courseTeachers);
        coursePublishPre.setTeachers(courseTeacherJson);
        //状态为已提交
        coursePublishPre.setStatus("202003");
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        //查询预发布表，如果有记录则更新，没有则插入
        CoursePublishPre coursePublishPreObj = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreObj == null) {
            //插入
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            //更新
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新课程基本信息表的审核状态为已提交
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003");//审核状态为已提交

        courseBaseMapper.updateById(courseBase);
    }

    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {
        // 从课程预发布表中获取发布数据    数据校验
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            EducationException.cast("课程没有提交审核，请提交审核后再次尝试");
        }
        if (!coursePublishPre.getCompanyId().equals(companyId)) {
            EducationException.cast("不允许提交其它机构的课程。");
        }
        //课程审核状态
        String auditStatus = coursePublishPre.getStatus();
        //审核通过方可发布
        if (!"202004".equals(auditStatus)) {
            EducationException.cast("操作失败，课程审核通过方可发布。");
        }
        // 将数据写到发布表中
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        CoursePublish coursePublishByQuery = coursePublishMapper.selectById(courseId);
        if (coursePublishByQuery != null) {
            coursePublishMapper.updateById(coursePublish);
        } else {
            coursePublishMapper.insert(coursePublish);
        }
        // 将数据写到 mq_message 消息表中
        saveCoursePublishMessage(courseId);
        // 将预发布表中的数据删除
        coursePublishPreMapper.deleteById(courseId);
        //修改课程发布状态为已发布
        LambdaUpdateWrapper<CourseBase> wrapper = new LambdaUpdateWrapper();
        wrapper.eq(CourseBase::getId,courseId);
        CourseBase courseBase = new CourseBase();
        courseBase.setStatus("203002");
        courseBaseMapper.update(courseBase,wrapper);
    }

    @Override
    public File generateCourseHtml(Long courseId) {
        //静态化文件
        File htmlFile  = null;

        try {
            //获取 freemarker 版本号  配置 freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            //创建静态化文件
            htmlFile = File.createTempFile("course",".html");
            log.debug("课程静态化，生成静态文件:{}",htmlFile.getAbsolutePath());
            //输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("课程静态化异常:{}",e.toString());
            EducationException.cast("课程静态化异常");
        }

        return htmlFile;

    }

    public CoursePublish getCoursePublish(Long courseId){
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        return coursePublish ;
    }


    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.uploadFile(multipartFile, "course/"+courseId+".html");
        if(course==null){
            EducationException.cast("上传静态文件异常");
        }

    }

    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null) {
            EducationException.cast(CommonError.UNKOWN_ERROR);
        }

    }

}
