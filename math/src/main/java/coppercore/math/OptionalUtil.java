package coppercore.math;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * The OptionalUtil class provides static methods to make it easier/more convenient to work with
 * multiple optionals at a time.
 */
public class OptionalUtil {
    private OptionalUtil() {}

    /**
     * Runs a function only when both optionals contain values.
     *
     * @param firstOptional first optional value
     * @param secondOptional second optional value
     * @param function function to run with both values
     * @param <A> first value type
     * @param <B> second value type
     */
    public static <A, B> void ifBothPresent(
            Optional<A> firstOptional, Optional<B> secondOptional, BiConsumer<A, B> function) {
        firstOptional.ifPresent(
                first -> secondOptional.ifPresent(second -> function.accept(first, second)));
    }

    /**
     * Maps two optionals into one value when both are present.
     *
     * @param firstOptional first optional value
     * @param secondOptional second optional value
     * @param mapper mapper that combines both values
     * @param <A> first value type
     * @param <B> second value type
     * @param <C> mapped value type
     * @return mapped optional, or empty if either input is empty
     */
    public static <A, B, C> Optional<C> mapTwo(
            Optional<A> firstOptional, Optional<B> secondOptional, BiFunction<A, B, C> mapper) {
        return firstOptional.flatMap(
                first -> secondOptional.map(second -> mapper.apply(first, second)));
    }
}
