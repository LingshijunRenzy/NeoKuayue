package willow.train.kuayue.behaviour;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import willow.train.kuayue.block.panels.carport.DF5ChimneyBlock;

import java.util.Map;

import static willow.train.kuayue.block.panels.TrainPanelBlock.FACING;
import static willow.train.kuayue.block.panels.carport.DF11GChimneyBlock.LIT;

public class DF5ChimneyMovementBehaviour extends ChimneyMovementBehaviour {

    public static final Map<Direction, DF5ChimneyBlock.DirectionProperties> ENTITY_PROPERTIES_MAP = Map.of(
            Direction.NORTH, new DF5ChimneyBlock.DirectionProperties(-0.60F, -0.80F, 0.0F, -0.05F),
            Direction.EAST,  new DF5ChimneyBlock.DirectionProperties(0.85F,  -0.60F, 0.05F, 0.0F),
            Direction.SOUTH, new DF5ChimneyBlock.DirectionProperties(0.60F,  0.85F, 0.0F, 0.05F),
            Direction.WEST,  new DF5ChimneyBlock.DirectionProperties(-0.80F, 0.60F, -0.05F, 0.0F)
    );

    @Override
    public void tick(MovementContext context) {

        Level pLevel = context.world;
        BlockState pState = context.state;
        if (pLevel == null || !pLevel.isClientSide || context.position == null
                || !pState.getValue(CampfireBlock.LIT) || context.disabled)
            return;

        RandomSource pRandom = pLevel.random;
        Direction direction = pState.getValue(FACING);
        DF5ChimneyBlock.DirectionProperties props = ENTITY_PROPERTIES_MAP.getOrDefault(direction,
                new DF5ChimneyBlock.DirectionProperties(0.0F, 0.0F, 0.0F, 0.0F));

        float density = 0.5F;
        if (isStopped) {
            density = 0.1F;
            posSpeedDiff = 0.0F;
        }

        if (pState.getValue(LIT)) {
            if (pRandom.nextFloat() < (density + posSpeedDiff * 10)) {
                pLevel.addParticle(
                        ParticleTypes.LARGE_SMOKE,
                        context.position.x() + props.xOffset(),
                        context.position.y() + 0.95F,
                        context.position.z() + props.zOffset(),
                        props.xSpeed(), 0.2F, props.zSpeed());
            }
        }
    }
}
