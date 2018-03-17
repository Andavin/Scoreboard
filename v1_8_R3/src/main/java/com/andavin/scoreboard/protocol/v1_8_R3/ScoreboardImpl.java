package com.andavin.scoreboard.protocol.v1_8_R3;

import com.andavin.scoreboard.protocol.Scoreboard;
import com.andavin.scoreboard.util.Reflection;
import net.minecraft.server.v1_8_R3.IScoreboardCriteria.EnumScoreboardHealthDisplay;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardObjective;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardScore.EnumScoreboardAction;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created on March 16, 2018
 *
 * @author Andavin
 */
public class ScoreboardImpl extends Scoreboard {

    // Objective Fields
    private static final Field OBJ_NAME = Reflection.getField(PacketPlayOutScoreboardObjective.class, "a");
    private static final Field DISPLAY = Reflection.getField(PacketPlayOutScoreboardObjective.class, "b");
    private static final Field HEALTH_DISPLAY = Reflection.getField(PacketPlayOutScoreboardObjective.class, "c");
    private static final Field ACTION = Reflection.getField(PacketPlayOutScoreboardObjective.class, "d");
    // Objective Slot Fields
    private static final Field SLOT = Reflection.getField(PacketPlayOutScoreboardDisplayObjective.class, "a");
    private static final Field OBJ_NAME_1 = Reflection.getField(PacketPlayOutScoreboardDisplayObjective.class, "b");
    // Score Fields
    private static final Field OBJ_NAME_2 = Reflection.getField(PacketPlayOutScoreboardScore.class, "b");
    private static final Field SCORE = Reflection.getField(PacketPlayOutScoreboardScore.class, "c");
    private static final Field ACTION_1 = Reflection.getField(PacketPlayOutScoreboardScore.class, "d");
    // Team Fields
    private static final Field TEAM_NAME = Reflection.getField(PacketPlayOutScoreboardTeam.class, "a");
    private static final Field DISPLAY_NAME = Reflection.getField(PacketPlayOutScoreboardTeam.class, "b");
    private static final Field PREFIX = Reflection.getField(PacketPlayOutScoreboardTeam.class, "c");
    private static final Field SUFFIX = Reflection.getField(PacketPlayOutScoreboardTeam.class, "d");
    private static final Field ENTRIES = Reflection.getField(PacketPlayOutScoreboardTeam.class, "g");
    private static final Field ACTION_2 = Reflection.getField(PacketPlayOutScoreboardTeam.class, "h");

    @Override
    protected Object createObjectivePacket(final String objName, final String displayName, final int actionId) {
        final PacketPlayOutScoreboardObjective packet = new PacketPlayOutScoreboardObjective();
        Reflection.setValue(OBJ_NAME, packet, objName);
        Reflection.setValue(DISPLAY, packet, displayName);
        Reflection.setValue(HEALTH_DISPLAY, packet, EnumScoreboardHealthDisplay.INTEGER);
        Reflection.setValue(ACTION, packet, actionId);
        return packet;
    }

    @Override
    protected Object createDisplaySlotPacket(final String objName, final int displaySlot) {
        final PacketPlayOutScoreboardDisplayObjective packet = new PacketPlayOutScoreboardDisplayObjective();
        Reflection.setValue(SLOT, packet, displaySlot);
        Reflection.setValue(OBJ_NAME_1, packet, objName);
        return packet;
    }

    @Override
    protected Object createTeamPacket(final String name, final String displayName, final String prefix,
            final String suffix, final int action) {

        final PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
        Reflection.setValue(TEAM_NAME, packet, name);
        Reflection.setValue(ACTION_2, packet, action);
        if (action == 0 || action == 2) { // Create or update
            Reflection.setValue(DISPLAY_NAME, packet, displayName);
            Reflection.setValue(PREFIX, packet, prefix);
            Reflection.setValue(SUFFIX, packet, suffix);
        }

        return packet;
    }

    @Override
    protected Object createAddPacket(final String objName, final String line, final int score) {
        // Set as many fields as possible using regular Java (just one)
        final PacketPlayOutScoreboardScore packet = new PacketPlayOutScoreboardScore(line);
        Reflection.setValue(OBJ_NAME_2, packet, objName);
        Reflection.setValue(SCORE, packet, score);
        Reflection.setValue(ACTION_1, packet, EnumScoreboardAction.CHANGE);
        return packet;
    }

    @Override
    protected Object createRemovePacket(final String objName, final String line) {
        final PacketPlayOutScoreboardScore score = new PacketPlayOutScoreboardScore(line);
        Reflection.setValue(OBJ_NAME_2, score, objName);
        return score;
    }

    @Override
    protected void send(final Player player, final Object packet) {
        // If this method is called while the player is still initializing
        // then the player connection could be null
        final PlayerConnection conn = ((CraftPlayer) player).getHandle().playerConnection;
        if (conn != null) {
            conn.sendPacket((Packet) packet);
        }
    }

    @Override
    protected <T> void send(final Player player, final List<T> packets) {

        final PlayerConnection conn = ((CraftPlayer) player).getHandle().playerConnection;
        if (conn != null) {
            packets.stream().map(Packet.class::cast).forEach(conn::sendPacket);
        }
    }
}
