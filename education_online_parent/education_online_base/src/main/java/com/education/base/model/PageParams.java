package com.education.base.model;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author yang
 * @create 2023-07-26 14:54
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Api(tags = "封装分页数据")
public class PageParams {
    //每页显示的数量
    @ApiModelProperty("每页的数量")
    private Long pageSize;
    //当前页码
    @ApiModelProperty("分页查询的第几页")
    private Long pageNo;
}
