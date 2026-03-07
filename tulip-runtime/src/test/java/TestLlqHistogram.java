import static org.junit.jupiter.api.Assertions.*;

import io.github.tulipltt.tulip.stats.LlqHistogram;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestLlqHistogram {

    private LlqHistogram llqh = new LlqHistogram();

    @BeforeEach
    public void setup() {
        llqh.reset();
    }

    @Test
    public void Test1() {
        long value = 1460139;
        long llqv = llqh.llq(value);
        assertEquals(1500000, llqv);
    }
}
