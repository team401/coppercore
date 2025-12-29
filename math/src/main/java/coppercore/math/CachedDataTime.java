package coppercore.math;

import edu.wpi.first.math.MathSharedStore;

/**
 * This class is used for temporarily storing data in a cache and determining whether or not it is
 * stale using time-based expiration.
 *
 * @param <Type> the type of the value to cache
 */
public class CachedDataTime<Type> extends CachedDataValue<Type> {

    /** The last time the cache was written in milliseconds */
    private double lastUpdateTimestampMs = 0.0;

    /** The amount of time it takes a cache to be stale in milliseconds */
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
     * This method checks if the cache is stale based on time.
     *
     * @return true if the cache is stale, false if not
     */
    @Override
    public boolean isStale() {
        double currentTime = MathSharedStore.getTimestamp() * 1000;
        return currentTime - lastUpdateTimestampMs >= staleTimeMs;
    }

    /** This method resets the expiration tracking value for time based expiration. */
    @Override
    protected void reset() {
        lastUpdateTimestampMs =
                MathSharedStore.getTimestamp()
                        * 1000; // Reset the last update time for time-based expiration
    }
}
