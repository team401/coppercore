package coppercore.math;

import edu.wpi.first.math.MathSharedStore;

/**
 * This class is used to keep track of the cached data as the code updates faster than the camera.
 */
public class CachedData<Type> {

    private Type value = null;
    private int readCount = 0;
    private int maxReads = 5;
    private double lastUpdateTime = 0.0;
    private double staleTime = -1.0;
    private boolean isTimeBased = true;

    /**
     * This is a constructor for the Time-based expiration of the Cached Data.
     *
     * @param staleTime This is the amount of time it takes a cache to be stale
     */
    public CachedData(double staleTime) {
        this.isTimeBased = true;
        this.staleTime = staleTime * 1000; // Convert seconds to milliseconds
        this.lastUpdateTime = MathSharedStore.getTimestamp() * 1000;
    }

    /**
     * This is a constructor for the readCount-based expiration of the Cached Data.
     *
     * @param maxReads This is the maximum amount of reads we can take before the cache is stale
     */
    public CachedData(int maxReads) {
        this.isTimeBased = false;
        this.maxReads = maxReads;
        this.readCount = 0;
    }

    // This method sets the value to become null when the cache is stale (for both time and
    // read-based mechanisms)
    private void makeStale() {
        if (isStale()) {
            value = null;
        }
    }

    /**
     * This method is used to write a new signal value(A new cache).
     *
     * @param signal This is the signal that is being sent.
     */
    public void write(Type signal) {
        if (signal != null) {
            value = signal;
            reset();
        }
    }

    /**
     * This method reads the cached value, increases the readcount, and returns the cached value.
     *
     * @return this returns the cached value
     */
    public Type read() {
        makeStale();
        readCount++;
        return value;
    }

    /**
     * This class checks if the cache is stale(whether based on time or readcount).
     *
     * @return A boolean for whether the cache is stale or not
     */
    public boolean isStale() {
        if (isTimeBased) {
            double currentTime = MathSharedStore.getTimestamp() * 1000;
            return currentTime - lastUpdateTime >= staleTime;
        } else {
            return readCount >= maxReads;
        }
    }

    /** This method resets the expiration tracking value(time or readCount) */
    private void reset() {
        if (isTimeBased) {
            lastUpdateTime =
                    MathSharedStore.getTimestamp()
                            * 1000; // Reset the last update time for time-based expiration
        } else {
            readCount = 0; // Reset the read count for read-based expiration
        }
    }
}
