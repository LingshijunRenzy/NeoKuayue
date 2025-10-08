package willow.train.kuayue.systems.overhead_line.block.support;

import net.minecraft.world.level.block.entity.BlockEntityType;

public class OverheadLineSupportBBlock extends OverheadLineSupportBlock<OverheadLineSupportBBlockEntity> {
    public OverheadLineSupportBBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Class<OverheadLineSupportBBlockEntity> getBlockEntityClass() {
        return OverheadLineSupportBBlockEntity.class;
    }

    @Override
    public BlockEntityType<OverheadLineSupportBBlockEntity> getBlockEntityType() {
        return AllOverheadLineSupportBlocks.OVERHEAD_LINE_SUPPORT_B_BLOCK_ENTITY.getType();
    }
}
