package willow.train.kuayue.initial;

import kasuga.lib.registrations.common.SoundReg;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import willow.train.kuayue.Kuayue;

public class AllSounds {
    public static final SoundReg TRAIN_COUPLER_SOUND = new SoundReg("train_coupler_sound", AllElements.testRegistry.asResource("coupler"))
            .soundSupplier(() -> SoundEvent.createVariableRangeEvent(AllElements.testRegistry.asResource("coupler")))
            .submit(AllElements.testRegistry);

    public static void invoke() {}
}
