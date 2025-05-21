package willow.train.kuayue.systems.device.driver.seat;

public enum AnchorPoint {
    TOP_LEFT(0, 0),
    TOP_CENTER(0.5f, 0),
    TOP_RIGHT(1, 0),
    MIDDLE_LEFT(0, 0.5f),
    MIDDLE_CENTER(0.5f, 0.5f),
    MIDDLE_RIGHT(1, 0.5f),
    BOTTOM_LEFT(0, 1),
    BOTTOM_CENTER(0.5f, 1),
    BOTTOM_RIGHT(1, 1);

    private final float xFactor;
    private final float yFactor;

    AnchorPoint(float xFactor, float yFactor) {
        this.xFactor = xFactor;
        this.yFactor = yFactor;
    }

    public float getXFactor() {
        return xFactor;
    }

    public float getYFactor() {
        return yFactor;
    }
} 