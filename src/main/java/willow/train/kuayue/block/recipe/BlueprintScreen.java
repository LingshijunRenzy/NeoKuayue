package willow.train.kuayue.block.recipe;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import kasuga.lib.core.client.render.texture.ImageMask;
import kasuga.lib.core.util.LazyRecomputable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import willow.train.kuayue.initial.ClientInit;
import willow.train.kuayue.systems.editable_panel.widget.ImageButton;
import willow.train.kuayue.systems.editable_panel.widget.OnClick;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTree;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeGroup;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeManager;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeNode;
import willow.train.kuayue.systems.tech_tree.client.gui.*;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class BlueprintScreen extends AbstractContainerScreen<BlueprintMenu> {

    private boolean showSub, hasJei;
    private final HashMap<String, ClientTechTree> trees;
    LazyRecomputable<ImageMask> bgMask = LazyRecomputable.of(() -> new ImageMask(ClientInit.blueprintTableBg.getImageSafe().get()));
    LazyRecomputable<ImageMask> bgNoSubMask = LazyRecomputable.of(() -> new ImageMask(ClientInit.blueprintTableNoSub.getImageSafe().get()));
    LazyRecomputable<ImageMask> upArrow = LazyRecomputable.of(
            () -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .copyWithOp(o -> o.rectangleUV(32f / 128f, 32f / 128f,
                            48f / 128f, 40f / 128f))
    );

    LazyRecomputable<ImageMask> downArrow = LazyRecomputable.of(
            () -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .copyWithOp(o -> o.rectangleUV(32f / 128f, 40f / 128f,
                            48f / 128f, 48f / 128f))
    );
    int windowWidth = 0, windowHeight = 0;

    private int windowCapacity = 0, windowTop = 0;
    private final ArrayList<TechTreeItemButton> groupButtons;
    private ImageButton guideUpBtn, guideDownBtn;

    private float bgX = 0, bgY = 0, scale = 1.0f;
    private ClientTechTreeGroup chosenGroup;
    private final HashMap<ClientTechTreeGroup, TechTreePanel> panels;
    private final TechTreeTitleLabel titleLabel;
    private NodeTooltip tooltip = null;

    // for node chosen
    private TechTreeLabel chosenLabel = null;
    private final HashSet<TechTreeLabel> prevLabels, nextLabels;
    private ArrayList<LabelGrid> prevGrids, nextGrids;

    public BlueprintScreen(BlueprintMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.showSub = false;
        trees = ClientTechTreeManager.MANAGER.trees();
        groupButtons = new ArrayList<>();
        this.hasJei = ModList.get().isLoaded("jei");
        panels = new HashMap<>();
        for (Map.Entry<String, ClientTechTree> tree : trees.entrySet()) {
            for (Map.Entry<String, ClientTechTreeGroup> group :
                    tree.getValue().getGroups().entrySet()) {
                chosenGroup = group.getValue();
                break;
            }
            break;
        }
        titleLabel = new TechTreeTitleLabel(chosenGroup == null ? Component.empty() :
                Component.translatable(chosenGroup.getTitleKey()));
        this.prevLabels = new HashSet<>();
        this.nextLabels = new HashSet<>();
        this.prevGrids = new ArrayList<>();
        this.nextGrids = new ArrayList<>();
    }

    public ImageButton genArrowButton(int x, int y, Button.OnPress action, boolean upArrow) {
        LazyRecomputable<ImageMask> mask = upArrow ? this.upArrow : this.downArrow;
        return new ImageButton(mask, x, y, 16, 8, Component.empty(), action);
    }

    public void updateGrids() {
        this.prevGrids.clear();
        this.nextGrids.clear();
        ArrayList<TechTreeLabel> prevLabels = new ArrayList<>(this.prevLabels);
        ArrayList<TechTreeLabel> nextLabels = new ArrayList<>(this.nextLabels);
        for (int i = 0; ;i += 9) {
            if (i >= prevLabels.size() && i >= nextLabels.size())
                break;
            if (i < prevLabels.size()) {
                LabelGrid grid = new LabelGrid(0, 0, prevLabels.subList(i, prevLabels.size()));
                prevGrids.add(grid);
            }
            if (i < nextLabels.size()) {
                LabelGrid grid = new LabelGrid(0, 0, nextLabels.subList(i, nextLabels.size()));
                nextGrids.add(grid);
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        onRefresh();
        for (Map.Entry<String, ClientTechTree> tree : trees.entrySet()) {
            for (Map.Entry<String, ClientTechTreeGroup> group :
                    tree.getValue().getGroups().entrySet()) {
                TechTreePanel panel = new TechTreePanel(0, 0, 300, 200, 100, 100);
                panel.compileGroup(group.getValue());
                panels.put(group.getValue(), panel);
                panel.setOnClick((p, mX, mY) -> {
                    TechTreeLabel label = p.getChosenLabel(mX, mY);
                    if (label == null) return;
                    updateSub(label.getNode());
                    updateGrids();
                });
                addRenderableWidget(panel);
                panel.visible = group.getValue() == chosenGroup;
            }
        }
        showSub =false;
    }

    public void setGroupsUpAndDownArrowButton() {
        if (this.guideDownBtn != null)
            removeWidget(this.guideDownBtn);
        if (this.guideUpBtn != null)
            addWidget(this.guideUpBtn);
        ImageButton upArrowBtn = genArrowButton(0, 0,
                button -> moveGuideWindow(true), true);
        ImageButton downArrowBtn = genArrowButton(0, 0,
                button -> moveGuideWindow(false), false);
        addRenderableWidget(upArrowBtn);
        addRenderableWidget(downArrowBtn);
        guideUpBtn = upArrowBtn;
        guideDownBtn = downArrowBtn;
    }

    public void moveGuideWindow(boolean up) {
        if (up && windowTop > 0) {
            windowTop --;
            return;
        }
        if (!up && windowTop < groupButtons.size() - windowCapacity)
            windowTop ++;
    }

    public void onRefresh() {
        groupButtons.forEach(this::removeWidget);
        groupButtons.clear();
        for (ClientTechTree tree : trees.values()) {
            tree.getGroups().forEach((name, group) -> groupButtons
                    .add(new TechTreeItemButton(group.getIcon(), 20, 20, (a, b, c) -> {
                        showSub = false;
                        chosenGroup = group;
                        panels.forEach((g, p) -> p.visible = g == chosenGroup);
                        titleLabel.setTitle(Component.translatable(chosenGroup.getTitleKey()));
                        clearSub();
                    })));
        }
        groupButtons.forEach(btn -> {
            addRenderableWidget(btn);
            btn.setVisible(false);
        });
        updateGuidelines(this.scale);
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick,
                            int mouseX, int mouseY) {
        renderBackground(poseStack);
        Minecraft mc = Minecraft.getInstance();
        ImageMask mask = setParams(mc);
        if (mask == null) return;
        poseStack.pushPose();
        mask.renderToGui(poseStack.last());
        poseStack.popPose();
    }

    private void onPositionChanged(float neoBgx, float neoBgy) {
        if (neoBgx == bgX && neoBgy == bgY) return;
        bgX = neoBgx;
        bgY = neoBgy;
        setPanelsPosition();
        panels.forEach((g, p) -> {
            p.setSize(map(247, scale), map(117 ,scale));
            // p.adjustSize(map(247, scale), map(117, scale));
            p.moveToWindowCentral(scale);
        });
    }

    private void onScaleChanged(float neoScale) {
        if (neoScale == scale) return;
        scale = neoScale;
        setPanelsSize();
        updateGuidelines(scale);
    }

    private void setPanelsPosition() {
        panels.forEach((g, p) -> p.setPosition(
                Math.round(bgX + map(33, scale)),
                Math.round(bgY + map(15, scale))
        ));
    }

    private void setPanelsSize() {
        panels.forEach((g, p) -> p.setSize(map(252, scale), map(122, scale)));
    }

    private ImageMask setParams(Minecraft mc) {
        if (mc.screen == null) return null;
        windowWidth = mc.screen.width;
        windowHeight = mc.screen.height;
        ImageMask mask = showSub ? bgMask.get() : bgNoSubMask.get();
        int w = (int) (windowWidth * (hasJei ? .7f : .9f));
        onScaleChanged(((float) w / (float) mask.getImage().width()));
        int h = map(mask.getImage().height(), scale);
        onPositionChanged((windowWidth * (hasJei ? .725f : 1f) - w) / 2, (float) (windowHeight - h) / 2);
        mask.rectangle(new Vector3f(bgX, bgY, 0),
                ImageMask.Axis.X, ImageMask.Axis.Y,
                true, true, w, h);
        return mask;
    }

    private int map(int xOry, float scale) {
        return (int) (xOry * scale);
    }

    // leftUp border: (7, 19); (23, 106)
    private void updateGuidelines(float scale) {
        int leftTopX = map(7, scale);
        int leftTopY = map(19, scale);

        int rightDownX = map(23, scale);
        int rightDownY = map(106, scale);

        int guideWidth = rightDownX - leftTopX;
        int guideHeight = rightDownY - leftTopY;
        int btnHeight = guideHeight - 20;
        windowCapacity = btnHeight / 20;

        titleLabel.setPosition(Math.round(bgX) + map(35, scale), Math.round(bgY) + map(12, scale));

        int grpBtnY = Math.round(bgY) + leftTopY + (guideHeight - btnHeight) / 2;
        int grpBtnX = Math.round(bgX) + leftTopX + (guideWidth - 20) / 2;
        for (int i = 0; i < groupButtons.size(); i++) {
            boolean flag = i >= windowTop &&
                    i < Math.min(windowTop + windowCapacity, groupButtons.size());
            TechTreeItemButton button = groupButtons.get(i);
            if (flag) button.setPosition(grpBtnX, grpBtnY + (i - windowTop) * 20);
            button.setVisible(flag);
        }
        if (this.windowCapacity < this.groupButtons.size() &&
                this.guideDownBtn == null && this.guideUpBtn == null) {
            setGroupsUpAndDownArrowButton();
        }
        int btnX = Math.round(bgX + leftTopX + (float) (guideWidth - 16) / 2);
        if (guideUpBtn != null)
            guideUpBtn.setPos(btnX, Math.round(bgY) + leftTopY + 1);
        if (guideDownBtn != null)
            guideDownBtn.setPos(btnX, Math.round(bgY) + rightDownY - 8);
    }

    public void updateSub(ClientTechTreeNode chosenNode) {
        if (chosenLabel != null) clearSub();
        chosenLabel = TechTreeLabel.largeLabel(chosenNode, map(getBgX() + 140, scale),
                map(getBgY() + 45, scale), Component.empty());
        addRenderableWidget(chosenLabel);
        for (ClientTechTreeNode node : chosenNode.getPrevNode()) {
            TechTreeLabel label = TechTreeLabel.smallLabel(node, 0, 0, Component.empty());
            prevLabels.add(label);
            addRenderableWidget(label);
        }
        for (ClientTechTreeNode node : chosenNode.getNextNode()) {
            TechTreeLabel label = TechTreeLabel.smallLabel(node, 0, 0, Component.empty());
            nextLabels.add(label);
            addRenderableWidget(label);
        }
        this.showSub = true;
        titleLabel.visible = false;
    }

    public void clearSub() {
        removeWidget(chosenLabel);
        chosenLabel = null;
        prevLabels.forEach(this::removeWidget);
        nextLabels.forEach(this::removeWidget);
        prevLabels.clear();
        nextLabels.clear();
        showSub = false;
        titleLabel.visible = true;
    }

    private void renderTooltip(PoseStack poseStack, TechTreeLabel label,
                               int mouseX, int mouseY, float partial) {
        if (label == null) return;
        ClientTechTreeNode node = label.getNode();
        if (tooltip == null) {
            tooltip = new NodeTooltip(node);
            tooltip.updateNode();
        } else if (tooltip.getNode() != node) {
            tooltip = new NodeTooltip(node);
            tooltip.updateNode();
        }
        if (label.getX() + label.getWidth() + 1 + tooltip.getWidth() <= windowWidth)
            tooltip.setPosition(label.getX() + label.getWidth() + 1, label.getY());
        else
            tooltip.setPosition(label.getX() - tooltip.getWidth() - 1, label.getY());
        tooltip.render(poseStack, mouseX, mouseY, partial);
    }

    private int getBgX() {
        return Math.round(bgX);
    }

    private int getBgY() {
        return Math.round(bgY);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        if (showSub) {
            panels.forEach((g, p) -> p.visible = false);
            return;
        }
        if (chosenGroup == null || !panels.containsKey(chosenGroup)) return;
        if (!panels.get(chosenGroup).visible) return;
        TechTreeLabel label = panels.get(chosenGroup).getChosenLabel(mouseX, mouseY);
        renderTooltip(poseStack, label, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        // super.renderLabels(pPoseStack, pMouseX, pMouseY);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        boolean flag = super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        for (Map.Entry<ClientTechTreeGroup, TechTreePanel> entry : panels.entrySet()) {
            flag |= entry.getValue().mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
        return flag;
    }

}
