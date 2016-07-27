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

import com.frdfsnlght.inquisitor.StatisticsManager.Job;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Statistics {

    private StatisticsGroup group;
    private Object key;

    private boolean inDB = false;
    private TypeMap stats = new TypeMap();
    private Set<Statistic> dirty = new HashSet<>();
    private long lastFlushed = 0;
    private boolean valid = true;

    public Statistics(StatisticsGroup group, Object key) {
        if (group == null)
            throw new IllegalArgumentException("group cannot be null");
        if (key == null)
            throw new IllegalArgumentException("key cannot be null");
        this.group = group;
        this.key = key;
        this.group.getStatistics().forEach(statistic -> addStatistic(statistic));
        dirty.clear();
    }

    public StatisticsGroup getGroup() {
        return group;
    }

    public Object getKey() {
        return key;
    }

    public long getLastFlushed() {
        return lastFlushed;
    }

    public boolean isValid() {
        return valid;
    }

    public void setInvalid() {
        valid = false;
    }

    public TypeMap getStats() {
        return stats;
    }

    public void addStatistic(Statistic statistic) {
        if (stats.containsKey(statistic.getName())) return;
        if (statistic.isMapped())
            stats.set(statistic.getName(), null);
        else
            switch (statistic.getType()) {
                case STRING:
                    stats.set(statistic.getName(), statistic.getDefault());
                case BOOLEAN:
                    stats.set(statistic.getName(), false);
                case OBJECT:
                case TIMESTAMP:
                    stats.set(statistic.getName(), null);
                case ELAPSED_TIME:
                case DISTANCE:
                case INTEGER:
                case LONG:
                case FLOAT:
                case DOUBLE:
                    stats.set(statistic.getName(), 0);
            }
        dirty.add(statistic);
    }

    public void removeStatistic(Statistic statistic) {
        if (! stats.containsKey(statistic.getName())) return;
        stats.remove(statistic.getName());
        dirty.remove(statistic);
    }

    public Collection<Statistic> getDirty() {
        return dirty;
    }

    public boolean purge() {
        if (! valid) {
            group.removeStatistics(key);
            return true;
        }
        return false;
    }

    public void load(ResultSet rs) throws SQLException {
        stats.clear();
        inDB = true;
        dirty.clear();
        stats.putAll(group.loadStatistics(rs));
    }

    public boolean isInDB() {
        return inDB;
    }

    public String getString(String name) {
        return stats.getString(name);
    }

    public boolean getBoolean(String name) {
        return stats.getBoolean(name);
    }

    public int getInt(String name) {
        return stats.getInt(name);
    }

    public long getLong(String name) {
        return stats.getLong(name);
    }

    public float getFloat(String name) {
        return stats.getFloat(name);
    }

    public double getDouble(String name) {
        return stats.getDouble(name);
    }

    /*public void set(String name, Object value) {
        Statistic statistic = group.getStatistic(name);
        if (statistic == null)
            throw new IllegalArgumentException("statistic '" + name + "' does not belong to " + group);
        set(statistic, value);
    }*/

    public void set(Statistic statistic, Object value) {
        if (statistic.isMapped())
            throw new UnsupportedOperationException(statistic + " requires use of the mapped setter");
        String name = statistic.getName();
        switch (statistic.getType()) {
            case STRING:
                stats.set(name, (value == null) ? null : value.toString());
                break;
            case BOOLEAN:
                stats.set(name, (value == null) ? false : (value instanceof Boolean) ? (Boolean)value : true);
                break;
            case OBJECT:
                stats.set(name, value);
                break;
            case TIMESTAMP:
                stats.set(name, (value == null) ? null :
                                (value instanceof Date) ? (Date)value :
                                (value instanceof Calendar) ? new Date(((Calendar)value).getTimeInMillis()) :
                                0);
                break;
            case INTEGER:
                stats.set(name, (value == null) ? 0 : (value instanceof Number) ? ((Number)value).intValue() : 0);
                break;
            case LONG:
                stats.set(name, (value == null) ? 0 : (value instanceof Number) ? ((Number)value).longValue() : 0);
                break;
            case ELAPSED_TIME:
            case DISTANCE:
            case FLOAT:
                stats.set(name, (value == null) ? 0 : (value instanceof Number) ? ((Number)value).floatValue() : 0);
                break;
            case DOUBLE:
                stats.set(name, (value == null) ? 0 : (value instanceof Number) ? ((Number)value).doubleValue() : 0);
                break;
            default:
                throw new UnsupportedOperationException(statistic.getType() + " can not be set");
        }
        dirty.add(statistic);
    }

    /*public void set(String name, String key, Object value) {
        Statistic statistic = group.getStatistic(name);
        if (statistic == null)
            throw new IllegalArgumentException("statistic '" + name + "' does not belong to " + group);
        set(statistic, key, value);
    }*/

    public void set(Statistic statistic, String key, Object value) {
        if (! statistic.isMapped())
            throw new UnsupportedOperationException(statistic + " requires use of the non-mapped setter");
        String name = statistic.getName();
        TypeMap map = stats.getMap(name);
        if (map == null) {
            map = new TypeMap();
            stats.set(name, map);
        }
        switch (statistic.getType()) {
            case STRING:
                map.set(key, (value == null) ? null : value.toString());
                break;
            case BOOLEAN:
                map.set(key, (value == null) ? false : (value instanceof Boolean) ? (Boolean)value : true);
                break;
            case OBJECT:
                map.set(key, value);
                break;
            case TIMESTAMP:
                map.set(key, (value == null) ? null :
                                (value instanceof Date) ? (Date)value :
                                (value instanceof Calendar) ? new Date(((Calendar)value).getTimeInMillis()) :
                                0);
                break;
            case INTEGER:
                map.set(key, (value == null) ? 0 : (value instanceof Number) ? ((Number)value).intValue() : 0);
                break;
            case LONG:
                map.set(key, (value == null) ? 0 : (value instanceof Number) ? ((Number)value).longValue() : 0);
                break;
            case ELAPSED_TIME:
            case DISTANCE:
            case FLOAT:
                map.set(key, (value == null) ? 0 : (value instanceof Number) ? ((Number)value).floatValue() : 0);
                break;
            case DOUBLE:
                map.set(key, (value == null) ? 0 : (value instanceof Number) ? ((Number)value).doubleValue() : 0);
                break;
            default:
                throw new UnsupportedOperationException(statistic.getType() + " cannot be set");
        }
        dirty.add(statistic);
    }

    /*public void add(String name, Number value) {
        Statistic statistic = group.getStatistic(name);
        if (statistic == null)
            throw new IllegalArgumentException("statistic '" + name + "' does not belong to " + group);
        add(statistic, value);
    }*/

    public void add(Statistic statistic, Number value) {
        if (value == null) return;
        if (statistic.isMapped())
            throw new UnsupportedOperationException(statistic + " requires use of the mapped setter");
        String name = statistic.getName();
        switch (statistic.getType()) {
            case INTEGER:
                stats.set(name, stats.getInt(name, 0) + value.intValue());
                break;
            case LONG:
                stats.set(name, stats.getLong(name, 0L) + value.longValue());
                break;
            case ELAPSED_TIME:
            case DISTANCE:
            case FLOAT:
                stats.set(name, stats.getFloat(name, 0f) + value.floatValue());
                break;
            case DOUBLE:
                stats.set(name, stats.getDouble(name, 0d) + value.doubleValue());
                break;
            default:
                throw new UnsupportedOperationException(statistic.getType() + " cannot be added");
        }
        dirty.add(statistic);
    }

    /*public void add(String name, String key, Number value) {
        Statistic statistic = group.getStatistic(name);
        if (statistic == null)
            throw new IllegalArgumentException("statistic '" + name + "' does not belong to " + group);
        add(statistic, key, value);
    }*/

    public void add(Statistic statistic, String key, Number value) {
        if (value == null) return;
        if (! statistic.isMapped())
            throw new UnsupportedOperationException(statistic + " requires use of the non-mapped setter");
        String name = statistic.getName();
        TypeMap map = stats.getMap(name);
        if (map == null) {
            map = new TypeMap();
            stats.set(name, map);
        }
        switch (statistic.getType()) {
            case INTEGER:
                map.set(key, map.getInt(key, 0) + value.intValue());
                break;
            case LONG:
                map.set(key, map.getLong(key, 0L) + value.longValue());
                break;
            case ELAPSED_TIME:
            case DISTANCE:
            case FLOAT:
                map.set(key, map.getFloat(key, 0f) + value.floatValue());
                break;
            case DOUBLE:
                map.set(key, map.getDouble(key, 0d) + value.doubleValue());
                break;
            default:
                throw new UnsupportedOperationException(statistic.getType() + " cannot be added");
        }
        dirty.add(statistic);
    }

    /*public void incr(String name) {
        add(name, 1);
    }*/

    public void incr(Statistic statistic) {
        add(statistic, 1);
    }

    /*public void incr(String name, String key) {
        add(name, key, 1);
    }*/

    public void incr(Statistic statistic, String key) {
        add(statistic, key, 1);
    }

    public void flush() {
        _flush();
    }

    public void flushSync() {
        Job job = _flush();
        if (job == null) return;
        if (! valid) return;
        synchronized (job) {
            while (! job.isCommitted())
                try {
                    job.wait();
                } catch (InterruptedException ie) {}
        }
    }

    private Job _flush() {
        group.fireBeforeFlush(this);
        if (dirty.isEmpty()) return null;
        Map<String,Object> jobData = new HashMap<>();
        TypeMap mappedObjects = null;
        for (Statistic statistic : dirty) {
            if (statistic == null) continue;
            if (statistic.isMapped()) {
                if (mappedObjects == null) {
                    mappedObjects = new TypeMap();
                    for (String sName : stats.keySet()) { 
                        Statistic s = Statistic.getFromName(sName);
                        if (s.isMapped()) {
                            mappedObjects.set(sName, stats.get(sName));
                        }
                    }
                }
            } else {
                String statName = statistic.getName();
                switch (statistic.getType()) {
                    case STRING:
                        jobData.put(statName, stats.getString(statName));
                        break;
                    case BOOLEAN:
                        jobData.put(statName, stats.getBoolean(statName) ? 1 : 0);
                        break;
                    case OBJECT:
                        try {
                            jobData.put(statName, DB.encodeToJSON(stats.get(statName)));
                        } catch (Throwable t) {
                            Utils.warning("Unable to encode %s to Clob: %s", statName, t.getMessage());
                        }
                        break;
                    case TIMESTAMP:
                        jobData.put(statName, stats.getDate(statName));
                        break;
                    case INTEGER:
                        jobData.put(statName, stats.getInt(statName));
                        break;
                    case LONG:
                        jobData.put(statName, stats.getLong(statName));
                        break;
                    case ELAPSED_TIME:
                    case DISTANCE:
                    case FLOAT:
                        jobData.put(statName, stats.getFloat(statName));
                        break;
                    case DOUBLE:
                        jobData.put(statName, stats.getDouble(statName));
                        break;
                    default:
                        throw new UnsupportedOperationException(statistic + " cannot be flushed");
                }
            }
        }
        if (mappedObjects != null) {
            try {
                jobData.put(Statistic.MappedObjectsColumn, DB.encodeToJSON(mappedObjects));
            } catch (Throwable t) {
                Utils.warning("Unable to encode mapped objects to Clob: %s", t.getMessage());
            }
        }
        jobData.put("lastUpdate", new Date());
        Job job;
        if (inDB)
            job = new Job(group.getName(), jobData, key, group.getKeyName());
        else {
            switch (group.getKeyType()) {
                case INTEGER:
                    jobData.put(group.getKeyName(), ((Number)key).intValue());
                    break;
                case LONG:
                    jobData.put(group.getKeyName(), ((Number)key).longValue());
                    break;
                case STRING:
                    jobData.put(group.getKeyName(), key.toString());
                    break;
            }
            job = new Job(group.getName(), jobData, key);
        }

        if (valid)
            StatisticsManager.submitJob(job);
        lastFlushed = System.currentTimeMillis();
        inDB = true;
        dirty.clear();
        Utils.debug("flushed %s %s", group.getName(), key);

        return job;
    }

}
