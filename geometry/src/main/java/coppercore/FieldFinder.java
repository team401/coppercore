package coppercore.geometry;

/**
 * This checks if a point(which could be a robot) is in a defined polygon(which could be a field)
 */
public class FieldFinder {
    public ConvexPolygon polygon;

    /** This creates the convex polygon */
    public FieldFinder(ConvexPolygon polygon) {
        this.polygon = polygon;
    }

    /** This updates the polygon and allows us to change its shape */
    public void setShape(ConvexPolygon polygon) {
        this.polygon = polygon;
    }

    /** This checks if a given point is in the convex polygon */
    public boolean isRobotInShape(Point p) {
        return this.polygon.containsPoint(p);
    }
}
