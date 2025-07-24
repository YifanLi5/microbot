package net.runelite.client.plugins.microbot.yfoo.GeneralUtil;

import net.runelite.client.plugins.microbot.Microbot;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


public class RngUtil {

    public static int gaussian(int mean, int stddev, int lowBound, int highBound) {
        int gaussian = (int) Math.abs((ThreadLocalRandom.current().nextGaussian() * stddev + mean));
        if (gaussian < lowBound)
            gaussian = lowBound;
        else if (gaussian > highBound)
            gaussian = highBound;
        return gaussian;
    }

    public static int randomInclusive(int min, int max) {
        return ThreadLocalRandom.current().nextInt((max - min) + 1) + min;
    }

    public static boolean boolD100Roll(int trueChance) {
        return ThreadLocalRandom.current().nextInt(101) <= trueChance;
    }

    public static <T> T rollForWeightedAction(Map<T, Integer> weightedMap) {
        if (weightedMap.isEmpty()) {
            throw new IllegalArgumentException("Weighted map cannot be empty.");
        }

        // Filter out entries with zero or negative weight
        List<Map.Entry<T, Integer>> validEntries = weightedMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .collect(Collectors.toList());

        if (validEntries.isEmpty()) {
            throw new IllegalStateException("No valid entries found in weighted map.");
        }

        // Calculate total weight sum
        int weightSum = validEntries.stream()
                .mapToInt(Map.Entry::getValue)
                .sum();

        // Roll for a random number within weightSum
        int roll = ThreadLocalRandom.current().nextInt(weightSum) + 1;

        // Select an entry based on weighted probability
        for (Map.Entry<T, Integer> entry : validEntries) {
            roll -= entry.getValue();
            if (roll <= 0) {
                return entry.getKey();
            }
        }

        // This should never happen due to correct weight sum calculations
        throw new IllegalStateException("Error: Rolling for an action returned null, this should not happen.");
    }

    public static void main(String[] args) {
        // Example usage
        Map<String, Integer> actions = Map.of(
                "Attack", 50,
                "Defend", 30,
                "Run", 20
        );

        // Roll 10 times to test
        for (int i = 0; i < 10; i++) {
            System.out.println("Selected action: " + rollForWeightedAction(actions));
        }
    }
}
