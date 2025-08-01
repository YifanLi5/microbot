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
package net.runelite.client.plugins.microbot.questhelper.helpers.quests.thefremenniktrials;

import net.runelite.client.plugins.microbot.questhelper.bank.banktab.BankSlotIcons;
import net.runelite.client.plugins.microbot.questhelper.collections.ItemCollections;
import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.BasicQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.requirements.ChatMessageRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.Conditions;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.NpcCondition;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.ObjectCondition;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirements;
import net.runelite.client.plugins.microbot.questhelper.requirements.npc.DialogRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.player.SkillRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.runelite.RuneliteRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.LogicType;
import net.runelite.client.plugins.microbot.questhelper.requirements.var.VarbitRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.widget.WidgetTextRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.ExperienceReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.QuestPointReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.UnlockReward;
import net.runelite.client.plugins.microbot.questhelper.steps.*;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;

import java.util.*;

public class TheFremennikTrials extends BasicQuestHelper
{
	//Items Required
	ItemRequirement coins, coins250, coins5000, beer, rawShark, tinderbox, axe, knife, potato, onion, cabbage, branch, lyre, lyreUnstrung,
		petRock, goldenFleece, goldenWool, enchantedLyre, strangeObject, litStrangeObject, alcoholFreeBeer, kegOfBeer, dramenBranch,
		huntersTalisman, chargedHuntersTalisman, promissoryNote, legendaryCocktail, championsToken, warriorsContract, weatherForecast,
		seaFishingMap, unusualFish, customBowString, trackingMap, fiscalStatement, sturdyBoots, ballad, exoticFlower, koscheiGear,
		optionalKoscheiGear, redHerring, woodenDisk, redDiskOld, emptyJug, redDiskNew, stickyRedGoop, emptyBucket, vase, fullBucket, fullJug, vaseLid,
		jug23, jug13, bucket45, bucket35, bucket25, bucket15, filledVase, filledVaseWithLid, vaseWithLidWrong, frozenKey, seersKey, frozenVase;


	Requirement emptySlot4;

	//Items Recommended
	ItemRequirement camelotTeleport, rellekkaTeleports, ringsOfRecoil, combatGear;

	Requirement synced, syncedAll, syncedOlaf, syncedManni, syncedSigmund, inQuestJournal, hasStartedOlaf,
		talkedToLalli, gottenRock, finishedOlafTask, petRockInCauldron, cabbageInCauldron, potatoInCauldron, onionInCauldron,
		cauldronFilledDialog, stewReady,finishedOlafMessage, finishedOlafWidget,talkedToManni,
		isNearPipe, hasPlacedStrangeObject, hasReplacedBeer, finishedManniTask, talkedToManniWidget,
		talkedToManniChat, finishedSigliTask, getFlower, talkedToSailor, talkedToOlafForSigmund,
		talkedToYsra, talkedToBrundtForSigmund, talkedToSigliForSigmund, talkedToSkulgrimenForSigmund, talkedToFishermanForSigmund, talkedToSwensenForSigmund,
		talkedToPeerForSigmund, talkedToThorvaldForSigmund, talkedToManniForSigmund, talkedToThoraForSigmund, finishedSigmundTask,
		inKoscheiRoom, talkedToThorvald, koschei1Near, koschei2Near, koschei3Near, koschei4Near, syncedThorvald,
		finishedThorvaldTask, talkedToSwensen, syncedSwensen, inSwensenRoom1, inSwensenRoom2, inSwensenRoom3, inSwensenRoom4, inSwensenRoom5,
		inSwensenRoom6, inSwensenRoom7, inSwensenArea, inSwensenFinalRoom, finishedSwensenTask, talkedToPeer, finishedPeerTask, isMind, isTree, isLife, isFire,
		isTime, isWind, inPeerEntrance, inPeerUpstairs, inPeerExit, hasSolvedDoor, trapDoorOpen, hasUsedDisk, muralHasDisks,
		hasAnyBucket, hasAnyJug, cupboardOpen, chestOpen, syncedPeer, noRockAskeladdenNearby;

	QuestStep talkToBrundt, talkToOlaf, pickVeg, chopSwayingTree, fletchLyre, talkToLalli,
		talkToAskeladdenForRock, useCabbage, useOnion, usePotato, useRock, talkToLaliAfterStew, spinWool,
		makeLyre, enchantLyre, performMusic, talkToManni, pickUpBeer, getAlcoholFreeBeer, getStrangeObject,
		prepareToUseStrangeObject, useStrangeObject, useStrangeObjectOnPipe, cheatInBeerDrinking, getKegOfBeer,
		useAlcoholFreeOnKeg, talkToSigli, huntDraugen, returnToSigli, talkToSigmund, talkToSailor, talkToOlafForSigmund,
		talkToYsra, talkToBrundtForSigmund, talkToSigliForSigmund, talkToSkulgrimenForSigmund, talkToFishermanForSigmund, talkToSwenesenForSigmund,
		talkToPeerForSigmund, talkToThorvaldForSigmund, talkToManniForSigmund, talkToThoraForSigmund, talkToAskeladdenForSigmund, talkToAskeladdenForSigmund2, bringNoteToThora,
		bringCocktailToManni, bringChampionsTokenToThorvald, bringWarriorsContractToPeer, bringWeatherForecastToSwensen, bringSeaFishingMapToFisherman,
		bringUnusualFishToSkulgrimen, bringCustomBowStringToSigli, bringTrackingMapToBrundt, bringFiscalStatementToYsra, bringSturdyBootsToOlaf, bringBalladToSailor,
		bringExoticFlowerToSigmund, talkToThorvald, goDownLadderToKoschei, waitForKoschei, killKoschei1, killKoschei2, killKoschei3, killKoschei4, talkToSwensen, goDownLadderSwensen,
		swensen1South, swensen2West, swensen3East, swensen4North, swensen5South, swensen6East, swensen7North, swensenUpLadder, resetSwensen, talkToPeer, enterPeerHouse, inputMind,
		inputTree, inputLife, inputFire, inputTime, inputWind, goUpEntranceLadderPeer, enterCode, searchBookcase, cookHerring, searchUnicorn, searchBull, searchChest1, searchChest2, searchCupboard1,
		searchCupboard2, useGoopOnDisk, openTrapDoorAndGoDown1, goDown1, useDiskAnyOnMural, useDiskOldOnMural, useDiskNewOnMural, useBucketOnTap1, useBucketOnJug1, useJugOnDrain1, useBucketOnJug2, useBucketOnTap2, useBucketOnJug3,
		useBucketOnScale, goBackUpstairs, goUpstairsWithVaseLid, emptyJugAndBucket, fillVase, useVaseOnTable, useLidOnVase, takeLidOff, useFrozenKeyOnRange, goDownstairsWithKey, goDownstairsWithKey2,
		leaveSeersHouse, warmFrozenVase, finishQuest;

	ConditionalStep olafTask, sigliTask, manniTask, sigmundTask, thorvaldTask, swensenTask, peerTask;

	//Zones
	Zone nearPipe, koscheiRoom, swensenRoom1, swensenRoom2, swensenRoom3, swensenRoom4, swensenRoom5, swensenRoom6, swensenRoom7, swensenFinalRoom, swensenArea, peerEntrance, peerUpstairs, peerExit;

	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		initializeRequirements();
		setupConditions();
		setupSteps();
		Map<Integer, QuestStep> steps = new HashMap<>();

		steps.put(0, talkToBrundt);

		olafTask = new ConditionalStep(this, talkToOlaf);
		olafTask.addStep(enchantedLyre.alsoCheckBank(questBank), performMusic);
		olafTask.addStep(new Conditions(hasStartedOlaf, lyre.alsoCheckBank(questBank)), enchantLyre);
		olafTask.addStep(new Conditions(goldenWool, lyreUnstrung), makeLyre);
		olafTask.addStep(new Conditions(goldenFleece, lyreUnstrung), spinWool);
		olafTask.addStep(new Conditions(goldenFleece, branch), fletchLyre);
		olafTask.addStep(goldenFleece, chopSwayingTree);
		olafTask.addStep(new Conditions(gottenRock, stewReady), talkToLaliAfterStew);
		olafTask.addStep(new Conditions(gottenRock, onionInCauldron, cabbageInCauldron, potatoInCauldron), useRock);
		olafTask.addStep(new Conditions(gottenRock, onionInCauldron, cabbageInCauldron), usePotato);
		olafTask.addStep(new Conditions(gottenRock, onionInCauldron), useCabbage);
		olafTask.addStep(gottenRock, useOnion);
		olafTask.addStep(new Conditions(hasStartedOlaf, talkedToLalli), talkToAskeladdenForRock);
		olafTask.addStep(hasStartedOlaf, talkToLalli);
		olafTask.setLockingCondition(finishedOlafTask);

		manniTask = new ConditionalStep(this, talkToManni);
		manniTask.addStep(new Conditions(hasReplacedBeer, kegOfBeer), cheatInBeerDrinking);
		manniTask.addStep(new Conditions(hasReplacedBeer), getKegOfBeer);
		manniTask.addStep(new Conditions(hasPlacedStrangeObject, alcoholFreeBeer, kegOfBeer), useAlcoholFreeOnKeg);
		manniTask.addStep(new Conditions(hasPlacedStrangeObject, alcoholFreeBeer), getKegOfBeer);
		manniTask.addStep(new Conditions(hasPlacedStrangeObject), getAlcoholFreeBeer);
		manniTask.addStep(new Conditions(talkedToManni, litStrangeObject, alcoholFreeBeer, isNearPipe), useStrangeObjectOnPipe);
		manniTask.addStep(new Conditions(talkedToManni, strangeObject.alsoCheckBank(questBank), alcoholFreeBeer, isNearPipe), useStrangeObject);
		manniTask.addStep(new Conditions(talkedToManni, strangeObject.alsoCheckBank(questBank), alcoholFreeBeer), prepareToUseStrangeObject);
		manniTask.addStep(new Conditions(talkedToManni, strangeObject.alsoCheckBank(questBank)), getAlcoholFreeBeer);
		manniTask.addStep(new Conditions(talkedToManni, beer), getStrangeObject);
		manniTask.addStep(talkedToManni, pickUpBeer);
		manniTask.setLockingCondition(finishedManniTask);

		sigliTask = new ConditionalStep(this, talkToSigli);
		sigliTask.addStep(chargedHuntersTalisman, returnToSigli);
		sigliTask.addStep(huntersTalisman, huntDraugen);
		sigliTask.setLockingCondition(finishedSigliTask);

		sigmundTask = new ConditionalStep(this, talkToSigmund);
		sigmundTask.addStep(exoticFlower, bringExoticFlowerToSigmund);
		sigmundTask.addStep(ballad, bringBalladToSailor);
		sigmundTask.addStep(sturdyBoots, bringSturdyBootsToOlaf);
		sigmundTask.addStep(fiscalStatement, bringFiscalStatementToYsra);
		sigmundTask.addStep(trackingMap, bringTrackingMapToBrundt);
		sigmundTask.addStep(customBowString, bringCustomBowStringToSigli);
		sigmundTask.addStep(unusualFish, bringUnusualFishToSkulgrimen);
		sigmundTask.addStep(seaFishingMap, bringSeaFishingMapToFisherman);
		sigmundTask.addStep(weatherForecast, bringWeatherForecastToSwensen);
		sigmundTask.addStep(warriorsContract, bringWarriorsContractToPeer);
		sigmundTask.addStep(championsToken, bringChampionsTokenToThorvald);
		sigmundTask.addStep(legendaryCocktail, bringCocktailToManni);
		sigmundTask.addStep(promissoryNote, bringNoteToThora);

		sigmundTask.addStep(new Conditions(talkedToThoraForSigmund, noRockAskeladdenNearby), talkToAskeladdenForSigmund2);
		sigmundTask.addStep(talkedToThoraForSigmund, talkToAskeladdenForSigmund);
		sigmundTask.addStep(talkedToManniForSigmund, talkToThoraForSigmund);
		sigmundTask.addStep(talkedToThorvaldForSigmund, talkToManniForSigmund);
		sigmundTask.addStep(talkedToPeerForSigmund, talkToThorvaldForSigmund);
		sigmundTask.addStep(talkedToSwensenForSigmund, talkToPeerForSigmund);
		sigmundTask.addStep(talkedToFishermanForSigmund, talkToSwenesenForSigmund);
		sigmundTask.addStep(talkedToSkulgrimenForSigmund, talkToFishermanForSigmund);
		sigmundTask.addStep(talkedToSigliForSigmund, talkToSkulgrimenForSigmund);
		sigmundTask.addStep(talkedToBrundtForSigmund, talkToSigliForSigmund);
		sigmundTask.addStep(talkedToYsra, talkToBrundtForSigmund);
		sigmundTask.addStep(talkedToOlafForSigmund, talkToYsra);
		sigmundTask.addStep(talkedToSailor, talkToOlafForSigmund);
		sigmundTask.addStep(getFlower, talkToSailor);
		sigmundTask.setLockingCondition(finishedSigmundTask);

		thorvaldTask = new ConditionalStep(this, talkToThorvald);
		thorvaldTask.addStep(koschei4Near, killKoschei4);
		thorvaldTask.addStep(koschei3Near, killKoschei3);
		thorvaldTask.addStep(koschei2Near, killKoschei2);
		thorvaldTask.addStep(koschei1Near, killKoschei1);
		thorvaldTask.addStep(inKoscheiRoom, waitForKoschei);
		thorvaldTask.addStep(talkedToThorvald, goDownLadderToKoschei);
		thorvaldTask.setLockingCondition(finishedThorvaldTask);

		swensenTask = new ConditionalStep(this, talkToSwensen);
		swensenTask.addStep(inSwensenFinalRoom, swensenUpLadder);
		swensenTask.addStep(inSwensenRoom7, swensen7North);
		swensenTask.addStep(inSwensenRoom6, swensen6East);
		swensenTask.addStep(inSwensenRoom5, swensen5South);
		swensenTask.addStep(inSwensenRoom4, swensen4North);
		swensenTask.addStep(inSwensenRoom3, swensen3East);
		swensenTask.addStep(inSwensenRoom2, swensen2West);
		swensenTask.addStep(inSwensenRoom1, swensen1South);
		swensenTask.addStep(inSwensenArea, resetSwensen);
		swensenTask.addStep(talkedToSwensen, goDownLadderSwensen);
		swensenTask.setLockingCondition(finishedSwensenTask);

		peerTask = new ConditionalStep(this, talkToPeer);
		peerTask.addStep(new Conditions(inPeerExit, seersKey), leaveSeersHouse);
		peerTask.addStep(new Conditions(inPeerUpstairs, seersKey, trapDoorOpen), goDownstairsWithKey2);
		peerTask.addStep(new Conditions(inPeerUpstairs, seersKey), goDownstairsWithKey);
		peerTask.addStep(new Conditions(inPeerUpstairs, frozenKey), useFrozenKeyOnRange);
		peerTask.addStep(new Conditions(inPeerUpstairs, filledVaseWithLid), useVaseOnTable);
		peerTask.addStep(new Conditions(inPeerUpstairs, vaseLid, filledVase), useLidOnVase);
		peerTask.addStep(new Conditions(inPeerUpstairs, vaseLid, vase), fillVase);
		peerTask.addStep(new Conditions(inPeerUpstairs, vaseWithLidWrong), takeLidOff);
		peerTask.addStep(new Conditions(inPeerUpstairs, vaseLid, bucket45), useBucketOnScale);
		peerTask.addStep(new Conditions(inPeerUpstairs, vaseLid, fullBucket, jug23), useBucketOnJug3);
		peerTask.addStep(new Conditions(inPeerUpstairs, vaseLid, emptyBucket, jug23), useBucketOnTap2);
		peerTask.addStep(new Conditions(inPeerUpstairs, vaseLid, bucket25, emptyJug), useBucketOnJug2);
		peerTask.addStep(new Conditions(inPeerUpstairs, vaseLid, bucket25, fullJug), useJugOnDrain1);
		peerTask.addStep(new Conditions(inPeerUpstairs, vaseLid, fullBucket, emptyJug), useBucketOnJug1);
		peerTask.addStep(new Conditions(inPeerUpstairs, vaseLid, emptyBucket, emptyJug), useBucketOnTap1);
		peerTask.addStep(new Conditions(inPeerUpstairs, vaseLid, hasAnyBucket, hasAnyJug), emptyJugAndBucket);
		peerTask.addStep(new Conditions(inPeerUpstairs, vaseLid, hasAnyBucket, chestOpen), searchChest2);
		peerTask.addStep(new Conditions(inPeerUpstairs, vaseLid, hasAnyBucket), searchChest1);
		peerTask.addStep(new Conditions(inPeerUpstairs, vaseLid, cupboardOpen), searchCupboard2);
		peerTask.addStep(new Conditions(inPeerUpstairs, vaseLid), searchCupboard1);
		peerTask.addStep(new Conditions(inPeerExit, vaseLid), goUpstairsWithVaseLid);
		peerTask.addStep(new Conditions(inPeerExit, redDiskNew, hasUsedDisk), useDiskNewOnMural);
		peerTask.addStep(new Conditions(inPeerExit, redDiskOld, hasUsedDisk), useDiskOldOnMural);
		peerTask.addStep(new Conditions(inPeerExit, redDiskOld, redDiskNew), useDiskAnyOnMural);
		peerTask.addStep(new Conditions(inPeerUpstairs, redDiskOld, redDiskNew, trapDoorOpen), goDown1);
		peerTask.addStep(new Conditions(inPeerUpstairs, redDiskOld, redDiskNew), openTrapDoorAndGoDown1);
		peerTask.addStep(new Conditions(inPeerUpstairs, stickyRedGoop, woodenDisk, redDiskOld), useGoopOnDisk);
		peerTask.addStep(new Conditions(inPeerUpstairs, redHerring, woodenDisk, redDiskOld), cookHerring);
		peerTask.addStep(new Conditions(inPeerUpstairs, new Conditions(LogicType.OR, redDiskNew,
			new Conditions(redHerring, woodenDisk))), searchUnicorn);
		peerTask.addStep(new Conditions(inPeerUpstairs, new Conditions(LogicType.OR, stickyRedGoop, redHerring)),
			searchBull);
		peerTask.addStep(inPeerUpstairs, searchBookcase);
		peerTask.addStep(inPeerExit, goBackUpstairs);
		peerTask.addStep(new Conditions(LogicType.OR, inPeerEntrance, hasSolvedDoor), goUpEntranceLadderPeer);
		peerTask.addStep(isWind, inputWind);
		peerTask.addStep(isTime, inputTime);
		peerTask.addStep(isFire, inputFire);
		peerTask.addStep(isLife, inputLife);
		peerTask.addStep(isTree, inputTree);
		peerTask.addStep(isMind, inputMind);
		peerTask.addStep(talkedToPeer, enterPeerHouse);
		peerTask.setLockingCondition(finishedPeerTask);

		QuestSyncStep resyncStep = new QuestSyncStep(this, getQuest(),
			"Please open the quest's Quest Journal to sync your state.");
		ConditionalStep resyncCondition = new ConditionalStep(this, resyncStep);
		resyncCondition.setLockingCondition(syncedAll);

		ConditionalStep trials = new ConditionalStep(this, resyncCondition);
		trials.addStep(new Conditions(syncedPeer, finishedOlafTask, finishedManniTask, finishedSigliTask, finishedSigmundTask, finishedThorvaldTask, finishedSwensenTask), peerTask);
		trials.addStep(new Conditions(syncedSwensen, finishedOlafTask, finishedManniTask, finishedSigliTask, finishedSigmundTask, finishedThorvaldTask), swensenTask);
		trials.addStep(new Conditions(syncedThorvald, finishedOlafTask, finishedManniTask, finishedSigliTask, finishedSigmundTask), thorvaldTask);
		trials.addStep(new Conditions(syncedSigmund, finishedOlafTask, finishedManniTask, finishedSigliTask), sigmundTask);
		trials.addStep(new Conditions(finishedOlafTask, finishedManniTask), sigliTask);
		trials.addStep(new Conditions(syncedManni, finishedOlafTask), manniTask);
		trials.addStep(syncedOlaf, olafTask);
		steps.put(1, trials);
		steps.put(2, trials);
		steps.put(3, trials);
		steps.put(4, trials);
		steps.put(5, trials);
		steps.put(6, trials);
		steps.put(7, trials);
		steps.put(8, finishQuest);
		return steps;
	}

	@Override
	protected void setupRequirements()
	{
		coins = new ItemRequirement("Coins", ItemCollections.COINS, 5250);
		coins250 = new ItemRequirement("Coins", ItemCollections.COINS, 250);
		coins5000 = new ItemRequirement("Coins", ItemCollections.COINS, 5000);
		beer = new ItemRequirement(true, "Beer", ItemID.BEER);
		beer.addAlternates(ItemID.VIKING_TANKARD_FULL);
		rawShark = new ItemRequirement("Raw shark, manta ray or sea turtle", ItemID.RAW_SHARK);
		rawShark.addAlternates(ItemID.RAW_MANTARAY, ItemID.RAW_SEATURTLE);

		dramenBranch = new ItemRequirement("Dramen branch for Koschei", ItemID.DRAMEN_BRANCH);
		ringsOfRecoil = new ItemRequirement("Rings of recoil for Koschei", ItemID.RING_OF_RECOIL, -1);
		camelotTeleport = new ItemRequirement("Camelot teleport", ItemID.POH_TABLET_CAMELOTTELEPORT);
		rellekkaTeleports = new ItemRequirement("Teleports to Rellekka", ItemID.NZONE_TELETAB_RELLEKKA);

		tinderbox = new ItemRequirement("Tinderbox", ItemID.TINDERBOX).isNotConsumed();
		axe = new ItemRequirement("Any axe", ItemCollections.AXES).isNotConsumed();
		knife = new ItemRequirement("Knife", ItemID.KNIFE).isNotConsumed();
		potato = new ItemRequirement("Potato", ItemID.POTATO);
		onion = new ItemRequirement("Onion", ItemID.ONION);
		cabbage = new ItemRequirement("Cabbage", ItemID.CABBAGE);
		branch = new ItemRequirement("Branch", ItemID.VIKING_MUSICAL_TREE_BRANCH);
		lyreUnstrung = new ItemRequirement("Unstrung lyre", ItemID.VIKING_UNSTRUNG_LYRE);
		lyre = new ItemRequirement("Lyre", ItemID.VIKING_STRUNG_LYRE);
		petRock = new ItemRequirement("Pet rock", ItemID.VT_USELESS_ROCK).isNotConsumed();
		petRock.setTooltip("You can get another from Askeladden");

		emptySlot4 = new ItemRequirement("4 empty inventory slots", -1, 4);

		goldenWool = new ItemRequirement("Golden wool", ItemID.VIKING_GOLDEN_WOOL);
		goldenFleece = new ItemRequirement("Golden fleece", ItemID.VIKING_GOLDEN_FLEECE);
		goldenFleece.addAlternates(ItemID.VIKING_GOLDEN_WOOL);

		enchantedLyre = new ItemRequirement("Enchanted lyre", ItemID.VIKING_ENCHANTED_STRUNG_LYRE);
		strangeObject = new ItemRequirement("Strange object", ItemID.VIKING_FIRECRACKER);
		strangeObject.addAlternates(ItemID.VIKING_FIRECRACKER_LIT);
		litStrangeObject = new ItemRequirement("Lit strange object", ItemID.VIKING_FIRECRACKER_LIT);
		alcoholFreeBeer = new ItemRequirement("Low alcohol keg", ItemID.VIKING_LOW_ALCAHOL_BEERKEG);
		kegOfBeer = new ItemRequirement("Keg of beer", ItemID.VIKING_BEERKEG);

		combatGear = new ItemRequirement("Combat gear + food", -1, -1).isNotConsumed();
		combatGear.setDisplayItemId(BankSlotIcons.getCombatGear());
		huntersTalisman = new ItemRequirement("Hunters' talisman", ItemID.VIKING_DRAUGEN_TALISMAN_UNCHARGED);
		chargedHuntersTalisman = new ItemRequirement("Hunters' talisman", ItemID.VIKING_DRAUGEN_TALISMAN);
		promissoryNote = new ItemRequirement("Promissory note", ItemID.VIKING_PROMISSARY_NOTE2);
		legendaryCocktail = new ItemRequirement("Legendary cocktail", ItemID.VIKING_LEGENDARY_COCKTAIL);
		championsToken = new ItemRequirement("Champions token", ItemID.VIKING_CHAMPION_TOKEN);
		warriorsContract = new ItemRequirement("Warriors' contract", ItemID.VIKING_PROMISSARY_NOTE3);
		weatherForecast = new ItemRequirement("Weather forecast", ItemID.VIKING_WEATHER_FORECAST);
		seaFishingMap = new ItemRequirement("Sea fishing map", ItemID.VIKING_ANOTHER_MAP);
		unusualFish = new ItemRequirement("Unusual fish", ItemID.VIKING_UNIQUE_FISH);
		customBowString = new ItemRequirement("Custom bow string", ItemID.VIKING_BOWSTRING);
		trackingMap = new ItemRequirement("Tracking map", ItemID.VIKING_MAP_TO_HUNTING_GROUNDS);
		fiscalStatement = new ItemRequirement("Fiscal statement", ItemID.VIKING_PROMISSARY_NOTE);
		sturdyBoots = new ItemRequirement("Sturdy boots", ItemID.VIKING_NEW_BOOTS);
		ballad = new ItemRequirement("Fremennik ballad", ItemID.VIKING_SONG);
		exoticFlower = new ItemRequirement("Exotic flower", ItemID.VIKING_RARE_FLOWER);
		optionalKoscheiGear = new ItemRequirements(LogicType.AND, "Optionally a Dramen branch + knife to make a dramen staff once inside Koschei's arena",
			new ItemRequirement("Knife", ItemID.KNIFE), new ItemRequirement("Dramen branch", ItemID.DRAMEN_BRANCH));
		koscheiGear = new ItemRequirement("Nothing except for food, potions, and rings of recoil", -1, -1).isNotConsumed();
		koscheiGear.setDisplayItemId(ItemID._100GUIDE_GUIDECAKE);
		redHerring = new ItemRequirement(true, "Red herring", ItemID.VIKING_RED_HERRING);
		woodenDisk = new ItemRequirement(true, "Wooden disk", ItemID.VIKING_UNCOLOURED_WOODEN_COIN);
		redDiskOld = new ItemRequirement(true, "Old red disk", ItemID.VIKING_RED_WOODEN_COIN_OLD);
		redDiskNew = new ItemRequirement(true, "Red disk", ItemID.VIKING_RED_WOODEN_COIN);
		stickyRedGoop = new ItemRequirement(true, "Sticky red goop", ItemID.VIKING_RED_SPLAT);
		vase = new ItemRequirement(true, "Vase", ItemID.VIKING_AIRTIGHT_VASE);
		vaseLid = new ItemRequirement(true, "Vase lid", ItemID.VIKING_VASE_LID);
		emptyJug = new ItemRequirement(true, "Empty jug", ItemID.VIKING_JUG_EMPTY);
		fullJug = new ItemRequirement(true, "Full jug", ItemID.VIKING_JUG_3);
		jug13 = new ItemRequirement(true, "1/3rds full jug", ItemID.VIKING_JUG_1);
		jug23 = new ItemRequirement(true, "2/3rds full jug", ItemID.VIKING_JUG_2);
		emptyBucket = new ItemRequirement(true, "Empty bucket", ItemID.VIKING_BUCKET_EMPTY);
		fullBucket = new ItemRequirement(true, "Full bucket", ItemID.VIKING_BUCKET_5);
		bucket45 = new ItemRequirement(true, "4/5ths full bucket", ItemID.VIKING_BUCKET_4);
		bucket35 = new ItemRequirement(true, "3/5ths full bucket", ItemID.VIKING_BUCKET_3);
		bucket25 = new ItemRequirement(true, "2/5ths full bucket", ItemID.VIKING_BUCKET_2);
		bucket15 = new ItemRequirement(true, "1/5ths full bucket", ItemID.VIKING_BUCKET_1);
		filledVase = new ItemRequirement(true, "Vase of water", ItemID.VIKING_AIRTIGHT_VASE_WATER);
		filledVaseWithLid = new ItemRequirement(true, "Sealed vase", ItemID.VIKING_AIRTIGHT_VASE_WITH_LID_WATER);
		vaseWithLidWrong = new ItemRequirement(true, "Sealed vase", ItemID.VIKING_AIRTIGHT_VASE_WITH_LID);
		frozenKey = new ItemRequirement(true, "Frozen key", ItemID.VIKING_KEY_IN_ICE);
		seersKey = new ItemRequirement("Seer's key", ItemID.VIKING_KEY);
		frozenVase = new ItemRequirement(true, "Frozen vase", ItemID.VIKING_AIRTIGHT_VASE_FROZEN);
	}

	@Override
	protected void setupZones()
	{
		nearPipe = new Zone(new WorldPoint(2663, 3671, 0), new WorldPoint(2667, 3676, 0));
		koscheiRoom = new Zone(new WorldPoint(2641, 10064, 2), new WorldPoint(2672, 10099, 2));

		swensenRoom1 = new Zone(new WorldPoint(2626, 10001, 0), new WorldPoint(2634, 10008, 0));
		swensenRoom2 = new Zone(new WorldPoint(2639, 10012, 0), new WorldPoint(2645, 10018, 0));
		swensenRoom3 = new Zone(new WorldPoint(2650, 10001, 0), new WorldPoint(2656, 10007, 0));
		swensenRoom4 = new Zone(new WorldPoint(2662, 10012, 0), new WorldPoint(2668, 10018, 0));
		swensenRoom5 = new Zone(new WorldPoint(2627, 10023, 0), new WorldPoint(2633, 10029, 0));
		swensenRoom6 = new Zone(new WorldPoint(2650, 10034, 0), new WorldPoint(2656, 10040, 0));
		swensenRoom7 = new Zone(new WorldPoint(2662, 10022, 0), new WorldPoint(2670, 10030, 0));
		swensenFinalRoom = new Zone(new WorldPoint(2661, 10034, 0), new WorldPoint(2670, 10043, 0));
		swensenArea = new Zone(new WorldPoint(2626, 10001, 0), new WorldPoint(2674, 10044, 0));

		peerEntrance = new Zone(new WorldPoint(2629, 3659, 0), new WorldPoint(2633, 3666, 0));
		peerUpstairs = new Zone(new WorldPoint(2629, 3659, 2), new WorldPoint(2638, 3666, 2));
		peerExit = new Zone(new WorldPoint(2634, 3659, 0), new WorldPoint(2638, 3666, 0));
	}

	public void setupConditions()
	{
		inQuestJournal = new WidgetTextRequirement(InterfaceID.Questjournal.TITLE, "The Fremennik Trials");

		Requirement syncedReqs = new Conditions(true, LogicType.OR, inQuestJournal,
			new DialogRequirement("I think I would enjoy the challenge"));
		synced = new RuneliteRequirement(configManager, "fremmytrialssynced", syncedReqs);

		hasStartedOlaf = new RuneliteRequirement(configManager, "fremmytrialsstartedolaf",
			new Conditions(true, LogicType.OR,
			new DialogRequirement("That is great news outerlander! We always need more music lovers here!"),
			new DialogRequirement("So how would I go about writing this epic?"),
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "Bard<col=000080> will vote for me if"))
		);

		syncedOlaf = new Conditions(true, synced, hasStartedOlaf);

		talkedToLalli = new RuneliteRequirement(configManager, "fremmytrialstalkedtolalli",
			new Conditions(true, LogicType.OR,
				new DialogRequirement("I see... okay, well, bye!"),
				new DialogRequirement("Human call itself Askeladden!")
			));
		gottenRock = new VarbitRequirement(6486, 1);

		petRockInCauldron = new RuneliteRequirement(configManager, "fremmytrialsaddedpetrock",
			new ChatMessageRequirement("You put your pet rock into the cauldron.")
		);
		cabbageInCauldron = new RuneliteRequirement(configManager, "fremmytrialsaddedcabbage",
			new ChatMessageRequirement("You put a cabbage into the cauldron.")
		);
		potatoInCauldron = new RuneliteRequirement(configManager, "fremmytrialsaddedpotato",
			new ChatMessageRequirement("You put a potato into the cauldron.")
		);

		onionInCauldron = new RuneliteRequirement(configManager, "fremmytrialsaddedonion",
			new ChatMessageRequirement("You put an onion into the cauldron.")
		);

		cauldronFilledDialog = new DialogRequirement("Indeed it is. Try it and see.");

		stewReady = new RuneliteRequirement(configManager, "fremmytrialsstewready",
			new Conditions(petRockInCauldron, cabbageInCauldron, potatoInCauldron, onionInCauldron, cauldronFilledDialog));

		finishedOlafMessage = new ChatMessageRequirement("Congratulations! You have completed the Bard's Trial!");
		finishedOlafWidget = new Conditions(true, new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "I now have the Bard's vote"));
		finishedOlafTask = new RuneliteRequirement(configManager, "fremmytrialscompletedolaf",
			new Conditions(true, LogicType.OR, finishedOlafMessage, finishedOlafWidget));

		talkedToManniWidget = new Conditions(true, new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "Reveller<col=000080> will vote for me"));
		talkedToManniChat = new Conditions(true, LogicType.OR,
			new DialogRequirement("pick up a keg from that table over there"),
			new DialogRequirement("Grab a keg of beer from that table near the bar, and come back here with it.")
		);
		talkedToManni = new RuneliteRequirement(configManager, "fremmytrialsstartedmanni",
			new Conditions(true, LogicType.OR, talkedToManniWidget, talkedToManniChat)
		);

		syncedManni = talkedToManni;

		isNearPipe = new ZoneRequirement(nearPipe);

		hasPlacedStrangeObject = new RuneliteRequirement(configManager, "fremmytrialsplacedstrangeobject",
			new Conditions(true, LogicType.OR,
			new WidgetTextRequirement(InterfaceID.ChatRight.TEXT,
				"That is going to make a really loud bang when it goes off!"),
			new ChatMessageRequirement("You put the lit strange object into the pipe."))
		);

		hasReplacedBeer = new RuneliteRequirement(configManager, "fremmytrialsreplacedbeer",
			new ChatMessageRequirement("You empty the keg and refill it with low alcohol beer.")
		);
		finishedManniTask = new RuneliteRequirement(configManager, "fremmytrialsfinishedmanni",
			new Conditions(true, LogicType.OR,
			new ChatMessageRequirement("Congratulations! You have completed the Revellers' Trial."),
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "I now have the Reveller's vote"))
		);

		// No gz message
		finishedSigliTask = new RuneliteRequirement(configManager, "fremmytrialsfinishedsigli",
			new Conditions(true, LogicType.OR,
				new ChatMessageRequirement("Congratulations! You have completed the Hunter's Trial!"),
				new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "I now have the Hunter's vote")
			)
		);

		getFlower = new RuneliteRequirement(configManager, "fremmytrialsstartedsigmund",
			new Conditions(true, LogicType.OR,
				new DialogRequirement("We are a very insular clan"),
				new DialogRequirement("Any suggestions on where to start looking for this flower?"),
				new DialogRequirement("Did you manage to obtain my flower for me yet?"),
				new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "has a <col=800000>rare flower<col=000080> that he wants."))
		);

		talkedToSailor = new RuneliteRequirement(configManager, "fremmytrialssigmundsailor",
			new Conditions(true, LogicType.OR,
			new DialogRequirement("That sounds like a fair deal to me, outerlander."),
			new DialogRequirement("find a love ballad, do you?"))
		);

		talkedToOlafForSigmund = new RuneliteRequirement(configManager, "fremmytrialssigmundolaf",
			new Conditions(true, LogicType.OR,
			new DialogRequirement("composing you a romantic ballad"),
			new DialogRequirement("where I could find some custom sturdy boots, do you?"))
		);

		Conditions ysraAsked = new Conditions(true, LogicType.AND,
			new DialogRequirement("Okay, I will see what I can do."),
			new ZoneRequirement(new Zone(new WorldPoint(2622, 3672, 0), new WorldPoint(2629, 3679, 0))));

		talkedToYsra = new RuneliteRequirement(configManager, "fremmytrialssigmundysra",
			new Conditions(true, LogicType.OR,
			ysraAsked,
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true,
				"<col=000080>The <col=800000>shopkeeper<col=000080> is looking for a <col=800000>tax " +
					"reduction<col=000080>..."))
		);

		talkedToBrundtForSigmund = new RuneliteRequirement(configManager, "fremmytrialssigmundbrundt",
			new Conditions(true, LogicType.OR,
			new DialogRequirement("Speak to Sigli then, and you may have my promise to reduce our sales taxes. And best of luck with the rest of your trials."),
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "The <col=800000>chieftain<col=000080> wants a <col=800000>map of new hunting grounds<col=000080>...")
			));

		talkedToSigliForSigmund = new RuneliteRequirement(configManager, "fremmytrialssigmundsigli",
			new Conditions(true, LogicType.OR,
			// TODO: Fix this check, missing a br
			new DialogRequirement("who knows where my hunting ground is."),
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "<col=000080>The hunter<col=000080> is looking for a <col=800000>custom bow string<col=000080>...")
			));

		talkedToSkulgrimenForSigmund = new RuneliteRequirement(configManager, "fremmytrialssigmundskulgrimen",
			new Conditions(true, LogicType.OR,
			new DialogRequirement("Sounds good to me."),
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "<col=000080>The <col=800000>armourer<col=000080> is looking for a <col=800000>rare inedible fish<col=000080>...")
			));

		talkedToFishermanForSigmund = new RuneliteRequirement(configManager, "fremmytrialssigmundfisherman",
			new Conditions(true, LogicType.OR,
			new DialogRequirement("I'll see what I can do."),
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "<col=000080>The <col=800000>fisherman<col=000080> is looking for a <col=800000>map of fishing spots<col=000080>..."))
		);

		talkedToSwensenForSigmund = new RuneliteRequirement(configManager, "fremmytrialssigmundswensen",
			new Conditions(true, LogicType.OR,
			new DialogRequirement(" take the time to make a forecast somehow."),
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "<col=000080>The <col=800000>navigator<col=000080> is looking for a <col=800000>weather forecast<col=000080>..."))
		);

		talkedToPeerForSigmund = new RuneliteRequirement(configManager, "fremmytrialssigmundpeer",
			new Conditions(true, LogicType.OR,
			new DialogRequirement("That is all."),
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "<col=000080>The <col=800000>seer<col=000080> is looking for a <col=800000>warrior to be his bodyguard<col=000080>..."))
		);
		Conditions thorvaldAsked = new Conditions(true, LogicType.AND, new DialogRequirement("Okay, I'll see what I can do."),
			new ZoneRequirement(new Zone(new WorldPoint(2661, 3690, 0), new WorldPoint(2669, 3696, 0))));

		talkedToThorvaldForSigmund = new RuneliteRequirement(configManager, "fremmytrialssigmundthorvald",
			new Conditions(true, LogicType.OR,
			thorvaldAsked,
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "<col=000080>The <col=800000>warrior<col=000080> is looking for a <col=800000>champions token<col=000080>..."))
		);

		talkedToManniForSigmund = new RuneliteRequirement(configManager, "fremmytrialssigmundmanni",
			new Conditions(true, LogicType.OR,
			new DialogRequirement("That's all."),
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "<col=000080>The <col=800000>reveller<col=000080> is looking for a <col=800000>legendary cocktail<col=000080>..."))
		);

		talkedToThoraForSigmund = new RuneliteRequirement(configManager, "fremmytrialssigmundthora",
			new Conditions(true, LogicType.OR,
			new DialogRequirement("cash. You should go ask him"),
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "<col=000080>All <col=800000>Askeladden<col=000080> wants is " +
				"<col=800000>some money<col=000080>!"),
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "<col=800000>Thora")
			));

		// TODO: No gz message
		finishedSigmundTask = new RuneliteRequirement(configManager, "fremmytrialssigmundfinished",
			new Conditions(true, LogicType.OR,
			new ChatMessageRequirement("Congratulations! You have completed the Merchant's Trial!"),
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "I now have the Merchant's vote")));

		syncedSigmund = new Conditions(LogicType.OR, getFlower, talkedToSailor, talkedToOlafForSigmund, talkedToYsra, talkedToBrundtForSigmund, talkedToSigliForSigmund, talkedToSkulgrimenForSigmund,
			talkedToFishermanForSigmund, talkedToSwensenForSigmund, talkedToPeerForSigmund, talkedToThorvaldForSigmund, talkedToManniForSigmund, talkedToThoraForSigmund);

		noRockAskeladdenNearby = new NpcCondition(NpcID.VIKING_ASKELLADEN_NOROCKOP);

		inKoscheiRoom = new ZoneRequirement(koscheiRoom);
		talkedToThorvald = new RuneliteRequirement(configManager, "fremmytrialsthorvaldstarted",
			new Conditions(true, LogicType.OR,
			new DialogRequirement("Hahahahaha! I'm beginning"),
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "Warrior<col=000080> will vote for me if"))
		);

		koschei1Near = new NpcCondition(NpcID.VIKING_ENEMY1);
		koschei2Near = new NpcCondition(NpcID.VIKING_ENEMY2);
		koschei3Near = new NpcCondition(NpcID.VIKING_ENEMY3);
		koschei4Near = new NpcCondition(NpcID.VIKING_ENEMY4);

		syncedThorvald = new Conditions(talkedToThorvald);

		finishedThorvaldTask = new RuneliteRequirement(configManager, "fremmytrialsthorvaldfinished",
			new Conditions(true, LogicType.OR,
			new ChatMessageRequirement("Congratulations! You have completed the warrior's trial!"),
				new ChatMessageRequirement("Congratulations! You have completed the warriors trial!"),
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "I now have the Warrior's vote"))
		);

		talkedToSwensen = new RuneliteRequirement(configManager, "fremmytrialsswensenstarted",
			new Conditions(true, LogicType.OR,
			new DialogRequirement("A maze? Is that all?"),
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "Navigator<col=000080> will vote for me if"))
		);

		inSwensenRoom1 = new ZoneRequirement(swensenRoom1);
		inSwensenRoom2 = new ZoneRequirement(swensenRoom2);
		inSwensenRoom3 = new ZoneRequirement(swensenRoom3);
		inSwensenRoom4 = new ZoneRequirement(swensenRoom4);
		inSwensenRoom5 = new ZoneRequirement(swensenRoom5);
		inSwensenRoom6 = new ZoneRequirement(swensenRoom6);
		inSwensenRoom7 = new ZoneRequirement(swensenRoom7);
		inSwensenFinalRoom = new ZoneRequirement(swensenFinalRoom);
		inSwensenArea = new ZoneRequirement(swensenArea);

		syncedSwensen = new Conditions(talkedToSwensen);
		finishedSwensenTask = new RuneliteRequirement(configManager, "fremmytrialswensenfinished",
			new Conditions(true, LogicType.OR,
			new DialogRequirement("Outerlander! You have finished my maze!"),
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "I now have the Navigator's vote"))
		);

		/* Peer Task */

		talkedToPeer = new RuneliteRequirement(configManager, "fremmytrialpeerstarted",
			new Conditions(true, LogicType.OR,
			new DialogRequirement("I have one small question"),
			new DialogRequirement("So I can bring nothing with me when I enter your house?"),
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "Seer<col=000080> will vote for me if")));

		isMind = new Conditions(true, new WidgetTextRequirement(229, 1, "My first is in mage"));
		isTree = new Conditions(true, new WidgetTextRequirement(229, 1, "My first is in tar"));
		isLife = new Conditions(true, new WidgetTextRequirement(229, 1, "My first is in well"));
		isFire = new Conditions(true, new WidgetTextRequirement(229, 1, "My first is in fish"));
		isTime = new Conditions(true, new WidgetTextRequirement(229, 1, "My first is in water"));
		isWind = new Conditions(true, new WidgetTextRequirement(229, 1, "My first is in wizard"));

		inPeerEntrance = new ZoneRequirement(peerEntrance);
		inPeerUpstairs = new ZoneRequirement(peerUpstairs);
		inPeerExit = new ZoneRequirement(peerExit);
		hasSolvedDoor = new ChatMessageRequirement("You have solved the riddle!");

		cupboardOpen = new ObjectCondition(ObjectID.VIKING_CUPBOARDOPEN_HIGH);
		chestOpen = new ObjectCondition(ObjectID.VIKING_SEER_CHEST_OPEN, new WorldPoint(2635, 3660, 2));
		trapDoorOpen = new ObjectCondition(ObjectID.VIKING_SEER_TRAPDOOR_OPEN, new WorldPoint(2636, 3663, 2));

		/* Currently does not capture the case a user uses a disk, then logs out */
		hasUsedDisk = new RuneliteRequirement(configManager, "fremmytrialsuseddisk",
			new ChatMessageRequirement("You put the red disk into the empty hole on the mural.", "You've already put the red disk into the empty hole on the mural.")
		);
		muralHasDisks = new ObjectCondition(ObjectID.VIKING_SEERS_MURAL_DONE);
		hasAnyBucket = new Conditions(LogicType.OR, bucket15, bucket25, bucket35, bucket45, fullBucket, emptyBucket);
		hasAnyJug = new Conditions(LogicType.OR, jug13, jug23, emptyJug, fullJug);

		finishedPeerTask = new Conditions(true, LogicType.OR,
			new DialogRequirement("To have solved my puzzle"),
			new WidgetTextRequirement(InterfaceID.Questjournal.TEXTLAYER, true, "I now have the Seer's vote"));

		syncedPeer = new Conditions(talkedToPeer);

		syncedAll = new Conditions(true, LogicType.OR, synced, new Conditions(syncedOlaf, syncedManni, syncedSigmund, syncedThorvald));
	}

	public void setupSteps()
	{
		talkToBrundt = new NpcStep(this, NpcID.VIKING_BRUNDT_CHILD, new WorldPoint(2658, 3669, 0), "Talk to Brundt in Rellekka's longhall.");
		talkToBrundt.addDialogStep("Ask about anything else.");
		talkToBrundt.addDialogStep("Do you have any quests?");
		talkToBrundt.addDialogStep("Yes, I am interested.");
		talkToBrundt.addDialogStep("I want to become a Fremennik!");

		/* Olaf Task */
		talkToOlaf = new NpcStep(this, NpcID.VIKING_OLAF, new WorldPoint(2673, 3683, 0),
			"Talk to Olaf the Bard north east of the longhall.");
		talkToOlaf.addDialogStep("Ask about becoming a Fremennik");
		talkToOlaf.addDialogStep("Yes");
		chopSwayingTree = new ObjectStep(this, ObjectID.VIKING_MUSICAL_TREE, new WorldPoint(2739, 3639, 0), "Go south east of Rellekka and cut a branch from the swaying tree", axe, knife);
		fletchLyre = new DetailedQuestStep(this, "Use a knife on the branch to make an unstrung lyre", knife.highlighted(), branch.highlighted());
		talkToLalli = new NpcStep(this, NpcID.VIKING_LALLI_TROLL, new WorldPoint(2771, 3621, 0), "Talk to Lalli to the south east of Rellekka.");
		talkToLalli.addDialogStep("Other human?");
		talkToAskeladdenForRock = new NpcStep(this, NpcID.VIKING_ASKELLADEN_NOROCKOP, new WorldPoint(2658, 3660, 0), "Return to Rellekka and talk to Askeladden for a pet rock.");
		talkToAskeladdenForRock.addDialogStep("Ask about becoming a Fremennik");
		pickVeg = new DetailedQuestStep(this, new WorldPoint(2677, 3652, 0), "Pick a potato, cabbage and onion from the field in south east Rellekka.", cabbage, onion, potato);

		useOnion = new ObjectStep(this, ObjectID.VIKING_TROLL_CAULDRON, new WorldPoint(2773, 3624, 0),
			"Return to Lalli and use a onion, cabbage, potato, and pet rock on their stew. " +
				"You can find the vegetables growing in the south east of Rellekka.",
			petRock.hideConditioned(petRockInCauldron), cabbage.hideConditioned(cabbageInCauldron),
			potato.hideConditioned(potatoInCauldron), onion);
		useOnion.addIcon(ItemID.ONION);
		useCabbage = new ObjectStep(this, ObjectID.VIKING_TROLL_CAULDRON, new WorldPoint(2773, 3624, 0),
			"Use a cabbage on the stew.", petRock.hideConditioned(petRockInCauldron),
			cabbage, potato.hideConditioned(potatoInCauldron));
		useCabbage.addIcon(ItemID.CABBAGE);
		usePotato = new ObjectStep(this, ObjectID.VIKING_TROLL_CAULDRON, new WorldPoint(2773, 3624, 0),
			"Use a potato on the stew.", petRock.hideConditioned(petRockInCauldron),
			potato);
		usePotato.addIcon(ItemID.POTATO);
		useRock = new ObjectStep(this, ObjectID.VIKING_TROLL_CAULDRON, new WorldPoint(2773, 3624, 0),
			"Use your pet rock on the stew.", petRock);
		useRock.addIcon(ItemID.VT_USELESS_ROCK);
		useOnion.addSubSteps(useCabbage, usePotato, useRock);
		talkToLaliAfterStew = new NpcStep(this, NpcID.VIKING_LALLI_TROLL, new WorldPoint(2771, 3621, 0), "Talk to Lalli for golden wool.");
		spinWool = new DetailedQuestStep(this, "Spin the golden fleece into wool using a spinning wheel. The closest wheel is in Seers' Village.", goldenFleece);
		makeLyre = new DetailedQuestStep(this, "Use the golden wool on your unstrung lyre.", lyreUnstrung.highlighted(), goldenWool.highlighted());
		enchantLyre = new ObjectStep(this, ObjectID.VIKING_LAKE_SHRINE_ALTAR, new WorldPoint(2626, 3598, 0), "Bring your lyre to the Strange Altar south west of Rellekka, and use either a raw shark, raw sea turtle or raw manta ray on it.", lyre, rawShark);
		enchantLyre.addIcon(ItemID.RAW_SHARK);
		performMusic = new DetailedQuestStep(this, new WorldPoint(2658, 3683, 0), "Return to Rellekka and enter the longhall's stage to perform. Once on stage, play the enchanted lyre.", enchantedLyre.highlighted());

		/* Manni Task */
		talkToManni = new NpcStep(this, NpcID.VIKING_REVELLER_3, new WorldPoint(2658, 3675, 0), "Talk to Manni the Reveller in the Rellekka longhall.");
		talkToManni.addDialogSteps("Ask about becoming a Fremennik", "Yes");
		pickUpBeer = new DetailedQuestStep(this, new WorldPoint(2658, 3676, 0),
			"Get some beer. There is beer tankard in the Rellekka longhall.", beer);
		getStrangeObject = new NpcStep(this, NpcID.VT_COUNCIL_WORKMEN, new WorldPoint(2654, 3593, 0), "Give the Council workman on the bridge south of Rellekka going to Seer's Village a beer.", beer);
		getStrangeObject.addIcon(ItemID.VIKING_TANKARD_FULL);
		getStrangeObject.addDialogSteps("Yes");
		getAlcoholFreeBeer = new NpcStep(this, NpcID.POISON_SALESMAN, new WorldPoint(2695, 3491, 0), "Buy some alcohol free beer from the Poison Salesman in the Seer's Village pub.", coins250);
		getAlcoholFreeBeer.addDialogStep("Talk about the Fremennik Trials");
		getAlcoholFreeBeer.addDialogStep("Yes");

		prepareToUseStrangeObject = new DetailedQuestStep(this, new WorldPoint(2664, 3674, 0), "Return to Rellekka with the strange object, a tinderbox, and low alcohol beer.", strangeObject, tinderbox, alcoholFreeBeer);
		useStrangeObject = new ObjectStep(this, ObjectID.VIKING_PIPE_END_LONGHALL, new WorldPoint(2663, 3674, 0), "Use a tinderbox on the strange object then use it on the nearby pipe.", strangeObject, tinderbox);
		useStrangeObjectOnPipe = new ObjectStep(this, ObjectID.VIKING_PIPE_END_LONGHALL, new WorldPoint(2663, 3674, 0),
			"Use the lit strange object on the nearby pipe!", litStrangeObject.highlighted());
		useStrangeObjectOnPipe.addIcon(ItemID.VIKING_FIRECRACKER_LIT);
		useStrangeObject.addSubSteps(useStrangeObjectOnPipe);
		getKegOfBeer = new DetailedQuestStep(this, new WorldPoint(2660, 3676, 0), "Pick up a keg of beer in the longhall.", kegOfBeer);
		useAlcoholFreeOnKeg = new DetailedQuestStep(this, new WorldPoint(2658, 3673, 0), "Use the low alcohol beer on the keg of beer whilst in the longhall.", alcoholFreeBeer.highlighted(), kegOfBeer.highlighted());

		cheatInBeerDrinking = new NpcStep(this, NpcID.VIKING_REVELLER_3, new WorldPoint(2658, 3675, 0), "Talk to Manni the Reveller to cheat in the competition.", kegOfBeer);
		cheatInBeerDrinking.addDialogStep("Ask about becoming a Fremennik");
		cheatInBeerDrinking.addDialogStep("Yes");

		talkToSigli = new NpcStep(this, NpcID.VIKING_SIGLI, new WorldPoint(2658, 3650, 0), "Talk to Sigli south of the longhall for a hunters' talisman.");
		talkToSigli.addDialogSteps("Ask about becoming a Fremennik", "What's a Draugen?", "Yes");

		huntDraugen = new NpcStep(this, NpcID.VIKING_DRAUGEN_SAFE, "Use the hunter's talisman to hunt down the Draugen. It'll appear near one of the butterflies in the area.", true);

		returnToSigli = new NpcStep(this, NpcID.VIKING_SIGLI, new WorldPoint(2658, 3650, 0), "Return the charged hunters' talisman to Sigli.", chargedHuntersTalisman);

		/* Sigmund Task */
		talkToSigmund = new NpcStep(this, NpcID.VIKING_SIGMUND, new WorldPoint(2642, 3680, 0), "Talk to Sigmund the Merchant in the Rellekka market.");
		talkToSigmund.addDialogStep("Yes");

		talkToSailor = new NpcStep(this, NpcID.VIKING_SAILOR, new WorldPoint(2629, 3693, 0), "Talk to the Sailor on the docks.");
		talkToSailor.addDialogStep("Ask about the Merchant's trial");
		talkToOlafForSigmund = new NpcStep(this, NpcID.VIKING_OLAF, new WorldPoint(2673, 3683, 0), "Talk to Olaf the Bard east of the longhall.");
		talkToOlafForSigmund.addDialogStep("Ask about the Merchant's trial");
		talkToYsra = new NpcStep(this, NpcID.VIKING_CLOTHING_SHOPKEEPER, new WorldPoint(2624, 3675, 0), "Talk to Ysra in her shop west of the market.");
		talkToYsra.addDialogStep("Ask about the Merchant's trial");
		talkToBrundtForSigmund = new NpcStep(this, NpcID.VIKING_BRUNDT_CHILD, new WorldPoint(2658, 3669, 0), "Talk to Brundt in the longhall.");
		talkToBrundtForSigmund.addDialogStep("Ask about the Merchant's trial");
		talkToBrundtForSigmund.addDialogStep("Ask about anything else.");
		talkToSigliForSigmund = new NpcStep(this, NpcID.VIKING_SIGLI, new WorldPoint(2658, 3650, 0), "Talk to Sigli south of the longhall.");
		talkToSigliForSigmund.addDialogStep("Ask about the Merchant's trial");
		talkToSkulgrimenForSigmund = new NpcStep(this, NpcID.VIKING_WEAPONS_SALESMAN, new WorldPoint(2663, 3692, 0), "Talk to Skulgrimen north of the longhall.");
		talkToSkulgrimenForSigmund.addDialogStep("Ask about the Merchant's trial");
		talkToFishermanForSigmund = new NpcStep(this, NpcID.VIKING_FISHERMAN1, new WorldPoint(2641, 3699, 0), "Talk to the Fisherman on the pier north of the market.");
		talkToFishermanForSigmund.addDialogStep("Ask about the Merchant's trial");
		talkToSwenesenForSigmund = new NpcStep(this, NpcID.VIKING_HALLIFRED, new WorldPoint(2646, 3660, 0), "Talk to Swensen the Navigator in his hut south west of the longhall.");
		talkToSwenesenForSigmund.addDialogStep("Ask about the Merchant's trial");
		talkToPeerForSigmund = new NpcStep(this, NpcID.VIKING_PEER, new WorldPoint(2633, 3667, 0), "Talk to Peer the Seer south west of the market.");
		talkToPeerForSigmund.addDialogStep("Ask about the Merchant's trial");
		talkToThorvaldForSigmund = new NpcStep(this, NpcID.VIKING_THORVALD, new WorldPoint(2666, 3693, 0), "Talk to Thorvald north of the longhall.");
		talkToThorvaldForSigmund.addDialogStep("Ask about the Merchant's trial");
		talkToManniForSigmund = new NpcStep(this, NpcID.VIKING_REVELLER_3, new WorldPoint(2658, 3675, 0), "Talk to Manni the Reveller in the longhall.");
		talkToManniForSigmund.addDialogStep("Ask about the Merchant's trial");
		talkToThoraForSigmund = new NpcStep(this, NpcID.VIKING_LONGHALL_BARKEEP, new WorldPoint(2662, 3673, 0), "Talk to Thora the Barkeep in the longhall.");
		talkToThoraForSigmund.addDialogStep("Ask about the Merchant's trial");
		talkToAskeladdenForSigmund2 = new NpcStep(this, NpcID.VIKING_ASKELLADEN_NOROCKOP, new WorldPoint(2659, 3660, 0), "Talk to Askeladden south of the longhall.");
		talkToAskeladdenForSigmund2.addDialogStep("Ask about the Merchant's trial");
		talkToAskeladdenForSigmund2.addDialogStep("Yes");

		talkToAskeladdenForSigmund = new NpcStep(this, NpcID.VIKING_ASKELLADEN_ROCKOP, new WorldPoint(2659, 3660, 0), "Talk to Askeladden south of the longhall.");
		talkToAskeladdenForSigmund.addDialogStep("Ask about the Merchant's trial");
		talkToAskeladdenForSigmund.addDialogStep("Yes");
		talkToAskeladdenForSigmund.addSubSteps(talkToAskeladdenForSigmund2);

		bringNoteToThora = new NpcStep(this, NpcID.VIKING_LONGHALL_BARKEEP, new WorldPoint(2662, 3673, 0), "Bring the promissory note to Thora the Barkeep in the longhall.", promissoryNote);
		bringNoteToThora.addDialogStep("Ask about the Merchant's trial");
		bringCocktailToManni = new NpcStep(this, NpcID.VIKING_REVELLER_3, new WorldPoint(2658, 3675, 0), "Bring the cocktail to Manni the Reveller in the longhall.", legendaryCocktail);
		bringCocktailToManni.addDialogStep("Ask about the Merchant's trial");
		bringChampionsTokenToThorvald = new NpcStep(this, NpcID.VIKING_THORVALD, new WorldPoint(2666, 3693, 0), "Bring the champions token to Thorvald north of the longhall.", championsToken);
		bringChampionsTokenToThorvald.addDialogStep("Ask about the Merchant's trial");
		bringWarriorsContractToPeer = new NpcStep(this, NpcID.VIKING_PEER, new WorldPoint(2633, 3667, 0), "Bring the warriors' contract to Peer the Seer south west of the market.", warriorsContract);
		bringWarriorsContractToPeer.addDialogStep("Ask about the Merchant's trial");
		bringWeatherForecastToSwensen = new NpcStep(this, NpcID.VIKING_HALLIFRED, new WorldPoint(2646, 3660, 0), "Bring the weather forecast to Swensen the Navigator in his hut south west of the longhall.", weatherForecast);
		bringWeatherForecastToSwensen.addDialogStep("Ask about the Merchant's trial");
		bringSeaFishingMapToFisherman = new NpcStep(this, NpcID.VIKING_FISHERMAN1, new WorldPoint(2641, 3699, 0), "Bring the sea fishing map to the Fisherman on the pier north of the market.", seaFishingMap);
		bringSeaFishingMapToFisherman.addDialogStep("Ask about the Merchant's trial");
		bringUnusualFishToSkulgrimen = new NpcStep(this, NpcID.VIKING_WEAPONS_SALESMAN, new WorldPoint(2663, 3692, 0), "Bring the unusual fish to Skulgrimen north of the longhall.", unusualFish);
		bringUnusualFishToSkulgrimen.addDialogStep("Ask about the Merchant's trial");
		bringCustomBowStringToSigli = new NpcStep(this, NpcID.VIKING_SIGLI, new WorldPoint(2658, 3650, 0), "Bring the custom bow string to Sigli south of the longhall.", customBowString);
		bringCustomBowStringToSigli.addDialogStep("Ask about the Merchant's trial");
		bringTrackingMapToBrundt = new NpcStep(this, NpcID.VIKING_BRUNDT_CHILD, new WorldPoint(2658, 3669, 0), "Bring the tracking map to Brundt in the longhall.", trackingMap);
		bringTrackingMapToBrundt.addDialogStep("Ask about the Merchant's trial");
		bringTrackingMapToBrundt.addDialogStep("Ask about anything else.");
		bringFiscalStatementToYsra = new NpcStep(this, NpcID.VIKING_CLOTHING_SHOPKEEPER, new WorldPoint(2624, 3675, 0), "Bring the fiscal statement to Ysra in her shop west of the market.", fiscalStatement);
		bringFiscalStatementToYsra.addDialogStep("Ask about the Merchant's trial");
		bringSturdyBootsToOlaf = new NpcStep(this, NpcID.VIKING_OLAF, new WorldPoint(2673, 3683, 0), "Bring the sturdy boots to Olaf the Bard east of the longhall.", sturdyBoots);
		bringSturdyBootsToOlaf.addDialogStep("Ask about the Merchant's trial");
		bringBalladToSailor = new NpcStep(this, NpcID.VIKING_SAILOR, new WorldPoint(2629, 3693, 0), "Bring the ballad to the Sailor on the docks.", ballad);
		bringBalladToSailor.addDialogStep("Ask about the Merchant's trial");
		bringExoticFlowerToSigmund = new NpcStep(this, NpcID.VIKING_SIGMUND, new WorldPoint(2642, 3680, 0), "Finally, bring the exotic flower to Sigmund the Merchant in the Rellekka market.", exoticFlower);

		/* Thorvald Task */
		talkToThorvald = new NpcStep(this, NpcID.VIKING_THORVALD, new WorldPoint(2666, 3693, 0), "Talk to Thorvald north of the longhall.");
		talkToThorvald.addDialogStep("Ask about becoming a Fremennik");
		bringChampionsTokenToThorvald.addDialogStep("Yes");
		goDownLadderToKoschei = new ObjectStep(this, ObjectID.VIKING_WARRIOR_LADDER, new WorldPoint(2667, 3694, 0), "When you're ready to fight Koschei, go down the ladder.", koscheiGear, optionalKoscheiGear);
		waitForKoschei = new DetailedQuestStep(this, "Wait for Koschei the Deathless to spawn. If you brought a dramen branch you should now fletch it and equip the staff.");
		killKoschei1 = new NpcStep(this, NpcID.VIKING_ENEMY1, new WorldPoint(2660, 10080, 2), "Fight Koschei the Deathless and 'kill' him at least 3 times. Dying to his fourth phase is a SAFE death.");
		killKoschei2 = new NpcStep(this, NpcID.VIKING_ENEMY1, new WorldPoint(2660, 10080, 2), "Fight Koschei the Deathless and 'kill' him at least 3 times. Dying to his fourth phase is a SAFE death.");
		killKoschei3 = new NpcStep(this, NpcID.VIKING_ENEMY1, new WorldPoint(2660, 10080, 2), "Fight Koschei the Deathless and 'kill' him at least 3 times. Dying to his fourth phase is a SAFE death.");
		killKoschei4 = new NpcStep(this, NpcID.VIKING_ENEMY1, new WorldPoint(2660, 10080, 2), "You must now either die (SAFE DEATH), or defeat Koschei once more. Do not leave or you'll have to fight him again.");
		killKoschei1.addSubSteps(killKoschei2, killKoschei3, killKoschei4);

		/* Swensen Task */
		talkToSwensen = new NpcStep(this, NpcID.VIKING_HALLIFRED, new WorldPoint(2646, 3660, 0), "Talk to Swensen the Navigator in his hut south west of the longhall.");
		talkToSwensen.addDialogStep("Yes");
		talkToSwensen.addDialogStep("Ask about becoming a Fremennik");
		goDownLadderSwensen = new ObjectStep(this, ObjectID.VT_MAZELADDERTOPENTRANCE, new WorldPoint(2644, 3657, 0), "Climb down Swensen's ladder.");
		swensen1South = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VT_MAZEPORTAL_1, new WorldPoint(2631, 10002, 0), "Go through the south portal."), "Solve Swensen's challenge.");
		swensen2West = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VT_MAZEPORTAL_2, new WorldPoint(2639, 10015, 0), "Go through the west portal."), "Solve Swensen's challenge.");
		swensen3East = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VT_MAZEPORTAL_3, new WorldPoint(2656, 10004, 0), "Go through the east portal."), "Solve Swensen's challenge.");
		swensen4North = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VT_MAZEPORTAL_4, new WorldPoint(2665, 10018, 0), "Go through the north portal."), "Solve Swensen's challenge.");
		swensen5South = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VT_MAZEPORTAL_5, new WorldPoint(2630, 10023, 0), "Go through the south portal."), "Solve Swensen's challenge.");
		swensen6East = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VT_MAZEPORTAL_6, new WorldPoint(2656, 10037, 0), "Go through the east portal."), "Solve Swensen's challenge.");
		swensen7North = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VT_MAZEPORTAL_7, new WorldPoint(2666, 10029, 0), "Go through the north portal."), "Solve Swensen's challenge.");
		swensenUpLadder = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VT_MAZELADDEREXIT, new WorldPoint(2665, 10037, 0), "Climb up the ladder."), "Solve Swensen's challenge.");
		resetSwensen = new PuzzleWrapperStep(this, new DetailedQuestStep(this, "Climb up a rope/ladder to restart the puzzle."), "Solve Swensen's challenge.");

		/* Peer Task */
		talkToPeer = new NpcStep(this, NpcID.VIKING_PEER, new WorldPoint(2633, 3667, 0), "Talk to Peer the Seer south west of the Rellekka market. You will need to bank all your items. Peer will offer to do this for you.");
		talkToPeer.addDialogStep("Ask about becoming a Fremennik");
		talkToPeer.addDialogStep("Yes");
		// 261 1->0 when banking all
		enterPeerHouse = new ObjectStep(this, ObjectID.VIKING_SEERS_DOOR1, new WorldPoint(2631, 3667, 0), "Try to enter Peer the Seer's house after banking all your items. Peer the Seer will offer to bank everything for you.");
		enterPeerHouse.addDialogStep("Read the riddle");

		enterCode = new DetailedQuestStep(this, "Enter the solution to Peer's door.");

		// TODO: IDEA: Somehow have a parent challenge/puzzle step, so they all highlight the same text.
		inputMind = new PuzzleWrapperStep(this, new CombinationPuzzle(this, "MIND"), "Solve Peer's challenge.");
		inputTree = new PuzzleWrapperStep(this, new CombinationPuzzle(this, "TREE"), "Solve Peer's challenge.");
		inputLife = new PuzzleWrapperStep(this, new CombinationPuzzle(this, "LIFE"), "Solve Peer's challenge.");
		inputFire = new PuzzleWrapperStep(this, new CombinationPuzzle(this, "FIRE"), "Solve Peer's challenge.");
		inputTime = new PuzzleWrapperStep(this, new CombinationPuzzle(this, "TIME"), "Solve Peer's challenge.");
		inputWind = new PuzzleWrapperStep(this, new CombinationPuzzle(this, "WIND"), "Solve Peer's challenge.");
		enterCode.addSubSteps(inputMind, inputTree, inputLife, inputFire, inputTime, inputWind);

		goUpEntranceLadderPeer = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_SEER_UP_LADDER, new WorldPoint(2631, 3663, 0), "Go up the ladder."),
			"Solve Peer's challenge.");
		searchBookcase = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_SEER_BOOKCASE, new WorldPoint(2634, 3665, 2), "Search the bookcase for a red herring. If you've already unlocked the mural, go search it instead."),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		ObjectStep cookHerringRealStep = new ObjectStep(this, ObjectID.VIKING_SEER_RANGE, new WorldPoint(2629, 3664, 2), "Use the red herring on the cooking range.", redHerring);
		cookHerringRealStep.addIcon(ItemID.VIKING_RED_HERRING);
		cookHerring = new PuzzleWrapperStep(this, cookHerringRealStep,
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);

		searchUnicorn = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_UNIMOUNTEDHEAD, new WorldPoint(2632, 3660, 2), "Study the unicorn's head."),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		searchBull = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_BULLMOUNTEDHEAD, new WorldPoint(2634, 3660, 2), "Study the bull's head."),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		searchChest1 = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_SEER_CHEST_CLOSED, new WorldPoint(2635, 3660, 2), "Search the chest in the south of the room."),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		searchChest2 = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_SEER_CHEST_OPEN, new WorldPoint(2635, 3660, 2), "Search the chest in the south of the room."),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		searchChest1.addSubSteps(searchChest2);
		searchCupboard1 = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_CUPBOARDHIGH, new WorldPoint(2630, 3660, 2), "Search the cupboard in the south west of the room."),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		searchCupboard2 = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_CUPBOARDOPEN_HIGH, new WorldPoint(2630, 3660, 2), "Search the cupboard in the south west of the room."),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		searchCupboard1.addSubSteps(searchCupboard2);

		useGoopOnDisk = new PuzzleWrapperStep(this, new DetailedQuestStep(this, "Use the sticky red goop on the wooden disk.", stickyRedGoop, woodenDisk),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);

		openTrapDoorAndGoDown1 = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_SEER_TRAPDOOR_CLOSED, new WorldPoint(2636, 3663, 2), "Open and go down the trapdoor."),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		goDown1 = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_SEER_TRAPDOOR_OPEN, new WorldPoint(2636, 3663, 2), "Go down the trapdoor."),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		openTrapDoorAndGoDown1.addSubSteps(goDown1);

		ObjectStep useDiskAnyOnMuralRealStep = new ObjectStep(this, ObjectID.VIKING_SEERS_MURAL, new WorldPoint(2634, 3663, 0), "Use the red disks on the abstract mural.", redDiskOld, redDiskNew);
		useDiskAnyOnMuralRealStep.addIcon(ItemID.VIKING_RED_WOODEN_COIN_OLD);
		useDiskAnyOnMural = new PuzzleWrapperStep(this, useDiskAnyOnMuralRealStep, "Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);

		ObjectStep useDiskOldOnMuralRealStep = new ObjectStep(this, ObjectID.VIKING_SEERS_MURAL, new WorldPoint(2634, 3663, 0), "Use the old red disk on the abstract mural.", redDiskOld);
		useDiskOldOnMuralRealStep.addIcon(ItemID.VIKING_RED_WOODEN_COIN_OLD);
		useDiskOldOnMural = new PuzzleWrapperStep(this, useDiskOldOnMuralRealStep, "Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);

		ObjectStep useDiskNewOnMuralRealStep = new ObjectStep(this, ObjectID.VIKING_SEERS_MURAL, new WorldPoint(2634, 3663, 0), "Use the other red disk on the abstract mural.", redDiskNew);
		useDiskNewOnMural = new PuzzleWrapperStep(this, useDiskNewOnMuralRealStep, "Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		useDiskNewOnMuralRealStep.addIcon(ItemID.VIKING_RED_WOODEN_COIN);

		useDiskAnyOnMural.addSubSteps(useDiskOldOnMural, useDiskNewOnMural);

		goBackUpstairs = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_SEER_DOWN_LADDER, new WorldPoint(2636, 3663, 0), "Go back upstairs."),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		goUpstairsWithVaseLid = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_SEER_DOWN_LADDER, new WorldPoint(2636, 3663, 0), "Go back upstairs."),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);

		ObjectStep useBucketOnTap1RealStep = new ObjectStep(this, ObjectID.VIKING_SEERS_TAP, new WorldPoint(2629, 3661, 2), "Use the empty bucket on the tap.", emptyBucket);
		useBucketOnTap1RealStep.addIcon(ItemID.VIKING_BUCKET_EMPTY);
		useBucketOnTap1 = new PuzzleWrapperStep(this, useBucketOnTap1RealStep,
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);

		useBucketOnJug1 = new PuzzleWrapperStep(this, new DetailedQuestStep(this, "Use the full bucket on the empty jug.", fullBucket, emptyJug),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);

		ObjectStep useJugOnDrain1RealStep = new ObjectStep(this, ObjectID.VIKING_SEERS_DRAIN, new WorldPoint(2629, 3662, 2), "Empty the full jug into the drain.", fullJug);
		useJugOnDrain1RealStep.addIcon(ItemID.VIKING_JUG_3);
		useJugOnDrain1 = new PuzzleWrapperStep(this, useJugOnDrain1RealStep,
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);

		useBucketOnJug2 = new PuzzleWrapperStep(this, new DetailedQuestStep(this, "Use the 2/5ths full bucket on the empty jug.", bucket25, emptyJug),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		useBucketOnTap2 = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_SEERS_TAP, new WorldPoint(2629, 3661, 2), "Use the empty bucket on the tap.", emptyBucket),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		useBucketOnTap2.addIcon(ItemID.VIKING_BUCKET_EMPTY);
		useBucketOnJug3 = new PuzzleWrapperStep(this, new DetailedQuestStep(this, "Use the full bucket on the 2/3rds full jug.", fullBucket, jug23),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		useBucketOnScale = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_SEER_CHEST_CLOSED_SCALES, new WorldPoint(2632, 3665, 2), "Use the 4/5ths full bucket on the chest with a scale on it.", bucket45),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		useBucketOnScale.addIcon(ItemID.VIKING_BUCKET_4);

		fillVase = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_SEERS_TAP, new WorldPoint(2629, 3661, 2), "Use the empty vase on the tap.", vase),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		fillVase.addIcon(ItemID.VIKING_AIRTIGHT_VASE);

		useLidOnVase = new PuzzleWrapperStep(this, new DetailedQuestStep(this, "Use the vase lid on the filled vase", filledVase, vaseLid),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);

		useVaseOnTable = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_SMALL_TABLE_FROZEN, new WorldPoint(2638, 3665, 2), "Use the filled, sealed vase on the frozen table.", filledVaseWithLid),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		useVaseOnTable.addIcon(ItemID.VIKING_AIRTIGHT_VASE_WITH_LID_FROZEN);

		takeLidOff = new PuzzleWrapperStep(this, new DetailedQuestStep(this, "Take the lid off the vase and fill the vase.", vaseWithLidWrong),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		warmFrozenVase = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_SEER_RANGE, new WorldPoint(2629, 3664, 2), "Use the frozen vase on the cooking range.", frozenVase),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		warmFrozenVase.addIcon(ItemID.VIKING_AIRTIGHT_VASE_FROZEN);
		emptyJugAndBucket = new PuzzleWrapperStep(this, new DetailedQuestStep(this, "Empty jug and bucket to restart."), "Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);

		useFrozenKeyOnRange = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_SEER_RANGE, new WorldPoint(2629, 3664, 2), "Use the frozen key on the cooking range.", frozenKey),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		useFrozenKeyOnRange.addIcon(ItemID.VIKING_KEY_IN_ICE);
		goDownstairsWithKey = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_SEER_TRAPDOOR_CLOSED, new WorldPoint(2636, 3663, 2), "Open and go down the trapdoor.", seersKey),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		goDownstairsWithKey2 = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_SEER_TRAPDOOR_OPEN, new WorldPoint(2636, 3663, 2), "Go down the trapdoor.", seersKey),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);
		goDownstairsWithKey.addSubSteps(goDownstairsWithKey2);
		leaveSeersHouse = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.VIKING_SEERS_DOOR2, new WorldPoint(2636, 3667, 0), "Leave the Seer's house.", seersKey),
			"Solve Peer's challenge.").withNoHelpHiddenInSidebar(true);

		finishQuest = new NpcStep(this, NpcID.VIKING_BRUNDT_CHILD, new WorldPoint(2658, 3669, 0), "Talk to Brundt in Rellekka's longhall to finish the quest.");
		finishQuest.addDialogStep("Ask about anything else.");
	}

	@Override
	public List<Requirement> getGeneralRequirements()
	{
		ArrayList<Requirement> reqs = new ArrayList<>();
		reqs.add(new SkillRequirement(Skill.FLETCHING, 25, true));
		reqs.add(new SkillRequirement(Skill.WOODCUTTING, 40, true));
		reqs.add(new SkillRequirement(Skill.CRAFTING, 40, true));

		return reqs;
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		ArrayList<ItemRequirement> reqs = new ArrayList<>();
		reqs.add(coins);
		reqs.add(rawShark);
		reqs.add(tinderbox);
		reqs.add(knife);
		reqs.add(axe);
		return reqs;
	}

	@Override
	public List<String> getNotes()
	{
		ArrayList<String> reqs = new ArrayList<>();
		reqs.add("If the guide at any point appears to de-sync during this quest, please try opening the quest journal to fix it.");
		return reqs;
	}

	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		ArrayList<ItemRequirement> reqs = new ArrayList<>();
		reqs.add(combatGear);
		reqs.add(ringsOfRecoil);
		reqs.add(camelotTeleport);
		reqs.add(rellekkaTeleports);
		return reqs;
	}

	@Override
	public List<String> getCombatRequirements()
	{
		ArrayList<String> reqs = new ArrayList<>();
		reqs.add("Koschei the deathless");
		reqs.add("Draugen (level 69)");
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
		return Arrays.asList(
				new ExperienceReward(Skill.AGILITY, 2812),
				new ExperienceReward(Skill.ATTACK, 2812),
				new ExperienceReward(Skill.CRAFTING, 2812),
				new ExperienceReward(Skill.DEFENCE, 2812),
				new ExperienceReward(Skill.FISHING, 2812),
				new ExperienceReward(Skill.FLETCHING, 2812),
				new ExperienceReward(Skill.HITPOINTS, 2812),
				new ExperienceReward(Skill.STRENGTH, 2812),
				new ExperienceReward(Skill.THIEVING, 2812),
				new ExperienceReward(Skill.WOODCUTTING, 2812));
	}

	@Override
	public List<UnlockReward> getUnlockRewards()
	{
		return Arrays.asList(
				new UnlockReward("Access to Miscellania, Etceteria, Neitiznot, Jatizso and the facilities of Rellekka."),
				new UnlockReward("Ability to wear and purchase Fremmennik Helms"),
				new UnlockReward("Free travel to Waterbirth Island"));
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();
		allSteps.add(new PanelDetails("Talk to Brundt", Collections.singletonList(talkToBrundt), coins, rawShark, tinderbox, knife, axe, combatGear));

		PanelDetails olafPanel = new PanelDetails("Olaf's task",
			Arrays.asList(talkToOlaf, talkToLalli, talkToAskeladdenForRock, useOnion, talkToLaliAfterStew, chopSwayingTree,
				fletchLyre, spinWool, makeLyre, enchantLyre, performMusic), axe, knife, rawShark, emptySlot4);
		olafPanel.setLockingStep(olafTask);
		olafPanel.setVars(1, 2, 3, 4, 5, 6, 7);

		PanelDetails manniPanel = new PanelDetails("Manni's task",
			Arrays.asList(talkToManni, pickUpBeer, getStrangeObject, getAlcoholFreeBeer, prepareToUseStrangeObject,
				useStrangeObject, getKegOfBeer, useAlcoholFreeOnKeg, cheatInBeerDrinking), tinderbox, coins250);
		manniPanel.setLockingStep(manniTask);
		manniPanel.setVars(1, 2, 3, 4, 5, 6, 7);

		PanelDetails sigliPanel = new PanelDetails("Sigli's task",
			Arrays.asList(talkToSigli, huntDraugen, returnToSigli), combatGear);
		sigliPanel.setLockingStep(sigliTask);
		sigliPanel.setVars(1, 2, 3, 4, 5, 6, 7);

		PanelDetails sigmundPanel = new PanelDetails("Sigmund's task",
			Arrays.asList(talkToSigmund, talkToSailor, talkToOlafForSigmund, talkToYsra, talkToBrundtForSigmund, talkToSigliForSigmund,
				talkToSkulgrimenForSigmund, talkToFishermanForSigmund, talkToSwenesenForSigmund, talkToPeerForSigmund, talkToThorvaldForSigmund, talkToManniForSigmund,
				talkToThoraForSigmund, talkToAskeladdenForSigmund, bringNoteToThora, bringCocktailToManni, bringChampionsTokenToThorvald, bringWarriorsContractToPeer, bringWeatherForecastToSwensen,
				bringSeaFishingMapToFisherman, bringUnusualFishToSkulgrimen, bringCustomBowStringToSigli, bringTrackingMapToBrundt, bringFiscalStatementToYsra, bringSturdyBootsToOlaf, bringBalladToSailor,
				bringExoticFlowerToSigmund), coins5000);
		sigmundPanel.setLockingStep(sigmundTask);
		sigmundPanel.setVars(1, 2, 3, 4, 5, 6, 7);

		PanelDetails thorvaldPanel = new PanelDetails("Thorvald's task",
			Arrays.asList(talkToThorvald, goDownLadderToKoschei, killKoschei1), koscheiGear, optionalKoscheiGear);
		thorvaldPanel.setLockingStep(thorvaldTask);
		thorvaldPanel.setVars(1, 2, 3, 4, 5, 6, 7);

		PanelDetails swensenPanel = new PanelDetails("Swensen's task",
			Arrays.asList(talkToSwensen, goDownLadderSwensen, swensen1South, swensen2West, swensen3East, swensen4North, swensen5South, swensen6East, swensen7North, swensenUpLadder));
		swensenPanel.setLockingStep(swensenTask);
		swensenPanel.setVars(1, 2, 3, 4, 5, 6, 7);

		PanelDetails peerPanel = new PanelDetails("Peer's task",
			Arrays.asList(talkToPeer, enterPeerHouse, enterCode, goUpEntranceLadderPeer, searchBookcase, searchBull, searchUnicorn, cookHerring, useGoopOnDisk, openTrapDoorAndGoDown1,
				useDiskAnyOnMural, goUpstairsWithVaseLid, searchCupboard1, searchChest1, useBucketOnTap1, useBucketOnJug1, useJugOnDrain1, useBucketOnJug2, useBucketOnTap2, useBucketOnJug3, useBucketOnScale,
				fillVase, useLidOnVase, useVaseOnTable, useFrozenKeyOnRange, goDownstairsWithKey, leaveSeersHouse));
		peerPanel.setLockingStep(peerTask);
		peerPanel.setVars(1, 2, 3, 4, 5, 6, 7);

		PanelDetails finalPanel = new PanelDetails("Finish off",
			Collections.singletonList(finishQuest));

		allSteps.add(olafPanel);
		allSteps.add(manniPanel);
		allSteps.add(sigliPanel);
		allSteps.add(sigmundPanel);
		allSteps.add(thorvaldPanel);
		allSteps.add(swensenPanel);
		allSteps.add(peerPanel);
		allSteps.add(finalPanel);
		return allSteps;
	}
}
