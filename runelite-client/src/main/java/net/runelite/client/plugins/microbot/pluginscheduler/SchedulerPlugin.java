package net.runelite.client.plugins.microbot.pluginscheduler;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilOnClientThread;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.slf4j.event.Level;
import java.nio.file.Files;
import com.google.inject.Provides;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Notification;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ClientShutdown;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerConfig;
import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerScript;
import net.runelite.client.plugins.microbot.pluginscheduler.api.SchedulablePlugin;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.Condition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.time.TimeCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.event.PluginScheduleEntryFinishedEvent;
import net.runelite.client.plugins.microbot.pluginscheduler.model.PluginScheduleEntry;
import net.runelite.client.plugins.microbot.pluginscheduler.model.PluginScheduleEntry.StopReason;
import net.runelite.client.plugins.microbot.pluginscheduler.ui.SchedulerPanel;
import net.runelite.client.plugins.microbot.pluginscheduler.ui.SchedulerWindow;
import net.runelite.client.plugins.microbot.pluginscheduler.ui.Antiban.AntibanDialogWindow;
import net.runelite.client.plugins.microbot.pluginscheduler.ui.util.SchedulerUIUtils;
import net.runelite.client.plugins.microbot.pluginscheduler.util.SchedulerPluginUtil;
import net.runelite.client.plugins.microbot.qualityoflife.QoLPlugin;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.antiban.enums.ActivityIntensity;
import net.runelite.client.plugins.microbot.util.antiban.enums.CombatSkills;
import net.runelite.client.plugins.microbot.util.events.PluginPauseEvent;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(name = PluginDescriptor.Mocrosoft + PluginDescriptor.VOX
        + "Plugin Scheduler", description = "Schedule plugins at your will", tags = { "microbot", "schedule",
                "automation" }, enabledByDefault = false,priority = false)
public class SchedulerPlugin extends Plugin {
    public static final String VERSION = "0.1.0";
    @Inject
    private SchedulerConfig config;    
    final static String configGroup = "PluginScheduler";

    // Store the original break handler logout setting
    private Boolean savedBreakHandlerLogoutSetting = null;
    private int savedBreakHandlerMaxBreakTime = -1;
    private int savedBreakHandlerMinBreakTime = -1;

    @Provides
    public SchedulerConfig provideConfig(ConfigManager configManager) {
        if (configManager == null) {
            return null;
        }
        return configManager.getConfig(SchedulerConfig.class);
    }

    @Inject
    private ClientToolbar clientToolbar;
    @Inject
    private ScheduledExecutorService executorService;
    @Inject
    private OverlayManager overlayManager;

    private NavigationButton navButton;
    private SchedulerPanel panel;
    private ScheduledFuture<?> updateTask;
    private SchedulerWindow schedulerWindow;
    @Inject
    private SchedulerInfoOverlay overlay;
    @Getter
    private PluginScheduleEntry currentPlugin;
    @Getter
    private PluginScheduleEntry lastPlugin;
    private void setCurrentPlugin(PluginScheduleEntry plugin) {
        // Update last plugin when setting new one
        if (this.currentPlugin != null && plugin != this.currentPlugin) {
            this.lastPlugin = this.currentPlugin;
        }
        this.currentPlugin = plugin;
    }

    /**
     * Returns the list of scheduled plugins
     * @return List of PluginScheduleEntry objects
     */
    @Getter
    private List<PluginScheduleEntry> scheduledPlugins = new ArrayList<>();

    // private final Map<String, PluginScheduleEntry> nextPluginCache = new
    // HashMap<>();

    private int initCheckCount = 0;
    private static final int MAX_INIT_CHECKS = 10;

    @Getter
    private SchedulerState currentState = SchedulerState.UNINITIALIZED;
    private SchedulerState prvState = SchedulerState.UNINITIALIZED;
    private GameState lastGameState = GameState.UNKNOWN;

    // Activity and state tracking
    private final Map<Skill, Integer> skillExp = new EnumMap<>(Skill.class);
    private Skill lastSkillChanged;
    private Instant lastActivityTime = Instant.now();
    private Instant loginTime;
    private Activity currentActivity;
    private ActivityIntensity currentIntensity;
    @Getter
    private int idleTime = 0;
    // Break tracking
    private Duration currentBreakDuration = Duration.ZERO;
    private Duration timeUntilNextBreak = Duration.ZERO;
    private Optional<ZonedDateTime> breakStartTime = Optional.empty();
    // login tracking
    private Thread loginMonitor;
    private boolean hasDisabledQoLPlugin = false;
    @Inject
    private Notifier notifier;
    
    // UI update throttling
    private long lastPanelUpdateTime = 0;
    private static final long PANEL_UPDATE_THROTTLE_MS = 500; // Minimum 500ms between panel updates
    @Override
    protected void startUp() {
        hasDisabledQoLPlugin=false;
        panel = new SchedulerPanel(this);

        final BufferedImage icon = ImageUtil.loadImageResource(SchedulerPlugin.class, "calendar-icon.png");
        navButton = NavigationButton.builder()
                .tooltip("Plugin Scheduler")
                .priority(10)
                .icon(icon)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);

        // Enable overlay if configured
        if (config.showOverlay()) {
            overlayManager.add(overlay);
        }

        // Load saved schedules from config

        // Check initialization status before fully enabling scheduler
        //checkInitialization();

        // Run the main loop
        updateTask = executorService.scheduleWithFixedDelay(() -> {
            SwingUtilities.invokeLater(() -> {
                // Only run scheduling logic if fully initialized
                if (currentState.isSchedulerActive()) {
                    checkSchedule();
                } else if (currentState == SchedulerState.INITIALIZING
                        || currentState == SchedulerState.UNINITIALIZED) {
                    // Retry initialization check if not already checking
                    checkInitialization();
                }
                updatePanels();
            });
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Checks if all required plugins are loaded and initialized.
     * This runs until initialization is complete or max check count is reached.
     */
    private void checkInitialization() {
        if (!currentState.isInitializing()) {
            return;
        }
        if (Microbot.getClientThread() == null || Microbot.getClient() == null) {
            return;
        }
        setState(SchedulerState.INITIALIZING);       
        // Schedule repeated checks until initialized or max checks reached
        
        Microbot.getClientThread().invokeLater(() -> {
            // Check if client is at login screen
            List<Plugin> conditionProviders = new ArrayList<>();
            if (Microbot.getPluginManager() == null || Microbot.getClient() == null) {
                return;

            } else {
                // Find all plugins implementing ConditionProvider
                conditionProviders = Microbot.getPluginManager().getPlugins().stream()
                        .filter(plugin -> plugin instanceof SchedulablePlugin)
                        .collect(Collectors.toList());
                List<Plugin> enabledList = conditionProviders.stream()
                        .filter(plugin -> Microbot.getPluginManager().isPluginEnabled(plugin))
                        .collect(Collectors.toList());
            }

            boolean isAtLoginScreen = Microbot.getClient().getGameState() == GameState.LOGIN_SCREEN;
            boolean isLoggedIn = Microbot.getClient().getGameState() == GameState.LOGGED_IN;
            boolean isAtLoginAuth = Microbot.getClient().getGameState() == GameState.LOGIN_SCREEN_AUTHENTICATOR;
            // If any conditions met, mark as initialized
            if (isAtLoginScreen || isLoggedIn || isAtLoginAuth) {
                log.info("Scheduler initialization complete - {} stopping condition providers loaded",
                        conditionProviders.size());

                loadScheduledPlugin();
                for (Plugin plugin : conditionProviders) {
                    try {
                        Microbot.getClientThread().runOnSeperateThread(() -> {
                            Microbot.stopPlugin(plugin);
                            return false;
                        });
                    } catch (Exception e) {
                    }
                }
                setState(SchedulerState.READY);

                // Initial cleanup of one-time plugins after loading
                cleanupCompletedOneTimePlugins();
            }
            // If max checks reached, mark as initialized but log warning
            else if (++initCheckCount >= MAX_INIT_CHECKS) {
                log.warn("Scheduler initialization timed out");
                loadScheduledPlugin();

                setState(SchedulerState.ERROR);
            }
            // Otherwise, schedule another check
            else {
                log.info("Waiting for initialization: loginScreen={}, providers={}/{}, checks={}/{}",
                        isAtLoginScreen,
                        conditionProviders.stream().count(),
                        conditionProviders.size(),
                        initCheckCount,
                        MAX_INIT_CHECKS);
                setState(SchedulerState.INITIALIZING);
                checkInitialization();
            }
        });

    }

    public void openSchedulerWindow() {
        if (schedulerWindow == null) {
            schedulerWindow = new SchedulerWindow(this);
        }

        if (!schedulerWindow.isVisible()) {
            schedulerWindow.setVisible(true);
        } else {
            schedulerWindow.toFront();
            schedulerWindow.requestFocus();
        }
    }

    @Override
    protected void shutDown() {
        saveScheduledPlugins();
        clientToolbar.removeNavigation(navButton);
        overlayManager.remove(overlay);
        forceStopCurrentPluginScheduleEntry(true);
        interruptBreak();
        for (PluginScheduleEntry entry : scheduledPlugins) {
            entry.close();
        }
        if (this.loginMonitor != null && this.loginMonitor.isAlive()) {
            this.loginMonitor.interrupt();
            this.loginMonitor = null;
        }
        if (updateTask != null) {
            updateTask.cancel(false);
            updateTask = null;
        }

        if (schedulerWindow != null) {
            schedulerWindow.dispose(); // This will stop the timer
            schedulerWindow = null;
        }
        setState(SchedulerState.UNINITIALIZED);
        this.lastGameState = GameState.UNKNOWN;
    }

    /**
     * Starts the scheduler
     */
    public void startScheduler() {
        Microbot.log("Starting scheduler request...", Level.INFO);
        Microbot.getClientThread().runOnClientThreadOptional(() -> {
            // If already active, nothing to do
            if (currentState.isSchedulerActive()) {
                log.info("Scheduler already active");
                return true;
            }
            // If initialized, start immediately
            if (SchedulerState.READY == currentState || currentState == SchedulerState.HOLD) {
                setState(SchedulerState.SCHEDULING);
                log.info("Plugin Scheduler started");
                
                // Check schedule immediately when started
                SwingUtilities.invokeLater(() -> {
                    checkSchedule();
                });
                return true;
            }
            return true;
        });
        return;
    }

    /**
     * Stops the scheduler
     */
    public void stopScheduler() {
        if (loginMonitor != null && loginMonitor.isAlive()) {
            loginMonitor.interrupt();
        }
        Microbot.getClientThread().runOnClientThreadOptional(() -> {
            if (!currentState.isSchedulerActive()) {
                return false; // Already stopped
            }
            setState(SchedulerState.HOLD);
            log.info("Stopping scheduler...");
            if (currentPlugin != null) {
                forceStopCurrentPluginScheduleEntry(true);
            }
            // Restore the original logout setting if it was stored
            if (savedBreakHandlerLogoutSetting != null) {
                Microbot.getConfigManager().setConfiguration(BreakHandlerConfig.configGroup, "Logout", (boolean)savedBreakHandlerLogoutSetting);
                log.info("Restored original logout setting: {}", savedBreakHandlerLogoutSetting);
                savedBreakHandlerLogoutSetting = null; // Clear the stored value
                
            }
            if (savedBreakHandlerMaxBreakTime != -1) {
               Microbot.getConfigManager().setConfiguration(BreakHandlerConfig.configGroup, "Max BreakTime", (int)savedBreakHandlerMaxBreakTime); 
               savedBreakHandlerMaxBreakTime = -1; // Clear the stored value
            }
            if (savedBreakHandlerMinBreakTime != -1) {
                Microbot.getConfigManager().setConfiguration(BreakHandlerConfig.configGroup, "Min BreakTime", (int)savedBreakHandlerMinBreakTime);
                savedBreakHandlerMinBreakTime = -1; // Clear the stored value
            }

            // Final state after fully stopped, disable the plugins we auto-enabled
            if (isBreakHandlerEnabled() && config.enableBreakHandlerForSchedule()) {
                if (disableBreakHandler()) {
                    log.info("Automatically disabled BreakHandler plugin");
                }
            }
            if (hasDisabledQoLPlugin){
                SchedulerPluginUtil.enablePlugin(QoLPlugin.class);
            }

            interruptBreak();
            setState(SchedulerState.HOLD);
            if(config.autoLogOutOnStop()){
                logout();
            }

            log.info("Scheduler stopped - status: {}", currentState);
            return false;
        });
    }
    private boolean checkBreakAndLoginStatus() {     
        if (currentPlugin!=null){
            if ( !isOnBreak()
                && (currentState.isBreaking()) 
                && prvState == SchedulerState.RUNNING_PLUGIN) {
                log.info("Plugin '{}' is paused, but break is not active, resuming plugin",
                        currentPlugin.getCleanName());
                setState(SchedulerState.RUNNING_PLUGIN); // Ensure the break state is set before pausing
                currentPlugin.resume();
                if(config.pauseSchedulerDuringBreak() || allPluginEntryPaused()){
                    log.info("\n---config for pause scheduler during break is enabled, resuming all scheduled plugins");
                    resumeAllScheduledPlugins();                    
                }
                return true; // Do not continue checking schedule if plugin is running

            }else if (isOnBreak() && currentState == SchedulerState.RUNNING_PLUGIN
                        ) {
                log.info("Plugin '{}' is running, but break is active, pausing plugin",
                        currentPlugin.getCleanName());
                setState(SchedulerState.BREAK); // Ensure the break state is set before pausing
                if (config.pauseSchedulerDuringBreak() || allPluginEntryPaused()) {
                    log.info("\n---config for pause scheduler during break is enabled, pausing all scheduled plugins");
                    pauseAllScheduledPlugins();
                }
                currentPlugin.pause(); 

                return true; // Do not continue checking schedule if plugin is running            
            }else if (currentPlugin.isRunning() && currentState.isPluginRunning()) {
                
                if (!Microbot.isLoggedIn()){
                    if (!isAutoLoginEnabled() ){
                        log.info("Plugin '{}' is running, but not logged in and auto-login is disabled, starting login monitoring",
                                currentPlugin.getCleanName());
                        startLoginMonitoringThread();
                        return true; // If not logged in, wait for login
                    }else if (isAutoLoginEnabled() ) {
                        log.info(" wait for auto-login to complete for plugin '{}'",
                                currentPlugin.getCleanName());  
                        if( (Microbot.pauseAllScripts.get() || PluginPauseEvent.isPaused() ) && !currentState.isPaused()){
                            Microbot.pauseAllScripts.set(false);
                            PluginPauseEvent.setPaused(hasDisabledQoLPlugin);
                        }
                        return true; // If not logged in, wait for login
                    }        
                }        
            }
        }
        return false;
    }
    private void checkSchedule() {            
        // Update break status
        if (SchedulerState.LOGIN == currentState ||
                SchedulerState.WAITING_FOR_LOGIN == currentState ||
                SchedulerState.HARD_STOPPING_PLUGIN == currentState ||
                SchedulerState.SOFT_STOPPING_PLUGIN == currentState ||
                currentState == SchedulerState.HOLD
                // Skip if scheduler is paused
               ) { // Skip if running plugin is paused
            return;
        }
        
        if(checkBreakAndLoginStatus()){
            return;
        }
        
        // First, check if we need to stop the current plugin
        if (isScheduledPluginRunning()) {
            checkCurrentPlugin();

        }
       
        // If no plugin is running, check for scheduled plugins
        if (!isScheduledPluginRunning()) {            
            int minBreakDuration = config.minBreakDuration();
            PluginScheduleEntry nextUpComingPluginPossibleWithInTime = null;
            PluginScheduleEntry nextUpComingPluginPossible = getNextScheduledPlugin(false, null).orElse(null);
          
            if (minBreakDuration == 0) { // 0 means no break
                minBreakDuration = 1;                
                nextUpComingPluginPossibleWithInTime = getNextScheduledPlugin(true, null).orElse(null);
            } else {
                minBreakDuration = Math.max(1, minBreakDuration);                
                // Get the next scheduled plugin within minBreakDuration
                nextUpComingPluginPossibleWithInTime = getUpComingPluginWithinTime(
                        Duration.ofMinutes(minBreakDuration));
            }

            if (    (nextUpComingPluginPossibleWithInTime == null && 
                    nextUpComingPluginPossible != null && 
                    !nextUpComingPluginPossible.hasOnlyTimeConditions() 
                    && !isOnBreak() && !Microbot.isLoggedIn()) ){                    
                // when the the next possible plugin is not a time condition and we are not logged in
                log.info("\n\nLogin required before the next possible plugin{}can run, start login before hand", nextUpComingPluginPossible.getCleanName());
                
                startLoginMonitoringThread();
                return;                
            }
            
            if (nextUpComingPluginPossibleWithInTime != null 
                && nextUpComingPluginPossibleWithInTime.getCurrentStartTriggerTime().isPresent() 
                && (!config.usePlaySchedule() || !config.playSchedule().isOutsideSchedule())) {           
                boolean nextWithinFlag = false;

                int withinSeconds = Rs2Random.between(15, 30); // is there plugin upcoming within 15-30, than we stop
                                                               // the break
                
                if (nextUpComingPluginPossibleWithInTime.getCurrentStartTriggerTime().isPresent()) {
                    nextWithinFlag = Duration
                            .between(ZonedDateTime.now(ZoneId.systemDefault()),
                                    nextUpComingPluginPossibleWithInTime.getCurrentStartTriggerTime().get())
                            .compareTo(Duration.ofSeconds(withinSeconds)) < 0;
                } else {
                    if (nextUpComingPluginPossibleWithInTime.isDueToRun()) {
                        nextWithinFlag = true;
                    }else {
                        
                    }
                }
                // If we're on a break, interrupt it

                if (isOnBreak() && (nextWithinFlag)) {
                    log.info("\n\tInterrupting active break to start scheduled plugin: {}", nextUpComingPluginPossibleWithInTime.getCleanName());
                    setState(SchedulerState.BREAK); //ensure the break state is set before interrupting
                    interruptBreak();

                }
                if (currentState.isBreaking() && nextWithinFlag) {
                    setState(SchedulerState.WAITING_FOR_SCHEDULE);
                }

                if (!currentState.isPluginRunning() && !currentState.isAboutStarting()) {                    
                    scheduleNextPlugin();
                } else {
                    if(currentPlugin == null){                                                
                        setState(SchedulerState.WAITING_FOR_SCHEDULE);                        
                    }else{
                        if (!currentPlugin.isRunning() && !currentState.isAboutStarting()) {
                            setState( SchedulerState.WAITING_FOR_SCHEDULE);
                            log.info("Plugin is not running, and it not about to start");
                        }
                        checkCurrentPlugin();   
                        
                    }
                }
            } else {
                if(config.usePlaySchedule() && config.playSchedule().isOutsideSchedule() && currentState != SchedulerState.PLAYSCHEDULE_BREAK){
                    log.info("\n\tOutside play schedule, starting not started break");
                    startBreakBetweenSchedules(config.autoLogOutOnBreak(), 1, 2);                    
                }else if(!isOnBreak() &&
                    currentState != SchedulerState.WAITING_FOR_SCHEDULE &&
                    currentState == SchedulerState.SCHEDULING) {
                     // If we're not on a break and there's nothing running, take a short break until
                // next plugin
                    int minDuration = config.minBreakDuration();
                    int maxDuration = config.maxBreakDuration();                    
                    if(nextUpComingPluginPossibleWithInTime != null && nextUpComingPluginPossibleWithInTime.getCurrentStartTriggerTime().isPresent()){
                        ZonedDateTime nextPluginTriggerTime = null;
                        nextPluginTriggerTime = nextUpComingPluginPossibleWithInTime.getCurrentStartTriggerTime().get();
                        int maxMinIntervall =  maxDuration - minDuration;
                        minBreakDuration = (int) Duration.between(ZonedDateTime.now(ZoneId.systemDefault()), nextPluginTriggerTime).toMinutes() ;
                        maxDuration = minBreakDuration + maxMinIntervall;
                    }
                    
                    startBreakBetweenSchedules(config.autoLogOutOnBreak(), minDuration, maxDuration);
                }else if(currentState != SchedulerState.WAITING_FOR_SCHEDULE && currentState.isBreaking()){
                    //make a resume break function  when no plugin is upcoming and the left break time is smaller than "threshold"
                    //currentBreakDuration  -> last set break duration type "Duration"
                    //breakStartTime breakStartTime -> last set break start time type "Optional<ZonedDateTime>"
                    //breakStartTime.get().plus(currentBreakDuration) -> break end time type "ZonedDateTime"                    
                    extendBreakIfNeeded(nextUpComingPluginPossibleWithInTime, 30);
                }
            }

        }
        // Clean up completed one-time plugins
        cleanupCompletedOneTimePlugins();

    }
    public void resumeBreak() {
        if (currentState == SchedulerState.PLAYSCHEDULE_BREAK){
            // If we are in a play schedule break, we need to reset the state, because otherwise we would break agin, because we are still outside the play schedule
            Microbot.getConfigManager().setConfiguration(SchedulerPlugin.configGroup, "usePlaySchedule", false);
        }
        interruptBreak();
    }
    /**
     * Interrupts an active break to allow a plugin to start
     */
    private void interruptBreak() {
        if (!isOnBreak() || (!currentState.isPaused() && !currentState.isBreaking())) {
            return;
        }
        this.currentBreakDuration = Duration.ZERO;
        breakStartTime = Optional.empty();
        // Set break duration to 0 to end the break
        //BreakHandlerScript.breakDuration = 0;
        if (!isBreakHandlerEnabled()) {
            return;
        }

        log.info("Interrupting active break to start scheduled plugin");

        

        // Also reset the breakNow setting if it was set
        Microbot.getConfigManager().setConfiguration(BreakHandlerConfig.configGroup, "breakNow", false);
        // Set break end now to true to force end the break immediately
        Microbot.getConfigManager().setConfiguration(BreakHandlerConfig.configGroup, "breakEndNow", true);

        // Restore the original logout setting if it was stored
        if (savedBreakHandlerLogoutSetting != null) {
            Microbot.getConfigManager().setConfiguration(BreakHandlerConfig.configGroup, "Logout", savedBreakHandlerLogoutSetting);
            log.info("Restored original logout setting: {}", savedBreakHandlerLogoutSetting);
            savedBreakHandlerLogoutSetting = null; // Clear the stored value
        }
        // Restore the original max break time if it was stored
        if (savedBreakHandlerMaxBreakTime != -1) {
               Microbot.getConfigManager().setConfiguration(BreakHandlerConfig.configGroup, "Max BreakTime", savedBreakHandlerMaxBreakTime); 
               savedBreakHandlerMaxBreakTime = -1; // Clear the stored value
        }
        if (savedBreakHandlerMinBreakTime != -1) {
            Microbot.getConfigManager().setConfiguration(BreakHandlerConfig.configGroup, "Min BreakTime", savedBreakHandlerMinBreakTime);
            savedBreakHandlerMinBreakTime = -1; // Clear the stored value
        }

        // Ensure we're not locked for future breaks
        unlockBreakHandler();

        // Wait a moment for the break to fully end
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (BreakHandlerScript.isBreakActive()) {
            SwingUtilities.invokeLater(() -> {
                log.info("\n\t--Break was not interrupted successfully");
                interruptBreak();
            });
            return;
        }
        log.info("\n\t--Break interrupted successfully");
        if ( isBreakHandlerEnabled() && !config.enableBreakHandlerForSchedule()) {
            if (disableBreakHandler()) {
                log.info("Automatically disabled BreakHandler plugin, should not be used for scheduling");
            }
        }
        if (currentState.isBreaking()) {
            // If we were on a break, reset the state to scheduling
            log.info("Resetting state after break interruption currentState: {} prev.  state {} ", currentState,prvState);
            setState(prvState);// before it was SchedulerState.WAITING_FOR_SCHEDULE
        } else {
            if (!currentState.isPaused()) {
                // If we were paused, reset to the previous state
                log.info("Resetting state after break interruption currentState: {} prev.  state {} ", currentState,prvState);
                // Otherwise, set to waiting for schedule
                throw new IllegalStateException("Scheduler state is not breaking or paused, cannot reset to SCHEDULING");
            }
            
            //setState(prvState);// before it was SchedulerState.SCHEDULING
        }
    }

    /**
     * Hard resets all user conditions for all plugins in the scheduler
     * @return A list of plugin names that were reset
     */
    public List<String> hardResetAllUserConditions() {
        List<String> resetPlugins = new ArrayList<>();
        
        for (PluginScheduleEntry entry : scheduledPlugins) {
            if (entry != null) {
                // Get the condition managers from the entry
                try {
                    entry.hardResetConditions();
                } catch (Exception e) {
                    log.error("Error resetting conditions for plugin " + entry.getCleanName(), e);
                }
            }
        }
        
        return resetPlugins;
    }
    /**
     * Starts a short break until the next plugin is scheduled to run
     */
    private boolean startBreakBetweenSchedules(boolean logout, 
        int minBreakDurationMinutes, int maxBreakDurationMinutes) {
        StringBuilder logBuilder = new StringBuilder();
        
        if (!isBreakHandlerEnabled()) {
            if (SchedulerPluginUtil.enableBreakHandler()) {
                logBuilder.append("\n\tAutomatically enabled BreakHandler plugin");
            }
            log.info(logBuilder.toString());
            return false;
        }
        
        if (BreakHandlerScript.isLockState())
            BreakHandlerScript.setLockState(false);
        
        PluginScheduleEntry nextUpComingPlugin = getUpComingPlugin();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        Duration timeUntilNext = Duration.ZERO;
        
        // Check if we're outside play schedule
        if (config.usePlaySchedule() && config.playSchedule().isOutsideSchedule()) {
            Duration untilNextSchedule = config.playSchedule().timeUntilNextSchedule();
            logBuilder.append("\n\tOutside play schedule")
                      .append("\n\t\tNext schedule in: ").append(formatDuration(untilNextSchedule));
            
            // Configure a break until the next play schedule time
            BreakHandlerScript.breakDuration = (int) untilNextSchedule.getSeconds();
            this.currentBreakDuration = untilNextSchedule;
            BreakHandlerScript.breakIn = 0;
            
            // Store the original logout setting before changing it
            savedBreakHandlerLogoutSetting = Microbot.getConfigManager().getConfiguration(
                BreakHandlerConfig.configGroup, "Logout", Boolean.class);
            // Set the new logout setting            
            Microbot.getConfigManager().setConfiguration(BreakHandlerConfig.configGroup, "Logout", true);
            
            if (untilNextSchedule.getSeconds() > 60){
                savedBreakHandlerMaxBreakTime = Microbot.getConfigManager().getConfiguration(
                    BreakHandlerConfig.configGroup, "Max BreakTime", Integer.class);
                Microbot.getConfigManager().setConfiguration(BreakHandlerConfig.configGroup, "Max BreakTime",(int)(untilNextSchedule.toMinutes()));
                savedBreakHandlerMinBreakTime = Microbot.getConfigManager().getConfiguration(
                    BreakHandlerConfig.configGroup, "Min BreakTime", Integer.class);
                Microbot.getConfigManager().setConfiguration(BreakHandlerConfig.configGroup, "Min BreakTime",(int)(untilNextSchedule.toMinutes()));                
            }
            
            // Set state to indicate we're in a break
            sleepUntil(() -> BreakHandlerScript.isBreakActive(), 1000);
            
            if (!BreakHandlerScript.isBreakActive()) {
                logBuilder.append("\n\t\tWarning: Break handler is not active, unable to start break for play schedule");
                log.info(logBuilder.toString());
                return false;
            }
            
            setState(SchedulerState.PLAYSCHEDULE_BREAK);
            log.info(logBuilder.toString());
            return true;
        }
        
        // Store the original logout setting before changing it
        savedBreakHandlerLogoutSetting = Microbot.getConfigManager().getConfiguration(
            BreakHandlerConfig.configGroup, "Logout", Boolean.class);        
        // Set the new logout setting
        Microbot.getConfigManager().setConfiguration(BreakHandlerConfig.configGroup, "Logout", logout);
       
        // Determine the time until the next plugin is scheduled
        if (nextUpComingPlugin != null) {
            Optional<ZonedDateTime> nextStartTime = nextUpComingPlugin.getCurrentStartTriggerTime();
            if (nextStartTime.isPresent()) {
                timeUntilNext = Duration.between(now, nextStartTime.get());
            }
        }
        
        // Determine the break duration based on config and next plugin time
        long breakSeconds;
        
        // Calculate a random break duration between min and max
        int randomBreakMinutes = Rs2Random.between(minBreakDurationMinutes, maxBreakDurationMinutes);
        breakSeconds = randomBreakMinutes * 60;
        
        logBuilder.append("\n\tStarting break between schedules")
                  .append("\n\t\tInitial break duration: ").append(formatDuration(Duration.ofSeconds(breakSeconds)));
        
        // If there's a next plugin scheduled, make sure we don't break past its start time
        if (nextUpComingPlugin != null && timeUntilNext.toSeconds() > 0) {
            // Subtract 30 seconds buffer to ensure we're back before the plugin needs to start            
            long maxBreakForNextPlugin = timeUntilNext.toSeconds() - 30;
            if (maxBreakForNextPlugin > 60) { // Only consider breaks that would be at least 1 minute
                breakSeconds = Math.max(breakSeconds, maxBreakForNextPlugin);
                logBuilder.append("\n\t\tLimited break duration to: ").append(formatDuration(Duration.ofSeconds(breakSeconds)))
                          .append("\n\t\tUpcoming plugin: ").append(nextUpComingPlugin.getCleanName())
                          .append(" (in ").append(formatDuration(timeUntilNext)).append(")");
            }
            
            logBuilder.append("\n\t\tNext plugin scheduled:")
                      .append("\n\t\t\tTime until next: ").append(formatDuration(timeUntilNext))
                      .append("\n\t\t\tNext start time: ").append(nextUpComingPlugin.getCurrentStartTriggerTime().get())
                      .append("\n\t\t\tCurrent time: ").append(now);
        }
        
        if (breakSeconds > 0){
            this.savedBreakHandlerMaxBreakTime = Microbot.getConfigManager().getConfiguration(
                BreakHandlerConfig.configGroup, "Max BreakTime", Integer.class);
            this.savedBreakHandlerMinBreakTime = Microbot.getConfigManager().getConfiguration(
                BreakHandlerConfig.configGroup, "Min BreakTime", Integer.class);
            
            int maxBreakMinutes = (int)(breakSeconds / 60) + 1;
            int minBreakMinutes = (int)(breakSeconds / 60);
            
            logBuilder.append("\n\t\tConfiguring BreakHandler:")
                      .append("\n\t\t\tMax break time: ").append(maxBreakMinutes).append(" minutes")
                      .append("\n\t\t\tMin break time: ").append(minBreakMinutes).append(" minutes");
            
            Microbot.getConfigManager().setConfiguration(BreakHandlerConfig.configGroup, "Max BreakTime", maxBreakMinutes);
            Microbot.getConfigManager().setConfiguration(BreakHandlerConfig.configGroup, "Min BreakTime", minBreakMinutes);
        }
        
        int currentMaxBreakTimeBreakHandler = Microbot.getConfigManager().getConfiguration(
            BreakHandlerConfig.configGroup, "Max BreakTime", Integer.class);
        int currentMinBreakTimeBreakHandler = Microbot.getConfigManager().getConfiguration(
            BreakHandlerConfig.configGroup, "Min BreakTime", Integer.class);
        
        logBuilder.append("\n\t\tCurrent break handler settings:")
                  .append("\n\t\t\tMin: ").append(currentMinBreakTimeBreakHandler).append(" minutes")
                  .append("\n\t\t\tMax: ").append(currentMaxBreakTimeBreakHandler).append(" minutes");
        
        if (breakSeconds < 60) {
            // Break would be too short, don't take one
            logBuilder.append("\n\t\tNot taking break - duration would be less than 1 minute");
            savedBreakHandlerLogoutSetting = null; // Clear the stored value
            log.info(logBuilder.toString());
            return false;
        }
        
        logBuilder.append("\n\t\tFinal break duration: ").append(formatDuration(Duration.ofSeconds(breakSeconds)));
        
        // Configure the break
        BreakHandlerScript.breakDuration = (int) breakSeconds;
        this.currentBreakDuration = Duration.ofSeconds(breakSeconds);
        BreakHandlerScript.breakIn = 0;
        
        // Set state to indicate we're in a break
        sleepUntil(() -> BreakHandlerScript.isBreakActive(), 1000);
        
        if (!BreakHandlerScript.isBreakActive()) {
            logBuilder.append("\n\t\tError: Break handler is not active, unable to start break");
            log.info(logBuilder.toString());
            return false;
        }
        
        setState(SchedulerState.BREAK);
        logBuilder.append("\n\t\tBreak successfully started");
        
        log.info(logBuilder.toString());
        return true;
    }

    /**
     * Format a duration for display
     */
    private String formatDuration(Duration duration) {
        return SchedulerPluginUtil.formatDuration(duration);
    }

    /**
     * Schedules the next plugin to run if none is running
     */
    private void scheduleNextPlugin() {
        // Check if a non-default plugin is coming up soon
        boolean prioritizeNonDefaultPlugins = config.prioritizeNonDefaultPlugins();
        int nonDefaultPluginLookAheadMinutes = config.nonDefaultPluginLookAheadMinutes();
        
        if (prioritizeNonDefaultPlugins) {
            // Look for any upcoming non-default plugin within the configured time window
            PluginScheduleEntry upcomingNonDefault = getNextScheduledPlugin(false, 
                                                                                Duration.ofMinutes(nonDefaultPluginLookAheadMinutes))
                .filter(plugin -> !plugin.isDefault())
                .orElse(null);
                
            // If we found an upcoming non-default plugin, check if it's already due to run
            if (upcomingNonDefault != null && !upcomingNonDefault.isDueToRun()) {
                // Get the next plugin that's due to run now
                Optional<PluginScheduleEntry> nextDuePlugin = getNextScheduledPlugin(true, null);
                
                // If the next due plugin is a default plugin, don't start it
                // Instead, wait for the non-default plugin
                if (nextDuePlugin.isPresent() && nextDuePlugin.get().isDefault()) {
                    log.info("\nNot starting default plugin '{}' because non-default plugin '{}' is scheduled within {}[configured] minutes",
                        nextDuePlugin.get().getCleanName(),
                        upcomingNonDefault.getCleanName(),
                        nonDefaultPluginLookAheadMinutes);
                    return;
                }
            }
        }
        
        // Get the next plugin that's due to run
        Optional<PluginScheduleEntry> selected = getNextScheduledPlugin(true, null);
        if (selected.isEmpty()) {
            return;
        }
        
        // If we're on a break, interrupt it, only we have initialized the break
        if (isOnBreak() && currentBreakDuration != null && currentBreakDuration.getSeconds() > 0) {
            log.info("\nInterrupting active break to start scheduled plugin: \n\t{}", selected.get().getCleanName());
            interruptBreak();
        }
     
       
        log.info("\nStarting scheduled plugin: \n\t{}\ncurrent state \n\t{}", selected.get().getCleanName(),this.currentState);
        startPluginScheduleEntry(selected.get());
        if (!selected.get().isRunning()) {
            saveScheduledPlugins();
        }
    }

  
    public void startPluginScheduleEntry(PluginScheduleEntry scheduledPlugin) {
        
        Microbot.getClientThread().runOnClientThreadOptional(() -> {

            if (scheduledPlugin == null)
                return false;
            // Ensure BreakHandler is enabled when we start a plugin
            if (!SchedulerPluginUtil.isBreakHandlerEnabled() && config.enableBreakHandlerForSchedule()) {
                log.info("Start enabling BreakHandler plugin");
                if (SchedulerPluginUtil.enableBreakHandler()) {
                    log.info("Automatically enabled BreakHandler plugin");
                }
            }

            // Ensure Antiban is enabled when we start a plugin -> should be allways
            // enabled?
            if (!SchedulerPluginUtil.isAntibanEnabled()) {
                log.info("Start enabling Antiban plugin");
                if (SchedulerPluginUtil.enableAntiban()) {
                    log.info("Automatically enabled Antiban plugin");
                }
            }
            // Ensure QoL is disabled when we start a plugin
            if (SchedulerPluginUtil.isPluginEnabled(QoLPlugin.class)) {
                log.info("Disabling QoL plugin");
                if (SchedulerPluginUtil.disablePlugin(QoLPlugin.class)) {
                    hasDisabledQoLPlugin = true;
                    log.info("Automatically disabled QoL plugin");
                }
            }
            
            // Ensure break handler is unlocked before starting a plugin
            SchedulerPluginUtil.unlockBreakHandler();

            // If we're on a break, interrupt it
            if (isOnBreak()) {
                interruptBreak();
            }
            SchedulerState stateBeforeScheduling = currentState;
            setCurrentPlugin(scheduledPlugin);
            

            // Check for stop conditions if enforcement is enabled -> ensure we have stop
            // condition so the plugin doesn't run forever (only manual stop possible
            // otherwise)
            if (    config.enforceTimeBasedStopCondition() 
                    && scheduledPlugin.isNeedsStopCondition()
                    && scheduledPlugin.getStopConditionManager().getUserTimeConditions().isEmpty() 
                    && SchedulerState.SCHEDULING == currentState) {
                // If the user chooses to add stop conditions, we wait for them to be added
                // and then continue the scheduling process
                // If the user chooses not to add stop conditions, we proceed with the plugin
                // start
                // If the user cancels, we reset the state and do not start the plugin
                // Show confirmation dialog on EDT to prevent blocking
                // Start the dialog in a separate thread to avoid blocking the EDT
                setState(SchedulerState.WAITING_FOR_STOP_CONDITION);
                startAddStopConditionDialog(scheduledPlugin, stateBeforeScheduling);
                log.info("No stop conditions set for plugin: " + scheduledPlugin.getCleanName());
                return false;
            } else {
                if (currentState != SchedulerState.STARTING_PLUGIN){
                    setState(SchedulerState.STARTING_PLUGIN);
                    // Stop conditions exist or enforcement disabled - proceed normally
                    continueStartingPluginScheduleEntry(scheduledPlugin);
                }
                return true;
            }
        });
    }

    private void startAddStopConditionDialog(PluginScheduleEntry scheduledPlugin,
            SchedulerState stateBeforeScheduling) {
        // Show confirmation dialog on EDT to prevent blocking
        Microbot.getClientThread().runOnSeperateThread(() -> {
            // Create dialog with timeout
            final JOptionPane optionPane = new JOptionPane(
                    "Plugin '" + scheduledPlugin.getCleanName() + "' has no stop time based conditions set.\n" +
                            "It will run until manually stopped or a other condition (when defined).\n\n" +
                            "Would you like to configure stop conditions now?",
                    JOptionPane.QUESTION_MESSAGE,
                    JOptionPane.YES_NO_CANCEL_OPTION);

            final JDialog dialog = optionPane.createDialog("No Stop Conditions");

            // Create timer for dialog timeout
            int timeoutSeconds = config.dialogTimeoutSeconds();
            if (timeoutSeconds <= 0) {
                timeoutSeconds = 30; // Default timeout if config value is invalid
            }

            final Timer timer = new Timer(timeoutSeconds * 1000, e -> {
                dialog.setVisible(false);
                dialog.dispose();
            });
            timer.setRepeats(false);
            timer.start();

            // Update dialog title to show countdown
            final int finalTimeoutSeconds = timeoutSeconds;
            final Timer countdownTimer = new Timer(1000, new ActionListener() {
                int remainingSeconds = finalTimeoutSeconds;

                @Override
                public void actionPerformed(ActionEvent e) {
                    remainingSeconds--;
                    if (remainingSeconds > 0) {
                        dialog.setTitle("No Stop Conditions (Timeout: " + remainingSeconds + "s)");
                    } else {
                        dialog.setTitle("No Stop Conditions (Timing out...)");
                    }
                }
            });
            countdownTimer.start();

            try {
                dialog.setVisible(true); // blocks until dialog is closed or timer expires
            } finally {
                timer.stop();
                countdownTimer.stop();
            }

            // Handle user choice or timeout
            Object selectedValue = optionPane.getValue();
            int result = selectedValue instanceof Integer ? (Integer) selectedValue : JOptionPane.CLOSED_OPTION;
            log.info("User selected: " + result);
            if (result == JOptionPane.YES_OPTION) {
                // User wants to add stop conditions
                openSchedulerWindow();
                if (schedulerWindow != null) {
                    // Switch to stop conditions tab
                    schedulerWindow.selectPlugin(scheduledPlugin);
                    schedulerWindow.switchToStopConditionsTab();
                    schedulerWindow.toFront();

                    // Start a timer to check if conditions have been added
                    int conditionTimeoutSeconds = config.conditionConfigTimeoutSeconds();
                    if (conditionTimeoutSeconds <= 0) {
                        conditionTimeoutSeconds = 60; // Default if config value is invalid
                    }

                    final Timer conditionTimer = new Timer(conditionTimeoutSeconds * 1000, evt -> {
                        // Check if any time conditions have been added
                        if (scheduledPlugin.getStopConditionManager().getConditions().isEmpty()) {
                            log.info("No conditions added within timeout period. Returning to previous state.");                            
                            setCurrentPlugin(null);
                            setState(stateBeforeScheduling);

                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                        schedulerWindow,
                                        "No time conditions were added within the timeout period.\n" +
                                                "Plugin start has been canceled.",
                                        "Configuration Timeout",
                                        JOptionPane.WARNING_MESSAGE);
                            });
                        }else{
                            // Stop the timer if conditions are added
                            
                            log.info("Stop conditions added successfully for plugin: " + scheduledPlugin.getCleanName());
                            setState(SchedulerState.STARTING_PLUGIN);
                            continueStartingPluginScheduleEntry(scheduledPlugin);
                        }
                    });
                    conditionTimer.setRepeats(false);
                    conditionTimer.start();
                }
            } else if (result == JOptionPane.NO_OPTION) {
                setState(SchedulerState.STARTING_PLUGIN);
                // User confirms to run without stop conditions
                continueStartingPluginScheduleEntry(scheduledPlugin);
                scheduledPlugin.setNeedsStopCondition(false);
                log.info("User confirmed to run plugin without stop conditions: {}", scheduledPlugin.getCleanName());
            } else {
                // User canceled or dialog timed out - abort starting
                log.info("Plugin start canceled by user or timed out: {}", scheduledPlugin.getCleanName());
                scheduledPlugin.setNeedsStopCondition(false);
                setCurrentPlugin(null);                
                setState(stateBeforeScheduling);
            }
            return null;
        });
    }

    /**
     * Resets any pending plugin start operation
     */
    public void resetPendingStart() {
        if (currentState == SchedulerState.STARTING_PLUGIN || currentState == SchedulerState.WAITING_FOR_LOGIN || 
                currentState == SchedulerState.WAITING_FOR_STOP_CONDITION) {
            setCurrentPlugin(null);
            
            setState(SchedulerState.SCHEDULING);
        }
    }
    public void continuePendingStart(PluginScheduleEntry scheduledPlugin) {
        if (currentState == SchedulerState.WAITING_FOR_STOP_CONDITION ) {            
            if (currentPlugin != null && !currentPlugin.isRunning()  && currentPlugin.equals(scheduledPlugin)) {
                setState(SchedulerState.STARTING_PLUGIN);   
                log.info("Continuing pending start for plugin: " + scheduledPlugin.getCleanName());
                this.continueStartingPluginScheduleEntry(scheduledPlugin);                
            }
        }
    }
    /**
     * Continues the plugin starting process after stop condition checks
     */
    private void continueStartingPluginScheduleEntry(PluginScheduleEntry scheduledPlugin) {        
        if (scheduledPlugin == null || currentState  != SchedulerState.STARTING_PLUGIN) {            
            setCurrentPlugin(null);
            setState(SchedulerState.SCHEDULING);                
            return;
        }
        Microbot.getClientThread().runOnClientThreadOptional(() -> {
            if (scheduledPlugin.isRunning()) {
                log.info("\n\tPlugin started successfully: " + scheduledPlugin.getCleanName());    
                setState(SchedulerState.RUNNING_PLUGIN);
                return true;
            }
            if (!Microbot.isLoggedIn()) {
                log.info("Login required before running plugin: " + scheduledPlugin.getCleanName()+"current state: " + currentState + "previous state: " + prvState);
                startLoginMonitoringThread();
                return false;
            }
            if (!scheduledPlugin.start(false)) {
                log.error("Failed to start plugin: " + scheduledPlugin.getCleanName());
                setCurrentPlugin(null);                
                setState(SchedulerState.SCHEDULING);
                return false;
            }
          
            Microbot.getClientThread().invokeLater( ()->{
               continueStartingPluginScheduleEntry(scheduledPlugin);
            });
            return false;
            
            
        });
    }

    public void forceStopCurrentPluginScheduleEntry(boolean successful) {
        if (currentPlugin != null && currentPlugin.isRunning()) {
            log.info("Force Stopping current plugin: " + currentPlugin.getCleanName());
            if (currentState == SchedulerState.RUNNING_PLUGIN) {
                setState(SchedulerState.HARD_STOPPING_PLUGIN);
            }
            currentPlugin.stop(successful, StopReason.HARD_STOP, "Plugin was forcibly stopped by user request");
            // Wait a short time to see if the plugin stops immediately
            if (currentPlugin != null) {

                if (!currentPlugin.isRunning()) {
                    log.info("Plugin stopped successfully: " + currentPlugin.getCleanName());

                } else {
                    SwingUtilities.invokeLater(() -> {
                        forceStopCurrentPluginScheduleEntry(successful);
                    });
                    log.info("Failed to hard stop plugin: " + currentPlugin.getCleanName());
                }
            }
        }
        updatePanels();
    }

    /**
     * Update all UI panels with the current state.
     * Throttled to prevent excessive refresh calls.
     */
    void updatePanels() {
        long currentTime = System.currentTimeMillis();
        
        // Throttle panel updates to prevent excessive refreshes
        if (currentTime - lastPanelUpdateTime < PANEL_UPDATE_THROTTLE_MS) {
            return;
        }
        
        lastPanelUpdateTime = currentTime;
        
        if (panel != null) {
            panel.refresh();
        }

        if (schedulerWindow != null && schedulerWindow.isVisible()) {
            schedulerWindow.refresh();
        }
    }
    
    /**
     * Force immediate update of all UI panels, bypassing throttling.
     * Use this for critical state changes that require immediate UI updates.
     */
    void forceUpdatePanels() {
        if (panel != null) {
            panel.refresh();
        }

        if (schedulerWindow != null && schedulerWindow.isVisible()) {
            schedulerWindow.refresh();
        }
    }

    public void addScheduledPlugin(PluginScheduleEntry plugin) {        
        scheduledPlugins.add(plugin);
        // Register the stop completion callback
        registerStopCompletionCallback(plugin);
    }

    public void removeScheduledPlugin(PluginScheduleEntry plugin) {
        plugin.setEnabled(false);
        scheduledPlugins.remove(plugin);
    }

    public void updateScheduledPlugin(PluginScheduleEntry oldPlugin, PluginScheduleEntry newPlugin) {
        int index = scheduledPlugins.indexOf(oldPlugin);
        if (index >= 0) {
            scheduledPlugins.set(index, newPlugin);
            // Register the stop completion callback for the new plugin
            registerStopCompletionCallback(newPlugin);
        }
    }


    /**
     * Adds conditions to a scheduled plugin with support for saving to a specific file
     * 
     * @param plugin The plugin to add conditions to
     * @param userStopConditions List of stop conditions
     * @param userStartConditions List of start conditions
     * @param requireAll Whether all conditions must be met
     * @param stopOnConditionsMet Whether to stop the plugin when conditions are met
     * @param saveFile Optional file to save the conditions to, or null to use default config
     */
    public void saveUserConditionsToScheduledPlugin(PluginScheduleEntry plugin, List<Condition> userStopConditions,
            List<Condition> userStartConditions, boolean requireAll, boolean stopOnConditionsMet, File saveFile) {
        if (plugin == null)
            return;
        List<Condition> stopPluginConditions  = plugin.getStopConditionManager().getPluginCondition().getConditions();
        
        // Remove any existing stop conditions which are not user-defined
        for (Condition condition : userStopConditions) {
            if (stopPluginConditions.contains(condition)) {
                userStopConditions.remove(condition);
            }
        }
        // Clear existing conditions
        plugin.getStopConditionManager().getUserConditions().clear();

        // Add new user conditions
        for (Condition condition : userStopConditions) {
            plugin.addStopCondition(condition);
        }
        
        // Add start conditions if provided
        if (userStartConditions != null && !userStartConditions.isEmpty()) {
            List<Condition> startPluginConditions  = plugin.getStartConditionManager().getPluginCondition().getConditions();        
            // Remove any existing start conditions which are not user-defined -> is a plugin condition, avoid duplication
            for (Condition condition : userStartConditions) {
                if (startPluginConditions.contains(condition)) {
                    userStartConditions.remove(condition);
                }
            }
            plugin.getStartConditionManager().getUserConditions().clear();

            for (Condition condition : userStartConditions) {
                plugin.addStartCondition(condition);
            }
        }

        // Set condition manager properties
        if (requireAll) {
            plugin.getStopConditionManager().setRequireAll();
        } else {
            plugin.getStopConditionManager().setRequireAny();
        }

        // Save to specified file if provided, otherwise to config
        if (saveFile != null) {
            saveScheduledPluginsToFile(saveFile);
        } else {
            // Save to config
            saveScheduledPlugins();
        }
    }
    
    /**
     * Saves scheduled plugins to a specific file
     * 
     * @param file The file to save to
     * @return true if save was successful, false otherwise
     */
    public boolean saveScheduledPluginsToFile(File file) {
        try {
            // Convert to JSON
            String json = PluginScheduleEntry.toJson(scheduledPlugins, SchedulerPlugin.VERSION);
            
            // Write to file
            java.nio.file.Files.writeString(file.toPath(), json);
            log.info("Saved scheduled plugins to file: {}", file.getAbsolutePath());
            return true;
        } catch (Exception e) {
            log.error("Error saving scheduled plugins to file", e);
            return false;
        }
    }
    
    /**
     * Loads scheduled plugins from a specific file
     * 
     * @param file The file to load from
     * @return true if load was successful, false otherwise
     */
    public boolean loadScheduledPluginsFromFile(File file) {
        try {
            stopScheduler();
            if(currentPlugin != null && currentPlugin.isRunning()){
                forceStopCurrentPluginScheduleEntry(false);
                log.info("Stopping current plugin before loading new schedule");                
            }
            sleepUntil(() -> (currentPlugin == null || !currentPlugin.isRunning()), 2000);
            // Read JSON from file
            String json = Files.readString(file.toPath());
            log.info("Loading scheduled plugins from file: {}", file.getAbsolutePath());
            
            // Parse JSON
            List<PluginScheduleEntry> loadedPlugins = PluginScheduleEntry.fromJson(json,  this.VERSION); 
            if (loadedPlugins == null) {
                log.error("Failed to parse JSON from file");
                return false;
            }
            
            // Resolve plugin references
            for (PluginScheduleEntry entry : loadedPlugins) {
                resolvePluginReferences(entry);
                // Register stop completion callback
                registerStopCompletionCallback(entry);
            }
            
            // Replace current plugins
            scheduledPlugins = loadedPlugins;
            
            // Update UI
            SwingUtilities.invokeLater(this::updatePanels);
            return true;
        } catch (Exception e) {
            log.error("Error loading scheduled plugins from file", e);
            return false;
        }
    }
    
    /**
     * Adds stop conditions to a scheduled plugin
     */
    public void saveUserStopConditionsToPlugin(PluginScheduleEntry plugin, List<Condition> conditions,
            boolean requireAll, boolean stopOnConditionsMet) {
        // Call the enhanced version with null file to use default config
        saveUserConditionsToPlugin(plugin, conditions, null, requireAll, stopOnConditionsMet, null);
    }
    
    /**
     * Adds conditions to a scheduled plugin with support for saving to a specific file
     * 
     * @param plugin The plugin to add conditions to
     * @param stopConditions List of stop conditions
     * @param startConditions List of start conditions (optional, can be null)
     * @param requireAll Whether all conditions must be met
     * @param stopOnConditionsMet Whether to stop the plugin when conditions are met
     * @param saveFile Optional file to save the conditions to, or null to use default config
     */
    public void saveUserConditionsToPlugin(PluginScheduleEntry plugin, List<Condition> stopConditions,
            List<Condition> startConditions, boolean requireAll, boolean stopOnConditionsMet, File saveFile) {
        if (plugin == null)
            return;

        // Clear existing stop conditions
        plugin.getStopConditionManager().getUserConditions().clear();

        // Add new stop conditions
        for (Condition condition : stopConditions) {
            plugin.addStopCondition(condition);
        }
        
        // Add start conditions if provided
        if (startConditions != null) {
            plugin.getStartConditionManager().getUserConditions().clear();
            for (Condition condition : startConditions) {
                plugin.addStartCondition(condition);
            }
        }

        // Set condition manager properties
        if (requireAll) {
            plugin.getStopConditionManager().setRequireAll();
        } else {
            plugin.getStopConditionManager().setRequireAny();
        }

        // Save to specified file if provided, otherwise to config
        if (saveFile != null) {
            saveScheduledPluginsToFile(saveFile);
        } else {
            // Save to config
            saveScheduledPlugins();
        }
    }

    /**
     * Gets the list of plugins that have stop conditions set
     */
    public List<PluginScheduleEntry> getScheduledPluginsWithStopConditions() {
        return scheduledPlugins.stream()
                .filter(p -> !p.getStopConditionManager().getConditions().isEmpty())
                .collect(Collectors.toList());
    }

    public List<PluginScheduleEntry> getScheduledPlugins() {
        return new ArrayList<>(scheduledPlugins);
    }

    public void saveScheduledPlugins() {
        // Convert to JSON and save to config
        String json = PluginScheduleEntry.toJson(scheduledPlugins, this.VERSION);

        // log.info("Saving scheduled plugins to config: {}", json);
        // config.setScheduledPlugins(json);
        if (Microbot.getConfigManager() == null) {
            return;
        }
        Microbot.getConfigManager().setConfiguration(SchedulerPlugin.configGroup, "scheduledPlugins", json);

    }

    private void loadScheduledPlugin() {
        try {
            // Load from config and parse JSON
            if (Microbot.getConfigManager() == null) {
                return;
            }
            String json = Microbot.getConfigManager().getConfiguration(SchedulerConfig.CONFIG_GROUP,
                    "scheduledPlugins");
            log.debug("Loading scheduled plugins from config: {}\n\n", json);

            if (json != null && !json.isEmpty()) {
                scheduledPlugins = PluginScheduleEntry.fromJson(json,  this.VERSION);

                // Apply stop settings from config to all loaded plugins
                for (PluginScheduleEntry plugin : scheduledPlugins) {
                    // Set timeout values from config
                    plugin.setSoftStopRetryInterval(Duration.ofSeconds(config.softStopRetrySeconds()));
                    plugin.setHardStopTimeout(Duration.ofSeconds(config.hardStopTimeoutSeconds()));

                    // Resolve plugin references
                    resolvePluginReferences(plugin);
                    
                    // Register stop completion callback
                    registerStopCompletionCallback(plugin);

                    StringBuilder logMessage = new StringBuilder();
                    logMessage.append(String.format("\nLoaded scheduled plugin:\n %s with %d conditions:\n",
                            plugin.getName(),
                            plugin.getStopConditionManager().getConditions().size() + plugin.getStartConditionManager().getConditions().size()));

                    // Start conditions section
                    logMessage.append(String.format("\tStart user condition (%d):\n\t\t%s\n",
                            plugin.getStartConditionManager().getUserLogicalCondition().getTotalConditionCount(),
                            plugin.getStartConditionManager().getUserLogicalCondition().getDescription()));
                    logMessage.append(String.format("\tStart plugin conditions (%d):\n\t\t%s",
                            plugin.getStartConditionManager().getPluginCondition().getTotalConditionCount(),
                            plugin.getStartConditionManager().getPluginCondition().getDescription()));
                    // Stop conditions section
                    logMessage.append(String.format("\tStop user condition (%d):\n\t\t%s\n",
                            plugin.getStopConditionManager().getUserLogicalCondition().getTotalConditionCount(),
                            plugin.getStopConditionManager().getUserLogicalCondition().getDescription()));
                    logMessage.append(String.format("\tStop plugin conditions (%d):\n\t\t%s\n",
                            plugin.getStopConditionManager().getPluginCondition().getTotalConditionCount(),
                            plugin.getStopConditionManager().getPluginCondition().getDescription()));

                   

                    log.info(logMessage.toString());

                    // Log condition details at debug level
                    if (Microbot.isDebug()) {
                        plugin.logConditionInfo(plugin.getStopConditionManager().getConditions(),
                                "LOADING - Stop Conditions", true);
                        plugin.logConditionInfo(plugin.getStartConditionManager().getConditions(),
                                "LOADING - Start Conditions", true);
                    }
                }

                // Force UI update after loading plugins
                SwingUtilities.invokeLater(this::updatePanels);
            }
        } catch (Exception e) {
            log.error("Error loading scheduled plugins", e);
            scheduledPlugins = new ArrayList<>();
        }
    }

    /**
     * Resolves plugin references for a ScheduledPlugin instance.
     * This must be done after deserialization since Plugin objects can't be
     * serialized directly.
     */
    private void resolvePluginReferences(PluginScheduleEntry scheduled) {
        if (scheduled.getName() == null) {
            return;
        }

        // Find the plugin by name
        Plugin plugin = Microbot.getPluginManager().getPlugins().stream()
                .filter(p -> p.getName().equals(scheduled.getName()))
                .findFirst()
                .orElse(null);

        if (plugin != null) {
            scheduled.setPlugin(plugin);

            // If plugin implements StoppingConditionProvider, make sure any plugin-defined
            // conditions are properly registered
            if (plugin instanceof SchedulablePlugin) {
                log.debug("Found StoppingConditionProvider plugin: {}", plugin.getName());
                // This will preserve user-defined conditions while adding plugin-defined ones
                //scheduled.registerPluginStoppingConditions();
            }
        } else {
            log.warn("Could not find plugin with name: {}", scheduled.getName());
        }
    }

    public List<String> getAvailablePlugins() {
        return Microbot.getPluginManager().getPlugins().stream()
                .filter(plugin -> {
                    PluginDescriptor descriptor = plugin.getClass().getAnnotation(PluginDescriptor.class);
                    return descriptor != null && plugin instanceof SchedulablePlugin;
                            
                })
                .map(Plugin::getName)
                .sorted()
                .collect(Collectors.toList());
    }

    public PluginScheduleEntry getNextPluginToBeScheduled() {
        return getNextScheduledPlugin(true, null).orElse(null);
    }

    public PluginScheduleEntry getUpComingPluginWithinTime(Duration timeWindow) {
        return getNextScheduledPlugin(false, timeWindow).orElse(null);
    }
    public PluginScheduleEntry getUpComingPlugin() {
        return getNextScheduledPlugin(false, null).orElse(null);
    }

    /**
     * Core method to find the next plugin based on various criteria.
     * This uses sortPluginScheduleEntries with weighted selection to handle
     * randomizable plugins.
     * 
     * The selection priority depends on the timeWindow parameter:
     * 
     * When timeWindow is NULL (immediate execution context):
     * 1. Plugins that are due to run NOW get priority over priority level
     * 2. Within due/not-due groups: later sort by  scheduler group, earliest timing, over all groups, 
     * ->> find the group with the erlist timing(has a plugin which is upcomming next)
     * 3. Sorted using the enhanced sortPluginScheduleEntries method
     * 
     * When timeWindow is PROVIDED (looking ahead context):
     * 1. Highest priority plugins get priority (regardless of due-to-run status)
     * 2. Within sch groups: earliest timing and due-to-run status via sorting
     * 3. Sorted using the enhanced sortPluginScheduleEntries method
     * 
     * @param isDueToRun If true, only returns plugins that are due to run now
     * @param timeWindow If not null, limits to plugins triggered within this time
     *                   window and changes prioritization to favor priority over due-status
     * @return Optional containing the next plugin to run, or empty if none match
     *         criteria
     */
    public Optional<PluginScheduleEntry> getNextScheduledPlugin(boolean isDueToRun, 
                                                                Duration timeWindow) {

        if (scheduledPlugins.isEmpty()) {
            return Optional.empty();
        }
        // Apply filters based on parameters
        List<PluginScheduleEntry> filteredPlugins = scheduledPlugins.stream()
                .filter(PluginScheduleEntry::isEnabled)
                .filter(plugin -> {
                    // Filter by whether it's due to run now if requested
                    if (isDueToRun && !plugin.isDueToRun()) {
                        log.debug("Plugin '{}' is not due to run", plugin.getCleanName());
                        return false;
                    }
                    if (plugin.isStopInitiated()) {
                        log.debug("Plugin '{}' has stop initiated", plugin.getCleanName());
                        return false;
                    }

                    // Filter by time window if specified
                    if (timeWindow != null) {
                        Optional<ZonedDateTime> nextStartTime = plugin.getCurrentStartTriggerTime();
                        if (!nextStartTime.isPresent()) {
                            log.debug("Plugin '{}' has no trigger time", plugin.getCleanName());
                            return false;
                        }

                        ZonedDateTime cutoffTime = ZonedDateTime.now(ZoneId.systemDefault()).plus(timeWindow);
                        if (nextStartTime.get().isAfter(cutoffTime)) {
                            log.debug("Plugin '{}' trigger time is after cutoff", plugin.getCleanName());
                            return false;
                        }
                    }

                    // Must have a valid next trigger time
                    return plugin.getCurrentStartTriggerTime().isPresent();
                })
                .collect(Collectors.toList());

        if (filteredPlugins.isEmpty()) {            
            return Optional.empty();
        }                
        // Different prioritization logic based on whether we're looking within a time window
        List<PluginScheduleEntry> candidatePlugins;        
        if (timeWindow != null) {

            //TODO when we add scheduler groups, we need to filter by group name here
            // When looking within a time window, we want to prioritize by priority first
            // and then by earliest start time within that priority group
            // This ensures we see the highest priority plugins that are coming up next
            
            // When looking within a time window, prioritize by priority first (user wants to see what's coming up)
            // Find the highest priority plugins within the time window
            int highestPriority = filteredPlugins.stream()
                    .mapToInt(PluginScheduleEntry::getPriority)
                    .max()
                    .orElse(0);

            candidatePlugins = filteredPlugins.stream()
                    .filter(p -> p.getPriority() == highestPriority)
                    .collect(Collectors.toList());

        } else {
            // When no time window, prioritize due-to-run status over priority (for immediate execution)
            List<PluginScheduleEntry> duePlugins = filteredPlugins.stream()
                    .filter(PluginScheduleEntry::isDueToRun)
                    .collect(Collectors.toList());            
            List<PluginScheduleEntry> notDuePlugins = filteredPlugins.stream()
                    .filter(p -> !p.isDueToRun())
                    .collect(Collectors.toList());            
            // Choose the appropriate group - prefer due plugins when available
            List<PluginScheduleEntry> candidateGroup = !duePlugins.isEmpty() ? duePlugins : notDuePlugins;            
            
            candidatePlugins = candidateGroup; 
            // NOTE: not filtering by priority here, later we want to implement, scheuler groups, but need to think about how to handle that
            // what is a scheduler group? -> which attrribute we add to a PluginScheduleEntry? within a group, plugins can have different priorities
            // i think scheduler groups, could be string identifiers, like "combat", "skilling", "questing" etc.
            //candidatePlugins = candidateGroup.stream()
            //        .filter(p -> p.getScheulderGroup().toLowerCast().contains(groupName.toLowerCase()))
            //        .collect(Collectors.toList());
        }        
        // Sort the candidate plugins with weighted selection
        // This handles both randomizable and non-randomizable plugins
        List<PluginScheduleEntry> sortedCandidates = SchedulerPluginUtil.sortPluginScheduleEntries(candidatePlugins, true);
        //log.debug("Sorted candidate plugins: {}", sortedCandidates.stream()
         //       .map((entry) -> {return "name: "+entry.getCleanName() + "next start: " + entry.getNextRunDisplay();})
          //      .collect(Collectors.joining(", ")));
        // The first plugin after sorting is our selected plugin
        if (!sortedCandidates.isEmpty()) {
            PluginScheduleEntry selectedPlugin = sortedCandidates.get(0);
            return Optional.of(selectedPlugin);
        }

        return Optional.empty();
    }

    /**
     * Helper method to check if all plugins in a list have the same start trigger
     * time
     * (truncated to millisecond precision for stable comparisons)
     * 
     * @param plugins List of plugins to check
     * @return true if all plugins have the same trigger time
     */
    private boolean isAllSameTimestamp(List<PluginScheduleEntry> plugins) {
        return SchedulerPluginUtil.isAllSameTimestamp(plugins);
    }

    /**
     * Checks if the current plugin should be stopped based on conditions
     */
    private void checkCurrentPlugin() {
        if (currentPlugin == null || !currentPlugin.isRunning()) {
            // should not happen because only called when is isScheduledPluginRunning() is
            // true
            return;
            //throw new IllegalStateException("No current plugin is running");                        
        }

        // Call the update hook if the plugin is a condition provider
        Plugin runningPlugin = currentPlugin.getPlugin();
        if (runningPlugin instanceof SchedulablePlugin) {
            ((SchedulablePlugin) runningPlugin).onStopConditionCheck();
        }
       
        if(currentPlugin.isPaused() && isOnBreak()){            
            return;            
        }

        // Log condition progress if debug mode is enabled
        if (Microbot.isDebug()) {
            // Log current progress of all conditions
            currentPlugin.logConditionInfo(currentPlugin.getStopConditions(), "DEBUG_CHECK Running Plugin", true);

            // If there are progress-tracking conditions, log their progress percentage
            double overallProgress = currentPlugin.getStopConditionProgress();
            if (overallProgress > 0) {
                log.info("Overall condition progress for '{}': {}%",
                        currentPlugin.getCleanName(),
                        String.format("%.1f", overallProgress));
            }
        }

        // Check if conditions are met
        boolean stopStarted = currentPlugin.checkConditionsAndStop(true);
        if (currentPlugin.isRunning() && !stopStarted && currentState != SchedulerState.SOFT_STOPPING_PLUGIN && currentPlugin.isDefault()){
            boolean prioritizeNonDefaultPlugins = config.prioritizeNonDefaultPlugins();
            // Use the configured look-ahead time window
            int nonDefaultPluginLookAheadMinutes = config.nonDefaultPluginLookAheadMinutes(); 
            PluginScheduleEntry nextPluginWithin = getNextScheduledPlugin(true, 
                                                        Duration.ofMinutes(nonDefaultPluginLookAheadMinutes)).orElse(null);
            
            if (nextPluginWithin != null && !nextPluginWithin.isDefault()) {
                //String builder
                StringBuilder sb = new StringBuilder();
                sb.append("\nPlugin '").append(currentPlugin.getCleanName()).append("' is running and has a next scheduled plugin within ")
                        .append(nonDefaultPluginLookAheadMinutes)
                        .append(" minutes that is not a default plugin: '")
                        .append(nextPluginWithin.getCleanName()).append("'");
                log.info(sb.toString());
                    
            } 
            
            if(prioritizeNonDefaultPlugins && nextPluginWithin != null && !nextPluginWithin.isDefault()){
                log.info("Try to Stop default plugin '{}' because a non-default plugin '{}'' is scheduled to run within {} minutes",
                        currentPlugin.getCleanName(), nextPluginWithin.getCleanName(),nonDefaultPluginLookAheadMinutes);
                currentPlugin.setLastStopReason("Plugin '" + nextPluginWithin.getCleanName() + "' is scheduled to run within " + nonDefaultPluginLookAheadMinutes + " minutes");
                stopStarted = currentPlugin.stop(true, 
                                                    StopReason.INTERRUPTED,
                                                    "Plugin '" + nextPluginWithin.getCleanName() + "' is scheduled to run within " + nonDefaultPluginLookAheadMinutes + " minutes");

            }
        }
        if (stopStarted) {
            if (config.notificationsOn()){
                String notificationMessage = "SoftStop Plugin '" + currentPlugin.getCleanName() + "' stopped because conditions were met or non-default plugin is scheduled to run soon";         
                notifier.notify(Notification.ON, notificationMessage);
            }
            if (hasDisabledQoLPlugin){
                SchedulerPluginUtil.enablePlugin(QoLPlugin.class);
            }
            log.info("Plugin '{}' stopped because conditions were met",
                    currentPlugin.getCleanName());
            // Set state to indicate we're stopping the plugin
            setState(SchedulerState.SOFT_STOPPING_PLUGIN);
        }
        if (!currentPlugin.isRunning()) {
            log.info("Plugin '{}' stopped because conditions were met",
                    currentPlugin.getCleanName());
            setCurrentPlugin(null);            
            setState(SchedulerState.SCHEDULING);
        }
    }

    /**
     * Gets condition progress for a scheduled plugin.
     * 
     * @param scheduled The scheduled plugin
     * @return Progress percentage (0-100)
     */
    public double getStopConditionProgress(PluginScheduleEntry scheduled) {
        if (scheduled == null || scheduled.getStopConditionManager().getConditions().isEmpty()) {
            return 0;
        }

        return scheduled.getStopConditionProgress();
    }

    /**
     * Gets the list of plugins that have conditions set
     */
    public List<PluginScheduleEntry> getPluginsWithConditions() {
        return scheduledPlugins.stream()
                .filter(p -> !p.getStopConditionManager().getConditions().isEmpty())
                .collect(Collectors.toList());
    }

   

    /**
     * Adds conditions to a scheduled plugin with support for saving to a specific file
     * 
     * @param plugin The plugin to add conditions to
     * @param userStopConditions List of stop conditions
     * @param userStartConditions List of start conditions
     * @param requireAll Whether all conditions must be met
     * @param stopOnConditionsMet Whether to stop the plugin when conditions are met
     * @param saveFile Optional file to save the conditions to, or null to use default config
     */
    public void saveConditionsToPlugin(PluginScheduleEntry plugin, List<Condition> stopConditions,
            List<Condition> startConditions, boolean requireAll, boolean stopOnConditionsMet, File saveFile) {
        if (plugin == null)
            return;

        // Clear existing stop conditions
        plugin.getStopConditionManager().getConditions().clear();

        // Add new stop conditions
        for (Condition condition : stopConditions) {
            plugin.addStopCondition(condition);
        }
        
        // Add start conditions if provided
        if (startConditions != null) {
            plugin.getStartConditionManager().getConditions().clear();
            for (Condition condition : startConditions) {
                plugin.addStartCondition(condition);
            }
        }

        // Set condition manager properties
        if (requireAll) {
            plugin.getStopConditionManager().setRequireAll();
        } else {
            plugin.getStopConditionManager().setRequireAny();
        }

        // Save to specified file if provided, otherwise to config
        if (saveFile != null) {
            saveScheduledPluginsToFile(saveFile);
        } else {
            // Save to config
            saveScheduledPlugins();
        }
    }

  

    /**
     * Checks if a specific plugin schedule entry is currently running
     * This explicitly compares by reference, not just by name
     */
    public boolean isRunningEntry(PluginScheduleEntry entry) {
        return entry.isRunning();
    }

    /**
     * Checks if a completed one-time plugin should be removed
     * 
     * @param scheduled The scheduled plugin to check
     * @return True if the plugin should be removed
     */
    private boolean shouldRemoveCompletedOneTimePlugin(PluginScheduleEntry scheduled) {
        // Check if it's been run at least once and is not currently running
        boolean hasRun = scheduled.getRunCount() > 0 && !scheduled.isRunning();

        // Check if it can't be triggered again (based on start conditions)
        boolean cantTriggerAgain = !scheduled.canStartTriggerAgain();

        return hasRun && cantTriggerAgain;
    }

    /**
     * Cleans up the scheduled plugins list by removing completed one-time plugins
     */
    private void cleanupCompletedOneTimePlugins() {
        List<PluginScheduleEntry> toRemove = scheduledPlugins.stream()
                .filter(this::shouldRemoveCompletedOneTimePlugin)
                .collect(Collectors.toList());

        if (!toRemove.isEmpty()) {
            scheduledPlugins.removeAll(toRemove);
            saveScheduledPlugins();
            log.info("Removed {} completed one-time plugins", toRemove.size());
        }
    }

    public boolean isScheduledPluginRunning() {
        return currentPlugin != null && currentPlugin.isRunning();
    }

    private boolean isBreakHandlerEnabled() {
        return SchedulerPluginUtil.isBreakHandlerEnabled();
    }

    private boolean isAntibanEnabled() {
        return SchedulerPluginUtil.isAntibanEnabled();
    }

    private boolean isAutoLoginEnabled() {
        return SchedulerPluginUtil.isAutoLoginEnabled();
    }

    /**
     * Forces the bot to take a break immediately if BreakHandler is enabled
     * 
     * @return true if break was initiated, false otherwise
     */
    private boolean forceBreak() {
        return SchedulerPluginUtil.forceBreak();
    }

    private boolean takeMicroBreak() {
        return SchedulerPluginUtil.takeMicroBreak(() -> setState(SchedulerState.BREAK));
    }

    private boolean lockBreakHandler() {
        return SchedulerPluginUtil.lockBreakHandler();
    }

    private void unlockBreakHandler() {
        SchedulerPluginUtil.unlockBreakHandler();
    }

    /**
     * Checks if the bot is currently on a break
     * 
     * @return true if on break, false otherwise
     */
    public boolean isOnBreak() {
        return SchedulerPluginUtil.isOnBreak();
    }

    /**
     * Gets the current activity being performed
     * 
     * @return The current activity, or null if not tracking
     */
    public Activity getCurrentActivity() {
        return currentActivity;
    }

    /**
     * Gets the current activity intensity
     * 
     * @return The current activity intensity, or null if not tracking
     */
    public ActivityIntensity getCurrentIntensity() {
        return currentIntensity;
    }

    /**
     * Gets the current idle time in game ticks
     * 
     * @return Idle time (ticks)
     */
    public int getIdleTime() {
        return idleTime;
    }

    /**
     * Gets the time elapsed since login
     * 
     * @return Duration since login, or Duration.ZERO if not logged in
     */
    public Duration getLoginDuration() {
        if (loginTime == null) {
            return Duration.ZERO;
        }
        return Duration.between(loginTime, Instant.now());
    }

    /**
     * Gets the time since last detected activity
     * 
     * @return Duration since last activity
     */
    public Duration getTimeSinceLastActivity() {
        return Duration.between(lastActivityTime, Instant.now());
    }

   /**
     * Gets the time until the next scheduled break
     * 
     * @return Duration until next break
     */
    public Duration getTimeUntilNextBreak() {
        if (SchedulerPluginUtil.isBreakHandlerEnabled() && BreakHandlerScript.breakIn >= 0) {
            return Duration.ofSeconds(BreakHandlerScript.breakIn);
        }
        return timeUntilNextBreak;
    }

    /**
     * Gets the duration of the current break
     * 
     * @return Current break duration
     */
    public Duration getCurrentBreakDuration() {
        if (SchedulerPluginUtil.isBreakHandlerEnabled() && BreakHandlerScript.breakDuration > 0) {
            return Duration.ofSeconds(BreakHandlerScript.breakDuration);
        }
        return currentBreakDuration;
    }

    public void startLoginMonitoringThread() {
        String  pluginName = "";
        if (!currentState.isSchedulerActive() || (currentState.isBreaking()|| currentState == SchedulerState.RUNNING_PLUGIN || currentState == SchedulerState.LOGIN ) ||(Microbot.isLoggedIn())) {
            log.info("Login monitoring thread not started, current state: {} - {}", currentState,currentState.isWaiting() );
            return;
        }        
        if (currentPlugin != null) {
            pluginName = currentPlugin.getName();                
        }
        
        if (loginMonitor != null && loginMonitor.isAlive()) {
            log.info("Login monitoring thread already running for plugin '{}'", pluginName);
            return;
        }
        setState(SchedulerState.LOGIN);
        this.loginMonitor = new Thread(() -> {
            try {
                
                log.debug("Login monitoring thread started for plugin");
                int loginAttempts = 0;
                final int MAX_LOGIN_ATTEMPTS = 6;

                // Keep checking until login completes or max attempts reached
                while (loginAttempts < MAX_LOGIN_ATTEMPTS) {
                    // Wait for login attempt to complete
                    
                    log.info("Login attempt {} of {}",
                            loginAttempts, MAX_LOGIN_ATTEMPTS);
                    // Try login again if needed
                    
                    if (loginAttempts < MAX_LOGIN_ATTEMPTS) {                    
                        login();
                    }
                    if (Microbot.isLoggedIn()) {
                        // Successfully logged in, now increment the run count                        
                        if (currentPlugin != null) {
                            log.info("Login successful, finalizing plugin start: {}", currentPlugin.getName());
                            if (currentPlugin.isRunning()) {
                                // If we were running the plugin, continue with that
                                log.info("Continuing to run plugin after login: {}", currentPlugin.getName());
                                setState(SchedulerState.RUNNING_PLUGIN);
                               
                               

                            }else if(!currentPlugin.isRunning()){
                                // If we were starting the plugin, continue with that
                                setState(SchedulerState.STARTING_PLUGIN);
                                log.info("Continuing to start plugin after login: {}", currentPlugin.getName());
                                Microbot.getClientThread().invokeLater(() -> {
                                    continueStartingPluginScheduleEntry(currentPlugin);
                                // setState(SchedulerState.RUNNING_PLUGIN);
                            });    
                            
                            }
                           
                            return;
                        }else{
                            log.info("Login successful, but no plugin to start back to scheduling");
                            setState(SchedulerState.SCHEDULING);
                        }                       
                        return;
                    }
                    if (Microbot.getClient().getGameState() != GameState.LOGGED_IN &&
                            Microbot.getClient().getGameState() != GameState.LOGGING_IN) {
                        loginAttempts++;
                    }
                    Thread.sleep(2000);
                }

                // If we get here, login failed too many times
                log.error("Failed to login after {} attempts",
                        MAX_LOGIN_ATTEMPTS);
                SwingUtilities.invokeLater(() -> {
                    // Clean up and set proper state
                    if (currentPlugin != null && currentPlugin.isRunning()) {
                        currentPlugin.stop(false, StopReason.SCHEDULED_STOP, "Plugin stopped due to scheduled time conditions");
                        setState(SchedulerState.SOFT_STOPPING_PLUGIN);
                    } else {
                        if (currentPlugin != null) {
                            currentPlugin.setEnabled(false);
                        }
                        currentPlugin = null;
                        
                        setState(SchedulerState.SCHEDULING);
                    }

                });
            } catch (InterruptedException e) {
                if (currentPlugin != null) log.debug("Login monitoring thread for '{}' was interrupted", currentPlugin.getName());
            }
        });

        loginMonitor.setName("LoginMonitor - " + pluginName);
        loginMonitor.setDaemon(true);
        loginMonitor.start();
    }

    private void logout() {
        SchedulerPluginUtil.logout();
    }

    private void login() {      
        // First check if AutoLogin plugin is available and enabled
        if (!isAutoLoginEnabled() && config.autoLogIn()) {
            // Try to enable AutoLogin plugin
            if (SchedulerPluginUtil.enableAutoLogin()) {                
                // Give it a moment to initialize
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // Fallback to manual login if AutoLogin is not available
        // log.info("Using manual login (AutoLogin plugin not available)");
        boolean successfulLogin = Microbot.getClientThread().runOnClientThreadOptional(() -> {
            // check which login index means we are in authifcation or not a member
            // TODO add these to "LOGIN" class ->
            // net.runelite.client.plugins.microbot.util.security
            int currentLoginIndex = Microbot.getClient().getLoginIndex();
            boolean tryMemberWorld =  config.worldType() == 2 || config.worldType() ==1 ; // TODO get correct one
            tryMemberWorld = Login.activeProfile.isMember();
            if (currentLoginIndex == 4 || currentLoginIndex == 3) { // we are in the auth screen and cannot login
                // 3 mean wrong authtifaction
                return false; // we are in auth
            }
            if (currentLoginIndex == 34) { // we are not a member and cannot login
                if (isAutoLoginEnabled() || config.autoLogInWorld() == 1) {                    
                    Microbot.getConfigManager().setConfiguration("AutoLoginConfig", "World",
                            Login.getRandomWorld(false));
                }
                int loginScreenWidth = 804;
                int startingWidth = (Microbot.getClient().getCanvasWidth() / 2) - (loginScreenWidth / 2);
                Microbot.getMouse().click(365 + startingWidth, 308); // clicks a button "OK" when you've been
                                                                     // disconnected
                sleep(600);
                if (config.worldType() != 2){
                    // Show dialog for free world selection using the SchedulerUIUtils class
                    SchedulerUIUtils.showNonMemberWorldDialog(currentPlugin, config, (switchToFreeWorlds) -> {
                        if (!switchToFreeWorlds) {
                            // User chose not to switch to free worlds or dialog timed out
                            if (currentPlugin != null) {
                                currentPlugin.setEnabled(false);
                                currentPlugin = null;
                                setState(SchedulerState.SCHEDULING);
                                log.info("Login to member world canceled, stopping current plugin");
                            }
                        }
                    });
                }
                tryMemberWorld = false; // we are not a member

                
            }
            if (currentLoginIndex == 2) {
                // connected to the server
            }
            
            if (isAutoLoginEnabled() ) {                
                if (Microbot.pauseAllScripts.get()) {
                    log.info("AutoLogin is enabled but paused, stopping AutoLogin");
                                        
                }
                ConfigManager configManager = Microbot.getConfigManager();
                if (configManager != null) {
                    configManager.setConfiguration("AutoLoginConfig", "World",
                            Login.getRandomWorld(tryMemberWorld));                    
                }
                // Give it a moment to initialize
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                log.info("forced login by scheduler plugin -> currentLoginIndex: {} - member {}", currentLoginIndex,
                        tryMemberWorld);
                new Login(Login.getRandomWorld(tryMemberWorld));
            }
            return true;
        }).orElse(false);        
        if (!successfulLogin) {
            currentPlugin.setEnabled(false);
            currentPlugin = null;
            setState(SchedulerState.SCHEDULING);
            log.error("Failed to login, stopping plugin: {}", currentPlugin.getName());
            //stopScheduler();
        }
    }

    /**
     * Prints detailed diagnostic information about all scheduled plugins
     */
    public void debugAllScheduledPlugins() {
        log.info("==== PLUGIN SCHEDULER DIAGNOSTICS ====");
        log.info("Current state: {}", currentState);
        log.info("Number of scheduled plugins: {}", scheduledPlugins.size());

        for (PluginScheduleEntry plugin : scheduledPlugins) {
            log.info("\n----- Plugin: {} -----", plugin.getCleanName());
            log.info("Enabled: {}", plugin.isEnabled());
            log.info("Running: {}", plugin.isRunning());
            log.info("Is default: {}", plugin.isDefault());
            log.info("Due to run: {}", plugin.isDueToRun());
            log.info("Has start conditions: {}", plugin.hasAnyStartConditions());

            if (plugin.hasAnyStartConditions()) {
                log.info("Start conditions met: {}", plugin.getStartConditionManager().areAllConditionsMet());

                // Get next trigger time if any
                Optional<ZonedDateTime> nextTrigger = plugin.getCurrentStartTriggerTime();
                log.info("Next trigger time: {}",
                        nextTrigger.isPresent() ? nextTrigger.get() : "None found");

                // Print detailed diagnostics
                log.info("\nDetailed start condition diagnosis:");
                log.info(plugin.diagnoseStartConditions());
            }
        }
        log.info("==== END DIAGNOSTICS ====");
    }

 
    /**
     * Enables the BreakHandler plugin
     * 
     * @return true if plugin was enabled successfully, false otherwise
     */
    private boolean enableBreakHandler() {
        return SchedulerPluginUtil.enableBreakHandler();
    }

    /**
     * Disables the BreakHandler plugin
     * 
     * @return true if plugin was disabled successfully, false otherwise
     */
    private boolean disableBreakHandler() {
        return SchedulerPluginUtil.disableBreakHandler();
    }

    /**
     * Enables the Antiban plugin
     * 
     * @return true if plugin was enabled successfully, false otherwise
     */
    private boolean enableAntiban() {
        return SchedulerPluginUtil.enableAntiban();
    }

    /**
     * Disables the Antiban plugin
     * 
     * @return true if plugin was disabled successfully, false otherwise
     */
    private boolean disableAntiban() {
        return SchedulerPluginUtil.disableAntiban();
    }

    public void openAntibanSettings() {
        // Get the parent frame
        SwingUtilities.invokeLater(() -> {
            try {
                // Use the utility class to open the Antiban settings in a new window
                AntibanDialogWindow.showAntibanSettings(panel);
            } catch (Exception ex) {
                log.error("Error opening Antiban settings: {}", ex.getMessage());
            }
        });
    }

    /**
     * Sets the current scheduler state and updates UI
     */
    private void setState(SchedulerState newState) {
        if (currentState != newState) {
            prvState = currentState;
            log.debug("Scheduler state changed: {} -> {}", currentState, newState);
            breakStartTime = Optional.empty();
            // Set additional state information based on context
            switch (newState) {
                case INITIALIZING:
                    newState.setStateInformation(String.format("Checking for required plugins (%d/%d)",
                            initCheckCount, MAX_INIT_CHECKS));
                    break;

                case ERROR:
                    newState.setStateInformation(String.format(
                            "Initialization failed after %d/%d attempts. Client may not be at login screen.",
                            initCheckCount, MAX_INIT_CHECKS));
                    break;

                case WAITING_FOR_LOGIN:
                    newState.setStateInformation("Waiting for player to login to start plugin");
                    break;

                case SOFT_STOPPING_PLUGIN:
                    newState.setStateInformation(
                            currentPlugin != null ? "Attempting to gracefully stop " + currentPlugin.getCleanName()
                                    : "Attempting to gracefully stop plugin");
                    break;

                case HARD_STOPPING_PLUGIN:
                    newState.setStateInformation(
                            currentPlugin != null ? "Forcing " + currentPlugin.getCleanName() + " to stop"
                                    : "Forcing plugin to stop");
                    break;

                case RUNNING_PLUGIN:
                    newState.setStateInformation(
                            currentPlugin != null ? "Running " + currentPlugin.getCleanName() : "Running plugin");
                    break;

                case STARTING_PLUGIN:
                    newState.setStateInformation(
                            currentPlugin != null ? "Starting " + currentPlugin.getCleanName() : "Starting plugin");
                    break;

                case BREAK:
                    PluginScheduleEntry nextPlugin = getUpComingPlugin();                    
                    if (nextPlugin != null) {
                        Optional<ZonedDateTime> nextStartTime = nextPlugin.getCurrentStartTriggerTime();
                        String displayTime = nextStartTime.isPresent() ? 
                                nextStartTime.get().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")) :
                                "unknown time";                        
                        newState.setStateInformation("Taking break until " +
                                nextPlugin.getCleanName() + " is scheduled to run at " + displayTime
                                );
                    } else {
                        newState.setStateInformation("Taking a break between schedules");
                    }
                    breakStartTime = Optional.of(ZonedDateTime.now(ZoneId.systemDefault()));
                    break;
                case PLAYSCHEDULE_BREAK:
                    
                    Duration timeUntilNext = config.playSchedule().timeUntilNextSchedule();
                    //LocalTime startTime = config.playSchedule().getStartTime();
                    //LocalTime endTime = config.playSchedule().getEndTime();
                    if (timeUntilNext != null) {
                        newState.setStateInformation("Taking a break Play Schedule: \n\t" + config.playSchedule().displayString());
                    } else {
                        newState.setStateInformation("Taking a break between schedules");    
                    }     
                    breakStartTime = Optional.of(ZonedDateTime.now(ZoneId.systemDefault()));               
                    //breakEndTime = Optional.of(ZonedDateTime.now(ZoneId.systemDefault()).plus(timeUntilNext));
                    break;
                case WAITING_FOR_SCHEDULE:
                    newState.setStateInformation("Waiting for the next scheduled plugin to become due");
                    break;

                case SCHEDULING:
                    newState.setStateInformation("Actively checking plugin schedules");
                    break;

                case READY:
                    newState.setStateInformation("Ready to run - click Start to begin scheduling");
                    break;

                case HOLD:
                    newState.setStateInformation("Scheduler was manually stopped");
                    break;

                case SCHEDULER_PAUSED:
                    newState.setStateInformation("Scheduler is paused. Resume to continue.");
                    break;
                case RUNNING_PLUGIN_PAUSED:
                    newState.setStateInformation("Running plugin is paused. Resume to continue.");
                    break;
                default:
                    newState.setStateInformation(""); // Clear any previous information
                    break;
            }

            currentState = newState;
            SwingUtilities.invokeLater(this::forceUpdatePanels);
        }
    }

    /**
     * Checks if the player has been idle for longer than the specified timeout
     * 
     * @param timeout The timeout in game ticks
     * @return True if idle for longer than timeout
     */
    public boolean isIdleTooLong(int timeout) {
        return idleTime > timeout && !isOnBreak();
    }


    @Subscribe(priority = 100)
    private void onClientShutdown(ClientShutdown e) {
        if (currentPlugin != null && currentPlugin.isRunning()) {
            log.info("Client shutdown detected, stopping current plugin: {}", currentPlugin.getCleanName());
            // Stop the current plugin gracefully
            currentPlugin.stop(false, StopReason.CLIENT_SHUTDOWN, "Client is shutting down");
            setState(SchedulerState.SCHEDULING);
        }
    }
    @Subscribe
    public void onGameTick(GameTick event) {
        // Update idle time tracking
        if (!Rs2Player.isAnimating() && !Rs2Player.isMoving() && !isOnBreak()
                && this.currentState == SchedulerState.RUNNING_PLUGIN) {
            idleTime++;
        } else {
            idleTime = 0;
        }
    }

    @Subscribe
    public void onPluginScheduleEntryFinishedEvent(PluginScheduleEntryFinishedEvent event) {
        if (currentPlugin != null && event.getPlugin() == currentPlugin.getPlugin()) {
            log.info("Plugin '{}' self-reported as finished: {} (Success: {})",
                    currentPlugin.getCleanName(),
                    event.getReason(),
                    event.isSuccess());
            if (config.notificationsOn()){                
                String notificationMessage = "Plugin '" + currentPlugin.getCleanName() + "' finished: " + event.getReason();
                if (event.isSuccess()) {
                    notificationMessage += " (Success)";
                } else {
                    notificationMessage += " (Failed)";
                }
                notifier.notify(Notification.ON, notificationMessage);
                
            }
            
            // Stop the plugin with the success state from the event
            if (currentState == SchedulerState.RUNNING_PLUGIN) {
                setState(SchedulerState.SOFT_STOPPING_PLUGIN);
            }
            
            // Format the reason message for better readability
            String eventReason = event.getReason();
            String formattedReason = SchedulerPluginUtil.formatReasonMessage(eventReason);
            
            String reasonMessage = event.isSuccess() ? 
                "Plugin completed its task successfully:\n\t\t\"" + formattedReason+"\"":
                "Plugin reported completion but indicated an unsuccessful run:\n" + formattedReason;
                
            currentPlugin.stop(event.isSuccess(), StopReason.PLUGIN_FINISHED, reasonMessage);
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {        

        // Track login time
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN
                && (lastGameState == GameState.LOGIN_SCREEN || lastGameState == GameState.HOPPING)) {
            loginTime = Instant.now();            
            // Reset idle counter on login
            idleTime = 0;
        }

        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            // If the game state is LOGGED_IN, start the scheduler

        } else if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN) {
            // If the game state is LOGIN_SCREEN, stop the current plugin

            // Clear login time when logging out
            loginTime = null;
        } else if (gameStateChanged.getGameState() == GameState.HOPPING) {
            // If the game state is HOPPING, stop the current plugin

        } else if (gameStateChanged.getGameState() == GameState.CONNECTION_LOST) {
            // If the game state is CONNECTION_LOST, stop the current plugin
            // Clear login time when connection is lost
            loginTime = null;

        } else if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN_AUTHENTICATOR) {
            // If the game state is LOGGING_IN, stop the current plugin

            // Clear login time when logging out
            loginTime = null;
            stopScheduler();

        }

        this.lastGameState = gameStateChanged.getGameState();
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("PluginScheduler")) {
            // Handle overlay toggle
            if (event.getKey().equals("showOverlay")) {
                if (config.showOverlay()) {
                    overlayManager.add(overlay);
                } else {
                    overlayManager.remove(overlay);
                }
            }
            
            // Update plugin configurations
            for (PluginScheduleEntry plugin : scheduledPlugins) {
                plugin.setSoftStopRetryInterval(Duration.ofSeconds(config.softStopRetrySeconds()));
                plugin.setHardStopTimeout(Duration.ofSeconds(config.hardStopTimeoutSeconds()));
            }
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged statChanged) {
        // Reset idle time when gaining experience
        idleTime = 0;
        lastActivityTime = Instant.now();

        final Skill skill = statChanged.getSkill();
        final int exp = statChanged.getXp();
        final Integer previous = skillExp.put(skill, exp);

        if (lastSkillChanged != null && (lastSkillChanged.equals(skill) ||
                (CombatSkills.isCombatSkill(lastSkillChanged) && CombatSkills.isCombatSkill(skill)))) {

            return;
        }

        lastSkillChanged = skill;

        if (previous == null || previous >= exp) {
            return;
        }

        // Update our local tracking of activity
        Activity activity = Activity.fromSkill(skill);
        if (activity != null) {
            currentActivity = activity;
            if (Microbot.isDebug()) {
                log.debug("Activity updated from skill: {} -> {}", skill.getName(), activity);
            }
        }

        ActivityIntensity intensity = ActivityIntensity.fromSkill(skill);
        if (intensity != null) {
            currentIntensity = intensity;
        }
    }

    @Subscribe
    public void onPluginChanged(PluginChanged event) {
        if (currentPlugin != null && event.getPlugin() == currentPlugin.getPlugin()) {
            // The plugin changed state - check if it's no longer running
            boolean isRunningNow = currentPlugin.isRunning();
            boolean wasStartedByScheduler = currentPlugin.isHasStarted();

            // If plugin was running but is now stopped
            if (!isRunningNow) {
                log.info("\n\tPlugin '{}' state change detected: \n\t -from running to stopped", currentPlugin.getCleanName());

                // Check if this was an expected stop based on our current state
                boolean wasExpectedStop = (currentState == SchedulerState.SOFT_STOPPING_PLUGIN ||
                        currentState == SchedulerState.HARD_STOPPING_PLUGIN);

                // If the stop wasn't initiated by us, it was unexpected (error or manual stop)
                if (!wasExpectedStop && currentState == SchedulerState.RUNNING_PLUGIN) {
                    log.warn("Plugin '{}' stopped unexpectedly while in {} state",
                            currentPlugin.getCleanName(), currentState.name());

                    // Set error information
                    currentPlugin.setLastStopReason("Plugin stopped unexpectedly");
                    currentPlugin.setLastRunSuccessful(false);
                    currentPlugin.setLastStopReasonType(PluginScheduleEntry.StopReason.ERROR);
                    // Disable the plugin to prevent it from running again until issue is fixed
                    currentPlugin.setEnabled(false);

                    // Set state to error
                    
                } else if (currentState == SchedulerState.SOFT_STOPPING_PLUGIN) {                    
                    // If we were soft stopping and it completed, make sure stop reason is set
                    if (currentPlugin.getLastStopReasonType() == PluginScheduleEntry.StopReason.NONE) {
                        // Set stop reason if it wasn't already set
                        if (currentPlugin.getLastStopReasonType() == PluginScheduleEntry.StopReason.NONE) {                        
                            currentPlugin.setLastStopReasonType(PluginScheduleEntry.StopReason.SCHEDULED_STOP);
                            currentPlugin.setLastStopReason("Scheduled stop completed successfully");
                            currentPlugin.setLastRunSuccessful(true);
                        }
                    }
                } else if (currentState == SchedulerState.HARD_STOPPING_PLUGIN) {                    
                    // Hard stop completed
                    if (currentPlugin.getLastStopReasonType() == PluginScheduleEntry.StopReason.NONE) {
                        currentPlugin.setLastStopReasonType(PluginScheduleEntry.StopReason.HARD_STOP);
                        currentPlugin.setLastStopReason("Plugin was forcibly stopped after timeout");
                        currentPlugin.setLastRunSuccessful(false);
                    }
                }
                  
                // Return to scheduling state regardless of stop reason
                if (currentState != SchedulerState.HOLD) {
                    log.info("Plugin '{}' stopped \n\t- returning to scheduling state with reason: \n\t\t\"{}\"",
                            currentPlugin.getCleanName(),
                            currentPlugin.getLastStopReason());
                   
                    setState(SchedulerState.SCHEDULING);
                }
                currentPlugin.cancelStop();
                setCurrentPlugin(null);
               // Microbot.getClientThread().invokeLater(() -> {
                    // Check if the plugin is still stopping
                 //   checkIfStopFinished();
                //});
               

            } else if (isRunningNow && wasStartedByScheduler && currentState == SchedulerState.SCHEDULING) {
                // Plugin was started by scheduler and is now running - this is expected
                log.info("Plugin '{}' started by scheduler and is now running", event.getPlugin().getName());
               
            } else if (isRunningNow && wasStartedByScheduler && currentState != SchedulerState.STARTING_PLUGIN) {
                // Plugin was started outside our control or restarted - this is unexpected but
                // we'll monitor it
                log.info("Plugin '{}' started or restarted outside scheduler control", event.getPlugin().getName());
            }

            SwingUtilities.invokeLater(this::updatePanels);
        }
    }
    void checkIfStopFinished(){
        
        log.info("current state after plugin stop: {}", currentState);
        sleepUntilOnClientThread(() -> { return !currentPlugin.isStopping();}, 1000); 
        // Check if the plugin is still stopping
        if (currentPlugin.isStopping()) {
            log.info("Plugin '{}' is still stopping, waiting for it to finish", currentPlugin.getCleanName());
            SwingUtilities.invokeLater(() -> {
                // Check if the plugin is still stopping
                checkIfStopFinished();
            });
        } else {
            log.info("Plugin '{}' is not stopping, continuing", currentPlugin.getCleanName());
        }
        saveScheduledPlugins();//start conditions could have changed ->  next trigger,..., save transient data
    
    
        // Clear current plugin reference
        setCurrentPlugin(null);
    }

      /**
     * Sorts all scheduled plugins according to a consistent order.
     * See {@link #sortPluginScheduleEntries(List, boolean)} for the sorting
     * criteria.
     * 
     * @param applyWeightedSelection Whether to apply weighted selection for
     *                               randomizable plugins
     * @return A sorted list of all scheduled plugins
     */
    public List<PluginScheduleEntry> sortPluginScheduleEntries(boolean applyWeightedSelection) {
        return SchedulerPluginUtil.sortPluginScheduleEntries(scheduledPlugins, applyWeightedSelection);
    }
   /**
     * Overloaded method that calls sortPluginScheduleEntries without weighted
     * selection by default
     */
    public List<PluginScheduleEntry> sortPluginScheduleEntries() {
        return SchedulerPluginUtil.sortPluginScheduleEntries(scheduledPlugins, false);
    }
    /**
     * Extends an active break when it's about to end and there are no upcoming plugins
     * @param thresholdSeconds Time in seconds before break end when we consider extending
     * @return true if break was extended, false otherwise
     */
    private boolean extendBreakIfNeeded(PluginScheduleEntry nextPlugin, int thresholdSeconds) {
        // Check if we're on a break and have break information
        if (!isOnBreak() || !breakStartTime.isPresent() || currentBreakDuration.equals(Duration.ZERO) 
        || !currentState.isBreaking()) {
            if (!isOnBreak() &&currentState.isBreaking()){
                interruptBreak();
            }

            return false;
        }
        if (isOnBreak()){
            this.currentBreakDuration =  Duration.ofSeconds(BreakHandlerScript.breakDuration);
        }
        // Calculate when the current break will end
        ZonedDateTime breakEndTime = breakStartTime.get().plus(this.currentBreakDuration);
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        
        // Calculate how much time is left in the current break
        Duration timeRemaining = Duration.between(now, breakEndTime);        
        // If the break is about to end within the threshold seconds
        if (timeRemaining.getSeconds() <= thresholdSeconds) {                                    
            if (nextPlugin == null) {
                // No upcoming plugin, extend the break
                log.info("Break is about to end in {} seconds with no upcoming plugins. Extending break.", 
                    timeRemaining.getSeconds());
                    int minDuration = config.minBreakDuration();
                    int maxDuration = config.maxBreakDuration();
                // Ensure min and max durations are valid
                              
                
                startBreakBetweenSchedules(config.autoLogOutOnBreak(), minDuration, maxDuration);
                this.breakStartTime = Optional.of(now);                
                log.info("Break extended, no upcomming plugin detected New end time: {}", now.plus(this.currentBreakDuration));                
                return true;
            }
        }
        
        return false;
    }

    /**
     * Manually start a plugin from the UI. This method ensures that:
     * 1. The scheduler is in a safe state (SCHEDULING or SHORT_BREAK)
     * 2. The requested plugin is in the scheduledPlugins list
     * 3. There's enough time until the next scheduled plugin
     *
     * @param pluginEntry The plugin to start
     * @return true if the plugin was started successfully, false otherwise with a reason message
     */
    public String manualStartPlugin(PluginScheduleEntry pluginEntry) {
        // Check if plugin is null
        if (pluginEntry == null) {
            return "Invalid plugin selected";
        }        
        if (pluginEntry.getMainTimeStartCondition()!=null && pluginEntry.getMainTimeStartCondition().canTriggerAgain()){
            TimeCondition mainTimeStartCondition = pluginEntry.getMainTimeStartCondition();
            ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
            boolean isSatisfied  = mainTimeStartCondition.isSatisfiedAt(now);
            if (!isSatisfied) {
                //return "Cannot start plugin: Main time condition is not satisfied";
                log.warn("\n\tMain time condition is not satisfied, setting next trigger time to now \n\t{}",mainTimeStartCondition.toString()
                    );
            }
            mainTimeStartCondition.setNextTriggerTime(now);
        }
        // Check if scheduler is in a safe state to start a plugin
        if (currentState != SchedulerState.SCHEDULING && !currentState.isBreaking()
                && currentState != SchedulerState.WAITING_FOR_SCHEDULE) {
            return "Cannot start plugin in current state: \n\t" + currentState.getDisplayName();
        }
        if(currentState == SchedulerState.SCHEDULER_PAUSED || currentState == SchedulerState.RUNNING_PLUGIN_PAUSED){
            return "Cannot start plugin: \n\tScheduler is paused";
        }
        
        // Check if a plugin is already running
        if (isScheduledPluginRunning()) {
            return "Cannot start plugin: \n\tAnother plugin is already running";
        }
        
        // Check if the plugin is in the scheduled plugins list
        if (!scheduledPlugins.contains(pluginEntry)) {
            return "Cannot start plugin: \n\tPlugin is not in the scheduled plugins list";
        }
        
        // Check if the plugin is enabled
        if (!pluginEntry.isEnabled()) {
            return "Cannot start plugin: \n\tPlugin is disabled";
        }

        // Check time until next scheduled plugin
        PluginScheduleEntry nextUpComingPlugin = getUpComingPlugin();
        if (nextUpComingPlugin != null && !nextUpComingPlugin.equals(pluginEntry)) {
            Optional<ZonedDateTime> nextStartTime = nextUpComingPlugin.getCurrentStartTriggerTime();
            if (nextStartTime.isPresent()) {
                Duration timeUntilNext = Duration.between(
                    ZonedDateTime.now(ZoneId.systemDefault()), nextStartTime.get());
                
                int minThreshold = config.minManualStartThresholdMinutes();
                
                if (timeUntilNext.toMinutes() < minThreshold) {
                    return "Cannot start plugin: \n\tNext scheduled plugin due in less than " + 
                           minThreshold + " minute(s)";
                }
            }
        }
        
        // If we're on a break, interrupt it
        if (currentState.isBreaking()) {
            log.info("\n--Interrupting break to manually start plugin: \n\t\n--\"{}\"", pluginEntry.getCleanName());
            interruptBreak();
        }
        
        // Start the plugin
        log.info("Manually starting plugin: {}", pluginEntry.getCleanName());
        startPluginScheduleEntry(pluginEntry);
        
        return ""; // Empty string means success
    }
    
    /**
     * Register a stop completion callback with the given plugin schedule entry.
     * The callback will save the scheduled plugins state when a plugin stop is completed.
     * 
     * @param entry The plugin schedule entry to register the callback with
     */
    private void registerStopCompletionCallback(PluginScheduleEntry entry) {
        entry.setStopCompletionCallback((stopEntry, wasSuccessful) -> {
            // Save scheduled plugins state when a plugin stop is completed
            saveScheduledPlugins();
            log.info("\n\t -Saved scheduled plugins after stop completion for plugin \n\t\t'{}'", 
                stopEntry.getName());
        });
    }

    
    
    /**
     * Checks if the scheduler or the currently running plugin is paused.
     * 
     * @return true if either the scheduler or the current plugin is paused
     */
    public boolean isPaused() {
        return currentState == SchedulerState.SCHEDULER_PAUSED || 
               currentState == SchedulerState.RUNNING_PLUGIN_PAUSED;
    }

    public boolean isCurrentPluginPaused() {        
        if (getCurrentPlugin() == null) {
            return false; // No current plugin
        }
        return getCurrentPlugin().isPaused() && 
               (currentState.isPaused() || currentState.isBreaking());
    }
    public boolean allPluginEntryPaused() {
        // Check if all scheduled plugins are paused
        return scheduledPlugins.stream().allMatch(PluginScheduleEntry::isPaused);
    }
    public boolean anyPluginEntryPaused() {
        // Check if any scheduled plugin is paused
        return scheduledPlugins.stream().anyMatch(PluginScheduleEntry::isPaused);
    }
    private void pauseAllScheduledPlugins() {       
        scheduledPlugins.stream().map( PluginScheduleEntry::pause);
           
    }
    private void resumeAllScheduledPlugins() {        
        scheduledPlugins.stream().map( PluginScheduleEntry::resume);
    }
    public boolean pauseRunningPlugin(){
        if (currentState != SchedulerState.RUNNING_PLUGIN ||  getCurrentPlugin() == null) {            
            return false; // Not running a plugin
        }           
        if (currentState != SchedulerState.RUNNING_PLUGIN){
            log.error("Scheduler state is not RUNNING_PLUGIN, but {}", currentState);
            return false; // Not paused
        }
        // Use the PluginPauseEvent to pause the current plugin
        PluginPauseEvent.setPaused(true);
        
        
        
        setState(SchedulerState.RUNNING_PLUGIN_PAUSED);
        
        // Also pause time conditions on the current plugin
        getCurrentPlugin().pause();                    
        log.info("Paused currently running plugin: {}", getCurrentPlugin().getName());
        SwingUtilities.invokeLater(this::forceUpdatePanels);
        return true;
    }

    public boolean resumeRunningPlugin(){
        if(isOnBreak() ){
            log.info("Interrupting break to resume running plugin: {}", getCurrentPlugin().getName());
            interruptBreak();            
        
        }
         if ( ( currentState != SchedulerState.RUNNING_PLUGIN_PAUSED && !currentState.isBreaking())|| getCurrentPlugin() == null) {
            log.error("resumeRunningPlugin -  Scheduler state is", currentState);
            return false; // Not paused
        }
        if (prvState != SchedulerState.RUNNING_PLUGIN ){
            log.error("Prv Scheduler state is not RUNNING_PLUGIN_PAUSED, but {}", prvState);
            return false; // Not paused
        }                              
        if (isCurrentPluginPaused() == false) {
            log.error("Current plugin is not paused, but {}", currentState);
            return false; // Not paused
        }          
      
        // Restore previous state
        setState(SchedulerState.RUNNING_PLUGIN);
        
        // Use the PluginPauseEvent to resume the current plugin
        PluginPauseEvent.setPaused(false);
        
        // resume time conditions on the current plugin
        getCurrentPlugin().resume();

        

        boolean anyPausedPluginEntry = anyPluginEntryPaused();
        log.info("resumed currently running plugin: {} -> are any paused plugin? -{} - Pause Event? -{}", getCurrentPlugin().getName(),anyPausedPluginEntry,
            PluginPauseEvent.isPaused());        
        SwingUtilities.invokeLater(this::forceUpdatePanels);
        return true;
    }

    /**
     * Pauses the scheduler or the currently running plugin.
     * If a plugin is currently running, it will be paused using the PluginPauseEvent.
     * Otherwise, the entire scheduler will be paused.
     * 
     * @return true if successfully paused, false otherwise
     */
    public boolean pauseScheduler() {
        if (isPaused()) {
            return false; // Already paused
        }                                      
        if (getCurrentPlugin() != null && currentState == SchedulerState.RUNNING_PLUGIN) {
             // Use the PluginPauseEvent to pause the current plugin
            PluginPauseEvent.setPaused(true);
        }
                            
        setState(SchedulerState.SCHEDULER_PAUSED);

            
        // Pause time conditions for all scheduled plugins
        for (PluginScheduleEntry entry : scheduledPlugins) {
            entry.pause();
        }
                                           
        SwingUtilities.invokeLater(this::forceUpdatePanels);
        return true;
    }
    
    /**
     * resumes the scheduler or the currently running plugin.
     * 
     * @return true if successfully resumed, false otherwise
     */ 
    public boolean resumeScheduler() {
        if (!isPaused()) {
            return false; // Not paused
        }
        SchedulerState prvStateLocal = this.prvState;
        if (getCurrentPlugin() != null && prvStateLocal == SchedulerState.RUNNING_PLUGIN) {
             // Use the PluginPauseEvent to pause the current plugin
            PluginPauseEvent.setPaused(false);
        }
            
        if(isOnBreak() && prvStateLocal == SchedulerState.RUNNING_PLUGIN && currentState.isBreaking() ){
           interruptBreak(); 
           setState( SchedulerState.RUNNING_PLUGIN);
           log.info("resuming the plugin scheduler and interrupted break");
        }else if (currentState == SchedulerState.SCHEDULER_PAUSED  || currentState.isBreaking()) {                       
            // Restore previous state
            if (currentState.isBreaking() && !isOnBreak()){            
                if(currentPlugin!=null ){
                    setState( SchedulerState.RUNNING_PLUGIN);
                    currentPlugin.resume();
                    log.info("resumed scheduler in to running plugin, previous state: {}", prvStateLocal);
                }else{
                    setState(SchedulerState.SCHEDULING);
                    log.info("resumed scheduler in to waiting for schedule, previous state: {}", prvStateLocal);
                }
            }else if (isOnBreak()){
                setState(SchedulerState.BREAK);              
                log.info("resumed scheduler in to break, previous state: {}", prvStateLocal);
            }else{
                setState(prvStateLocal);
            }
            
        }else{
            log.error("Cannot resume scheduler, current state is: {}", currentState);
            return false; // Not paused
        }
        // resume time conditions for all scheduled plugins
        for (PluginScheduleEntry entry : scheduledPlugins) {
            entry.resume();
        }      
        boolean anyPausedPluginEntry = anyPluginEntryPaused();
        log.info("resumed the scheduler plugin: {} -> are any paused plugin? -{} - Pause Event? -{}", getCurrentPlugin().getName(),anyPausedPluginEntry,
            PluginPauseEvent.isPaused());      
        SwingUtilities.invokeLater(this::forceUpdatePanels);
        return true;
    }
    
    
    /**
     * Gets the estimated time until the next scheduled plugin will be ready to run.
     * This method uses the new estimation system to provide more accurate predictions
     * for when plugins will be scheduled, considering both current running plugins
     * and upcoming plugin start conditions.
     * 
     * @return Optional containing the estimated duration until the next plugin can be scheduled
     */
    public Optional<Duration> getUpComingEstimatedScheduleTime() {
        // First, check if we have a currently running plugin that might stop soon
        Optional<Duration> currentPluginStopEstimate = getCurrentPluginEstimatedStopTime();
        
        // Get the next upcoming plugin
        PluginScheduleEntry upcomingPlugin = getUpComingPlugin();
        if (upcomingPlugin == null) {
            // If no upcoming plugin, return the current plugin's estimated stop time
            return currentPluginStopEstimate;
        }
        
        // Get the estimated start time for the upcoming plugin
        Optional<Duration> upcomingPluginStartEstimate = upcomingPlugin.getEstimatedStartTimeWhenIsSatisfied();
        
        // If we have both estimates, return the longer one (more conservative estimate)
        if (currentPluginStopEstimate.isPresent() && upcomingPluginStartEstimate.isPresent()) {
            Duration stopTime = currentPluginStopEstimate.get();
            Duration startTime = upcomingPluginStartEstimate.get();
            return Optional.of(stopTime.compareTo(startTime) > 0 ? stopTime : startTime);
        }
        
        // Return whichever estimate we have
        return upcomingPluginStartEstimate.isPresent() ? upcomingPluginStartEstimate : currentPluginStopEstimate;
    }
    
    /**
     * Gets the estimated time until the next plugin will be scheduled within a specific time window.
     * This method considers both current plugin stop conditions and upcoming plugin start conditions
     * within the specified time frame.
     * 
     * @param timeWindow The time window to look ahead for upcoming plugins
     * @return Optional containing the estimated duration until the next plugin can be scheduled within the window
     */
    public Optional<Duration> getUpComingEstimatedScheduleTimeWithinTime(Duration timeWindow) {
        // First, check if we have a currently running plugin that might stop soon
        Optional<Duration> currentPluginStopEstimate = getCurrentPluginEstimatedStopTime();
        
        // Get the next upcoming plugin within the time window
        PluginScheduleEntry upcomingPlugin = getUpComingPluginWithinTime(timeWindow);
        if (upcomingPlugin == null) {
            // If no upcoming plugin within time window, return current plugin's stop estimate
            // but only if it's within the time window
            if (currentPluginStopEstimate.isPresent() && 
                currentPluginStopEstimate.get().compareTo(timeWindow) <= 0) {
                return currentPluginStopEstimate;
            }
            return Optional.empty();
        }
        
        // Get the estimated start time for the upcoming plugin
        Optional<Duration> upcomingPluginStartEstimate = upcomingPlugin.getEstimatedStartTimeWhenIsSatisfied();
        
        // Filter estimates to only include those within the time window
        if (upcomingPluginStartEstimate.isPresent() && 
            upcomingPluginStartEstimate.get().compareTo(timeWindow) > 0) {
            upcomingPluginStartEstimate = Optional.empty();
        }
        
        if (currentPluginStopEstimate.isPresent() && 
            currentPluginStopEstimate.get().compareTo(timeWindow) > 0) {
            currentPluginStopEstimate = Optional.empty();
        }
        
        // If we have both estimates within the window, return the longer one
        if (currentPluginStopEstimate.isPresent() && upcomingPluginStartEstimate.isPresent()) {
            Duration stopTime = currentPluginStopEstimate.get();
            Duration startTime = upcomingPluginStartEstimate.get();
            return Optional.of(stopTime.compareTo(startTime) > 0 ? stopTime : startTime);
        }
        
        // Return whichever estimate we have within the window
        return upcomingPluginStartEstimate.isPresent() ? upcomingPluginStartEstimate : currentPluginStopEstimate;
    }
    
    /**
     * Gets the estimated time until the currently running plugin will stop.
     * This considers user-defined stop conditions for the current plugin.
     * 
     * @return Optional containing the estimated duration until the current plugin stops
     */
    private Optional<Duration> getCurrentPluginEstimatedStopTime() {
        if (currentPlugin == null || !currentPlugin.isRunning()) {
            return Optional.empty();
        }
        
        return currentPlugin.getEstimatedStopTimeWhenIsSatisfied();
    }
    
    /**
     * Gets a formatted string representation of the estimated schedule time.
     * 
     * @return A human-readable string describing when the next plugin is estimated to be scheduled
     */
    public String getUpComingEstimatedScheduleTimeDisplay() {
        Optional<Duration> estimate = getUpComingEstimatedScheduleTime();
        if (estimate.isPresent()) {
            return formatEstimatedScheduleDuration(estimate.get());
        }
        return "Cannot estimate next schedule time";
    }
    
    /**
     * Gets a formatted string representation of the estimated schedule time within a time window.
     * 
     * @param timeWindow The time window to consider
     * @return A human-readable string describing when the next plugin is estimated to be scheduled
     */
    public String getUpComingEstimatedScheduleTimeWithinTimeDisplay(Duration timeWindow) {
        Optional<Duration> estimate = getUpComingEstimatedScheduleTimeWithinTime(timeWindow);
        if (estimate.isPresent()) {
            return formatEstimatedScheduleDuration(estimate.get());
        }
        return "No plugins estimated within time window";
    }
    
    /**
     * Helper method to format estimated schedule durations into human-readable strings.
     * 
     * @param duration The duration to format
     * @return A formatted string representation
     */
    private String formatEstimatedScheduleDuration(Duration duration) {
        long seconds = duration.getSeconds();
        
        if (seconds <= 0) {
            return "Next plugin can be scheduled now";
        } else if (seconds < 60) {
            return String.format("Next plugin estimated in ~%d seconds", seconds);
        } else if (seconds < 3600) {
            return String.format("Next plugin estimated in ~%d minutes", seconds / 60);
        } else if (seconds < 86400) {
            return String.format("Next plugin estimated in ~%d hours", seconds / 3600);
        } else {
            long days = seconds / 86400;
            return String.format("Next plugin estimated in ~%d days", days);
        }
    }    
}
