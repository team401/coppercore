package coppercore.math;

/**
 * A utility that runs a {@link Runnable} only on the first invocation. Subsequent calls to {@link
 * #run(Runnable)} are no-ops.
 *
 * <p>This class is not thread-safe.
 */
public class RunOnce {
    private boolean hasRun = false;

    /**
     * Runs the given {@link Runnable} if this is the first call. Subsequent calls are ignored.
     *
     * @param runnable the action to run on the first invocation
     */
    public void run(Runnable runnable) {
        if (!hasRun) {
            hasRun = true;
            runnable.run();
        }
    }

    /** Resets this instance so the next call to {@link #run(Runnable)} will execute again. */
    public void reset() {
        hasRun = false;
    }
}
