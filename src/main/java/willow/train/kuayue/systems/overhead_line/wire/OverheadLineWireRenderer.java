package willow.train.kuayue.systems.overhead_line.wire;

import kasuga.lib.core.client.model.anim_model.AnimModel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.systems.overhead_line.block.line.OverheadLineRenderer;
import willow.train.kuayue.systems.overhead_line.block.support.variants.AllOverheadLineSupportModels;
import willow.train.kuayue.systems.overhead_line.block.support.variants.OverheadLineEndCounterWeightRenderer;
import willow.train.kuayue.systems.overhead_line.render.OverheadLineCurveGenerator;
import willow.train.kuayue.systems.overhead_line.render.RenderCurve;

public class OverheadLineWireRenderer implements OverheadLineRenderer {

    public OverheadLineWireRenderer(){}

    @Override
    public RenderCurve getRenderCurveFor(Level level, Vec3 from, Vec3 to) {
        return OverheadLineCurveGenerator.conicHangLine(
                level,
                from,
                to,
                1.3f,
                1.3f,
                0.5f,
                5,
                0.05f
        );
    }

    @Override
    public AnimModel getModel() {
        return AllOverheadLineSupportModels.KUAYUE_TEST_LINE;
    }
}
