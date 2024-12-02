package coppercore.geometry;

public class Triangle extends ConvexPolygon {
    private Point p1, p2, p3;

    public Triangle(Point p1, Point p2, Point p3) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }

    public Point getPoint1() {
        return p1;
    }

    public Point getPoint2() {
        return p2;
    }

    public Point getPoint3() {
        return p3;
    }

    public double getArea() {
        return 0.5
                * Math.abs(
                        p1.getX() * (p2.getY() - p3.getY())
                                + p2.getX() * (p3.getY() - p1.getY())
                                + p3.getX() * (p1.getY() - p2.getY()));
    }

    public boolean containsPoint(Point point) {
        Triangle t1 = new Triangle(point, this.getPoint2(), this.getPoint3());
        Triangle t2 = new Triangle(this.getPoint1(), point, this.getPoint3());
        Triangle t3 = new Triangle(this.getPoint1(), this.getPoint2(), point);

        double area = this.getArea();

        double sumOfT = t1.getArea() + t2.getArea() + t3.getArea();

        return Math.abs(sumOfT - area) < 0.05;
    }

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
