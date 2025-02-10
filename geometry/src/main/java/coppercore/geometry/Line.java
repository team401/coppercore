package coppercore.geometry;

/** This class makes a line and checks if a point is on that line and how long the line is. */
public class Line {
    private double x1;
    private double y1;
    private double x2;
    private double y2;

    /**
     * This makes the x and y values for the two points that make up the line.
     *
     * @param x1 x-position of point 1
     * @param y1 y-position of point 1
     * @param x2 x-position of point 2
     * @param y2 y-position of point 2
     */
    public Line(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    /**
     * This sets the x and y values that we choose earlier onto the points.
     *
     * @param point1 The position of the first point
     * @param point2 The position of the second point
     */
    public Line(Point point1, Point point2) {
        this.x1 = point1.getX();
        this.y1 = point1.getY();
        this.x2 = point2.getX();
        this.y2 = point2.getY();
    }

    /**
     * This checks if a given point is on the line we set earlier.
     *
     * @param point this is the point that we are checking for
     * @return returns whether or not the point is on the line
     */
    public boolean isPointOnLine(Point point) {
        // if Distance(a, b) == Distance(A, c) + Distance(c, b) then c is on the line ab
        // tolerance of 1e-10 accepted
        double distanceAC = new Line(x1, y1, point.getX(), point.getY()).getDistance();
        double distanceCB = new Line(point.getX(), point.getY(), x2, y2).getDistance();

        return Math.abs(this.getDistance() - (distanceAC + distanceCB)) <= 1e-10;
    }

    /**
     * This gives the distance of the line that we set.
     *
     * @return returns the distance of the line that we set.
     */
    public double getDistance() {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}
