package net.runelite.client.plugins.microbot.yfoo.MicroAction;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.yfoo.GeneralUtil.RngUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public abstract class MicroAction implements Comparable<MicroAction> {

    // In case of tie in order, stochastically sort with weighting (higher = more likely to be first)
    int order;
    int weighting;

    public MicroAction(int order, int weighting) {
        this.order = order;
        this.weighting = weighting;
    }

    public abstract boolean doAction();

    public static boolean runActionsInRandomOrder(List<MicroAction> actions) throws InterruptedException {
        actions.sort(MicroAction::compareTo);
        //Microbot.log("Sort Order: " + actions.stream().map(Object::toString).collect(Collectors.joining(",")));
        boolean result = true;
        for(MicroAction a: actions) {
            boolean actionSuccess = a.doAction();
            if(!actionSuccess) {
                result = false;
                Thread.sleep(600);
                break;
            }
            Thread.sleep(RngUtil.gaussian(300, 200, 100, 800));
        }
        return result;
    }

    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    @Override
    public int compareTo(@NotNull MicroAction o) {
        int orderComparison = Integer.compare(this.order, o.order);
        if (orderComparison == 0) {
            if (this.weighting != o.weighting) {
                int totalWeight = this.weighting + o.weighting;
                double probability = (double) this.weighting / totalWeight;
                return ThreadLocalRandom.current().nextDouble() < probability ? -1 : 1;
            }
            return ThreadLocalRandom.current().nextBoolean() ? -1 : 1;
        }
        return orderComparison;
    }

    @Override
    public String toString() {
        return "MicroAction{order=" + order + ", weighting=" + weighting + "}";
    }

    static class TestMicroAction extends MicroAction {
        public TestMicroAction(int order, int weight) {
            super(order, weight);
        }

        @Override
        public boolean doAction() {
            return false;
        }
    }

    public static void main(String[] args) {
        List<MicroAction> actions = new ArrayList<>();

        // Create instances with the same order but different weightings
        for (int i = 1; i <= 5; i++) {
            actions.add(new TestMicroAction(10, i * 2)); // Order = 10, different weights
        }

        for (int i = 1; i <= 5; i++) {
            actions.add(new TestMicroAction(i, i * 2)); // Order = 10, different weights
        }

        System.out.println("Before Sorting:");
        actions.forEach(System.out::println);

        // Sort using the custom comparator
        Collections.sort(actions);

        System.out.println("\nAfter Sorting:");
        actions.forEach(System.out::println);
    }


}
