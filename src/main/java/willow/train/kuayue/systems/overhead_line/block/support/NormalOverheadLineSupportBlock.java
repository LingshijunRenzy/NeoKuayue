package willow.train.kuayue.systems.overhead_line.block.support;

import net.minecraft.world.level.block.entity.BlockEntityType;
import willow.train.kuayue.systems.overhead_line.OverheadLineSystem;

public class NormalOverheadLineSupportBlock extends OverheadLineSupportBlock<OverheadLineSupportBlockEntity> {
    public NormalOverheadLineSupportBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Class<OverheadLineSupportBlockEntity> getBlockEntityClass() {
        return OverheadLineSupportBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends OverheadLineSupportBlockEntity> getBlockEntityType() {
        return OverheadLineSystem.OVERHEAD_LINE_SUPPORT_BLOCK_ENTITY.getType();
    }
}
