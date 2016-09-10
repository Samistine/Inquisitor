package com.frdfsnlght.inquisitor;

import java.util.UUID;

/**
 *
 * @author Samuel Seidel
 */
public interface InquisitorPlayer {

    public UUID getUUID();

    @Deprecated
    public String getName();

    public default boolean isOnline() {
        org.bukkit.entity.Player bukkitPlayer = Global.getServer().getPlayer(getUUID());
        return bukkitPlayer != null && bukkitPlayer.isOnline();
    }

    public static class InquisitorPlayerImpl implements InquisitorPlayer {

        private final UUID uuid;
        private final String name;

        public InquisitorPlayerImpl(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        @Override
        public UUID getUUID() {
            return uuid;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "InquisitorPlayerImpl{" + "uuid=" + uuid + ", name=" + name + '}';
        }

    }
}
