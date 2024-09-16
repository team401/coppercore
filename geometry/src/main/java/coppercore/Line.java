package coppercore.geometry;

public class Line {
    private double x1;
    private double y1;
    private double x2;
    private double y2;
    private double slope;

    public Line(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.slope = (y2 - y1) / (x2 - x1);
    }

    public Line(Point point1, Point point2) {
        this.x1 = point1.getX();
        this.y1 = point1.getY();
        this.x2 = point2.getX();
        this.y2 = point2.getY();
        this.slope = (this.y2 - this.y1) / (this.x2 - this.x1);
    }

    public boolean isPointOnLine(Point point) {
        return point.getY() == point.getX() * slope
                && (x1 < point.getX() && point.getX() < x2)
                && (y1 < point.getY() && point.getY() < y2);
    }
}
