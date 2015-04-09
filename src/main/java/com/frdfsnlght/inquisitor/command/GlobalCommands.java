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

import com.frdfsnlght.inquisitor.Config;
import com.frdfsnlght.inquisitor.Context;
import com.frdfsnlght.inquisitor.Global;
import com.frdfsnlght.inquisitor.InquisitorException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class GlobalCommands extends CommandProcessor {

    @Override
    public boolean matches(Context ctx, Command cmd, List<String> args) {
        if (! ctx.isOp()) return false;
        return super.matches(ctx, cmd, args) && (
               ("status".startsWith(args.get(0).toLowerCase())) ||
               ("get".startsWith(args.get(0).toLowerCase())) ||
               ("set".startsWith(args.get(0).toLowerCase()))
            );
    }

    @Override
    public List<String> getUsage(Context ctx) {
        List<String> cmds = new ArrayList<String>();
        if (ctx.isOp()) {
            cmds.add(getPrefix(ctx) + "status");
            cmds.add(getPrefix(ctx) + "get <option>|*");
            cmds.add(getPrefix(ctx) + "set <option> <value>");
        }
        return cmds;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(Context ctx, Command cmd, List<String> args) throws InquisitorException {
        if (args.isEmpty())
            throw new CommandException("do what?");
        String subCmd = args.remove(0).toLowerCase();

        if ("status".startsWith(subCmd)) {
            Global.plugin.getServer().dispatchCommand(ctx.getSender(), "inq db status");
            Global.plugin.getServer().dispatchCommand(ctx.getSender(), "inq stats status");
            Global.plugin.getServer().dispatchCommand(ctx.getSender(), "inq webserver status");
            return;
        }

        if ("set".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            if (args.isEmpty())
                throw new CommandException("option value required");
            String value = args.remove(0);
            Config.setOption(ctx, option, value);
            return;
        }

        if ("get".startsWith(subCmd)) {
            if (args.isEmpty())
                throw new CommandException("option name required");
            String option = args.remove(0);
            Config.getOptions(ctx, option);
        }

    }

}
