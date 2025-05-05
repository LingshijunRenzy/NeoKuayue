package willow.train.kuayue.mixins.mixin;

import com.simibubi.create.content.trains.bogey.AbstractBogeyBlock;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
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
}