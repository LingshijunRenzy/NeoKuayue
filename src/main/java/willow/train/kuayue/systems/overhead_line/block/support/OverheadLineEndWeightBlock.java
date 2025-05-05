package willow.train.kuayue.systems.overhead_line.block.support;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class OverheadLineEndWeightBlock extends OverheadLineSupportBlock<OverheadLineEndWeightBlockEntity> {
    public OverheadLineEndWeightBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Class<OverheadLineEndWeightBlockEntity> getBlockEntityClass() {
        return OverheadLineEndWeightBlockEntity.class;
    }

    @Override
    public BlockEntityType<OverheadLineEndWeightBlockEntity> getBlockEntityType() {
        return AllOverheadLineSupportBlocks.OVERHEAD_LINE_END_WEIGHT_BLOCK_ENTITY.getType();
    }
}
