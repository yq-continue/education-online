package com.education.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.content.model.dto.CourseCategoryTreeDto;
import com.education.content.model.po.CourseCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author yq
 */
@Mapper
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    public List<CourseCategoryTreeDto> selectTreeNodes(@Param("id") String id);

    public String getName(@Param("id") String id);

}
