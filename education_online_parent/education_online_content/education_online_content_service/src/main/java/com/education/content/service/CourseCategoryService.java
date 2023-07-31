package com.education.content.service;

import com.education.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @author yang
 * @create 2023-07-27 22:35
 */
public interface CourseCategoryService {
    /**
     *
     * @param id 查询此节点下的所有子节点，并将其封装
     * @return
     */
    public List<CourseCategoryTreeDto> queryTreeNodes(String id);

}
