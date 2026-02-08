// GPT5.2 prompted with `write a single-threaded OnceConsumer<T> class in Java`
// This should go into a `utils` package which as of now we do not have.
package coppercore.math;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A single-threaded Consumer that allows its accept method to be executed only once. Subsequent
 * calls are ignored.
 *
 * @param <T> the input type
 */
public final class OnceConsumer<T> implements Consumer<T> {

    private final Consumer<? super T> delegate;
    private boolean used = false;

    public OnceConsumer(Consumer<? super T> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public void accept(T value) {
        if (!used) {
            used = true;
            delegate.accept(value);
        }
    }

    /**
     * @return true if the consumer has already been executed
     */
    public boolean hasRun() {
        return used;
    }

    /** Factory method for convenience. */
    public static <T> OnceConsumer<T> of(Consumer<? super T> consumer) {
        return new OnceConsumer<>(consumer);
    }
}
