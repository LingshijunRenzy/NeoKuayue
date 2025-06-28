package willow.train.kuayue.block.panels.door;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import willow.train.kuayue.block.panels.base.TrainPanelShapes;

public class JY290DoorBlock extends TrainDoorBlock {

    public JY290DoorBlock(Properties pProperties, Vec2 beginPos, Vec2 endPos) {
        super(pProperties, beginPos, endPos);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction direction = pState.getValue(FACING).getOpposite();
        DoorHingeSide hinge = pState.getValue(HINGE);
        return TrainPanelShapes.getJY290DoorCloseShape(hinge, direction);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        boolean open = pState.getValue(BlockStateProperties.OPEN);
        Direction direction = pState.getValue(FACING).getOpposite();
        DoorHingeSide hinge = pState.getValue(HINGE);
        return TrainPanelShapes.getJY290DoorShape(open, hinge, direction);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        Direction direction = pState.getValue(FACING).getOpposite();
        DoorHingeSide hinge = pState.getValue(HINGE);
        return TrainPanelShapes.getJY290DoorCloseShape(hinge, direction);
    }

    @Override
    public VoxelShape getVisualShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return getShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }
}
