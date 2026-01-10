package coppercore.math;

/**
 * This class is used for temporarily storing data in a cache and determining whether or not it is
 * stale using readcount-based expiration.
 *
 * @param <Type> the type of the value to cache
 */
public class CachedDataRead<Type> extends CachedDataValue<Type> {
    private int readCount = 0;
    private int maxReads;

    /**
     * This is a constructor for the readCount-based expiration of the Cached Data.
     *
     * @param maxReads the maximum amount of reads we can take before the cache is stale
     */
    public CachedDataRead(int maxReads) {
        this.maxReads = maxReads;
    }

    /**
     * This method increases the readCount and then returns the cached value unless it has expired.
     *
     * @return returns the cached value if not stale, else null.
     */
    public Type read() {
        readCount++;
        return super.read();
    }

    /**
     * This method checks if the cache is stale(readcount). If the readCount is more than the
     * maximum number of reads.
     *
     * @return true if the cache is stale, false if not
     */
    @Override
    public boolean isStale() {
        return readCount > maxReads;
    }

    /** This method resets the expiration tracking value (readCount) */
    @Override
    protected void reset() {
        readCount = 0; // Reset the read count for read-based expiration
    }
}
