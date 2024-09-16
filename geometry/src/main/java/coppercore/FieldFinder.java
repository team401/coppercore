package coppercore.geometry;

public class FieldFinder {
    public Triangle triangle;

    public FieldFinder(Triangle triangle) {
        this.triangle = triangle;
    }

    public void setTriangle(Triangle triangle) {
        this.triangle = triangle;
    }

    public boolean isPointInsideTriangle(Point point) {
        Triangle t1 = new Triangle(point, triangle.getPoint2(), triangle.getPoint3());
        Triangle t2 = new Triangle(triangle.getPoint1(), point, triangle.getPoint3());
        Triangle t3 = new Triangle(triangle.getPoint1(), triangle.getPoint2(), point);

        double area = triangle.getArea();

        double sumOfT = t1.getArea() + t2.getArea() + t3.getArea();

        return Math.abs(sumOfT - area) < 0.05;
    }

    public boolean isPointCollidingWithTriangle(Point point) {
        Line line1 = new Line(triangle.getPoint1(), triangle.getPoint2());
        Line line2 = new Line(triangle.getPoint2(), triangle.getPoint3());
        Line line3 = new Line(triangle.getPoint3(), triangle.getPoint1());

        return isPointInsideTriangle(point)
                || line1.isPointOnLine(point)
                || line2.isPointOnLine(point)
                || line3.isPointOnLine(point);
    }
}
