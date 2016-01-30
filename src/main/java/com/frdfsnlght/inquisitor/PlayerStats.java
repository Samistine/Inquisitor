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
 * distributed under the License is distributed on an "AS IS", BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frdfsnlght.inquisitor;

import com.frdfsnlght.inquisitor.exceptions.OptionsException;
import com.frdfsnlght.inquisitor.exceptions.PermissionsException;
import com.frdfsnlght.inquisitor.Statistic.Type;
import com.frdfsnlght.inquisitor.StatisticsGroup.BeforeFlushListener;
import com.frdfsnlght.inquisitor.StatisticsManager.StatisticsManagerListener;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class PlayerStats {

    private static final Set<String> OPTIONS = new HashSet<String>();
    private static final Set<String> RESTART_OPTIONS = new HashSet<String>();
    private static final Options options;

    private static final Set<PlayerStatsListener> listeners = new HashSet<PlayerStatsListener>();

    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("#.###", new DecimalFormatSymbols(Locale.US));

    public static final StatisticsGroup group = new StatisticsGroup("players", "name", Type.STRING, 30);

    protected static final Set<String> bedOwners = Collections.synchronizedSet(new HashSet<String>());
    private static final Set<String> ignoredPlayerJoins = new HashSet<String>();
    private static final Set<String> kickedPlayers = new HashSet<String>();
    
    protected static final Map<String, PlayerState> playerStates = Collections.synchronizedMap(new HashMap<String, PlayerState>());

    private static final Map<Integer, String> playerLoginTasks = Collections.synchronizedMap(new HashMap<Integer, String>());
    private static boolean started = false;
    private static int bedCheckTask = -1;

    private static boolean invalidPlayerNamePatternSet = true;
    private static Pattern invalidPlayerNamePattern = null;

    static {
        OPTIONS.add("flushInterval");
        OPTIONS.add("bedCheckInterval");
        OPTIONS.add("deleteAge");
        OPTIONS.add("invalidPlayerNamePattern");

        RESTART_OPTIONS.add("flushInterval");
        RESTART_OPTIONS.add("bedCheckInterval");

        options = new Options(PlayerStats.class, OPTIONS, "inq.players",
                new OptionsListener() {
                    public void onOptionSet(Context ctx, String name, String value) {
                        ctx.sendLog("player stats option '%s' set to '%s'", name, value);
                        if (RESTART_OPTIONS.contains(name)) {
                            Config.save(ctx);
                            stop();
                            start();
                        }
                    }

                    public String getOptionPermission(Context ctx, String name) {
                        return name;
                    }
                });

        group.addStatistic(Statistic.displayName);
        group.addStatistic(Statistic.uuid);

        group.addStatistic(Statistic.address);
        group.addStatistic(Statistic.inventory);
        group.addStatistic(Statistic.armor);
        group.addStatistic(Statistic.ender);
        group.addStatistic(Statistic.heldItemSlot);
        group.addStatistic(Statistic.health);
        group.addStatistic(Statistic.remainingAir);
        group.addStatistic(Statistic.fireTicks);
        group.addStatistic(Statistic.foodLevel);
        group.addStatistic(Statistic.exhaustion);
        group.addStatistic(Statistic.saturation);
        group.addStatistic(Statistic.gameMode);
        group.addStatistic(Statistic.level);
        group.addStatistic(Statistic.exp);
        group.addStatistic(Statistic.totalExperience);
        group.addStatistic(Statistic.potionEffects);
        group.addStatistic(Statistic.online);

        group.addStatistic(Statistic.server);
        group.addStatistic(Statistic.world);
        group.addStatistic(Statistic.coords);

        group.addStatistic(Statistic.groups);
        group.addStatistic(Statistic.money);

        group.addStatistic(Statistic.bedServer);
        group.addStatistic(Statistic.bedWorld);
        group.addStatistic(Statistic.bedCoords);

        group.addStatistic(Statistic.joins);
        group.addStatistic(Statistic.firstJoin);
        group.addStatistic(Statistic.lastJoin);
        group.addStatistic(Statistic.quits);
        group.addStatistic(Statistic.lastQuit);
        group.addStatistic(Statistic.kicks);
        group.addStatistic(Statistic.lastKick);
        group.addStatistic(Statistic.lastKickMessage);
        group.addStatistic(Statistic.deaths);
        group.addStatistic(Statistic.deathCauses);
        group.addStatistic(Statistic.lastDeath);
        group.addStatistic(Statistic.lastDeathMessage);
        group.addStatistic(Statistic.totalPlayersKilled);
        group.addStatistic(Statistic.playersKilled);
        group.addStatistic(Statistic.playersKilledByWeapon);

        group.addStatistic(Statistic.lastPlayerKill);
        group.addStatistic(Statistic.lastPlayerKilled);
        group.addStatistic(Statistic.totalMobsKilled);
        group.addStatistic(Statistic.mobsKilled);
        group.addStatistic(Statistic.mobsKilledByWeapon);
        group.addStatistic(Statistic.lastMobKill);
        group.addStatistic(Statistic.lastMobKilled);

        group.addStatistic(Statistic.totalBlocksBroken);
        group.addStatistic(Statistic.blocksBroken);
        group.addStatistic(Statistic.totalBlocksPlaced);
        group.addStatistic(Statistic.blocksPlaced);
        group.addStatistic(Statistic.animalsTamed);
        group.addStatistic(Statistic.totalDistanceTraveled);
        group.addStatistic(Statistic.travelDistances);
        group.addStatistic(Statistic.biomeDistances);
        group.addStatistic(Statistic.travelTimes);
        group.addStatistic(Statistic.biomeTimes);
        group.addStatistic(Statistic.totalItemsDropped);
        group.addStatistic(Statistic.itemsDropped);
        group.addStatistic(Statistic.totalItemsPickedUp);
        group.addStatistic(Statistic.itemsPickedUp);
        group.addStatistic(Statistic.totalItemsCrafted);
        group.addStatistic(Statistic.itemsCrafted);
        group.addStatistic(Statistic.eggsThrown);
        group.addStatistic(Statistic.foodEaten);

        group.addStatistic(Statistic.timesSlept);
        group.addStatistic(Statistic.arrowsShot);
        group.addStatistic(Statistic.firesStarted);
        group.addStatistic(Statistic.fishCaught);
        group.addStatistic(Statistic.sheepSheared);
        group.addStatistic(Statistic.chatMessages);
        group.addStatistic(Statistic.portalsCrossed);
        group.addStatistic(Statistic.waterBucketsFilled);
        group.addStatistic(Statistic.waterBucketsEmptied);
        group.addStatistic(Statistic.lavaBucketsFilled);
        group.addStatistic(Statistic.lavaBucketsEmptied);
        group.addStatistic(Statistic.cowsMilked);
        group.addStatistic(Statistic.mooshroomsMilked);
        group.addStatistic(Statistic.mooshroomsSheared);
        group.addStatistic(Statistic.sheepDyed);
        group.addStatistic(Statistic.lifetimeExperience);
        group.addStatistic(Statistic.itemsEnchanted);
        group.addStatistic(Statistic.itemEnchantmentLevels);

        group.addStatistic(Statistic.sessionTime);
        group.addStatistic(Statistic.totalTime);

        group.addListener(new BeforeFlushListener() {
            public void beforeFlush(Statistics stats) {
                savePlayerInfo(stats);
            }
        });

        StatisticsManager.addGroup(group);

        StatisticsManager.addListener(new StatisticsManagerListener() {
            public void onStatisticsManagerStarted() {
                start();
            }

            public void onStatisticsManagerStopping() {
                stop();
            }
        });

    }

    public static void init() {
    }

    public static void addListener(PlayerStatsListener listener) {
        listeners.add(listener);
    }

    public static boolean isStarted() {
        return started;
    }

    public static void start() {
        if (started) {
            return;
        }
        try {
            if (!StatisticsManager.isStarted()) {
                throw new Exception("statistics manager is not started");
            }

            group.setFlushInterval(Config.getIntDirect("players.flushInterval",
                    (int) group.getFlushInterval()));
            group.setDeleteAge(Config.getIntDirect("players.deleteAge",
                    (int) group.getDeleteAge()));

            initializeBedOwners();
            scheduleBedCheck();
            initializeOnline();

            started = true;
            Utils.info("player stats collection started");

            for (PlayerStatsListener listener : listeners) {
                listener.onPlayerStatsStarted();
            }

        } catch (Exception e) {
            Utils.warning("player stats collection cannot be started: %s", e.getMessage());
        }
    }

    public static void stop() {
        if (!started) {
            return;
        }
        for (PlayerStatsListener listener : listeners) {
            listener.onPlayerStatsStopping();
        }
        if (bedCheckTask != -1) {
            Global.plugin.getServer().getScheduler().cancelTask(bedCheckTask);
        }
        bedCheckTask = -1;
        started = false;
        bedOwners.clear();
        ignoredPlayerJoins.clear();
        kickedPlayers.clear();
        playerStates.clear();
        Utils.info("player stats collection stopped");
    }

    public static boolean isStatsPlayer(Player player) {

        if (player.getGameMode() == null) {
            return false;
        }
        if (!invalidPlayerNamePatternSet) {
            return true;
        }
        if (invalidPlayerNamePattern == null) {
            String pat = getInvalidPlayerNamePattern();
            if (pat == null) {
                invalidPlayerNamePatternSet = false;
                return true;
            }
            try {
                invalidPlayerNamePattern = Pattern.compile(pat);
            } catch (PatternSyntaxException e) {
                Utils.severe("invalid regular expression for invalidPlayerNamePattern");
                invalidPlayerNamePatternSet = false;
            }
        }
        return !invalidPlayerNamePattern.matcher(player.getName()).matches();
    }

    public static void onPlayerJoin(final Player player) {
        String puuidst = player.getUniqueId().toString();
        String pname = player.getName();
        Date date = new Date();
        String servername = Global.plugin.getServer().getServerName();
        if (ignoredPlayerJoins.contains(pname)) {
            Utils.debug("ignored join for player '%s'", pname);
            return;
        }
        Utils.debug("onPlayerJoin '%s'", pname);

        BukkitRunnable onJoin = new PlayerJoinRunnable(puuidst, pname, date, servername);
        playerLoginTasks.put(onJoin.runTaskAsynchronously(Global.plugin).getTaskId(), puuidst);

    }

    /**
     *
     * @param uuid
     * @return false if the player is still logging in
     */
    public static boolean hasNoPendingLogin(UUID uuid) {
        BukkitScheduler scheduler = Global.plugin.getServer().getScheduler();
        synchronized (playerLoginTasks) {
            Iterator iterator = playerLoginTasks.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, String> entry = (Map.Entry) iterator.next();
                if (scheduler.isQueued(entry.getKey()) || scheduler.isCurrentlyRunning(entry.getKey())) { //This task is still running
                    if (entry.getValue().equals(uuid.toString())) { //Check if its the task for our player
                        Global.plugin.getLogger().severe("ATTEMPTED TO GET/MODIFY STATS WHILE THE PLAYER's STATS WERE BEING LOADED");
                        //System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
                        return false;
                    }
                } else {
                    iterator.remove();
                }
            }
        }
        return true;
    }

    /*    public static void onPlayerJoin(Player player) {
     if (ignoredPlayerJoins.contains(player.getName())) {
     Utils.debug("ignored join for player '%s'", player.getName());
     return;
     }

     Utils.debug("onPlayerJoin '%s'", player.getName());
     if (! isStatsPlayer(player)) return;

     final Statistics stats = group.getStatistics(player.getName());
     stats.incr("joins");
     stats.set("lastJoin", new Date());
     stats.set("sessionTime", 0);
     stats.set("online", true);
     if (! stats.isInDB())
     stats.set("firstJoin", new Date());
     String bedServer = stats.getString("bedServer");
     if ((bedServer != null) && bedServer.equals(Global.plugin.getServer().getServerName()))
     bedOwners.add(player.getName());
     playerStates.put(player.getName(), new PlayerState(stats.getFloat("totalTime")));
     Global.plugin.getServer().getScheduler().runTaskAsynchronously(Global.plugin, new Runnable() {
     public void run() {
     stats.flushSync();
     }
     });
     }*/

    /*	public static void onPlayerQuit(final Player player) {
     Global.plugin.getServer().getScheduler().runTaskAsynchronously(Global.plugin, new Runnable() {
			
     String pname = player.getName();
     Date date = new Date();
			
     public void run() {
     if (!isStatsPlayer(player))
     return;
     if (ignoredPlayerJoins.remove(pname)) {
     Utils.debug("ignored quit for player '%s'", pname);
     return;
     }
     Utils.debug("onPlayerQuit '%s'", pname);

     try {
     Statistics stats = group.getStatistics(pname);
     if (!kickedPlayers.remove(pname)) {
     stats.incr("quits");
     stats.set("lastQuit", date);
     }
     stats.set("online", false);
     stats.flushSync();

     group.removeStatistics(pname);
     playerStates.remove(pname);
     } catch (Exception ex) {
     Utils.severe("OnPlayerQuit Exception message: " + ex.getMessage());
     StringWriter sw = new StringWriter();
     ex.printStackTrace(new PrintWriter(sw));
     Utils.severe("Stack Trace: " + sw.toString());
     }
     }
     });
     }*/
    public static void onPlayerQuit(Player player) {
        Utils.debug("onPlayerQuit '%s'", player.getName());
        if (ignoredPlayerJoins.remove(player.getName())) {
            Utils.debug("ignored quit for player '%s'", player.getName());
            return;
        }
        final Statistics stats = group.getStatistics(player.getName());

        try {
            String traveling = stats.getString("biomeTimes");
            String[] split = traveling.split("\\r?\\n");
            List<Double> values = new ArrayList<Double>();
            for (String s : split) {
                if (s.contains(": ")) {
                    String[] oneMoreSplit = s.split(": ");
                    values.add(Double.parseDouble(oneMoreSplit[1]));
                }
            }
            Double totalTime = 0.00;
            for (Double value : values) {
                totalTime = totalTime + value;
            }
            //System.out.println(TimeUnit.SECONDS.toDays(totalTime.intValue()));
            if (totalTime > 1) {
                stats.set("totalTime", totalTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!kickedPlayers.remove(player.getName())) {
            stats.incr("quits");
            stats.set("lastQuit", new Date());
        }
        stats.set("online", false);
        Global.plugin.getServer().getScheduler().runTaskAsynchronously(Global.plugin, new Runnable() {
            public void run() {
                stats.flushSync();
            }
        });

        group.removeStatistics(player.getName());
        playerStates.remove(player.getName());

    }

    public static void onPlayerKick(Player player, String message) {
        if (ignoredPlayerJoins.contains(player.getName())) {
            Utils.debug("ignored kick for player '%s'", player.getName());
            return;
        }
        Utils.debug("onPlayerKick '%s'", player.getName());

        if ((message != null) && message.contains("[Redirect]")) {
            if (message.contains("[InterRealm]")) {
                Utils.debug("player '%s' is leaving the realm", player.getName());
                onPlayerQuit(player);
            } else {
                Utils.debug("ignoring kick for player '%s' due to transport to intra-realm server");
            }
            return;
        }

        Statistics stats = group.getStatistics(player.getName());
        stats.incr("kicks");
        stats.set("lastKick", new Date());
        stats.set("lastKickMessage", message);
        kickedPlayers.add(player.getName());
    }

    public static void onPlayerDeath(Player player, String message, EntityDamageEvent.DamageCause cause) {
        Utils.debug("onPlayerDeath '%s'", player.getName());

        final Statistics stats = group.getStatistics(player.getName());
        stats.incr("deaths");
        stats.set("lastDeath", new Date());
        stats.set("lastDeathMessage", message);
        stats.incr("deathCauses", Utils.titleCase(cause.name()));
        Global.plugin.getServer().getScheduler().runTaskAsynchronously(Global.plugin, new Runnable() {
            public void run() {
                stats.flushSync();
            }
        });

        onPlayerMove(player, player.getLocation());
        PlayerState state = playerStates.get(player.getName());
        if (state != null) {
            state.reset();
        }
    }

    public static void onPlayerMove(Player player, Location to) {
        // Utils.debug("onPlayerMove '%s'", player.getName());

        Statistics stats = group.getStatistics(player.getName());
        PlayerState state = playerStates.get(player.getName());
        if (state == null) {
            return;
        }
        double distance = 0;
        if (state.lastLocation != null) {
            double dx = state.lastLocation.getBlockX() - to.getBlockX();
            double dy = state.lastLocation.getBlockY() - to.getBlockY();
            double dz = state.lastLocation.getBlockZ() - to.getBlockZ();
            distance = Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
        }
        state.lastLocation = to;

        float elapsed = 0; // fractional seconds
        if (state.lastTime != 0) {
            elapsed = (float) (System.currentTimeMillis() - state.lastTime) / 1000.0f;
        }
        state.lastTime = System.currentTimeMillis();

        Block block = to.getBlock();
        TravelMode newMode = TravelMode.WALKING;
        if (player.isFlying()) {
            newMode = TravelMode.FLYING;
        } else if (player.isInsideVehicle()) {
            Entity vehicle = player.getVehicle();
            if (vehicle == null) {
                newMode = TravelMode.RIDING;
            } else {
                switch (vehicle.getType()) {
                    case MINECART:
                        newMode = TravelMode.RIDING_MINECART;
                        break;
                    case PIG:
                        newMode = TravelMode.RIDING_PIG;
                        break;
                    case BOAT:
                        newMode = TravelMode.RIDING_BOAT;
                        break;
                    case HORSE:
                        newMode = TravelMode.RIDING_HORSE;
                        break;
                    default:
                        newMode = TravelMode.RIDING;
                }
            }
        } else if ((block.getType() == Material.WATER) || (block.getType() == Material.STATIONARY_WATER)) {
            newMode = TravelMode.SWIMMING;
        } else if (player.isSprinting()) {
            newMode = TravelMode.SPRINTING;
        } else if (player.isSneaking()) {
            newMode = TravelMode.SNEAKING;
        }

        String newModeName = Utils.titleCase(newMode.name());
        if (state.lastMode == newMode) {
            stats.add("travelDistances", newModeName, distance);
            stats.add("totalDistanceTraveled", distance);
            if (distance >= 1) {
                stats.add("travelTimes", newModeName, elapsed);
            }
        } else {
            state.lastMode = newMode;
        }

//		if (block.getY() < 255 && block.getLightFromSky() < 6) {
        if (block.getY() < 65 && isInside(block.getLocation(), 2)) {
            if (state.lastBiome == null) {
                stats.add("biomeDistances", "Underground", distance);
                stats.add("biomeTimes", "Underground", elapsed);
            } else {
                state.lastBiome = null;
            }
        } else {
            if (state.lastBiome == block.getBiome()) {
                String biomeName = Utils.titleCase(state.lastBiome.name());
                stats.add("biomeDistances", biomeName, distance);
                stats.add("biomeTimes", biomeName, elapsed);
            } else {
                state.lastBiome = block.getBiome();
            }
        }
    }

    public static boolean isInside(Location loc, int maxSpacesBetweenHeadandCeiling) {
        loc.add(0, 1, 0);
        maxSpacesBetweenHeadandCeiling++;
        for (int x = 1; x < maxSpacesBetweenHeadandCeiling; x++) {
            Location newLoc = loc.clone();
            Location finalLoc = newLoc.add(0, x, 0);
            if (!finalLoc.getBlock().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static void onPlayerTeleport(Player player, Location to) {
        if (!started) {
            return;
        }
        PlayerState state = playerStates.get(player.getName());
        if (state != null) {
            onPlayerMove(player, player.getLocation());
            state.lastLocation = to;
            state.lastTime = System.currentTimeMillis();
        }
    }

    public static void onPlayerEnterBed(Player player) {
        bedOwners.add(player.getName());
        Statistics stats = group.getStatistics(player.getName());
        stats.incr("timesSlept");
    }

    public static void checkBeds() {
        for (String name : new HashSet<String>(bedOwners)) {
            OfflinePlayer player = Global.plugin.getServer().getOfflinePlayer(name);
            if (player == null) {
                player = Global.plugin.getServer().getPlayer(name);
                if (player == null) {
                    continue;
                }
            }
            if (!player.hasPlayedBefore()) {
                continue;
            }
            if ((player.getBedSpawnLocation() == null) || (player.getBedSpawnLocation().getBlock().getType() != Material.BED_BLOCK)) {
                bedOwners.remove(name);
                Utils.debug("player '%s' no longer has a bed", name);

                Statistics stats = group.getStatistics(player.getName());
                stats.set("bedServer", null);
                stats.set("bedWorld", null);
                stats.set("bedCoords", null);
                stats.flush();
                if (!player.isOnline()) {
                    group.removeStatistics(stats);
                }
            }
        }
    }

    public static TypeMap getPlayerStats(String playerName) {
        boolean isOnline = Global.plugin.getServer().getPlayer(playerName) != null;
        Statistics stats = group.getStatistics(playerName);
        if (!isOnline) {
            group.removeStatistics(stats);
        }
        return stats.getStats();
    }

    /*
     * public static com.frdfsnlght.inquisitor.api.Location getLocation(String
     * playerName) { boolean isOnline =
     * Global.plugin.getServer().getPlayer(playerName) != null; Statistics stats
     * = group.getStatistics(playerName); String server =
     * stats.getString("server"); String world = stats.getString("world");
     * double[] coords = decodeCoords(stats.getString("coords"));
     * com.frdfsnlght.inquisitor.api.Location location = null; if ((server !=
     * null) && (world != null) && (coords != null)) location = new
     * com.frdfsnlght.inquisitor.api.Location(server, world, coords); if (!
     * isOnline) group.removeStatistics(stats); return location; }
     * 
     * public static com.frdfsnlght.inquisitor.api.Location
     * getBedLocation(String playerName) { boolean isOnline =
     * Global.plugin.getServer().getPlayer(playerName) != null; Statistics stats
     * = group.getStatistics(playerName); String server =
     * stats.getString("bedServer"); String world = stats.getString("bedWorld");
     * double[] coords = decodeCoords(stats.getString("bedCoords"));
     * com.frdfsnlght.inquisitor.api.Location location = null; if ((server !=
     * null) && (world != null) && (coords != null)) location = new
     * com.frdfsnlght.inquisitor.api.Location(server, world, coords); if (!
     * isOnline) group.removeStatistics(stats); return location; }
     */
    public static void ignorePlayerJoin(String name) {
        ignoredPlayerJoins.add(name);
        Utils.debug("will ignore future join/kick/quit for player '%s'", name);
    }

    /* Begin options */
    public static int getFlushInterval() {
        return Config.getIntDirect("players.flushInterval", (int) group.getFlushInterval());
    }

    public static void setFlushInterval(int i) {
        if (i < 1000) {
            throw new IllegalArgumentException("flushInterval must be at least 1000");
        }
        Config.setPropertyDirect("players.flushInterval", i);
        group.setFlushInterval(i);
    }

    public static int getBedCheckInterval() {
        return Config.getIntDirect("players.bedCheckInterval", 20000);
    }

    public static void setBedCheckInterval(int i) {
        if (i < 1000) {
            throw new IllegalArgumentException("bedCheckInterval must be at least 1000");
        }
        Config.setPropertyDirect("players.bedCheckInterval", i);
    }

    public static int getDeleteAge() {
        return Config.getIntDirect("players.deleteAge", (int) group.getDeleteAge());
    }

    public static void setDeleteAge(int i) {
        if (i < 0) {
            i = -1;
        }
        Config.setPropertyDirect("players.deleteAge", i);
        group.setDeleteAge(i);
    }

    public static String getInvalidPlayerNamePattern() {
        return Config.getStringDirect("players.invalidPlayerNamePattern");
    }

    public static void setInvalidPlayerNamePattern(String s) {
        if (s != null) {
            if (s.isEmpty() || s.equals("-") || s.equals("*")) {
                s = null;
            }
        }
        Config.setPropertyDirect("players.invalidPlayerNamePattern", s);
        invalidPlayerNamePattern = null;
        invalidPlayerNamePatternSet = (s != null);
    }

    public static void getOptions(Context ctx, String name) throws OptionsException, PermissionsException {
        options.getOptions(ctx, name);
    }

    public static String getOption(Context ctx, String name) throws OptionsException, PermissionsException {
        return options.getOption(ctx, name);
    }

    public static void setOption(Context ctx, String name, String value) throws OptionsException, PermissionsException {
        options.setOption(ctx, name, value);
    }

    /* End options */
    private static void savePlayerInfo(Statistics stats) {
        String name = (String) stats.getKey();
        Player player = Global.plugin.getServer().getPlayer(name);
        if (player == null) {
            return;
        }

        stats.set("displayName", player.getDisplayName());

        stats.set("address", player.getAddress().getAddress().getHostAddress());
        stats.set("inventory", encodeItemStacks(player.getInventory().getContents()));
        stats.set("armor", encodeItemStacks(player.getInventory().getArmorContents()));
        stats.set("ender", encodeItemStacks(player.getEnderChest().getContents()));
        stats.set("heldItemSlot", player.getInventory().getHeldItemSlot());
        stats.set("health", player.getHealth());
        stats.set("remainingAir", player.getRemainingAir());
        stats.set("fireTicks", player.getFireTicks());
        stats.set("foodLevel", player.getFoodLevel());
        stats.set("exhaustion", player.getExhaustion());
        stats.set("saturation", player.getSaturation());
        stats.set("gameMode", player.getGameMode().toString());
        stats.set("level", player.getLevel());
        stats.set("exp", player.getExp());
        stats.set("totalExperience", player.getTotalExperience());
        stats.set("potionEffects", encodePotionEffects(player.getActivePotionEffects()));

        stats.set("server", Global.plugin.getServer().getServerName());
        stats.set("world", player.getWorld().getName());
        stats.set("coords", encodeCoords(player.getLocation()));

        if (Global.enabled) {
            stats.set("groups", Permissions.getGroups(player));
            stats.set("money", Economy.getBalanace(player));
        }

        if ((!DB.getShared()) || bedOwners.contains(player.getName())) {
            if ((player.getBedSpawnLocation() == null) || (player.getBedSpawnLocation().getBlock().getType() != Material.BED_BLOCK)) {
                stats.set("bedServer", null);
                stats.set("bedWorld", null);
                stats.set("bedCoords", null);
            } else {
                stats.set("bedServer", Global.plugin.getServer().getServerName());
                stats.set("bedWorld", player.getBedSpawnLocation().getWorld().getName());
                stats.set("bedCoords", encodeCoords(player.getBedSpawnLocation()));
            }
        }

        PlayerState state = playerStates.get(player.getName());
        if (state != null) {
            float sessionTime = (float) (System.currentTimeMillis() - state.joinTime) / 1000f;
            stats.set("sessionTime", sessionTime);
            stats.set("totalTime", state.totalTimeBase + sessionTime);
        }
    }

    private static Object encodeItemStacks(ItemStack[] stacks) {
        if (stacks == null) {
            return null;
        }
        List<TypeMap> stackMaps = new ArrayList<TypeMap>();
        for (int slot = 0; slot < stacks.length; slot++) {
            ItemStack stack = stacks[slot];
            if (stack == null) {
                stackMaps.add(null);
            } else {
                TypeMap stackMap = new TypeMap();
                // Code for moving to Names
                // stackMap.put("type", stack.getType().toString());
                stackMap.put("type", stack.getTypeId());
                stackMap.put("amount", stack.getAmount());
                stackMap.put("durability", stack.getDurability());
                MaterialData data = stack.getData();
                if (data != null) {
                    // stackMap.put(
                    // "data",
                    // Integer.parseInt(data.toString().replaceAll(
                    // "[^\\d]", "")));
                    stackMap.put("data", (int) data.getData());
                }
                TypeMap ench = new TypeMap();
                for (Enchantment e : stack.getEnchantments().keySet()) {
                    ench.put(e.getName(), stack.getEnchantments().get(e));
                }
                stackMap.put("enchantments", ench);
                stackMaps.add(stackMap);
            }
        }
        return stackMaps;
    }

    private static Object encodePotionEffects(Collection<PotionEffect> effects) {
        if (effects == null) {
            return null;
        }
        List<TypeMap> peMaps = new ArrayList<TypeMap>();
        for (PotionEffect effect : effects) {
            if (effect == null) {
                continue;
            }
            TypeMap peMap = new TypeMap();
            peMap.put("type", effect.getType().toString());
            peMap.put("duration", effect.getDuration());
            peMap.put("amplifier", effect.getAmplifier());
            peMaps.add(peMap);
        }
        return peMaps;
    }

    private static String encodeCoords(Location loc) {
        if (loc == null) {
            return null;
        }
        return DOUBLE_FORMAT.format(loc.getX()) + ","
                + DOUBLE_FORMAT.format(loc.getY()) + ","
                + DOUBLE_FORMAT.format(loc.getZ());
    }

    public static double[] decodeCoords(String coords) {
        if (coords == null) {
            return null;
        }
        String[] ords = coords.split(",");
        try {
            return new double[]{Double.parseDouble(ords[0]),
                Double.parseDouble(ords[1]), Double.parseDouble(ords[2])};
        } catch (Throwable t) {
            return null;
        }
    }

    private static void initializeBedOwners() {
        if (!DB.getShared()) {
            return;
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = DB.prepare("SELECT `name` FROM "
                    + DB.tableName(group.getName()) + " WHERE `bedServer`=?");
            stmt.setString(1, Global.plugin.getServer().getServerName());
            rs = stmt.executeQuery();
            while (rs.next()) {
                bedOwners.add(rs.getString("name"));
                Utils.debug("added '%s' as a bed owner", rs.getString("name"));
            }
        } catch (SQLException se) {
            Utils.warning("SQLException during bed owner initialization: %s", se.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se) {
            }
        }
    }

    private static void scheduleBedCheck() {
        if (bedCheckTask != -1) {
            Global.plugin.getServer().getScheduler().cancelTask(bedCheckTask);
        }
        bedCheckTask = -1;
        if (!DB.getShared()) {
            return;
        }

        bedCheckTask = Utils.fireDelayed(new Runnable() {
            public void run() {
                checkBeds();
                scheduleBedCheck();
            }
        }, getBedCheckInterval());
    }

    private static void initializeOnline() {
        PreparedStatement stmt = null;
        try {
            stmt = DB.prepare("UPDATE " + DB.tableName(group.getName())
                    + " SET `online`=0 WHERE `online`=1 AND `server`=?");
            stmt.setString(1, Global.plugin.getServer().getServerName());
            stmt.executeUpdate();
        } catch (SQLException se) {
            Utils.warning("SQLException during online state change: %s", se.getMessage());
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se) {
            }
        }
    }

    // Inner classes
    public static interface PlayerStatsListener {

        public void onPlayerStatsStarted();

        public void onPlayerStatsStopping();
    }

    protected static enum TravelMode {

        WALKING, SPRINTING, SNEAKING, FLYING, SWIMMING, RIDING, RIDING_MINECART, RIDING_PIG, RIDING_BOAT, RIDING_HORSE;
    }

}
