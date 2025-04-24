package willow.train.kuayue.systems.overhead_line.block.support;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public class OverheadLineSupportInsulatorBlock extends NormalOverheadLineSupportBlock {
    public static final BooleanProperty WALL = BooleanProperty.create("wall");
    public OverheadLineSupportInsulatorBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected BlockState getDefaultState() {
        return
                super.getDefaultState()
                        .setValue(WALL, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(WALL);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return super.getStateForPlacement(pContext)
                .setValue(WALL, pContext.getClickedFace().getAxis() != Direction.Axis.Y);
    }
}
