package willow.train.kuayue.systems.device.track.exit;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class StationExitBlock extends Block implements EntityBlock, IBE<StationExitBlockEntity> {
    public StationExitBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Class<StationExitBlockEntity> getBlockEntityClass() {
        return null;
    }

    @Override
    public BlockEntityType<? extends StationExitBlockEntity> getBlockEntityType() {
        return null;
    }
}
