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
package com.frdfsnlght.inquisitor.command;

import com.frdfsnlght.inquisitor.Context;
import com.frdfsnlght.inquisitor.InquisitorException;
import com.frdfsnlght.inquisitor.Permissions;
import com.frdfsnlght.inquisitor.Statistics;
import com.frdfsnlght.inquisitor.StatisticsGroup;
import com.frdfsnlght.inquisitor.StatisticsManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.command.Command;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class StatsCommands  extends CommandProcessor {

    private static final String GROUP = "stats ";

    @Override
    public boolean matches(Context ctx, Command cmd, List<String> args) {
        return super.matches(ctx, cmd, args) &&
               GROUP.startsWith(args.get(0).toLowerCase());
    }

    @Override
    public List<String> getUsage(Context ctx) {
        List<String> cmds = new ArrayList<String>();
        cmds.add(getPrefix(ctx) + GROUP + "status");
        cmds.add(getPrefix(ctx) + GROUP + "flush [<group> [<key>]]");
        cmds.add(getPrefix(ctx) + GROUP + "purge [<group> [<key>]]");
        cmds.add(getPrefix(ctx) + GROUP + "get <option>|*");
        cmds.add(getPrefix(ctx) + GROUP + "set <option> <value>");

        return cmds;
    }

    @Override
    public void process(Context ctx, Command cmd, List<String> args) throws InquisitorException {
        args.remove(0);
        if (args.isEmpty())
            throw new CommandException("do what with stats?");
        String subCmd = args.get(0).toLowerCase();
        args.remove(0);

        if ("status".startsWith(subCmd)) {
            Permissions.require(ctx.getPlayer(), "inq.stats.status");
            ctx.send("statistics manager %s started", StatisticsManager.isStarted() ? "is" : "is not");
            Collection<StatisticsGroup> groups = StatisticsManager.getGroups();
            ctx.send("%s groups registered", groups.size());
            for (StatisticsGroup group : groups) {
                Collection<Statistics> stats = group.getCachedStatistics();
                ctx.send("  group '%s' has %s statistics, %s instances cached", group.getName(), group.getStatistics().size(), stats.size());
                for (Statistics s : stats)
                    ctx.send("    '%s' has %s dirty statistics %s", s.getKey(), s.getDirty().size(), s.isValid() ? "":"(invalid)");
            }
            Collection<String> jobs = StatisticsManager.getJobsSnapshot();
            ctx.send("%s pending updates", jobs.size());
            for (String job : jobs)
                ctx.send("  %s", job);
            return;
        }

        if ("flush".startsWith(subCmd)) {
            Permissions.require(ctx.getPlayer(), "inq.stats.flush");
            if (! StatisticsManager.isStarted())
                throw new CommandException("statistics manager has not been started");
            if (args.isEmpty()) {
                StatisticsManager.flushAll();
                return;
            }
            String groupName = args.remove(0);
            StatisticsGroup group = StatisticsManager.findGroup(groupName);
            if (group == null)
                throw new CommandException("unknown group '%s'", groupName);
            if (args.isEmpty()) {
                group.flushAll();
                return;
            }
            String key = args.remove(0);
            Statistics stats = group.findStatistics(key);
            if (stats == null)
                throw new CommandException("unknown key '%s' in group '%s'", key, groupName);
            stats.flush();
            return;
        }

        if ("purge".startsWith(subCmd)) {
            Permissions.require(ctx.getPlayer(), "inq.stats.purge");
            if (! StatisticsManager.isStarted())
                throw new CommandException("statistics manager has not been started");
            if (args.isEmpty()) {
                StatisticsManager.purge();
                return;
            }
            String groupName = args.remove(0);
            StatisticsGroup group = StatisticsManager.findGroup(groupName);
            if (group == null)
                throw new CommandException("unknown group '%s'", groupName);
            if (args.isEmpty()) {
                group.purge();
                return;
            }
            String key = args.remove(0);
            Statistics stats = group.findStatistics(key);
            if (stats == null)
                throw new CommandException("unknown key '%s' in group '%s'", key, groupName);
            if (! stats.isValid())
                stats.purge();
            else
                ctx.warn("cached instance is not invalid");
            return;
        }

        if ("set".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            if (args.isEmpty())
                throw new CommandException("option value required");
            String value = args.remove(0);
            StatisticsManager.setOption(ctx, option, value);
            return;
        }

        if ("get".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            StatisticsManager.getOptions(ctx, option);
            return;
        }

        throw new CommandException("do what with stats?");
    }

}
