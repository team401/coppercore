package coppercore.math;

public class CachedDataRead<Type> {
    private Type value = null;
    private int readCount = 0;
    private int maxReads = 5;

    /**
     * This is a constructor for the readCount-based expiration of the Cached Data.
     *
     * @param maxReads the maximum amount of reads we can take before the cache is stale
     */
    public CachedDataRead(int maxReads) {
        this.maxReads = maxReads;
        this.readCount = 0;
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
     * This method reads the cached value, increases the readcount, and returns the cached value.
     *
     * @return returns the cached value
     */
    public Type read() {
        if (isStale()) {
            value = null;
        }
        readCount++;
        return value;
    }

    /**
     * This class checks if the cache is stale(readcount).
     *
     * @return A boolean for whether the cache is stale or not
     */
    public boolean isStale() {
        return readCount >= maxReads;
    }

    /** This method resets the expiration tracking value(readCount) */
    private void reset() {
        readCount = 0; // Reset the read count for read-based expiration
    }
}
