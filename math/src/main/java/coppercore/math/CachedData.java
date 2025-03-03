package coppercore.math;

public class CachedData<Type> {

    private Type value = null;
    private int readCount = 0;
    private int maxReads = 5;
    private double lastUpdateTime = 0.0;
    private double staleTime = -1.0;
    private boolean isTimeBased = true;

    // Constructor for Time-based expiration
    public CachedData(double staleTime) {
        this.isTimeBased = true;
        this.staleTime = staleTime * 1000; // Convert seconds to milliseconds
        this.lastUpdateTime = System.currentTimeMillis();
    }

    // Constructor for readCount-based expiration
    public CachedData(int maxReads) {
        this.isTimeBased = false;
        this.maxReads = maxReads;
        this.readCount = 0;
    }

    // Update the value based on the change (for both time and read-based mechanisms)
    private void update() {
        if (isStale()){
            value = null;
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
        readCount++;
        return value; // Return the cached value
    }

    // Check if the cache has expired (is stale)
    public boolean isStale() {
        if (isTimeBased) {
            double currentTime = System.currentTimeMillis();
            return currentTime - lastUpdateTime >= staleTime; // Check if time has passed
        } else {
            return readCount > maxReads; // Check if the read count has exceeded
        }
    }

    // Reset the expiration tracking
    private void reset() {
        if (isTimeBased) {
            lastUpdateTime =
                    System.currentTimeMillis(); // Reset the last update time for time-based
            // expiration
        } else {
            readCount = 0; // Reset the read count for read-based expiration
        }
    }
}
