package willow.train.kuayue.systems.blueprint_distributor;

import kasuga.lib.core.resource.Resources;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;
import willow.train.kuayue.Kuayue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class DistributionEvent {

    @SubscribeEvent
    public static void onClientStarted(FMLClientSetupEvent event) {
        Logger logger = Kuayue.LOGGER;
        int counter = 0, all = 0, skip = 0;
        try {
            Map<String, Resource> resources =
                    Resources.getResources(
                            new ResourceLocation(Kuayue.MODID, "schematic"),
                            false);
            all = resources.size();
            File dir = new File("schematics");
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    logger.warn("Schematic distribution failed, cause: " +
                            "unable to create schematics directory");
                    return;
                }
            }
            for (Map.Entry<String, Resource> entry : resources.entrySet()) {
                File file = new File("schematics/" + entry.getKey());
                if (file.exists()) {
                    skip++;
                    continue;
                }
                counter++;
                FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                InputStream is = entry.getValue().open();
                channel.write(ByteBuffer.wrap(is.readAllBytes()));
                is.close();
                channel.close();
            }
        } catch (Exception e) {
            logger.error("Error while distributing kuayue schematics.", e);
        } finally {
            logger.info("Schematic distribution finished. " +
                            "Need to distribute: {}, " +
                            "distributed: {}, " +
                            "skipped: {}, " +
                            "error: {}.",
                    all, skip, counter, all - skip - counter);
        }
    }
}
