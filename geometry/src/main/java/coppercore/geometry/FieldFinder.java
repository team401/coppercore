package coppercore.geometry;

/**
 * This checks if a point(which could be a robot) is in a defined polygon(which could be a field)
 */
public class FieldFinder {
    /** This is a public variable polygon */
    public ConvexPolygon polygon;

    /**
     * This creates the convex polygon
     *
     * @param polygon a variable for the constructer of FieldFinder
     */
    public FieldFinder(ConvexPolygon polygon) {
        this.polygon = polygon;
    }

    /**
     * This updates the polygon and allows us to change its shape
     *
     * @param polygon a variable that is used to change the shape of the polygon
     */
    public void setShape(ConvexPolygon polygon) {
        this.polygon = polygon;
    }

    /**
     * This checks if a given point is in the convex polygon
     *
     * @param p a point that could be a robot that is being checked if it is in the polygon
     * @return a boolean for whether or not the polygon contains p
     */
    public boolean isRobotInShape(Point p) {
        return this.polygon.containsPoint(p);
    }
}
