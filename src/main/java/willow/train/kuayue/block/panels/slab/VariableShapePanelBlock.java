package willow.train.kuayue.block.panels.slab;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import willow.train.kuayue.block.panels.TrainPanelBlock;

import java.util.function.Supplier;

public class VariableShapePanelBlock extends TrainPanelBlock implements IWrenchable {

    public final static VoxelShape HALF_PANEL_SHAPE_EAST = Block.box(0, 0, 0, 8, 16, 16);

    public static final VoxelShape QUARTER_PANEL_SHAPE_EAST = Block.box(0, 0, 0, 4, 16, 16);

    private Supplier<VoxelShapeSup> voxelShapeSupplier;

    private Supplier<VoxelShapeSup> collisionShapeSupplier;

    public interface VoxelShapeSup {
        public VoxelShape getVoxelShape(BlockState state, BlockGetter level,
                                        BlockPos blockPos, CollisionContext context);
    }

    public VariableShapePanelBlock(Properties pProperties, Vec2 beginPos, Vec2 endPos) {
        super(pProperties, beginPos, endPos);
        this.voxelShapeSupplier = () -> (state, level, blockPos, context) -> Shapes.block();
        this.collisionShapeSupplier = () -> (state, level, blockPos, context) -> Shapes.block();
    }

    public VariableShapePanelBlock(Properties properties, Vec2 beginPos, Vec2 endPos, Supplier<VoxelShapeSup> shapeSupplier) {
        super(properties);
        this.voxelShapeSupplier = shapeSupplier;
        this.collisionShapeSupplier = shapeSupplier;
    }

    public VariableShapePanelBlock(Properties properties, Vec2 beginPos, Vec2 endPos,
                                   Supplier<VoxelShapeSup> shapeSupplier, Supplier<VoxelShapeSup> collisionShapeSupplier) {
        super(properties);
        this.voxelShapeSupplier = shapeSupplier;
        this.collisionShapeSupplier = collisionShapeSupplier;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return voxelShapeSupplier.get().getVoxelShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return collisionShapeSupplier.get().getVoxelShape(pState, pLevel, pPos, pContext);
    }
}
