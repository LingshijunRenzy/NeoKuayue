package willow.train.kuayue.systems.overhead_line.block.support;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import willow.train.kuayue.systems.overhead_line.wire.AllWires;


public class OverheadLineEndWeightBlockEntity extends OverheadLineSupportBlockEntity {

    public enum RenderState {
        EMPTY,
        SINGLE,
        DUAL
    }

    protected int height = 0;

    protected RenderState renderState = RenderState.EMPTY;

    public OverheadLineEndWeightBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
    }

    public OverheadLineEndWeightBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
        setLazyTickRate(20);
    }


    @Override
    public void lazyTick() {
        super.lazyTick();
        if(this.level == null || !this.level.isClientSide)
            return;
        refreshHeight(this.level);
    }


    public void refreshHeight(Level level) {
        ChunkAccess chunkAccess = level.getChunk(this.getBlockPos());
        BlockPos blockPos = this.getBlockPos().below();
        for(;blockPos.getY() > chunkAccess.getMinBuildHeight(); blockPos = blockPos.below()) {
            if(!level.getBlockState(blockPos).isAir()) {
                this.height = this.getBlockPos().getY() - blockPos.getY();
                break;
            }
        }
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void onConnectionModification() {
        super.onConnectionModification();
        if(connections.isEmpty())
            renderState = RenderState.EMPTY;
        else if(connections.parallelStream().anyMatch((connection)-> connection.type() == AllWires.OVERHEAD_LINE_WIRE.getWireType()))
            renderState = RenderState.DUAL;
        else
            renderState = RenderState.SINGLE;
    }

    public RenderState getRenderState() {
        return renderState;
    }
}
