package io.github.biezhi.anima.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author biezhi
 * @date 2018/3/16
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    FROM_NOT_NULL(1001, "from class cannot be null, please check :)");

    private Integer code;
    private String  msg;

}
