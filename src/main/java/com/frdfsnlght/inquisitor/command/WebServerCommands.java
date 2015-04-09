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
import com.frdfsnlght.inquisitor.WebServer;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class WebServerCommands  extends CommandProcessor {

    private static final String GROUP = "webserver ";

    @Override
    public boolean matches(Context ctx, Command cmd, List<String> args) {
        return super.matches(ctx, cmd, args) &&
               GROUP.startsWith(args.get(0).toLowerCase());
    }

    @Override
    public List<String> getUsage(Context ctx) {
        List<String> cmds = new ArrayList<String>();
        cmds.add(getPrefix(ctx) + GROUP + "status");
        cmds.add(getPrefix(ctx) + GROUP + "enable");
        cmds.add(getPrefix(ctx) + GROUP + "disable");
        cmds.add(getPrefix(ctx) + GROUP + "get <option>|*");
        cmds.add(getPrefix(ctx) + GROUP + "set <option> <value>");
        cmds.add(getPrefix(ctx) + GROUP + "add <option> <value>");
        cmds.add(getPrefix(ctx) + GROUP + "remove <option> <value>");

        return cmds;
    }

    @Override
    public void process(Context ctx, Command cmd, List<String> args) throws InquisitorException {
        args.remove(0);
        if (args.isEmpty())
            throw new CommandException("do what with the web server?");
        String subCmd = args.get(0).toLowerCase();
        args.remove(0);

        if ("status".startsWith(subCmd)) {
            Permissions.require(ctx.getPlayer(), "inq.webserver.status");
            ctx.send("web server %s started", WebServer.isStarted() ? "is" : "is not");
            return;
        }

        if ("set".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            if (args.isEmpty())
                throw new CommandException("option value required");
            String value = args.remove(0);
            WebServer.setOption(ctx, option, value);
            return;
        }

        if ("get".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            WebServer.getOptions(ctx, option);
            return;
        }

        if ("add".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            if (args.isEmpty())
                throw new CommandException("option value required");
            String value = args.remove(0);
            WebServer.addOption(ctx, option, value);
            return;
        }

        if ("remove".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            if (args.isEmpty())
                throw new CommandException("option value required");
            String value = args.remove(0);
            WebServer.removeOption(ctx, option, value);
            return;
        }

        if ("enable".startsWith(subCmd)) {
            Permissions.require(ctx.getPlayer(), "inq.webserver.enable");
            WebServer.setEnabled(true);
            return;
        }

        if ("disable".startsWith(subCmd)) {
            Permissions.require(ctx.getPlayer(), "inq.webserver.disable");
            WebServer.setEnabled(false);
            return;
        }

        throw new CommandException("do what with the web server?");
    }

}
