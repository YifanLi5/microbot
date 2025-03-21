package net.runelite.client.plugins.microbot.yfoo.yBlastFurnace.Util;

import net.runelite.api.ItemID;
import net.runelite.api.Varbits;
import net.runelite.client.plugins.microbot.Microbot;

public enum BFBarRecipes {
    STEEL_BAR(ItemID.IRON_ORE, Varbits.BLAST_FURNACE_IRON_ORE, Varbits.BLAST_FURNACE_STEEL_BAR, 1),
    MITHRIL_BAR(ItemID.MITHRIL_ORE, Varbits.BLAST_FURNACE_MITHRIL_ORE, Varbits.BLAST_FURNACE_MITHRIL_BAR, 2),
    ADAMANTITE_BAR(ItemID.ADAMANTITE_ORE, Varbits.BLAST_FURNACE_ADAMANTITE_ORE, Varbits.BLAST_FURNACE_ADAMANTITE_BAR, 3),
    RUNITE_BAR(ItemID.RUNITE_ORE, Varbits.BLAST_FURNACE_RUNITE_ORE, Varbits.BLAST_FURNACE_RUNITE_BAR, 4);

    public final int oreId;
    public final int oreVarbit;
    public final int barVarbit;
    public final int coalNeeded;

    BFBarRecipes(int oreId, int oreVarbit, int barVarbit, int coalNeeded) {
        this.oreId = oreId;
        this.oreVarbit = oreVarbit;
        this.barVarbit = barVarbit;
        this.coalNeeded = coalNeeded;
    }

    public int getNumOreInFurnace() {
        return Microbot.getVarbitValue(this.oreVarbit);
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
