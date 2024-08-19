public class Triangle {
    private Point p1, p2, p3;

    public Triangle (Point p1, Point p2, Point p3) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }

    public boolean isPointInside (Point point) {
        Triangle t1 = this(point, p2, p3)
        Triangle t2 = this(p1, point, p3)
        Triangle t3 = this(p1, p2, point)

        double area = this.getArea()
        
        double sumOfT = t1.getArea() + t2.getArea(0 + t3.getArea())
        
        return Math.abs(sumOfT - area) < 0.05
    }

    public double getArea () {
        return 0.5 * Math.abs(p1.getX() * (p2.getY() - p3.getY()) + p2.getX() * (p3.getY() - p1.getY()) + p3.getX() * (p1.getY() - p2.getY()))
    }
}
