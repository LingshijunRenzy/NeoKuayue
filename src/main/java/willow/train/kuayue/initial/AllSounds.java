package willow.train.kuayue.initial;

import kasuga.lib.registrations.common.SoundReg;
import net.minecraft.resources.ResourceLocation;
import willow.train.kuayue.Kuayue;

public class AllSounds {
    public static final SoundReg TRAIN_COUPLER_SOUND = new SoundReg(
        "train_coupler_sound",
            new ResourceLocation(Kuayue.MODID, "coupler")
    ).submit(AllElements.testRegistry);

    public static void invoke() {}
}
