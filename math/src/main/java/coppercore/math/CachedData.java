package coppercore.math;

public class CachedData<Type> {

    private Type value = null;
    private int writeCount = 0;
    private int maxWrites = 5;
    private double lastUpdateTime = 0.0;
    private double staleTime = -1.0;
    private boolean isTimeBased = true;

    // Constructor for Time-based expiration
    public CachedData(double staleTime, double change) {
        this.isTimeBased = true;
        this.staleTime = staleTime * 1000; // Convert seconds to milliseconds
        this.lastUpdateTime = System.currentTimeMillis();
    }

    // Constructor for WriteCount-based expiration
    public CachedData(int maxWrites) {
        this.isTimeBased = false;
        this.maxWrites = maxWrites;
        this.writeCount = 0;
    }

    // Update the value based on the change (for both time and write-based mechanisms)
    public void update() {
        if (isTimeBased) {
            double currentTime = System.currentTimeMillis();
            if (currentTime - lastUpdateTime >= staleTime) {
                value = null; // Reset value if time has expired
            }
        } else {
            writeCount++;
            if (writeCount >= maxWrites) {
                value = null; // Reset value if write count has exceeded
            }
        }
    }

    // Write a new signal value
    public void write(Type signal) {
        if (signal != null) {
            value = signal;
            reset(); // Resetting the expiration count or time
        }
    }

    // Read the cached value, returning null if the cache is stale
    public Type read() {
        update();
        return value; // Return the cached value
    }

    // Check if the cache has expired (is stale)
    public boolean isStale() {
        if (isTimeBased) {
            double currentTime = System.currentTimeMillis();
            return currentTime - lastUpdateTime >= staleTime; // Check if time has passed
        } else {
            return writeCount >= maxWrites; // Check if the write count has exceeded
        }
    }

    // Reset the expiration tracking
    private void reset() {
        if (isTimeBased) {
            lastUpdateTime =
                    System.currentTimeMillis(); // Reset the last update time for time-based
            // expiration
        } else {
            writeCount = 0; // Reset the write count for write-based expiration
        }
    }
}
