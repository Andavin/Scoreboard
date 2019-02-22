package com.andavin.scoreboard.sidebar;

import com.andavin.scoreboard.SBPlugin;
import com.andavin.scoreboard.ScoreboardModule;
import com.andavin.scoreboard.protocol.Scoreboard;
import com.andavin.scoreboard.util.Limiter;
import com.andavin.scoreboard.util.NoLimit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.synchronizedList;

/**
 * Created on March 13, 2018
 *
 * @author Andavin
 */
public abstract class Sidebar extends ScoreboardModule {

    /**
     * Create a new sidebar for the given player and with the initial display name.
     * <p>
     * This default the {@link Limiter} to a limit of 100 milliseconds. This tends
     * to be a good balance for update spacing so as to disallow duplicating lines
     * in the sidebar while still being an extremely fast update speed.
     * <br>
     * This can be changed using the {@link #create(Player, String, Limiter)} method.
     *
     * @param player The player that this sidebar will be shown to.
     * @param displayName The initial display name of the sidebar.
     * @return The new {@link Sidebar instance}.
     */
    @ParametersAreNonnullByDefault
    public static Sidebar create(Player player, String displayName) {
        return Sidebar.create(player, displayName, new Limiter(100L, TimeUnit.MILLISECONDS));
    }

    /**
     * Create a new sidebar for the given player and with the initial display name.
     * The {@link Limiter} can also be set to a custom setting here. Note that if
     * the limit is to be removed that {@link NoLimit} should be used over a zero
     * value (should never be {@code null}).
     *
     * @param player The player that this sidebar will be shown to.
     * @param displayName The initial display name of the sidebar.
     * @return The new {@link Sidebar instance}.
     */
    @ParametersAreNonnullByDefault
    public static Sidebar create(Player player, String displayName, Limiter limiter) {
        return SBPlugin.getSideBarType().newInstance(player, displayName, limiter);
    }

//    private static final long MAX_LOG_TIME = TimeUnit.MINUTES.toMillis(2);

    // Tracker values for how often the scoreboard
    // actually updates for the player
//    private long lastUpdate, lastAverageTaken, lastAverage;
//    private final List<Long> updateIntervals = new LinkedList<>();

    boolean destroyed;
    final String objName;
    final Limiter limiter;
    final List<String> oldLines = synchronizedList(new ArrayList<>(19));

    Sidebar(@Nonnull Player player, String displayName, Limiter limiter) {
        super(player);
        this.limiter = limiter;
        this.objName = "obj-" + Scoreboard.getNextId();
        Scoreboard.createObjective(player, displayName, this.objName, DisplaySlot.SIDEBAR);
    }

    /**
     * Set the display name of this sidebar to the given text.
     * <p>
     * <b>Warning:</b> If this method is called directly after
     * construction on a 1.7.10 server version of Minecraft it
     * can crash the player client and should be delayed at least
     * 1 tick.
     *
     * @param displayName The new display name.
     */
    public void setDisplayName(String displayName) {

        Player player = this.getPlayer();
        if (player != null) {
            Scoreboard.setDisplayName(player, displayName, this.objName);
        }
    }

    @Override
    public void destroy() {

        super.destroy();
        Player player = this.getPlayer();
        if (player != null) {
            Scoreboard.deleteObjective(player, this.objName);
        }
    }

    /**
     * Display lines of text in the sidebar for the player
     * that this is wrapping.
     * <p>
     * This method will schedule the task and then immediately
     * return. The update does not happen in this method, but
     * an unspecified amount of time after this method is called.
     *
     * @param lines The lines to send to the player.
     */
    public void display(List<String> lines) {
        this.display(lines.toArray(new String[0]));
    }

    /**
     * Display lines of text in the sidebar for the player
     * that this is wrapping.
     * <p>
     * This method will schedule the task and then immediately
     * return. The update does not happen in this method, but
     * an unspecified amount of time after this method is called.
     *
     * @param lines The lines to send to the player.
     */
    public abstract void display(String... lines);

    /**
     * Update a recalculate the basic timing statistics
     * for this scoreboard of how often it updates for
     * the player.
     *
     * @param player The player is being displayed to.
     */
    void updateStatistics(Player player) {

//        long now = System.currentTimeMillis();
//        if (this.lastUpdate > 0) {
//            // Only calculate if we have received at least one update
//            long diff = now - this.lastUpdate;
//            this.updateIntervals.add(diff);
//            int updateSize = this.updateIntervals.size();
//            if (updateSize > 200 || now - this.lastAverageTaken > MAX_LOG_TIME) {
//                // Calculate the average, log it and clear stats
//                long average = 0, lastInterval = 0, fluctuation = 0;
//                for (Long interval : this.updateIntervals) {
//
//                    if (lastInterval > 0) {
//                        fluctuation = Math.max(interval - lastInterval, fluctuation);
//                    }
//
//                    lastInterval = interval;
//                    average += interval;
//                }
//
//                average /= updateSize;
//                if (SBPlugin.getPlugin().getConfig().getBoolean("debug")) {
//
//                    Logger.debug("Average display interval for {} is {} for {} records.", player.getName(),
//                            TimeUtil.formatDifference(0, average, true, true), updateSize);
//                    Logger.debug("The largest fluctuation in the interval logs was {}.",
//                            TimeUtil.formatDifference(0, fluctuation, true, true));
//
//                    if (this.lastAverageTaken > 0) {
//                        Logger.debug("The last average of {} was taken {} ago.",
//                                TimeUtil.formatDifference(0, this.lastAverage, true, true),
//                                TimeUtil.formatDifference(now, this.lastAverageTaken, true, true)
//                        );
//                    }
//                }
//
//                this.lastAverage = average;
//                this.lastAverageTaken = now;
//                this.updateIntervals.clear();
//            }
//        }
//
//        this.lastUpdate = now;
    }
}
