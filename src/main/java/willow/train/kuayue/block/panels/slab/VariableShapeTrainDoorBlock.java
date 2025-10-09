package willow.train.kuayue.block.panels.slab;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import willow.train.kuayue.block.panels.door.TrainDoorBlock;

import java.util.function.Supplier;

public class VariableShapeTrainDoorBlock extends TrainDoorBlock {

    private Supplier<VariableShapePanelBlock.VoxelShapeSup> doorVoxelShapeSupplier;

    private Supplier<VariableShapePanelBlock.VoxelShapeSup> doorCollisionShapeSupplier;

    private Supplier<VariableShapePanelBlock.VoxelShapeSup> doorInteractionShapeSupplier;

    public VariableShapeTrainDoorBlock(Properties pProperties, Vec2 beginPos, Vec2 endPos) {
        super(pProperties, beginPos, endPos);
        this.doorVoxelShapeSupplier = () -> (state, level, blockPos, context) -> Shapes.block();
        this.doorCollisionShapeSupplier = () -> (state, level, blockPos, context) -> Shapes.block();
        this.doorInteractionShapeSupplier = () -> (state, level, blockPos, context) -> Shapes.block();
    }

    public VariableShapeTrainDoorBlock(Properties pProperties, Vec2 beginPos, Vec2 endPos,
                                       Supplier<VariableShapePanelBlock.VoxelShapeSup> doorShapeSupplier) {
        super(pProperties, beginPos, endPos);
        this.doorVoxelShapeSupplier = doorShapeSupplier;
        this.doorCollisionShapeSupplier = doorShapeSupplier;
        this.doorInteractionShapeSupplier = doorShapeSupplier;
    }

    public VariableShapeTrainDoorBlock(Properties pProperties, Vec2 beginPos, Vec2 endPos,
                                       Supplier<VariableShapePanelBlock.VoxelShapeSup> doorShapeSupplier,
                                       Supplier<VariableShapePanelBlock.VoxelShapeSup> doorCollisionShapeSupplier) {
        super(pProperties, beginPos, endPos);
        this.doorVoxelShapeSupplier = doorShapeSupplier;
        this.doorCollisionShapeSupplier = doorCollisionShapeSupplier;
        this.doorInteractionShapeSupplier = doorShapeSupplier;
    }

    public VariableShapeTrainDoorBlock(Properties pProperties, Vec2 beginPos, Vec2 endPos,
                                       Supplier<VariableShapePanelBlock.VoxelShapeSup> doorShapeSupplier,
                                       Supplier<VariableShapePanelBlock.VoxelShapeSup> doorCollisionShapeSupplier,
                                       Supplier<VariableShapePanelBlock.VoxelShapeSup> doorInteractionShapeSupplier) {
        super(pProperties, beginPos, endPos);
        this.doorVoxelShapeSupplier = doorShapeSupplier;
        this.doorCollisionShapeSupplier = doorCollisionShapeSupplier;
        this.doorInteractionShapeSupplier = doorInteractionShapeSupplier;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return doorVoxelShapeSupplier.get().getVoxelShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return doorCollisionShapeSupplier.get().getVoxelShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return doorInteractionShapeSupplier.get().getVoxelShape(pState, pLevel, pPos, null);
    }
}
