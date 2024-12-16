package coppercore.geometry;

public abstract class ConvexPolygon {
    public abstract double getArea();

    public abstract boolean containsPoint(Point p);

    public abstract boolean isPointColliding(Point p);
}
