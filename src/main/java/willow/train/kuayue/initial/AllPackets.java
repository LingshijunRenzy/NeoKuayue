package willow.train.kuayue.initial;

import kasuga.lib.registrations.common.ChannelReg;
import willow.train.kuayue.network.c2s.*;
import willow.train.kuayue.network.c2s.tech_tree.CanUnlockNodePacket;
import willow.train.kuayue.network.c2s.tech_tree.TechTreeEOFC2SPacket;
import willow.train.kuayue.network.c2s.tech_tree.TechTreeHandShakeC2SPacket;
import willow.train.kuayue.network.c2s.tech_tree.UnlockNodePacket;
import willow.train.kuayue.network.s2c.*;
import willow.train.kuayue.network.s2c.tech_tree.*;

public class AllPackets {
    public static final String KUAYUE_NETWORK_VERSION = "v1.0.0";

    public static final ChannelReg CHANNEL = new ChannelReg("kuayue_main_channel")
            .brand(KUAYUE_NETWORK_VERSION)
            .loadPacket(ContraptionTagChangedPacket.class, ContraptionTagChangedPacket::new)
            .loadPacket(ColorTemplateS2CPacket.class, ColorTemplateS2CPacket::new)
            .loadPacket(ColorTemplateC2SPacket.class, ColorTemplateC2SPacket::new)
            .loadPacket(DiscardChangeC2SPacket.class, DiscardChangeC2SPacket::new)
            .loadPacket(NbtC2SPacket.class, NbtC2SPacket::new)
            .submit(AllElements.testRegistry);

    public static final ChannelReg TECH_TREE_CHANNEL = new ChannelReg("kuayue_tech_tree_channel")
            .brand(KUAYUE_NETWORK_VERSION)
            .loadPacket(TechTreePacket.class, TechTreePacket::new)
            .loadPacket(TechTreeGroupPacket.class, TechTreeGroupPacket::new)
            .loadPacket(TechTreeNodePacket.class, TechTreeNodePacket::new)
            .loadPacket(TechTreeHandShakeS2CPacket.class, TechTreeHandShakeS2CPacket::new)
            .loadPacket(TechTreeHandShakeC2SPacket.class, TechTreeHandShakeC2SPacket::new)
            .loadPacket(TechTreeEOFS2CPacket.class, TechTreeEOFS2CPacket::new)
            .loadPacket(TechTreeEOFC2SPacket.class, TechTreeEOFC2SPacket::new)
            .loadPacket(UpdateUnlockedS2CPacket.class, UpdateUnlockedS2CPacket::new)
            .loadPacket(CanUnlockNodePacket.class, CanUnlockNodePacket::new)
            .loadPacket(CanUnlockNodeS2CPacket.class, CanUnlockNodeS2CPacket::new)
            .loadPacket(UnlockNodePacket.class, UnlockNodePacket::new)
            .loadPacket(UnlockNodeResultPacket.class, UnlockNodeResultPacket::new)
            .submit(AllElements.testRegistry);

    public static final ChannelReg INTERACTION = new ChannelReg("interaction")
            .brand(KUAYUE_NETWORK_VERSION)
            .loadPacket(ContraptionNbtUpdatePacket.class, ContraptionNbtUpdatePacket::new)
            .loadPacket(SeatDismountPacket.class, SeatDismountPacket::new)
            .loadPacket(OnSeatActionPacket.class, OnSeatActionPacket::new)
            .submit(AllElements.testRegistry);

    public static void invoke() {}
}
