package willow.train.kuayue.mixins.mixin;

import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TravellingPoint;
import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import willow.train.kuayue.systems.train_extension.ExtensionHelper;
import willow.train.kuayue.systems.train_extension.TrainExtensionConstants;
import willow.train.kuayue.systems.train_extension.conductor.ConductorHelper;

@Mixin(Train.class)
public abstract class MixinTrain {

    @Shadow
    protected abstract void collideWithOtherTrains(Level level, Carriage carriage);

    @Shadow
    public abstract Pair<Train, Vec3> findCollidingTrain(Level level, Vec3 start, Vec3 end, ResourceKey<Level> dimension);

//    @Redirect(method = "tick", at=@At(value = "INVOKE",
//            target = "Lcom/simibubi/create/content/trains/entity/Train;collideWithOtherTrains(Lnet/minecraft/world/level/Level;Lcom/simibubi/create/content/trains/entity/Carriage;)V"),
//            remap = false)
//    public void doCollideWithOtherTrains(Train instance, Level level, Carriage carriage) {
//        if (instance.derailed)
//            return;
//
//        TravellingPoint trailingPoint = carriage.getTrailingPoint();
//        TravellingPoint leadingPoint = carriage.getLeadingPoint();
//
//        if (leadingPoint.node1 == null || trailingPoint.node1 == null)
//            return;
//        ResourceKey<Level> dimension = leadingPoint.node1.getLocation().dimension;
//        if (!dimension.equals(trailingPoint.node1.getLocation().dimension))
//            return;
//
//        Vec3 start = (instance.speed < 0 ? trailingPoint : leadingPoint).getPosition(instance.graph);
//        Vec3 end = (instance.speed < 0 ? leadingPoint : trailingPoint).getPosition(instance.graph);
//
//        Pair<Train, Vec3> collision = instance.findCollidingTrain(level, start, end, dimension);
//        if (collision == null)
//            return;
//
//        Train train = collision.getFirst();
//
//        double combinedSpeed = Math.abs(instance.speed) + Math.abs(train.speed);
//        if (combinedSpeed > .2f) {
//            Vec3 v = collision.getSecond();
//            level.explode(null, v.x, v.y, v.z, (float) Math.min(3 * combinedSpeed, 5), Explosion.BlockInteraction.NONE);
//        }
//
//        kasuga.lib.core.util.data_type.Pair<Float, Float> speedAfterCrash = ConductorHelper.momentumExchange(instance, train, 0.8f);
//        if (speedAfterCrash == null) {
//            instance.crash();
//            train.crash();
//            return;
//        }
//        ExtensionHelper.gentlyCrash(instance, speedAfterCrash.getFirst());
//        ExtensionHelper.gentlyCrash(train, speedAfterCrash.getSecond());
//    }

    @Redirect(method = "collideWithOtherTrains", at = @At(
            value = "INVOKE", target = "Lcom/simibubi/create/content/trains/entity/Train;findCollidingTrain(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/resources/ResourceKey;)Lcom/simibubi/create/foundation/utility/Pair;"),
    remap = false)
    public Pair<Train, Vec3> doFindCollidingTrain(Train instance, Level otherLeading, Vec3 otherTrailing, Vec3 otherDimension, ResourceKey<Level> start2) {
        Pair<Train, Vec3> pair = findCollidingTrain(otherLeading, otherTrailing, otherDimension, start2);
        if (pair == null) return null;
        TrainExtensionConstants.colliedTrains.put(instance, pair.getFirst());
        return pair;
    }

    @Inject(method = "collideWithOtherTrains", at = @At(
            value = "INVOKE", target = "Lcom/simibubi/create/content/trains/entity/Train;crash()V"),
    remap = false, cancellable = true)
    public void onCrash(Level level, Carriage carriage, CallbackInfo ci) {
        Train train = carriage.train;
        Train other = TrainExtensionConstants.colliedTrains.remove(train);
        if (other == null) return;

        float deltaSpeed = (float) Math.abs(train.speed - other.speed);
        float reverseCoefficient = Math.max(Math.min(
                0.1f / deltaSpeed, 1f), 0f);
        System.out.println("collide speed: " + deltaSpeed +
                ", rC: " + reverseCoefficient);
        kasuga.lib.core.util.data_type.Pair<Float, Float> speedAfterCrash =
                ConductorHelper.momentumExchange(train, other, reverseCoefficient);
        if (speedAfterCrash == null) {
            train.crash();
            other.crash();
            return;
        }
        ExtensionHelper.gentlyCrash(train, speedAfterCrash.getSecond());
        ExtensionHelper.gentlyCrash(other, speedAfterCrash.getFirst());
        ci.cancel();
    }
}
