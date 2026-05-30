package coppercore.geometry;

import edu.wpi.first.math.geometry.Translation2d;

public class Rectangle {
    protected final Translation2d bottomLeft;
    protected final double width;
    protected final double height;
    private Translation2d[] outline;

    /**
     * Creates an axis-aligned rectangle from its bottom-left corner.
     *
     * @param bottomLeft bottom-left corner of the rectangle
     * @param width rectangle width
     * @param height rectangle height
     */
    public Rectangle(Translation2d bottomLeft, double width, double height) {
        this.bottomLeft = bottomLeft;
        this.width = width;
        this.height = height;
        this.outline =
                new Translation2d[] {
                    bottomLeft,
                    bottomLeft.plus(new Translation2d(width, 0)),
                    bottomLeft.plus(new Translation2d(width, height)),
                    bottomLeft.plus(new Translation2d(0, height)),
                    bottomLeft,
                };
    }

    /**
     * Creates an axis-aligned rectangle from its center point.
     *
     * @param center center of the rectangle
     * @param width rectangle width
     * @param height rectangle height
     * @return rectangle centered at the given point
     */
    public static Rectangle fromCenter(Translation2d center, double width, double height) {
        return new Rectangle(center.minus(new Translation2d(width / 2, height / 2)), width, height);
    }

    /**
     * Checks whether a point is inside or on the rectangle.
     *
     * @param point point to test
     * @return true if the point is inside the rectangle bounds
     */
    public boolean contains(Translation2d point) {
        return bottomLeft.getX() <= point.getX()
                && point.getX() <= bottomLeft.getX() + width
                && bottomLeft.getY() <= point.getY()
                && point.getY() <= bottomLeft.getY() + height;
    }

    /**
     * Gets the closed outline of the rectangle.
     *
     * @return corners in order, ending again at the first corner
     */
    public Translation2d[] getOutline() {
        return outline;
    }
}
