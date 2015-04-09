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

import com.frdfsnlght.inquisitor.command.CommandProcessor;
import com.frdfsnlght.inquisitor.command.DBCommands;
import com.frdfsnlght.inquisitor.command.GlobalCommands;
import com.frdfsnlght.inquisitor.command.HelpCommand;
import com.frdfsnlght.inquisitor.command.PlayersCommands;
import com.frdfsnlght.inquisitor.command.StatsCommands;
import com.frdfsnlght.inquisitor.command.WebServerCommands;
import com.frdfsnlght.inquisitor.test.TestCommands;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Global {

    public static boolean enabled = false;
    public static Inquisitor plugin = null;
    public static String pluginName;
    public static String pluginVersion;
    public static boolean started = false;

    public static final List<CommandProcessor> commands = new ArrayList<CommandProcessor>();

    static {
        commands.add(new HelpCommand());
        commands.add(new GlobalCommands());
        commands.add(new DBCommands());
        commands.add(new StatsCommands());
        commands.add(new PlayersCommands());
        commands.add(new WebServerCommands());

        if (isTesting()) {
            System.out.println("**** Inquisitor testing mode is enabled! ****");
            commands.add(new TestCommands());
        }

    }

    public static boolean isTesting() {
    	//return true;
        return System.getenv("INQUISITOR_TEST") != null;
    }

}
