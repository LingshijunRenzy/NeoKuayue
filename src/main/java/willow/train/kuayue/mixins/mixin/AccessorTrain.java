package willow.train.kuayue.mixins.mixin;

import com.simibubi.create.content.trains.entity.Train;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Train.class)
public interface AccessorTrain {

    @Accessor
    double[] getStress();

    @Accessor
    void setStress(double[] stress);
}
