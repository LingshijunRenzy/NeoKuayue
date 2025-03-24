package willow.train.kuayue.systems.overhead_line.block.support.variants;

import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.systems.overhead_line.types.OverheadLineType;

import java.util.List;
import java.util.function.Predicate;

public record OverheadLineBlockDynamicConfiguration(
        List<Vec3> connectionPoints,
        Predicate<OverheadLineType> typePredictor
) {

}
