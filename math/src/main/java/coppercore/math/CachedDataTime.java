package coppercore.math;

import edu.wpi.first.math.MathSharedStore;

public class CachedDataTime<Type> {

    private Type value = null;
    private double lastUpdateTime = 0.0;
    private double staleTime = -1.0;

    /**
     * This is a constructor for the Time-based expiration of the Cached Data.
     *
     * @param staleTime the amount of time it takes a cache to be stale
     */
    public CachedDataTime(double staleTime) {
        this.staleTime = staleTime * 1000; // Convert seconds to milliseconds
        this.lastUpdateTime = MathSharedStore.getTimestamp() * 1000;
    }

    /**
     * This method is used to write a new signal value(A new cache).
     *
     * @param signal the signal that is being sent.
     */
    public void write(Type signal) {
        if (signal != null) {
            value = signal;
            reset();
        }
    }

    /**
     * This method reads the cached value.
     *
     * @return returns the cached value
     */
    public Type read() {
        if (isStale()) {
            value = null;
        }
        return value;
    }

    /**
     * This class checks if the cache is stale based on time.
     *
     * @return true if the cache is stale, false if not
     */
    public boolean isStale() {
        double currentTime = MathSharedStore.getTimestamp() * 1000;
        return currentTime - lastUpdateTime >= staleTime;
    }

    /** This method resets the expiration tracking value for time based expiration */
    private void reset() {
        lastUpdateTime =
                MathSharedStore.getTimestamp()
                        * 1000; // Reset the last update time for time-based expiration
    }
}
