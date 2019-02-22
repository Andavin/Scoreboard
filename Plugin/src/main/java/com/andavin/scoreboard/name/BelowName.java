package com.andavin.scoreboard.name;

import com.andavin.scoreboard.ScoreboardModule;
import org.bukkit.entity.Player;

/**
 * @since February 13, 2019
 * @author Andavin
 */
public class BelowName extends ScoreboardModule {

    public BelowName(Player player, String displayName) {
        super(player);
//        Scoreboard.createObjective(player, displayName, this.objName, DisplaySlot.BELOW_NAME);
    }

    /**
     * Set the display name that will show before the score.
     *
     * @param displayName The new display name.
     */
    public void setDisplayName(String displayName) {

    }

    /**
     * Set the score of the player that will show below
     * their name.
     *
     * @param score The score to set to.
     */
    public void setScore(int score) {

    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
