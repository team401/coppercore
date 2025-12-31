package coppercore.controls.test;

import java.util.Comparator;
import java.util.PriorityQueue;

/*
 * Discrete Event Simulation engine for testing state machines
 */
public class DES {
    int currentTime;
    PriorityQueue<Event> eventQueue =
            new PriorityQueue<>(
                    Comparator.comparingInt((Event e) -> e.when)
                            .thenComparing(System::identityHashCode));

    record Event(int when, Runnable runnable) {}

    @FunctionalInterface
    interface Runnable {
        /**
         * @param simulationTime the current simulation time
         */
        void run(int simulationTime);
    }

    /**
     * Run simulation until endTime is reached or we run out of events to simulate.
     *
     * @param endTime time of last event to be simulated, inclusive.
     */
    void simulate(int endTime) {
        while (!eventQueue.isEmpty() && eventQueue.peek().when <= endTime) {
            Event next = eventQueue.poll();
            currentTime = next.when;
            next.runnable.run(currentTime);
        }
    }

    /**
     * Schedule an event for simulation.
     *
     * @param time at which event will run.
     * @param runnable a runnable to execute then.
     */
    void schedule(int time, Runnable runnable) {
        eventQueue.offer(new Event(time, runnable));
    }
}
