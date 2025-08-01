package willow.train.kuayue.systems.overhead_line.block.support;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.systems.overhead_line.wire.AllWires;

import java.util.ArrayList;
import java.util.List;

public class OverheadLineSupportB2BlockEntity extends OverheadLineSupportBlockEntity{

    public OverheadLineSupportB2BlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public OverheadLineSupportB2BlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    @Override
    public List<Vec3> getActualConnectionPoints() {
        List<Vec3> basePoints = super.getActualConnectionPoints();
        List<Vec3> adjustedPoints = new ArrayList<>();
        
        for(int i = 0; i < basePoints.size(); i++) {
            Vec3 point = basePoints.get(i);

            int finalI = i;
            boolean hasOverheadLineWire = connections.stream()
                .anyMatch(conn -> conn.connectionIndex() == finalI &&
                         conn.type() == AllWires.OVERHEAD_LINE_WIRE.getWireType());
            
            if(hasOverheadLineWire) {
                // 接触网线：Y轴下调到-1.9
                point = new Vec3(point.x(), point.y() - 1.9 + 0.6, point.z());
            }
            
            adjustedPoints.add(point);
        }
        
        return adjustedPoints;
    }

    @Override
    public Vec3 getConnectionPointByIndex(int index) {
        List<Vec3> actualPoints = getActualConnectionPoints();
        return index >= actualPoints.size() ? Vec3.atCenterOf(getBlockPos()) : actualPoints.get(index);
    }
}
