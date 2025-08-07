package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.Util;

import net.runelite.api.ItemID;
import net.runelite.api.Varbits;
import net.runelite.client.plugins.microbot.Microbot;

public enum BFBarRecipes {
    STEEL_BAR(ItemID.IRON_ORE, Varbits.BLAST_FURNACE_IRON_ORE, Varbits.BLAST_FURNACE_STEEL_BAR, 1, false),
    MITHRIL_BAR(ItemID.MITHRIL_ORE, Varbits.BLAST_FURNACE_MITHRIL_ORE, Varbits.BLAST_FURNACE_MITHRIL_BAR, 2, false),
    ADAMANTITE_BAR(ItemID.ADAMANTITE_ORE, Varbits.BLAST_FURNACE_ADAMANTITE_ORE, Varbits.BLAST_FURNACE_ADAMANTITE_BAR, 3, false),
    RUNITE_BAR(ItemID.RUNITE_ORE, Varbits.BLAST_FURNACE_RUNITE_ORE, Varbits.BLAST_FURNACE_RUNITE_BAR, 4, false),

    RUNITE_GOLD_BARS(ItemID.RUNITE_ORE, Varbits.BLAST_FURNACE_RUNITE_ORE, Varbits.BLAST_FURNACE_RUNITE_BAR, 4, true),
    ADAMANTITE_GOLD_BARS(ItemID.ADAMANTITE_ORE, Varbits.BLAST_FURNACE_ADAMANTITE_ORE, Varbits.BLAST_FURNACE_ADAMANTITE_BAR, 3, true);

    public final int oreId;
    public final int oreVarbit;
    public final int barVarbit;
    public final int coalNeeded;
    public final boolean isHybrid;

    BFBarRecipes(int oreId, int oreVarbit, int barVarbit, int coalNeeded, boolean isHybrid) {
        this.oreId = oreId;
        this.oreVarbit = oreVarbit;
        this.barVarbit = barVarbit;
        this.coalNeeded = coalNeeded;
        this.isHybrid = isHybrid;
    }

    public int getNumOreInFurnace() {
        return Microbot.getVarbitValue(this.oreVarbit);
    }

    public int getNumGoldBarsInFurnace() {
        return Microbot.getVarbitValue(Varbits.BLAST_FURNACE_GOLD_BAR);
    }

    public int getNumBarsInDispenser() {
        return Microbot.getVarbitValue(this.barVarbit);
    }

    public boolean furnaceRequiresMoreCoal() {
        return Microbot.getVarbitValue(this.oreVarbit) * Microbot.getVarbitValue(this.coalNeeded)
                >= Microbot.getVarbitValue(Varbits.BLAST_FURNACE_COAL);
    }

    public int coalRequired() {
        return 27 * this.coalNeeded;
    }
}
