package willow.train.kuayue.systems.overhead_line.block.support;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class OverheadLineSupportInsulatorBlock extends OverheadLineSupportBlock{
    public static final BooleanProperty WALL = BooleanProperty.create("wall");
    public OverheadLineSupportInsulatorBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected BlockState getDefaultState() {
        return
                super.getDefaultState()
                        .setValue(WALL, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(WALL);
    }
}
