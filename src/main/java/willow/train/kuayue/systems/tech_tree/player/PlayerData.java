package willow.train.kuayue.systems.tech_tree.player;

import kasuga.lib.core.base.NbtSerializable;
import kasuga.lib.core.util.data_type.Pair;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.mixins.mixin.AccessorPlayerAdvancement;
import willow.train.kuayue.systems.tech_tree.NodeLocation;
import willow.train.kuayue.systems.tech_tree.json.HideContext;
import willow.train.kuayue.systems.tech_tree.json.OnUnlockContext;
import willow.train.kuayue.systems.tech_tree.json.UnlockCondition;
import willow.train.kuayue.systems.tech_tree.server.TechTree;
import willow.train.kuayue.systems.tech_tree.server.TechTreeGroup;
import willow.train.kuayue.systems.tech_tree.server.TechTreeManager;
import willow.train.kuayue.systems.tech_tree.server.TechTreeNode;

import javax.annotation.Nullable;
import java.util.*;

public class PlayerData implements NbtSerializable {
    public final UUID playerID;
    public final Set<NodeLocation> unlocked;
    public final Set<ResourceLocation> unlockedGroups;
    public final Set<ResourceLocation> visibleGroups;
    public final Set<NodeLocation> visibleNodes;

    public PlayerData(UUID uuid) {
        this.playerID = uuid;
        unlocked = new HashSet<>();
        visibleGroups = new HashSet<>();
        unlockedGroups = new HashSet<>();
        visibleNodes = new HashSet<>();
    }

    public PlayerData(Player player) {
        this(player.getUUID());
    }

    public void read(CompoundTag nbt) {
        ListTag nodes = nbt.getList("nodes", Tag.TAG_STRING);
        unlocked.clear();
        nodes.forEach(tag -> unlocked.add(new NodeLocation(tag.getAsString())));

        ListTag uGrp = nbt.getList("unlock_groups", Tag.TAG_STRING);
        unlockedGroups.clear();
        uGrp.forEach(tag -> unlockedGroups.add(new ResourceLocation(tag.getAsString())));

        ListTag vGrp = nbt.getList("visible_groups", Tag.TAG_STRING);
        visibleGroups.clear();
        vGrp.forEach(tag -> visibleGroups.add(new ResourceLocation(tag.getAsString())));

        ListTag vNode = nbt.getList("visible_nodes", Tag.TAG_STRING);
        visibleNodes.clear();;
        vNode.forEach(node -> visibleNodes.add(new NodeLocation(node.getAsString())));
    }

    public void write(CompoundTag nbt) {
        ListTag nodes = new ListTag();
        unlocked.forEach(loc -> nodes.add(StringTag.valueOf(loc.toString())));
        nbt.put("nodes", nodes);

        ListTag uGrp = new ListTag();
        unlockedGroups.forEach(grp -> uGrp.add(StringTag.valueOf(grp.toString())));
        nbt.put("unlock_groups", uGrp);

        ListTag vGrp = new ListTag();
        visibleGroups.forEach(grp -> vGrp.add(StringTag.valueOf(grp.toString())));
        nbt.put("visible_groups", vGrp);

        ListTag vNode = new ListTag();
        visibleNodes.forEach(node -> vNode.add(StringTag.valueOf(node.toString())));
        nbt.put("visible_nodes", vNode);
    }

    public CheckReason canUnlock(ServerPlayer player, TechTreeGroup group) {
        if (player.isCreative()) {
            return CheckReason.creativeSuccess(group.getRoot().getItemReward());
        }
        boolean seenFlag = canBeSeen(player, group.getHideContext());
        boolean groupUnlocked = unlockedGroups.contains(group.getId());
        Pair<Boolean, Collection<NodeLocation>> requiredNodes = checkNodes(group);
        boolean flag = seenFlag && requiredNodes.getFirst();
        return new CheckReason(flag, seenFlag, groupUnlocked,
                true, List.of(), requiredNodes.getSecond(),
                flag ? group.getUnlockContext().getReward() : List.of(), "");
    }

    public Collection<NodeLocation> getRequiredNodes(TechTreeNode node) {
        HashSet<NodeLocation> nodes = new HashSet<>();
        if (node.getUnlockCondition() != null) {
            UnlockCondition unlockCondition = node.getUnlockCondition();
            return unlockCondition.checkNode(node, this);
        }
        node.getPrev().forEach(prev -> nodes.add(prev.getLocation()));
        nodes.removeIf(unlocked::contains);
        return nodes;
    }

    public Collection<NodeLocation> getRequiredNodes(TechTreeGroup group) {
        HashSet<NodeLocation> nodes = new HashSet<>();
        if (group.getUnlockCondition() != null) {
            UnlockCondition unlockCondition = group.getUnlockCondition();
            return unlockCondition.checkGroup(group, this);
        }
        group.getPrev().forEach(prev -> nodes.add(prev.getLocation()));
        nodes.removeIf(unlocked::contains);
        return nodes;
    }

    public Pair<Boolean, Collection<NodeLocation>> checkNodes(TechTreeNode node) {
        Collection<NodeLocation> requiredNodes = getRequiredNodes(node);
        return Pair.of(requiredNodes.isEmpty(), requiredNodes);
    }

    public Pair<Boolean, Collection<NodeLocation>> checkNodes(TechTreeGroup group) {
        Collection<NodeLocation> requiredNodes = getRequiredNodes(group);
        return Pair.of(requiredNodes.isEmpty(), requiredNodes);
    }

    public CheckReason canUnlock(ServerPlayer player, TechTreeNode node) {
        Set<ItemStack> item = new HashSet<>();
        if (node.getData() != null) {
            Set<ItemStack> blueprintStacks = node.getBlueprints();
            blueprintStacks.forEach(stack -> {
                if (stack == null || stack.equals(ItemStack.EMPTY)) return;
                stack.getOrCreateTag().putString("node", node.getLocation().toString());
            });
            item.addAll(blueprintStacks);
            item.addAll(node.getData().getItemRewards());
        }
        if (player.isCreative()) {
            return CheckReason.creativeSuccess(item);
        }
        boolean seenFLag = canBeSeen(player, node.getHideContext());
        boolean groupUnlockedFlag = unlockedGroups.contains(node.group.getId());
        boolean expFlag = checkExpAndLevel(player, node.getExpAndLevel());
        Pair<Boolean, Collection<ItemStack>> requiredItems = checkItems(player.getInventory(), node.getItemConsume());
        Pair<Boolean, Collection<NodeLocation>> requiredNodes = checkNodes(node);
        boolean flag = seenFLag && groupUnlockedFlag && expFlag && requiredNodes.getFirst() && requiredItems.getFirst();
        return new CheckReason(flag, seenFLag, groupUnlockedFlag, expFlag,
                requiredItems.getSecond(), requiredNodes.getSecond(), flag ? item : Set.of(), "");
    }

    public UnlockResult unlock(ServerLevel level, ServerPlayer player, TechTreeNode node) {
        CheckReason reason = canUnlock(player, node);
        if (!reason.flag) return UnlockResult.failedEmpty();
        ArrayList<Pair<Integer, Integer>> itemGrow = new ArrayList<>();
        HashMap<ResourceLocation, Collection<String>> advResultHolder = new HashMap<>();
        int expConsume = consumeExpAndLevel(player, node.getExpAndLevel());
        consumePlayerItem(player, node.getItemConsume(), itemGrow);
        Collection<NodeLocation> neoUnlockNodes = new HashSet<>(),
                                neoVisibleNodes = new HashSet<>();
        Collection<ResourceLocation> neoUnlockGroups = new HashSet<>(),
                                    neoVisibleGroups = new HashSet<>();
        forceUnlock(level, player, node, advResultHolder,
                neoUnlockNodes, neoVisibleNodes, neoUnlockGroups, neoVisibleGroups);
        return new UnlockResult(true, itemGrow, expConsume, advResultHolder,
                neoUnlockNodes, neoVisibleNodes, neoUnlockGroups, neoVisibleGroups);
    }

    public void forceUnlock(ServerLevel level, ServerPlayer player, TechTreeNode node,
                            HashMap<ResourceLocation, Collection<String>> advResultHolder,
                            Collection<NodeLocation> neoUnlockNodes, Collection<NodeLocation> neoVisibleNodes,
                            Collection<ResourceLocation> neoUnlockGroups, Collection<ResourceLocation> neoVisibleGroups) {
        unlocked.add(node.getLocation());
        neoUnlockNodes.add(node.getLocation());
        if (player.isCreative() && !unlockedGroups.contains(node.group.getId())) {
            forceUnlock(level, player, node.group, advResultHolder, neoUnlockNodes,
                    neoVisibleNodes, neoUnlockGroups, neoVisibleGroups);
        }
        if (!visibleNodes.contains(node.getLocation())) {
            visibleNodes.add(node.getLocation());
            neoVisibleNodes.add(node.getLocation());
        }
        rewardPlayer(level, player, node.getUnlockContext(), node.getGroup().getTree(), advResultHolder,
                neoUnlockNodes, neoVisibleNodes, neoUnlockGroups, neoVisibleGroups);
        Set<ItemStack> blueprintStacks = node.getBlueprints();
        blueprintStacks.forEach(stack -> {
            if (stack == null || stack.equals(ItemStack.EMPTY)) return;
            stack.getOrCreateTag().putString("node", node.getLocation().toString());
        });
        givePlayerItem(player, level, blueprintStacks);
        givePlayerItem(player, level, node.getItemReward());
        checkVisible(node.group.getTree(), player, neoVisibleNodes, neoVisibleGroups);
    }

    public UnlockResult unlock(ServerLevel level, ServerPlayer player, TechTreeGroup group) {
        CheckReason reason = canUnlock(player, group);
        if (!reason.flag) return UnlockResult.failedEmpty();
        HashMap<ResourceLocation, Collection<String>> advResultHolder = new HashMap<>();
        Collection<NodeLocation> neoUnlockNodes = new HashSet<>(),
                neoVisibleNodes = new HashSet<>();
        Collection<ResourceLocation> neoUnlockGroups = new HashSet<>(),
                neoVisibleGroups = new HashSet<>();
        forceUnlock(level, player, group, advResultHolder,
                neoUnlockNodes, neoVisibleNodes, neoUnlockGroups, neoVisibleGroups);
        return new UnlockResult(true, List.of(), 0, advResultHolder,
                neoUnlockNodes, neoVisibleNodes, neoUnlockGroups, neoVisibleGroups);
    }

    public void forceUnlock(ServerLevel level, ServerPlayer player, TechTreeGroup group,
                            HashMap<ResourceLocation, Collection<String>> advResultHolder,
                            Collection<NodeLocation> neoUnlockNodes, Collection<NodeLocation> neoVisibleNodes,
                            Collection<ResourceLocation> neoUnlockGroups, Collection<ResourceLocation> neoVisibleGroups) {
        unlockedGroups.add(group.getId());
        neoUnlockGroups.add(group.getId());
        if (!visibleGroups.contains(group.getId())) {
            visibleGroups.add(group.getId());
            neoVisibleGroups.add(group.getId());
        }
        rewardPlayer(level, player, group.getUnlockContext(), group.tree, advResultHolder,
                neoUnlockNodes, neoVisibleNodes, neoUnlockGroups, neoVisibleGroups);
        checkVisible(group.tree, player, neoVisibleNodes, neoVisibleGroups);
    }

    public boolean checkExpAndLevel(ServerPlayer player, Pair<Integer, Integer> expAndLevel) {
        if (player.isCreative()) return true;
        int level = player.experienceLevel;
        int exp = player.totalExperience;
        return level >= expAndLevel.getSecond() && exp >= expAndLevel.getFirst();
    }

    public static int consumeExpAndLevel(ServerPlayer player, Pair<Integer, Integer> expAndLevel) {
        int expConsume = expAndLevel.getFirst();
        int level;
        int exp = player.totalExperience - expConsume;
        if (exp < 0) {
            level = 0;
            exp = 0;
        } else {
            level = expToLevel(exp);
        }
        int levelChange = level - player.experienceLevel;

        // from Player#giveExperienceLevels(int pLevels)
        net.minecraftforge.event.entity.player.PlayerXpEvent.LevelChange event = new net.minecraftforge.event.entity.player.PlayerXpEvent.LevelChange(player, levelChange);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);

        player.experienceLevel = level;
        player.totalExperience = exp;
        return expConsume;
    }

    public void rewardPlayer(ServerLevel level, ServerPlayer player, @Nullable OnUnlockContext context, TechTree tree,
                             HashMap<ResourceLocation, Collection<String>> advResultHolder,
                             Collection<NodeLocation> neoUnlockNodes, Collection<NodeLocation> neoVisibleNodes,
                             Collection<ResourceLocation> neoUnlockGroups, Collection<ResourceLocation> neoVisibleGroups) {
        if (context == null) return;

        // items
        Set<ItemStack> reward = context.getReward();
        givePlayerItem(player, level, reward);

        //collect advancements
        Set<Pair<Advancement, Collection<String>>> rewardAdvancement = new HashSet<>();
        HashMap<ResourceLocation, Collection<String>> advancements = context.getUnlockAdvancements();
        for (Map.Entry<ResourceLocation, Collection<String>> entry : advancements.entrySet()) {
            for (Advancement advancement : PlayerDataManager.MANAGER.getAdvancements()) {
                if (advancement.getId().equals(entry.getKey())) {
                    rewardAdvancement.add(Pair.of(advancement, entry.getValue()));
                    break;
                }
            }
        }

        // give all advancements
        rewardAdvancement.forEach(adv -> {
            Advancement advancement = adv.getFirst();
            Collection<String> criteria = adv.getSecond();
            if (criteria.isEmpty()) {
                advancement.getCriteria().forEach((s, criterion) ->
                        player.getAdvancements().award(advancement, s)
                );
                advResultHolder.put(advancement.getId(), advancement.getCriteria().keySet());
                return;
            }
            criteria.forEach(str -> {
                advResultHolder.put(advancement.getId(), new HashSet<>());
                if (advancement.getCriteria().containsKey(str)) {
                    player.getAdvancements().award(advancement, str);
                    advResultHolder.get(advancement.getId()).add(str);
                }
            });
        });

        // unlock nodes;
        for (NodeLocation location : context.getUnlockNodes()) {
            TechTreeNode node = tree.getNodes().getOrDefault(location, null);
            if (node == null) continue;
            forceUnlock(level, player, node, advResultHolder,
                    neoUnlockNodes, neoVisibleNodes, neoUnlockGroups, neoVisibleGroups);
        }
        unlocked.addAll(Arrays.asList(context.getUnlockNodes()));
    }

    public static void givePlayerItem(Player player, ServerLevel level, Set<ItemStack> items) {
        items.forEach(item -> {
            if (player.addItem(item)) return;
            Vec3 eyePos = player.getEyePosition();
            // drop item;
            level.addFreshEntity(new ItemEntity(level, eyePos.x(), eyePos.y(), eyePos.z(), item));
        });
    }

    public static boolean consumePlayerItem(Player player, Set<ItemStack> items, Collection<Pair<Integer, Integer>> resultHolder) {
        if (player.isCreative()) return true;
        Inventory inventory = player.getInventory();
        HashSet<ItemStack> result = new HashSet<>();
        items.forEach(item -> {
            Item type = item.getItem();
            boolean flag = item.hasTag();

            // items that has nbt
            if (flag) {
                for (int i = 0; i < 36; i++) {
                    ItemStack stack = inventory.getItem(i);
                    if (stack.getItem().equals(type) &&
                        stack.hasTag() && item.getTag().equals(stack.getTag())
                    ) {
                        resultHolder.add(Pair.of(i, -1));
                        inventory.setItem(i, ItemStack.EMPTY);
                        result.add(item);
                        return;
                    }
                }
            }

            // items that without nbt
            int count = item.getCount();
            for (int i = 0; i < 36; i++) {
                ItemStack stack = inventory.getItem(i);
                if (!stack.getItem().equals(type)) continue;
                if (stack.getCount() > count) {
                    resultHolder.add(Pair.of(i, -count));
                    stack.setCount(stack.getCount() - count);
                    result.add(item);
                    return;
                } else if (stack.getCount() == count) {
                    resultHolder.add(Pair.of(i, -count));
                    inventory.setItem(i, ItemStack.EMPTY);
                    result.add(item);
                    return;
                } else {
                    resultHolder.add(Pair.of(i, -count));
                    inventory.setItem(i, ItemStack.EMPTY);
                    count -= stack.getCount();
                }
            }
        });
        return result.size() == items.size();
    }

    public Pair<Boolean, Collection<ItemStack>> checkItems(Inventory inventory, Collection<ItemStack> items) {
        Collection<ItemStack> result = getRequiredItems(inventory, items);
        return Pair.of(result.isEmpty(), result);
    }

    public Collection<ItemStack> getRequiredItems(Inventory inventory,
                                                  Collection<ItemStack> items) {
        Collection<ItemStack> collection = new HashSet<>(items);
        collection.removeIf(stack -> {
            int count = stack.getCount();
            for (int i = 0; i < 36; i++) {
                ItemStack item = inventory.getItem(i);
                if (!item.getItem().equals(stack.getItem())) continue;
                if (count <= 0) return true;
                if (stack.hasTag() && item.hasTag()) {
                    if (stack.getTag().equals(item.getTag())) count --;
                    continue;
                }
                if (stack.hasTag() || item.hasTag()) continue;
                count -= item.getCount();
            }
            return count <= 0;
        });
        return collection;
    }

    public boolean canBeSeen(ServerPlayer player, @Nullable HideContext hide) {
        if (hide == null) return true;
        // check namespaces;
        HashSet<String> needNamespaces = new HashSet<>(Arrays.asList(hide.getNeedNamespaces()));
        TechTreeManager.MANAGER.getNamespaces().forEach(needNamespaces::remove);
        if (!needNamespaces.isEmpty()) return false;

        // check advancements
        PlayerAdvancements adv = player.getAdvancements();
        Map<Advancement, AdvancementProgress> allAdvancements = ((AccessorPlayerAdvancement) adv).getAdvancements();
        Set<ResourceLocation> needAdvancements = new HashSet<>(Arrays.asList(hide.getNeedAdvancements()));
        allAdvancements.forEach((a, ap) -> {
            if (needAdvancements.contains(a.getId()) && ap.isDone()) needAdvancements.remove(a.getId());
        });
        if (!needAdvancements.isEmpty()) return false;

        // check nodes;
        HashSet<NodeLocation> needNodes = new HashSet<>(Arrays.asList(hide.getNeedNodes()));
        unlocked.forEach(needNodes::remove);
        return needNodes.isEmpty();
    }

    public static int levelToExp(int level) {
        if (level < 17) {
            return level * level + 6 * level;
        } else if (level < 32) {
            return (int) (level * level * 2.5f - 40.5f * level + 360);
        } else {
            return (int) (level * level * 4.5f - 162.5 * level + 2220);
        }
    }

    public static int expToLevel(int exp) {
        int level17 = levelToExp(17);
        int level32 = levelToExp(32);
        if (exp >= level32) {
            return (int) quadraticYtoX(4.5, -162.5, 2220, exp, true);
        }
        if (exp >= level17) {
            return (int) quadraticYtoX(2.5, -40.5, 360, exp, true);
        }
        return (int) quadraticYtoX(1, 6, 0, exp, true);
    }

    public static Pair<Double, Double> quadraticYtoX(double A, double B, double C, double y) {
        double q = B * B - 4 * A * (C - y);
        assert q >= 0;
        return Pair.of((-B - Math.sqrt(q)) / (2 * A), (-B + Math.sqrt(q)) / (2 * A));
    }


    public static double quadraticYtoX(double A, double B, double C, double y, boolean right) {
        Pair<Double, Double> pair = quadraticYtoX(A, B, C, y);
        return right ? pair.getSecond() : pair.getFirst();
    }

    public void checkVisible(TechTree tree, ServerPlayer player,
                             Collection<NodeLocation> neoVisibleNodes,
                             Collection<ResourceLocation> neoVisibleGroups) {
        tree.getGroups().forEach(
                (location, grp) -> {
                    if (grp.isHide() && !canBeSeen(player, grp.getHideContext())) return;
                    if (!checkNodes(grp).getFirst()) return;
                    if (neoVisibleGroups != null &&
                            !visibleGroups.contains(grp.getId()))
                        neoVisibleGroups.add(grp.getId());
                    this.visibleGroups.add(grp.getId());
                }
        );
        tree.getNodes().forEach(
                (location, node) -> {
                    if (node.isHide() && !canBeSeen(player, node.getHideContext())) return;
                    if (!checkNodes(node).getFirst()) return;
                    if (neoVisibleNodes != null &&
                            !visibleNodes.contains(node.getLocation()))
                        neoVisibleNodes.add(node.getLocation());
                    this.visibleNodes.add(node.getLocation());
                }
        );
    }

    public void checkAllDefaults() {
        TechTreeManager.MANAGER.trees().forEach((treeName, tree) -> {
            tree.getGroups().forEach((grpName, grp) -> {
                if (!grp.hasRing() && grp.getData().isInitialVisibility()) {
                    visibleGroups.add(grp.getId());
                    unlockedGroups.add(grp.getId());
                    visibleNodes.add(grp.getRoot().getLocation());
                }
            });
        });
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerID);
        buf.writeInt(unlocked.size());
        unlocked.forEach(node -> {
            node.writeToByteBuf(buf);
        });

        buf.writeInt(unlockedGroups.size());
        unlockedGroups.forEach(buf::writeResourceLocation);

        buf.writeInt(visibleGroups.size());
        visibleGroups.forEach(buf::writeResourceLocation);

        buf.writeInt(visibleNodes.size());
        visibleNodes.forEach(node -> node.writeToByteBuf(buf));
    }


    public void fromNetwork(FriendlyByteBuf buf) {
        int unlockSize = buf.readInt();
        for (int i = 0; i < unlockSize; i++) {
            this.unlocked.add(NodeLocation.readFromByteBuf(buf));
        }

        int unlockGroupSize = buf.readInt();
        for (int i = 0; i < unlockGroupSize; i++) {
            this.unlockedGroups.add(buf.readResourceLocation());
        }

        int visibleGroupSize = buf.readInt();
        for (int i = 0; i < visibleGroupSize; i++) {
            this.visibleGroups.add(buf.readResourceLocation());
        }

        int visibleNodeSize = buf.readInt();
        for (int i = 0; i < visibleNodeSize; i++) {
            this.visibleNodes.add(NodeLocation.readFromByteBuf(buf));
        }
    }

    public void clearUnlock() {
        this.unlocked.clear();
        this.unlockedGroups.clear();
        this.visibleGroups.clear();
        this.visibleNodes.clear();
    }

    public record CheckReason(boolean flag, boolean canBeSeen, boolean groupUnlocked,
                              boolean enoughLevel, Collection<ItemStack> requiredItems,
                              Collection<NodeLocation> requiredNodes,
                              Collection<ItemStack> itemGot, String message) {

        public static CheckReason exceptionCase(String msg) {
            return new CheckReason(false, false, false, false,
                    List.of(), List.of(), List.of(), msg);
        }

        public static CheckReason creativeSuccess(Collection<ItemStack> itemGot) {
            return new CheckReason(true, true, true, true,
                    List.of(), List.of(), itemGot, "");
        }

        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeBoolean(this.flag);
            buf.writeBoolean(this.canBeSeen);
            buf.writeBoolean(this.groupUnlocked);
            buf.writeBoolean(this.enoughLevel);

            buf.writeInt(requiredItems.size());
            requiredItems.forEach(item -> buf.writeItemStack(item, false));

            buf.writeInt(requiredNodes.size());
            requiredNodes.forEach(node -> node.writeToByteBuf(buf));

            buf.writeInt(itemGot.size());
            itemGot.forEach(item -> buf.writeItemStack(item, false));

            buf.writeUtf(message);
        }

        public static CheckReason fromNetwork(FriendlyByteBuf buf) {
            boolean flag = buf.readBoolean();
            boolean canBeSeen = buf.readBoolean();
            boolean groupUnlocked = buf.readBoolean();
            boolean enoughLevel = buf.readBoolean();

            int requiredItemCount = buf.readInt();
            ArrayList<ItemStack> requiredItems = new ArrayList<>(requiredItemCount);
            for (int i = 0; i < requiredItemCount; i++) {
                requiredItems.add(buf.readItem());
            }

            int requiredNodeCount = buf.readInt();
            ArrayList<NodeLocation> requiredNodes = new ArrayList<>(requiredNodeCount);
            for (int i = 0; i < requiredNodeCount; i++) {
                requiredNodes.add(NodeLocation.readFromByteBuf(buf));
            }

            int itemGotSize = buf.readInt();
            ArrayList<ItemStack> itemGot = new ArrayList<>(itemGotSize);
            for (int i = 0; i < itemGotSize; i++) {
                itemGot.add(buf.readItem());
            }

            String message = buf.readUtf();
            return new CheckReason(flag, canBeSeen, groupUnlocked, enoughLevel, requiredItems, requiredNodes, itemGot, message);
        }
    }

    public record UnlockResult(boolean flag,
                               Collection<Pair<Integer, Integer>> itemGrow,
                               int expConsume,
                               HashMap<ResourceLocation, Collection<String>> criteria,
                               Collection<NodeLocation> updatedUnlockedNodes,
                               Collection<NodeLocation> updatedVisibleNodes,
                               Collection<ResourceLocation> updatedUnlockedGroups,
                               Collection<ResourceLocation> updatedVisibleGroups) {

        public static UnlockResult failedEmpty() {
            return new UnlockResult(false, List.of(), 0, new HashMap<>(), Set.of(), Set.of(), Set.of(), Set.of());
        }

        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeBoolean(flag);
            buf.writeInt(itemGrow.size());
            itemGrow.forEach(pair -> {
                buf.writeInt(pair.getFirst());
                buf.writeInt(pair.getSecond());
            });

            buf.writeInt(expConsume);
            buf.writeInt(criteria.size());
            criteria.forEach((rl, collection) -> {
                buf.writeResourceLocation(rl);
                buf.writeInt(collection.size());
                collection.forEach(buf::writeUtf);
            });

            buf.writeInt(updatedUnlockedNodes.size());
            updatedUnlockedNodes.forEach(node -> node.writeToByteBuf(buf));

            buf.writeInt(updatedVisibleNodes.size());
            updatedVisibleNodes.forEach(node -> node.writeToByteBuf(buf));

            buf.writeInt(updatedUnlockedGroups.size());
            updatedUnlockedGroups.forEach(buf::writeResourceLocation);

            buf.writeInt(updatedVisibleGroups.size());
            updatedVisibleGroups.forEach(buf::writeResourceLocation);
        }

        public static UnlockResult fromNetwork(FriendlyByteBuf buf) {
            boolean flag = buf.readBoolean();
            int itemCount = buf.readInt();
            ArrayList<Pair<Integer, Integer>> itemGrow = new ArrayList<>(itemCount);
            for (int i = 0; i < itemCount; i++) {
                itemGrow.add(Pair.of(buf.readInt(), buf.readInt()));
            }
            int expConsume = buf.readInt();
            int criteriaCount = buf.readInt();
            HashMap<ResourceLocation, Collection<String>> criteria = new HashMap<>(criteriaCount);
            for (int i = 0; i < criteriaCount; i++) {
                ResourceLocation rl = buf.readResourceLocation();
                int count = buf.readInt();
                HashSet<String> set = new HashSet<>(count);
                for (int j = 0; j < count; j++) {
                    set.add(buf.readUtf());
                }
                criteria.put(rl, set);
            }

            int updatedUnlockedNodeCount = buf.readInt();
            HashSet<NodeLocation> updatedUnlockedNodes = new HashSet<>(updatedUnlockedNodeCount);
            for (int i = 0; i < updatedUnlockedNodeCount; i++) {
                updatedUnlockedNodes.add(NodeLocation.readFromByteBuf(buf));
            }

            int updatedVisibleNodeCount = buf.readInt();
            HashSet<NodeLocation> updatedVisibleNodes = new HashSet<>(updatedVisibleNodeCount);
            for (int i = 0; i < updatedVisibleNodeCount; i++) {
                updatedVisibleNodes.add(NodeLocation.readFromByteBuf(buf));
            }

            int updatedUnlockedGroupCount = buf.readInt();
            HashSet<ResourceLocation> updatedUnlockedGroups = new HashSet<>(updatedUnlockedGroupCount);
            for (int i = 0; i < updatedUnlockedGroupCount; i++) {
                updatedUnlockedGroups.add(buf.readResourceLocation());
            }

            int updatedVisibleGroupCount = buf.readInt();
            HashSet<ResourceLocation> updatedVisibleGroups = new HashSet<>(updatedVisibleGroupCount);
            for (int i = 0; i < updatedVisibleGroupCount; i++) {
                updatedVisibleGroups.add(buf.readResourceLocation());
            }
            return new UnlockResult(flag, itemGrow, expConsume, criteria,
                    updatedUnlockedNodes, updatedVisibleNodes, updatedUnlockedGroups, updatedVisibleGroups);
        }
    }
}
