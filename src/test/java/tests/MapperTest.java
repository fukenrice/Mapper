package tests;

import org.junit.jupiter.api.Test;
import ru.hse.homework4.*;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MapperTest {

    enum Time {
        NIGHT, DAY;
    }
    @Exported
    class TestClass {
        String name;
        @DateFormat("HH:mm:s")
        List<LocalTime> numbers = List.of(LocalTime.of(3, 2,1), LocalTime.of(1,2,3));
        Set<String> words = new HashSet<>(List.of("zxc", "qwe"));
        Time time;
        boolean asleep;
        TestClass(String name, Time time, boolean asleep) {
            this.name = name;
            this.time = time;
            this.asleep = asleep;
        }
    }


    @Test
    void writeToStringTest() throws IllegalAccessException {
        Mapper mapper = new mapper.Mapper();
        System.out.println(mapper.writeToString(new TestClass("artem", Time.DAY, false)));
    }


}
