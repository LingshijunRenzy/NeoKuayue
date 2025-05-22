package willow.train.kuayue.systems.device.driver.seat;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import kasuga.lib.core.client.frontend.gui.GuiInstance;
import kasuga.lib.core.javascript.engine.JavascriptValue;
import kasuga.lib.core.javascript.engine.annotations.HostAccess;
import kasuga.lib.core.menu.base.BindingClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class WorldTrainSoundManager {
    private final GuiInstance instance;

    public static void init(){
        BindingClient.registerBinding(GuiTargets.SOUND, WorldTrainSoundManager::new);
    }

    public WorldTrainSoundManager(GuiInstance instance) {
        this.instance = instance;
    }

    public void inject(MovementContext movementContext){
        if(this.instance.contextObject.containsKey("sound"))
            return;
        this.instance.putContextObject("sound", new SoundEngine(movementContext));
    }

    public static class SoundEngine {
        private final MovementContext movementContext;

        public SoundEngine(MovementContext movementContext) {
            this.movementContext = movementContext;
        }

        @HostAccess.Export
        public void dispatchSound(String name){
            // todo: need to check the range of this sound.
            SoundEvent soundEvent = SoundEvent.createFixedRangeEvent(new ResourceLocation(name), 7.0f);
            SoundInstance instance = new TrackingContextSound(soundEvent, movementContext);
            Minecraft.getInstance().submit(()->Minecraft.getInstance().getSoundManager().play(instance));
        }
    }
}
