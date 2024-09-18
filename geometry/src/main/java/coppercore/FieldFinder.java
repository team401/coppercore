package coppercore.geometry;

public class FieldFinder {
    public ConvexPolygon polygon;

    public FieldFinder(ConvexPolygon polygon) {
        this.polygon = polygon;
    }

    public void setShape(ConvexPolygon polygon) {
        this.polygon = polygon;
    }

    public boolean isRobotInShape(Point p) {
        return this.polygon.containsPoint(p);
    }
}
