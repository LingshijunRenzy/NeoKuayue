package willow.train.kuayue.block.panels.pantograph;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.block.panels.block_entity.SingleArmPantographBlockEntity;
import willow.train.kuayue.initial.AllBlocks;

import java.util.HashMap;
import java.util.Map;

import static willow.train.kuayue.block.panels.pantograph.IPantographModel.*;

public class SingleArmPantographBlock extends Block implements IBE<SingleArmPantographBlockEntity>, IWrenchable {

    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private final PantographProps pantographType;
    private final Map<String, PartialModel> pantographModel;

    public SingleArmPantographBlock(Properties pProperties, PantographProps pantographType,
                                    String basePath, String largeArmPath,
                                    String pullRodPath, String smallArmPath,
                                    String bowHeadPath) {
        super(pProperties);
        this.pantographType = pantographType;
        Map<String, PartialModel> map = new HashMap<>();
        map.put(BASE_MODEL, basePath == null ? null :
                new PartialModel(new ResourceLocation(Kuayue.MODID,"block/" + basePath)));
        map.put(LARGE_ARM_MODEL, largeArmPath == null ? null :
                new PartialModel(new ResourceLocation(Kuayue.MODID,"block/" + largeArmPath)));
        map.put(PULL_ROD_MODEL, pullRodPath == null ? null :
                new PartialModel(new ResourceLocation(Kuayue.MODID,"block/" + pullRodPath)));
        map.put(SMALL_ARM_MODEL, smallArmPath == null ? null :
                new PartialModel(new ResourceLocation(Kuayue.MODID,"block/" + smallArmPath)));
        map.put(BOW_HEAD_MODEL, bowHeadPath == null ? null :
                new PartialModel(new ResourceLocation(Kuayue.MODID,"block/" + bowHeadPath)));
        this.pantographModel = map;

        registerDefaultState(this.getStateDefinition().any()
                .setValue(OPEN, false)
                .setValue(FACING, Direction.EAST));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(OPEN, FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return super.getStateForPlacement(pContext)
                .setValue(OPEN, false)
                .setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(!pLevel.isClientSide()) {
            pLevel.setBlock(pPos, pState.cycle(OPEN),3);
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

    public Map<String, PartialModel> getPantographModel() {
        return pantographModel;
    }
}
