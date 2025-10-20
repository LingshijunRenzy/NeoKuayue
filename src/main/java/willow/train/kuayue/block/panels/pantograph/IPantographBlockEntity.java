package willow.train.kuayue.block.panels.pantograph;


import kasuga.lib.core.util.data_type.Pair;

public interface IPantographBlockEntity {
    public void setRisen(boolean risen);

    boolean isRisen();

    void setAngle(Pair<Double, Double> angle);

    double getYOffset();

    Pair<Double, Double> getAngle();

    void setCache(CurrOverheadLineCache cache);

    CurrOverheadLineCache getCache();

    IPantographAngleMapping getPantographAngleMapping();
}
