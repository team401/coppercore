package coppercore.math;

/**
 * This class is used for holding a cached value; subclasses implement specific expiration policies.
 *
 * @param <Type> the type of the value to cache
 */
public abstract class CachedDataValue<Type> {
    private Type value = null;

    /**
     * Write a new data value (a cache entry) and reset expiration policy.
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
     * Returns cached value.
     *
     * @return returns the cached value or null if the value is stale.
     */
    public Type read() {
        if (isStale()) {
            return null;
        }
        return value;
    }

    /**
     * This method checks if the cache is stale.
     *
     * @return true if the cache is stale, false if not
     */
    protected abstract boolean isStale();

    /** Reset expiration policy for this value */
    protected abstract void reset();
}
