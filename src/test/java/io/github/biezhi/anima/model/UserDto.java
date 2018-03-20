package io.github.biezhi.anima.model;

import io.github.biezhi.anima.Model;
import io.github.biezhi.anima.annotation.HasMany;
import io.github.biezhi.anima.annotation.Table;
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

    @HasMany(fk = "uid")
    private List<OrderInfo> orders;

}
