package coppercore.math;

import edu.wpi.first.math.MathSharedStore;

/**
 * This class is used for temporarily storing data in a cache and determining whether or not it is
 * stale using time-based expiration.
 *
 * @param <Type> the type of the value to cache
 */
public class CachedDataTime<Type> {

    private Type value = null;

    /** The last time the cache was written in milliseconds */
    private double lastUpdateTimestampMs = 0.0;

    /** The amount of time it takes a chace to be stale in milliseconds */
    private double staleTimeMs = -1.0;

    /**
     * This is a constructor for the Time-based expiration of the Cached Data.
     *
     * @param staleTimeSeconds the amount of time it takes a cache to be stale in seconds
     */
    public CachedDataTime(double staleTimeSeconds) {
        this.staleTimeMs = staleTimeSeconds * 1000; // Convert seconds to milliseconds
        this.lastUpdateTimestampMs = MathSharedStore.getTimestamp() * 1000;
    }

    /**
     * This method is used to write a data value (A cache entry) and it resets the data expiration
     * timer.
     *
     * @param data the new value to write to the cache.
     */
    public void write(Type data) {
        if (data != null) {
            value = data;
            reset();
        }
    }

    /**
     * This method reads the cached value. If that value is stale, then it will return null, however
     * the value of the cached value remains the same.
     *
     * @return returns the cached value
     */
    public Type read() {
        if (isStale()) {
            return null;
        }
        return value;
    }

    /**
     * This method checks if the cache is stale based on time.
     *
     * @return true if the cache is stale, false if not
     */
    public boolean isStale() {
        double currentTime = MathSharedStore.getTimestamp() * 1000;
        return currentTime - lastUpdateTimestampMs >= staleTimeMs;
    }

    /** This method resets the expiration tracking value for time based expiration */
    private void reset() {
        lastUpdateTimestampMs =
                MathSharedStore.getTimestamp()
                        * 1000; // Reset the last update time for time-based expiration
    }
}
