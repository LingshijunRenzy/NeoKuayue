package willow.train.kuayue.systems.overhead_line.block.decorating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.Lazy;
import willow.train.kuayue.block.panels.base.CompanyTrainPanel;
import willow.train.kuayue.block.panels.base.TrainPanelProperties;
import willow.train.kuayue.block.panels.base.TrainPanelShapes;
import willow.train.kuayue.initial.AllBlocks;
import willow.train.kuayue.block.panels.TrainPanelBlock;

import java.util.HashMap;
import java.util.Map;

public class OverheadTruss extends TrainPanelBlock {

    private final Vec3 begin3dPos, end3dPos;
    // 碰撞箱缓存
    private static final Lazy<Map<Direction, VoxelShape>> BIG_TRUSS_SHAPES = Lazy.of(() -> {
        Map<Direction, VoxelShape> shapes = new HashMap<>();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            shapes.put(dir, TrainPanelShapes.getOverheadBigTrussShape(dir));
        }
        return shapes;
    });

    // vec3坐标构造函数
    public OverheadTruss(Properties properties, Vec3 begin3dPos, Vec3 end3dPos) {
        super(properties,
                new Vec2((float) begin3dPos.x, (float) begin3dPos.y),
                new Vec2((float) end3dPos.x, (float) end3dPos.y));
        this.begin3dPos = begin3dPos;
        this.end3dPos = end3dPos;
    }

    public OverheadTruss(Properties properties, Vec2 beginPos, Vec2 endPos) {
        this(properties,
                new Vec3(beginPos.x, beginPos.y, 0),
                new Vec3(endPos.x, endPos.y, 0));
    }

    public OverheadTruss(Properties properties) {
        super(properties);
        this.begin3dPos = Vec3.ZERO;
        this.end3dPos = new Vec3(1, 1, 0);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return BIG_TRUSS_SHAPES.get().get(pState.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return getShape(pState, pLevel, pPos, pContext);
    }

    public boolean canSurvive(BlockState state, BlockGetter level, BlockPos pos) {
        Direction direction = state.getValue(FACING);

        // 检查主方块位置
        if (!level.getBlockState(pos).isAir()) {
            return false;
        }

        BlockUseFunction function = (l, p, parentState, myPos, myState, player, hand, hit) -> {
            if (p.equals(myPos)) return InteractionResult.SUCCESS; // 跳过主方块

            if (!l.getBlockState(myPos).isAir()) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.SUCCESS;
        };

        return checkAllPositions(level, pos, state, null, null, null, function);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        Player player = pContext.getPlayer();


        BlockState state = super.getStateForPlacement(pContext);
        if (state == null) {
            return null;
        }


        if (!checkAllPositions(level, pos, state, player, pContext.getHand(), null,
                (l, p, parentState, myPos, myState, pPlayer, pHand, pHit) -> {
                    if (p.equals(myPos)) return InteractionResult.SUCCESS;
                    if (!l.getBlockState(myPos).isAir()) {
                        if (pPlayer instanceof ServerPlayer serverPlayer && !l.isClientSide())
                        return InteractionResult.FAIL;
                    }
                    return InteractionResult.SUCCESS;
                })) {
            return null;
        }

        return state;
    }

    private boolean checkAllPositions(BlockGetter level, BlockPos blockPos, BlockState parentState,
                                      Player player, InteractionHand hand, BlockHitResult hit, BlockUseFunction function) {
        Direction direction = parentState.getValue(FACING);

        BlockPos firstPos = blockPos
                .offset((int) begin3dPos.x, (int) begin3dPos.y, (int) begin3dPos.z);

        int lengthX = (int) (end3dPos.x - begin3dPos.x);
        int lengthY = (int) (end3dPos.y - begin3dPos.y);
        int lengthZ = (int) (end3dPos.z - begin3dPos.z);

        for (int x = 0; x < lengthX; x++) {
            for (int y = 0; y < lengthY; y++) {
                for (int z = 0; z < lengthZ; z++) {
                    BlockPos currentPos = firstPos.offset(x, y, z);
                    currentPos = rotatePosByDirection(currentPos, blockPos, direction);

                    if (function.apply((Level) level, blockPos, parentState, currentPos,
                            level.getBlockState(currentPos), player, hand, hit) == InteractionResult.FAIL) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // 多方块三维
    @Override
    public void generateCompanyBlock(Level level, BlockState state, BlockPos pos, boolean isMoving) {

        Direction direction = state.getValue(FACING);
        boolean leftHinge = !state.hasProperty(BlockStateProperties.DOOR_HINGE) ||
                state.getValue(BlockStateProperties.DOOR_HINGE) == DoorHingeSide.LEFT;
        boolean open = state.hasProperty(BlockStateProperties.OPEN) ?
                state.getValue(BlockStateProperties.OPEN) : false;

        BlockUseFunction function = (l, p, parentState, myPos, myState, player, hand, hit) -> {
            if (p.equals(myPos)) return InteractionResult.SUCCESS;

            BlockState companyState = AllBlocks.COMPANY_TRAIN_PANEL.instance().defaultBlockState()
                    .setValue(CompanyTrainPanel.FACING, direction)
                    .setValue(CompanyTrainPanel.SHAPE_TYPE, TrainPanelProperties.ShapeType.BIG_TRUSS)
                    .setValue(BlockStateProperties.DOOR_HINGE, leftHinge ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT)
                    .setValue(BlockStateProperties.OPEN, open);

            l.setBlock(myPos, companyState, 10);
            CompanyTrainPanel.setParentBlock(myPos, level, companyState, pos);
            return InteractionResult.SUCCESS;
        };

        walkAllValidPos(level, pos, state, null, null, null, function);
    }

    @Override
    public void walkAllValidPos(Level level, BlockPos blockPos, BlockState parentState,
                                Player player, InteractionHand hand, BlockHitResult hit, BlockUseFunction function) {
        Direction direction = parentState.getValue(FACING);

        BlockPos firstPos = blockPos
                .offset((int) begin3dPos.x, (int) begin3dPos.y, (int) begin3dPos.z);

        int lengthX = (int) (end3dPos.x - begin3dPos.x);
        int lengthY = (int) (end3dPos.y - begin3dPos.y);
        int lengthZ = (int) (end3dPos.z - begin3dPos.z);

        for (int x = 0; x < lengthX; x++) {
            for (int y = 0; y < lengthY; y++) {
                for (int z = 0; z < lengthZ; z++) {
                    BlockPos currentPos = firstPos.offset(x, y, z);
                    currentPos = rotatePosByDirection(currentPos, blockPos, direction);
                    function.apply(level, blockPos, parentState, currentPos, level.getBlockState(currentPos), player, hand, hit);
                }
            }
        }
    }

    @Override
    public void removeCompanyBlock(Level level, BlockState state, BlockPos pos, boolean isMoving) {
        BlockUseFunction function = (l, p, parentState, myPos, myState, player, hand, hit) -> {
            if (p.equals(myPos)) return InteractionResult.SUCCESS;
            l.removeBlockEntity(myPos);
            l.removeBlock(myPos, isMoving);
            return InteractionResult.SUCCESS;
        };

        walkAllValidPos(level, pos, state, null, null, null, function);
    }

    private BlockPos rotatePosByDirection(BlockPos pos, BlockPos origin, Direction direction) {
        int dx = pos.getX() - origin.getX();
        int dy = pos.getY() - origin.getY();
        int dz = pos.getZ() - origin.getZ();

        return switch (direction) {
            case NORTH -> origin.offset(dx, dy, dz);
            case SOUTH -> origin.offset(-dx, dy, -dz);
            case WEST -> origin.offset(dz, dy, -dx);
            case EAST -> origin.offset(-dz, dy, dx);
            case UP, DOWN -> throw new IllegalArgumentException("不支持垂直放置");
        };
    }



    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return super.onWrenched(state, context);
    }
}