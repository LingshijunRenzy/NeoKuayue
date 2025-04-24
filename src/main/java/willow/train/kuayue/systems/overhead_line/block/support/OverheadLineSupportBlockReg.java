package willow.train.kuayue.systems.overhead_line.block.support;

import kasuga.lib.registrations.common.BlockEntityReg;
import kasuga.lib.registrations.common.BlockReg;
import kasuga.lib.registrations.common.CreativeTabReg;
import kasuga.lib.registrations.common.ItemReg;
import kasuga.lib.registrations.registry.SimpleRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import willow.train.kuayue.systems.overhead_line.OverheadLineSystem;
import willow.train.kuayue.systems.overhead_line.block.support.variants.OverheadLineBlockDynamicConfiguration;
import willow.train.kuayue.systems.overhead_line.types.OverheadLineType;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class OverheadLineSupportBlockReg<T extends OverheadLineSupportBlock<V>, V extends OverheadLineSupportBlockEntity> extends BlockReg<T> {
    private Predicate<OverheadLineType> allowedOverheadLineTypePredictor = (p)->true;
    private Supplier<BlockEntityRendererProvider<OverheadLineSupportBlockEntity>> renderer = null;
    private List<ResourceLocation> lineRenderModes = List.of();
    private List<Vec3> connectionPoints = List.of();

    public OverheadLineSupportBlockReg(String registrationKey) {
        super(registrationKey);
        this.withBlockEntity(OverheadLineSystem.OVERHEAD_LINE_SUPPORT_BLOCK_ENTITY);
    }

    public OverheadLineSupportBlockReg<T, V> allowOverheadLineType(Predicate<OverheadLineType> overheadLineTypePredictor) {
        this.allowedOverheadLineTypePredictor = overheadLineTypePredictor;
        return this;
    }

    public OverheadLineSupportBlockReg<T, V> withLineRendererMode(List<ResourceLocation> lineRendererTypes) {
        this.lineRenderModes = lineRendererTypes;
        return this;
    }

    @Override
    public OverheadLineSupportBlockReg<T, V> submit(SimpleRegistry simpleRegistry) {
        super.submit(simpleRegistry);
        if(this.renderer != null) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    ()->()-> OverheadSupportBlockRenderer.register(this::getBlock, this.renderer)
            );
        }
        OverheadLineBlockDynamicConfiguration configuration = new OverheadLineBlockDynamicConfiguration(
                this.connectionPoints,
                this.allowedOverheadLineTypePredictor,
                this.lineRenderModes
        );
        OverheadLineSupportBlockEntity.registerPoint(this::getBlock, configuration);
        return this;
    }

    @Override
    public <R extends Item> OverheadLineSupportBlockReg<T, V> withItem(ItemReg.ItemBuilder<R> builder, ResourceLocation itemModelLocation) {
        super.withItem(builder, itemModelLocation);
        return this;
    }

    @Override
    public OverheadLineSupportBlockReg<T, V> defaultBlockItem() {
        super.defaultBlockItem();
        return this;
    }

    @Override
    public OverheadLineSupportBlockReg<T, V> withBlockEntity(BlockEntityReg<? extends BlockEntity> blockEntityReg) {
        super.withBlockEntity(blockEntityReg);
        return this;
    }

    @Override
    public OverheadLineSupportBlockReg<T, V> blockType(BlockBuilder<T> builder) {
        super.blockType(builder);
        return this;
    }

    public OverheadLineSupportBlockReg<T, V> withRenderer(Supplier<BlockEntityRendererProvider<OverheadLineSupportBlockEntity>> renderer) {
        this.renderer = renderer;
        return this;
    }

    @Override
    public OverheadLineSupportBlockReg<T, V> tabTo(CreativeTabReg reg) {
        super.tabTo(reg);
        return this;
    }

    public OverheadLineSupportBlockReg<T, V> connectionPoints(Vec3 ...connectionPositions) {
        this.connectionPoints = List.of(connectionPositions);
        return this;
    }

    public String getIdentifier() {
        return "overhead_line_support_block";
    }
}