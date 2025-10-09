package willow.train.kuayue.block.panels.carport;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class DF5ChimneyBlock extends DF11GChimneyBlock{

    public record DirectionProperties(double xOffset, double zOffset, double xSpeed, double zSpeed) {}

    public static final Map<Direction, DirectionProperties> PROPERTIES_MAP = Map.of(
            Direction.NORTH, new DirectionProperties(-0.1F, -0.3F, 0.0F, -0.05F),
            Direction.EAST,  new DirectionProperties(1.3F,  -0.1F, 0.05F, 0.0F),
            Direction.SOUTH, new DirectionProperties(1.1F,  1.3F, 0.0F, 0.05F),
            Direction.WEST,  new DirectionProperties(-0.3F, 1.1F, -0.05F, 0.0F)
    );

    public DF5ChimneyBlock(Properties properties, boolean isCarport) {
        super(properties, isCarport);
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        dF5SpawnParticles(pState, pLevel, pPos);
    }

    private static void dF5SpawnParticles(BlockState pState, Level pLevel, BlockPos pPos) {

        Direction direction = pState.getValue(FACING);

        DirectionProperties props = PROPERTIES_MAP.getOrDefault(direction,
                new DirectionProperties(0.0F, 0.0F, 0.0F, 0.0F));

        if (pState.getValue(LIT)) {
            for (int i = 0; i < 2; i++) {
                pLevel.addParticle(
                        ParticleTypes.LARGE_SMOKE,
                        (double)pPos.getX() + props.xOffset,
                        (double)pPos.getY() + 1.2F,
                        (double)pPos.getZ() + props.zOffset,
                        props.xSpeed, 0.2F, props.zSpeed);
            }
        }
    }
}
