package willow.train.kuayue.systems.overhead_line.block.line;

import kasuga.lib.core.client.model.anim_model.AnimModel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.systems.overhead_line.render.RenderCurve;

public interface OverheadLineRenderer {
    RenderCurve getRenderCurveFor(Level level, Vec3 from, Vec3 to);

    AnimModel getModel();
}
