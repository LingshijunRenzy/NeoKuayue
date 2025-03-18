package willow.train.kuayue.systems.overhead_line.block.support;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.systems.overhead_line.OverheadLineSystem;

public class OverheadLineSupportBlock extends Block implements IBE<OverheadLineSupportBlockEntity>, IWrenchable {
    public OverheadLineSupportBlock(Properties pProperties) {
        super(pProperties.noOcclusion());
        this.registerDefaultState(getDefaultState());
    }

    protected BlockState getDefaultState(){
        return this.getStateDefinition().any()
                .setValue(FACING, Direction.EAST);
    }

    @Override
    public Class<OverheadLineSupportBlockEntity> getBlockEntityClass() {
        return OverheadLineSupportBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends OverheadLineSupportBlockEntity> getBlockEntityType() {
        return OverheadLineSystem.OVERHEAD_LINE_SUPPORT_BLOCK_ENTITY.getType();
    }

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        IBE.onRemove(state, level, pos, newState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return super.getStateForPlacement(pContext).setValue(FACING, pContext.getHorizontalDirection());
    }
}
