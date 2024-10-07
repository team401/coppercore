package coppercore.geometry;

public class Line {
    private double x1;
    private double y1;
    private double x2;
    private double y2;

    public Line(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public Line(Point point1, Point point2) {
        this.x1 = point1.getX();
        this.y1 = point1.getY();
        this.x2 = point2.getX();
        this.y2 = point2.getY();
    }

    public boolean isPointOnLine(Point point) {
        // if Distance(a, b) == Distance(A, c) + Distance(c, b) then c is on the line ab
        // tolerance of 1e-10 accepted
        double distanceAC = new Line(x1, y1, point.getX(), point.getY()).getDistance();
        double distanceCB = new Line(point.getX(), point.getY(), x2, y2).getDistance();

        return Math.abs(this.getDistance() - (distanceAC + distanceCB)) <= 1e-10;
    }

    public double getDistance() {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}