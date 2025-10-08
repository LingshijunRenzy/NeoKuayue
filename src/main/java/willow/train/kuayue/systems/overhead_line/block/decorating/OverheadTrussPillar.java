package willow.train.kuayue.systems.overhead_line.block.decorating;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.Lazy;
import willow.train.kuayue.block.panels.base.CompanyTrainPanel;
import willow.train.kuayue.block.panels.base.TrainPanelProperties;
import willow.train.kuayue.block.panels.base.TrainPanelShapes;

import willow.train.kuayue.initial.AllBlocks;


import java.util.HashMap;
import java.util.Map;



public class OverheadTrussPillar extends OverheadTruss {

    private static final Lazy<Map<Direction, VoxelShape>> PILLAR_TRUSS_SHAPES = Lazy.of(() -> {
        Map<Direction, VoxelShape> shapes = new HashMap<>();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            shapes.put(dir, TrainPanelShapes.getOverheadPillarTrussShape(dir));
        }
        return shapes;
    });


    public OverheadTrussPillar(Properties properties, Vec3 begin3dPos, Vec3 end3dPos) {
        super(properties, begin3dPos, end3dPos);
    }

    public OverheadTrussPillar(Properties properties, Vec2 beginPos, Vec2 endPos) {
        super(properties, beginPos, endPos);
    }

    public OverheadTrussPillar(Properties properties) {
        super(properties, new Vec3(0, 0, 0), new Vec3(1, 1, 1));
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return PILLAR_TRUSS_SHAPES.get().get(pState.getValue(FACING));
    }


    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return getShape(pState, pLevel, pPos, pContext);
    }


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
                    .setValue(CompanyTrainPanel.SHAPE_TYPE, TrainPanelProperties.ShapeType.PILLAR_TRUSS)
                    .setValue(BlockStateProperties.DOOR_HINGE, leftHinge ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT)
                    .setValue(BlockStateProperties.OPEN, open);

            l.setBlock(myPos, companyState, 10);
            CompanyTrainPanel.setParentBlock(myPos, level, companyState, pos);
            return InteractionResult.SUCCESS;
        };

        walkAllValidPos(level, pos, state, null, null, null, function);
    }
}