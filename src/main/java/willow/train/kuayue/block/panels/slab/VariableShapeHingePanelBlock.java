package willow.train.kuayue.block.panels.slab;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.block.panels.window.TrainOpenableWindowBlock;

import java.util.function.Supplier;

public class VariableShapeHingePanelBlock extends VariableShapePanelBlock{

    public VariableShapeHingePanelBlock(Properties pProperties, Vec2 beginPos, Vec2 endPos) {
        super(pProperties, beginPos, endPos);
        registerDefaultState(this.getStateDefinition().any()
                .setValue(BlockStateProperties.DOOR_HINGE, DoorHingeSide.LEFT));
    }

    public VariableShapeHingePanelBlock(Properties properties, Vec2 beginPos, Vec2 endPos, Supplier<VoxelShapeSup> shapeSupplier) {
        super(properties, beginPos, endPos, shapeSupplier);
        registerDefaultState(this.getStateDefinition().any()
                .setValue(BlockStateProperties.DOOR_HINGE, DoorHingeSide.LEFT));
    }

    public VariableShapeHingePanelBlock(Properties properties, Vec2 beginPos, Vec2 endPos, Supplier<VoxelShapeSup> shapeSupplier, Supplier<VoxelShapeSup> collisionShapeSupplier) {
        super(properties, beginPos, endPos, shapeSupplier, collisionShapeSupplier);
        registerDefaultState(this.getStateDefinition().any()
                .setValue(BlockStateProperties.DOOR_HINGE, DoorHingeSide.LEFT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(BlockStateProperties.DOOR_HINGE));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return super.getStateForPlacement(pContext)
                .setValue(BlockStateProperties.DOOR_HINGE, TrainOpenableWindowBlock.getHinge(pContext));
    }
}
