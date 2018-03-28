package com.andavin.scoreboard.sidebar;

import com.andavin.scoreboard.SBPlugin;
import com.andavin.scoreboard.protocol.Scoreboard;
import com.andavin.scoreboard.util.Limiter;
import com.andavin.scoreboard.util.Logger;
import com.andavin.scoreboard.util.NoLimit;
import com.andavin.scoreboard.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created on March 13, 2018
 *
 * @author Andavin
 */
public abstract class Sidebar {

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
    public static Sidebar create(final Player player, final String displayName) {
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
    public static Sidebar create(final Player player, final String displayName, final Limiter limiter) {
        return SBPlugin.getSideBarType().newInstance(player, displayName, limiter);
    }

    private static final long MAX_LOG_TIME = TimeUnit.MINUTES.toMillis(2);
    // Tracker values for how often the scoreboard
    // actually updates for the player
    private long lastUpdate, lastAverageTaken, lastAverage;
    private final List<Long> updateIntervals = new LinkedList<>();

    final String objName;
    final Limiter limiter;
    private final UUID uuid;
    private final WeakReference<Player> player;
    final List<String> oldLines = Collections.synchronizedList(new ArrayList<>(19));

    Sidebar(@Nonnull final Player player, final String displayName, final Limiter limiter) {
        this.uuid = player.getUniqueId();
        this.player = new WeakReference<>(player);
        this.limiter = limiter;
        this.objName = "obj-" + Scoreboard.getNextId();
        Scoreboard.createObjective(player, displayName, this.objName, DisplaySlot.SIDEBAR);
    }

    /**
     * Get the player that this sidebar is being
     * displayed for.
     *
     * @return The player.
     */
    @Nullable
    public Player getPlayer() {
        final Player player = this.player.get();
        return player != null ? player : Bukkit.getPlayer(this.uuid);
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
    public void setDisplayName(final String displayName) {

        final Player player = this.getPlayer();
        if (player != null) {
            Scoreboard.setDisplayName(player, displayName, this.objName);
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
    public void display(final List<String> lines) {
        this.display(lines.toArray(new String[lines.size()]));
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
    public abstract void display(final String... lines);

    /**
     * Update a recalculate the basic timing statistics
     * for this scoreboard of how often it updates for
     * the player.
     *
     * @param player The player is being displayed to.
     */
    void updateStatistics(final Player player) {

        final long now = System.currentTimeMillis();
        if (this.lastUpdate > 0) {
            // Only calculate if we have received at least one update
            final long diff = now - this.lastUpdate;
            this.updateIntervals.add(diff);
            final int updateSize = this.updateIntervals.size();
            if (updateSize > 200 || now - this.lastAverageTaken > MAX_LOG_TIME) {
                // Calculate the average, log it and clear stats
                long average = 0, lastInterval = 0, fluctuation = 0;
                for (final Long interval : this.updateIntervals) {

                    if (lastInterval > 0) {
                        fluctuation = Math.max(interval - lastInterval, fluctuation);
                    }

                    lastInterval = interval;
                    average += interval;
                }

                average /= updateSize;
                if (this.lastAverageTaken > 0) {
                    Logger.debug("Average display interval for {} is {} for {} records.\n" +
                                 "The last average of {} was taken {} ago.\n" +
                                 "The largest fluctuation in the interval logs was {}.\n" +
                                 "High fluctuation is only cause for concern if the usage dictates consistent updates.",
                            player.getName(), TimeUtil.formatDifference(0, average, true, true), updateSize,
                            TimeUtil.formatDifference(0, this.lastAverage, true, true),
                            TimeUtil.formatDifference(now, this.lastAverageTaken, true, true),
                            TimeUtil.formatDifference(0, fluctuation, true, true)
                    );
                }

                this.lastAverage = average;
                this.lastAverageTaken = now;
                this.updateIntervals.clear();
            }
        }

        this.lastUpdate = now;
    }
}
