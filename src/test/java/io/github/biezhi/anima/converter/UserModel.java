package io.github.biezhi.anima.converter;

import io.github.biezhi.anima.Model;
import lombok.Data;

/**
 * @author biezhi
 * @date 2018/9/18
 */
@Data
public class UserModel extends Model {

    private Integer    uid;
    private UserStatus status;

}
