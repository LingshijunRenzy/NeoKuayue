package willow.train.kuayue.systems.overhead_line.wire;

import kasuga.lib.core.client.model.anim_model.AnimModel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.systems.overhead_line.block.line.OverheadLineRenderer;
import willow.train.kuayue.systems.overhead_line.block.support.variants.AllOverheadLineSupportModels;
import willow.train.kuayue.systems.overhead_line.render.OverheadLineCurveGenerator;
import willow.train.kuayue.systems.overhead_line.render.RenderCurve;

public class ElectronicLineRenderer implements OverheadLineRenderer {

    public ElectronicLineRenderer(){}

    @Override
    public RenderCurve getRenderCurveFor(Level level, Vec3 from, Vec3 to) {
        return
                OverheadLineCurveGenerator.catenaryLine(
                        level,
                        from,
                        to,
                        0.2f,
                        0.05f
                );
    }

    @Override
    public AnimModel getModel() {
        return AllOverheadLineSupportModels.KUAYUE_TEST_LINE;
    }
}
