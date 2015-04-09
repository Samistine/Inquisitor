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

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */


public final class Statistic {

    public static final String MappedObjectsColumn = "mapped";

    private String name;
    private Type type;
    private boolean mapped;
    private int size;   // for STRING
    private String def; // for STRING
    private StatisticsGroup group = null;
    private Set<String> oldNames = new HashSet<String>();

    public Statistic(String name, Type type) {
        this(name, type, false, -1, null);
    }

    public Statistic(String name, Type type, int size) {
        this(name, type, false, size, null);
    }

    public Statistic(String name, Type type, int size, String def) {
        this(name, type, false, size, def);
    }

    public Statistic(String name, Type type, boolean mapped) {
        this(name, type, mapped, -1, null);
    }

    public Statistic(String name, Type type, boolean mapped, int size, String def) {
        if (! name.matches("^\\w+$"))
            throw new IllegalArgumentException("'" + name + "' is not a valid statistic name");
        if (mapped) {
            size = -1;
            def = null;
        } else
            switch (type) {
                case STRING:
                    if (size < 1)
                        throw new IllegalArgumentException("size must be at least 1");
                    break;
                default:
                    size = -1;
                    def = null;
                    break;
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

    public void setGroup(StatisticsGroup group) {
        this.group = group;
    }

    public StatisticsGroup getGroup() {
        return group;
    }

    public void addOldName(String oldName) {
        oldNames.add(oldName);
    }

    public void validate() {
        if (group == null)
            throw new IllegalStateException(this + " group has not been set");
        if (mapped)
            group.validateColumn(MappedObjectsColumn, Type.OBJECT, -1, null, null);
        else
            group.validateColumn(name, type, size, def, oldNames);
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
