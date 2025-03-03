package willow.train.kuayue.block.recipe;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import kasuga.lib.core.client.render.texture.ImageMask;
import kasuga.lib.core.util.LazyRecomputable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    LazyRecomputable<ImageMask> subRightArrow = LazyRecomputable.of(
            () -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .copyWithOp(o -> o.rectangleUV(64f / 128f, 0, 96f / 128f, 18f / 128f))
    );

    LazyRecomputable<ImageMask> subRightArrow2 = LazyRecomputable.of(
            () -> subRightArrow.get().copyWithOp(o -> o)
    );

    LazyRecomputable<ImageMask> groupChosenFrame = LazyRecomputable.of(
            () -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .copyWithOp(o -> o.rectangleUV(64f / 128f, 18f / 128f,
                            80f / 128f, 36f / 128f))
    );

    int windowWidth = 0, windowHeight = 0;

    private int windowCapacity = 0, windowTop = 0;
    private final ArrayList<TechTreeItemButton> groupButtons;
    private ImageButton guideUpBtn, guideDownBtn;

    private float bgX = 0, bgY = 0, scale = 1.0f;
    private ClientTechTreeGroup chosenGroup;
    private final HashMap<ClientTechTreeGroup, TechTreePanel> panels;
    private final TechTreeTitleLabel titleLabel;
    private Tooltip tooltip = null;

    // for node chosen
    private TechTreeLabel chosenLabel = null;
    private final HashSet<TechTreeLabel> prevLabels, nextLabels;
    private ArrayList<LabelGrid> prevGrids, nextGrids;
    private int prevGridIndex = -1, nextGridIndex = -1;
    private final ItemSlot[] consumptionSlots, resultSlots;
    private MutableComponent nodeTitleComponent = null;


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
        consumptionSlots = new ItemSlot[8];
        resultSlots = new ItemSlot[4];
        for (int i = 0; i < consumptionSlots.length; i++)
            consumptionSlots[i] = new ItemSlot(0, 0);
        for (int i = 0; i < resultSlots.length; i++)
            resultSlots[i] = new ItemSlot(0, 0);
    }

    public ImageButton genArrowButton(int x, int y, Button.OnPress action, boolean upArrow) {
        LazyRecomputable<ImageMask> mask = upArrow ?
                LazyRecomputable.of(() -> this.upArrow.get().copyWithOp(m -> m)) :
                LazyRecomputable.of(() -> this.downArrow.get().copyWithOp(m -> m));
        return new ImageButton(mask, x, y, 16, 8, Component.empty(), action);
    }

    public void updateGrids() {
        clearAllGrids();
        ArrayList<TechTreeLabel> prevLabels = new ArrayList<>(this.prevLabels);
        ArrayList<TechTreeLabel> nextLabels = new ArrayList<>(this.nextLabels);
        OnClick<TechTreeLabel> click = (label, mx, my) -> {
            setChosenLabel(label);
        };
        for (int i = 0; ;i += 9) {
            if (i >= prevLabels.size() && i >= nextLabels.size())
                break;
            genGrid(prevLabels, click, i, prevGrids);
            genGrid(nextLabels, click, i, nextGrids);
        }
        if (!prevGrids.isEmpty()) {
            prevGridIndex = 0;
            prevGrids.get(prevGridIndex).visible = true;
        }
        if (!nextGrids.isEmpty()) {
            nextGridIndex = 0;
            nextGrids.get(nextGridIndex).visible = true;
        }
        updateGridsPosition(scale);
    }

    private void updateSlotPos(float scale) {
        consumptionSlots[0].setPosition(getBgX() + map( 55, scale) + getSlotSidePos(scale),
                getBgY() + map( 113, scale) + getSlotSidePos(scale));
        for (int i = 1; i < 7; i++) {
            consumptionSlots[i].setPosition(getBgX() + map(71, scale) +
                            ((i - 1) / 2) * getSlotSide(scale) + getSlotSidePos(scale),
                    getBgY() + map(105, scale) + ((i - 1) % 2) * getSlotSide(scale) + getSlotSidePos(scale));
        }
        consumptionSlots[7].setPosition(getBgX() + map(119, scale) + getSlotSidePos(scale),
                getBgY() + map(113, scale) + getSlotSidePos(scale));
        resultSlots[0].setPosition(getBgX() + map(216, scale) + getSlotSidePos(scale),
                getBgY() + map(114, scale) + getSlotSidePos(scale));
        resultSlots[1].setPosition(getBgX() + map(231, scale) + getSlotSidePos(scale),
                getBgY() + map(105, scale) + getSlotSidePos(scale));
        resultSlots[2].setPosition(getBgX() + map(231, scale) + getSlotSidePos(scale),
                getBgY() + map(105, scale) + getSlotSide(scale) + getSlotSidePos(scale));
        resultSlots[3].setPosition(getBgX() + map(247, scale) + getSlotSidePos(scale),
                getBgY() + map(113, scale) + getSlotSidePos(scale));
    }

    public void updateSlotItems(ClientTechTreeNode node) {
        clearSlotItems();
        int counter = 0;
        for (ItemStack item : node.getItemConsume()) {
            ItemSlot slot = consumptionSlots[counter];
            slot.setItemStack(item);
            counter++;
            if (counter >= consumptionSlots.length) break;
        }
    }

    public void clearSlotItems() {
        for (ItemSlot slot : consumptionSlots) {
            slot.setItemStack(ItemStack.EMPTY);
        }
        for (ItemSlot slot : resultSlots) {
            slot.setItemStack(ItemStack.EMPTY);
        }
    }

    private void renderAllSlots(boolean flag) {
        for (ItemSlot slot : consumptionSlots)
            slot.visible = flag;
        for (ItemSlot slot : resultSlots)
            slot.visible = flag;
    }

    private int getSlotSide(float scale) {
        return Math.round(scale * 16f);
    }

    private int getSlotSidePos(float scale) {
        return getSlotSide(scale) / 2 - 8;
    }

    private void genGrid(ArrayList<TechTreeLabel> nextLabels, OnClick<TechTreeLabel> click, int i, ArrayList<LabelGrid> nextGrids) {
        if (i < nextLabels.size()) {
            LabelGrid grid = new LabelGrid(0, 0, nextLabels.subList(i, nextLabels.size()));
            grid.visible = false;
            grid.setOnClick(click);
            nextGrids.add(grid);
            addRenderableWidget(grid);
        }
    }

    private void updateGridsPosition(float scale) {
        prevGrids.forEach(grid -> {
            grid.setPos(map(getBgX() + Math.round(314f / 4f) - Math.round(grid.getWidth() / 2f), scale),
                    map(getBgY() + 55 - grid.getHeight() / 2, scale));
        });
        nextGrids.forEach(grid -> {
            grid.setPos(map(getBgX() + Math.round(942f / 4f) - Math.round(grid.getWidth() / 2f), scale),
                    map(getBgY() + 55 - grid.getHeight() / 2, scale));
        });
    }

    private void renderSubArrows(float scale) {
        if (!prevGrids.isEmpty()) {
            ImageMask arrow1 = subRightArrow.get();
            arrow1.rectangle(new Vector3f(map(getBgX() + Math.round(314f * 3 / 8) - 16, scale), map(getBgY() + 55 - 9, scale), 0),
                    ImageMask.Axis.X, ImageMask.Axis.Y, true, true, 32, 18);
            arrow1.renderToGui();
        }
        if (!nextGrids.isEmpty()) {
            ImageMask arrow2 = subRightArrow2.get();
            arrow2.rectangle(new Vector3f(map(getBgX() + Math.round(314f * 5 / 8) - 17, scale), map(getBgY() + 55 - 9, scale), 0),
                    ImageMask.Axis.X, ImageMask.Axis.Y, true, true, 32, 18);
            arrow2.renderToGui();
        }
    }

    private void clearAllGrids() {
        prevGrids.forEach(this::removeWidget);
        nextGrids.forEach(this::removeWidget);
        prevGrids.clear();
        nextGrids.clear();
        clearGridIndex();
    }

    private void clearGridIndex() {
        prevGridIndex = -1;
        nextGridIndex = -1;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(titleLabel);
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
                    setChosenLabel(label);
                });
                addRenderableWidget(panel);
                panel.visible = group.getValue() == chosenGroup;
            }
        }
        updateSlotPos(scale);
        for (ItemSlot slot : consumptionSlots) {
            addRenderableWidget(slot);
        }
        for (ItemSlot slot : resultSlots) {
            addRenderableWidget(slot);
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
                    .add(new TechTreeItemButton(group.getIcon(), 20, 20, group,
                            (a, b, c) -> {
                        showSub = false;
                        chosenGroup = group;
                        panels.forEach((g, p) -> p.visible = g == chosenGroup);
                        titleLabel.setTitle(Component.translatable(chosenGroup.getTitleKey()));
                        clearSub();
                        clearAllGrids();
                        renderAllSlots(false);
                        titleLabel.visible = true;
                        updateFramePosition(chosenGroup);
                    })));
        }
        groupButtons.forEach(btn -> {
            addRenderableWidget(btn);
            btn.setVisible(false);
        });
        updateGuidelines(this.scale);
    }

    private void setChosenLabel(TechTreeLabel label) {
        updateSub(label.getNode());
        updateGrids();
        updateSlotPos(scale);
        renderAllSlots(true);
        nodeTitleComponent = Component.translatable(label.getNode().getName())
                .setStyle(Style.EMPTY.applyFormat(ChatFormatting.BOLD)
                        .withUnderlined(true));
        updateSlotItems(label.getNode());
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
        updateGuidelines(scale);
        clearAllGrids();
        renderAllSlots(false);
        updateSlotPos(scale);
        titleLabel.visible = true;
    }

    private void onScaleChanged(float neoScale) {
        if (neoScale == scale) return;
        scale = neoScale;
        setPanelsSize();
        updateGuidelines(scale);
        clearAllGrids();
        renderAllSlots(false);
        updateSlotPos(scale);
        titleLabel.visible = true;
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
        updateFramePosition(chosenGroup);
    }

    private void updateFramePosition(ClientTechTreeGroup group) {
        if (group == null) return;
        int index = -1;
        TechTreeItemButton chosenButton = null;
        for (int i = 0; i < groupButtons.size(); i++) {
            TechTreeItemButton button = groupButtons.get(i);
            if (chosenGroup == button.getGroup()) {
                index = i;
                chosenButton = button;
                break;
            }
        }
        if (index < 0) return;
        index -= windowTop;
        if (index >= windowCapacity) return;
        ImageMask frame = groupChosenFrame.get();
        frame.rectangle(new Vector3f(chosenButton.x + 2, chosenButton.y + 2, 0),
                ImageMask.Axis.X, ImageMask.Axis.Y, true, true,
                16, 18);
    }

    private void updateSub(ClientTechTreeNode chosenNode) {
        if (chosenLabel != null) clearSub();
        // 150 - 12, 55 - 12
        chosenLabel = TechTreeLabel.largeLabel(chosenNode, map(getBgX() + 145, scale),
                map(getBgY() + 43, scale), Component.empty());
        addRenderableWidget(chosenLabel);
        for (ClientTechTreeNode node : chosenNode.getPrevNode()) {
            TechTreeLabel label = TechTreeLabel.smallLabel(node, 0, 0, Component.empty());
            prevLabels.add(label);
        }
        for (ClientTechTreeNode node : chosenNode.getNextNode()) {
            TechTreeLabel label = TechTreeLabel.smallLabel(node, 0, 0, Component.empty());
            nextLabels.add(label);
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
                               TechTreeItemButton grpBtn,
                               int mouseX, int mouseY, float partial) {
        if (label == null && grpBtn == null) return;
        int tooltipX, tooltipY, smallerX;
        if (grpBtn != null) {
            ClientTechTreeGroup group = grpBtn.getGroup();
            if (tooltip == null) {
                tooltip = Tooltip.fromGroup(group);
            } else if (!tooltip.is(group)) {
                tooltip = Tooltip.fromGroup(group);
            }
            tooltipX = grpBtn.x + grpBtn.getWidth();
            tooltipY = grpBtn.y;
            smallerX = grpBtn.x;
        } else {
            ClientTechTreeNode node = label.getNode();
            if (tooltip == null) {
                tooltip = Tooltip.fromNode(node);
            } else if (!tooltip.is(node)) {
                tooltip = Tooltip.fromNode(node);
            }
            tooltipX = label.x + label.getWidth();
            tooltipY = label.y;
            smallerX = label.x;
        }
        if (tooltipX + 1 + tooltip.getWidth() <= windowWidth)
            tooltip.setPosition(tooltipX + 1, tooltipY);
        else
            tooltip.setPosition(smallerX - tooltip.getWidth() - 1, tooltipY);
        tooltip.render(poseStack, mouseX, mouseY, partial);
    }

    private int getBgX() {
        return Math.round(bgX);
    }

    private int getBgY() {
        return Math.round(bgY);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        if (chosenGroup != null)
            groupChosenFrame.get().renderToGui();
        if (showSub) {
            panels.forEach((g, p) -> p.visible = false);
            renderSubArrows(scale);
            if (nodeTitleComponent != null) {
                font.draw(poseStack, nodeTitleComponent,
                        bgX + map(39, scale),
                        bgY + map(16, scale), 0xffffff);
            }
        }
        TechTreeItemButton button = null;
        for (TechTreeItemButton btn : groupButtons) {
            if (btn.isMouseOver(mouseX, mouseY) && btn.visible) {
                button = btn;
                break;
            }
        }
        TechTreeLabel label = getChosenLabel(mouseX, mouseY);
        renderTooltip(poseStack, label, button, mouseX, mouseY, partialTick);
    }

    public @Nullable TechTreeLabel getChosenLabel(double mouseX, double mouseY) {
        if (!showSub) {
            if (chosenGroup == null || !panels.containsKey(chosenGroup)) return null;
            if (!panels.get(chosenGroup).visible) return null;
            return panels.get(chosenGroup).getChosenLabel(mouseX, mouseY);
        } else {
            if (chosenLabel != null && chosenLabel.isMouseOver(mouseX, mouseY))
                return chosenLabel;
            if (prevGridIndex != -1) {
                TechTreeLabel label = prevGrids.get(prevGridIndex)
                        .getChosenLabel(mouseX, mouseY);
                if (label != null) return label;
            }
            if (nextGridIndex != -1) {
                return nextGrids.get(nextGridIndex)
                        .getChosenLabel(mouseX, mouseY);
            }
        }
        return null;
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        // super.renderLabels(pPoseStack, pMouseX, pMouseY);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (showSub) return false;
        boolean flag = super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        for (Map.Entry<ClientTechTreeGroup, TechTreePanel> entry : panels.entrySet()) {
            flag |= entry.getValue().mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
        return flag;
    }

}
