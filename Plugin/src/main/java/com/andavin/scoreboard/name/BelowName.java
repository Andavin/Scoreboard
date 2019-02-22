//package com.andavin.scoreboard.name;
//
//import com.andavin.scoreboard.ScoreboardModule;
//import com.andavin.scoreboard.protocol.Scoreboard;
//import org.bukkit.entity.Player;
//import org.bukkit.scoreboard.DisplaySlot;
//
///**
// * @since February 13, 2019
// * @author Andavin
// */
//public class BelowName extends ScoreboardModule {
//
//    private final String objName;
//
//    public BelowName(Player player, String displayName) {
//        super(player);
//        this.objName = "obj-" + Scoreboard.getNextId();
//        Scoreboard.createObjective(player, displayName, this.objName, DisplaySlot.BELOW_NAME);
//    }
//
//    /**
//     * Set the display name that will show before the score
//     * below their name.
//     *
//     * @param displayName The new display name.
//     */
//    public void setDisplayName(String displayName) {
//        Scoreboard.setDisplayName(this.getPlayer(), displayName, this.objName);
//    }
//
//    /**
//     * Set the score of the player that will show below
//     * their name.
//     *
//     * @param score The score to set to.
//     */
//    public void setScore(int score) {
//
//    }
//
//    @Override
//    public void destroy() {
//        super.destroy();
//    }
//}
