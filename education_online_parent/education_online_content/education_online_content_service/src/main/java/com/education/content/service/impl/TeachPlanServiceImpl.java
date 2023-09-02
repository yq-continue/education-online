package com.education.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.base.exception.EducationException;
import com.education.content.mapper.CourseBaseMapper;
import com.education.content.mapper.TeachplanMapper;
import com.education.content.mapper.TeachplanMediaMapper;
import com.education.content.model.dto.BindTeachplanMediaDto;
import com.education.content.model.dto.SaveTeachplanDto;
import com.education.content.model.dto.TeachplanDto;
import com.education.content.model.po.CourseBase;
import com.education.content.model.po.Teachplan;
import com.education.content.model.po.TeachplanMedia;
import com.education.content.service.TeachPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yang
 * @create 2023-07-29 17:14
 */
@Service
public class TeachPlanServiceImpl implements TeachPlanService {

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Override
    public List<TeachplanDto> getTreeNodes(Long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(courseId);
        return teachplanDtos;
    }

    @Override
    public void saveTeachPlanOfService(SaveTeachplanDto saveTeachplanDto) {
        Long id = saveTeachplanDto.getId();
        Teachplan teachplan = new Teachplan();
        BeanUtils.copyProperties(saveTeachplanDto,teachplan);
        if (id == null){
            //新增
            teachplan.setCreateDate(LocalDateTime.now());
            //添加 order 排序信息
            Integer orderBy = teachplanMapper.getOrderBy(saveTeachplanDto.getParentid(),saveTeachplanDto.getCourseId(),saveTeachplanDto.getGrade());
            teachplan.setOrderby(orderBy);
            teachplanMapper.insert(teachplan);
        }else {
            teachplanMapper.updateById(teachplan);
        }
    }

    /**
     * 删除课程计划
     * @param teachPlanId
     */
    @Override
    @Transactional
    public void deleteTeachPlanOfService(Long teachPlanId) {
        //检查课程计划状态  课程还没有提交时可以删除课程计划。
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
        CourseBase courseBase = courseBaseMapper.selectById(teachplan.getCourseId());
        if ("203002".equals(courseBase.getStatus())){
            EducationException.cast("课程已经发布，无法删除课程信息");
        }
        //删除对应课程计划  若父节点下存在子节点则无法删除
        LambdaQueryWrapper<Teachplan> wrapperOfTeachplan = new LambdaQueryWrapper<>();
        //SELECT COUNT(*) FROM `teachplan` WHERE parentid =
        wrapperOfTeachplan.eq(Teachplan::getParentid,teachPlanId);
        Integer count = teachplanMapper.selectCount(wrapperOfTeachplan);
        if (count != 0){
            EducationException.cast("请将子节点删除后再次尝试");
        }
        teachplanMapper.deleteById(teachPlanId);
        //删除对应媒体信息
        //如果是是一级则不需要删除媒体资源
        if (teachplan.getGrade() != 1){
            LambdaQueryWrapper<TeachplanMedia> wrapperOfTeachplanMedia = new LambdaQueryWrapper<>();
            wrapperOfTeachplanMedia.eq(TeachplanMedia::getTeachplanId,teachPlanId);
            teachplanMediaMapper.delete(wrapperOfTeachplanMedia);
        }

    }

    /**
     * 课程计划排序修改
     * @param type
     * @param teachPlanId
     */
    @Override
    public void move(String type, Long teachPlanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
        Integer tempOfOrder = null;
        if ("movedown".equals(type)){
            //获取下一个元素
            Teachplan next = teachplanMapper.getNext(teachplan.getCourseId(), teachplan.getGrade(), teachplan.getParentid(), teachplan.getOrderby());
            if (next == null){
                EducationException.cast("最后一个元素无法下移");
            }
            //修改排序
            tempOfOrder = teachplan.getOrderby();
            teachplan.setOrderby(next.getOrderby());
            next.setOrderby(tempOfOrder);
            teachplanMapper.updateById(teachplan);
            teachplanMapper.updateById(next);
        }else {
            //获取上一个元素
            Teachplan pre = teachplanMapper.getPre(teachplan.getCourseId(), teachplan.getGrade(), teachplan.getParentid(), teachplan.getOrderby());
            if (pre == null){
                EducationException.cast("第一个元素无法上移");
            }
            //修改排序
            tempOfOrder = pre.getOrderby();
            pre.setOrderby(teachplan.getOrderby());
            teachplan.setOrderby(tempOfOrder);
            teachplanMapper.updateById(teachplan);
            teachplanMapper.updateById(pre);
        }
    }

    @Override
    @Transactional
    public void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        Teachplan teachplan = teachplanMapper.selectById(bindTeachplanMediaDto.getTeachplanId());
        //删除表中对应的绑定信息
        LambdaQueryWrapper<TeachplanMedia> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeachplanMedia::getTeachplanId,bindTeachplanMediaDto.getTeachplanId());
        teachplanMediaMapper.delete(wrapper);
        //添加新的绑定信息
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        BeanUtils.copyProperties(bindTeachplanMediaDto,teachplanMedia);
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);
    }

    @Override
    public void unbindMedia(Long teachPlanId, String mediaId) {
        LambdaQueryWrapper<TeachplanMedia> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(teachPlanId != null,TeachplanMedia::getTeachplanId,teachPlanId);
        wrapper.eq(mediaId != null,TeachplanMedia::getMediaId,mediaId);
        teachplanMediaMapper.delete(wrapper);
    }

    @Override
    public Teachplan queryCourseplan(Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        return teachplan;
    }

    @Override
    public boolean isBinding(String mediaId) {
        LambdaQueryWrapper<TeachplanMedia> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(mediaId != null,TeachplanMedia::getMediaId,mediaId);
        TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(wrapper);
        if (teachplanMedia != null){
            return true;
        }
        return false;
    }
}
