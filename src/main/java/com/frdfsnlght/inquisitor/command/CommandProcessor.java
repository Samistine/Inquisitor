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
import java.util.List;
import org.bukkit.command.Command;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public abstract class CommandProcessor {

    public boolean matches(Context ctx, Command cmd, List<String> args) {
        if (! cmd.getName().toLowerCase().equals("inq")) return false;
        if (args.isEmpty()) return false;
        return true;
    }

    protected String getPrefix(Context ctx) {
        return (ctx.isPlayer() ? "/" : "") + "inq ";
    }

    public abstract void process(Context ctx, Command cmd, List<String> args) throws InquisitorException;
    public abstract List<String> getUsage(Context ctx);

    protected String rebuildCommandArgs(List<String> args) {
        StringBuilder b = new StringBuilder();
        for (String arg : args) {
            if (arg.contains(" "))
                b.append("\"").append(arg).append("\"");
            else
                b.append(arg);
            b.append(" ");
        }
        return b.toString().trim();
    }

}
