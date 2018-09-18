package io.github.biezhi.anima.converter;

/**
 * @author biezhi
 * @date 2018/9/18
 */
public enum UserStatus {

    NOMARL(1),DISABLE(0);

    private int status;

    UserStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
