package com.andavin.scoreboard.protocol;

import com.andavin.scoreboard.util.Logger;
import com.andavin.scoreboard.util.Reflection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created on March 16, 2018
 *
 * @author Andavin
 */
public abstract class Scoreboard {

    /**
     * A basic {@link Executor executor service} for sending packets
     * asynchronously. This thread pool can have a minimum of {@code 1}
     * thread in it if it is not in use and can have a maximum of {@code 5}
     * threads alive at one time.
     * <p>
     * The pool will let threads die if they have gone 60 seconds
     * without use.
     */
    public static final Executor EXECUTOR = new ThreadPoolExecutor(1, 5,
            60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("Scoreboard - %d").build());

    private static int objId;
    private static final Scoreboard INSTANCE;
    private static final int CREATE = 0, UPDATE = 2;
    private static final Map<DisplaySlot, Integer> SLOTS;

    static {

        Logger.info("Finding the proper protocol manager for your server version {} - {}.",
                Bukkit.getVersion(), Reflection.VERSION_STRING);

        final Class<? extends Scoreboard> clazz = Reflection.getClassType(
                "com.andavin.scoreboard.protocol." + Reflection.VERSION_STRING + ".ScoreboardImpl");
        if (clazz != null) {
            INSTANCE = Reflection.getInstance(clazz);
        } else {
            throw new UnsupportedOperationException("This version of Minecraft (" + Bukkit.getVersion() + ") is not supported.");
        }

        final Builder<DisplaySlot, Integer> builder = ImmutableMap.builder();
        for (final DisplaySlot slot : DisplaySlot.values()) {

            switch (slot) {
                case PLAYER_LIST:
                    builder.put(slot, 1);
                    break;
                case SIDEBAR:
                    builder.put(slot, 2);
                    break;
                case BELOW_NAME:
                    builder.put(slot, 2);
                    break;
            }
        }

        SLOTS = builder.build();
    }

    public static void sendPacket(final Player player, final Object packet) {
        INSTANCE.send(player, packet);
    }

    public static <Packet> void sendPacket(final Player player, final List<Packet> packets) {
        INSTANCE.send(player, packets);
    }

    public static void createObjective(final Player player, final String displayName, final String objName, final DisplaySlot slot) {

        EXECUTOR.execute(() -> {
            final Object create = INSTANCE.createObjectivePacket(objName, displayName, CREATE);
            final Object display = INSTANCE.createDisplaySlotPacket(objName, SLOTS.get(slot));
            INSTANCE.send(player, Arrays.asList(create, display));
        });
    }

    public static void setDisplayName(final Player player, final String displayName, final String objName) {
        EXECUTOR.execute(() -> INSTANCE.send(player, INSTANCE.createObjectivePacket(objName, displayName, UPDATE)));
    }

    public static Object getAddPacket(final String objName, final String line, final int score) {
        return INSTANCE.createAddPacket(objName, line, score);
    }

    public static Object getRemovePacket(final String objName, final String line) {
        return INSTANCE.createRemovePacket(objName, line);
    }

    public static int getNextId() {
        return objId++;
    }

    protected abstract Object createObjectivePacket(final String objName, final String displayName, final int actionId);

    protected abstract Object createDisplaySlotPacket(final String objName, final int displaySlot);

    protected abstract Object createAddPacket(final String objName, final String line, final int score);

    protected abstract Object createRemovePacket(final String objName, final String line);

    protected abstract void send(final Player player, final Object packet);

    protected abstract <Packet> void send(final Player player, final List<Packet> packets);
}
