package willow.train.kuayue.block.recipe;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.initial.recipe.AllRecipeBlock;
import willow.train.kuayue.systems.tech_tree.player.PlayerDataManager;

public class BlueprintBlock extends Block implements IBE<BlueprintBlockEntity> {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final VoxelShape SN_SHAPE = Block.box(-11, 1, -2, 27, 18, 18),
                                    WE_SHAPE = Block.box(-2, 1, -11, 18, 18, 27);
    public BlueprintBlock(Properties p_52591_) {
        super(p_52591_);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return super.getStateForPlacement(pContext)
                .setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction facing = pState.getValue(FACING);
        if (facing == Direction.EAST || facing == Direction.WEST) return WE_SHAPE;
        else return SN_SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level,
                                 BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide())
            return InteractionResult.PASS;
        BlockEntity entity = level.getBlockEntity(pos);
        if (!(entity instanceof BlueprintBlockEntity bpEntity))
            return InteractionResult.PASS;
        ServerPlayer serverPlayer = (ServerPlayer) player;
        PlayerDataManager.MANAGER.updateDataToClient(serverPlayer);
        NetworkHooks.openScreen(serverPlayer, bpEntity);
        return InteractionResult.SUCCESS;
    }

    @Override
    public Class<BlueprintBlockEntity> getBlockEntityClass() {
        return BlueprintBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BlueprintBlockEntity> getBlockEntityType() {
        return (BlockEntityType<? extends BlueprintBlockEntity>)
                AllRecipeBlock.BLUEPRINT_TABLE.getBlockEntityReg().getType();
    }
}
