package willow.train.kuayue.systems.overhead_line.block.decorating;

import kasuga.lib.registrations.common.BlockReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.block.panels.base.TrainPanelShapes;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class LinkedTrussBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<ConnectedType> CONNECTED_TYPE =
            EnumProperty.create("connected_type", ConnectedType.class);
    public static final EnumProperty<ConnectionDirection> CONNECTION_DIRECTION =
            EnumProperty.create("connection_direction", ConnectionDirection.class);

    private final Map<ResourceLocation, ConnectedType> specialBlocks;

    public LinkedTrussBlock(Properties properties, SpecialBlockConfig config) {
        super(properties.noOcclusion());
        this.specialBlocks = new HashMap<>(config.specialBlockMap);
        registerDefaultState(getStateDefinition().any()
                .setValue(FACING, Direction.EAST)
                .setValue(CONNECTED_TYPE, ConnectedType.NONE)
                .setValue(CONNECTION_DIRECTION, ConnectionDirection.NONE)
        );
    }

    // 连接类型枚举
    public enum ConnectedType implements StringRepresentable {
        NONE("none"),
        SINGLE("single"),
        DOUBLE("double"),
        CONCRETE("concrete"),
        TRIANGLE("triangle");

        private final String name;
        ConnectedType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    // 连接方向枚举
    public enum ConnectionDirection implements StringRepresentable {
        NONE("none"),
        ABOVE("above"),
        BELOW("below");

        private final String name;
        ConnectionDirection(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return TrainPanelShapes.getOverheadLinePillarShape(state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CONNECTED_TYPE, CONNECTION_DIRECTION);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        Direction initialFacing = context.getHorizontalDirection().getOpposite();
        BlockState state = defaultBlockState().setValue(FACING, initialFacing);

        return updateConnectedState(state, level, pos);
    }

    // 自动更新
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!isMoving) {
            updateVerticalNeighbors(level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        if (!isMoving) {
            updateVerticalNeighbors(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, neighborPos, isMoving);
        if (neighborPos.equals(pos.above()) || neighborPos.equals(pos.below())) {
            BlockState newState = updateConnectedState(state, level, pos);
            if (newState != state) {
                level.setBlock(pos, newState, 3);
            }
        }
    }

    // 连接状态更新
    private BlockState updateConnectedState(BlockState currentState, Level level, BlockPos pos) {
        BlockState aboveState = level.getBlockState(pos.above());
        BlockState belowState = level.getBlockState(pos.below());

        ConnectedType aboveType = getConnectedType(aboveState);
        ConnectedType belowType = getConnectedType(belowState);

        ConnectedType targetType = ConnectedType.NONE;
        ConnectionDirection connectionDirection = ConnectionDirection.NONE;

        // 优先处理上方特殊方块
        if (aboveType != ConnectedType.NONE) {
            targetType = aboveType;
            connectionDirection = ConnectionDirection.ABOVE;
        }
        // 仅当上方没有特殊方块时，才处理下方特殊方块
        else if (belowType != ConnectedType.NONE) {
            targetType = belowType;
            connectionDirection = ConnectionDirection.BELOW;
        }

        Direction inheritedFacing = currentState.getValue(FACING);

        if (aboveType != ConnectedType.NONE && canInheritFacing(aboveState)) {
            Direction specialFacing = getFacingFromBlock(aboveState);
            inheritedFacing = rotateLeft(specialFacing);
        } else if (belowType != ConnectedType.NONE && canInheritFacing(belowState)) {
            Direction specialFacing = getFacingFromBlock(belowState);
            inheritedFacing = rotateLeft(specialFacing);
        } else {
            if (aboveState.getBlock() instanceof LinkedTrussBlock) {
                inheritedFacing = aboveState.getValue(FACING);
            } else if (belowState.getBlock() instanceof LinkedTrussBlock) {
                inheritedFacing = belowState.getValue(FACING);
            }
        }

        return currentState.setValue(CONNECTED_TYPE, targetType)
                .setValue(CONNECTION_DIRECTION, connectionDirection)
                .setValue(FACING, inheritedFacing);
    }

    // 邻居更新逻辑
    private void updateVerticalNeighbors(Level level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);

        if (aboveState.getBlock() instanceof LinkedTrussBlock) {
            BlockState newAboveState = ((LinkedTrussBlock) aboveState.getBlock())
                    .updateConnectedState(aboveState, level, abovePos);
            if (newAboveState != aboveState) {
                level.setBlock(abovePos, newAboveState, 3);
            }
        }

        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        if (belowState.getBlock() instanceof LinkedTrussBlock) {
            BlockState newBelowState = ((LinkedTrussBlock) belowState.getBlock())
                    .updateConnectedState(belowState, level, belowPos);
            if (newBelowState != belowState) {
                level.setBlock(belowPos, newBelowState, 3);
            }
        }
    }

    private boolean canInheritFacing(BlockState state) {
        return state.hasProperty(FACING);
    }

    private Direction getFacingFromBlock(BlockState state) {
        return state.getValue(FACING);
    }

    private ConnectedType getConnectedType(BlockState state) {
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (blockId == null) return ConnectedType.NONE;
        return specialBlocks.getOrDefault(blockId, ConnectedType.NONE);
    }

    public static class SpecialBlockConfig {
        final Map<ResourceLocation, ConnectedType> specialBlockMap = new HashMap<>();

        public SpecialBlockConfig addSpecialBlock(ResourceLocation blockId, ConnectedType type) {
            specialBlockMap.put(blockId, type);
            return this;
        }

        public SpecialBlockConfig addSpecialBlock(Supplier<Block> blockSupplier, ConnectedType type) {
            return addSpecialBlock(ForgeRegistries.BLOCKS.getKey(blockSupplier.get()), type);
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    private Direction rotateLeft(Direction original) {
        return switch (original) {
            case NORTH -> Direction.WEST;
            case EAST -> Direction.NORTH;
            case SOUTH -> Direction.EAST;
            case WEST -> Direction.SOUTH;
            default -> original;
        };
    }

    public static class Builder implements BlockReg.BlockBuilder<LinkedTrussBlock> {
        private final SpecialBlockConfig config;

        private Builder(SpecialBlockConfig config) {
            this.config = config;
        }

        public static Builder create(SpecialBlockConfig config) {
            return new Builder(config);
        }

        @Override
        public LinkedTrussBlock build(BlockBehaviour.Properties properties) {
            return new LinkedTrussBlock(properties, config);
        }
    }
}