package willow.train.kuayue.mixins.mixin;

import com.simibubi.create.content.trains.GlobalRailwayManager;
import com.simibubi.create.content.trains.entity.Train;
import kasuga.lib.core.util.Envs;
import net.minecraftforge.fml.LogicalSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.initial.AllPackets;
import willow.train.kuayue.systems.train_extension.TrainExtensionSystem;

import java.util.UUID;

@Mixin(GlobalRailwayManager.class)
public class MixinGlobalRailwayManager {

    @Inject(method = "addTrain", at = @At(value = "HEAD"), remap = false)
    public void addTrain(Train train, CallbackInfo ci) {
        TrainExtensionSystem sys = Kuayue.TRAIN_EXTENSION;
        if (sys.contains(train.id)) {
            sys.get(train.id).updateInternalConnections();
        }
    }

    @Inject(method = "removeTrain", at = @At(value = "HEAD"), remap = false)
    public void removeTrain(UUID id, CallbackInfo ci) {
        Kuayue.TRAIN_EXTENSION.remove(id);
    }
}
