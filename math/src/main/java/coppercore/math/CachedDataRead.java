package coppercore.math;

/**
 * This class is used for temporarily storing data in a cache and determining whether or not it is
 * stale using readcount-based expiration.
 *
 * @param <Type> the type of the value to cache
 */
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
     * This method is used to write a data value (A cache entry) and resets the data expiration
     * value (readCount = 0).
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
     * This method increases the readCount and then returns the cached value. If that value is
     * stale, then it will return null, however the value of the cached value remains the same.
     *
     * @return returns the cached value
     */
    public Type read() {
        readCount++;
        if (isStale()) {
            return null;
        }
        return value;
    }

    /**
     * This method checks if the cache is stale(readcount). If the readCount is more than the
     * maximum number of reads.
     *
     * @return true if the cache is stale, false if not
     */
    public boolean isStale() {
        return readCount > maxReads;
    }

    /** This method resets the expiration tracking value(readCount) */
    private void reset() {
        readCount = 0; // Reset the read count for read-based expiration
    }
}
