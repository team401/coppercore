package coppercore.geometry;

/**
 * This class extends ConvexPolygon to work on triangles specifically. It can construct a triangle
 * with three given points, find the area of the triangle, check if a given point is inside the
 * triangle, and whether or not a given point is on one of the lines of the triangle, and which line
 * it is on.
 */
public class Triangle extends ConvexPolygon {
    private Point p1, p2, p3;

    /**
     * This constructs the three points for the triangle.
     *
     * @param p1 the value of the triangles first point
     * @param p2 the value of the triangles second point
     * @param p3 the value of the triangles third point
     */
    public Triangle(Point p1, Point p2, Point p3) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }

    /**
     * This gets the first point of the triangle.
     *
     * @return returns the value of the first point of the triangle
     */
    public Point getPoint1() {
        return p1;
    }

    /**
     * This gets the second point of the triangle.
     *
     * @return returns the value of the second point of the triangle
     */
    public Point getPoint2() {
        return p2;
    }

    /**
     * This gets the third point of the triangle.
     *
     * @return returns the value of the third point of the triangle
     */
    public Point getPoint3() {
        return p3;
    }

    /**
     * This gets the area of the triangle
     *
     * @return returns the value of the area of the triangle
     */
    public double getArea() {
        return 0.5
                * Math.abs(
                        p1.getX() * (p2.getY() - p3.getY())
                                + p2.getX() * (p3.getY() - p1.getY())
                                + p3.getX() * (p1.getY() - p2.getY()));
    }

    /**
     * This checks if the triangle is containing a point
     *
     * @param point the point that we are checking to contain
     * @return whether or not the triangle contains that point
     */
    public boolean containsPoint(Point point) {
        Triangle t1 = new Triangle(point, this.getPoint2(), this.getPoint3());
        Triangle t2 = new Triangle(this.getPoint1(), point, this.getPoint3());
        Triangle t3 = new Triangle(this.getPoint1(), this.getPoint2(), point);

        double area = this.getArea();

        double sumOfT = t1.getArea() + t2.getArea() + t3.getArea();

        return Math.abs(sumOfT - area) < 0.05;
    }

    /**
     * This function checks if a point is colliding with the triangle
     *
     * @param point the point that we are checking for
     * @return whether or not the point is on any of the lines of the triangle and which line it is
     *     on
     */
    public boolean isPointColliding(Point point) {
        Line line1 = new Line(this.getPoint1(), this.getPoint2());
        Line line2 = new Line(this.getPoint2(), this.getPoint3());
        Line line3 = new Line(this.getPoint3(), this.getPoint1());

        return this.containsPoint(point)
                || line1.isPointOnLine(point)
                || line2.isPointOnLine(point)
                || line3.isPointOnLine(point);
    }
}
