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
import com.frdfsnlght.inquisitor.api.TravelMode;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class PlayerStats {

    private static final ExecutorService pool = Executors.newSingleThreadExecutor();

    private static final Set<String> OPTIONS = new HashSet<>();
    private static final Set<String> RESTART_OPTIONS = new HashSet<>();
    private static final Options options;

    private static final Set<PlayerStatsListener> listeners = new HashSet<>();

    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("#.###", new DecimalFormatSymbols(Locale.US));

    public static final StatisticsGroup group = new StatisticsGroup("players", "name", Type.STRING, 30);

    private static final Set<UUID> bedOwners = Collections.synchronizedSet(new HashSet<UUID>());
    private static final Set<String> ignoredPlayerJoins = new HashSet<>();
    private static final Set<UUID> kickedPlayers = new HashSet<>();
    private static final Map<UUID, PlayerState> playerStates = Collections.synchronizedMap(new HashMap<UUID, PlayerState>());

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

        options = new Options(PlayerStats.class, OPTIONS, "inq.players", new OptionsListener() {
            @Override
            public void onOptionSet(Context ctx, String name, String value) {
                ctx.sendLog("player stats option '%s' set to '%s'", name, value);
                if (RESTART_OPTIONS.contains(name)) {
                    Config.save(ctx);
                    stop();
                    start();
                }
            }

            @Override
            public String getOptionPermission(Context ctx, String name) {
                return name;
            }
        });

        EnumSet.range(Statistic.displayName, Statistic.totalTime).forEach(group::addStatistic);

        group.addListener(new BeforeFlushListener() {
            @Override
            public void beforeFlush(Statistics stats) {
                savePlayerInfo(stats);
            }
        });

        StatisticsManager.addGroup(group);

        StatisticsManager.addListener(new StatisticsManagerListener() {
            @Override
            public void onStatisticsManagerStarted() {
                start();
            }

            @Override
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

            group.setFlushInterval(Config.getIntDirect("players.flushInterval", (int) group.getFlushInterval()));
            group.setDeleteAge(Config.getIntDirect("players.deleteAge", (int) group.getDeleteAge()));

            initializeBedOwners();
            scheduleBedCheck();
            initializeOnline();

            started = true;
            Utils.info("player stats collection started");

            listeners.forEach(PlayerStatsListener::onPlayerStatsStarted);

        } catch (Exception e) {
            Utils.warning("player stats collection cannot be started: %s", e.getMessage());
        }
    }

    public static void stop() {
        if (!started) {
            return;
        }
        listeners.forEach(PlayerStatsListener::onPlayerStatsStopping);
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

    public static boolean isStatsPlayer(PlayerSnapshot player) {
        if (player == null) {
            return false;
        }
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

    public static void onPlayerJoin(PlayerSnapshot player) {
        String servername = Global.plugin.getServer().getServerName();
        if (ignoredPlayerJoins.contains(player.getName())) {
            Utils.debug("ignored join for player '%s'", player.getName());
            return;
        }

        Utils.debug("onPlayerJoin '%s'", player.getName());
        try {
            //Very simply update statement that will allow players to add their UUIDs to the db during the
            // conversion process, then once a name change happens it will update it based on the uuid matching.
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE ").append(DB.tableName(group.getName())).append(" SET `");
            sql.append(group.getKeyName()).append("`=?, `uuid`=? WHERE `");
            sql.append(group.getKeyName()).append("`=? OR `uuid`=?");
            PreparedStatement stmt = DB.prepare(sql.toString());
            stmt.setString(1, player.getName());
            stmt.setString(2, player.getUUID().toString());
            stmt.setString(3, player.getName());
            stmt.setString(4, player.getUUID().toString());
            stmt.execute();

            final Statistics stats = group.getStatistics(player);
            stats.set(Statistic.uuid, player.getUUID().toString());
            stats.incr(Statistic.joins);
            stats.set(Statistic.lastJoin, player.getDate());
            stats.set(Statistic.sessionTime, 0);
            stats.set(Statistic.online, true);
            if (!stats.isInDB()) {
                stats.set(Statistic.firstJoin, player.getDate());
            }
            String bedServer = stats.getString("bedServer");
            if (bedServer != null && bedServer.equals(servername)) {
                bedOwners.add(player.getUUID());
            }
            playerStates.put(player.getUUID(), new PlayerState(stats.getFloat("totalTime")));

            Utils.debug("totalTime: " + stats.getFloat("totalTime"));

            stats.flushSync();
        } catch (Exception ex) {
            Utils.severe("OnPlayerJoin Exception message: " + ex.getMessage());
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            Utils.severe("Stack Trace: " + sw.toString());
        }
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
    public static void onPlayerQuit(PlayerSnapshot player) {
        if (ignoredPlayerJoins.remove(player.getName())) {
            Utils.debug("ignored quit for player '%s'", player.getName());
            return;
        }

        Utils.debug("onPlayerQuit '%s'", player.getName());

        final Statistics stats = group.getStatistics(player);

        /*try {
            double totalTime = stats.getDouble("totalTime");
            String traveling = stats.getString("biomeTimes");
            String[] split = traveling.split("\\r?\\n");
            List<Double> values = new ArrayList<>();
            for (String s : split) {
                if (s.contains(": ")) {
                    String[] oneMoreSplit = s.split(": ");
                    values.add(Double.parseDouble(oneMoreSplit[1]));
                }
            }
            Double newTotalTime = 0.00;
            for (Double value : values) {
                newTotalTime = newTotalTime + value;
            }
            totalTime += stats.getFloat("sessionTime");
            //System.out.println(TimeUnit.SECONDS.toDays(totalTime.intValue()));
            if (totalTime < newTotalTime) {
                System.out.println("Looks like there was an issue with a player in Inquisitor. We set his play time based off biomeTimes");
                stats.set(Statistic.totalTime, newTotalTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        if (!kickedPlayers.remove(player.getUUID())) {
            stats.incr(Statistic.quits);
            stats.set(Statistic.lastQuit, player.getDate());
        }
        stats.set(Statistic.online, false);
        stats.flushSync();

        group.removeStatistics(player.getName());
        playerStates.remove(player.getUUID());
    }

    public static void onPlayerKick(PlayerSnapshot player, String message) {
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

        final Statistics stats = group.getStatistics(player);
        stats.incr(Statistic.kicks);
        stats.set(Statistic.lastKick, player.getDate());
        stats.set(Statistic.lastKickMessage, message);
        kickedPlayers.add(player.getUUID());
    }

    public static void onPlayerDeath(PlayerSnapshot player, String message, EntityDamageEvent.DamageCause cause) {
        Utils.debug("onPlayerDeath '%s'", player.getName());

        final Statistics stats = group.getStatistics(player);
        stats.incr(Statistic.deaths);
        stats.set(Statistic.lastDeath, player.getDate());
        stats.set(Statistic.lastDeathMessage, message);
        stats.incr(Statistic.deathCauses, Utils.titleCase(cause.name()));
        stats.flushSync();

        onPlayerMove(player, player.getLocation());
        final PlayerState state = playerStates.get(player.getUUID());
        if (state != null) {
            state.reset();
        }
    }

    public static void onPlayerMove(PlayerSnapshot player, Location to) {
        Utils.debug("onPlayerMove '%s'", player.getName());

        final Statistics stats = group.getStatistics(player);
        final PlayerState state = playerStates.get(player.getUUID());
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
            stats.add(Statistic.travelDistances, newModeName, distance);
            stats.add(Statistic.totalDistanceTraveled, distance);
            if (distance >= 1) {
                stats.add(Statistic.travelTimes, newModeName, elapsed);
            }
        } else {
            state.lastMode = newMode;
        }

//		if (block.getY() < 255 && block.getLightFromSky() < 6) {
        if (block.getY() < 65 && isInside(block.getLocation(), 2)) {
            if (state.lastBiome == null) {
                stats.add(Statistic.biomeDistances, "Underground", distance);
                stats.add(Statistic.biomeTimes, "Underground", elapsed);
            } else {
                state.lastBiome = null;
            }
        } else if (state.lastBiome == block.getBiome()) {
            String biomeName = Utils.titleCase(state.lastBiome.name());
            stats.add(Statistic.biomeDistances, biomeName, distance);
            stats.add(Statistic.biomeTimes, biomeName, elapsed);
        } else {
            state.lastBiome = block.getBiome();
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

    public static void onPlayerTeleport(PlayerSnapshot player, Location to) {
        if (!started) {
            return;
        }
        final PlayerState state = playerStates.get(player.getUUID());
        if (state != null) {
            onPlayerMove(player, player.getLocation());
            state.lastLocation = to;
            state.lastTime = System.currentTimeMillis();
        }
    }

    public static void onPlayerEnterBed(PlayerSnapshot player) {
        bedOwners.add(player.getUUID());
        final Statistics stats = group.getStatistics(player);
        stats.incr(Statistic.timesSlept);
    }

    public static void checkBeds() {
        Utils.fire(new BedCheck(Global.plugin.getServer(), bedOwners));
    }

    public static TypeMap getPlayerStats(String playerName) {
        boolean isOnline = Global.plugin.getServer().getPlayer(playerName) != null;
        final Statistics stats = group.getStatistics(playerName);
        if (!isOnline) {
            group.removeStatistics(stats);
        }
        return stats.getStats();
    }

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

        stats.set(Statistic.displayName, player.getDisplayName());

        stats.set(Statistic.address, player.getAddress().getAddress().getHostAddress());
        stats.set(Statistic.inventory, encodeItemStacks(player.getInventory().getContents()));
        stats.set(Statistic.armor, encodeItemStacks(player.getInventory().getArmorContents()));
        stats.set(Statistic.ender, encodeItemStacks(player.getEnderChest().getContents()));
        stats.set(Statistic.heldItemSlot, player.getInventory().getHeldItemSlot());
        stats.set(Statistic.health, player.getHealth());
        stats.set(Statistic.remainingAir, player.getRemainingAir());
        stats.set(Statistic.fireTicks, player.getFireTicks());
        stats.set(Statistic.foodLevel, player.getFoodLevel());
        stats.set(Statistic.exhaustion, player.getExhaustion());
        stats.set(Statistic.saturation, player.getSaturation());
        stats.set(Statistic.gameMode, player.getGameMode().toString());
        stats.set(Statistic.level, player.getLevel());
        stats.set(Statistic.exp, player.getExp());
        stats.set(Statistic.totalExperience, player.getTotalExperience());
        stats.set(Statistic.potionEffects, encodePotionEffects(player.getActivePotionEffects()));

        stats.set(Statistic.server, Global.plugin.getServer().getServerName());
        stats.set(Statistic.world, player.getWorld().getName());
        stats.set(Statistic.coords, encodeCoords(player.getLocation()));

        if (Global.enabled) {
            stats.set(Statistic.groups, Permissions.getGroups(player));
            stats.set(Statistic.money, Economy.getBalanace(player));
        }

        if ((!DB.getShared()) || bedOwners.contains(player.getUniqueId())) {
            if ((player.getBedSpawnLocation() == null) || (player.getBedSpawnLocation().getBlock().getType() != Material.BED_BLOCK)) {
                stats.set(Statistic.bedServer, null);
                stats.set(Statistic.bedWorld, null);
                stats.set(Statistic.bedCoords, null);
            } else {
                stats.set(Statistic.bedServer, Global.plugin.getServer().getServerName());
                stats.set(Statistic.bedWorld, player.getBedSpawnLocation().getWorld().getName());
                stats.set(Statistic.bedCoords, encodeCoords(player.getBedSpawnLocation()));
            }
        }

        PlayerState state = playerStates.get(player.getUniqueId());
        if (state != null) {
            float sessionTime = (float) (System.currentTimeMillis() - state.joinTime) / 1000f;
            stats.set(Statistic.sessionTime, sessionTime);
            stats.set(Statistic.totalTime, state.totalTimeBase + sessionTime);
        }
    }

    private static Object encodeItemStacks(ItemStack[] stacks) {
        if (stacks == null) {
            return null;
        }
        List<TypeMap> stackMaps = new ArrayList<>();
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
        List<TypeMap> peMaps = new ArrayList<>(effects.size());
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
                bedOwners.add(UUID.fromString(rs.getString("uuid")));
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

        bedCheckTask = Utils.fireDelayed(() -> {
            checkBeds();
            scheduleBedCheck();
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

    /**
     * Changes to the underlying data must use this method to ensure sequential
     * updates.
     *
     * @param change logic to update stats.
     */
    public static void submitChange(Runnable change) {
        pool.submit(change);
    }

    // Inner classes
    public static interface PlayerStatsListener {

        public void onPlayerStatsStarted();

        public void onPlayerStatsStopping();
    }

}
