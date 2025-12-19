package willow.train.kuayue.mixins.mixin;

import com.simibubi.create.content.contraptions.minecart.TrainCargoManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TrainCargoManager.class)
public interface AccessorTrainCargoManager {
    @Invoker("changeDetected")
    void invokeChangeDetected();
}
