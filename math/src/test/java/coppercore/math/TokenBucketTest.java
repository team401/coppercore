package coppercore.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

// This was written by Codex 5.5.
class TokenBucketTest {
    @Test
    void startsFull() {
        TokenBucket bucket = new TokenBucket(5, 2);

        assertTrue(bucket.consumeTokens(5));
        assertFalse(bucket.consumeTokens(1));
    }

    @Test
    void consumeTokensOnlyConsumesWhenEnoughTokensAreAvailable() {
        TokenBucket bucket = new TokenBucket(5, 2);

        assertFalse(bucket.consumeTokens(6));
        assertTrue(bucket.consumeTokens(5));
    }

    @Test
    void incrementRefillsByConfiguredAmount() {
        TokenBucket bucket = new TokenBucket(5, 2);

        assertTrue(bucket.consumeTokens(5));
        bucket.increment();

        assertTrue(bucket.consumeTokens(2));
        assertFalse(bucket.consumeTokens(1));
    }

    @Test
    void incrementDoesNotExceedMaxCapacity() {
        TokenBucket bucket = new TokenBucket(5, 4);

        assertTrue(bucket.consumeTokens(3));
        bucket.increment();

        assertTrue(bucket.consumeTokens(5));
        assertFalse(bucket.consumeTokens(1));
    }

    @Test
    void failedConsumeDoesNotChangeCurrentAmount() {
        TokenBucket bucket = new TokenBucket(5, 2);

        assertTrue(bucket.consumeTokens(4));
        assertFalse(bucket.consumeTokens(2));
        assertTrue(bucket.consumeTokens(1));
    }

    @ParameterizedTest
    @CsvSource({
        "4, 1, 1",
        "5, 2, 1",
        "6, 3, 1",
        "8, 1, 2",
        "10, 3, 2",
        "12, 4, 3",
        "15, 5, 3",
        "16, 2, 4",
        "20, 6, 4",
        "24, 8, 6",
        "25, 5, 7",
        "32, 10, 8"
    })
    void poissonArrivalsAreBurstAndRateLimited(
            int maxCapacity, int refillPerTick, int tokensPerArrival) {
        int ticks = 1_000;
        int slotsPerTick = 10;
        TokenBucket bucket = new TokenBucket(maxCapacity, refillPerTick);

        // Fixed seed keeps the random arrival process repeatable.
        Random random = new Random(401);

        // A sufficiently large first wave should consume the full bucket, but no more.
        int burstArrivals = (maxCapacity * 3) / tokensPerArrival + 1;
        int burstAccepted = consumeArrivals(bucket, burstArrivals, tokensPerArrival);
        assertTrue(burstArrivals * tokensPerArrival > maxCapacity);
        assertEquals((maxCapacity / tokensPerArrival) * tokensPerArrival, burstAccepted);

        // Sustained arrivals are intentionally spaced close enough together to
        // exceed the refill rate, so the bucket should only allow tokens
        // replenished during the simulation.
        int steadyStateAccepted = 0;
        int steadyStateRequested = 0;
        int tokensLeftAfterBurst = maxCapacity - burstAccepted;
        int nextArrivalSlot = 0;
        for (int slot = 0; slot < ticks * slotsPerTick; slot++) {
            if (slot % slotsPerTick == 0) {
                bucket.increment();
            }

            while (nextArrivalSlot <= slot) {
                steadyStateRequested += tokensPerArrival;
                if (bucket.consumeTokens(tokensPerArrival)) {
                    steadyStateAccepted += tokensPerArrival;
                }
                nextArrivalSlot += poissonSample(1.0, random);
            }
        }

        assertTrue(steadyStateRequested > tokensLeftAfterBurst + refillPerTick * ticks);
        assertTrue(steadyStateAccepted <= tokensLeftAfterBurst + refillPerTick * ticks);
    }

    private static int consumeArrivals(TokenBucket bucket, int arrivals, int tokensPerArrival) {
        // Each arrival attempts to consume the same configured number of tokens.
        int accepted = 0;
        for (int i = 0; i < arrivals; i++) {
            if (bucket.consumeTokens(tokensPerArrival)) {
                accepted += tokensPerArrival;
            }
        }
        return accepted;
    }

    private static int poissonSample(double mean, Random random) {
        // Knuth's algorithm for sampling a random spacing between arrivals.
        double threshold = Math.exp(-mean);
        int sample = 0;
        double product = 1.0;

        do {
            sample++;
            product *= random.nextDouble();
        } while (product > threshold);

        return sample - 1;
    }
}
