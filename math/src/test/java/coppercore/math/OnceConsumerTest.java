package coppercore.math;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OnceConsumerTest {

    @Test
    public void acceptRunsDelegateOnce() {
        AtomicInteger count = new AtomicInteger(0);
        OnceConsumer<String> once = OnceConsumer.of(s -> count.incrementAndGet());

        once.accept("a");
        Assertions.assertEquals(1, count.get());
    }

    @Test
    public void secondAcceptIsIgnored() {
        AtomicInteger count = new AtomicInteger(0);
        OnceConsumer<String> once = OnceConsumer.of(s -> count.incrementAndGet());

        once.accept("a");
        once.accept("b");
        once.accept("c");
        Assertions.assertEquals(1, count.get());
    }

    @Test
    public void hasRunReturnsFalseBeforeAccept() {
        OnceConsumer<Integer> once = new OnceConsumer<>(i -> {});
        Assertions.assertFalse(once.hasRun());
    }

    @Test
    public void hasRunReturnsTrueAfterAccept() {
        OnceConsumer<Integer> once = new OnceConsumer<>(i -> {});
        once.accept(42);
        Assertions.assertTrue(once.hasRun());
    }

    @Test
    public void delegateReceivesCorrectValue() {
        StringBuilder captured = new StringBuilder();
        OnceConsumer<String> once = OnceConsumer.of(captured::append);

        once.accept("hello");
        Assertions.assertEquals("hello", captured.toString());
    }

    @Test
    public void constructorRejectsNullDelegate() {
        Assertions.assertThrows(NullPointerException.class, () -> new OnceConsumer<>(null));
    }

    @Test
    public void factoryMethodRejectsNullDelegate() {
        Assertions.assertThrows(NullPointerException.class, () -> OnceConsumer.of(null));
    }
}
