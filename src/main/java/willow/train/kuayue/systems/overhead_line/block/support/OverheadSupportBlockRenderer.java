package willow.train.kuayue.systems.overhead_line.block.support;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import kasuga.lib.core.client.model.BedrockModelLoader;
import kasuga.lib.core.client.model.anim_model.AnimModel;
import kasuga.lib.registrations.BlockEntityRendererBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
import java.util.function.Supplier;

public class OverheadSupportBlockRenderer<T extends OverheadLineSupportBlockEntity> extends SmartBlockEntityRenderer<T> implements BlockEntityRenderer<T> {

    private static final HashMap<Supplier<Block>, BlockEntityRendererBuilder<OverheadLineSupportBlockEntity>> RENDERER_SUPPLIERS = new HashMap<>();
    private final HashMap<Block, BlockEntityRenderer<OverheadLineSupportBlockEntity>> RENDERERS = new HashMap<>();

    private final WeakHashMap<OverheadLineSupportBlockEntity.Connection, RenderCurve> curveRenderCache = new WeakHashMap<>();

    public OverheadSupportBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        apply(context);
        AllOverheadLineSupportModels.invoke();
    }

    @Override
    protected void renderSafe(OverheadLineSupportBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        Block block = blockEntity.getBlockState().getBlock();
        if(RENDERERS.containsKey(block)){
            RENDERERS.get(block).render(blockEntity, partialTicks, ms, buffer, light, overlay);
        }
        ms.pushPose();
        List<OverheadLineSupportBlockEntity.Connection> connections = blockEntity.getConnections();
        ms.scale(-0.01f,-0.01f,-0.01f);
        Minecraft.getInstance().font.drawInBatch(
                String.format("OverHeadLine (%d, %d, %d)", blockEntity.getBlockPos().getX(), blockEntity.getBlockPos().getY(), blockEntity.getBlockPos().getZ()),
                0,
                0,
                0xffffff,
                false,
                ms.last().pose(),
                buffer,
                Font.DisplayMode.NORMAL,
                0,
                light
        );
        ms.translate(0, 10, 0);
        for (int i = 0; i < connections.size(); i++) {
            Minecraft.getInstance().font.drawInBatch(
                    String.format("#%d Absolute WorldPos: (%d, %d, %d) Type %s", i, connections.get(i).absolutePos().getX(), connections.get(i).absolutePos().getY(), connections.get(i).absolutePos().getZ(), WireReg.getName(connections.get(i).type()).toString()),
                    0,
                    0,
                    0xffffff,
                    false,
                    ms.last().pose(),
                    buffer,
                    Font.DisplayMode.NORMAL,
                    0,
                    light
            );
            ms.translate(0, 10, 0);
        }
        ms.popPose();
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
}
