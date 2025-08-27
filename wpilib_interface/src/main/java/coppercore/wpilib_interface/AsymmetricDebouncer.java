package coppercore.wpilib_interface;

import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.math.filter.Debouncer.DebounceType;

/**
 * An asymmetric debouncer: debounces both the rising and falling edge and allows for having
 * different rise and fall times.
 *
 * <p>Debouncers will initialize to {@code false} as their initial/baseline state.
 */
public class AsymmetricDebouncer {
    private boolean lastOutput = false;

    private final Debouncer riseDebouncer;
    private final Debouncer fallDebouncer;

    /**
     * Creates a new AsymmetricDebouncer.
     *
     * @param riseTimeSeconds The number of seconds the value must change from false to true for the
     *     filtered value to change to true.
     * @param fallTimeSeconds The number of seconds the value must change from true to false for the
     *     filtered value to change to false.
     */
    public AsymmetricDebouncer(double riseTimeSeconds, double fallTimeSeconds) {
        riseDebouncer = new Debouncer(riseTimeSeconds, DebounceType.kRising);
        fallDebouncer = new Debouncer(fallTimeSeconds, DebounceType.kFalling);
    }

    /**
     * Applies the debouncer to the input stream.
     *
     * @param input The current value of the input stream.
     * @return The debounced value of the input stream.
     */
    public boolean calculate(boolean input) {
        boolean riseOutput = riseDebouncer.calculate(input);
        boolean fallOutput = fallDebouncer.calculate(input);

        // The outputs of this method are based on the following possible cases:
        //  - Input off -> both off -> output off
        //  - Input on -> fall turns on before rise -> output off
        //  - Input on -> rise on -> output on
        //  - Input off -> rise turns off, fall still on -> output on
        //  - Input off -> fall turns off -> output off

        // The reasoning here is that:
        //  - If the last output was false, wait for the rise debouncer to let output rise to true
        //  - If the last output was true, wait for the fall debouncer to let output fall to false
        lastOutput = (riseOutput && !lastOutput) || (fallOutput && lastOutput);
        return lastOutput;
    }
}
