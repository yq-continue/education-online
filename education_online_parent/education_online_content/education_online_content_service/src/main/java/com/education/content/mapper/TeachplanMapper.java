package com.education.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.content.model.dto.TeachplanDto;
import com.education.content.model.po.Teachplan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author yq
 */
@Mapper
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    public List<TeachplanDto> selectTreeNodes(@Param("courseId") long courseId);

    public Integer getOrderBy(@Param("parentid") Long parentid,@Param("courseId") Long courseId,@Param("grade") Integer grade);

    public Teachplan getNext(@Param("courseId") Long courseId,@Param("grade") Integer grade,@Param("parentId") Long parentId,@Param("order")Integer order);

    public Teachplan getPre(@Param("courseId") Long courseId,@Param("grade") Integer grade,@Param("parentId") Long parentId,@Param("order")Integer order);

}
