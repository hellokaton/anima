package io.github.biezhi.anima;

import io.github.biezhi.anima.core.Anima;
import org.junit.Before;

/**
 * @author biezhi
 * @date 2018/3/13
 */
public class BaseTest {

    @Before
    public void before(){
        Anima.open("jdbc:mysql://127.0.0.1:3306/demo?useUnicode=true&characterEncoding=utf-8&useSSL=false", "root", "123456");
    }

}
