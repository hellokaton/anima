package com.hellokaton.anima.model;

import com.hellokaton.anima.Model;
import com.hellokaton.anima.annotation.Table;
import lombok.Data;

/**
 * 专家实体类
 */
@Table(name = "scott.uf_zjk")
@Data
public class ZhuanJia extends Model {

    private Integer id;

    private String zjxm;//姓名

    private String gzdw;//工作单位

    private String zw;//职级类型

    private String jszc;//技术职称

    private String zjlx;//专业领域

    private String sczy;//擅长专业

    private String szd;//所在地

}
