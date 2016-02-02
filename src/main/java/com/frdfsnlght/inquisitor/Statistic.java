/*
 * Copyright 2012 frdfsnlght <frdfsnlght@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frdfsnlght.inquisitor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */


public enum Statistic {
    
    displayName("displayName", Type.STRING, 255),
    uuid("uuid", Type.STRING, 36),
    //
    address("address", Type.STRING, 40),
    inventory("inventory", Type.OBJECT),
    armor("armor", Type.OBJECT),
    ender("ender", Type.OBJECT),
    heldItemSlot("heldItemSlot", Type.INTEGER),
    health("health", Type.INTEGER),
    remainingAir("remainingAir", Type.INTEGER),
    fireTicks("fireTicks", Type.INTEGER),
    foodLevel("foodLevel", Type.INTEGER),
    exhaustion("exhaustion", Type.FLOAT),
    saturation("saturation", Type.FLOAT),
    gameMode("gameMode", Type.STRING, 15),
    level("level", Type.INTEGER),
    exp("exp", Type.FLOAT),
    totalExperience("totalExperience", Type.INTEGER),
    potionEffects("potionEffects", Type.OBJECT),
    online("online", Type.BOOLEAN),
    //
    server("server", Type.STRING, 50),
    world("world", Type.STRING, 50),
    coords("coords", Type.STRING, 100),
    //
    groups("groups", Type.OBJECT),
    money("money", Type.DOUBLE),
    //
    bedServer("bedServer", Type.STRING, 50),
    bedWorld("bedWorld", Type.STRING, 50),
    bedCoords("bedCoords", Type.STRING, 100),
    //
    joins("joins", Type.INTEGER),
    firstJoin("firstJoin", Type.TIMESTAMP),
    lastJoin("lastJoin", Type.TIMESTAMP),
    quits("quits", Type.INTEGER),
    lastQuit("lastQuit", Type.TIMESTAMP),
    kicks("kicks", Type.INTEGER),
    lastKick("lastKick", Type.TIMESTAMP),
    lastKickMessage("lastKickMessage", Type.STRING, 255),
    deaths("deaths", Type.INTEGER),
    deathCauses("deathCauses", Type.INTEGER, true),
    lastDeath("lastDeath", Type.TIMESTAMP),
    lastDeathMessage("lastDeathMessage", Type.STRING, 255),
    totalPlayersKilled("totalPlayersKilled", Type.INTEGER),
    playersKilled("playersKilled", Type.INTEGER, true),
    playersKilledByWeapon("playersKilledByWeapon", Type.INTEGER, true),
    //
    lastPlayerKill("lastPlayerKill", Type.TIMESTAMP),
    lastPlayerKilled("lastPlayerKilled", Type.STRING, 30),
    totalMobsKilled("totalMobsKilled", Type.INTEGER),
    mobsKilled("mobsKilled", Type.INTEGER, true),
    mobsKilledByWeapon("mobsKilledByWeapon", Type.INTEGER, true),
    lastMobKill("lastMobKill", Type.TIMESTAMP),
    lastMobKilled("lastMobKilled", Type.STRING, 30),
    //
    totalBlocksBroken("totalBlocksBroken", Type.INTEGER),
    blocksBroken("blocksBroken", Type.INTEGER, true),
    totalBlocksPlaced("totalBlocksPlaced", Type.INTEGER),
    blocksPlaced("blocksPlaced", Type.INTEGER, true),
    animalsTamed("animalsTamed", Type.INTEGER, true),
    totalDistanceTraveled("totalDistanceTraveled", Type.FLOAT),
    travelDistances("travelDistances", Type.FLOAT, true),
    biomeDistances("biomeDistances", Type.FLOAT, true),
    travelTimes("travelTimes", Type.ELAPSED_TIME, true),
    biomeTimes("biomeTimes", Type.ELAPSED_TIME, true),
    totalItemsDropped("totalItemsDropped", Type.INTEGER),
    itemsDropped("itemsDropped", Type.INTEGER, true),
    totalItemsPickedUp("totalItemsPickedUp", Type.INTEGER),
    itemsPickedUp("itemsPickedUp", Type.INTEGER, true),
    totalItemsCrafted("totalItemsCrafted", Type.INTEGER),
    itemsCrafted("itemsCrafted", Type.INTEGER, true),
    eggsThrown("eggsThrown", Type.INTEGER, true),
    foodEaten("foodEaten", Type.INTEGER, true),
    //
    timesSlept("timesSlept", Type.INTEGER),
    arrowsShot("arrowsShot", Type.INTEGER),
    firesStarted("firesStarted", Type.INTEGER),
    fishCaught("fishCaught", Type.INTEGER),
    sheepSheared("sheepSheared", Type.INTEGER),
    chatMessages("chatMessages", Type.INTEGER),
    portalsCrossed("portalsCrossed", Type.INTEGER),
    waterBucketsFilled("waterBucketsFilled", Type.INTEGER),
    waterBucketsEmptied("waterBucketsEmptied", Type.INTEGER),
    lavaBucketsFilled("lavaBucketsFilled", Type.INTEGER),
    lavaBucketsEmptied("lavaBucketsEmptied", Type.INTEGER),
    cowsMilked("cowsMilked", Type.INTEGER),
    mooshroomsMilked("mooshroomsMilked", Type.INTEGER),
    mooshroomsSheared("mooshroomsSheared", Type.INTEGER),
    sheepDyed("sheepDyed", Type.INTEGER),
    lifetimeExperience("lifetimeExperience", Type.INTEGER),
    itemsEnchanted("itemsEnchanted", Type.INTEGER),
    itemEnchantmentLevels("itemEnchantmentLevels", Type.INTEGER),
    //
    sessionTime("sessionTime", Type.ELAPSED_TIME),
    totalTime("totalTime", Type.ELAPSED_TIME),
    
    BLAHS("blahs", Type.INTEGER, false);

    public static final String MappedObjectsColumn = "mapped";

    private final String name;
    private final Type type;
    private final boolean mapped;
    private final int size;   // for STRING
    private final String def; // for STRING

    private Statistic(String name, Type type) {
        this(name, type, false, -1, null);
    }

    private Statistic(String name, Type type, int size) {
        this(name, type, false, size, null);
    }

    private Statistic(String name, Type type, int size, String def) {
        this(name, type, false, size, def);
    }

    private Statistic(String name, Type type, boolean mapped) {
        this(name, type, mapped, -1, null);
    }

    private Statistic(String name, Type type, boolean mapped, int size, String def) {
        if (! name.matches("^\\w+$"))
            throw new IllegalArgumentException("'" + name + "' is not a valid statistic name");
        if (mapped) {
            size = -1;
            def = null;
        } else {
            if (type == Type.STRING) {
                if (size < 1) {
                    throw new IllegalArgumentException("size must be at least 1");
                }
            } else {
                size = -1;
                def = null;
            }
        }

        this.name = name;
        this.type = type;
        this.mapped = mapped;
        this.size = size;
        this.def = def;
    }

    @Override
    public String toString() {
        return "Statistic[" + name + "," + type.toString() + (mapped ? "(M)" : "") + "]";
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public boolean isMapped() {
        return mapped;
    }

    public int getSize() {
        return size;
    }

    public String getDefault() {
        return def;
    }

    public Set<String> getOldNames() {
        switch (this) {
            case mooshroomsMilked:
                return new HashSet<String>(Arrays.asList("mushroomCowsMilked"));
            case totalPlayersKilled:
                return new HashSet<String>(Arrays.asList("playerKills"));
            case totalMobsKilled:
                return new HashSet<String>(Arrays.asList("mobKills"));
            default:
                return null;
        }
    }

    public void validate(StatisticsGroup group) {
        if (group == null)
            throw new IllegalStateException(this + " group has not been set");
        if (mapped)
            group.validateColumn(MappedObjectsColumn, Type.OBJECT, -1, null, null);
        else
            group.validateColumn(name, type, size, def, getOldNames());
    }

    public static Statistic getFromName(String name) {
        for (Statistic value : values()) {
            if (value.getName().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public enum Type {

        STRING("varchar"),
        BOOLEAN("tinyint"),
        OBJECT("longtext"),
        TIMESTAMP("timestamp"),
        ELAPSED_TIME("float"),
        DISTANCE("float"),
        INTEGER("int"),
        LONG("bigint"),
        FLOAT("float"),
        DOUBLE("double");

        private String sqlType;

        Type(String sqlType) {
            this.sqlType = sqlType;
        }

        public String getSQLType() {
            return sqlType;
        }

        public String getSQLDef(int size, String def) {
            switch (this) {
                case STRING:
                    return sqlType + "(" + size + ") DEFAULT " + ((def == null) ? "NULL" : ("'" + def + "'"));
                default:
                    return getSQLDef(size);
            }
        }

        public String getSQLDef(int size) {
            switch (this) {
                case STRING:
                    return sqlType + "(" + size + ") NOT NULL";
                default:
                    return getSQLDef();
            }
        }

        public String getSQLDef() {
            switch (this) {
                case STRING:
                    throw new UnsupportedOperationException("cannot get definition of STRING without a size");
                case BOOLEAN:
                    return sqlType + "(1) DEFAULT 0";
                case OBJECT:
                    return sqlType + " DEFAULT NULL";
                case TIMESTAMP:
                    //return sqlType + " NOT NULL DEFAULT '0000-00-00 00:00:00'";
                    return sqlType + " NULL DEFAULT NULL";
                default:
                    return sqlType + " DEFAULT 0";
            }
        }

        /*
        public String getSQLType() {
            return String.format(columnDefFormat, defaultSize);
        }

        public String getColumnDef(int size) {
            if (size == -1) size = defaultSize;
            return String.format(columnDefFormat, size);
        }

        public Object getDefault() {
            return def;
        }
*/

    }

}
