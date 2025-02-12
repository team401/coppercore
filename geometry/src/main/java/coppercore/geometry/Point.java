package coppercore.geometry;

/** This class sets a point and gets its x and y values. */
public class Point {
    private double x;
    private double y;

    /**
     * This sets a point
     *
     * @param x the x-value of the point
     * @param y the y-value of the point
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * This gets the point's x-value
     *
     * @return the point's x-value
     */
    public double getX() {
        return x;
    }

    /**
     * This gets the point's y-value
     *
     * @return the point's y-value
     */
    public double getY() {
        return y;
    }
}
