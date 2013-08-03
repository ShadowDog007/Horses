/*
 * Copyright 2013 Michael McKnight. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package com.forgenz.horses.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.forgenz.horses.HorseType;
import com.forgenz.horses.Horses;

public class HorsesPermissionConfig extends AbstractConfig
{		
	/** If true horses will bypass spawn protection */
	public final boolean bypassSpawnProtection;
	
	/** If true the players horse will be dismissed upon the owner teleporting more than 32 blocks away */
	public final boolean dismissHorseOnTeleport;
	
	/** If true bought horses start with a saddle */
	public final boolean startWithSaddle;
	
	/** The maximum number of horses the player can have in their stable */
	public final int maxHorses;
	
	/** The delay in seconds a player has to wait for their horse to be summoned */
	public final int summonDelay;
	
	/** If true summons will be canceled if the player moves */
	public final boolean cancelSummonOnMove;
	
	public final Map<String, HorseTypeConfig> horseTypeConfigs;
	
	// ######## COMMANDS ######## //
	public final boolean allowBuyCommand;
	
	public final boolean allowDeleteCommand;
	
	public final boolean allowDismissCommand;
	
	public final boolean allowHealCommand;
	
	public final boolean allowListCommand;
	
	public final boolean allowRenameCommand;
	
	public final boolean allowSummonCommand;
	
	public final boolean allowTypesCommand;
	
	// ######## RENAMING ######## //	
	/** If true players will be unable to rename wild horses */
	public final boolean blockRenamingOnWildHorses;
	
	/** If true players will claim horses by right clicking them with Name Tags */
	public final boolean allowClaimingWithNameTag;
	
	/** If true players will be able to rename their stabled horses using a name tag */
	public final boolean allowRenameFromNameTag;
	
	/** If true players will be forced to hold a name tag in order to rename their */
	public final boolean requireNameTagForRenaming;
	
	/** The maximum length of a horses name */
	public final int maxHorseNameLength;
	
	// ######## DAMAGE ######## //
	/** If true horses can't take any damage */
	public final boolean invincibleHorses;
	
	/**
	 * Horses are protected from the given damage causes.
	 * See http://jd.bukkit.org/rb/apidocs/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html
	 */
	public final Set<DamageCause> protectedDamageCauses;
	
	/** If true horses are protected from their owners */
	public final boolean protectFromOwner;
	
	/** If true horses are protected from players */
	public final boolean protectFromPlayers;
	
	/** If true horses are protected from mobs */
	public final boolean protectFromMobs;
	
	/** If true horses will be protected if in the players current position they would be protected */
	public final boolean onlyHurtHorseIfOwnerCanBeHurt;
	
	/** Transfers damage to a players horse to the player riding it */
	public final boolean transferDamageToRider;
	
	/** If true horses will be deleted from the players stable upon death */
	public final boolean deleteHorseOnDeath;
	
	/** If true horses will be deleted from the players stable upon death by a player (Overridden by deleteHorseOnDeath) */
	public final boolean deleteHorseOnDeathByPlayer;
	
	/** If true horses will keep their equipment when they die else it is dropped to the ground */
	public final boolean keepEquipmentOnDeath;
	
	/** Time in ms before a players horse can be summoned after it has died */
	public final long deathCooldown;
	
	public HorsesPermissionConfig(Horses plugin, YamlConfiguration cfg)
	{
		this(plugin, cfg, null, null, false);
	}
	
	public HorsesPermissionConfig(Horses plugin, AbstractConfig parent, String permission)
	{
		this(plugin, null, parent, permission, true);
	}
	
	private HorsesPermissionConfig(Horses plugin, YamlConfiguration cfg, AbstractConfig parent, String permission, boolean standalone)
	{
		super(plugin, cfg, parent, null, permission, standalone);
		
		Configuration config = this.loadConfiguration();
		
		this.addResourseToHeader("header_permission.txt");
		
		// Misc
		{
			bypassSpawnProtection = getAndSet("BypassSpawnProtection", false, Boolean.class);
			// This setting is currently forced to true due to issues with teleporting horses
			dismissHorseOnTeleport = getAndSet("DismissHorseOnTeleport", true, Boolean.class) || true;
			startWithSaddle = getAndSet("StartWithSaddle", true, Boolean.class);
			
			maxHorses = getAndSet("MaxHorses", 5, Number.class).intValue();
			summonDelay = getAndSet("SummonDelay", 10, Number.class).intValue();
			cancelSummonOnMove = getAndSet("CancelSummonOnMove", true, Boolean.class);
		}
		
		// Commands
		{
			ConfigurationSection sect = getConfigSect("Commands");
			
			allowBuyCommand = getAndSet(sect, "AllowBuyCommand", config.getBoolean("AllowBuyCommand", true), Boolean.class);
			config.set("AllowBuyCommand", null);
			allowDeleteCommand = getAndSet(sect, "AllowDeleteCommand", config.getBoolean("AllowDeleteCommand", true), Boolean.class);
			config.set("AllowDeleteCommand", null);
			allowDismissCommand = getAndSet(sect, "AllowDismissCommand", config.getBoolean("AllowDismissCommand", true), Boolean.class);
			config.set("AllowDismissCommand", null);
			allowHealCommand = getAndSet(sect, "AllowHealCommand", config.getBoolean("AllowHealCommand", true), Boolean.class);
			config.set("AllowHealCommand", null);
			allowListCommand = getAndSet(sect, "AllowListCommand", config.getBoolean("AllowListCommand", true), Boolean.class);
			config.set("AllowListCommand", null);
			allowRenameCommand = getAndSet(sect, "AllowRenameCommand", config.getBoolean("AllowRenameCommand", true), Boolean.class);
			config.set("AllowRenameCommand", null);
			allowSummonCommand = getAndSet(sect, "AllowSummonCommand", config.getBoolean("AllowSummonCommand", true), Boolean.class);
			config.set("AllowSummonCommand", null);
			allowTypesCommand = getAndSet(sect, "AllowTypesCommand", config.getBoolean("AllowTypesCommand", true), Boolean.class);
			config.set("AllowTypesCommand", null);
		}
		
		// Renaming
		{
			ConfigurationSection sect = getConfigSect("Renaming");
			
			blockRenamingOnWildHorses = getAndSet(sect, "BlockRenamingOnWildHorses", false, Boolean.class);
			allowClaimingWithNameTag = getAndSet(sect, "AllowClaimingWithNameTag", false, Boolean.class);
			allowRenameFromNameTag = getAndSet(sect, "AllowRenamingFromNameTag", false, Boolean.class);
			requireNameTagForRenaming = getAndSet(sect, "RequireNameTagForRenaming", false, Boolean.class);
			
			int maxHorseNameLength = getAndSet(sect, "MaxHorseNameLength", 20, Number.class).intValue();
			if (maxHorseNameLength > 30)
				maxHorseNameLength = 30;
			this.maxHorseNameLength = maxHorseNameLength;
		}		
		
		// Damage
		{
			ConfigurationSection sect = getConfigSect("Damage");
			
			HashSet<DamageCause> protectedDamageCauses = new HashSet<DamageCause>();
			this.protectedDamageCauses = Collections.unmodifiableSet(protectedDamageCauses);
			
			List<String> causeList = sect.getStringList("ProtectedDamageCauses");
			for (int i = 0; i < causeList.size(); ++i)
			{
				String causeStr = causeList.get(i);
				for (DamageCause cause : DamageCause.values())
				{
					if (cause.toString().equalsIgnoreCase(causeStr))
					{
						causeList.set(i, cause.toString());
						protectedDamageCauses.add(cause);
					}
				}
			}
			sect.set("ProtectedDamageCauses", causeList);
			
			invincibleHorses = getAndSet(sect, "InvincibleHorses", false, Boolean.class);
			protectFromOwner = getAndSet(sect, "ProtectFromOwner", true, Boolean.class);
			protectFromPlayers = getAndSet(sect, "ProtectFromPlayers", true, Boolean.class);
			protectFromMobs = getAndSet(sect, "ProtectFromMobs", true, Boolean.class);
			onlyHurtHorseIfOwnerCanBeHurt = getAndSet(sect, "OnlyHurtHorseIfOwnerCanBeHurt", true, Boolean.class);
			transferDamageToRider = getAndSet(sect, "TransferDamageToRider", true, Boolean.class);
			
			deleteHorseOnDeath = getAndSet(sect, "DeleteHorseOnDeath", false, Boolean.class);
			deleteHorseOnDeathByPlayer = getAndSet(sect, "DeleteHorseOnDeathByPlayer", false, Boolean.class);
			keepEquipmentOnDeath = getAndSet(sect, "KeepEquipmentOnDeath", false, Boolean.class);
			
			deathCooldown = getAndSet(sect, "DeathCooldown", 120, Number.class).longValue() * 1000L;
		}
		
		
		// Types
		Map<String, HorseTypeConfig> horseTypeConfigs = new HashMap<String, HorseTypeConfig>();
		this.horseTypeConfigs = Collections.unmodifiableMap(horseTypeConfigs);
		
		ConfigurationSection typeSect = getConfigSect("Types");
		
		// Temporary fix for broken horse type
		set(typeSect, HorseType.PaintCreamy.toString(), getConfigSect(typeSect, "PaintCREAMY"));
		set(typeSect, "PaintCREAMY", null);
		
		for (HorseType type : HorseType.values())
		{
			ConfigurationSection sect = getConfigSect(typeSect, type.toString());
			
			horseTypeConfigs.put(type.toString(), new HorseTypeConfig(plugin, sect, type));
		}
		
		
		this.saveConfiguration();
	}
	
	public HorseTypeConfig getHorseTypeConfig(HorseType type)
	{
		return horseTypeConfigs.get(type.toString());
	}
	
	public HorseTypeConfig getHorseTypeConfigLike(String like)
	{
		like = like.toLowerCase();
		
		for (HorseType type : HorseType.values())
		{
			HorseTypeConfig cfg = getHorseTypeConfig(type);
			if (cfg.displayName.toLowerCase().startsWith(like))
			{
				return cfg;
			}
		}
		return null;
	}
	
	public HorseTypeConfig getHorseTypeConfig(String typeStr)
	{		
		for (HorseType type : HorseType.values())
		{
			HorseTypeConfig cfg = getHorseTypeConfig(type);
			if (cfg.displayName.equalsIgnoreCase(typeStr))
			{
				return cfg;
			}
		}
		return null;
	}
	
	public boolean isProtecting()
	{
		return invincibleHorses || protectFromOwner || protectFromPlayers || protectFromMobs || protectedDamageCauses.size() > 0;
	}
}
