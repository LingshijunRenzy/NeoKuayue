package willow.train.kuayue.systems.device.driver.seat;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import kasuga.lib.core.menu.base.GuiMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

public interface InteractiveBehaviour {
    public List<MenuEntry> getMenusOf(MovementContext context);

    public class MenuEntry {
        protected final Component _displayName;
        protected final ResourceLocation _icon;
        protected final Supplier<Boolean> _isAvailable;
        protected final Supplier<GuiMenu> _menuSupplier;
        protected final Vec2 size;
        private Vec2 position;
        private Vec2 scale;
        private AnchorPoint screenAnchor = AnchorPoint.TOP_LEFT;
        private AnchorPoint selfAnchor = AnchorPoint.TOP_LEFT;
        private boolean fixed = false;

        public MenuEntry(
                Component displayName,
                ResourceLocation icon,
                Supplier<Boolean> isAvailable,
                Supplier<GuiMenu> menuSupplier,
                Vec2 defaultPosition,
                Vec2 defaultScale,
                Vec2 size
        ) {
            this._displayName = displayName;
            this._icon = icon;
            this._isAvailable = isAvailable;
            this._menuSupplier = menuSupplier;
            this.position = defaultPosition;
            this.scale = defaultScale;
            this.size = size;
        }

        public Supplier<Boolean> isAvailable() {
            return _isAvailable;
        }

        public Supplier<GuiMenu> menuSupplier() {
            return _menuSupplier;
        }

        public void setPosition(Vec2 position) {
            this.position = position;
        }

        public void setScale(Vec2 scale) {
            this.scale = scale;
        }

        public Vec2 getPosition() {
            return position;
        }

        public Vec2 getScale() {
            return scale;
        }

        public Vec2 getSize() {
            return size;
        }

        public void setScreenAnchor(AnchorPoint anchor) {
            this.screenAnchor = anchor;
            this.selfAnchor = anchor;
        }

        public void setSelfAnchor(AnchorPoint anchor) {
            this.selfAnchor = anchor;
        }

        public AnchorPoint getScreenAnchor() {
            return screenAnchor;
        }

        public AnchorPoint getSelfAnchor() {
            return selfAnchor;
        }

        protected boolean dragging;
        public void setDragging(boolean b) {
            dragging = b;
        }

        public boolean isDragging() {
            return dragging;
        }

        public void setFixed(boolean fixed) {
            this.fixed = fixed;
        }

        public boolean isFixed() {
            return fixed;
        }
    }
}
