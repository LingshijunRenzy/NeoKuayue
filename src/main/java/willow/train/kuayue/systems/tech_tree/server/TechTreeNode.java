package willow.train.kuayue.systems.tech_tree.server;

import kasuga.lib.core.util.data_type.Pair;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import willow.train.kuayue.systems.tech_tree.NodeLocation;
import willow.train.kuayue.systems.tech_tree.NodeType;
import willow.train.kuayue.systems.tech_tree.json.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Getter
public class TechTreeNode {

    public final TechTreeNodeData data;

    public final TechTreeGroup group;

    private final ArrayList<TechTreeNode> prev;

    private final ArrayList<TechTreeNode> next;

    private final ArrayList<TechTreeGroup> nextGroups;

    public TechTreeNode(TechTreeGroup group, TechTreeNodeData data) {
        this.data = data;
        this.group = group;
        this.prev = new ArrayList<>();
        this.next = new ArrayList<>();
        this.nextGroups = new ArrayList<>();
    }

    public void compileConnections() {
        for (NodeLocation next : data.getNextNodes()) {
            TechTreeNode node = TechTreeManager.MANAGER.getNode(next);
            if (node == null) continue;
            this.next.add(node);
            node.prev.add(this);
        }
        for (ResourceLocation next : data.getNextGroups()) {
            TechTreeGroup g = TechTreeManager.MANAGER.getGroup(next);
            if (g == null) continue;
            this.nextGroups.add(g);
        }
    }

    public Set<ItemStack> getItemConsume() {
        return data.getItemConsume();
    }

    public Set<ItemStack> getBlueprints() {
        return data.getBlueprint();
    }

    public Set<ItemStack> getItemReward() {
        return data.getItemRewards();
    }

    public Pair<Integer, Integer> getExpAndLevel() {
        return Pair.of(data.getExp(), data.getLevel());
    }

    public NodeLocation getLocation() {
        return data.getLocation();
    }

    public boolean isHide() {
        return data.isHide();
    }

    public HideContext getHideContext() {
        return data.getHide();
    }

    @Nullable
    public OnUnlockContext getUnlockContext() {
        return data.getUnlock();
    }

    public void addPrev(TechTreeNode node) {
        prev.add(node);
    }

    public void addNext(TechTreeNode node) {
        next.add(node);
    }

    public void addNextGroup(TechTreeGroup group) {
        nextGroups.add(group);
    }

    public NodeType getType() {
        return data.getType();
    }

    public boolean is(NodeLocation location) {
        return location.equals(data.getLocation());
    }

    public boolean is(String location) {
        return new NodeLocation(group.getNamespace(), group.getIdentifier(), location).equals(data.getLocation());
    }

    public int getDegree() {
        return next.size() + prev.size();
    }

    public int getInDegree() {
        return prev.size();
    }

    public int getOutDegree() {
        return next.size();
    }

    public Set<TechTreeNode> getNearBys() {
        HashSet<TechTreeNode> near = new HashSet<>();
        near.addAll(next);
        near.addAll(prev);
        return near;
    }

    public void toNetwork(FriendlyByteBuf buf) {
        // meta data
        getLocation().writeToByteBuf(buf);
        getType().writeToByteBuf(buf);
        buf.writeUtf(getData().getName());
        buf.writeUtf(getData().getDescription());
        if (getData().getUnlock() == null)
            buf.writeUtf("");
        else
            buf.writeUtf(getData().getUnlock().description);

        buf.writeInt(getData().getLevel());
        buf.writeInt(getData().getExp());
        buf.writeItemStack(data.getLogo(), false);

        // item consume
        Set<ItemStack> consume = getItemConsume();
        buf.writeInt(consume.size());
        consume.forEach(stack -> buf.writeItemStack(stack, false));

        // next nodes
        buf.writeInt(next.size());
        next.forEach(node -> node.getLocation().writeToByteBuf(buf));

        // next groups
        buf.writeInt(nextGroups.size());
        nextGroups.forEach(grp -> buf.writeResourceLocation(grp.getId()));

        // prev nodes
        buf.writeInt(prev.size());
        prev.forEach(node -> node.getLocation().writeToByteBuf(buf));
    }

    public @Nullable UnlockCondition getUnlockCondition() {
        return getData().getUnlockCondition();
    }

    public void removeNext(TechTreeNode node) {
        this.next.remove(node);
    }

    public void removePrev(TechTreeNode node) {
        this.prev.remove(node);
    }
}
