package willow.train.kuayue.systems.overhead_line.block.support.variants;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.systems.overhead_line.types.OverheadLineType;

import java.util.List;
import java.util.function.Predicate;

public record OverheadLineBlockDynamicConfiguration(
        ConnectionPointBuilder connectionPoints,
        Predicate<OverheadLineType> typePredictor,
        List<ResourceLocation> renderTypes
) {
    public static interface ConnectionPointBuilder {
        public List<Vec3> get(Level level, BlockPos blockPos, BlockState blockState);
    }
}
