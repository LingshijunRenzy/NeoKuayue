package willow.train.kuayue.systems.overhead_line.block.line;

import kasuga.lib.core.util.data_type.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import willow.train.kuayue.systems.overhead_line.render.CachedCurveRenderer;
import willow.train.kuayue.systems.overhead_line.render.RenderCurve;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS;

public class OverheadLineRendererSystem {

    public HashMap<Pair<Vec3, Vec3>, ClientOverheadLine> overheadLines = new HashMap<>();


    public void onRenderLevelLast(RenderLevelStageEvent event) {

        MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        if(event.getStage() != AFTER_TRIPWIRE_BLOCKS)
            return;
        Frustum frustum = event.getFrustum();

        overheadLines.values().forEach((line)->{
            AABB boundingBox = line.getBoundingBox();
            if(boundingBox == null || !frustum.isVisible(boundingBox))
                return;
            RenderCurve curve = line.getCurve();
            if(curve == null)
                return;
            CachedCurveRenderer.render(
                    line.getModel(),
                    curve,
                    event.getPoseStack(),
                    bufferSource,
                    OverlayTexture.NO_OVERLAY
            );
        });
    }

    public void addLine(ClientOverheadLine overheadLine){
        overheadLines.put(overheadLine.getPosition(), overheadLine);
    }

    public void removeLine(ClientOverheadLine overheadLine) {
        overheadLines.remove(overheadLine.getPosition());
    }
}
