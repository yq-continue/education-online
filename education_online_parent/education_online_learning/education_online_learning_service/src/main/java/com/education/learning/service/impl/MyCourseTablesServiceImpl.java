package com.education.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.base.exception.EducationException;
import com.education.base.model.PageResult;
import com.education.content.model.po.CoursePublish;
import com.education.learning.feignclient.ContentServiceClient;
import com.education.learning.mapper.XcChooseCourseMapper;
import com.education.learning.mapper.XcCourseTablesMapper;
import com.education.learning.model.dto.MyCourseTableParams;
import com.education.learning.model.dto.XcChooseCourseDto;
import com.education.learning.model.dto.XcCourseTablesDto;
import com.education.learning.model.po.XcChooseCourse;
import com.education.learning.model.po.XcCourseTables;
import com.education.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yang
 * @create 2023-08-25 15:26
 */
@Slf4j
@Component
public class MyCourseTablesServiceImpl implements MyCourseTablesService {

    @Autowired
    private XcChooseCourseMapper chooseCourseMapper;//选课表

    @Autowired
    private XcCourseTablesMapper courseTablesMapper;//用户课程表

    @Autowired
    private ContentServiceClient contentServiceClient;

    @Override
    @Transactional
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {
        //查询需要添加的已发布课程信息
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        XcChooseCourse xcChooseCourse = null;
        if (coursepublish == null){
            EducationException.cast("课程还未发布");
        }
        String charge = coursepublish.getCharge();
        if ("201000".equals(charge)){
            //若为免费课程将数据添加到用户课程表和选课表
            xcChooseCourse = addFreeCoruse(userId, coursepublish);
            addCourseTabls(xcChooseCourse);
        }else {
            //若为收费课程则将数据写入选课表
            xcChooseCourse = addChargeCoruse(userId, coursepublish);
        }
        //判断学生的学习资格
        XcCourseTablesDto learningStatus = getLearningStatus(userId, courseId);
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(xcChooseCourse,xcChooseCourseDto);
        xcChooseCourseDto.setLearnStatus(learningStatus.getLearnStatus());
        return xcChooseCourseDto;
    }

    //学习资格，[{"code":"702001","desc":"正常学习"},
    // {"code":"702002","desc":"没有选课或选课后没有支付"},
    // {"code":"702003","desc":"已过期需要申请续期或重新支付"}]
    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        //返回的结果
        XcCourseTablesDto courseTablesDto = new XcCourseTablesDto();

        //查询我的课程表，如果查不到说明没有选课
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        if(xcCourseTables == null){
            //"code":"702002","desc":"没有选课或选课后没有支付"
            courseTablesDto.setLearnStatus("702002");
            return courseTablesDto;
        }
        //判断是否过期，如果过期不能继续学习，没有过期可以继续学习
        boolean before = xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        if(before){
            //"code":"702003","desc":"已过期需要申请续期或重新支付"
            BeanUtils.copyProperties(xcCourseTables,courseTablesDto);
            courseTablesDto.setLearnStatus("702003");
            return courseTablesDto;
        }else{
            //"code":"702001","desc":"正常学习"
            BeanUtils.copyProperties(xcCourseTables,courseTablesDto);
            courseTablesDto.setLearnStatus("702001");
            return courseTablesDto;
        }
    }

    @Override
    public boolean saveChooseCourseSuccess(String chooseCourseId) {

        //根据选课id查询选课表
        XcChooseCourse chooseCourse = chooseCourseMapper.selectById(chooseCourseId);
        if(chooseCourse == null){
            log.debug("接收购买课程的消息，根据选课id从数据库找不到选课记录,选课id:{}",chooseCourseId);
            return false;
        }
        //选课状态
        String status = chooseCourse.getStatus();
        //只有当未支付时才更新为已支付
        if("701002".equals(status)){
            //更新选课记录的状态为支付成功
            chooseCourse.setStatus("701001");
            int i = chooseCourseMapper.updateById(chooseCourse);
            if(i<=0){
                log.debug("添加选课记录失败:{}",chooseCourse);
                EducationException.cast("添加选课记录失败");
            }

            //向我的课程表插入记录
            XcCourseTables xcCourseTables = addCourseTabls(chooseCourse);
            return true;
        }

        return false;
    }

    @Override
    public PageResult<XcCourseTables> getCourseTable(MyCourseTableParams params) {
        LambdaQueryWrapper<XcCourseTables> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(XcCourseTables::getUserId,params.getUserId());
        //按拼装查询条件
        wrapper.orderBy(params.getSortType() != "",true,XcCourseTables::getCreateDate);
        wrapper.eq(params.getCourseType() != "",XcCourseTables::getCourseType,params.getCourseType());
        wrapper.lt("2".equals(params.getExpiresType()),XcCourseTables::getValidtimeEnd,LocalDateTime.now());
        wrapper.gt("1".equals(params.getExpiresType()),XcCourseTables::getValidtimeEnd,LocalDateTime.now());
        Page<XcCourseTables> page = new Page<>(params.getPageNo(),params.getPageSize());
        courseTablesMapper.selectPage(page,wrapper);
        PageResult<XcCourseTables> xcCourseTablesPageResult =
                new PageResult<>(page.getRecords(), page.getTotal(), params.getPageNo(), params.getPageSize());
        return xcCourseTablesPageResult;
    }

    //添加免费课程至选课表
    public XcChooseCourse addFreeCoruse(String userId, CoursePublish coursepublish) {
        //课程 id
        Long courseId = coursepublish.getId();
        //判断，如果存在免费的选课记录且选课状态为成功，直接返回了
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<XcChooseCourse>().eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, courseId)
                .eq(XcChooseCourse::getOrderType, "700001")//免费课程
                .eq(XcChooseCourse::getStatus, "701001");//选课成功
        List<XcChooseCourse> xcChooseCourses = chooseCourseMapper.selectList(queryWrapper);
        if(xcChooseCourses.size()>0){
            return xcChooseCourses.get(0);
        }

        //向选课记录表写数据
        XcChooseCourse chooseCourse = new XcChooseCourse();

        chooseCourse.setCourseId(courseId);
        chooseCourse.setCourseName(coursepublish.getName());
        chooseCourse.setUserId(userId);
        chooseCourse.setCompanyId(coursepublish.getCompanyId());
        chooseCourse.setOrderType("700001");//免费课程
        chooseCourse.setCreateDate(LocalDateTime.now());
        chooseCourse.setCoursePrice(coursepublish.getPrice());
        chooseCourse.setValidDays(365);
        chooseCourse.setStatus("701001");//选课成功
        chooseCourse.setValidtimeStart(LocalDateTime.now());//有效期的开始时间
        chooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));//有效期的结束时间

        int insert = chooseCourseMapper.insert(chooseCourse);
        if(insert<=0){
            EducationException.cast("添加选课记录失败");
        }
        return chooseCourse;
    }

    //添加收费课程至选课表
    public XcChooseCourse addChargeCoruse(String userId,CoursePublish coursepublish){
        //课程id
        Long courseId = coursepublish.getId();
        //判断，如果存在收费的选课记录且选课状态为待支付，直接返回了
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<XcChooseCourse>().eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, courseId)
                .eq(XcChooseCourse::getOrderType, "700002")//收费课程
                .eq(XcChooseCourse::getStatus, "701002");//待支付
        List<XcChooseCourse> xcChooseCourses = chooseCourseMapper.selectList(queryWrapper);
        if(xcChooseCourses.size()>0){
            return xcChooseCourses.get(0);
        }

        //向选课记录表写数据
        XcChooseCourse chooseCourse = new XcChooseCourse();

        chooseCourse.setCourseId(courseId);
        chooseCourse.setCourseName(coursepublish.getName());
        chooseCourse.setUserId(userId);
        chooseCourse.setCompanyId(coursepublish.getCompanyId());
        chooseCourse.setOrderType("700002");//收费课程
        chooseCourse.setCreateDate(LocalDateTime.now());
        chooseCourse.setCoursePrice(coursepublish.getPrice());
        chooseCourse.setValidDays(365);
        chooseCourse.setStatus("701002");//待支付
        chooseCourse.setValidtimeStart(LocalDateTime.now());//有效期的开始时间
        chooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));//有效期的结束时间

        int insert = chooseCourseMapper.insert(chooseCourse);
        if(insert<=0){
            EducationException.cast("添加选课记录失败");
        }

        return chooseCourse;
    }
    //添加到我的课程表
    public XcCourseTables addCourseTabls(XcChooseCourse xcChooseCourse){
        //选课成功了才可以向我的课程表添加
        String status = xcChooseCourse.getStatus();
        if(!"701001".equals(status)){
            EducationException.cast("选课没有成功无法添加到课程表");
        }
        XcCourseTables xcCourseTables = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if(xcCourseTables!=null){
            return xcCourseTables;
        }

        xcCourseTables = new XcCourseTables();
        BeanUtils.copyProperties(xcChooseCourse,xcCourseTables);
        xcCourseTables.setChooseCourseId(xcChooseCourse.getId());//记录选课表的逐渐
        xcCourseTables.setCourseType(xcChooseCourse.getOrderType());//选课类型
        xcCourseTables.setUpdateDate(LocalDateTime.now());
        int insert = courseTablesMapper.insert(xcCourseTables);
        if(insert<=0){
            EducationException.cast("添加我的课程表失败");
        }
        return xcCourseTables;
    }

    public XcCourseTables getXcCourseTables(String userId,Long courseId){
        XcCourseTables xcCourseTables = courseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId).eq(XcCourseTables::getCourseId, courseId));
        return xcCourseTables;
    }



}
