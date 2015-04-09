<#include "spriteData.ftl">
<#import "sprites.ftl" as sprite>

<#macro "slot" item>
    <#local sid="${item.type?c}-${item.data?c}">
    <#if sprites[sid]?has_content>
        <#local info=sprites[sid]>
    <#else>
        <#local sid="${item.type?c}-${item.durability?c}">
        <#if sprites[sid]?has_content>
            <#local info=sprites[sid]>
        <#else>
            <#local sid="${item.type?c}-0">
            <#if sprites[sid]?has_content>
                <#local info=sprites[sid]>
            <#else>
                <#local info="">
            </#if>
        </#if>
    </#if>
    <#if info?has_content>
        <#if info[2]?has_content && (! title?has_content)>
            <#local title=info[2]>
        </#if>
        <#if item.enchantments?has_content>
            <#list item.enchantments?keys?sort as type>
                <#local value=item.enchantments[type]>
                <#if enchantments[type]??>
                    <#local name=enchantments[type]>
                    <#local level="">
                    <#if (value == 1)><#local level="I"></#if>
                    <#if (value == 2)><#local level="II"></#if>
                    <#if (value == 3)><#local level="III"></#if>
                    <#if (value == 4)><#local level="IV"></#if>
                    <#if (value == 5)><#local level="V"></#if>
                    <#local title="${title}&#10;${name} ${level}">
                </#if>
            </#list>
        </#if>
        <div class="slot <#if item.enchantments?has_content>enchanted</#if>" title="${title}">
            <@sprite.sprite id=sid/>
            <#if (item.amount > 1)><div class="amount">${item.amount}</div></#if>
            <#if (item.durability > 0) && durabilities[sid]?has_content>
                <#local damage=(100 * (1 - (item.durability / durabilities[item.type?c])))>
                <div class="damage">
                    <div class="bar <#if (damage > 10)>normal<#else>critical</#if>" style="width:${damage}%">&nbsp;</div>
                </div>
            </#if>
        </div>
    <#else>
        <@emptySlot/>
    </#if>
</#macro>

<#macro "emptySlot">
    <div class="slot empty" title="Empty">&nbsp;</div>
</#macro>

<#assign durabilities = {
"256": 251,
"257": 251,
"258": 251,
"259": 65,
"261": 385,
"267": 251,
"268": 60,
"269": 60,
"270": 60,
"271": 60,
"272": 132,
"273": 132,
"274": 132,
"275": 132,
"276": 1562,
"277": 1562,
"278": 1562,
"279": 1562,
"283": 33,
"284": 33,
"285": 33,
"286": 33,
"290": 60,
"291": 132,
"292": 251,
"293": 1562,
"294": 33,
"298": 56,
"299": 82,
"300": 76,
"301": 66,
"302": 78,
"303": 114,
"304": 106,
"305": 92,
"306": 166,
"307": 242,
"308": 226,
"309": 196,
"310": 364,
"311": 529,
"312": 496,
"313": 430,
"314": 78,
"315": 114,
"316": 106,
"317": 92,
"346": 65,
"359": 239,
"398": 25
}>

<#assign enchantments = {
    "PROTECTION_ENVIRONMENTAL": "Protection",
    "PROTECTION_FIRE": "Fire Protection",
    "PROTECTION_FALL": "Feather Falling",
    "PROTECTION_EXPLOSIONS": "Blast Protection",
    "PROTECTION_PROJECTILE": "Projectile Protection",
    "OXYGEN": "Respiration",
    "WATER_WORKER": "Aqua Affinity",
    "DAMAGE_ALL": "Sharpness",
    "DAMAGE_UNDEAD": "Smite",
    "DAMAGE_ARTHROPODS": "Bane of Arthropods",
    "KNOCKBACK": "Knockback",
    "FIRE_ASPECT": "Fire Aspect",
    "LOOT_BONUS_MOBS": "Looting",
    "DIG_SPEED": "Efficiency",
    "SILK_TOUCH": "Silk Touch",
    "DURABILITY": "Unbreaking",
    "LOOT_BONUS_BLOCKS": "Fortune",
    "ARROW_DAMAGE": "Power",
    "ARROW_KNOCKBACK": "Punch",
    "ARROW_FIRE": "Flame",
    "ARROW_INFINITE": "Infinity",
	"LUCK": "Luck of the Sea",
	"LURE": "Lure"
}>

