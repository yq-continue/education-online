package com.education.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.content.mapper.CourseBaseMapper;
import com.education.content.mapper.CourseTeacherMapper;
import com.education.content.model.po.CourseTeacher;
import com.education.content.service.CourseTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yang
 * @create 2023-07-31 17:38
 */
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Autowired
    private CourseBaseMapper courseBaseMapper;


    @Override
    public CourseTeacher addTeacher(CourseTeacher courseTeacher) {
        //判断是添加教师还是修改教师
        Long id = courseTeacher.getId();
        if (id == null){
            //添加教师信息
            courseTeacher.setCreateDate(LocalDateTime.now());
            courseTeacherMapper.insert(courseTeacher);
        }else {
            //todo:只允许自己机构的账号修改自己机构的教师信息
//            Long courseId = courseTeacher.getCourseId();
//            CourseBase courseBase = courseBaseMapper.selectById(courseId);
//            if (courseBase.getCompanyId() != 1232141425L){
//                EducationException.cast("无法修改非本机构的课程");
//            }
            //修改教师信息
            courseTeacherMapper.updateById(courseTeacher);
        }
        return courseTeacher;
    }

    @Override
    public List<CourseTeacher> getCourseTeachers(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(courseId != null,CourseTeacher::getCourseId,courseId);
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(wrapper);
        return courseTeachers;
    }

    @Override
    public void deleteTeacher(Long courseId, Long teacherId) {
        //todo:只允许自己机构的账号删除自己机构的教师信息
//        CourseBase courseBase = courseBaseMapper.selectById(courseId);
//        if (courseBase.getCompanyId() != 1232141425L){
//            EducationException.cast("无法删除非本机构的课程");
//        }
        LambdaQueryWrapper<CourseTeacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(courseId != null,CourseTeacher::getCourseId,courseId);
        wrapper.eq(teacherId != null,CourseTeacher::getId,teacherId);
        courseTeacherMapper.delete(wrapper);
    }
}
