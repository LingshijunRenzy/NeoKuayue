package willow.train.kuayue.systems.overhead_line.block.support;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.systems.overhead_line.types.OverheadLineType;
import willow.train.kuayue.systems.overhead_line.wire.AllWires;

public class OverheadLineSupportBBlockEntity extends OverheadLineSupportBlockEntity{

    public OverheadLineSupportBBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    public OverheadLineSupportBBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public Vec3 getConnectionPointByIndex(int index, OverheadLineType wireType) {
        Vec3 basePoint = getActualConnectionPoints().get(index);

        float offset = 0f;
        if(wireType.equals(AllWires.OVERHEAD_LINE_WIRE.getWireType())){
            offset = -1.3f;
        }

        return basePoint.add(0, offset, 0);
    }
}

