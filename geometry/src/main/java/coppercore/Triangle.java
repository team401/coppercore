public class Triangle {
    private Point p1, p2, p3;

    public Triangle (Point p1, Point p2, Point p3) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }

    public Point getPoint1 () {
        return p1;
    }

    public Point getPoint2 () {
        return p2;
    }

    public Point getPoint3 () {
        return p3;
    }

    public double getArea () {
        return 0.5 * Math.abs(p1.getX() * (p2.getY() - p3.getY()) + p2.getX() * (p3.getY() - p1.getY()) + p3.getX() * (p1.getY() - p2.getY()));
    }
}
