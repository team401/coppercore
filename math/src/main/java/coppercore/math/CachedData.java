package coppercore.math;

public class CachedData {

    private double value;
    private int writeCount;
    private int maxWrites;
    private long lastUpdateTime;
    private long staleTime;
    private boolean isTimeBased;

    // Constructor for Time-based expiration
    public CachedData(double staleTime, double change) {
        this.isTimeBased = true;
        this.staleTime = (long) (staleTime * 1000); // Convert seconds to milliseconds
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
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUpdateTime >= staleTime) {
                value = 0;  // Reset value if time has expired
            }
        } else {
            writeCount++;
            if (writeCount >= maxWrites) {
                value = 0;  // Reset value if write count has exceeded
            }
        }
    }

    // Write a new signal value
    public void write(double signal) {
        if (signal != 0) {
            value = signal;
            reset(); // Resetting the expiration count or time
        }
    }

    // Read the cached value, returning null if the cache is stale
    public Double read() {
        if (isStale()) {
            return null;  // Return null if the data is stale
        }
        return value;  // Return the cached value
    }

    // Check if the cache has expired (is stale)
    public boolean isStale() {
        if (isTimeBased) {
            long currentTime = System.currentTimeMillis();
            return currentTime - lastUpdateTime >= staleTime;  // Check if time has passed
        } else {
            return writeCount >= maxWrites;  // Check if the write count has exceeded
        }
    }

    // Reset the expiration tracking
    private void reset() {
        if (isTimeBased) {
            lastUpdateTime = System.currentTimeMillis();  // Reset the last update time for time-based expiration
        } else {
            writeCount = 0;  // Reset the write count for write-based expiration
        }
    }
}

