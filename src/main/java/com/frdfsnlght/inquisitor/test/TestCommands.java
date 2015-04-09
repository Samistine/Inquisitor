/*
 * Copyright 2011 frdfsnlght <frdfsnlght@gmail.com>.
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
package com.frdfsnlght.inquisitor.test;

import com.frdfsnlght.inquisitor.Context;
import com.frdfsnlght.inquisitor.InquisitorException;
import com.frdfsnlght.inquisitor.Statistic;
import com.frdfsnlght.inquisitor.Statistic.Type;
import com.frdfsnlght.inquisitor.Statistics;
import com.frdfsnlght.inquisitor.StatisticsGroup;
import com.frdfsnlght.inquisitor.StatisticsManager;
import com.frdfsnlght.inquisitor.Utils;
import com.frdfsnlght.inquisitor.command.CommandException;
import com.frdfsnlght.inquisitor.command.CommandProcessor;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class TestCommands extends CommandProcessor {

    private static final String GROUP = "test ";

    @Override
    public boolean matches(Context ctx, Command cmd, List<String> args) {
        return super.matches(ctx, cmd, args) &&
               GROUP.startsWith(args.get(0).toLowerCase());
    }

    @Override
    public List<String> getUsage(Context ctx) {
        List<String> cmds = new ArrayList<String>();
        cmds.add(getPrefix(ctx) + GROUP + "enchant");
        cmds.add(getPrefix(ctx) + GROUP + "stats");
        return cmds;
    }

    @Override
    public void process(final Context ctx, Command cmd, List<String> args)  throws InquisitorException {
        args.remove(0);
        if (args.isEmpty())
            throw new CommandException("test what?");
        String subCmd = args.remove(0).toLowerCase();

        if ("enchant".startsWith(subCmd)) {
            if (! ctx.isPlayer())
                throw new CommandException("this command is only available to players");
            if (args.isEmpty())
                throw new CommandException("enchantment type required");
            String typeStr = args.remove(0);
            Enchantments enchs;
            try {
                enchs = Utils.valueOf(Enchantments.class, typeStr);
            } catch (IllegalArgumentException e) {
                throw new CommandException("%s value for enchantment type", e.getMessage());
            }
            int level = 1;
            if (! args.isEmpty()) {
                String levelStr = args.remove(0);
                try {
                    level = Integer.parseInt(levelStr);
                } catch (NumberFormatException nfe) {
                    throw new CommandException("%s is not a valid level", levelStr);
                }
            }
            ItemStack is = ctx.getPlayer().getItemInHand();
            Enchantment ench = enchs.enchantment;
            if (! ench.canEnchantItem(is))
                throw new CommandException("%s cannot be enchanted with %s", is.getType(), enchs);
            if (ench.getMaxLevel() < level)
                throw new CommandException("%s cannot be used at that level", enchs);
            is.addEnchantment(ench, level);
            ctx.send("TADA!");
            return;
        }

        if ("stats".startsWith(subCmd)) {
            if (statsGroup != null)
                StatisticsManager.removeGroup(statsGroup);
            if (statsGroup == null) {
                statsGroup = new StatisticsGroup("stats", "name", Type.STRING, 30);
                statsGroup.addStatistic(new Statistic("blahs", Type.INTEGER, false));
            }
            StatisticsManager.addGroup(statsGroup);

            Statistics stats = statsGroup.getStatistics("tab");
            stats.incr("blahs");
            stats.flush();

            return;
        }

        throw new CommandException("test what?");
    }

    private StatisticsGroup statsGroup = null;

    private enum Enchantments {

        PROTECTION_ENVIRONMENTAL(Enchantment.PROTECTION_ENVIRONMENTAL),
        PROTECTION_FIRE(Enchantment.PROTECTION_FIRE),
        PROTECTION_FALL(Enchantment.PROTECTION_FALL),
        PROTECTION_EXPLOSIONS(Enchantment.PROTECTION_EXPLOSIONS),
        PROTECTION_PROJECTILE(Enchantment.PROTECTION_PROJECTILE),
        OXYGEN(Enchantment.OXYGEN),
        WATER_WORKER(Enchantment.WATER_WORKER),
        DAMAGE_ALL(Enchantment.DAMAGE_ALL),
        DAMAGE_UNDEAD(Enchantment.DAMAGE_UNDEAD),
        DAMAGE_ARTHROPODS(Enchantment.DAMAGE_ARTHROPODS),
        KNOCKBACK(Enchantment.KNOCKBACK),
        FIRE_ASPECT(Enchantment.FIRE_ASPECT),
        LOOT_BONUS_MOBS(Enchantment.LOOT_BONUS_MOBS),
        DIG_SPEED(Enchantment.DIG_SPEED),
        SILK_TOUCH(Enchantment.SILK_TOUCH),
        DURABILITY(Enchantment.DURABILITY),
        LOOT_BONUS_BLOCKS(Enchantment.LOOT_BONUS_BLOCKS),
        ARROW_DAMAGE(Enchantment.ARROW_DAMAGE),
        ARROW_KNOCKBACK(Enchantment.ARROW_KNOCKBACK),
        ARROW_FIRE(Enchantment.ARROW_FIRE),
        ARROW_INFINITE(Enchantment.ARROW_INFINITE);

        Enchantment enchantment;

        Enchantments(Enchantment ench) {
            this.enchantment = ench;
        }
    }

}
