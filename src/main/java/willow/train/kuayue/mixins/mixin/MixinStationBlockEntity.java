package willow.train.kuayue.mixins.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.trains.bogey.AbstractBogeyBlock;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import com.simibubi.create.content.trains.track.ITrackBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import willow.train.kuayue.block.bogey.ISingleSideBogey;
import willow.train.kuayue.utils.StationMixinCache;

import java.util.List;

@Mixin(StationBlockEntity.class)
public class MixinStationBlockEntity {

    @Shadow(remap = false)
    AbstractBogeyBlock<?>[] bogeyTypes;

    @Shadow(remap = false)
    int[] bogeyLocations;

    @Redirect(method = "assemble", at = @At(value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z"),
            remap = false)
    public boolean tryInject(List instance, Object e) {
        if (!(e instanceof Double value)) {
            StationMixinCache.instance = null;
            return instance.add(e);
        }
        if (StationMixinCache.instance == null) return instance.add(value);
        int index = StationMixinCache.instance.index();
        AbstractBogeyBlock bogeyType = StationMixinCache.instance.bogey();
        if (index == -1) instance.add(e);
        int loc = bogeyLocations[index];
        double bogeySize = StationMixinCache.instance.bogeySpacing();

        if (bogeyType instanceof ISingleSideBogey single) {
            double frontOffset = single.getFrontOffset();
            double backOffset = single.getBackOffset();
            double front = (double) loc + 0.5 + frontOffset;
            double back = (double) loc + 0.5 + backOffset;

            if (value.equals(front)) {
                return instance.add(value);
            } else if (value.equals(back)) {
                return instance.add(value);
            } else {
                return instance.add((double) loc + 0.5);
            }
        }
        return instance.add(value);
    }

    @Redirect(method = "assemble", at = @At(value = "INVOKE",
            target = "Lcom/simibubi/create/content/trains/bogey/AbstractBogeyBlock;getWheelPointSpacing()D"),
            remap = false)
    public double getIndex(AbstractBogeyBlock instance) {
        if (!(instance instanceof ISingleSideBogey)) {
            StationMixinCache.instance = null;
            return instance.getWheelPointSpacing();
        }
        int index = -1;
        for (int i = 0; i < bogeyTypes.length; i++) {
            if (instance == bogeyTypes[i]) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            StationMixinCache.instance = new StationMixinCache(index, instance,
                    instance.getWheelPointSpacing());
        } else {
            StationMixinCache.instance = null;
        }
        return instance.getWheelPointSpacing();
    }

    @Inject(method = "trackClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/trains/bogey/AbstractBogeyBlockEntity;setBogeyData(Lnet/minecraft/nbt/CompoundTag;)V"),
            remap = false)
    public void addBogeyChangeText(Player player, InteractionHand hand, ITrackBlock track, BlockState state,
                                   BlockPos pos, CallbackInfoReturnable<Boolean> cir,
                                   @Local(ordinal = 2) BlockState newBlock) {

        Level level = ((StationBlockEntity)(Object) this).getLevel();
        String descriptionId = newBlock.getBlock().getDescriptionId();
        if(level != null && descriptionId.startsWith("kuayue", 6)) {
            player.displayClientMessage(
                    Component.translatable("msg.bogey.style.changed." +
                            descriptionId), true);
        }
    }
}
