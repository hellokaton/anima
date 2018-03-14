package io.github.biezhi.anima.model;

import io.github.biezhi.anima.core.ActiveRecord;
import lombok.Data;

/**
 * @author biezhi
 * @date 2018/3/13
 */
@Data
public class User extends ActiveRecord {

    private Integer id;
    private String  name;
    private Integer age;

}