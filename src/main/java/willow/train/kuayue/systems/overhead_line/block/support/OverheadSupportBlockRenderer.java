package willow.train.kuayue.systems.overhead_line.block.support;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import kasuga.lib.core.client.model.BedrockModelLoader;
import kasuga.lib.core.client.model.anim_model.AnimModel;
import kasuga.lib.registrations.BlockEntityRendererBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.systems.overhead_line.block.line.OverheadLineRendererSystem;
import willow.train.kuayue.systems.overhead_line.block.line.PositionComparator;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineSupportBlockEntity;
import willow.train.kuayue.systems.overhead_line.block.support.variants.AllOverheadLineSupportModels;
import willow.train.kuayue.systems.overhead_line.render.CachedCurveRenderer;
import willow.train.kuayue.systems.overhead_line.render.OverheadLineCurveGenerator;
import willow.train.kuayue.systems.overhead_line.render.RenderCurve;
import willow.train.kuayue.systems.overhead_line.wire.WireReg;

import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class OverheadSupportBlockRenderer<T extends OverheadLineSupportBlockEntity> extends SmartBlockEntityRenderer<T> implements BlockEntityRenderer<T> {

    private static final HashMap<Supplier<Block>, BlockEntityRendererBuilder<OverheadLineSupportBlockEntity>> RENDERER_SUPPLIERS = new HashMap<>();
    private final HashMap<Block, BlockEntityRenderer<OverheadLineSupportBlockEntity>> RENDERERS = new HashMap<>();

    private final WeakHashMap<ConnectionCacheKey, RenderCurve> curveRenderCache = new WeakHashMap<>();

    private static final ConcurrentHashMap<BlockPos, OverheadSupportBlockRenderer> RENDERER_INSTANCES = new ConcurrentHashMap<>();

    public OverheadSupportBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        apply(context);
        AllOverheadLineSupportModels.invoke();
    }

    @Override
    protected void renderSafe(OverheadLineSupportBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        RENDERER_INSTANCES.put(blockEntity.getBlockPos(), this);

        Block block = blockEntity.getBlockState().getBlock();
        if(RENDERERS.containsKey(block)){
            RENDERERS.get(block).render(blockEntity, partialTicks, ms, buffer, light, overlay);
        }
        if(Minecraft.getInstance().options.renderDebug){
            ms.pushPose();
            List<OverheadLineSupportBlockEntity.Connection> connections = blockEntity.getConnections();
            ms.scale(-0.01f,-0.01f,-0.01f);
            Minecraft.getInstance().font.draw(
                    ms,
                    String.format("OverHeadLine (%d, %d, %d)", blockEntity.getBlockPos().getX(), blockEntity.getBlockPos().getY(), blockEntity.getBlockPos().getZ()),
                    0,
                    0,
                    0xffffff
            );
            ms.translate(0, 10, 0);
            for (int i = 0; i < connections.size(); i++) {
                Minecraft.getInstance().font.draw(
                        ms,
                        String.format("#%d Absolute WorldPos: (%d, %d, %d) Type %s", i, connections.get(i).absolutePos().getX(), connections.get(i).absolutePos().getY(), connections.get(i).absolutePos().getZ(), WireReg.getName(connections.get(i).type()).toString()),
                        0,
                        0,
                        0xffffff
                );
                ms.translate(0, 10, 0);
            }
            ms.popPose();
        }
        /*
        ms.popPose();
        ms.pushPose();
        BlockPos pos = blockEntity.getBlockPos();
        ms.translate(-pos.getX(), -pos.getY(), -pos.getZ());
        ms.popPose();

        ms.pushPose();
        ms.translate(-pos.getX(), -pos.getY(), -pos.getZ());
        for (OverheadLineSupportBlockEntity.Connection connection : blockEntity.getConnections()) {
            if(PositionComparator.compareBlockPosition(
                    connection.absolutePos(),
                    blockEntity.getBlockPos()
            ) < 0) {
                continue;
            }

            RenderCurve curve = curveRenderCache.computeIfAbsent(connection, (c) -> OverheadLineRendererSystem.getRendererFor(connection.type()).getRenderCurveFor(
                    blockEntity.getLevel(),
                    blockEntity.getConnectionPointByIndex(connection.connectionIndex()),
                    new Vec3(connection.toPosition())
            ));


            CachedCurveRenderer.render(OverheadLineRendererSystem.getRendererFor(connection.type()).getModel(), curve, ms, buffer, overlay);
        }
         */
    }

    public static void register(Supplier<Block> block, Supplier<BlockEntityRendererBuilder<OverheadLineSupportBlockEntity>> renderer) {
        RENDERER_SUPPLIERS.put(block, renderer.get());
    }

    public void apply(BlockEntityRendererProvider.Context pContext){
        for (var entry : RENDERER_SUPPLIERS.entrySet()) {
            var block = entry.getKey();
            var renderer = entry.getValue();
            RENDERERS.put(block.get(), renderer.build(pContext));
        }
    }

    public static void clearCacheForBlockEntity(OverheadLineSupportBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();

        OverheadSupportBlockRenderer<?> renderer = RENDERER_INSTANCES.get(pos);
        if (renderer != null) {
            renderer.curveRenderCache.entrySet().removeIf(entry -> {
                ConnectionCacheKey key = entry.getKey();
                return key.fromPos.equals(pos) || key.toPos.equals(pos);
            });
        }
    }

    private static class ConnectionCacheKey {
        private final BlockPos fromPos;
        private final BlockPos toPos;
        private final int connectionIndex;
        private final int targetIndex;
        private final float toPosX, toPosY, toPosZ; // 包含精确的位置信息

        public ConnectionCacheKey(OverheadLineSupportBlockEntity.Connection connection, BlockPos fromPos) {
            this.fromPos = fromPos;
            this.toPos = connection.absolutePos();
            this.connectionIndex = connection.connectionIndex();
            this.targetIndex = connection.targetIndex();
            this.toPosX = connection.toPosition().x();
            this.toPosY = connection.toPosition().y();
            this.toPosZ = connection.toPosition().z();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ConnectionCacheKey that)) return false;

            return connectionIndex == that.connectionIndex &&
                    targetIndex == that.targetIndex &&
                    Float.compare(that.toPosX, toPosX) == 0 &&
                    Float.compare(that.toPosY, toPosY) == 0 &&
                    Float.compare(that.toPosZ, toPosZ) == 0 &&
                    fromPos.equals(that.fromPos) &&
                    toPos.equals(that.toPos);
        }

        @Override
        public int hashCode() {
            int result = fromPos.hashCode();
            result = 31 * result + toPos.hashCode();
            result = 31 * result + connectionIndex;
            result = 31 * result + targetIndex;
            result = 31 * result + Float.floatToIntBits(toPosX);
            result = 31 * result + Float.floatToIntBits(toPosY);
            result = 31 * result + Float.floatToIntBits(toPosZ);
            return result;
        }

        @Override
        public String toString() {
            return String.format("ConnectionCacheKey{%s->%s, idx=%d->%d, pos=(%.3f,%.3f,%.3f)}",
                    fromPos, toPos, connectionIndex, targetIndex, toPosX, toPosY, toPosZ);
        }
    }
}
