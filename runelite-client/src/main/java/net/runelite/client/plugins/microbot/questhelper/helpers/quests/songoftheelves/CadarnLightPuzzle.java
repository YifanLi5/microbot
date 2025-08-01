/*
 * Copyright (c) 2021, Zoinkwiz <https://github.com/Zoinkwiz>
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
package net.runelite.client.plugins.microbot.questhelper.helpers.quests.songoftheelves;

import net.runelite.client.plugins.microbot.questhelper.questhelpers.QuestHelper;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.QuestUtil;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.Conditions;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirements;
import net.runelite.client.plugins.microbot.questhelper.requirements.var.VarbitRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.steps.ConditionalStep;
import net.runelite.client.plugins.microbot.questhelper.steps.DetailedQuestStep;
import net.runelite.client.plugins.microbot.questhelper.steps.ObjectStep;
import net.runelite.client.plugins.microbot.questhelper.steps.QuestStep;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.ObjectID;

import java.util.List;

public class CadarnLightPuzzle extends ConditionalStep
{
	Zone f0, f1, f2;

	ItemRequirement handMirrorHighlighted, redCrystalHighlighted;

	DetailedQuestStep p1Pillar1, p1Pillar2, p1Pillar3, p1Pillar4, p1Pillar5, p1Pillar6, p1Pillar7, p1Pillar8, collectMirrors;

	QuestStep goDownstairs;

	Requirement hasMirrorsAndCrystal, onF1, onF2, onF0, r1, r2, r3, r4, r5, r6, r7;

	public CadarnLightPuzzle(QuestHelper questHelper, ConditionalStep goToF0Steps, ConditionalStep goToF1Steps)
	{
		super(questHelper, goToF1Steps);
		setupItemRequirements();
		setupZones();
		setupConditions();
		setupSteps();
		goDownstairs = goToF0Steps.copy();
		collectMirrors.addSubSteps(goToF1Steps);

		addStep(new Conditions(onF0, r7), p1Pillar8);
		addStep(new Conditions(onF0, r6), p1Pillar7);
		addStep(new Conditions(onF0, r5), p1Pillar6);
		addStep(new Conditions(onF0, r4), p1Pillar5);
		addStep(new Conditions(onF0, r3), p1Pillar4);
		addStep(new Conditions(onF0, r2), p1Pillar3);
		addStep(new Conditions(r2), goDownstairs);
		addStep(new Conditions(onF1, r1), p1Pillar2);
		addStep(new Conditions(hasMirrorsAndCrystal, onF1), p1Pillar1);
		addStep(onF1, collectMirrors);
	}

	protected void setupSteps()
	{
		setupItemRequirements();
		setupZones();
		setupConditions();

		collectMirrors = new ObjectStep(getQuestHelper(), ObjectID.SOTE_LIBRARY_DISPENSER, new WorldPoint(2623, 6118, 1), "Collect 7 mirrors and a red crystal from the dispenser in the central room.");
		collectMirrors.addDialogStep("Take everything.");

		p1Pillar1 = new ObjectStep(getQuestHelper(), ObjectID.SOTE_PILLAR_1_D_5, new WorldPoint(2609, 6158, 1),
			"Add a mirror to a pillar to the north. Rotate it to point the light east.", handMirrorHighlighted);
		p1Pillar1.addIcon(ItemID.SOTE_MIRROR);

		p1Pillar2 = new ObjectStep(getQuestHelper(), ObjectID.SOTE_PILLAR_1_E_5, new WorldPoint(2623, 6158, 1),
			"Add a mirror to a pillar to the east. Rotate it to point the light down.", handMirrorHighlighted);
		p1Pillar2.addIcon(ItemID.SOTE_MIRROR);

		p1Pillar3 = new ObjectStep(getQuestHelper(), ObjectID.SOTE_PILLAR_0_E_5, new WorldPoint(2623, 6158, 0),
			"Add a mirror to the pillar near the stairs. Rotate it to point the light south.", handMirrorHighlighted);
		p1Pillar3.addIcon(ItemID.SOTE_MIRROR);

		p1Pillar4 = new ObjectStep(getQuestHelper(), ObjectID.SOTE_PILLAR_0_E_3, new WorldPoint(2623, 6130,0),
			"Add a mirror to the pillar to the south. Rotate it to point the light west.", handMirrorHighlighted);
		p1Pillar4.addIcon(ItemID.SOTE_MIRROR);

		p1Pillar5 = new ObjectStep(getQuestHelper(), ObjectID.SOTE_PILLAR_0_D_3, new WorldPoint(2609, 6130,0),
			"Add a mirror to the pillar to the west. Rotate it to point the light south.", handMirrorHighlighted);
		p1Pillar5.addIcon(ItemID.SOTE_MIRROR);

		p1Pillar6 = new ObjectStep(getQuestHelper(), ObjectID.SOTE_PILLAR_0_D_2, new WorldPoint(2609, 6116,0),
			"Add the red crystal to the pillar to the south.", redCrystalHighlighted);
		p1Pillar6.addIcon(ItemID.SOTE_CRYSTAL_RED);

		p1Pillar7 = new ObjectStep(getQuestHelper(), ObjectID.SOTE_PILLAR_0_D_1, new WorldPoint(2609, 6102,0),
			"Add a mirror to the pillar to the south. Rotate it to point the light east.", handMirrorHighlighted);
		p1Pillar7.addIcon(ItemID.SOTE_MIRROR);

		p1Pillar8 = new ObjectStep(getQuestHelper(), ObjectID.SOTE_PILLAR_0_E_1, new WorldPoint(2623, 6102,0),
			"Add a mirror to the pillar to the east. Rotate it to point the light south at the Seal of Cadarn.", handMirrorHighlighted);
		p1Pillar8.addIcon(ItemID.SOTE_MIRROR);
	}

	protected void setupItemRequirements()
	{
		handMirrorHighlighted = new ItemRequirement("Hand mirror", ItemID.SOTE_MIRROR);
		handMirrorHighlighted.setHighlightInInventory(true);

		redCrystalHighlighted = new ItemRequirement("Red crystal", ItemID.SOTE_CRYSTAL_RED);
		redCrystalHighlighted.setHighlightInInventory(true);
	}

	protected void setupZones()
	{
		f0 = new Zone(new WorldPoint(2565, 6080, 0), new WorldPoint(2740, 6204, 0));
		f1 = new Zone(new WorldPoint(2565, 6080, 1), new WorldPoint(2740, 6204, 1));
		f2 = new Zone(new WorldPoint(2565, 6080, 2), new WorldPoint(2740, 6204, 2));
	}

	protected void setupConditions()
	{
		// Check is for 6 mirrors to cover having placed but not rotated first spot
		hasMirrorsAndCrystal = new Conditions(
			new ItemRequirements(new ItemRequirement("Hand mirror", ItemID.SOTE_MIRROR, 6)),
			new ItemRequirements(new ItemRequirement("Red crystal", ItemID.SOTE_CRYSTAL_RED)));

		onF0 = new ZoneRequirement(f0);
		onF1 = new ZoneRequirement(f1);
		onF2 = new ZoneRequirement(f2);

		int MAGENTA = 4;
		int BLUE = 3;

		r1 = new VarbitRequirement(8971, MAGENTA);
		r2 = new VarbitRequirement(8586, MAGENTA);
		r3 = new VarbitRequirement(8858, MAGENTA);
		r4 = new VarbitRequirement(8854, BLUE);
		r5 = new VarbitRequirement(8853, BLUE);
		r6 = new VarbitRequirement(8844, MAGENTA);
		r7 = new VarbitRequirement(8845, MAGENTA);
	}

	public List<QuestStep> getDisplaySteps()
	{
		return QuestUtil.toArrayList(collectMirrors, p1Pillar1, p1Pillar2, goDownstairs, p1Pillar3, p1Pillar4, p1Pillar5, p1Pillar6, p1Pillar7, p1Pillar8);
	}
}
