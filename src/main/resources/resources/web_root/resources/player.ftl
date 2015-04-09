<#escape x as x?html>
<#include "/resources/docStart.ftl">

<#import "/resources/macros.ftl" as macros>
<#import "/resources/sprites.ftl" as sprites>
<#import "/resources/inventory.ftl" as inventory>

<div id="player" class="grid_16">

<h1>${player.name}</h1>

<div id="skin" class="grid_4 alpha"></div>

<div id="info" class="grid_6">

    <h2>Player Info</h2>

    <table class="info">
        <tr><td class="name">Name:</td><td class="value">${player.name}</td></tr>
        <tr><td class="name">Level:</td><td class="value">
            ${player.level} (${(player.exp * 100)?round}% to level ${player.level + 1})
        </td></tr>
        <tr><td class="name">Total XP gained:</td><td class="value">${player.lifetimeExperience}</td></tr>

        <tr><td class="name">Health:</td><td class="value">
            <@macros.iconMeter baseSrc="heart" level=player.health/>
        </td></tr>
        <tr><td class="name">Food level:</td><td class="value">
            <@macros.iconMeter baseSrc="food" level=player.foodLevel/>
        </td></tr>
        <tr><td class="name">Game mode:</td><td class="value">${player.gameMode?lower_case?capitalize}</td></tr>

        <tr><td class="name">Under water:</td><td class="value"><#if (player.remainingAir < 300)>Yes<#else>No</#if></td></tr>
        <tr><td class="name">On fire:</td><td class="value"><#if (player.fireTicks > -20)>Yes<#else>No</#if></td></tr>

        <tr><td class="name">Server:</td><td class="value">${player.server}</td></tr>
        <tr><td class="name">World:</td><td class="value">${player.world}</td></tr>

        <tr><td class="name">Joins:</td><td class="value">${(player.joins)!"Never"}</td></tr>
        <tr><td class="name">First joined:</td><td class="value">${(player.firstJoin?datetime?string.long_short)!"Never"}</td></tr>
        <tr><td class="name">Last joined:</td><td class="value">${(player.lastJoin?datetime?string.long_short)!"Never"}</td></tr>
        <tr><td class="name">Quits:</td><td class="value">${(player.quits)!"Never"}</td></tr>
        <tr><td class="name">Last quit:</td><td class="value">${(player.lastQuit?datetime?string.long_short)!"Never"}</td></tr>
        <tr><td class="name">Kicks:</td><td class="value">${(player.kicks)!"Never"}</td></tr>
        <tr><td class="name">Last kick:</td><td class="value">${(player.lastKick?datetime?string.long_short)!"Never"}</td></tr>

        <tr><td class="name">Session play time:</td><td class="value"><@macros.elapsedTime time=player.sessionTime/></td></tr>
        <tr><td class="name">Total play time:</td><td class="value"><@macros.elapsedTime time=player.totalTime/></td></tr>
        <tr><td class="name">Online:</td><td class="value">${player.online?string("Yes", "No")}</td></tr>

    </table>

</div>

<div id="inventory" class="grid_6 omega">

    <h2>Inventory</h2>

    <table class="inventory stuff">
        <tr>
            <#list 9..17 as slot>
                <td class="slot">
                    <#if player.inventory[slot]?has_content>
                        <@inventory.slot item=player.inventory[slot]/>
                    <#else>
                        <@inventory.emptySlot/>
                    </#if>
                </td>
            </#list>
        </tr>
        <tr>
            <#list 18..26 as slot>
                <td class="slot">
                    <#if player.inventory[slot]?has_content>
                        <@inventory.slot item=player.inventory[slot]/>
                    <#else>
                        <@inventory.emptySlot/>
                    </#if>
                </td>
            </#list>
        </tr>
        <tr>
            <#list 27..35 as slot>
                <td class="slot">
                    <#if player.inventory[slot]?has_content>
                        <@inventory.slot item=player.inventory[slot]/>
                    <#else>
                        <@inventory.emptySlot/>
                    </#if>
                </td>
            </#list>
        </tr>
        <tr class="spacer"><td></td></tr>
        <tr>
            <#list 0..8 as slot>
                <td class="slot">
                    <#if player.inventory[slot]?has_content>
                        <@inventory.slot item=player.inventory[slot]/>
                    <#else>
                        <@inventory.emptySlot/>
                    </#if>
                </td>
            </#list>
        </tr>
    </table>
	<#if player.ender?has_content>
	    <h2>Ender Chest</h2>
	
	    <table class="inventory stuff">
	        <tr>
	            <#list 0..8 as slot>
	                <td class="slot">
	                    <#if player.ender[slot]?has_content>
	                        <@inventory.slot item=player.ender[slot]/>
	                    <#else>
	                        <@inventory.emptySlot/>
	                    </#if>
	                </td>
	            </#list>
	        </tr>
	        <tr>
	            <#list 9..17 as slot>
	                <td class="slot">
	                    <#if player.ender[slot]?has_content>
	                        <@inventory.slot item=player.ender[slot]/>
	                    <#else>
	                        <@inventory.emptySlot/>
	                    </#if>
	                </td>
	            </#list>
	        </tr>
	        <tr>
	            <#list 18..26 as slot>
	                <td class="slot">
	                    <#if player.ender[slot]?has_content>
	                        <@inventory.slot item=player.ender[slot]/>
	                    <#else>
	                        <@inventory.emptySlot/>
	                    </#if>
	                </td>
	            </#list>
	        </tr>
	    </table>
	</#if>
    <h2>Armor</h2>

    <table class="inventory armor">
        <tr>
            <#list 3..0 as slot>
                <td class="slot">
                    <#if player.armor[slot]?has_content>
                        <@inventory.slot item=player.armor[slot]/>
                    <#else>
                        <@inventory.emptySlot/>
                    </#if>
                </td>
            </#list>
        </tr>
    </table>

    <h2>Potion Effects</h2>
    <table class="potions">
        <#if player.potionEffects?has_content>
            <#list player.potionEffects as pe>
                <tr class="potion"><td class="name">${pe.type?lower_case?capitalize}</td></tr>
            </#list>
        <#else>
            <tr class="none"><td>None</td></tr>
        </#if>
    </table>

</div>
<div class="clear spacer"></div>

<div id="deaths" class="grid_4 alpha stats-block">
    <h2>Deaths</h2>
    <div class="baseStat">
        Total: ${player.deaths}
        <#if player.lastDeath?has_content>
            (last death on ${player.lastDeath?datetime?string.long_short}: ${player.lastDeathMessage!"Unknown"})
        </#if>
    </div>
    <#if player.deathCauses?has_content>
        <table class="deaths">
            <#list player.deathCauses?keys as cause>
                <tr><td class="name">${cause}</td><td class="value number">${player.deathCauses[cause]}</td></tr>
            </#list>
        </table>
    </#if>
</div>

<div id="playersKilled" class="grid_6 stats-block">
    <h2>PVP Kills</h2>
    <div class="grid_6 alpha omega baseStat">
        Total: ${player.totalPlayersKilled}
        <#if player.lastPlayerKill?has_content>
            (last killed ${player.lastPlayerKilled} on ${player.lastPlayerKill?datetime?string.long_short})
        </#if>
    </div>
    <div class="clear"></div>
    <div id="playersKilledByName" class="grid_3 alpha subCategory">
        <h3>Players</h3>
        <table class="kills">
            <#if player.playersKilled?has_content>
                <#list player.playersKilled?keys?sort as deadPlayer>
                    <tr><td class="name">${deadPlayer}</td><td class="value number">${player.playersKilled[deadPlayer]}</td></tr>
                </#list>
            <#else>
                <tr class="none"><td>None</td></tr>
            </#if>
        </table>
    </div>

    <div id="playersKilledByWeapon" class="grid_3 omega subCategory">
        <h3>Weapons</h3>
        <table class="kills">
            <#if player.playersKilledByWeapon?has_content>
                <#list player.playersKilledByWeapon?keys?sort as weapon>
                    <tr><td class="name">${weapon}</td><td class="value number">${player.playersKilledByWeapon[weapon]}</td></tr>
                </#list>
            <#else>
                <tr class="none"><td>None</td></tr>
            </#if>
        </table>
    </div>
</div>

<div id="mobsKilled" class="grid_6 omega stats-block">
    <h2>Mob Kills</h2>
    <div class="grid_6 alpha omega baseStat">
        Total: ${player.totalMobsKilled}
        <#if player.lastMobKill?has_content>
            (last killed a ${player.lastMobKilled} on ${player.lastMobKill?datetime?string.long_short})
        </#if>
    </div>
    <div class="clear"></div>
    <div id="playersKilledByType" class="grid_3 alpha subCategory">
        <h3>Mobs</h3>
        <table class="kills">
            <#if player.mobsKilled?has_content>
                <#list player.mobsKilled?keys?sort as mob>
                    <tr><td class="name">${mob}</td><td class="value number">${player.mobsKilled[mob]}</td></tr>
                </#list>
            <#else>
                <tr class="none"><td>None</td></tr>
            </#if>
        </table>
    </div>
    <div id="mobsKilledByWeapon" class="grid_3 omega subCategory">
        <h3>Weapons</h3>
        <table class="kills">
            <#if player.mobsKilledByWeapon?has_content>
                <#list player.mobsKilledByWeapon?keys?sort as weapon>
                    <tr><td class="name">${weapon}</td><td class="value number">${player.mobsKilledByWeapon[weapon]}</td></tr>
                </#list>
            <#else>
                <tr class="none"><td>None</td></tr>
            </#if>
        </table>
    </div>

</div>
<div class="clear spacer"></div>

<div id="blocks" class="grid_6 alpha stats-block">
    <h2>Blocks</h2>
    <div id="blocksPlaced" class="grid_3 alpha subCategory">
        <h3>Placed</h3>
        <div class="grid_3 alpha omega baseStat">
            Total: ${player.totalBlocksPlaced}
        </div>
        <div class="clear"></div>
        <table class="blocks">
            <#if player.blocksPlaced?has_content>
                <#list player.blocksPlaced?keys?sort as type>
                    <tr><td class="name">${type}</td><td class="value number">${player.blocksPlaced[type]}</td></tr>
                </#list>
            <#else>
                <tr class="none"><td>None</td></tr>
            </#if>
        </table>
    </div>
    <div id="blocksBroken" class="grid_3 omega subCategory">
        <h3>Broken</h3>
        <div class="grid_3 alpha omega baseStat">
            Total: ${player.totalBlocksBroken}
        </div>
        <div class="clear"></div>
        <table class="blocks">
            <#if player.blocksBroken?has_content>
                <#list player.blocksBroken?keys?sort as type>
                    <tr><td class="name">${type}</td><td class="value number">${player.blocksBroken[type]}</td></tr>
                </#list>
            <#else>
                <tr class="none"><td>None</td></tr>
            </#if>
        </table>
    </div>
</div>

<div id="items" class="grid_10 omega stats-block">
    <h2>Items</h2>
    <div id="itemsDropped" class="grid_3 alpha subCategory">
        <h3>Dropped</h3>
        <div class="grid_3 alpha omega baseStat">
            Total: ${player.totalItemsDropped}
        </div>
        <div class="clear"></div>
        <table class="itemsDropped">
            <#if player.itemsDropped?has_content>
                <#list player.itemsDropped?keys as type>
                    <tr><td class="name">${type}</td><td class="value number">${player.itemsDropped[type]}</td></tr>
                </#list>
            <#else>
                <tr class="none"><td>None</td></tr>
            </#if>
        </table>
    </div>
    <div id="itemsPickedUp" class="grid_3 subCategory">
        <h3>Picked Up</h3>
        <div class="grid_3 alpha omega baseStat">
            Total: ${player.totalItemsPickedUp}
        </div>
        <div class="clear"></div>
        <table class="itemsPickedUp">
            <#if player.itemsPickedUp?has_content>
                <#list player.itemsPickedUp?keys as type>
                    <tr><td class="name">${type}</td><td class="value number">${player.itemsPickedUp[type]}</td></tr>
                </#list>
            <#else>
                <tr class="none"><td>None</td></tr>
            </#if>
        </table>
    </div>
    <div id="itemsCrafted" class="grid_3 omega subCategory">
        <h3>Crafted</h3>
        <div class="grid_3 alpha omega baseStat">
            Total: ${player.totalItemsCrafted}
        </div>
        <div class="clear"></div>
        <table class="itemsCrafted">
            <#if player.itemsCrafted?has_content>
                <#list player.itemsCrafted?keys as type>
                    <tr><td class="name">${type}</td><td class="value number">${player.itemsCrafted[type]}</td></tr>
                </#list>
            <#else>
                <tr class="none"><td>None</td></tr>
            </#if>
        </table>
    </div>
</div>
<div class="clear spacer"></div>

<div id="distances" class="grid_8 alpha stats-block">
    <h2>Distance Traveled</h2>
    <div class="grid_3 alpha omega baseStat">
        Total: <@macros.distance distance=player.totalDistanceTraveled/>
    </div>
    <div class="clear"></div>
    <div id="travelDistances" class="grid_4 alpha subCategory">
        <h3>Method</h3>
        <table class="travelDistances">
            <#if player.travelDistances?has_content>
                <#list player.travelDistances?keys as type>
                    <tr><td class="name">${type}</td><td class="value number"><@macros.distance distance=player.travelDistances[type]/></td></tr>
                </#list>
            <#else>
                <tr class="none"><td>None</td></tr>
            </#if>
        </table>
    </div>
    <div id="biomeDistances" class="grid_4 omega subCategory">
        <h3>Biomes</h3>
        <table class="biomeDistances">
            <#if player.biomeDistances?has_content>
                <#list player.biomeDistances?keys as type>
                    <tr><td class="name">${type}</td><td class="value number"><@macros.distance distance=player.biomeDistances[type]/></td></tr>
                </#list>
            <#else>
                <tr class="none"><td>None</td></tr>
            </#if>
        </table>
    </div>
</div>

<div id="times" class="grid_8 omega stats-block">
    <h2>Time Spent</h2>
    <div id="travelTimes" class="grid_4 alpha subCategory">
        <h3>Traveling</h3>
        <table class="travelTimes">
            <#if player.travelTimes?has_content>
                <#list player.travelTimes?keys as type>
                    <tr><td class="name">${type}</td><td class="value number"><@macros.elapsedTime time=player.travelTimes[type]/></td></tr>
                </#list>
            <#else>
                <tr class="none"><td>None</td></tr>
            </#if>
        </table>
    </div>
    <div id="biomeTimes" class="grid_4 omega subCategory">
        <h3>Biomes</h3>
        <table class="biomeTimes">
            <#if player.biomeTimes?has_content>
                <#list player.biomeTimes?keys as type>
                    <tr><td class="name">${type}</td><td class="value number"><@macros.elapsedTime time=player.biomeTimes[type]/></td></tr>
                </#list>
            <#else>
                <tr class="none"><td>None</td></tr>
            </#if>
        </table>
    </div>
</div>
<div class="clear spacer"></div>

<div id="otherStats" class="grid_16 alpha omega stats-block">
    <h2>Other Statistics</h2>
    <div id="miscStats" class="grid_4 alpha subCategory">
        <table class="otherStats">
            <tr><td class="name">Times slept:</td><td class="value number">${player.timesSlept}</td></tr>
            <tr><td class="name">Arrows shot:</td><td class="value number">${player.arrowsShot}</td></tr>
            <tr><td class="name">Fires started:</td><td class="value number">${player.firesStarted}</td></tr>
            <tr><td class="name">Messages sent:</td><td class="value number">${player.chatMessages}</td></tr>
            <tr><td class="name">Portals crossed:</td><td class="value number">${player.portalsCrossed}</td></tr>
            <tr><td class="name">Water buckets filled:</td><td class="value number">${player.waterBucketsFilled}</td></tr>
            <tr><td class="name">Water buckets emptied:</td><td class="value number">${player.waterBucketsEmptied}</td></tr>
            <tr><td class="name">Lava buckets filled:</td><td class="value number">${player.lavaBucketsFilled}</td></tr>
            <tr><td class="name">Lava buckets emptied:</td><td class="value number">${player.lavaBucketsEmptied}</td></tr>
            <tr><td class="name">Cows milked:</td><td class="value number">${player.cowsMilked}</td></tr>
            <tr><td class="name">Mooshrooms milked:</td><td class="value number">${player.mooshroomsMilked}</td></tr>
            <tr><td class="name">Mooshrooms sheared:</td><td class="value number">${player.mooshroomsSheared}</td></tr>
            <tr><td class="name">Sheep sheared:</td><td class="value number">${player.sheepSheared}</td></tr>
            <tr><td class="name">Sheep dyed:</td><td class="value number">${player.sheepDyed}</td></tr>
            <tr><td class="name">Items enchanted:</td><td class="value number">${player.itemsEnchanted}</td></tr>
            <tr><td class="name">Levels spent enchanting:</td><td class="value number">${player.itemEnchantmentLevels}</td></tr>
        </table>
    </div>
    <div id="animalsTamed" class="grid_4 subCategory">
        <h3>Animals Tamed</h3>
        <table class="animalsTamed">
            <#if player.animalsTamed?has_content>
                <#list player.animalsTamed?keys as type>
                    <tr><td class="name">${type}</td><td class="value number">${player.animalsTamed[type]}</td></tr>
                </#list>
            <#else>
                <tr class="none"><td>None</td></tr>
            </#if>
        </table>
    </div>
    <div id="eggsThrown" class="grid_4 subCategory">
        <h3>Eggs Thrown</h3>
        <table class="eggsThrown">
            <#if player.eggsThrown?has_content>
                <#list player.eggsThrown?keys as type>
                    <tr><td class="name">${type}</td><td class="value number">${player.eggsThrown[type]}</td></tr>
                </#list>
            <#else>
                <tr class="none"><td>None</td></tr>
            </#if>
        </table>
    </div>
    <div id="foodEaten" class="grid_4 omega subCategory">
        <h3>Food Eaten</h3>
        <table class="foodEaten">
            <#if player.foodEaten?has_content>
                <#list player.foodEaten?keys as type>
                    <tr><td class="name">${type}</td><td class="value number">${player.foodEaten[type]}</td></tr>
                </#list>
            <#else>
                <tr class="none"><td>None</td></tr>
            </#if>
        </table>
    </div>
</div>

</div> <!-- player -->
<div class="clear spacer"></div>

<div id="playerFooter" class="grid_16">
    3D model library <a href="https://github.com/mrdoob/three.js/" target="_blank">Three.js</a><br/>
    Skin model and code adapated from work by <a href="http://djazz.mine.nu/" target="_blank">djazz</a><br/>
</div>

<script>
    PlayerSkin.setSkin('${playerName}');
</script>

<#include "/resources/docEnd.ftl">
</#escape>
