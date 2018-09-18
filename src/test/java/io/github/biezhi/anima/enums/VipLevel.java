package io.github.biezhi.anima.enums;

import io.github.biezhi.anima.annotation.EnumMapping;
import lombok.Getter;

@Getter
@EnumMapping("code")
public enum VipLevel {

    VIP1(1, "初级会员"),
    VIP2(2, "高级会员"),
    VIP3(3, "至尊会员");

    private int    code;
    private String desc;

    VipLevel(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}