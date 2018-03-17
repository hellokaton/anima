package io.github.biezhi.anima.mysql;

import io.github.biezhi.anima.model.OrderInfo;

import static io.github.biezhi.anima.Anima.select;

/**
 * @author biezhi
 * @date 2018/3/17
 */
public class OrderInfoTest {

//    @Test
    public void testQuery() {
        OrderInfo orderInfo = select().from(OrderInfo.class).one();
        System.out.println(orderInfo);
    }

}
