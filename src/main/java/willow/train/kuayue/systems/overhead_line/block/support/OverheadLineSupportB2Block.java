package willow.train.kuayue.systems.overhead_line.block.support;

import net.minecraft.world.level.block.entity.BlockEntityType;

public class OverheadLineSupportB2Block extends OverheadLineSupportBlock<OverheadLineSupportB2BlockEntity>{
    public OverheadLineSupportB2Block(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Class<OverheadLineSupportB2BlockEntity> getBlockEntityClass() {
        return OverheadLineSupportB2BlockEntity.class;
    }

    @Override
    public BlockEntityType<OverheadLineSupportB2BlockEntity> getBlockEntityType() {
        return AllOverheadLineSupportBlocks.OVERHEAD_LINE_SUPPORT_B2_BLOCK_ENTITY.getType();
    }
}
