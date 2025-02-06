package willow.train.kuayue.systems.tech_tree.client.gui;

import kasuga.lib.core.util.data_type.Vec2i;
import net.minecraft.core.BlockPos;

public class Vec2iE extends Vec2i {

    public Vec2iE(int x, int y) {
        super(x, y);
    }

    public Vec2iE() {
        super();
    }

    public Vec2iE(Vec2iE vec2iE) {
        super(vec2iE.x, vec2iE.y);
    }

    public Vec2iE(Vec2i vec2i) {
        super(vec2i.x, vec2i.y);
    }

    public Vec2iE add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vec2iE add(Vec2i vec) {
        this.x += vec.x;
        this.y += vec.y;
        return this;
    }

    public Vec2iE subtract(int x, int y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vec2iE subtract(Vec2i vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        return this;
    }

    public Vec2iE multiply(int x, int y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    public Vec2iE multiply(Vec2i vec) {
        this.x *= vec.x;
        this.y *= vec.y;
        return this;
    }

    public int cross(Vec2i vec) {
        return x * vec.y - vec.x * y;
    }

    public int lenSqr() {
        return x * x + y * y;
    }

    public float len() {
        return (float) Math.sqrt(lenSqr());
    }

    public int manhattanDistance(Vec2i vec) {
        return Math.abs(x - vec.x) + Math.abs(y - vec.y);
    }

    public boolean nextTo(Vec2i vec) {
        return manhattanDistance(vec) == 1;
    }

    public int distanceSqr(Vec2i vec) {
        int dx = Math.abs(x - vec.x);
        int dy = Math.abs(y - vec.y);
        return dx * dx + dy * dy;
    }

    public float distance(Vec2i vec) {
        return (float) Math.sqrt((float) distanceSqr(vec));
    }

    public Vec2iE copy() {
        return new Vec2iE(this);
    }

    public Vec2i toVec2i() {
        return new Vec2i(this.x, this.y);
    }

    @Override
    public int hashCode() {
        return toVec2i().x << 31 + toVec2i().y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vec2iE vec2iE)) return false;
        return vec2iE.x == this.x && vec2iE.y == this.y;
    }
}
