package willow.train.kuayue.block.panels.pantograph;

public interface IPantographAngleMapping {

    double getAngleByHeight(double bowHeight);

    double getBaseHeight();

    boolean readyForMap();
}
