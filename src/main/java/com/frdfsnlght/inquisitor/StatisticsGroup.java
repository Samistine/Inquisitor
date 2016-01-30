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

import com.frdfsnlght.inquisitor.Statistic.Type;

import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class StatisticsGroup {

    private String name;
    private String keyName;
    private Type keyType;
    private int keySize;
    private long flushInterval = 1000 * 60; // every minute
    private long deleteInterval = 1000 * 3600; // every hour
    private long deleteAge = -1;

    private long lastDelete = 0;

    private final Map<String, Statistic> statistics = new HashMap<String, Statistic>();
    private final Map<Object, Statistics> stats = new HashMap<Object, Statistics>();

    private List<BeforeFlushListener> beforeFlushListeners = new ArrayList<BeforeFlushListener>();

    public StatisticsGroup(String name, String keyName, Type keyType, int keySize) {
        if (!name.matches("^\\w+$")) {
            throw new IllegalArgumentException("'" + name + "' is not a valid statistics group name");
        }
        if (!keyName.matches("^\\w+$")) {
            throw new IllegalArgumentException("'" + keyName + "' is not a valid statistics group key name");
        }
        switch (keyType) {
            case STRING:
                break;
            case INTEGER:
            case LONG:
                keySize = -1;
                break;
            default:
                throw new IllegalArgumentException("type '" + keyType + "' is not a supported key type");
        }
        this.name = name;
        this.keyName = keyName;
        this.keyType = keyType;
        this.keySize = keySize;
    }

    @Override
    public String toString() {
        return "StatisticsGroup[" + name + "]";
    }

    public String getName() {
        return name;
    }

    public String getKeyName() {
        return keyName;
    }

    public Type getKeyType() {
        return keyType;
    }

    public int getKeySize() {
        return keySize;
    }

    public long getFlushInterval() {
        return flushInterval;
    }

    public void setFlushInterval(long interval) {
        if (interval < 1) {
            throw new IllegalArgumentException("flush interval must be greater than 0");
        }
        flushInterval = interval;
    }

    public long getDeleteInterval() {
        return deleteInterval;
    }

    public void setDeleteInterval(long interval) {
        if (interval < 1) {
            throw new IllegalArgumentException("delete interval must be greater than 0");
        }
        deleteInterval = interval;
    }

    public long getDeleteAge() {
        return deleteAge;
    }

    public void setDeleteAge(long age) {
        if (age < 1) {
            age = -1;
        }
        deleteAge = age;
    }

    public int purge() {
        int count = 0;
        for (Object key : new HashSet<Object>(stats.keySet())) {
            Statistics s = stats.get(key);
            if (s.purge()) {
                count++;
            }
        }
        return count;
    }

    public void addListener(BeforeFlushListener listener) {
        beforeFlushListeners.add(listener);
    }

    public void removeListener(BeforeFlushListener listener) {
        beforeFlushListeners.remove(listener);
    }

    public void addStatistic(Statistic statistic) {
        synchronized (statistics) {
            if (statistics.containsKey(statistic.getName())) {
                throw new IllegalArgumentException(statistic + " already exists in " + this);
            }
            statistics.put(statistic.getName(), statistic);
        }
        statistic.validate(this);
        for (Statistics s : stats.values()) {
            s.addStatistic(statistic);
        }
    }

    public void removeStatistic(Statistic statistic) {
        synchronized (statistics) {
            if (!statistics.containsKey(statistic.getName())) {
                return;
            }
            statistics.remove(statistic.getName());
        }
        for (Statistics s : stats.values()) {
            s.removeStatistic(statistic);
        }
    }

    public Collection<Statistic> getStatistics() {
        synchronized (statistics) {
            return new HashSet<Statistic>(statistics.values());
        }
    }

    public Statistic getStatistic(String name) {
        synchronized (statistics) {
            return statistics.get(name);
        }
    }

    public Collection<Statistics> getCachedStatistics() {
        return stats.values();
    }

    public Statistics getStatistics(Object key) {
        Statistics s = stats.get(key);
        if (s != null) {
            return s;
        }

        switch (keyType) {
            case INTEGER:
            case LONG:
                if (!(key instanceof Number)) {
                    throw new IllegalArgumentException("key must be numeric");
                }
                break;
            default: // Added for better readability if error occurs
//			System.out.println("MYSQL ERROR1 StatisticsGroup.getStatistics");
                break;
        }
        s = new Statistics(this, key);
        stats.put(key, s);
        if (!StatisticsManager.isStarted()) {
            s.setInvalid();
            return s;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            synchronized (statistics) {
                boolean addedMapped = false;
                for (String statName : statistics.keySet()) {
                    if (statistics.get(statName).isMapped()) {
                        if (!addedMapped) {
                            addedMapped = true;
                            sql.append('`')
                                    .append(Statistic.MappedObjectsColumn)
                                    .append("`,");
                        }
                    } else {
                        sql.append('`').append(statName).append("`,");
                    }
                }
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(" FROM ").append(DB.tableName(name));
            sql.append(" WHERE `").append(keyName).append("`=?");
            stmt = DB.prepare(sql.toString());
            switch (keyType) {
                case INTEGER:
                    stmt.setInt(1, ((Number) key).intValue());
                    break;
                case LONG:
                    stmt.setLong(1, ((Number) key).longValue());
                    break;
                case STRING:
                    stmt.setString(1, key.toString());
                    break;
                default: // Added for better readability if error occurs
                    System.out
                            .println("MYSQL ERROR2 StatisticsGroup.getStatistics");
                    break;
            }
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return s;
            }
            s.load(rs);

        } catch (SQLException se) {
            Utils.severe(
                    "SQLException while loading statistics for %s key '%s': %s",
                    this, key, se.getMessage());
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
        return s;
    }

    public Set<String> getStatisticsNames() {
        synchronized (statistics) {
            return new HashSet<String>(statistics.keySet());
        }
    }

    public TypeMap loadStatistics(ResultSet rs) throws SQLException {
        return loadStatistics(rs, getStatisticsNames());
    }

    public TypeMap loadStatistics(ResultSet rs, Collection<String> statNames) throws SQLException {
        TypeMap values = new TypeMap();
        TypeMap mappedObjects = null;
        Utils.debug("Entering loadStatistics(ResultSet rs, Collection<String> statNames)");
        for (String statName : statNames) {
            Utils.debug("Loading statistic %s", statName);
            Statistic stat = getStatistic(statName);
            if (stat == null) {
                continue;
            }
            if (stat.isMapped()) {
                Utils.debug("stat.isMapped == true");
                if (mappedObjects == null) {
                    Utils.debug("mappedObject == null");
                    Clob c = rs.getClob(Statistic.MappedObjectsColumn);
                    Utils.debug("c = %s", c.toString());
                    Object o = DB.decodeFromJSON(c);
                    Utils.debug("o is object type %s", o.getClass().getName());
                    if (o instanceof TypeMap) {
                        mappedObjects = (TypeMap) o;
                    } else {
                        throw new UnsupportedOperationException(
                                "mapped objects cannot be read from the database");
                    }
                }
                Utils.debug("set stat value to %s", mappedObjects.get(statName));
                values.set(statName, mappedObjects.get(statName));
            } else {
                switch (stat.getType()) {
                    case STRING:
                        values.set(statName, rs.getString(statName));
                        break;
                    case BOOLEAN:
                        values.set(statName, rs.getBoolean(statName));
                        break;
                    case OBJECT:
                        values.set(statName,
                                DB.decodeFromJSON(rs.getClob(statName)));
                        break;
                    case TIMESTAMP:
                        values.set(statName,
                                DB.decodeTimestamp(rs.getTimestamp(statName)));
                        break;
                    case INTEGER:
                        values.set(statName, rs.getInt(statName));
                        break;
                    case LONG:
                        values.set(statName, rs.getLong(statName));
                        break;
                    case ELAPSED_TIME:
                    case DISTANCE:
                    case FLOAT:
                        values.set(statName, rs.getFloat(statName));
                        break;
                    case DOUBLE:
                        values.set(statName, rs.getDouble(statName));
                        break;
                    default:
                        throw new UnsupportedOperationException(stat + " cannot be read from the database");
                }
            }
        }
        return values;
    }

    public Statistics findStatistics(Object statsKey) {
        if (stats.containsKey(statsKey)) {
            return stats.get(statsKey);
        }
        String lkey = statsKey.toString().toLowerCase();
        Statistics s = null;
        for (Object key : stats.keySet()) {
            if (key.toString().toLowerCase().startsWith(lkey)) {
                if (s == null) {
                    s = stats.get(key);
                } else {
                    return null;
                }
            }
        }
        return s;
    }

    public void removeStatistics(Statistics s) {
        stats.remove(s.getKey());
    }

    public void removeStatistics(Object key) {
        stats.remove(key);
    }

    public void flushAll() {
        for (Statistics stat : stats.values()) {
            stat.flush();
        }
    }

    public void flushAllSync() {
        for (Statistics stat : stats.values()) {
            stat.flushSync();
        }
    }

    public void flush() {
        for (Statistics stat : stats.values()) {
            if ((stat.getLastFlushed() + flushInterval) < System.currentTimeMillis()) {
                stat.flush();
            }
        }
    }

    public void fireBeforeFlush(Statistics stats) {
        for (BeforeFlushListener listener : beforeFlushListeners) {
            listener.beforeFlush(stats);
        }
    }

    public void delete() {
        if (((System.currentTimeMillis() - lastDelete) <= getDeleteInterval()) || (getDeleteAge() <= 0) || (!DB.isConnected())) {
            return;
        }

        lastDelete = System.currentTimeMillis();

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuilder sql = new StringBuilder();
            Date cutoff = new Date(System.currentTimeMillis() - getDeleteAge());

            sql.append("SELECT COUNT(1) FROM ").append(DB.tableName(name));
            sql.append(" WHERE lastUpdate < ?");
            stmt = DB.prepare(sql.toString());
            stmt.setDate(1, cutoff);
            rs = stmt.executeQuery();
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            stmt.close();

            if (count > 0) {
                sql = new StringBuilder();
                sql.append("DELETE FROM ").append(DB.tableName(name));
                sql.append(" WHERE lastUpdate < ?");
                stmt = DB.prepare(sql.toString());
                stmt.setDate(1, cutoff);
                stmt.executeUpdate();
                stmt.close();
                Utils.info("Deleted %s %s records", count, this);
            }
        } catch (SQLException se) {
            Utils.severe("SQLException while deleting from %s: %s", this,
                    se.getMessage());
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

    public void validate() {
        if (!DB.isConnected()) {
            return;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuilder sql = new StringBuilder();

            if (!DB.tableExists(name)) {
                sql.append("CREATE TABLE ").append(DB.tableName(name))
                        .append('(');
                sql.append("`id` int NOT NULL AUTO_INCREMENT,");
                sql.append("`").append(keyName).append("` ")
                        .append(keyType.getSQLDef(keySize)).append(',');
                sql.append("`lastUpdate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',");
                sql.append("PRIMARY KEY (`id`),");
                sql.append("UNIQUE KEY `").append(keyName).append("` (`")
                        .append(keyName).append("`)");
                sql.append(") CHARACTER SET utf8, COLLATE utf8_general_ci");
                stmt = DB.prepare(sql.toString());
                stmt.executeUpdate();
                stmt.close();
                Utils.info("Created table for statistic group '%s'.", name);
            } else {
                sql.append("SHOW CREATE TABLE ").append(DB.tableName(name));
                stmt = DB.prepare(sql.toString());
                rs = stmt.executeQuery();
                boolean alter = true;
                if (rs.next()) {
                    Matcher matcher = Pattern.compile("DEFAULT CHARSET=(\\w+)")
                            .matcher(rs.getString(2));
                    rs.close();
                    stmt.close();
                    if (matcher.find()) {
                        String charSet = matcher.group(1);
                        alter = (!charSet.equals("utf8"));
                    }
                }
                if (alter) {
                    sql = new StringBuilder();
                    sql.append("ALTER TABLE ").append(DB.tableName(name));
                    sql.append(" CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci");
                    stmt = DB.prepare(sql.toString());
                    stmt.executeUpdate();
                    stmt.close();
                    Utils.info(
                            "Altered character set and collation for statistic group '%s'.",
                            name);
                    stmt.close();
                }
            }

            synchronized (statistics) {
                for (Statistic statistic : statistics.values()) {
                    statistic.validate(this);
                }
            }

        } catch (SQLException se) {
            Utils.severe("SQLException while validating %s: %s", this, se.getMessage());
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

    public boolean validateColumn(String colName, Type type, int size, String def, Set<String> oldNames) {
        if (!DB.isConnected()) {
            return false;
        }

        StringBuilder colDef = new StringBuilder();
        colDef.append('`').append(colName).append("` ")
                .append(type.getSQLDef(size, def));

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuilder sql = new StringBuilder();

            sql.append("SHOW COLUMNS FROM ").append(DB.tableName(name));
            sql.append(" LIKE ?");
            stmt = DB.prepare(sql.toString());
            stmt.setString(1, colName);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                rs.close();
                stmt.close();
                boolean renamed = false;

                if (oldNames != null) {
                    for (String oldName : oldNames) {
                        sql = new StringBuilder();
                        sql.append("SHOW COLUMNS FROM ").append(
                                DB.tableName(name));
                        sql.append(" LIKE ?");
                        stmt = DB.prepare(sql.toString());
                        stmt.setString(1, oldName);
                        rs = stmt.executeQuery();
                        if (!rs.next()) {
                            continue;
                        }
                        rs.close();
                        stmt.close();
                        sql = new StringBuilder();
                        sql.append("ALTER TABLE ").append(DB.tableName(name));
                        sql.append(" CHANGE COLUMN `").append(oldName)
                                .append("` ");
                        sql.append(colDef.toString());
                        stmt = DB.prepare(sql.toString());
                        stmt.executeUpdate();
                        Utils.info(
                                "Renamed statistic column '%s' to '%s' for statistic group '%s'.",
                                oldName, colName, name);
                        renamed = true;
                        break;
                    }
                }

                if (!renamed) {
                    sql = new StringBuilder();
                    sql.append("ALTER TABLE ").append(DB.tableName(name));
                    sql.append(" ADD COLUMN ");
                    sql.append(colDef.toString());
                    stmt = DB.prepare(sql.toString());
                    stmt.executeUpdate();
                    stmt.close();
                    Utils.info(
                            "Added statistic column '%s' for statistic group '%s'.",
                            colName, name);
                }

            } else {
                String dbType = rs.getString("type");
                rs.close();
                stmt.close();
                int pos = dbType.indexOf('(');
                int dbSize = -1;
                if (pos != -1) {
                    int pos2 = Math.max(dbType.indexOf(')', pos + 1),
                            dbType.indexOf(',', pos + 1));
                    try {
                        dbSize = Integer.parseInt(dbType.substring(pos + 1,
                                pos2));
                    } catch (NumberFormatException nfe) {
                    }
                    dbType = dbType.substring(0, pos);
                }
                if ((!dbType.equals(type.getSQLType()))
                        || ((size != -1) && (dbSize != size))) {
                    sql = new StringBuilder();
                    sql.append("ALTER TABLE ").append(DB.tableName(name));
                    sql.append(" MODIFY COLUMN ");
                    sql.append(colDef.toString());
                    stmt = DB.prepare(sql.toString());
                    stmt.executeUpdate();
                    Utils.info(
                            "Altered statistic column '%s' for statistic group '%s'.",
                            colName, name);
                }
            }
            return true;

        } catch (SQLException se) {
            Utils.severe("SQLException while validating column %s in %s: %s", colName, this, se.getMessage());
            return false;
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

    public interface BeforeFlushListener {

        public void beforeFlush(Statistics stats);
    }

}
