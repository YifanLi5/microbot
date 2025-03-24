package net.runelite.client.plugins.microbot.pluginscheduler.type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.pluginscheduler.event.ScheduledStopEvent;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Scheduled {
    private transient Plugin plugin;
    private String name;
    private ScheduleType scheduleType;
    private int intervalValue; // The numeric value for the interval
    private String duration; // Optional duration to run the plugin
    private boolean enabled;
    private long lastRunTime; // When the plugin last ran (epoch millis)
    private long nextRunTime; // When the plugin should next run (epoch millis)
    private String cleanName;

    // Static formatter for time display
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public Scheduled(String pluginName, ScheduleType scheduleType, int intervalValue,
                     String duration, boolean enabled) {
        this.name = pluginName;
        this.scheduleType = scheduleType != null ? scheduleType : ScheduleType.HOURS;
        this.intervalValue = Math.max(1, intervalValue); // Ensure interval is at least 1
        this.duration = duration;
        this.enabled = enabled;
        this.cleanName = pluginName.replaceAll("<html>|</html>", "")
                .replaceAll("<[^>]*>([^<]*)</[^>]*>", "$1")
                .replaceAll("<[^>]*>", "");

        // Set nextRunTime to now by default (run immediately)
        this.nextRunTime = roundToMinutes(System.currentTimeMillis());
    }

    public Plugin getPlugin() {
        if (plugin == null) {
            plugin = Microbot.getPluginManager().getPlugins().stream()
                    .filter(p -> Objects.equals(p.getName(), name))
                    .findFirst()
                    .orElse(null);
        }
        return plugin;
    }

    public boolean start() {
        if (getPlugin() == null) {
            return false;
        }

        try {
            Microbot.getClientThread().runOnSeperateThread(() -> {
                Microbot.startPlugin(plugin);
                return false;
            });

            // Update the last run time and calculate next run
            updateAfterRun();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean stop() {
        if (getPlugin() == null) {
            return false;
        }

        try {
            Microbot.getClientThread().runOnSeperateThread(() -> {
                Microbot.getEventBus().post(new ScheduledStopEvent(plugin));
                return false;
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean forceStop() {
        if (getPlugin() == null) {
            return false;
        }

        try {
            Microbot.getClientThread().runOnSeperateThread(() -> {
                Microbot.stopPlugin(plugin);
                return false;
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRunning() {
        return getPlugin() != null && Microbot.getPluginManager().isPluginEnabled(plugin);
    }

    /**
     * Round time to nearest minute (remove seconds and milliseconds)
     */
    private long roundToMinutes(long timeMillis) {
        return (timeMillis / 60000) * 60000;
    }

    /**
     * Set when this plugin should next run
     */
    public void setNextRunTime(long timeMillis) {
        this.nextRunTime = roundToMinutes(timeMillis);
    }

    /**
     * Check if the plugin is due to run
     */
    public boolean isDueToRun(long currentTimeMillis) {
        currentTimeMillis = roundToMinutes(currentTimeMillis);

        if (!enabled) {
            return false;
        }

        return currentTimeMillis >= nextRunTime;
    }

    /**
     * Update the lastRunTime to now and calculate the next run time
     */
    public void updateAfterRun() {
        lastRunTime = roundToMinutes(System.currentTimeMillis());

        // Calculate next run time based on schedule type and interval
        switch (scheduleType) {
            case MINUTES:
                nextRunTime = lastRunTime + TimeUnit.MINUTES.toMillis(intervalValue);
                break;
            case HOURS:
                nextRunTime = lastRunTime + TimeUnit.HOURS.toMillis(intervalValue);
                break;
            case DAYS:
                nextRunTime = lastRunTime + TimeUnit.DAYS.toMillis(intervalValue);
                break;
            default:
                nextRunTime = lastRunTime + TimeUnit.HOURS.toMillis(1); // Default fallback
        }
    }

    /**
     * Get a formatted display of the interval
     */
    public String getIntervalDisplay() {
        return "Every " + intervalValue + " " + scheduleType.toString().toLowerCase();
    }

    /**
     * Get a formatted display of when this plugin will run next
     */
    public String getNextRunDisplay(long currentTimeMillis) {
        currentTimeMillis = roundToMinutes(currentTimeMillis);

        if (!enabled) {
            return "Disabled";
        }

        if (isRunning()) {
            return "Running";
        }

        if (currentTimeMillis >= nextRunTime) {
            return "Ready to run";
        }

        long timeUntilMillis = nextRunTime - currentTimeMillis;
        long hours = TimeUnit.MILLISECONDS.toHours(timeUntilMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeUntilMillis) % 60;

        if (hours > 0) {
            return String.format("In %dh %dm", hours, minutes);
        } else {
            return String.format("In %dm", minutes);
        }
    }

    /**
     * Get a formatted time string for the next run
     */
    public String getNextRunTimeString() {
        return TIME_FORMAT.format(new Date(nextRunTime));
    }

    /**
     * Get the duration in minutes
     */
    public long getDurationMinutes() {
        if (duration == null || duration.isEmpty()) {
            return 0;
        }

        try {
            String[] parts = duration.split(":");
            if (parts.length == 2) {
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                return hours * 60L + minutes;
            }
        } catch (Exception e) {
            // Fall through to return 0
        }
        return 0;
    }

    /**
     * Convert a list of ScheduledPlugin objects to JSON
     */
    public static String toJson(List<Scheduled> plugins) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(plugins);
    }

    /**
     * Parse JSON into a list of ScheduledPlugin objects
     */
    public static List<Scheduled> fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            Gson gson = new GsonBuilder().create();
            Type listType = new TypeToken<ArrayList<Scheduled>>() {
            }.getType();
            List<Scheduled> plugins = gson.fromJson(json, listType);

            // Fix any null scheduleType values
            for (Scheduled plugin : plugins) {
                if (plugin.getScheduleType() == null) {
                    plugin.scheduleType = ScheduleType.HOURS; // Default to HOURS
                }

                // Ensure times are rounded to minutes
                plugin.lastRunTime = plugin.roundToMinutes(plugin.lastRunTime);
                plugin.nextRunTime = plugin.roundToMinutes(plugin.nextRunTime);
            }

            return plugins;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scheduled that = (Scheduled) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(scheduleType, that.scheduleType) &&
                intervalValue == that.intervalValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, scheduleType, intervalValue);
    }
}
