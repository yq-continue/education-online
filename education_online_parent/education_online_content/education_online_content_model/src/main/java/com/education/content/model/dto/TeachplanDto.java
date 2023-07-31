package com.education.content.model.dto;

import com.education.content.model.po.Teachplan;
import com.education.content.model.po.TeachplanMedia;
import lombok.Data;

import java.util.List;

/**
 * @author yang
 * @create 2023-07-28 23:24
 */
@Data
public class TeachplanDto extends Teachplan {

    private List<TeachplanDto> teachPlanTreeNodes;

    private TeachplanMedia teachplanMedia;

}
