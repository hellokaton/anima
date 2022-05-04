package com.hellokaton.anima;

import com.hellokaton.anima.model.Person;
import org.junit.Test;

import java.util.List;

import static com.hellokaton.anima.Anima.select;

/**
 * @author biezhi
 * @date 2018/6/2
 */
public class EnumTest extends BaseTest {

    @Test
    public void testQueryEnum(){
        System.out.println(Anima.select().from(Person.class).count());
        List<Person> all = Anima.select().from(Person.class).all();
        System.out.println(all);
    }

}
