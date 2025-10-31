package willow.train.kuayue.mixins.mixin;

import com.simibubi.create.content.trains.bogey.AbstractBogeyBlock;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CarriageBogey.class)
public interface AccessorCarriageBogey {

    @Accessor("type")
    public AbstractBogeyBlock<?> type();
}
