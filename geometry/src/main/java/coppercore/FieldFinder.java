package coppercore.geometry;

import java.awt.geom.Line2D;

public class FieldFinder {
    public Triangle triangle;

    public FieldFinder(Triangle triangle) {
        this.triangle = triangle;
    }

    public boolean isPointInside (Point point) {
        Triangle t1 = new Triangle(point, triangle.getPoint2(), triangle.getPoint3());
        Triangle t2 = new Triangle(triangle.getPoint1(), point, triangle.getPoint3());
        Triangle t3 = new Triangle(triangle.getPoint1(), triangle.getPoint2(), point);

        double area = triangle.getArea();
        
        double sumOfT = t1.getArea() + t2.getArea() + t3.getArea();
        
        return Math.abs(sumOfT - area) < 0.05;
    }
}
