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

import com.frdfsnlght.inquisitor.exceptions.OptionsException;
import com.frdfsnlght.inquisitor.exceptions.PermissionsException;
import com.frdfsnlght.inquisitor.DB.DBListener;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class StatisticsManager {

    private static final Set<String> OPTIONS = new HashSet<>();
    private static final Set<String> RESTART_OPTIONS = new HashSet<>();
    private static final Options options;

    private static final Set<StatisticsManagerListener> listeners = new HashSet<>();

    private static final Map<String, StatisticsGroup> groups = new HashMap<>();
    private static final List<Job> jobs = new ArrayList<>();

    private static Thread jobThread;
    private static boolean started = false;
    private static int flushCheckTask = -1;

    static {
        OPTIONS.add("debug");
        OPTIONS.add("flushCheckInterval");

        RESTART_OPTIONS.add("flushCheckInterval");

        options = new Options(StatisticsManager.class, OPTIONS, "inq.stats", new OptionsListener() {
            @Override
            public void onOptionSet(Context ctx, String name, String value) {
                ctx.sendLog("statistics option '%s' set to '%s'", name, value);
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

        DB.addListener(new DBListener() {
            @Override
            public void onDBConnected() {
                start();
            }

            @Override
            public void onDBDisconnecting() {
                stop();
            }
        });
    }

    public static void init() {
    }

    public static void addListener(StatisticsManagerListener listener) {
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
            if (!DB.isConnected()) {
                throw new Exception("database is not connected");
            }

            groups.values().forEach(group -> group.validate());

            int purged = purge();
            Utils.info("purged %s invalid cached statistics instances", purged);

            scheduleFlushCheck();

            started = true;
            jobThread = new Thread(StatisticsManager::background);
            jobThread.setDaemon(true);
            jobThread.setName("Statistics updater");
            jobThread.start();
            Utils.info("statistics manager started");

            listeners.forEach(StatisticsManagerListener::onStatisticsManagerStarted);

        } catch (Exception e) {
            Utils.warning("statistics manager cannot be started: %s", e.getMessage());
        }
    }

    public static void stop() {
        if (!started) {
            return;
        }
        listeners.forEach(listener -> listener.onStatisticsManagerStopping());
        if (flushCheckTask != -1) {
            Global.plugin.getServer().getScheduler().cancelTask(flushCheckTask);
        }
        flushCheckTask = -1;
        flushAllSync();
        started = false;
        jobThread.interrupt();
        jobThread = null;
        Utils.info("statistics manager stopped");
    }

    public static void addGroup(StatisticsGroup group) {
        if (groups.containsKey(group.getName())) {
            return;
        }
        groups.put(group.getName(), group);
        group.validate();
    }

    public static void removeGroup(StatisticsGroup group) {
        if (!groups.containsKey(group.getName())) {
            return;
        }
        groups.remove(group.getName());
    }

    public static Collection<StatisticsGroup> getGroups() {
        return groups.values();
    }

    public static StatisticsGroup getGroup(String name) {
        return groups.get(name);
    }

    public static StatisticsGroup findGroup(String name) {
        if (groups.containsKey(name)) {
            return groups.get(name);
        }
        String lname = name.toLowerCase();
        StatisticsGroup group = null;
        for (String key : groups.keySet()) {
            if (key.toLowerCase().startsWith(lname)) {
                if (group == null) {
                    group = groups.get(key);
                } else {
                    return null;
                }
            }
        }
        return group;
    }

    public static int getJobCount() {
        synchronized (jobs) {
            return jobs.size();
        }
    }

    public static Collection<String> getJobsSnapshot() {
        synchronized (jobs) {
            return jobs.stream()
                    .map(Job::toString)
                    .collect(Collectors.toList());
        }
    }

    public static Statistics getStatistics(String groupName, Object key) {
        StatisticsGroup group = groups.get(groupName);
        if (group == null) {
            throw new IllegalArgumentException("statistics group '" + groupName + "' does not exist");
        }
        return group.getStatistics(key);
    }

    public static int purge() {
        return groups.values().stream().mapToInt(StatisticsGroup::purge).sum();
    }

    public static void flushAll() {
        groups.values().forEach(StatisticsGroup::flushAll);
    }

    public static void flushAllSync() {
        groups.values().forEach(StatisticsGroup::flushAllSync);
    }

    public static void flush() {
        groups.values().forEach(StatisticsGroup::flush);
    }

    public static void delete() {
        groups.values().forEach(StatisticsGroup::delete);
    }

    public static void submitJob(Job job) {
        if (!started) {
            Utils.warning("statistics manager is not started, discarding %s", job);
            return;
        }
        synchronized (jobs) {
            jobs.add(job);
            jobs.notify();
        }
    }

    /* Begin options */
    public static boolean getDebug() {
        return Config.getBooleanDirect("stats.debug", false);
    }

    public static void setDebug(boolean b) {
        Config.setPropertyDirect("stats.debug", b);
    }

    public static int getFlushCheckInterval() {
        return Config.getIntDirect("stats.flushCheckInterval", 10000);
    }

    public static void setFlushCheckInterval(int i) {
        if (i < 1000) {
            throw new IllegalArgumentException("flushCheckInterval must be at least 1000");
        }
        Config.setPropertyDirect("stats.flushCheckInterval", i);
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
    private static void scheduleFlushCheck() {
        if (flushCheckTask != -1) {
            Global.plugin.getServer().getScheduler().cancelTask(flushCheckTask);
        }
        flushCheckTask = Utils.fireDelayed(() -> {
            PlayerStats.submitChange(() -> {
                flush();
                delete();
                scheduleFlushCheck();
            });
        }, getFlushCheckInterval());
    }

    private static void background() {
        Job job = null;
        for (;;) {
            synchronized (jobs) {
                while (started && jobs.isEmpty()) {
                    try {
                        jobs.wait(5000);
                    } catch (InterruptedException ie) {
                    }
                    if (!started) {
                        break;
                    }
                }
                if (!jobs.isEmpty()) {
                    job = jobs.remove(0);
                }
            }
            if (job != null) {
                if (getDebug()) {
                    Utils.debug("committing %s", job);
                }
                job.commit();
                job = null;
            } else if (!started) {
                break;
            }
        }
    }

    public static interface StatisticsManagerListener {

        public void onStatisticsManagerStarted();

        public void onStatisticsManagerStopping();
    }

    public static class Job {

        private String tableName;
        private Map<String, Object> data;
        private Object keyValue;
        private String keyColumn;
        private boolean committed = false;

        public Job(String tableName, Map<String, Object> data, Object keyValue) {
            this(tableName, data, keyValue, null);
        }

        public Job(String tableName, Map<String, Object> data, Object keyValue, String keyColumn) {
            this.tableName = tableName;
            this.data = data;
            this.keyValue = keyValue;
            this.keyColumn = keyColumn;
        }

        @Override
        public String toString() {
            return "Job[" + tableName + "," + keyValue + "]";
        }

        public boolean isCommitted() {
            return committed;
        }

        void commit() {
            PreparedStatement stmt = null;
            StringBuilder sql = new StringBuilder();
            List<String> cols = new ArrayList<>(data.keySet());
            try {
                if (keyColumn == null) {
                    // insert
                    sql.append("INSERT INTO ").append(DB.tableName(tableName)).append(" (");
                    for (String col : cols) {
                        sql.append('`').append(col).append("`,");
                    }
                    sql.deleteCharAt(sql.length() - 1);
                    sql.append(") VALUES (");
                    for (String col : cols) {
                        sql.append("?,");
                    }
                    sql.deleteCharAt(sql.length() - 1);
                    sql.append(")");
                    stmt = DB.prepare(sql.toString());
                    int colNum = 1;
                    for (String col : cols) {
                        setParameter(stmt, colNum++, data.get(col));
                    }
                } else {
                    // update
                    sql.append("UPDATE ").append(DB.tableName(tableName)).append(" SET ");
                    for (String col : cols) {
                        sql.append('`').append(col).append("`=?,");
                    }
                    sql.deleteCharAt(sql.length() - 1);
                    sql.append(" WHERE `").append(keyColumn).append("`=?");
                    stmt = DB.prepare(sql.toString());
                    int colNum = 1;
                    for (String col : cols) {
                        setParameter(stmt, colNum++, data.get(col));
                    }
                    setParameter(stmt, colNum++, keyValue);
                }
                stmt.executeUpdate();

            } catch (SQLException se) {
                Utils.severe("SQLException while committing statistics for '%s' in '%s': %s", keyValue, tableName, se.getMessage());
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException se) {
                }
            }
            committed = true;
            synchronized (this) {
                notifyAll();
            }
        }

        private void setParameter(PreparedStatement stmt, int colNum, Object val) throws SQLException {
            if (val == null) {
                stmt.setString(colNum++, null);
            } else if (val instanceof String) {
                stmt.setString(colNum++, (String) val);
            } else if (val instanceof Boolean) {
                stmt.setInt(colNum++, ((Boolean) val) ? 1 : 0);
            } else if (val instanceof Byte) {
                stmt.setByte(colNum++, (Byte) val);
            } else if (val instanceof Short) {
                stmt.setShort(colNum++, (Short) val);
            } else if (val instanceof Integer) {
                stmt.setInt(colNum++, (Integer) val);
            } else if (val instanceof Long) {
                stmt.setLong(colNum++, (Long) val);
            } else if (val instanceof Float) {
                stmt.setFloat(colNum++, (Float) val);
            } else if (val instanceof Double) {
                stmt.setDouble(colNum++, (Double) val);
            } else if (val instanceof Date) {
                stmt.setTimestamp(colNum++, new Timestamp(((Date) val).getTime()));
            } else if (val instanceof Clob) {
                stmt.setClob(colNum++, (Clob) val);
            } else {
                throw new SQLException(val.getClass().getName() + " is an unsupported parameter type");
            }
        }

    }

}
