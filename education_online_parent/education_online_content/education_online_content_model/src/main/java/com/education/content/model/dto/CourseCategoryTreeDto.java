package com.education.content.model.dto;

import com.education.content.model.po.CourseCategory;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author yang
 * @create 2023-07-27 19:30
 */
@Data
@ToString
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {

    private List<CourseCategoryTreeDto> childrenTreeNodes;

}
