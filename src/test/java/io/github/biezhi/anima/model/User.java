package io.github.biezhi.anima.model;

import io.github.biezhi.anima.core.ActiveRecord;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author biezhi
 * @date 2018/3/13
 */
@Data
@NoArgsConstructor
public class User extends ActiveRecord {

    private Integer id;
    private String  name;
    private Integer age;

    public User(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

}