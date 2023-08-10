package com.education.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.base.exception.EducationException;
import com.education.base.model.PageParams;
import com.education.base.model.PageResult;
import com.education.content.mapper.*;
import com.education.content.model.dto.AddCourseDto;
import com.education.content.model.dto.CourseBaseInfoDto;
import com.education.content.model.dto.QueryCourseParamsDto;
import com.education.content.model.po.*;
import com.education.content.service.CourseBaseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @author yang
 * @create 2023-07-27 14:19
 */
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto dto) {
        //创建 Page 对象
        Page<CourseBase> courseBasePage = new Page<>(pageParams.getPageNo(),pageParams.getPageSize());
        //封装查询条件
        LambdaQueryWrapper<CourseBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotEmpty(dto.getPublishStatus()),CourseBase::getStatus,dto.getPublishStatus());
        wrapper.eq(StringUtils.isNotEmpty(dto.getAuditStatus()),CourseBase::getAuditStatus,dto.getAuditStatus());
        wrapper.like(StringUtils.isNotEmpty(dto.getCourseName()),CourseBase::getName,dto.getCourseName());
        //查询数据
        courseBaseMapper.selectPage(courseBasePage,wrapper);
        //返回结果
        PageResult<CourseBase> pageResult = new PageResult<>(courseBasePage.getRecords(),courseBasePage.getTotal(),pageParams.getPageNo(),pageParams.getPageSize());
        return pageResult;
    }


    @Override
    @Transactional
    public CourseBaseInfoDto addCourseOfService(Long compantId,AddCourseDto addCourseDto) {
        //合法性校验
        if (StringUtils.isBlank(addCourseDto.getName())) {
//            throw new RuntimeException("课程名称为空");
            EducationException.cast("课程名称为空");
        }

        if (StringUtils.isBlank(addCourseDto.getMt())) {
//            throw new RuntimeException("课程分类为空");
            EducationException.cast("课程分类为空");
        }

        if (StringUtils.isBlank(addCourseDto.getSt())) {
//            throw new RuntimeException("课程分类为空");
            EducationException.cast("课程分类为空");
        }

        if (StringUtils.isBlank(addCourseDto.getGrade())) {
//            throw new RuntimeException("课程等级为空");
            EducationException.cast("课程等级为空");
        }

        if (StringUtils.isBlank(addCourseDto.getTeachmode())) {
//            throw new RuntimeException("教育模式为空");
            EducationException.cast("教育模式为空");
        }

        if (StringUtils.isBlank(addCourseDto.getUsers())) {
//            throw new RuntimeException("适应人群为空");
            EducationException.cast("适应人群为空");
        }

        if (StringUtils.isBlank(addCourseDto.getCharge())) {
//            throw new RuntimeException("收费规则为空");
            EducationException.cast("收费规则为空");
        }
        //获取数据
        CourseBase courseBase = new CourseBase();
        CourseMarket courseMarket = new CourseMarket();
        //将数据保存到 CourseBase 表中
        BeanUtils.copyProperties(addCourseDto,courseBase);
        courseBase.setCompanyId(compantId);
        courseBase.setCreateDate(LocalDateTime.now());
        courseBase.setChangeDate(LocalDateTime.now());
        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");
        courseBaseMapper.insert(courseBase);
        //将数据保存到 CourseMarket 表中
        BeanUtils.copyProperties(addCourseDto,courseMarket);
        courseMarket.setId(courseBase.getId());
        updateCourseMarket(courseMarket);
        //封装返回数据
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        // 大分类、小分类名称
        String mtName = courseCategoryMapper.getName(courseBase.getMt());
        String stName = courseCategoryMapper.getName(courseBase.getSt());
        courseBaseInfoDto.setMtName(mtName);
        courseBaseInfoDto.setStName(stName);
        return courseBaseInfoDto;
    }

    @Override
    public CourseBaseInfoDto getCourseByIdOfService(Long id) {
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        //查询数据
        CourseBase courseBase = courseBaseMapper.selectById(id);
        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        //将数据封装到 CourseBaseInfoDto 中
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        String mtName = courseCategoryMapper.getName(courseBase.getMt());
        String stName = courseCategoryMapper.getName(courseBase.getSt());
        courseBaseInfoDto.setMtName(mtName);
        courseBaseInfoDto.setStName(stName);
        //返回数据
        return courseBaseInfoDto;
    }

    @Override
    public CourseBaseInfoDto updateCourseById(Long companyId,CourseBaseInfoDto courseBaseInfoDto) {
        //数据校验
//        if (!companyId.equals(courseBaseInfoDto.getCompanyId())){
//            EducationException.cast("本机构只能修改本机构的课程");
//        }
        //获取数据
        CourseBase courseBase = new CourseBase();
        CourseMarket courseMarket = new CourseMarket();
        //修改 basecourse 表
        BeanUtils.copyProperties(courseBaseInfoDto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        courseBase.setCompanyId(companyId);
        courseBaseMapper.updateById(courseBase);
        //修改 market 表
        BeanUtils.copyProperties(courseBaseInfoDto,courseMarket);
        updateCourseMarket(courseMarket);
        //返回数据
        return courseBaseInfoDto;
    }


    @Transactional
    @Override
    public void deleteCourse(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        String auditStatus = courseBase.getAuditStatus();
        if (!"202002".equals(auditStatus)){
            EducationException.cast("未提交审核的稿件才能执行删除操作");
        }
        //删除营销信息
        courseMarketMapper.deleteById(courseId);
        //删除课程计划对应的媒体信息
        LambdaQueryWrapper<TeachplanMedia> wrapperOfTeachPlanMedia = new LambdaQueryWrapper<>();
        wrapperOfTeachPlanMedia.eq(TeachplanMedia::getCourseId,courseId);
        teachplanMediaMapper.delete(wrapperOfTeachPlanMedia);
        //删除课程计划
        LambdaQueryWrapper<Teachplan> wrapperOfTeachPlan = new LambdaQueryWrapper<>();
        wrapperOfTeachPlan.eq(Teachplan::getCourseId,courseId);
        teachplanMapper.delete(wrapperOfTeachPlan);
        //删除课程师资信息
        LambdaQueryWrapper<CourseTeacher> wrapperOfCourseTeacher = new LambdaQueryWrapper<>();
        wrapperOfCourseTeacher.eq(CourseTeacher::getCourseId,courseId);
        courseTeacherMapper.delete(wrapperOfCourseTeacher);
        //删除课程信息
        courseBaseMapper.deleteById(courseId);
    }


    private Integer updateCourseMarket(CourseMarket courseMarket){
        //合法性校验
        if ("201001".equals(courseMarket.getCharge()) && (courseMarket.getOriginalPrice() == null || courseMarket.getPrice() == null)){
//            throw new RuntimeException("收费课程价格栏不能为空");
            EducationException.cast("收费课程价格栏不能为空");
        }
        if ("201000".equals(courseMarket.getCharge())){
                if ((courseMarket.getOriginalPrice() != null || courseMarket.getPrice() != null) && (courseMarket.getOriginalPrice() != 0 || courseMarket.getPrice() != 0)){
                    EducationException.cast("免费课程请勿输入价格信息");
                }
            courseMarket.setPrice(0f);
            courseMarket.setOriginalPrice(0f);
        }
        if (courseMarket.getPrice() < 0 || courseMarket.getOriginalPrice() < 0){
//            throw new RuntimeException("价格不能小于0");
            EducationException.cast("价格不能小于0");
        }

        CourseMarket courseMarket1 = courseMarketMapper.selectById(courseMarket.getId());

        if (courseMarket1 == null){
            //若不存在则添加
            int countOfInsert = courseMarketMapper.insert(courseMarket);
            return countOfInsert;
        }else{
            //若存在则修改信息
            int countOfUpdate = courseMarketMapper.updateById(courseMarket);
            return countOfUpdate;
        }
    }
}
