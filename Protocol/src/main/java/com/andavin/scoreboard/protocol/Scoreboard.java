package com.andavin.scoreboard.protocol;

import com.andavin.scoreboard.util.Logger;
import com.andavin.scoreboard.util.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on March 16, 2018
 *
 * @author Andavin
 */
public abstract class Scoreboard {

    private static final Scoreboard INSTANCE;
    private static final int CREATE = 0, DELETE = 1, UPDATE = 2;
    private static final AtomicInteger OBJ_ID = new AtomicInteger();
    private static final Map<DisplaySlot, Integer> SLOTS = new EnumMap<>(DisplaySlot.class);

    static {

        Logger.info("Finding the proper protocol manager for your server version {} - {}.",
                Bukkit.getVersion(), Reflection.VERSION_STRING);

        Class<? extends Scoreboard> clazz = Reflection.getClassType(
                "com.andavin.scoreboard.protocol." + Reflection.VERSION_STRING + ".ScoreboardImpl");
        if (clazz != null) {
            INSTANCE = Reflection.getInstance(clazz);
        } else {
            throw new UnsupportedOperationException("This version of Minecraft (" + Bukkit.getVersion() + ") is not supported.");
        }

        for (DisplaySlot slot : DisplaySlot.values()) {

            switch (slot) {
                case PLAYER_LIST:
                    SLOTS.put(slot, 0);
                    break;
                case SIDEBAR:
                    SLOTS.put(slot, 1);
                    break;
                case BELOW_NAME:
                    SLOTS.put(slot, 2);
                    break;
            }
        }
    }

    /**
     * Send a packet to the given player.
     *
     * @param player The player to send the packet to.
     * @param packet The packet object to send.
     * @throws ClassCastException If the packet type is not an instance of the NMS Packet class.
     */
    public static void sendPacket(Player player, Object packet) throws ClassCastException {
        INSTANCE.send(player, packet);
    }

    /**
     * Send multiple packets the given player at one. This method
     * is slightly more efficient than repeated called to {@link #send(Player, Object)}.
     *
     * @param player The player to send the packet to.
     * @param packets The list of packets to send.
     * @param <Packet> The type of the packet (usually {@link Object}).
     * @throws ClassCastException If the packet type is not an instance of the NMS Packet class.
     */
    public static <Packet> void sendPacket(Player player, List<Packet> packets) throws ClassCastException {
        INSTANCE.send(player, packets);
    }

    /**
     * Create a new objective and let the player know about it for the given
     * slot with the slot ID.
     *
     * @param player The player to create the objective for.
     * @param displayName The display name of the objective.
     * @param objName The objective unique ID name.
     * @param slot The ID of the slot the objective should be created for.
     */
    public static void createObjective(Player player, String displayName, String objName, DisplaySlot slot) {
        Object create = INSTANCE.createObjectivePacket(objName, displayName, CREATE);
        Object display = INSTANCE.createDisplaySlotPacket(objName, SLOTS.get(slot));
        INSTANCE.send(player, Arrays.asList(create, display));
    }

    public static void deleteObjective(Player player, String objName) {
        INSTANCE.send(player, INSTANCE.createObjectivePacket(objName, null, DELETE));
    }

    /**
     * Set the display name of an objective.
     *
     * @param player The player that has the objective to change the name for.
     * @param displayName The new display name of the objective.
     * @param objName The objective unique ID name.
     */
    public static void setDisplayName(Player player, String displayName, String objName) {
        INSTANCE.send(player, INSTANCE.createObjectivePacket(objName, displayName, UPDATE));
    }

    /**
     * Create a new team for the given player's scoreboard.
     *
     * @param player The player to create the team for.
     * @param name The unique ID name of the team.
     * @param line The line of text to make the displays of the team.
     */
    public static void createTeam(Player player, String name, String line) {
        // Create a team with an action ID of 0
        String displayName = getDisplayName(line);
        INSTANCE.send(player, INSTANCE.createTeamPacket(name, displayName, getPrefix(line), getSuffix(line), 0, displayName));
    }

    /**
     * Create a new team for the given player's scoreboard.
     *
     * @param player The player to create the team for.
     * @param name The unique ID name of the team.
     * @param prefix The prefix for the team to display.
     * @param suffix The suffix for the team to display.
     * @param member The name of the player to add to the team.
     */
    public static void createTeam(Player player, String name, String prefix, String suffix, String member) {
        // Create a team with an action ID of 0
        INSTANCE.send(player, INSTANCE.createTeamPacket(name, member, prefix, suffix, 0, member));
    }

    /**
     * Update a team for the given player.
     *
     * @param player The player to update the team for.
     * @param name The unique ID name of the team.
     * @param oldLine The old line of text that was displayed for the team.
     * @param newLine The new line of text to update the displays to.
     */
    public static void updateTeam(Player player, String name, String oldLine, String newLine) {
        String display = getDisplayName(newLine);
        // Remove the old "player" from the team then add the new one to it and update the team data in that order
        Object remove = INSTANCE.createTeamPacket(name, null, null, null, 4, getDisplayName(oldLine));
        Object add = INSTANCE.createTeamPacket(name, null, null, null, 3, display);
        Object update = INSTANCE.createTeamPacket(name, display, getPrefix(newLine), getSuffix(newLine), 2, null);
        INSTANCE.send(player, Arrays.asList(remove, add, update));
    }

    /**
     * Delete a team from the given player.
     *
     * @param player The player to delete the team for.
     * @param name The unique ID name of the team to delete.
     */
    public static void removeTeam(Player player, String name) {
        // Remove a team with action ID 1. The others will not be used if the ID isn't create or update
        INSTANCE.send(player, INSTANCE.createTeamPacket(name, null, null, null, 1, null));
    }

    /**
     * Get a new instance of a packet that contains the settings
     * to add the given line and score to a scoreboard objective.
     *
     * @param objName The objective name that the score belongs to.
     * @param line The line of text to display for the score.
     * @param score The score or, for our purposes, index of the score.
     * @return The newly created {@code "add"} packet object.
     */
    public static Object getAddPacket(String objName, String line, int score) {
        return INSTANCE.createAddPacket(objName, line, score);
    }

    /**
     * Get a new instance of a packet that contains the settings
     * to remove the given line and score from a scoreboard objective.
     *
     * @param objName The objective name that the score belongs to.
     * @param line The line of text that is being displayed for the score.
     * @return The newly created {@code "remove"} packet object.
     */
    public static Object getRemovePacket(String objName, String line) {
        return INSTANCE.createRemovePacket(objName, line);
    }

    /**
     * Get the next integer ID. This ID should be unique always
     * if, and only if, it is the only source of the ID.
     * <p>
     * Note that if this is used for an extended period of time
     * or at an extremely frequent rate so that it is used more
     * than 2<sup>32</sup> times in a single session and the oldest
     * IDs are still in use, then the IDs will no longer be unique.
     *
     * @return The next integer ID.
     */
    public static int getNextId() {
        return OBJ_ID.getAndIncrement();
    }

    /**
     * Create a new objective packet that will perform the given
     * action when sent to a client.
     *
     * @param objName The client relative unique name ID for the objective.
     * @param displayName The display name of the objective.
     * @param actionId The action to perform for this objective (create, edit, etc.).
     * @return The newly created objective packet object.
     */
    protected abstract Object createObjectivePacket(String objName, String displayName, int actionId);

    /**
     * Create a new objective packet that will tell the client
     * which display slot the objective is for.
     *
     * @param objName The client relative unique name ID for the objective.
     * @param displaySlot The ID of the display slot to place the objective at.
     * @return The newly created objective slot packet object.
     */
    protected abstract Object createDisplaySlotPacket(String objName, int displaySlot);

    /**
     * Create a new packet for a team to perform an action for that
     * team relative to the client it is sent to.
     *
     * @param name The client relative unique name ID for the team.
     * @param displayName The display name of the team (up to 32 characters).
     * @param prefix The prefix for the team (up to 16 characters).
     * @param suffix The suffix for the team (up to 16 characters).
     * @param action The action to perform for this team (create, update, etc.).
     * @param member The members of the team.
     * @return The newly created team action packet object.
     */
    protected abstract Object createTeamPacket(String name, String displayName, String prefix, String suffix,
                                               int action, String member);

    /**
     * Create a new packet that will add a score to a scoreboard
     * objective for the player.
     *
     * @param objName The client relative unique name ID for the objective.
     * @param line The line of text to display for the score.
     * @param score The score or, for our purposes, index of the score.
     * @return The newly created {@code "add"} packet object.
     */
    protected abstract Object createAddPacket(String objName, String line, int score);

    /**
     * Get a new instance of a packet that contains the settings
     * to remove the given line and score from a scoreboard objective.
     *
     * @param objName The objective name that the score belongs to.
     * @param line The line of text that is being displayed for the score.
     * @return The newly created {@code "remove"} packet object.
     */
    protected abstract Object createRemovePacket(String objName, String line);

    /**
     * Send a packet to the given player.
     *
     * @param player The player to send the packet to.
     * @param packet The packet object to send.
     * @throws ClassCastException If the packet type is not an instance of the NMS Packet class.
     */
    protected abstract void send(Player player, Object packet);

    /**
     * Send multiple packets the given player at one. This method
     * is slightly more efficient than repeated called to {@link #send(Player, Object)}.
     *
     * @param player The player to send the packet to.
     * @param packets The list of packets to send.
     * @param <Packet> The type of the packet (usually {@link Object}).
     * @throws ClassCastException If the packet type is not an instance of the NMS Packet class.
     */
    protected abstract <Packet> void send(Player player, List<Packet> packets);

    private static String getPrefix(String line) {
        // If the line is more than 32 long then get the first 16 characters
        // If it's not that long then the display name can cover the whole thing
        return line.length() <= 32 ? "" : line.substring(0, 16);
    }

    private static String getDisplayName(String line) {
        // If it's less than 32 character long then we're good to just use the whole thing
        // If the line is more than 32 long, but less than 48, then get from 16 on
        // If it is more than 48 then get from 16 to 48
        return line.length() <= 32 ? line : line.length() <= 48 ?
                line.substring(16) : line.substring(16, 48);
    }

    private static String getSuffix(String line) {
        // If the line is more than 48 then get whatever characters are after that
        return line.length() <= 48 ? "" : line.substring(48);
    }
}
