package coppercore.math;

/**
 * A token bucket that tracks a capped number of tokens for rate-limiting or budgeting repeated
 * actions.
 *
 * <p>The bucket starts full, can be refilled in fixed increments up to its maximum capacity, and
 * only consumes tokens when the requested amount is available.
 */
public class TokenBucket {
    private final int maxCapacity;
    private final int incrementAmount;
    private int currentAmount;

    /**
     * Creates a TokenBucket that is full.
     *
     * @param maxCapacity the maximum capacity of the bucket
     * @param incrementAmount how much the bucket should increment by when called
     */
    public TokenBucket(int maxCapacity, int incrementAmount) {
        this.maxCapacity = maxCapacity;
        this.incrementAmount = incrementAmount;
        this.currentAmount = maxCapacity;
    }

    /**
     * Increments the amount of tokens in the bucket by the amount given in the constructor, without
     * overflowing.
     */
    public void increment() {
        currentAmount = Math.min(currentAmount + incrementAmount, maxCapacity);
    }

    /**
     * Consumes the given amount of tokens, if possible.
     *
     * @param numTokens amount of tokens to be consumed
     * @return true if the given amount of tokens were consumed
     */
    public boolean consumeTokens(int numTokens) {
        if (currentAmount >= numTokens) {
            currentAmount -= numTokens;
            return true;
        }
        return false;
    }
}
