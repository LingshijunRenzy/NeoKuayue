package willow.train.kuayue.systems.editable_panel.screens;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import kasuga.lib.core.client.render.SimpleColor;
import kasuga.lib.core.client.render.texture.ImageMask;
import kasuga.lib.core.client.render.texture.Vec2f;
import kasuga.lib.core.util.LazyRecomputable;
import kasuga.lib.core.util.data_type.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;
import willow.train.kuayue.block.panels.block_entity.EditablePanelEntity;
import willow.train.kuayue.initial.AllPackets;
import willow.train.kuayue.initial.ClientInit;
import willow.train.kuayue.network.c2s.DiscardChangeC2SPacket;
import willow.train.kuayue.network.c2s.NbtC2SPacket;
import willow.train.kuayue.systems.editable_panel.EditablePanelEditMenu;
import willow.train.kuayue.systems.editable_panel.widget.*;

/**
 * 水牌的GUI呈现
 * GUI开发的第一步就是要有一个UI。MC的GUI分为Screen，即窗口；是GUI的总呈现器。一个这些Widget与玩家的交互
 *
 * @author 童话的爱
 * @since 2025-03-29
 */
// 这里AbstractContainerScreen后面的泛型要填你的menu类。
public class LaqueredScreen
        extends CustomScreen<EditablePanelEditMenu, EditablePanelEntity>
//        extends AbstractContainerScreen<LaqueredMenu>
{

    // 以下代码来自于 src\main\java\willow\train\kuayue\systems\editable_panel\screens\TypeScreen.java
    private int color;
    public boolean revert;
    public Label titleLabel;
    public ImageButton mirrorBtn, cancelBtn, confirmBtn;
    public EditBar editBar;
    public ColorScreenBundles colorEditor;
    private OffsetEditor offsetEditor;
    private float bgX = 0, bgY = 0, scale = 1.0f;
    private boolean showSub, hasJei;

    private final LazyRecomputable<ImageMask> cancelBtnImage =
            new LazyRecomputable<>(() -> GetShareTemplateScreen.cancelImage.get().copyWithOp(p -> p));

    private final LazyRecomputable<ImageMask> acceptBtnImage =
            new LazyRecomputable<>(() -> GetShareTemplateScreen.acceptImage.get().copyWithOp(p -> p));

    private final LazyRecomputable<ImageMask> mirrorBtnImage =
            new LazyRecomputable<>(() -> ColorTemplateScreen.buttons.get()
                    .copyWithOp(p -> p.rectangleUV(.125f, .375f, .25f, .5f)));

    public static final LazyRecomputable<ImageMask> laqueredBoardLogo = new LazyRecomputable<>(
            () -> ClientInit.laqueredBoardLogo.getImageSafe().get().getMask()
                    .rectangleUV(0, 0, 1, 1)
    );

    public static final LazyRecomputable<ImageMask> laqueredBoardWhiteBg = new LazyRecomputable<>(
            () -> ClientInit.laqueredBoardWhiteBg.getImageSafe().get().getMask()
                    .rectangleUV(48f / 128f, 64f / 128f, 64f / 128f, 80f / 128f)
    );

    public static final LazyRecomputable<ImageMask> leftLaqueredColorBoard = new LazyRecomputable<>(
            () -> ClientInit.laqueredBoardWhiteBg.getImageSafe().get().getMask()
                    .rectangleUV(0, 0, 1, 1)
    );
    public static final LazyRecomputable<ImageMask> rightLaqueredColorBoard = new LazyRecomputable<>(
            () -> ClientInit.laqueredBoardWhiteBg.getImageSafe().get().getMask()
                    .rectangleUV(0, 0, 1, 1)
    );

    public static final Integer
            YELLOW = 16776961,
            YELLOW2 = 16776960,
            RED = 15216648,
            BLUE = 22220,
            BLUE2 = 0x60A0B0,
            BLUE3 = 468326,
            BLUE4 = 0x2B4CA1,
            WHITE = 0xFFFFFF,
            BLACK = 789516;

    /**
     * @param pMenu
     * @param pPlayerInventory
     * @param pTitle
     */
//    public LaqueredScreen(LaqueredMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
//        super(pMenu, pPlayerInventory, pTitle);
//    }
    public LaqueredScreen(AbstractContainerScreen<EditablePanelEditMenu> editablePanelEditMenuAbstractContainerScreen, CompoundTag compoundTag) {
        super(editablePanelEditMenuAbstractContainerScreen, compoundTag);

        setBlockEntity(editablePanelEditMenuAbstractContainerScreen.getMenu().getEditablePanelEntity());
        editBar = new EditBar(0, 0, Component.empty(), "");
        // 检测是否加了jei mod，以防止打开screen时被右侧栏占用
        this.hasJei = ModList.get().isLoaded("jei");
    }


    /**
     * 初始化screen 时添加一个label 组件
     * 该init方法会在GUI最终开始显示之前调用。
     * 有一些参数(或方法)在Screen构造的时候是不存在(或不可用)的，那么若要调用这些参数，就都要写在init()方法里
     */
    @Override
    public void init() {
        Minecraft mcInstance = Minecraft.getInstance();
        if (mcInstance.screen == null) return;
        // 初始化颜色编辑器组件
        colorEditorInit();
        // 初始化按钮组件
        buttonsInit();

        Font font = mcInstance.font;
        CompoundTag nbt = getNbt();
        // 获取可编辑面板实体对象
        EditablePanelEntity entity = getScreen().getMenu().getEditablePanelEntity();
        // 获取实体的颜色并赋值给类成员变量 color
        color = entity.getColor();
        // 存储从 NBT 数据中读取的文本值
        String[] values = new String[4];
        values[0] = nbt.getString("left_top");
        values[1] = nbt.getString("left_bottom");
        values[2] = nbt.getString("right_top");
        values[3] = nbt.getString("right_bottom");

//        for (int i = 0; i < 4; i++) {
//            values[i] = nbt.getString("data" + i);
//        }
//        values
        // 原有渲染水牌图片背景的代码
//        int windowWidth = mcInstance.screen.width;
//        int windowHeight = mcInstance.screen.height;
//        ImageMask imageMask = laqueredBoardWhiteBg.get();
//        int bgWidth = (int) (windowWidth * (hasJei ? .7f : .9f));
////        onScaleChanged(((float) bgWidth / (float) imageMask.getImage().width()));
//        int bgHeight = (int) (150 * scale);
////        onPositionChanged((windowWidth * (hasJei ? .725f : 1f) - bgWidth) / 2, (float) (windowHeight - bgHeight) / 2);
//        imageMask.rectangle(new Vector3f(bgX, bgY, 0),
//                ImageMask.Axis.X, ImageMask.Axis.Y,
//                true, true, bgWidth, bgHeight);
//        imageMask.renderToGui();

//        revert = nbt.getBoolean("revert");
        innerInit(values, color, font, revert);

        cancelBtn.setOnClick((w, x, y) -> {
            AllPackets.CHANNEL.sendToServer(new DiscardChangeC2SPacket(entity.getBlockPos()));
            this.close();
        });

        confirmBtn.setOnClick((w, x, y) -> {
            BlockPos pos = entity.getBlockPos();
            nbt.putInt("color", this.color);
//            nbt.putBoolean("revert", this.revert);
            Pair<Float, Float> offset = offsetEditor.getCursorPosition();
            nbt.putFloat("offset_x", offset.getFirst());
            nbt.putFloat("offset_y", offset.getSecond());
            TransparentEditBox[] boxes = new TransparentEditBox[5];
            int counter = 0;
            for (Widget widget : getWidgets()) {
                if (widget instanceof TransparentEditBox box) {
                    boxes[counter] = box;
                    counter++;
                }
            }
            for (int i = 0; i < 4; i++) {
                // todo  bug 当使用刷子右键水牌编辑时，点击颜色确认按钮，游戏崩溃
                //  改为4元素数组防止boxes[4]空指针
                nbt.putString("data" + i, boxes[i].getValue());
            }
            CompoundTag tag = new CompoundTag();
            tag.put("data", nbt);
            entity.load(tag);
            entity.markUpdated();
            AllPackets.CHANNEL.sendToServer(new NbtC2SPacket(pos, tag));
            this.close();
        });
    }

    private void innerInit(String[] values, int color, Font font, boolean revert) {
        // 设置文本缩放因子
        float textScaleFactor = 4f;

        // 计算每个不同位置文本值对应的宽度，乘以相应系数和缩放因子，目前暂时去除了特定系数
        float size0 = ((float) Minecraft.getInstance().font.width(values[0])) * textScaleFactor; // 水牌中文文字左
        float size1 = ((float) Minecraft.getInstance().font.width(values[1])) * textScaleFactor; // 水牌英文文字左
        float size2 = ((float) Minecraft.getInstance().font.width(values[2])) * textScaleFactor; // 水牌中文文字右
        float size3 = ((float) Minecraft.getInstance().font.width(values[3])) * textScaleFactor; // 水牌英文文字右
//        float size4 = ((float) Minecraft.getInstance().font.width(values[4])) * 0.3f * textScaleFactor; // 25T
        //  检查屏幕并获取屏幕尺寸
        if (Minecraft.getInstance().screen == null) return;
        int sW = Minecraft.getInstance().screen.width;
        int sH = Minecraft.getInstance().screen.height;
        int guiScaledWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int guiScaledHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int height = font.lineHeight;

        int bgHeight = (int) (150 * scale);

        // 计算中文文字宽度（用于间距） todo 放入局标宽度
        int chineseCharWidth = (int) (bgHeight * 0.5); // 一个中文字符的宽度
// 计算布局参数
        int lineHeight = (int) (font.lineHeight * textScaleFactor); // 单行高度
        int verticalSpacing = lineHeight / 2; // 行间距等于行高
// 计算第一行宽度（values[0]和values[1] + 一个中文字符的间距）
        float firstLineWidth = size0 + chineseCharWidth + size1;
// 计算第二行宽度（values[2]和values[3] + 一个中文字符的间距）
        float secondLineWidth = size2 + chineseCharWidth + size3;
// 取两行中较大的宽度用于居中计算
        float maxLineWidth = Math.max(firstLineWidth, secondLineWidth);
        // 计算起始坐标（整体居中）
        // 动态计算基准位置（考虑窗口安全边距）
        int minMargin = 20; // 最小边距
        int baseX = (int)Math.max(minMargin, (guiScaledWidth - maxLineWidth) / 2);
        int baseY = (int)((guiScaledHeight - (lineHeight * 2 + verticalSpacing)) / 2);
//        int baseX = (int)((guiScaledWidth - maxLineWidth) / 2);
//        int baseY = (int)((guiScaledHeight - (lineHeight * 2 + verticalSpacing)) / 2);

        // 初始化界面中央偏下一排的按钮
        buttonsInnerInit(font, textScaleFactor, size0, size1, size2, size3
//                , size4
                , sW, sH);

        // 保存初始x坐标用于第二行
//        int initialX = basicX;
        // 第一行：values[0]和values[1]
        int firstLineY = baseY;
        addWidget(new TransparentEditBox(font,
                baseX,
                firstLineY,
                font.width(values[0]),
                height,
                textScaleFactor,
                textScaleFactor,
                Component.empty(),
                values[0],
                color)
        );

        // values[1]放在values[0]右侧，间隔一个values[0]的宽度
//        basicX += size0 + Minecraft.getInstance().font.width(values[0]) * textScaleFactor;
        addWidget(new TransparentEditBox(font,
                (int) (baseX + size0 + chineseCharWidth),
                firstLineY,
                font.width(values[1]),
                height,
                textScaleFactor,
                textScaleFactor,
                Component.empty(),
                values[1],
                color));

        // 第二行：values[2]和values[3]，放在第一行下方
//        basicY += height * textScaleFactor; // 下移一行
//        basicX = initialX; // 重置x坐标
// 第二行：values[2]和values[3]
        int secondLineY = baseY + lineHeight + verticalSpacing;
        addWidget(new TransparentEditBox(font,
                baseX,
                secondLineY,
                font.width(values[2]),
                height,
                textScaleFactor,
                textScaleFactor,
                Component.empty(),
                values[2],
                color));

        // values[3]放在values[2]右侧，间隔一个values[0]的宽度
//        basicX += size2 + Minecraft.getInstance().font.width(values[0]) * textScaleFactor;
        addWidget(new TransparentEditBox(font,
                (int) (baseX + size2 + chineseCharWidth),
                secondLineY,
                font.width(values[3]),
                height,
                textScaleFactor,
                textScaleFactor,
                Component.empty(),
                values[3],
                color));
    }


    public void buttonsInnerInit(Font font, float textScaleFactor, float size0,
                                 float size1, float size2, float size3
//            , float size4
            , int sW, int sH) {

        // todo 这里是否也有用于缩放文本的特定系数?
        int height = font.lineHeight;
        int labelW = (int) (size1 * 1.05f + size2 + size3 * 1.4f
//                + size4
        );
        int labelH = (int) (height * 0.18f * textScaleFactor - 23 + textScaleFactor * height * 0.13f);
        int basicX = (sW - labelW) / 2 + 20, basicY = (sH - labelH) / 2 - 10;
        titleLabel.setWidth(font.width(titleLabel.getPlainText()));
        titleLabel.setPosition((float) (sW - titleLabel.getWidth()) / 2, basicY - 20);

        int btnY = basicY + labelH + 20;
        colorEditor.getColorBtn().setPos(basicX, btnY);
        colorEditor.getTemplateBtn().setPos(basicX + 20, btnY);
        cancelBtn.setPos(basicX + labelW - 60, btnY);
        confirmBtn.setPos(basicX + labelW - 80, btnY);
//        mirrorBtn.setPos(basicX + 40, btnY);
        offsetEditor.getEditorBtn().setPos(basicX + labelW - 100, btnY);
    }


    public void buttonsInit() {
        titleLabel = new Label(Component.translatable("tooltip.kuayue.type_screen.title"));
        addWidget(titleLabel);

        mirrorBtn = new ImageButton(mirrorBtnImage, 0, 0, 16, 16, Component.empty(), b -> {
            revert = !revert;
            refresh();
        });

        offsetEditor = new OffsetEditor(0, 0, Component.literal("offset"),
                -.5f, .5f, -.5f, .5f, 0f, 0f);
        offsetEditor.setPosition((Minecraft.getInstance().screen.width - offsetEditor.getWidth()) / 2,
                (Minecraft.getInstance().screen.height - offsetEditor.getHeight()) / 2);
        offsetEditor.visible = false;

        cancelBtn = new ImageButton(cancelBtnImage, 0, 0, 16, 16, Component.empty(), b -> {
        });
        confirmBtn = new ImageButton(acceptBtnImage, 0, 0, 16, 16, Component.empty(), b -> {
        });

        editBar.onCancelClick((w, x, y) -> editBar.visible = false);
        editBar.visible = false;
        offsetEditor.onCancelBtnClick(((widget, mouseX, mouseY) -> {
            setBoardWidgetVisible(true);
            offsetEditor.visible = false;
        }));
        offsetEditor.onEditorBtnClick((widget, mouseX, mouseY) -> {
            setBoardWidgetVisible(false);
            offsetEditor.visible = true;
            offsetEditor.setCursorPosition(getNbt().getFloat("offset_x"), getNbt().getFloat("offset_y"));
        });
        offsetEditor.onAcceptBtnClick((widget, mouseX, mouseY) -> {
            setBoardWidgetVisible(true);
            Pair<Float, Float> offset = offsetEditor.getCursorPosition();
            getNbt().putFloat("offset_x", offset.getFirst());
            getNbt().putFloat("offset_y", offset.getSecond());
            getBlockEntity().saveNbt(this.getNbt());
            offsetEditor.visible = false;
        });

        addWidget(cancelBtn);
        addWidget(confirmBtn);
        addWidget(editBar);
        addWidget(mirrorBtn);
        addWidget(colorEditor.getColorBtn());
        addWidget(colorEditor.getTemplateBtn());
        addWidget(offsetEditor);
        addWidget(offsetEditor.getEditorBtn());
    }

    private void refresh() {
        if (editBar.visible) return;
        String[] values = new String[4];
        int counter = 0;
        int focus = -1;
        int focusIndex = -1;
        for (Widget w : getWidgets()) {
            if (!(w instanceof TransparentEditBox box)) continue;
            values[counter] = box.getValue();
            if (box.isFocused()) {
                focus = counter;
                focusIndex = box.getCursorPosition();
            }
            counter++;
        }
        clearWidgets();

        addWidget(cancelBtn);
        addWidget(confirmBtn);
        addWidget(titleLabel);
        addWidget(editBar);
        addWidget(mirrorBtn);
        addWidget(colorEditor);
        addWidget(colorEditor.getColorBtn());
        addWidget(colorEditor.getTemplateBtn());
        addWidget(offsetEditor.getEditorBtn());
        addWidget(offsetEditor);

        clearLabels();
        Font font = Minecraft.getInstance().font;
        CompoundTag nbt = getNbt();
        int color = getScreen().getMenu().getEditablePanelEntity().getColor();
        innerInit(values, color, font, revert);
        if (focus > -1 && focusIndex > -1) {
            Widget w = getWidgets().get(focus);
            if (!(w instanceof TransparentEditBox box)) return;
            box.setFocus(true);
            box.setCursorPosition(focusIndex);
        }
    }

    public void colorEditorInit() {
        colorEditor = new ColorScreenBundles();
        colorEditor.init();
        colorEditor.setOpen((selector, template, now) -> {
            selector.setRgb(this.color);
            setBoardWidgetVisible(false);
        });
        colorEditor.setCancel((selector, template, now) -> {
            setBoardWidgetVisible(true);
        });
        colorEditor.setSuccess((selector, template, now) -> {
            if (now == template) {
                this.color = template.getChosenBox().getTemplate().getColor();
                this.setTextColor(color);
                setBoardWidgetVisible(true);
            } else {
                this.color = selector.getColor().getRGB();
                setTextColor(color);
                setBoardWidgetVisible(true);
            }
        });
        colorEditor.visible = false;
        addWidget(colorEditor);
    }

    /**
     * 渲染背景
     * (一般来说，是GUI从开始显示到最终关闭都会存在的静态渲染对象)
     *
     * @param pose
     * @param mouseX
     * @param mouseY
     * @param partialTick
     **/
    @Override
    public void renderBackGround(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        Minecraft instance = Minecraft.getInstance();
        if (instance.screen == null) return;
        int sW = instance.screen.width;
        int sH = instance.screen.height;
// 获取窗口的 GUI 缩放尺寸（更准确，考虑 UI 缩放）
        Window window = instance.getWindow();
        int guiScaledWidth = window.getGuiScaledWidth();
        int guiScaledHeight = window.getGuiScaledHeight();

        // 获取 ImageMask 背景
        ImageMask imageMask = laqueredBoardWhiteBg.get();
        ImageMask logoIm = laqueredBoardLogo.get();
        ImageMask leftColorBoard = leftLaqueredColorBoard.get();
        ImageMask rightColorBoard = rightLaqueredColorBoard.get();
        // 计算背景尺寸（宽度和高度）
//        int bgWidth = (int) (windowWidth * (hasJei ? .7f : .9f));
//        int bgHeight = (int) (150 * scale);
        int bgWidth = (int) (guiScaledWidth * (hasJei ? 0.7f : 0.9f)); // 动态宽度（70% 或 90% 窗口宽度）
        int bgHeight = (int) (150 * scale); // 固定高度或按需调整
        // 计算背景的起始坐标 (bgX, bgY)，使其与 values[0] 对齐
        int bgX = (guiScaledWidth  - bgWidth) / 2;  // 与 values[0] 的 basicX 计算方式一致
        int bgY = (guiScaledHeight  - bgHeight) / 2;  // 与 values[0] 的 basicY 计算方式一致
        // 确保背景的 x 和 y 原点与 values[0] 对齐
        imageMask.rectangle(
                new Vector3f(bgX, bgY, 0),  // 使用与 values[0] 相同的原点
                ImageMask.Axis.X,
                ImageMask.Axis.Y,
                true,
                true,
                bgWidth,
                bgHeight
        );
        double logoWidthHalf = bgHeight * 0.5;
        logoIm.rectangle(
                new Vector3f((guiScaledWidth / 2) - (float)(logoWidthHalf / 2), (guiScaledHeight / 2) - 5, 1),  // 使用与 values[0] 相同的原点
                ImageMask.Axis.X,
                ImageMask.Axis.Y,
                true,
                true,
                (float) logoWidthHalf,
                (float) logoWidthHalf
        );
        leftColorBoard.rectangle(
                new Vector3f(bgX, bgY + 200, 1),  // 使用与 values[0] 相同的原点
                ImageMask.Axis.X,
                ImageMask.Axis.Y,
                true,
                true,
                (float) (bgWidth * 0.35),
                (float) (bgHeight * 0.15)
        );
        leftColorBoard .setColor(SimpleColor.BLACK);
        rightColorBoard.rectangle(
                new Vector3f(bgX - 400, bgY + 200, 2),  // 使用与 values[0] 相同的原点
                ImageMask.Axis.X,
                ImageMask.Axis.Y,
                true,
                true,
                (float) (bgWidth * 0.35),
                (float) (bgHeight * 0.15)
        );
        rightColorBoard.setColor(SimpleColor.fromRGB(0.5f, 0.6f, 0.7f));
        // 渲染背景
        imageMask.renderToGui();
        leftColorBoard.renderToGui();
        rightColorBoard.renderToGui();
        logoIm.renderToGui();
        // 添加半透明黑色背景覆盖整个屏幕
        GuiComponent.fill(pose, 0, 0, sW, sH, 0x80000000);
    }

    /**
     * Screen的核心渲染方法，用于控制其他所有渲染方法的工作
     *
     * @param pose
     * @param mouseX
     * @param mouseY
     * @param partial
     */
    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partial) {
        super.render(pose, mouseX, mouseY, partial);

        if (offsetEditor == null) return;
    }


//    @Override
//    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
//
//    }


    @Override
    public void mouseClicked(double mouseX, double mouseY, int btn) {
        for (Widget widget : getWidgets()) {
            if (!(widget instanceof GuiEventListener listener)) continue;
            if (!listener.isMouseOver(mouseX, mouseY)) continue;
            if (listener instanceof AbstractWidget widget1 && !widget1.visible) continue;
            if (listener instanceof ColorScreen cs && !cs.getVisible()) continue;
            if (listener instanceof GetShareTemplateScreen screen && !screen.isVisible()) continue;
            if (widget instanceof TransparentEditBox box) {
                editBar.setPosition(box.x + ((int) ((float) box.getWidth() * box.getScaleX()) - 200) / 2,
                        box.y + (int) ((float) box.getHeight() * box.getScaleY()) + 2);
                editBar.setText(box.getValue());
                editBar.onAcceptClick(
                        (w, x, y) -> {
                            box.setValue(editBar.getText());
                            editBar.visible = false;
                            refresh();
                            getBlockEntity().saveNbt(this.getNbt());
                        }
                );
                editBar.visible = true;
                editBar.setFocused(true);
                return;
            }
            listener.mouseClicked(mouseX, mouseY, btn);
            return;
        }
    }

    @Override
    public void renderTooltip(PoseStack pose, int mouseX, int mouseY) {
    }

    /**
     * 该screen 关闭时调用
     */
//    @Override
//    public void onClose() {
//        super.onClose();
//    }
    @Override
    public void charTyped(char code, int modifier) {
        super.charTyped(code, modifier);
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * 这个方法是AbstractContainerScreen特有的，默认情况下会在固定位置渲染"物品栏"("Inventory")几个字
     * 如果不想要这几个字的话就把他里面的super调用注释掉
     *
     * @param pPoseStack
     * @param pMouseX
     * @param pMouseY
     */
//    @Override
//    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
////        super.renderLabels(pPoseStack, pMouseX, pMouseY);
//    }
    public void setButtonsVisible(boolean visible) {
        colorEditor.getTemplateBtn().visible = visible;
        colorEditor.getColorBtn().visible = visible;
        this.cancelBtn.visible = visible;
        this.confirmBtn.visible = visible;
        this.titleLabel.visible = visible;
        this.mirrorBtn.visible = visible;
        editBar.visible = false;
        this.offsetEditor.getEditorBtn().visible = visible;
        editBar.setFocused(false);
    }

    public void setBoardWidgetVisible(boolean visible) {
        getWidgets().forEach(w -> {
            if (w instanceof TransparentEditBox box) box.visible = visible;
        });
        setButtonsVisible(visible);
    }

    public void setTextColor(int color) {
        getWidgets().forEach(w -> {
            if (w instanceof TransparentEditBox box) box.setTextColor(color);
        });
    }
}
