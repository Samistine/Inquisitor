/*
 * The MIT License
 *
 * Copyright 2016 Samuel Seidel.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.frdfsnlght.inquisitor.webserver.handlers.api.stats;

import com.frdfsnlght.inquisitor.TypeMap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Samuel Seidel
 */
public class ResultSetConverter {

    private final ResultSet rs;

    /**
     * Player's name.
     *
     * @inq.SQLData varchar(30)
     */
    private final String name;

    /**
     * Player's display name.
     *
     * @inq.SQLData varchar(255)
     */
    private final String displayName;

    /**
     * Player's last client address.
     *
     * @inq.SQLData varchar(40)
     */
    private String address;

    /**
     * Player's inventory.
     *
     * <br>The JSON is an array of objects, in slot order.
     * <br>Each object describes the item in the corresponding slot.
     * <br>Each item's keys are: "<b>type"</b>, "<b>data</b>", "<b>amount</b>",
     * "<b>durability</b>", "<b>enchantments</b>", and "tag</b>".
     *
     * @inq.SQLData longtext
     */
    private JSONArray inventory;

    /**
     * Player's armor.
     *
     * <br>See {@link #inventory} for encoding details.
     *
     * @inq.SQLData longtext
     * @see #inventory
     */
    private JSONArray armor;

    /**
     * The inventory slot number of which item is being held.
     *
     * @inq.SQLData int(11)
     */
    private int heldItemSlot;

    /**
     * Player's health, from 0 to 20.
     *
     * @inq.SQLData int(11)
     */
    private int health;

    /**
     * Player's remaining air, from 0 to 300.
     *
     * @inq.SQLData int(11)
     */
    private int remainingAir;

    /**
     * Player's remaining fire ticks, from -20 to 0.
     *
     * @inq.SQLData int(11)
     */
    private int fireTicks;

    /**
     * Player's food level, from 0 to 20.
     *
     * @inq.SQLData int(11)
     */
    private int foodLevel;

    /**
     * Player's exhaustion.
     *
     * @inq.SQLData float
     */
    private float exhaustion;

    /**
     * Player's saturation.
     *
     * @inq.SQLData float
     */
    private float saturation;

    /**
     * Last game mode.
     *
     * @inq.SQLData varchar(15)
     */
    private String gameMode;

    /**
     * Experience level.
     *
     * @inq.SQLData int(11)
     */
    private int level;

    /**
     * Amount of experience gained toward next level as a percentage.
     *
     * @inq.SQLData float
     */
    private float exp;

    /**
     * Total experience points.
     *
     * @inq.SQLData int(11)
     */
    private int totalExperience;

    /**
     * Current potion effects encoded as an array of strings.
     *
     * @inq.SQLData longtext
     */
    private JSONArray potionEffects;

    /**
     * The name of the server the player was last on.
     *
     * @inq.SQLData varchar(50)
     */
    private String server;

    /**
     * The name of the world the player was last in.
     *
     * @inq.SQLData varchar(50)
     */
    private String world;

    /**
     * The coordinates where the player was last.
     *
     * <br>The individual ordinates are separated by commas.
     *
     * @inq.SQLData varchar(100)
     */
    private String coords;

    /**
     * The groups the player belongs to.
     *
     * <br>This is only available if the Vault plugin is installed and providing
     * permissions integration.
     *
     * @inq.SQLData longtext
     */
    private JSONArray groups;

    /**
     * The amount of money the player has.
     *
     * <br>This is only available if the Vault plugin is installed and providing
     * economy integration.
     *
     * @inq.SQLData double
     */
    private double money;

    /**
     * The name of the server where the player's bed is located.
     *
     * @inq.SQLData varchar(50)
     */
    private String bedServer;

    /**
     * The name of the world where the player's bed is located.
     *
     * @inq.SQLData varchar(50)
     */
    private String bedWorld;

    /**
     * The coordinates of the player's bed.
     *
     * <br>The individual ordinates are separated by commas.
     *
     * @inq.SQLData varchar(100)
     */
    private String bedCoords;

    /**
     * Number of times the player has joined.
     *
     * @inq.SQLData int(11)
     */
    private int joins;

    /**
     * The date and time of the player's first join.
     *
     * @inq.SQLData timestamp
     */
    private Timestamp firstJoin;

    /**
     * The date and time of the player's last join.
     *
     * @inq.SQLData timestamp
     */
    private Timestamp lastJoin;

    /**
     * Number of times the player has quit.
     *
     * @inq.SQLData int(11)
     */
    private int quits;

    /**
     * The date and time of the player's last quit.
     *
     * @inq.SQLData timestamp
     */
    private Timestamp lastQuit;

    /**
     * Number of times the player has been kicked.
     *
     * @inq.SQLData int(11)
     */
    private int kicks;

    /**
     * The date and time of the last kick.
     *
     * @inq.SQLData timestamp
     */
    private Timestamp lastKick;

    /**
     * The message sent to the player during the last kick.
     *
     * @inq.SQLData varchar(255)
     */
    private String lastKickMessage;

    /**
     * Number of times the player has died.
     *
     * @inq.SQLData int(11)
     */
    private int deaths;

    /**
     * The causes of the player's deaths.
     *
     * <br>This is a JSON object where each key is a reason and the
     * corresponding value is a count.
     *
     * @see #mapped
     */
    private JSONObject deathCauses;

    /**
     * The date and time the player last died.
     *
     * @inq.SQLData timestamp
     */
    private Timestamp lastDeath;

    /**
     * The message sent to the player when they last died.
     *
     * @inq.SQLData varchar(255)
     */
    private String lastDeathMessage;

    /**
     * The number of other players killed by this player. (this field was
     * renamed from "playerKills" in v2.0)
     *
     * @inq.SQLData
     */
    private int totalPlayersKilled;

    /**
     * The other players killed.
     *
     * <br>This is a JSON object where each key is a player name and the
     * corresponding value is a count.
     *
     * @see #mapped
     */
    private JSONObject playersKilled;

    /**
     * The weapons used to kill other players.
     *
     * <br>This is a JSON object where each key is a weapon name and the
     * corresponding value is a count.
     *
     * @see #mapped
     */
    private JSONObject playersKilledByWeapon;

    /**
     * The date and time when the last player killed by this player was killed.
     *
     * @inq.SQLData timestamp
     */
    private Timestamp lastPlayerKill;

    /**
     * The name of the last player killed by this player.
     *
     * @inq.SQLData
     */
    private String lastPlayerKilled;

    /**
     * The number of mobs killed by this player. (this field was renamed from
     * "mobKills" in v2.0)
     *
     * @inq.SQLData
     */
    private int totalMobsKilled;

    /**
     * The mobs killed.
     *
     * <br>This is a JSON object where each key is a mob name and the
     * corresponding value is a count.
     *
     * @see #mapped
     */
    private JSONObject mobsKilled;

    /**
     * The weapons used to kill mobs.
     *
     * <br>This is a JSON object where each key is a weapon name and the
     * corresponding value is a count.
     *
     * @see #mapped
     */
    private JSONObject mobsKilledByWeapon;

    /**
     * The date and time when the last mob killed by this player was killed.
     *
     * @inq.SQLData timestamp
     */
    private Timestamp lastMobKill;

    /**
     * The name of the last creature killed by this player.
     *
     * @inq.SQLData varchar(30)
     */
    private String lastMobKilled;

    /**
     * @inq.SQLData
     */
    private int totalBlocksBroken;

    /**
     * @see #mapped
     */
    private JSONObject blocksBroken;

    /**
     * @inq.SQLData
     */
    private int totalBlocksPlaced;

    /**
     * @see #mapped
     */
    private JSONObject blocksPlaced;

    /**
     * @see #mapped
     */
    private JSONObject animalsTamed;

    /**
     * The total distance traveled.
     *
     * @inq.SQLData
     */
    private float totalDistanceTraveled;

    /**
     * The distances traveled by different means.
     *
     * <br>This is a JSON object where each key is a travel method and the
     * corresponding value is the number of meters traveled.
     *
     * @see #mapped
     */
    private JSONObject travelDistances;

    /**
     * The distances traveled in each type of biome.
     *
     * <br \>This is a JSON object where each key is a biome name and the
     * corresponding value is the number of meters traveled.
     *
     * @see #mapped
     */
    private JSONObject biomeDistances;

    /**
     * The amount of time traveled by different means.
     *
     * <br \>This is a JSON object where each key is a travel method and the
     * corresponding value is the number of seconds traveled.
     *
     * @see #mapped
     */
    private JSONObject travelTimes;

    /**
     * The amount of time traveled in each type of biome.
     *
     * <br \>This is a JSON object where each key is a biome name and the
     * corresponding value is the number of seconds traveled.
     *
     * @see #mapped
     */
    private JSONObject biomeTimes;

    /**
     * The total number of items dropped.
     *
     * @inq.SQLData int(11)
     */
    private int totalItemsDropped;

    /**
     * This is a JSON object where each key is a material name and the
     * corresponding value is a count.
     *
     * @see #mapped
     */
    private JSONObject itemsDropped;

    /**
     * The total number of items picked up.
     *
     * @inq.SQLData
     */
    private int totalItemsPickedUp;

    /**
     * This is a JSON object where each key is a material name and the
     * corresponding value is a count.
     *
     * @see #mapped
     */
    private JSONObject itemsPickedUp;

    /**
     * The total number of items crafted.
     *
     * @inq.SQLData int(11)
     */
    private int totalItemsCrafted;

    /**
     * This is a JSON object where each key is a material name and the
     * corresponding value is a count.
     *
     * @see #mapped
     */
    private JSONObject itemsCrafted;

    /**
     * This is a JSON object where each key is an egg type and the corresponding
     * value is a count.
     *
     * @see #mapped
     */
    private JSONObject eggsThrown;

    /**
     * This is a JSON object where each key is an item name and the
     * corresponding value is a count.
     *
     * @see #mapped
     */
    private JSONObject foodEaten;

    /**
     * Number of times the player has slept in a bed.
     *
     * @inq.SQLData int(11)
     */
    private int timesSlept;

    /**
     * Number of arrows shot from a bow.
     *
     * @inq.SQLData int(11)
     */
    private int arrowsShot;

    /**
     * Number of fires started with flint and steel.
     *
     * @inq.SQLData int(11)
     */
    private int firesStarted;

    /**
     * Number of fish caught.
     *
     * @inq.SQLData int(11)
     */
    private int fishCaught;

    /**
     * Number of chat messages sent.
     *
     * @inq.SQLData int(11)
     */
    private int chatMessages;

    /**
     * Number of nether portals crossed.
     *
     * @inq.SQLData int(11)
     */
    private int portalsCrossed;

    /**
     * Number of buckets filled with water.
     *
     * @inq.SQLData int(11)
     */
    private int waterBucketsFilled;

    /**
     * Number of water buckets emptied.
     *
     * @inq.SQLData int(11)
     */
    private int waterBucketsEmptied;

    /**
     * Number of buckets filled with lava.
     *
     * @inq.SQLData int(11)
     */
    private int lavaBucketsFilled;

    /**
     * Number of lava buckets emptied.
     *
     * @inq.SQLData int(11)
     */
    private int lavaBucketsEmptied;

    /**
     * Number of cows milked.
     *
     * @inq.SQLData int(11)
     */
    private int cowsMilked;

    /**
     * Number of mooshrooms milked.
     *
     * @inq.SQLData int(11)
     */
    private int mooshroomsMilked;

    /**
     * Number of mooshrooms sheared.
     *
     * @inq.SQLData int(11)
     */
    private int mooshroomsSheared;

    /**
     * Number of sheep sheared.
     *
     * @inq.SQLData int(11)
     */
    private int sheepSheared;

    /**
     * Number of sheep dyed.
     *
     * @inq.SQLData int(11)
     */
    private int sheepDyed;

    /**
     * Amount of experience accumulated over the player's life.
     *
     * @inq.SQLData int(11)
     */
    private int lifetimeExperience;

    /**
     * Number of items enchanted.
     *
     * @inq.SQLData int(11)
     */
    private int itemsEnchanted;

    /**
     * Number of levels spent enchanting items.
     *
     * @inq.SQLData int(11)
     */
    private int itemEnchantmentLevels;

    /**
     * The number of seconds the player's has been logged in, or was logged in
     * for.
     *
     * @inq.SQLData float
     */
    private float sessionTime;

    /**
     * The total number of seconds the player has played for.
     *
     * @inq.SQLData float
     */
    private float totalTime;

    /**
     * The date and time of the last update to the record.
     *
     * @inq.SQLData
     */
    private Timestamp lastUpdate;

    /**
     * A boolean indicating whether or not the player is currently online.
     *
     * @inq.SQLData tinyint(1)
     */
    private int online;

    /**
     * This JSONObject, encoded as a string in the database, contains the values
     * for a lot of other variables defined above
     *
     * @inq.SQLData longtext?
     */
    private JSONObject mapped;

    public ResultSetConverter(ResultSet rs) throws SQLException, ParseException {
        this.rs = rs;
        name = rs.getString("name");
        displayName = rs.getString("displayName");
        address = rs.getString("address");

        Object o = new JSONParser().parse(rs.getString("inventory"));
        if (o instanceof JSONArray) {
            inventory = (JSONArray) o;
        } else {
            inventory = new JSONArray();
        }

        Object o2 = new JSONParser().parse(rs.getString("armor"));
        if (o2 instanceof JSONArray) {
            armor = (JSONArray) o2;
        } else {
            armor = new JSONArray();
        }

        heldItemSlot = rs.getInt("heldItemSlot");
        health = rs.getInt("health");
        remainingAir = rs.getInt("remainingAir");
        fireTicks = rs.getInt("fireTicks");
        foodLevel = rs.getInt("foodLevel");
        exhaustion = rs.getFloat("exhaustion");
        saturation = rs.getFloat("saturation");
        gameMode = rs.getString("gameMode");
        level = rs.getInt("level");
        exp = rs.getFloat("exp");
        totalExperience = rs.getInt("totalExperience");

        Object o3 = new JSONParser().parse(rs.getString("potionEffects"));
        if (o3 instanceof JSONArray) {
            potionEffects = (JSONArray) o3;
        } else {
            potionEffects = new JSONArray();
        }

        server = rs.getString("server");
        world = rs.getString("world");
        coords = rs.getString("coords");

        Object o4 = new JSONParser().parse(rs.getString("groups"));
        if (o4 instanceof JSONArray) {
            groups = (JSONArray) o4;
        } else {
            groups = new JSONArray();
        }

        money = rs.getDouble("money");
        bedServer = rs.getString("bedServer");
        bedWorld = rs.getString("bedWorld");
        bedCoords = rs.getString("bedCoords");
        joins = rs.getInt("joins");
        firstJoin = rs.getTimestamp("firstJoin");
        lastJoin = rs.getTimestamp("lastJoin");
        quits = rs.getInt("quits");
        lastQuit = rs.getTimestamp("lastQuit");
        kicks = rs.getInt("kicks");
        lastKick = rs.getTimestamp("lastKick");
        lastKickMessage = rs.getString("lastKickMessage");
        deaths = rs.getInt("deaths");

        Object o5 = new JSONParser().parse(rs.getString("mapped"));
        if (o5 instanceof JSONObject) {
            mapped = (JSONObject) o5;
        } else {
            mapped = null;
        }

        deathCauses = getJSONObjectFromMAPPED("deathCauses");

        lastDeathMessage = rs.getString("lastDeathMessage");
        totalPlayersKilled = rs.getInt("totalPlayersKilled");
        playersKilled = getJSONObjectFromMAPPED("playersKilled");
        playersKilledByWeapon = getJSONObjectFromMAPPED("playersKilledByWeapon");
        lastPlayerKill = rs.getTimestamp("lastPlayerKill");
        lastPlayerKilled = rs.getString("lastPlayerKilled");
        totalMobsKilled = rs.getInt("totalMobsKilled");
        mobsKilled = getJSONObjectFromMAPPED("mobsKilled");
        mobsKilledByWeapon = getJSONObjectFromMAPPED("mobsKilledByWeapon");
        lastMobKill = rs.getTimestamp("lastMobKill");
        lastMobKilled = rs.getString("lastMobKilled");
        totalBlocksBroken = rs.getInt("totalBlocksBroken");
        blocksBroken = getJSONObjectFromMAPPED("blocksBroken");
        totalBlocksPlaced = rs.getInt("totalBlocksPlaced");
        blocksPlaced = getJSONObjectFromMAPPED("blocksPlaced");
        animalsTamed = getJSONObjectFromMAPPED("animalsTamed");
        totalDistanceTraveled = rs.getFloat("totalDistanceTraveled");
        travelDistances = getJSONObjectFromMAPPED("travelDistances");
        biomeDistances = getJSONObjectFromMAPPED("biomeDistances");
        travelTimes = getJSONObjectFromMAPPED("travelTimes");
        biomeTimes = getJSONObjectFromMAPPED("biomeTimes");
        totalItemsDropped = rs.getInt("totalItemsDropped");
        itemsDropped = getJSONObjectFromMAPPED("itemsDropped");
        totalItemsPickedUp = rs.getInt("totalItemsPickedUp");
        itemsPickedUp = getJSONObjectFromMAPPED("itemsPickedUp");
        totalItemsCrafted = rs.getInt("totalItemsCrafted");
        itemsCrafted = getJSONObjectFromMAPPED("itemsCrafted");
        eggsThrown = getJSONObjectFromMAPPED("eggsThrown");
        foodEaten = getJSONObjectFromMAPPED("foodEaten");
        timesSlept = rs.getInt("timesSlept");
        arrowsShot = rs.getInt("arrowsShot");
        firesStarted = rs.getInt("firesStarted");
        fishCaught = rs.getInt("fishCaught");
        chatMessages = rs.getInt("chatMessages");
        portalsCrossed = rs.getInt("portalsCrossed");
        waterBucketsFilled = rs.getInt("waterBucketsFilled");
        waterBucketsEmptied = rs.getInt("waterBucketsEmptied");
        lavaBucketsFilled = rs.getInt("lavaBucketsFilled");
        lavaBucketsEmptied = rs.getInt("lavaBucketsEmptied");
        cowsMilked = rs.getInt("cowsMilked");
        mooshroomsMilked = rs.getInt("mooshroomsMilked");
        mooshroomsSheared = rs.getInt("mooshroomsSheared");
        sheepSheared = rs.getInt("sheepSheared");
        sheepDyed = rs.getInt("sheepDyed");
        lifetimeExperience = rs.getInt("lifetimeExperience");
        itemsEnchanted = rs.getInt("itemsEnchanted");
        itemEnchantmentLevels = rs.getInt("itemEnchantmentLevels");
        sessionTime = rs.getInt("sessionTime");
        lastUpdate = rs.getTimestamp("lastUpdate");
        online = rs.getInt("online");
    }

    public final JSONObject getJSONObjectFromMAPPED(String key) {
        if (mapped != null) {
            Object returned = mapped.get(key);
            if (returned instanceof JSONObject) {
                return (JSONObject) returned;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public TypeMap getAllStats() throws SQLException {
        TypeMap map = new TypeMap();

        map.set("name", name);
        map.set("displayName", displayName);
        map.set("address", address);
        map.set("inventory", inventory);
        map.set("armor", armor);
        map.set("heldItemSlot", heldItemSlot);
        map.set("health", health);
        map.set("remainingAir", remainingAir);
        map.set("fireTicks", fireTicks);
        map.set("foodLevel", foodLevel);
        map.set("exp", exp);
        map.set("totalExperience", totalExperience);
        map.set("potionEffects", potionEffects);
        map.set("server", server);
        map.set("world", world);
        map.set("coords", coords);
        map.set("groups", groups);
        map.set("money", money);
        map.set("bedServer", bedServer);
        map.set("bedWorld", bedWorld);
        map.set("bedCoords", bedCoords);
        map.set("joins", joins);
        map.set("firstJoin", timestampToString(firstJoin));
        map.set("lastJoin", timestampToString(lastJoin));
        map.set("quits", quits);
        map.set("lastQuit", timestampToString(lastQuit));
        map.set("kicks", kicks);
        map.set("lastKick", timestampToString(lastKick));
        map.set("lastKickMessage", lastKickMessage);
        map.set("deaths", deaths);
        map.set("deathCauses", deathCauses);
        map.set("lastDeath", timestampToString(lastDeath));
        map.set("lastDeathMessage", lastDeathMessage);
        map.set("totalPlayersKilled", totalPlayersKilled);
        map.set("playersKilled", playersKilled);
        map.set("playersKilledByWeapon", playersKilledByWeapon);
        map.set("lastPlayerKill", timestampToString(lastPlayerKill));
        map.set("lastPlayerKilled", lastPlayerKilled);
        map.set("totalMobsKilled", totalMobsKilled);
        map.set("mobsKilled", mobsKilled);
        map.set("mobsKilledByWeapon", mobsKilledByWeapon);
        map.set("lastMobKill", timestampToString(lastMobKill));
        map.set("lastMobKilled", lastMobKilled);
        map.set("totalBlocksBroken", totalBlocksBroken);
        map.set("blocksBroken", blocksBroken);
        map.set("totalBlocksPlaced", totalBlocksPlaced);
        map.set("blocksPlaced", blocksPlaced);
        map.set("animalsTamed", animalsTamed);
        map.set("totalDistanceTraveled", totalDistanceTraveled);
        map.set("travelDistances", travelDistances);
        map.set("biomeDistances", biomeDistances);
        map.set("travelTimes", travelTimes);
        map.set("biomeTimes", biomeTimes);
        map.set("totalItemsDropped", totalItemsDropped);
        map.set("itemsDropped", itemsDropped);
        map.set("totalItemsPickedUp", totalItemsPickedUp);
        map.set("itemsPickedUp", itemsPickedUp);
        map.set("totalItemsCrafted", totalItemsCrafted);
        map.set("itemsCrafted", itemsCrafted);
        map.set("eggsThrown", eggsThrown);
        map.set("foodEaten", foodEaten);
        map.set("timesSlept", timesSlept);
        map.set("arrowsShot", arrowsShot);
        map.set("firesStarted", firesStarted);
        map.set("fishCaught", fishCaught);
        map.set("chatMessages", chatMessages);
        map.set("portalsCrossed", portalsCrossed);
        map.set("waterBucketsFilled", waterBucketsFilled);
        map.set("waterBucketsEmptied", waterBucketsEmptied);
        map.set("lavaBucketsFilled", lavaBucketsFilled);
        map.set("lavaBucketsEmptied", lavaBucketsEmptied);
        map.set("cowsMilked", cowsMilked);
        map.set("mooshroomsMilked", mooshroomsMilked);
        map.set("mooshroomsSheared", mooshroomsSheared);
        map.set("sheepSheared", sheepSheared);
        map.set("sheepDyed", sheepDyed);
        map.set("lifetimeExperience", lifetimeExperience);
        map.set("itemsEnchanted", itemsEnchanted);
        map.set("itemEnchantmentLevels", itemEnchantmentLevels);
        map.set("sessionTime", sessionTime);
        map.set("totalTime", totalTime);
        map.set("lastUpdate", timestampToString(lastUpdate));
        map.set("online", online);

        return map;
    }

    private String timestampToString(Timestamp timestamp) {
        return String.valueOf(timestamp);
    }

}
