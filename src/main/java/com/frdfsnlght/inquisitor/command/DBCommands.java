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
import com.frdfsnlght.inquisitor.DB;
import com.frdfsnlght.inquisitor.InquisitorException;
import com.frdfsnlght.inquisitor.Permissions;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class DBCommands  extends CommandProcessor {

    private static final String GROUP = "db ";

    @Override
    public boolean matches(Context ctx, Command cmd, List<String> args) {
        return super.matches(ctx, cmd, args) &&
               GROUP.startsWith(args.get(0).toLowerCase());
    }

    @Override
    public List<String> getUsage(Context ctx) {
        List<String> cmds = new ArrayList<String>();
        cmds.add(getPrefix(ctx) + GROUP + "status");
        cmds.add(getPrefix(ctx) + GROUP + "get <option>|*");
        cmds.add(getPrefix(ctx) + GROUP + "set <option> <value>");

        return cmds;
    }

    @Override
    public void process(Context ctx, Command cmd, List<String> args) throws InquisitorException {
        args.remove(0);
        if (args.isEmpty())
            throw new CommandException("do what with the database?");
        String subCmd = args.get(0).toLowerCase();
        args.remove(0);

        if ("status".startsWith(subCmd)) {
            Permissions.require(ctx.getPlayer(), "inq.db.status");
            ctx.send("database %s connected", DB.isConnected() ? "is" : "is not");
            return;
        }

        if ("set".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            if (args.isEmpty())
                throw new CommandException("option value required");
            String value = args.remove(0);
            DB.setOption(ctx, option, value);
            return;
        }

        if ("get".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            DB.getOptions(ctx, option);
            return;
        }

        throw new CommandException("do what with the database?");
    }

}
