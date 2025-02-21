package willow.train.kuayue.block.panels.pantograph;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.block.panels.block_entity.SingleArmPantographBlockEntity;
import willow.train.kuayue.initial.AllBlocks;

public class SingleArmPantographBlock extends Block implements IBE<SingleArmPantographBlockEntity>, IWrenchable {

    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final PantographProps pantographType;

    public SingleArmPantographBlock(Properties pProperties, PantographProps pantographType) {
        super(pProperties);
        this.pantographType = pantographType;
        registerDefaultState(this.getStateDefinition().any()
                .setValue(ENABLED, false)
                .setValue(FACING, Direction.EAST));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(ENABLED, FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return super.getStateForPlacement(pContext)
                .setValue(ENABLED, false)
                .setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if(!level.isClientSide()) {
            level.setBlock(pos, state.cycle(ENABLED),3);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public Class<SingleArmPantographBlockEntity> getBlockEntityClass() {
        return SingleArmPantographBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SingleArmPantographBlockEntity> getBlockEntityType() {
        return AllBlocks.HXD3D_PANTOGRAPH_ENTITY.getType();
    }

    public PantographProps getPantographType() {
        return pantographType;
    }
}
