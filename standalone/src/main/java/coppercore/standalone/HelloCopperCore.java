package coppercore.standalone;

import coppercore.geometry.Point;

/**
 * Minimal example of a standalone program that links against a coppercore library module.
 *
 * <p>Run it with:
 *
 * <pre>
 *   ./gradlew :standalone:run -PmainClass=coppercore.standalone.HelloCopperCore
 * </pre>
 *
 * <p>Delete this file once you have your own programs; it only exists as a template.
 */
public final class HelloCopperCore {
    private HelloCopperCore() {}

    public static void main(String[] args) {
        Point p = new Point(3.0, 4.0);
        double magnitude = Math.hypot(p.getX(), p.getY());
        System.out.printf(
                "Hello from coppercore! Point (%.1f, %.1f) has magnitude %.1f%n",
                p.getX(), p.getY(), magnitude);
    }
}
