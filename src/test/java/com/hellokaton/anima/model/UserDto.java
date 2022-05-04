package com.hellokaton.anima.model;

import com.hellokaton.anima.Model;
import com.hellokaton.anima.annotation.Ignore;
import com.hellokaton.anima.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author biezhi
 * @date 2018/3/19
 */
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper=true)
public class UserDto extends Model {

    private Integer id;

    private String userName;

    private Integer age;

    @Ignore
    private List<OrderInfo> orders;

}
