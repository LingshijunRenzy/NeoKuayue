package willow.train.kuayue.systems.device.driver.seat;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.drill.DrillMovementBehaviour;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class TrackingContextSound extends AbstractTickableSoundInstance {
    private final MovementContext context;

    TrackingContextSound(SoundEvent soundEvent, MovementContext context) {
        super(soundEvent, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.context = context;
    }

    @Override
    public void tick() {
        this.x = context.position.x;
        this.y = context.position.y;
        this.z = context.position.z;
    }

    @Override
    public float getVolume() {
        return 1.0F;
    }
}
