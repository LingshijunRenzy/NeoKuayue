package willow.train.kuayue.utils.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import willow.train.kuayue.Kuayue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Mod.EventBusSubscriber(modid= Kuayue.MODID, value= Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DebugDrawUtil {

    private static final ConcurrentMap<String, DebugBox> debugBoxes = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, DebugLine> debugLines = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, DebugText> debugTexts = new ConcurrentHashMap<>();

    public static record DebugBox(
            BlockPos pos,
            float r,
            float g,
            float b,
            float a,
            float expand,
            boolean fill
    ) {}

    public static record DebugLine(
            Vec3 start,
            Vec3 end,
            float r,
            float g,
            float b,
            float a,
            float width
    ) {}

    public static record DebugText(
            String text,
            Vec3 pos,
            int color,
            float scale,
            boolean seeThrough
    ) {}

    @SubscribeEvent
    public static void onRenderLevelLast(RenderLevelStageEvent event) {
        if(event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }

        if(!Minecraft.getInstance().options.renderDebug) {
            return;
        }

        PoseStack stack = event.getPoseStack();
        MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
        Vec3 cameraPos = event.getCamera().getPosition();

        renderDebugInfo(stack, source, cameraPos);

        source.endBatch();
    }

    private static void renderDebugInfo(PoseStack stack, MultiBufferSource source, Vec3 cameraPos){
        debugBoxes.forEach((id, box) -> {
            renderDebugBox(stack, source, cameraPos, box);
        });

        debugLines.forEach((id, line) -> {
            renderDebugLine(stack, source, cameraPos, line);
        });

        debugTexts.forEach((id, text) -> {
            renderDebugText(stack, source, cameraPos, text);
        });
    }

    private static void renderDebugBox(PoseStack stack, MultiBufferSource source, Vec3 cameraPos, DebugBox box) {
        BlockPos pos = box.pos();
        double x = pos.getX() - cameraPos.x();
        double y = pos.getY() - cameraPos.y();
        double z = pos.getZ() - cameraPos.z();

        AABB aabb = new AABB(x, y, z, x + 1, y + 1, z + 1).inflate(box.expand());

        if (box.fill()) {
            DebugRenderer.renderFilledBox(stack, source, aabb, box.r(), box.g(), box.b(), box.a());
        } else {
            // 渲染线框
            VertexConsumer consumer = source.getBuffer(RenderType.lines());
            LevelRenderer.renderLineBox(stack, consumer, aabb, box.r(), box.g(), box.b(), box.a());
        }
    }

    private static void renderDebugLine(PoseStack stack, MultiBufferSource source, Vec3 cameraPos, DebugLine line) {
        Vec3 start = line.start().subtract(cameraPos);
        Vec3 end = line.end().subtract(cameraPos);

        VertexConsumer consumer = source.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(
                stack, consumer,
                new AABB(start, end).inflate(line.width() / 2),
                line.r(), line.g(), line.b(), line.a()
        );
    }

    private static void renderDebugText(PoseStack stack, MultiBufferSource source, Vec3 cameraPos, DebugText text) {
        Vec3 pos = text.pos().subtract(cameraPos);
        DebugRenderer.renderFloatingText(
                stack, source,
                text.text(),
                pos.x(), pos.y(), pos.z(),
                text.color(),
                text.scale(),
                true,
                0,
                text.seeThrough()
        );
    }

    public static void addDebugBox(String id, BlockPos pos, float r, float g, float b, float a) {
        addDebugBox(id, pos, r, g, b, a, 0.0f, false);
    }

    public static void addDebugBox(String id, BlockPos pos, float r, float g, float b, float a, float expand, boolean fill) {
        debugBoxes.put(id, new DebugBox(pos, r, g, b, a, expand, fill));
    }

    public static void addDebugLine(String id, Vec3 start, Vec3 end, float r, float g, float b, float a) {
        addDebugLine(id, start, end, r, g, b, a, 1.0f / 16f);
    }

    public static void addDebugLine(String id, Vec3 start, Vec3 end, float r, float g, float b, float a, float width) {
        debugLines.put(id, new DebugLine(start, end, r, g, b, a, width));
    }

    public static void addDebugText(String id, String text, Vec3 pos, int color) {
        addDebugText(id, text, pos, color, 0.02f, false);
    }

    public static void addDebugText(String id, String text, Vec3 pos, int color, float scale, boolean seeThrough) {
        debugTexts.put(id, new DebugText(text, pos, color, scale, seeThrough));
    }

    public static void removeDebugBox(String id) {
        debugBoxes.remove(id);
    }

    public static void removeDebugLine(String id) {
        debugLines.remove(id);
    }

    public static void removeDebugText(String id) {
        debugTexts.remove(id);
    }

    public static void clearAllDebugElements() {
        debugBoxes.clear();
        debugLines.clear();
        debugTexts.clear();
    }

    public static List<String> getAllDebugBoxIds() {
        return new ArrayList<>(debugBoxes.keySet());
    }

    public static List<String> getAllDebugLineIds() {
        return new ArrayList<>(debugLines.keySet());
    }

    public static List<String> getAllDebugTextIds() {
        return new ArrayList<>(debugTexts.keySet());
    }

    private static final ConcurrentMap<String, Long> tempElements = new ConcurrentHashMap<>();

    public static void addTempDebugBox(String id, BlockPos pos, float r, float g, float b, float a, int durationTicks) {
        addDebugBox(id, pos, r, g, b, a);
        tempElements.put("box:" + id, System.currentTimeMillis() + durationTicks * 50L);
    }

    public static void addTempDebugLine(String id, Vec3 start, Vec3 end, float r, float g, float b, float a, int durationTicks) {
        addDebugLine(id, start, end, r, g, b, a);
        tempElements.put("line:" + id, System.currentTimeMillis() + durationTicks * 50L);
    }

    public static void addTempDebugText(String id, String text, Vec3 pos, int color, int durationTicks) {
        addDebugText(id, text, pos, color);
        tempElements.put("text:" + id, System.currentTimeMillis() + durationTicks * 50L);
    }

    @SubscribeEvent
    public static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        tempElements.entrySet().removeIf(entry -> {
            if (entry.getValue() < currentTime) {
                String key = entry.getKey();
                if (key.startsWith("box:")) {
                    debugBoxes.remove(key.substring(4));
                } else if (key.startsWith("line:")) {
                    debugLines.remove(key.substring(5));
                } else if (key.startsWith("text:")) {
                    debugTexts.remove(key.substring(5));
                }
                return true;
            }
            return false;
        });
    }
}
