package willow.train.kuayue.block.panels.pantograph;


import kasuga.lib.core.util.data_type.Pair;

public interface IPantographBlockEntity {

    void setRisen(boolean risen);

    boolean isRisen();

    void setAngle(double angle);

    void resetAngle();

    double getYOffset();

    double getAngle();

    void setCache(CurrOverheadLineCache cache);

    CurrOverheadLineCache getCache();

    IPantographAngleMapping getPantographAngleMapping();

    void tick();
}
