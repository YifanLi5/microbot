/*
 * Copyright (c) 2020, Zoinkwiz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.microbot.questhelper.helpers.quests.throneofmiscellania;

import net.runelite.client.plugins.microbot.questhelper.collections.ItemCollections;
import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.BasicQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.questinfo.QuestHelperQuest;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.Conditions;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.player.SkillRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.quest.QuestRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.Operation;
import net.runelite.client.plugins.microbot.questhelper.requirements.var.VarbitRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.var.VarplayerRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.QuestPointReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.UnlockReward;
import net.runelite.client.plugins.microbot.questhelper.steps.*;
import net.runelite.client.plugins.microbot.questhelper.steps.emote.QuestEmote;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;

import java.util.*;

public class ThroneOfMiscellania extends BasicQuestHelper
{
	//Items Required
	ItemRequirement ironBar, logs, pickaxe, rake, axe, harpoon, lobsterPot, reputationItems, ring, flowers, cake, bow,
		giantNib, giantPen, goodAnthem, awfulAnthem, treaty;

	//Items Recommended
	ItemRequirement runRestoreItems, dramenStaff, rellekkaTeleport;

	Requirement inIslands, inMiscCastleFirstFloor, inEtcCastleFirstFloor, inAstridRoom, inBrandRoom,
		talked1P1, talked1P2, talked1P3, givenFlowers, doneEmote, talked1P4, talked2P1, talked2P2, talked2P3, givenBowOrCake,
		talked2P4, talked3P1, talked3P2, talked3P3, blownKiss, diplomacyStep1, diplomacyStep2, diplomacyStep3, diplomacyStep4,
		diplomacyStep5, diplomacyStep6, hasCourted, has75Support, courtingBrand;

	DetailedQuestStep travelToMisc, talkToVargas, getFlowers, goUpToVargas, talkAstrid1, talkAstrid2, talkAstrid3,
		talkBrand1, talkBrand2, talkBrand3, giveFlowersToAstrid, giveFlowersToBrand, giveBowToAstrid,
		giveCakeToBrand, clapForBrand, danceForAstrid, goUpstairsToBrand, blowKissToAstrid, blowKissToBrand, useRingOnAstrid,
		useRingOnBrand, goUpstairsToAstrid, goUpEtcDip1, talkToSigridDip1, goDownEtcDip1, goUpMiscDip1, talkToVargasDip1, goDownMiscDip1,
		goUpEtcDip2, talkToSigridDip2, goDownEtcDip2, goUpMiscDip2, talkToBrandDip, talkToGhrimDip, goDownMiscDip2, goUpEtcDip3,
		talkToSigridDip3, goDownEtcDip3, goUpMiscDip3, talkToVargasDip2, goDownMiscDip3, talkToDerrik, makePen, goUpMiscDip4, giveVargasPen,
		getAnotherAwfulAnthem, get75Support, goUpMiscForSupport, goDownMiscForSupport, finishQuest;

	ConditionalStep courtBrand, courtAstrid;

	//Zones
	Zone islands, miscCastleFirstFloor, etcCastleFirstFloor, brandRoom1, brandRoom2, astridRoom1, astridRoom2;

	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		initializeRequirements();
		setupConditions();
		setupSteps();
		Map<Integer, QuestStep> steps = new HashMap<>();

		ConditionalStep startQuest = new ConditionalStep(this, travelToMisc);
		startQuest.addStep(inMiscCastleFirstFloor, talkToVargas);
		startQuest.addStep(new Conditions(inIslands, flowers), goUpToVargas);
		startQuest.addStep(inIslands, getFlowers);

		steps.put(0, startQuest);

		courtAstrid = new ConditionalStep(this, travelToMisc);
		courtAstrid.addStep(new Conditions(inMiscCastleFirstFloor, talked3P1, talked3P2, talked3P3, blownKiss), useRingOnAstrid);
		courtAstrid.addStep(new Conditions(inMiscCastleFirstFloor, talked3P1, talked3P2, talked3P3), blowKissToAstrid);
		courtAstrid.addStep(new Conditions(inMiscCastleFirstFloor, talked2P4), talkAstrid3);
		courtAstrid.addStep(new Conditions(inMiscCastleFirstFloor, talked2P1, talked2P2, talked2P3), giveBowToAstrid);
		courtAstrid.addStep(new Conditions(inMiscCastleFirstFloor, talked1P4), talkAstrid2);
		courtAstrid.addStep(new Conditions(inMiscCastleFirstFloor, talked1P3, talked1P2, talked1P3, givenFlowers), danceForAstrid);
		courtAstrid.addStep(new Conditions(inMiscCastleFirstFloor, talked1P1, talked1P2, talked1P3), giveFlowersToAstrid);
		courtAstrid.addStep(inMiscCastleFirstFloor, talkAstrid1);
		courtAstrid.addStep(inIslands, goUpstairsToAstrid);

		courtBrand = new ConditionalStep(this, travelToMisc);
		courtBrand.addStep(new Conditions(inMiscCastleFirstFloor, talked3P1, talked3P2, talked3P3, blownKiss), useRingOnBrand);
		courtBrand.addStep(new Conditions(inMiscCastleFirstFloor, talked3P1, talked3P2, talked3P3), blowKissToBrand);
		courtBrand.addStep(new Conditions(inMiscCastleFirstFloor, talked2P4), talkBrand3);
		courtBrand.addStep(new Conditions(inMiscCastleFirstFloor, talked2P1, talked2P2, talked2P3), giveCakeToBrand);
		courtBrand.addStep(new Conditions(inMiscCastleFirstFloor, talked1P4), talkBrand2);
		courtBrand.addStep(new Conditions(inMiscCastleFirstFloor, talked1P3, talked1P2, talked1P3, givenFlowers), clapForBrand);
		courtBrand.addStep(new Conditions(inMiscCastleFirstFloor,talked1P1, talked1P2, talked1P3), giveFlowersToBrand);
		courtBrand.addStep(inMiscCastleFirstFloor, talkBrand1);
		courtBrand.addStep(inIslands, goUpstairsToBrand);

		ConditionalStep establishPeace1 = new ConditionalStep(this, goUpEtcDip1);
		establishPeace1.addStep(inEtcCastleFirstFloor, talkToSigridDip1);

		ConditionalStep establishPeace2 = new ConditionalStep(this, goUpMiscDip1);
		establishPeace2.addStep(inMiscCastleFirstFloor, talkToVargasDip1);
		establishPeace2.addStep(inEtcCastleFirstFloor, goDownEtcDip1);

		ConditionalStep establishPeace3 = new ConditionalStep(this, goUpEtcDip2);
		establishPeace3.addStep(inEtcCastleFirstFloor, talkToSigridDip2);
		establishPeace3.addStep(inMiscCastleFirstFloor, goDownMiscDip1);

		ConditionalStep establishPeace4 = new ConditionalStep(this, goUpMiscDip2);
		establishPeace4.addStep(inMiscCastleFirstFloor, talkToBrandDip);
		establishPeace4.addStep(inEtcCastleFirstFloor, goDownEtcDip2);

		ConditionalStep establishPeace5 = new ConditionalStep(this, goUpMiscDip2);
		establishPeace5.addStep(new Conditions(inEtcCastleFirstFloor, goodAnthem), talkToSigridDip3);
		establishPeace5.addStep(new Conditions(inMiscCastleFirstFloor, goodAnthem), goDownMiscDip2);
		establishPeace5.addStep(goodAnthem, goUpEtcDip3);
		establishPeace5.addStep(new Conditions(awfulAnthem, inMiscCastleFirstFloor), talkToGhrimDip);
		establishPeace5.addStep(inMiscCastleFirstFloor, getAnotherAwfulAnthem);

		ConditionalStep establishPeace6 = new ConditionalStep(this, goUpMiscDip2);
		establishPeace6.addStep(new Conditions(inEtcCastleFirstFloor, goodAnthem), talkToSigridDip3);
		establishPeace6.addStep(new Conditions(inMiscCastleFirstFloor, goodAnthem), goDownMiscDip2);
		establishPeace6.addStep(goodAnthem, goUpEtcDip3);
		establishPeace6.addStep(new Conditions(awfulAnthem, inMiscCastleFirstFloor), talkToGhrimDip);
		establishPeace6.addStep(inMiscCastleFirstFloor, getAnotherAwfulAnthem);

		ConditionalStep establishPeace7 = new ConditionalStep(this, goUpEtcDip3);
		establishPeace7.addStep(new Conditions(treaty, inMiscCastleFirstFloor), talkToVargasDip2);
		establishPeace7.addStep(new Conditions(treaty, inEtcCastleFirstFloor), goDownEtcDip3);
		establishPeace7.addStep(treaty, goUpMiscDip3);
		establishPeace7.addStep(inEtcCastleFirstFloor, talkToSigridDip3);
		establishPeace7.addStep(inMiscCastleFirstFloor, goDownMiscDip2);

		ConditionalStep establishPeace8 = new ConditionalStep(this, talkToDerrik);
		establishPeace8.addStep(new Conditions(giantPen, inMiscCastleFirstFloor), giveVargasPen);
		establishPeace8.addStep(giantPen, goUpMiscDip4);
		establishPeace8.addStep(giantNib, makePen);
		establishPeace8.addStep(inMiscCastleFirstFloor, goDownMiscDip3);

		ConditionalStep courting;

		courting = new ConditionalStep(this, courtAstrid);
		courting.addStep(courtingBrand, courtBrand);

		// TODO: Add toggle for Brand once confirmed what's up with new progression

		ConditionalStep becomeRoyalty1 = new ConditionalStep(this, courting);
		ConditionalStep becomeRoyalty2 = new ConditionalStep(this, courting);
		ConditionalStep becomeRoyalty3 = new ConditionalStep(this, courting);
		ConditionalStep becomeRoyalty4 = new ConditionalStep(this, courting);
		ConditionalStep becomeRoyalty5 = new ConditionalStep(this, courting);
		ConditionalStep becomeRoyalty6 = new ConditionalStep(this, courting);
		ConditionalStep becomeRoyalty7 = new ConditionalStep(this, courting);
		ConditionalStep becomeRoyalty8 = new ConditionalStep(this, courting);
		becomeRoyalty1.addStep(hasCourted, establishPeace1);
		becomeRoyalty2.addStep(hasCourted, establishPeace2);
		becomeRoyalty3.addStep(hasCourted, establishPeace3);
		becomeRoyalty4.addStep(hasCourted, establishPeace4);
		becomeRoyalty5.addStep(hasCourted, establishPeace5);
		becomeRoyalty6.addStep(hasCourted, establishPeace6);
		becomeRoyalty7.addStep(hasCourted, establishPeace7);
		becomeRoyalty8.addStep(hasCourted, establishPeace8);

		steps.put(10, becomeRoyalty1);
		steps.put(20, becomeRoyalty2);
		steps.put(30, becomeRoyalty3);
		steps.put(40, becomeRoyalty4);
		steps.put(50, becomeRoyalty5);
		steps.put(60, becomeRoyalty6);
		steps.put(70, becomeRoyalty7);
		steps.put(80, becomeRoyalty8);

		ConditionalStep finishOff = new ConditionalStep(this, get75Support);
		finishOff.addStep(new Conditions(inMiscCastleFirstFloor, has75Support), finishQuest);
		finishOff.addStep(has75Support, goUpMiscForSupport);
		finishOff.addStep(inMiscCastleFirstFloor, goDownMiscForSupport);

		steps.put(90, finishOff);

		return steps;
	}

	@Override
	protected void setupRequirements()
	{
		ironBar = new ItemRequirement("Iron bar", ItemID.IRON_BAR);
		logs = new ItemRequirement("Logs", ItemID.LOGS);
		logs.setHighlightInInventory(true);
		logs.setTooltip("You can chop logs on Miscellania if you bring an axe.");
		pickaxe = new ItemRequirement("Any pickaxe", ItemCollections.PICKAXES).isNotConsumed();
		rake = new ItemRequirement("Rake", ItemID.RAKE).isNotConsumed();
		axe = new ItemRequirement("Any axe", ItemCollections.AXES).isNotConsumed();
		harpoon = new ItemRequirement("Harpoon", ItemCollections.HARPOONS).isNotConsumed();
		lobsterPot = new ItemRequirement("Lobster pot", ItemID.LOBSTER_POT).isNotConsumed();
		ring = new ItemRequirement("Any non-silver ring you are willing to lose", ItemID.GOLD_RING);
		ring.addAlternates(ItemID.SAPPHIRE_RING, ItemID.EMERALD_RING, ItemID.RUBY_RING, ItemID.DIAMOND_RING);

		flowers = new ItemRequirement("Flowers", ItemCollections.FLOWERS);
		flowers.setTooltip("You can buy some from the Flower Girl on Miscellania for 15 coins");
		flowers.setHighlightInInventory(true);
		cake = new ItemRequirement("Cake (if courting Brand)", ItemID.CAKE);
		cake.addAlternates(ItemID.CHOCOLATE_CAKE);
		bow = new ItemRequirement("Any normal/oak/willow/maple/yew shortbow or longbow (if courting Astrid)", ItemID.SHORTBOW);
		bow.addAlternates(ItemID.LONGBOW, ItemID.OAK_SHORTBOW, ItemID.OAK_LONGBOW, ItemID.WILLOW_SHORTBOW, ItemID.WILLOW_LONGBOW, ItemID.MAPLE_SHORTBOW, ItemID.MAPLE_LONGBOW, ItemID.YEW_SHORTBOW, ItemID.YEW_LONGBOW);
		bow.setTooltip("You will lose this bow");
		bow.setHighlightInInventory(true);
		runRestoreItems = new ItemRequirement("Potions/Items to restore run energy", ItemCollections.RUN_RESTORE_ITEMS);
		dramenStaff = new ItemRequirement("Dramen staff if travelling via Fairy Ring CIP", ItemCollections.FAIRY_STAFF).isNotConsumed();
		rellekkaTeleport = new ItemRequirement("Miscellania teleport (Fairy Ring (CIP), tablet, lyre)", ItemCollections.FAIRY_STAFF);
		rellekkaTeleport.addAlternates(ItemID.NZONE_TELETAB_RELLEKKA, ItemID.MAGIC_STRUNG_LYRE_INFINITE, ItemID.MAGIC_STRUNG_LYRE_5, ItemID.MAGIC_STRUNG_LYRE_4, ItemID.MAGIC_STRUNG_LYRE_3, ItemID.MAGIC_STRUNG_LYRE_2, ItemID.MAGIC_STRUNG_LYRE);

		giantNib = new ItemRequirement("Giant nib", ItemID.MISC_GIANT_NIB);
		giantNib.setHighlightInInventory(true);
		giantPen = new ItemRequirement("Giant pen", ItemID.MISC_GIANT_PEN);
		awfulAnthem = new ItemRequirement("Awful anthem", ItemID.MISC_AWFUL_ANTHEM);
		goodAnthem = new ItemRequirement("Good anthem", ItemID.MISC_GOOD_ANTHEM);
		treaty = new ItemRequirement("Treaty", ItemID.MISC_TREATY);

		reputationItems = new ItemRequirement("One of: ", ItemID.LOBSTER_POT);
		if (client.getRealSkillLevel(Skill.FARMING) >= 10)
		{
			reputationItems.setName(reputationItems.getName() + "a rake, ");
			// Set ID so it's first recommended item
			reputationItems.setId(ItemID.RAKE);
		}
		if (client.getRealSkillLevel(Skill.MINING) >= 30)
		{
			reputationItems.setName(reputationItems.getName() + "a pickaxe, ");
			reputationItems.addAlternates(ItemCollections.PICKAXES);
		}
		if (client.getRealSkillLevel(Skill.WOODCUTTING) >= 45)
		{
			reputationItems.setName(reputationItems.getName() + "an axe, ");
			reputationItems.addAlternates(ItemCollections.AXES);
		}
		// The player requires Heroes' Quest, thus can do this
		reputationItems.setName(reputationItems.getName() + "a harpoon or lobster pot.");
		reputationItems.addAlternates(ItemID.HARPOON, ItemID.LOBSTER_POT);
	}

	@Override
	protected void setupZones()
	{
		islands = new Zone(new WorldPoint(2491, 3835, 0), new WorldPoint(2627, 3904, 3));
		miscCastleFirstFloor = new Zone(new WorldPoint(2497, 3845, 1), new WorldPoint(2511, 3875, 1));
		etcCastleFirstFloor = new Zone(new WorldPoint(2607, 3864, 1), new WorldPoint(2618, 3886, 1));
		brandRoom1 = new Zone(new WorldPoint(2498, 3849, 1), new WorldPoint(2502, 3854, 1));
		brandRoom2 = new Zone(new WorldPoint(2503, 3851, 1), new WorldPoint(2504, 3854, 1));
		astridRoom1 = new Zone(new WorldPoint(2498, 3866, 1), new WorldPoint(2502, 3871, 1));
		astridRoom2 = new Zone(new WorldPoint(2503, 3866, 1), new WorldPoint(2504, 3869, 1));
	}

	public void setupConditions()
	{
		inIslands = new ZoneRequirement(islands);
		inMiscCastleFirstFloor = new ZoneRequirement(miscCastleFirstFloor);
		inEtcCastleFirstFloor = new ZoneRequirement(etcCastleFirstFloor);
		inBrandRoom = new ZoneRequirement(brandRoom1, brandRoom2);
		inAstridRoom = new ZoneRequirement(astridRoom1, astridRoom2);

		// Chose Brand, 14607 0->1
		courtingBrand = new VarbitRequirement(14607, 1);

		talked1P1 = new VarbitRequirement(85, 1);
		talked1P2 = new VarbitRequirement(86, 1);
		talked1P3 = new VarbitRequirement(87, 1);
		givenFlowers = new VarbitRequirement(94, 1);
		doneEmote = new VarbitRequirement(96, 1);
		talked1P4 = new VarbitRequirement(VarbitID.MISC_AFFECTION, 15, Operation.GREATER_EQUAL);

		talked2P1 = new VarbitRequirement(88, 1);
		talked2P2 = new VarbitRequirement(89, 1);
		talked2P3 = new VarbitRequirement(90, 1);
		givenBowOrCake = new VarbitRequirement(95, 1);
		talked2P4 = new VarbitRequirement(VarbitID.MISC_AFFECTION, 24, Operation.GREATER_EQUAL);

		talked3P1 = new VarbitRequirement(91, 1);
		talked3P2 = new VarbitRequirement(92, 1);
		talked3P3 = new VarbitRequirement(93, 1);
		blownKiss = new VarbitRequirement(97, 1);

		hasCourted = new VarbitRequirement(14606, 1);

		diplomacyStep1 = new VarplayerRequirement(359, 20);
		diplomacyStep2 = new VarplayerRequirement(359, 30);
		diplomacyStep3 = new VarplayerRequirement(359, 40);
		diplomacyStep4 = new VarplayerRequirement(359, 50);
		diplomacyStep5 = new VarplayerRequirement(359, 60);
		diplomacyStep6 = new VarplayerRequirement(359, 70);

		has75Support = new VarbitRequirement(VarbitID.MISC_APPROVAL, 96, Operation.GREATER_EQUAL);
	}

	public void setupSteps()
	{
		String travelText = "Travel to Miscellania. You can take a boat from Rellekka. You can also use Fairy Rings to teleport there with the code CIP If you've unlocked them.";
		travelToMisc = new NpcStep(this, NpcID.VIKING_SAILOR, new WorldPoint(2629, 3693, 0), travelText);
		travelToMisc.addTeleport(rellekkaTeleport);
		getFlowers = new NpcStep(this, NpcID.MISC_FLOWERGIRL, new WorldPoint(2511, 3865, 0), "Buy some flowers from the Flower Girl for 15gp.");
		getFlowers.addDialogStep("Yes, please.");
		goUpToVargas = new ObjectStep(this, ObjectID.SPIRALSTAIRS_WOODEN, new WorldPoint(2506, 3849, 0), "Go upstairs in the Miscellania castle.");
		talkToVargas = new NpcStep(this, NpcID.MISC_KING_VARGAS, new WorldPoint(2501, 3860, 1), "Talk to King Vargas. You can choose whether to get Brand's or Astrid's approval");
		talkToVargas.addDialogSteps("Yes.", "If I may be so bold...");
		talkToVargas.addSubSteps(goUpToVargas);

		/* Winning over Astrid */
		talkAstrid1 = new NpcStep(this, NpcID.MISC_PRINCESS_ASTRID, new WorldPoint(2502, 3867, 1), "Talk to Princess Astrid a few times.");
		talkAstrid1.addDialogStep("Archery is a noble art!");
		talkAstrid1.addDialogStep("He's been very helpful.");
		talkAstrid1.addDialogStep("Hahahaha!");

		talkAstrid2 = new NpcStep(this, NpcID.MISC_PRINCESS_ASTRID, new WorldPoint(2502, 3867, 1), "Keep talking to Princess Astrid.");
		talkAstrid2.addDialogStep("What happened next?");
		talkAstrid2.addDialogStep("That sounds like a good idea.");
		talkAstrid2.addDialogStep("I'm quite fond of it myself.");

		talkAstrid3 = new NpcStep(this, NpcID.MISC_PRINCESS_ASTRID, new WorldPoint(2502, 3867, 1), "Keep talking to Princess Astrid.");
		talkAstrid3.addDialogStep("It's a lovely little country.");
		talkAstrid3.addDialogStep("I suppose you don't have much opportunity to.");
		talkAstrid3.addDialogStep("And what a great bard he makes!");

		giveFlowersToAstrid = new NpcStep(this, NpcID.MISC_PRINCESS_ASTRID, new WorldPoint(2502, 3867, 1), "Use flowers on Astrid.", flowers);
		giveFlowersToAstrid.addIcon(ItemID.FLOWERS_WATERFALL_QUEST_MIXED);
		giveFlowersToAstrid.addDialogStep("Yes");
		giveBowToAstrid = new NpcStep(this, NpcID.MISC_PRINCESS_ASTRID, new WorldPoint(2502, 3867, 1),
			"Use any bow on Astrid.", bow.highlighted());
		giveBowToAstrid.addIcon(ItemID.SHORTBOW);
		giveBowToAstrid.addDialogStep("Yes");
		danceForAstrid = new EmoteStep(this, QuestEmote.DANCE, "Dance in Princess Astrid's room.");
		blowKissToAstrid = new EmoteStep(this, QuestEmote.BLOW_KISS, "Blow kiss emote next to Princess Astrid.");
		useRingOnAstrid = new NpcStep(this, NpcID.MISC_PRINCESS_ASTRID, new WorldPoint(2502, 3867, 1),
			"Use a ring on Astrid.", ring.highlighted());
		useRingOnAstrid.addIcon(ItemID.GOLD_RING);
		useRingOnAstrid.addDialogStep("Yes");

		talkBrand1 = new NpcStep(this, NpcID.MISC_PRINCE_BRAND, new WorldPoint(2502, 3852, 1), "Talk to Prince Brand a few times.");
		talkBrand1.addDialogStep("Be still, my heart.");
		talkBrand1.addDialogStep("You will be the greatest bard!");
		talkBrand1.addDialogStep("They don't understand your poetry as I do.");

		talkBrand2 = new NpcStep(this, NpcID.MISC_PRINCE_BRAND, new WorldPoint(2502, 3852, 1), "Talk to Prince Brand more.");
		talkBrand2.addDialogStep("A much nobler pursuit, to be sure.");
		talkBrand2.addDialogStep("How inspiring!");
		talkBrand2.addDialogStep("How poetic.");

		talkBrand3 = new NpcStep(this, NpcID.MISC_PRINCE_BRAND, new WorldPoint(2502, 3852, 1), "Talk to Prince Brand more.");
		talkBrand3.addDialogStep("I'm glad to hear it.");
		talkBrand3.addDialogStep("I wouldn't presume to have the skill...");
		talkBrand3.addDialogStep("That was lovely. I'm touched!");

		giveCakeToBrand = new NpcStep(this, NpcID.MISC_PRINCE_BRAND, new WorldPoint(2502, 3852, 1),
			"Give Prince Brand a cake.", cake.highlighted());
		giveCakeToBrand.addDialogStep("Yes");
		giveFlowersToBrand = new NpcStep(this, NpcID.MISC_PRINCE_BRAND, new WorldPoint(2502, 3852, 1), "Use flowers on Prince Brand.", flowers);
		giveFlowersToBrand.addDialogStep("Yes");
		giveFlowersToBrand.addIcon(ItemID.FLOWERS_WATERFALL_QUEST_MIXED);
		blowKissToBrand = new EmoteStep(this, QuestEmote.BLOW_KISS, "Use the blow kiss emote next to Prince Brand");
		clapForBrand = new EmoteStep(this, QuestEmote.CLAP, "Use the Clap emote next to Prince Brand");
		useRingOnBrand = new NpcStep(this, NpcID.MISC_PRINCE_BRAND, new WorldPoint(2502, 3852, 1),
			"Use a ring on Prince Brand.", ring.highlighted());
		useRingOnBrand.addDialogStep("Yes");
		useRingOnBrand.addIcon(ItemID.GOLD_RING);

		goUpstairsToBrand = new ObjectStep(this, ObjectID.SPIRALSTAIRS_WOODEN, new WorldPoint(2506, 3849, 0), "Go upstairs in the Miscellania castle.");
		goUpstairsToBrand.setShowInSidebar(false);
		goUpstairsToAstrid = new ObjectStep(this, ObjectID.SPIRALSTAIRS_WOODEN, new WorldPoint(2506, 3872, 0), "Go upstairs in the Miscellania castle.");
		goUpstairsToAstrid.setShowInSidebar(false);

		goUpEtcDip1 = new ObjectStep(this, ObjectID.SPIRALSTAIRS, new WorldPoint(2614, 3868, 0), "Go upstairs in Etceteria castle, east of Miscellania.");
		talkToSigridDip1 = new NpcStep(this, NpcID.MISC_QUEEN_SIGRID, new WorldPoint(2612, 3875, 1), "Talk to Queen Sigrid in Etceteria castle.");
		talkToSigridDip1.addSubSteps(goUpEtcDip1);

		goDownEtcDip1 = new ObjectStep(this, ObjectID.SPIRALSTAIRSTOP, new WorldPoint(2614, 3867, 1), "Return to King Vargas.");
		goUpMiscDip1 = new ObjectStep(this, ObjectID.SPIRALSTAIRS_WOODEN, new WorldPoint(2506, 3849, 0), "Return to King Vargas.");
		talkToVargasDip1 = new NpcStep(this, NpcID.MISC_KING_VARGAS, new WorldPoint(2501, 3860, 1), "Talk to King Vargas.");
		talkToVargasDip1.addSubSteps(goDownEtcDip1, goUpMiscDip1);

		goDownMiscDip1 = new ObjectStep(this, ObjectID.SPIRALSTAIRSMIDDLE_WOODEN, new WorldPoint(2506, 3849, 1), "Go downstairs then return to Queen Sigrid.");
		goDownMiscDip1.addDialogStep("Climb down the stairs.");
		goUpEtcDip2 = new ObjectStep(this, ObjectID.SPIRALSTAIRS, new WorldPoint(2614, 3868, 0), "Return to Queen Sigrid.");
		talkToSigridDip2 = new NpcStep(this, NpcID.MISC_QUEEN_SIGRID, new WorldPoint(2612, 3875, 1), "Talk to Queen Sigrid.");
		talkToSigridDip2.addSubSteps(goDownMiscDip1, goUpEtcDip2);

		goDownEtcDip2 = new ObjectStep(this, ObjectID.SPIRALSTAIRSTOP, new WorldPoint(2614, 3867, 1), "Go talk to Prince Brand in Miscellania castle.");
		goUpMiscDip2 = new ObjectStep(this, ObjectID.SPIRALSTAIRS_WOODEN, new WorldPoint(2506, 3849, 0), "Go talk to Prince Brand in Miscellania castle.");
		talkToBrandDip = new NpcStep(this, NpcID.MISC_PRINCE_BRAND, new WorldPoint(2502, 3852, 1), "Talk to Prince Brand in Miscellania castle.");
		getAnotherAwfulAnthem = new NpcStep(this, NpcID.MISC_PRINCE_BRAND, new WorldPoint(2502, 3852, 1), "Talk to Prince Brand in Miscellania castle for another Awful Anthem.", awfulAnthem);
		talkToBrandDip.addSubSteps(goDownEtcDip2, goUpMiscDip2, getAnotherAwfulAnthem);

		talkToGhrimDip = new NpcStep(this, NpcID.MISC_ADVISOR_GHRIM_1OP, new WorldPoint(2499, 3857, 1), "Talk to Advisor Ghrim.");
		talkToGhrimDip.addDialogStep("How do I make peace with Etceteria?");

		goDownMiscDip2 = new ObjectStep(this, ObjectID.SPIRALSTAIRSMIDDLE_WOODEN, new WorldPoint(2506, 3849, 1), "Go downstairs and return to Queen Sigrid.");
		goDownMiscDip2.addDialogStep("Climb down the stairs.");
		goUpEtcDip3 = new ObjectStep(this, ObjectID.SPIRALSTAIRS, new WorldPoint(2614, 3868, 0), "Return to Queen Sigrid.");
		talkToSigridDip3 = new NpcStep(this, NpcID.MISC_QUEEN_SIGRID, new WorldPoint(2612, 3875, 1), "Return to Queen Sigrid in Etceteria castle.");
		talkToSigridDip3.addSubSteps(goDownMiscDip2, goUpEtcDip3);

		goDownEtcDip3 = new ObjectStep(this, ObjectID.SPIRALSTAIRSTOP, new WorldPoint(2614, 3867, 1), "Return to King Vargas.");
		goUpMiscDip3 = new ObjectStep(this, ObjectID.SPIRALSTAIRS_WOODEN, new WorldPoint(2506, 3849, 0), "Return to King Vargas.");
		talkToVargasDip2 = new NpcStep(this, NpcID.MISC_KING_VARGAS, new WorldPoint(2501, 3860, 1), "Return to King Vargas.");
		talkToVargasDip2.addSubSteps(goDownEtcDip3, goUpMiscDip3);

		goDownMiscDip3 = new ObjectStep(this, ObjectID.SPIRALSTAIRSMIDDLE_WOODEN, new WorldPoint(2506, 3849, 1), "Go downstairs then go talk to Derrik in the north of Miscellania.", ironBar);
		goDownMiscDip3.addDialogStep("Climb down the stairs.");
		talkToDerrik = new NpcStep(this, NpcID.MISC_SMITHY, new WorldPoint(2550, 3895, 0), "Talk to Derrik in the north of Miscellania.", ironBar);
		talkToDerrik.addSubSteps(goDownMiscDip3);
		talkToDerrik.addDialogStep("I have a slightly strange request...");

		makePen = new DetailedQuestStep(this, "Use the giant nib on some logs. Cut a nearby evergreen if you don't have logs.", giantNib, logs);

		goUpMiscDip4 = new ObjectStep(this, ObjectID.SPIRALSTAIRS_WOODEN, new WorldPoint(2506, 3849, 0), "Return to King Vargas with the giant pen.", giantPen);
		giveVargasPen = new NpcStep(this, NpcID.MISC_KING_VARGAS, new WorldPoint(2501, 3860, 1), "Talk to King Vargas with the giant pen.", giantPen);
		giveVargasPen.addSubSteps(goUpMiscDip4);

		goUpMiscForSupport = new ObjectStep(this, ObjectID.SPIRALSTAIRS_WOODEN, new WorldPoint(2506, 3849, 0), "Return to King Vargas to finish the quest.");
		goDownMiscForSupport = new ObjectStep(this, ObjectID.SPIRALSTAIRSMIDDLE_WOODEN, new WorldPoint(2506, 3849, 1), "Go downstairs and get support from the subjects.");
		goDownMiscForSupport.addDialogStep("Climb down the stairs.");

		String getSupport = "Reach 75% support. You can do any of the following with your current levels: ";
		String rakePatch = "Rake the farming patches. ";
		String mineCoal = "Mining coal. ";
		String cuttingMaples = "Cutting maple trees. ";
		String fishing = "Fish tuna with a harpoon or lobsters with a lobster pot.";

		if (client.getRealSkillLevel(Skill.FARMING) >= 10)
		{
			getSupport += rakePatch;
		}
		if (client.getRealSkillLevel(Skill.MINING) >= 30)
		{
			getSupport += mineCoal;
		}
		if (client.getRealSkillLevel(Skill.WOODCUTTING) >= 45)
		{
			getSupport += cuttingMaples;
		}
		getSupport += fishing;

		get75Support = new DetailedQuestStep(this, getSupport, axe, pickaxe, rake, harpoon, lobsterPot);
		get75Support.addSubSteps(goDownMiscForSupport);
		finishQuest = new NpcStep(this, NpcID.MISC_KING_VARGAS, new WorldPoint(2501, 3860, 1), "Talk to King Vargas to finish the quest.");
		finishQuest.addSubSteps(goUpMiscForSupport);
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		ArrayList<ItemRequirement> reqs = new ArrayList<>();
		reqs.add(ironBar);
		reqs.add(logs);
		reqs.add(ring);
		reqs.add(flowers);

		reqs.add(bow);
		reqs.add(cake);
		reqs.add(reputationItems);
		return reqs;
	}

	@Override
	public List<String> getNotes()
	{
		ArrayList<String> reqs = new ArrayList<>();
		reqs.add("Currently this helper only shows you how to marry Astrid. If you'd like to be friends with her or choose to be with Brand, " +
			"you can either follow an external guide for now, or simply talk to King Vargas after the quest to change.");
		return reqs;
	}

	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		ArrayList<ItemRequirement> reqs = new ArrayList<>();
		reqs.add(dramenStaff);
		reqs.add(runRestoreItems);
		return reqs;
	}

	@Override
	public List<Requirement> getGeneralRequirements()
	{
		ArrayList<Requirement> req = new ArrayList<>();
		req.add(new QuestRequirement(QuestHelperQuest.HEROES_QUEST, QuestState.FINISHED));
		req.add(new QuestRequirement(QuestHelperQuest.THE_FREMENNIK_TRIALS, QuestState.FINISHED));
		req.add(new SkillRequirement(Skill.WOODCUTTING, 45, false, "45 Woodcutting (or any of the other skill requirements)"));
		req.add(new SkillRequirement(Skill.FARMING, 10, false, "10 Farming (or any of the other skill requirements)"));
		req.add(new SkillRequirement(Skill.MINING, 30, false, "30 Mining (or any of the other skill requirements)"));
		req.add(new SkillRequirement(Skill.SMITHING, 35, false, "35 Smithing (or any of the other skill requirements)"));
		return req;
	}

	@Override
	public QuestPointReward getQuestPointReward()
	{
		return new QuestPointReward(1);
	}

	@Override
	public List<UnlockReward> getUnlockRewards()
	{
		return Arrays.asList(
				new UnlockReward("Ability to manage Miscellania."),
				new UnlockReward("Ability to teleport to Miscellania with the Ring of Wealth."));
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();

		allSteps.add(new PanelDetails("Talk to King Vargas", Arrays.asList(travelToMisc, getFlowers,
			talkToVargas), flowers, bow, cake, ring, ironBar, logs, reputationItems));

		PanelDetails astridPanel = new PanelDetails("Win over Astrid",
			Arrays.asList(goUpstairsToAstrid, talkAstrid1, giveFlowersToAstrid, danceForAstrid, talkAstrid2,
				giveBowToAstrid, talkAstrid3, blowKissToAstrid, useRingOnAstrid));

		PanelDetails brandPanel = new PanelDetails("Win over Brand",
			Arrays.asList(goUpstairsToBrand, talkBrand1, giveFlowersToBrand, clapForBrand, talkBrand2,
				giveCakeToBrand, talkBrand3, blowKissToBrand, useRingOnBrand));


		allSteps.add(astridPanel);
		allSteps.add(brandPanel);

		allSteps.add(new PanelDetails("Establish peace",
			Arrays.asList(talkToSigridDip1, talkToVargasDip1, talkToSigridDip2, talkToBrandDip, talkToGhrimDip, talkToSigridDip3, talkToVargasDip2,
				talkToDerrik, makePen, giveVargasPen)));

		allSteps.add(new PanelDetails("Get support", Arrays.asList(get75Support, finishQuest), reputationItems));
		return allSteps;
	}
}
