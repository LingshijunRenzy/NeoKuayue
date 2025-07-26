package willow.train.kuayue.systems.overhead_line.block.decorating;

import kasuga.lib.registrations.common.BlockReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.block.panels.base.TrainPanelShapes;
import willow.train.kuayue.initial.AllTags;

public class SimpleOverheadLinePillarBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<VerticalConnectionType> VERTICAL_TYPE =
            EnumProperty.create("vertical_type", VerticalConnectionType.class);

    private final DecorationProperties decorationProperties;

    public SimpleOverheadLinePillarBlock(Properties pProperties, DecorationProperties decorationProperties) {
        super(pProperties.noOcclusion());
        this.decorationProperties = decorationProperties;
        registerDefaultState(this.getStateDefinition().any()
                .setValue(FACING, Direction.EAST)
                .setValue(VERTICAL_TYPE, VerticalConnectionType.SINGLE)
        );
    }

    public enum VerticalConnectionType implements StringRepresentable {
        TOP("top"),
        MIDDLE("middle"),
        BOTTOM("bottom"),
        SINGLE("single");

        private final String name;

        VerticalConnectionType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction direction = pState.getValue(FACING);
        return TrainPanelShapes.getOverheadLinePillarShape(direction);
    }
    @Override
        public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction direction = pState.getValue(FACING);
        return TrainPanelShapes.getOverheadLinePillarShape(direction);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(FACING, VERTICAL_TYPE));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockState state = super.getStateForPlacement(pContext);
        if (state == null) return null;

        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);

        if (canConnectVertically(belowState) && belowState.hasProperty(FACING)) {
            Direction belowFacing = belowState.getValue(FACING);
            state = state.setValue(FACING, belowFacing);
        } else {
            if (decorationProperties.isDirectional()) {
                state = state.setValue(FACING, pContext.getHorizontalDirection().getOpposite());
            } else {
                state = state.setValue(FACING, Direction.EAST);
            }
        }
        BlockPos above = pos.above();
        boolean hasAbove = canConnectVertically(level.getBlockState(above));
        boolean hasBelow = canConnectVertically(level.getBlockState(belowPos));

        VerticalConnectionType verticalType;
        if (hasAbove && hasBelow) {
            verticalType = VerticalConnectionType.MIDDLE;
        } else if (hasBelow) {
            verticalType = VerticalConnectionType.TOP;
        } else if (hasAbove) {
            verticalType = isValidSupport(level, belowPos) ? VerticalConnectionType.BOTTOM : VerticalConnectionType.MIDDLE;
        } else {
            verticalType = VerticalConnectionType.SINGLE;
        }

        return state.setValue(VERTICAL_TYPE, verticalType);
    }

    // 放置/移除时更新
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!isMoving) {
            updateVerticalNeighbors(pos, level);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        if (!isMoving) {
            updateVerticalNeighbors(pos, level);
        }
    }

    private void updateVerticalNeighbors(BlockPos pos, Level level) {
        BlockPos above = pos.above();
        BlockPos below = pos.below();

        if (canConnectVertically(level.getBlockState(above))) {
            updateVerticalType(level.getBlockState(above), level, above);
        }
        if (canConnectVertically(level.getBlockState(below))) {
            updateVerticalType(level.getBlockState(below), level, below);
        }
    }

    private void updateVerticalType(BlockState state, Level level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockPos below = pos.below();
        boolean hasAbove = canConnectVertically(level.getBlockState(above));
        boolean hasBelow = canConnectVertically(level.getBlockState(below));

        VerticalConnectionType newType;
        if (hasAbove && hasBelow) {
            newType = VerticalConnectionType.MIDDLE;
        } else if (hasBelow) {
            newType = VerticalConnectionType.TOP;
        } else if (hasAbove) {
            newType = isValidSupport(level, below) ? VerticalConnectionType.BOTTOM : VerticalConnectionType.MIDDLE;
        } else {
            newType = VerticalConnectionType.SINGLE;
        }

        if (state.getBlock() instanceof SimpleOverheadLinePillarBlock &&
                state.getValue(VERTICAL_TYPE) != newType) {
            level.setBlock(pos, state.setValue(VERTICAL_TYPE, newType), 11);
        }
    }

    private boolean canConnectVertically(BlockState state) {
        return state.is(this) || state.is(AllTags.OVERHEAD_PILLAR_MIDDLE.tag());
    }

    private boolean isValidSupport(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isSolidRender(level, pos);
    }

    @Override
    public int getLightBlock(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return 0;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return true;
    }

    public static class Builder {
        public static BlockReg.BlockBuilder<SimpleOverheadLinePillarBlock> create(DecorationProperties properties) {
            return (p)->new SimpleOverheadLinePillarBlock(p, properties);
        }
    }

    public static DecorationProperties defaultProperties() {
        return new DecorationProperties();
    }

    public static class DecorationProperties {
        protected boolean $directional = false;

        public DecorationProperties directional() {
            this.$directional = true;
            return this;
        }

        public boolean isDirectional() {
            return this.$directional;
        }
    }
}