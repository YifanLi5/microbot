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
package net.runelite.client.plugins.microbot.questhelper.helpers.quests.biohazard;

import net.runelite.client.plugins.microbot.questhelper.collections.ItemCollections;
import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.BasicQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.QuestUtil;
import net.runelite.client.plugins.microbot.questhelper.questinfo.QuestHelperQuest;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.Conditions;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.ObjectCondition;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirements;
import net.runelite.client.plugins.microbot.questhelper.requirements.quest.QuestRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.LogicType;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.ExperienceReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.QuestPointReward;
import net.runelite.client.plugins.microbot.questhelper.steps.*;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;

import java.util.*;

public class Biohazard extends BasicQuestHelper
{
	//Items Required
	ItemRequirement gasMask, birdFeed, birdCage, rottenApple, medicalGown, key, distillator, plagueSample, ethenea, liquidHoney, sulphuricBroline,
		touchPaper, priestGownTop, priestGownBottom, priestGownBottomEquipped, priestGownTopEquipped, medicalGownEquipped,
		birdCageHighlighted;

	//Items Recommended
	ItemRequirement teleportVarrock, teleportArdougne, teleportRimmington, coins;

	Requirement inMournerBackyard, inWestArdougne, inMournerBuilding, upstairsInMournerBuilding, inVarrockSouthEast,
		hasPriestSet, isUpstairsArdougneCastle, hasChemicals;

	QuestStep talkToElena, talkToJerico, getBirdFeed, getBirdFeed2, getPigeonCage, investigateWatchtower, clickPigeonCage, talkToOmartAgain,
		talkToOmartToReturnToWest, talkToKilron, enterBackyardOfHeadquarters, pickupRottenApple, useRottenAppleOnCauldron, searchSarahsCupboard,
		searchSarahsCupboard2, enterMournerHeadquarters, goUpstairsInMournerBuilding, killMourner, searchCrateForDistillator,
		goBackDownstairsInMournersHeadquarters, talkToElenaWithDistillator, talkToTheChemist, goToVarrock, vinciVarrock, chancyVarrock, hopsVarrock,
		talkToAsyff, talkToGuidor, returnToElenaAfterSampling, informTheKing, informTheKingGoUpstairs;

	GiveIngredientsToHelpersStep giveChemicals;

	//Zones
	Zone westArdougne1, westArdougne2, westArdougne3, mournerBackyard, mournerBuilding1, mournerBuilding2, mournersBuildingUpstairs, varrockSouthEast, upstairsArdougneCastle;

	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		initializeRequirements();
		setupConditions();
		setupSteps();
		Map<Integer, QuestStep> steps = new HashMap<>();

		steps.put(0, talkToElena);

		steps.put(1, talkToJerico);

		ConditionalStep prepareADistraction = new ConditionalStep(this, getBirdFeed);
		prepareADistraction.addStep(new Conditions(birdCage, birdFeed), investigateWatchtower);
		prepareADistraction.addStep(birdFeed, getPigeonCage);
		prepareADistraction.addStep(new ObjectCondition(ObjectID.JERICOSCUPBOARDOPEN), getBirdFeed2);
		steps.put(2, prepareADistraction);

		ConditionalStep causeADistraction = new ConditionalStep(this, getPigeonCage);
		causeADistraction.addStep(birdCage, clickPigeonCage);
		steps.put(3, causeADistraction);

		steps.put(4, talkToOmartAgain);

		ConditionalStep poisonFood = new ConditionalStep(this, talkToOmartToReturnToWest);
		poisonFood.addStep(new Conditions(inMournerBackyard, rottenApple), useRottenAppleOnCauldron);
		poisonFood.addStep(inMournerBackyard, pickupRottenApple);
		poisonFood.addStep(inWestArdougne, enterBackyardOfHeadquarters);

		steps.put(5, poisonFood);

		ConditionalStep infiltrateMourners = new ConditionalStep(this, talkToOmartToReturnToWest);
		infiltrateMourners.addStep(new Conditions(key, upstairsInMournerBuilding), searchCrateForDistillator);
		infiltrateMourners.addStep(upstairsInMournerBuilding, killMourner);
		infiltrateMourners.addStep(inMournerBuilding, goUpstairsInMournerBuilding);
		infiltrateMourners.addStep(new Conditions(inWestArdougne, medicalGown), enterMournerHeadquarters);
		infiltrateMourners.addStep(new Conditions(inWestArdougne, new ObjectCondition(ObjectID.BIONURSESCUPBOARDOPEN)), searchSarahsCupboard2);
		infiltrateMourners.addStep(inWestArdougne, searchSarahsCupboard);

		steps.put(6, infiltrateMourners);

		ConditionalStep returnToElenaWithDistillator = new ConditionalStep(this, talkToOmartToReturnToWest);
		returnToElenaWithDistillator.addStep(new Conditions(upstairsInMournerBuilding, distillator), goBackDownstairsInMournersHeadquarters);
		returnToElenaWithDistillator.addStep(new Conditions(distillator, inWestArdougne), talkToKilron);
		returnToElenaWithDistillator.addStep(distillator, talkToElenaWithDistillator);

		returnToElenaWithDistillator.addStep(new Conditions(key, upstairsInMournerBuilding), searchCrateForDistillator);
		returnToElenaWithDistillator.addStep(upstairsInMournerBuilding, killMourner);
		returnToElenaWithDistillator.addStep(inMournerBuilding, goUpstairsInMournerBuilding);
		returnToElenaWithDistillator.addStep(new Conditions(inWestArdougne, medicalGown), enterMournerHeadquarters);
		returnToElenaWithDistillator.addStep(new Conditions(inWestArdougne, new ObjectCondition(ObjectID.BIONURSESCUPBOARDOPEN)), searchSarahsCupboard2);
		returnToElenaWithDistillator.addStep(inWestArdougne, searchSarahsCupboard);

		steps.put(7, returnToElenaWithDistillator);

		steps.put(10, talkToTheChemist);

		ConditionalStep smuggleInChemicals = new ConditionalStep(this, goToVarrock);
		smuggleInChemicals.addStep(new Conditions(inVarrockSouthEast, liquidHoney, ethenea, sulphuricBroline, hasPriestSet), talkToGuidor);
		smuggleInChemicals.addStep(new Conditions(inVarrockSouthEast, liquidHoney, ethenea, sulphuricBroline), talkToAsyff);
		smuggleInChemicals.addStep(new Conditions(inVarrockSouthEast, liquidHoney, ethenea), hopsVarrock);
		smuggleInChemicals.addStep(new Conditions(inVarrockSouthEast, liquidHoney), vinciVarrock);
		smuggleInChemicals.addStep(inVarrockSouthEast, chancyVarrock);
		smuggleInChemicals.addStep(hasChemicals, giveChemicals);

		steps.put(12, smuggleInChemicals);

		steps.put(14, returnToElenaAfterSampling);

		ConditionalStep talkToTheKing = new ConditionalStep(this, informTheKingGoUpstairs);
		talkToTheKing.addStep(isUpstairsArdougneCastle, informTheKing);

		steps.put(15, talkToTheKing);
		// Finishing gives: 72: 0->17, 71: 0->4117, 70: 0->1
		return steps;
	}

	@Override
	protected void setupRequirements()
	{
		gasMask = new ItemRequirement("Gas mask", ItemID.GASMASK, 1, true).isNotConsumed();
		gasMask.setTooltip("You can get another from the cupboard in Edmond's house west of Elena's house.");
		birdCage = new ItemRequirement("Pigeon cage", ItemID.PIGEONS);
		birdCageHighlighted = new ItemRequirement("Pigeon cage", ItemID.PIGEONS);
		birdCageHighlighted.setHighlightInInventory(true);
		birdFeed = new ItemRequirement("Bird feed", ItemID.BIRDFEED);
		rottenApple = new ItemRequirement("Rotten apple", ItemID.ROTTENAPPLES);
		rottenApple.setHighlightInInventory(true);
		medicalGown = new ItemRequirement("Medical gown", ItemID.DOCTOR_GOWN).isNotConsumed();
		medicalGownEquipped = new ItemRequirement("Medical gown", ItemID.DOCTOR_GOWN, 1, true);
		key = new ItemRequirement("Key", ItemID.MOURNERKEYTW);
		distillator = new ItemRequirement("Distillator", ItemID.DISTILLATOR);
		plagueSample = new ItemRequirement("Plague sample", ItemID.PLAGUESAMPLE);
		plagueSample.setTooltip("You can get another from Elena in East Ardougne.");
		ethenea = new ItemRequirement("Ethenea", ItemID.ETHENEA);
		ethenea.setTooltip("You can get another from Elena in East Ardougne.");
		liquidHoney = new ItemRequirement("Liquid honey", ItemID.LIQUID_HONEY);
		liquidHoney.setTooltip("You can get another from Elena in East Ardougne.");
		sulphuricBroline = new ItemRequirement("Sulphuric broline", ItemID.SULPHURIC_BROLINE);
		sulphuricBroline.setTooltip("You can get another from Elena in East Ardougne.");
		touchPaper = new ItemRequirement("Touch paper", ItemID.TOUCH_PAPER);
		touchPaper.setTooltip("You can get more from the Chemist in Rimmington.");
		priestGownBottom = new ItemRequirement("Priest gown (bottom)", ItemID.PRIEST_ROBE).isNotConsumed();
		priestGownTop = new ItemRequirement("Priest gown (top)", ItemID.PRIEST_GOWN).isNotConsumed();
		priestGownBottomEquipped = priestGownBottom.equipped();
		priestGownTopEquipped = priestGownTop.equipped();
		teleportVarrock = new ItemRequirement("Teleport to Varrock", ItemID.POH_TABLET_VARROCKTELEPORT);
		teleportArdougne = new ItemRequirement("Teleport to Ardougne", ItemID.POH_TABLET_ARDOUGNETELEPORT, 3);
		teleportRimmington = new ItemRequirement("Teleport to Rimmington", ItemID.NZONE_TELETAB_RIMMINGTON);
		coins = new ItemRequirement("Coins", ItemCollections.COINS, 30);
	}

	@Override
	protected void setupZones()
	{
		mournerBackyard = new Zone(new WorldPoint(2542, 3328, 0), new WorldPoint(2555, 3333, 0));
		westArdougne1 = new Zone(new WorldPoint(2460, 3279, 0), new WorldPoint(2556, 3334, 2));
		westArdougne2 = new Zone(new WorldPoint(2434, 3305, 0), new WorldPoint(2464, 3323, 2));
		westArdougne3 = new Zone(new WorldPoint(2510, 3265, 0), new WorldPoint(2556, 3280, 2));
		mournerBuilding1 = new Zone(new WorldPoint(2547, 3321, 0), new WorldPoint(2555, 3327, 0));
		mournerBuilding2 = new Zone(new WorldPoint(2542, 3324, 0), new WorldPoint(2546, 3327, 0));
		mournersBuildingUpstairs = new Zone(new WorldPoint(2542, 3321, 1), new WorldPoint(2555, 3327, 1));
		varrockSouthEast = new Zone(new WorldPoint(3265, 3376, 0), new WorldPoint(3287, 3407, 1));
		upstairsArdougneCastle = new Zone(new WorldPoint(2570, 3283, 1), new WorldPoint(2590, 3310, 1));
	}

	public void setupConditions()
	{
		inWestArdougne = new ZoneRequirement(westArdougne1, westArdougne2, westArdougne3);
		inMournerBackyard = new ZoneRequirement(mournerBackyard);

		inMournerBuilding = new ZoneRequirement(mournerBuilding1, mournerBuilding2);
		upstairsInMournerBuilding = new ZoneRequirement(mournersBuildingUpstairs);

		hasChemicals = new Conditions(LogicType.OR, ethenea, liquidHoney, sulphuricBroline);
		inVarrockSouthEast = new ZoneRequirement(varrockSouthEast);
		hasPriestSet = new ItemRequirements(priestGownBottom, priestGownTop);
		isUpstairsArdougneCastle = new ZoneRequirement(upstairsArdougneCastle);
	}

	public void setupSteps()
	{
		talkToElena = new NpcStep(this, NpcID.ELENA2_VIS, new WorldPoint(2592, 3336, 0), "Talk to Elena in her house west of the northern Ardougne bank.");
		talkToElena.addDialogStep("Yes.");

		talkToJerico = new NpcStep(this, NpcID.JERICO, new WorldPoint(2612, 3324, 0), "Talk to Jerico in his house south of the northern Ardougne bank");

		getBirdFeed = new ObjectStep(this, ObjectID.JERICOSCUPBOARDSHUT, new WorldPoint(2612, 3326, 0), "Get birdfeed from the cupboard in Jerico's house.");
		getBirdFeed2 = new ObjectStep(this, ObjectID.JERICOSCUPBOARDOPEN, new WorldPoint(2612, 3326, 0), "Get birdfeed from the cupboard in Jerico's house.");

		getBirdFeed.addSubSteps(getBirdFeed2);

		getPigeonCage = new DetailedQuestStep(this, new WorldPoint(2618, 3325, 0), "Get a pigeon cage from behind Jerico's house.", birdCage, birdFeed);

		investigateWatchtower = new ObjectStep(this, ObjectID.BIOWATCHTOWER_OP, new WorldPoint(2562, 3301, 0), "Investigate the watchtower near the entrance to West Ardougne.", birdFeed, birdCage);

		clickPigeonCage = new DetailedQuestStep(this, new WorldPoint(2562, 3300, 0), "Open the Pigeon cage next to the watchtower.", birdCageHighlighted);

		talkToOmartAgain = new NpcStep(this, NpcID.OMART_VIS, new WorldPoint(2559, 3266, 0), "Talk to Omart to enter West Ardougne.", gasMask);
		talkToOmartAgain.addDialogStep("Okay, lets do it.");
		talkToOmartToReturnToWest = new NpcStep(this, NpcID.OMART_VIS, new WorldPoint(2559, 3266, 0), "Talk to Omart to return to West Ardougne");
		talkToOmartToReturnToWest.addDialogStep("Okay, lets do it.");
		talkToOmartAgain.addSubSteps(talkToOmartToReturnToWest);

		enterBackyardOfHeadquarters = new ObjectStep(this, ObjectID.MOURNERSTEWFENCE, new WorldPoint(2541, 3331, 0), "Squeeze through the fence to enter the Mourner's Headquarters yard in the north east of West Ardougne.");
		pickupRottenApple = new DetailedQuestStep(this, new WorldPoint(2549, 3332, 0), "Pick up the rotten apple in the yard.", rottenApple);
		useRottenAppleOnCauldron = new ObjectStep(this, ObjectID.MOURNERCAULDRON, new WorldPoint(2543, 3332, 0), "Use the rotten apple on the cauldron.", rottenApple);
		useRottenAppleOnCauldron.addIcon(ItemID.ROTTENAPPLES);

		searchSarahsCupboard = new ObjectStep(this, ObjectID.BIONURSESCUPBOARDSHUT, new WorldPoint(2518, 3276, 0), "Search the cupboard in Sarah's house south-west of the West Ardougne church.");
		searchSarahsCupboard2 = new ObjectStep(this, ObjectID.BIONURSESCUPBOARDOPEN, new WorldPoint(2518, 3276, 0), "Search the cupboard in Sarah's house south-west of the West Ardougne church.");
		searchSarahsCupboard.addSubSteps(searchSarahsCupboard2);
		enterMournerHeadquarters = new ObjectStep(this, ObjectID.MOURNERSTEWDOOR, new WorldPoint(2551, 3320, 0), "Enter the Mourners' Headquarters whilst wearing the medical gown.", medicalGownEquipped);

		goUpstairsInMournerBuilding = new ObjectStep(this, ObjectID.SPIRALSTAIRS, new WorldPoint(2543, 3325, 0), "Go upstairs and kill the mourner there.");
		killMourner = new NpcStep(this, NpcID.MOURNERSTEW2_VIS, new WorldPoint(2549, 3325, 1), "Kill the mourner here for a key to the caged area.");
		goUpstairsInMournerBuilding.addSubSteps(killMourner);

		searchCrateForDistillator = new ObjectStep(this, ObjectID.MOURNERCRATEUP, new WorldPoint(2554, 3327, 1), "Search the crate in the caged area for Elena's Distillator.");

		goBackDownstairsInMournersHeadquarters = new ObjectStep(this, ObjectID.SPIRALSTAIRSTOP, new WorldPoint(2543, 3325, 1), "Return to Elena. Go back downstairs or teleport out.");

		talkToKilron = new NpcStep(this, NpcID.KILRON_VIS, new WorldPoint(2556, 3266, 0), "Return to Elena. Talk to Kilron to return back to East Ardougne.");
		talkToKilron.addDialogStep("Yes I do.");

		talkToElenaWithDistillator = new NpcStep(this, NpcID.ELENA2_VIS, new WorldPoint(2592, 3336, 0), "Return to Elena.");
		talkToElenaWithDistillator.addSubSteps(goBackDownstairsInMournersHeadquarters, talkToKilron);

		talkToTheChemist = new NpcStep(this, NpcID.CHEMIST, new WorldPoint(2933, 3210, 0),
			"Take the Plague Sample to the Chemist in Rimmington. You can take a boat from Ardougne Dock to Rimmington for 30gp.", plagueSample, liquidHoney, sulphuricBroline, ethenea);
		talkToTheChemist.addDialogStep("Your quest.");

		giveChemicals = new GiveIngredientsToHelpersStep(this);
		goToVarrock = new DetailedQuestStep(this, new WorldPoint(3270, 3390, 0), "Go speak to Hops, Da Vinci and Chancy in the pub in the south east of Varrock. If you lost any of the chemicals, return to Elena to get more.", plagueSample, touchPaper);
		vinciVarrock = new NpcStep(this, NpcID.ARTIST2, new WorldPoint(3270, 3390, 0), "Talk to Da Vinci for the Ethenea.");
		hopsVarrock = new NpcStep(this, NpcID.DRUNK2, new WorldPoint(3270, 3390, 0), "Talk to Hops for the Sulphuric Broline.");
		chancyVarrock = new NpcStep(this, NpcID.GAMBLER2, new WorldPoint(3270, 3390, 0), "Talk to Chancy for the Liquid honey.");
		goToVarrock.addSubSteps(vinciVarrock, hopsVarrock, chancyVarrock);

		talkToAsyff = new NpcStep(this, NpcID.TAILORP, new WorldPoint(3277, 3397, 0), "Talk to Asyff to get a free priest gown. You can only get the free set once.");
		talkToAsyff.addDialogStep("Do you have a spare Priest Gown?");

		talkToGuidor = new NpcStep(this, NpcID.GUIDOR, new WorldPoint(3284, 3382, 0),
			"Talk to Guidor in his house to the south.",
			priestGownBottomEquipped, priestGownTopEquipped, plagueSample, sulphuricBroline, liquidHoney, ethenea, touchPaper);
		talkToGuidor.addDialogStep("I've come to ask your assistance in stopping a plague.");

		returnToElenaAfterSampling = new NpcStep(this, NpcID.ELENA2_VIS, new WorldPoint(2592, 3336, 0),
			"Return to Elena and inform her that the plague is a hoax.");

		informTheKing = new NpcStep(this, NpcID.KINGLATHAS_VIS, new WorldPoint(2578, 3293, 1), "Tell King Lathas that the Plague is a hoax.");
		informTheKingGoUpstairs = new ObjectStep(this, ObjectID.STAIRS, new WorldPoint(2572, 3296, 0), "Tell King Lathas that the Plague is a hoax.");
		informTheKing.addSubSteps(informTheKingGoUpstairs);
	}

	@Override
	public List<String> getCombatRequirements()
	{
		ArrayList<String> reqs = new ArrayList<>();
		reqs.add("Mourner (level 13)");
		return reqs;
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		ArrayList<ItemRequirement> reqs = new ArrayList<>();
		reqs.add(gasMask);
		return reqs;
	}

	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		ArrayList<ItemRequirement> reqs = new ArrayList<>();
		reqs.add(teleportArdougne);
		reqs.add(teleportRimmington);
		reqs.add(teleportVarrock);
		reqs.add(coins);
		return reqs;
	}

	@Override
	public QuestPointReward getQuestPointReward()
	{
		return new QuestPointReward(3);
	}

	@Override
	public List<ExperienceReward> getExperienceRewards()
	{
		return Collections.singletonList(new ExperienceReward(Skill.THIEVING, 1250));
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();
		allSteps.add(new PanelDetails("Start the quest", Collections.singletonList(talkToElena), gasMask));
		allSteps.add(new PanelDetails("Getting back into West Ardougne",
			Arrays.asList(talkToJerico, getBirdFeed, getPigeonCage, investigateWatchtower, clickPigeonCage, talkToOmartAgain)));
		allSteps.add(new PanelDetails("Getting the Distillator",
			enterBackyardOfHeadquarters, pickupRottenApple, useRottenAppleOnCauldron, searchSarahsCupboard,
			enterMournerHeadquarters, goUpstairsInMournerBuilding, searchCrateForDistillator, talkToElenaWithDistillator));

		List<QuestStep> testingSteps = QuestUtil.toArrayList(talkToTheChemist);
		testingSteps.addAll(giveChemicals.getDisplaySteps());
		testingSteps.addAll(Arrays.asList(goToVarrock, talkToAsyff, talkToGuidor));
		allSteps.add(new PanelDetails("Testing the plague sample", testingSteps, plagueSample, liquidHoney, ethenea, sulphuricBroline, touchPaper));

		allSteps.add(new PanelDetails("Revealing the truth", returnToElenaAfterSampling, informTheKing));
		return allSteps;
	}

	@Override
	public List<Requirement> getGeneralRequirements()
	{
		List<Requirement> requirements = new ArrayList<>();
		requirements.add(new QuestRequirement(QuestHelperQuest.PLAGUE_CITY, QuestState.FINISHED));
		return requirements;
	}
}
