package willow.train.kuayue.systems.overhead_line.block.support;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import kasuga.lib.core.client.model.BedrockModelLoader;
import kasuga.lib.core.client.model.anim_model.AnimModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineSupportBlockEntity;
import willow.train.kuayue.systems.overhead_line.block.support.variants.AllOverheadLineSupportModels;
import willow.train.kuayue.systems.overhead_line.render.CachedCurveRenderer;
import willow.train.kuayue.systems.overhead_line.render.OverheadLineCurveGenerator;
import willow.train.kuayue.systems.overhead_line.render.RenderCurve;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class OverheadSupportBlockRenderer extends SmartBlockEntityRenderer<OverheadLineSupportBlockEntity> implements BlockEntityRenderer<OverheadLineSupportBlockEntity> {

    private static final HashMap<Supplier<Block>, BlockEntityRendererProvider<OverheadLineSupportBlockEntity>> RENDERER_SUPPLIERS = new HashMap<>();
    private final HashMap<Block, BlockEntityRenderer<OverheadLineSupportBlockEntity>> RENDERERS = new HashMap<>();

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
                    String.format("#%d Absolute WorldPos: (%d, %d, %d)", i, connections.get(i).absolutePos().getX(), connections.get(i).absolutePos().getY(), connections.get(i).absolutePos().getZ()),
                    0,
                    0,
                    0xffffff
            );
            ms.translate(0, 10, 0);
        }
        ms.popPose();

        ms.pushPose();
        BlockPos pos = blockEntity.getBlockPos();
        ms.translate(-pos.getX(), -pos.getY(), -pos.getZ());
        // AllOverheadLineSupportModels.renderConnectionPointTest(blockEntity, ms, buffer, overlay);
        ms.popPose();

        ms.pushPose();
        ms.translate(-pos.getX(), -pos.getY(), -pos.getZ());
        for (OverheadLineSupportBlockEntity.Connection connection : blockEntity.getConnections()) {
            RenderCurve curve = OverheadLineCurveGenerator.conicHangLine(
                    blockEntity.getConnectionPointByIndex(connection.connectionIndex()),
                    new Vec3(connection.toPosition()),
                    1.3f,
                    1.3f,
                    0.5f,
                    5,
                    0.03f
            );
            CachedCurveRenderer.render(AllOverheadLineSupportModels.KUAYUE_TEST_LINE, curve, ms, buffer, light, overlay);
        }
        ms.popPose();
    }

    public static void register(Supplier<Block> block, Supplier<BlockEntityRendererProvider<OverheadLineSupportBlockEntity>> renderer) {
        RENDERER_SUPPLIERS.put(block, renderer.get());
    }

    public void apply(BlockEntityRendererProvider.Context pContext){
        for (var entry : RENDERER_SUPPLIERS.entrySet()) {
            var block = entry.getKey();
            var renderer = entry.getValue();
            RENDERERS.put(block.get(), renderer.create(pContext));
        }
    }
}
