package io.github.biezhi.anima.core;

/**
 * @author biezhi
 * @date 2018/3/16
 */
public class Model {

    public ResultKey save() {
        return new JavaRecord(this.getClass()).save(this);
    }

    public int update() {
        return new JavaRecord(this.getClass()).updateByModel(this);
    }

    public int delete() {
        return new JavaRecord(this.getClass()).deleteByModel(this);
    }

}
