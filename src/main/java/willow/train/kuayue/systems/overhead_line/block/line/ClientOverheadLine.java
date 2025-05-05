package willow.train.kuayue.systems.overhead_line.block.line;

import kasuga.lib.core.client.model.anim_model.AnimModel;
import kasuga.lib.core.util.LazyRecomputable;
import kasuga.lib.core.util.data_type.Pair;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.systems.overhead_line.render.RenderCurve;

public class ClientOverheadLine {
    OverheadLineRenderer renderType;
    Vec3 fromPosition;
    Vec3 toPosition;
    Level level;

    LazyRecomputable<RenderCurve> curve =
            LazyRecomputable.of(
                    ()-> fromPosition == null || toPosition == null ? null :
                    this.renderType.getRenderCurveFor(level, fromPosition, toPosition)
            );
    LazyRecomputable<AABB> boundingBox =
            LazyRecomputable.of(()->curve.get() == null ? null : curve.get().getMatrix().getBoundingBox());


    public ClientOverheadLine(OverheadLineRenderer renderType, Level level, Vec3 position1, Vec3 position2) {
        this.renderType = renderType;
        this.level = level;
        this.setPosition(position1, position2);
    }

    public void setPosition(Vec3 position1, Vec3 position2) {
        if(PositionComparator.comparePosition(position1, position2) > 0){
            this.fromPosition = position2;
            this.toPosition = position1;
        } else {
            this.fromPosition = position1;
            this.toPosition = position2;
        }
        this.curve.clear();
        this.boundingBox.clear();
    }


    public void setRenderType(OverheadLineRenderer renderer){
        this.renderType = renderer;
    }

    public RenderCurve getCurve() {
        return curve.get();
    }

    public AABB getBoundingBox() {
        return boundingBox.get();
    }

    public Pair<Vec3, Vec3> getPosition() {
        return Pair.of(fromPosition, toPosition);
    }

    public AnimModel getModel() {
        return this.renderType.getModel();
    }
}
