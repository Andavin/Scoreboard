package com.andavin.scoreboard.sidebar;

import com.andavin.scoreboard.util.Limiter;
import org.bukkit.entity.Player;

/**
 * Created on March 16, 2018
 *
 * @author Andavin
 */
public enum SideBarType {

    /**
     * A {@link SideBar} type that primarily uses plain
     * score text for displaying information.
     * <p>
     * This implementation tends to be a lot simpler. It
     * is more likely to bode well with a BungeeCord server,
     * but is about the same or worse performance than
     * {@link #TEAM}, however, has less overhead.
     * <p>
     * Downside of this type is that it has a limit of 40
     * characters per line. Usually, this should be plenty
     * and, unless using lots of different colors, would
     * already make the sidebar jut out quite a bit.
     */
    SCORE {
        @Override
        SideBar newInstance(final Player player, final String displayName, final Limiter limiter) {
            return new ScoreSideBar(player, displayName, limiter);
        }
    },

    /**
     * A {@link SideBar} type that primarily uses teams
     * with prefixes, suffixes and display names to display
     * information.
     * <p>
     * This implementation is a bit more efficient in some
     * ways and less in others. For changing the displayed
     * data, for instance, it is more efficient since the
     * display name of teams can be changed.
     * <br>
     * On the other hand, when scores are changed it takes
     * at least one or two more packets per entry over {@link #SCORE}.
     * <p>
     * The biggest upside to this implementation is that
     * the character limit is 60 characters. This almost unusable,
     * however, some will want to use it for lots of colors
     * or similar entries. The only reason this implementation should
     * be used is for the extra length in characters.
     */
    TEAM {
        @Override
        SideBar newInstance(final Player player, final String displayName, final Limiter limiter) {
            return new TeamSideBar(player, displayName, limiter);
        }
    };

    /**
     * Create a new instance of this type of {@link SideBar}.
     *
     * @param player The player to create the instance for.
     * @param displayName The display name to initially give the sidebar.
     * @param limiter The {@link Limiter} to limit how fast the sidebar can be updated.
     * @return The new {@link SideBar instance}.
     */
    abstract SideBar newInstance(final Player player, final String displayName, final Limiter limiter);
}
