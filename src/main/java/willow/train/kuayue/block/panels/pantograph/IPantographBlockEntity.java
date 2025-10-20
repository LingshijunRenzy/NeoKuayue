package willow.train.kuayue.block.panels.pantograph;

public interface IPantographBlockEntity {
    public void setRisen(boolean risen);

    boolean isRisen();

    void setAngle(double angle);

    double getYOffset();

    double getAngle();

    void setCache(CurrOverheadLineCache cache);

    CurrOverheadLineCache getCache();

    IPantographAngleMapping getPantographAngleMapping();
}
