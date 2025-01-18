package coppercore.geometry;

/**
 * This class allows us to be able to get the area of a given convex polygon. It tells us if a given
 * point is inside that polygon and checks to see if a given point is on one of the edges of the
 * polygon.
 */
public abstract class ConvexPolygon {
    /** This gets the area of the convex polygon */
    public abstract double getArea();

    /** This checks if a point is inside the boundaries of the convex polygon */
    public abstract boolean containsPoint(Point p);

    /** This checks if a point is on one of the edges of the convex polygon */
    public abstract boolean isPointColliding(Point p);
}
