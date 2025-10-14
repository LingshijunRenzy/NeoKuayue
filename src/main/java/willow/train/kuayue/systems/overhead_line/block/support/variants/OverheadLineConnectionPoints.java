package willow.train.kuayue.systems.overhead_line.block.support.variants;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineSupportInsulatorBlock;

import java.util.List;

public class OverheadLineConnectionPoints {
    public static List<Vec3> getInsulatorAConnectionPointIf(Level level, BlockPos blockPos, BlockState blockState) {
        if(blockState.getValue(OverheadLineSupportInsulatorBlock.WALL)) {
            return List.of(
                    new Vec3(0.55, 1.85, -0.03)
            );
        } else {
            return List.of(
                    new Vec3(-0.12, 0.9, 0.03)
            );
        }
    }

    public static List<Vec3> getInsulatorBConnectionPointIf(Level level, BlockPos blockPos, BlockState blockState) {
        if(blockState.getValue(OverheadLineSupportInsulatorBlock.WALL)) {
            return List.of(
                    new Vec3(0.55, -0.7, 0.05)
            );
        } else {
            return List.of(
                    new Vec3(-0.12, 0, 0)
            );
        }
    }

    public static List<Vec3> getEndCounterWeightConnectionPointIf(Level level, BlockPos blockPos, BlockState blockState) {
        return List.of(new Vec3(2.5,0.075,0));
    }
}
