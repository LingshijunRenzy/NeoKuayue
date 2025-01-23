package willow.train.kuayue.systems.device.driver.path.range;

import kasuga.lib.core.util.data_type.Couple;
import willow.train.kuayue.systems.device.driver.path.renderer.DirtyPathLineListener;
import willow.train.kuayue.systems.device.driver.path.list.EdgePathLineList;
import willow.train.kuayue.systems.device.driver.path.list.SpeedEdgePathLine;

import java.util.HashSet;

public class LineRange<T extends SpeedEdgePathLine>
        extends LinearRange
        implements DirtyPathLineListener<T>
{
    EdgePathLineList<T> lineList;

    public LineRange(EdgePathLineList<T> lineList){
        super();
        this.lineList = lineList;
        lineList.setListener(this);
    }

    @Override
    public void markDirty() {
        notifyUpdate();
    }

    @Override
    public void calculateRange() {
        this.range.clear();
        for (T line : lineList.getFrontElements()){
            range.computeIfAbsent(
                    line.distance(),
                    k -> new HashSet<>()
            ).add(
                    Couple.couple(
                            line.distance() + line.length(),
                            line.getSpeed()
                    )
            );
        }

        for (T line : lineList.getCurrentElements()){
            range.computeIfAbsent(
                    line.distance(),
                    k -> new HashSet<>()
            ).add(
                    Couple.couple(
                            line.distance() + line.length(),
                            line.getSpeed()
                    )
            );
        }

        for (T line : lineList.getBackElements()){
            range.computeIfAbsent(
                    line.distance(),
                    k -> new HashSet<>()
            ).add(
                    Couple.couple(
                            line.distance() + line.length(),
                            line.getSpeed()
                    )
            );
        }

    }
}
