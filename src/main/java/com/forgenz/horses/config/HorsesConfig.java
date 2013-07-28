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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.horses.HorseType;
import com.forgenz.horses.Horses;
import com.forgenz.horses.database.HorseDatabaseStorageType;

public class HorsesConfig extends AbstractConfig implements ForgeCore
{	
	public final WorldGuardConfig worldGuardCfg;
	
	private final HorsesWorldConfig globalCfg;
	private final Map<String, HorsesWorldConfig> worldConfigs;
	
	public final HorseDatabaseStorageType databaseType;
	
	public final boolean showAuthor;
	public final boolean forceEnglishCharacters;
	
	public final boolean fixZeroJumpStrength;
	
	public final Pattern rejectedHorseNamePattern;
	
	public HorsesConfig(Horses plugin)
	{
		super(plugin, (YamlConfiguration) plugin.getConfig(), null, null, null, false);
		
		YamlConfiguration cfg = this.loadConfiguration();
		
		this.initializeHeader();
		this.addResourseToHeader("header_main.txt");
		
		Map<String, HorsesWorldConfig> worldConfigs = new HashMap<String, HorsesWorldConfig>();
		this.worldConfigs = Collections.unmodifiableMap(worldConfigs);
		
		List<String> worlds = cfg.getStringList("WorldConfigs");
		for (String world : worlds)
		{
			worldConfigs.put(world, new HorsesWorldConfig(plugin, world.toLowerCase()));
		}
		cfg.set("WorldConfigs", null);
		cfg.set("WorldConfigs", worlds);
		
		String dbString = getAndSet("DatabaseType", HorseDatabaseStorageType.YAML.toString(), String.class).toUpperCase();
		HorseDatabaseStorageType databaseType = HorseDatabaseStorageType.getFromString(dbString);
		if (databaseType == null)
		{
			getPlugin().severe("Invalid database type %s", dbString);
			plugin.severe("#################################");
			plugin.severe("Falling back to a dummy database");
			plugin.severe("WARNING: No data will be saved");
			plugin.severe("#################################");
			databaseType = HorseDatabaseStorageType.DUMMY;
		}
		this.databaseType = databaseType;
		
		if (getAndSet("EnableWorldGuardIntegration", false, Boolean.class))
			worldGuardCfg = new WorldGuardConfig(plugin);
		else
			worldGuardCfg = null;
		
		showAuthor = getAndSet("ShowAuthorInCommand", true, Boolean.class);
		forceEnglishCharacters = getAndSet("ForceEnglishCharacters", true, Boolean.class);
		
		String defPattern = "f.?u.?c.?k|d.?[1i].?(c.?k?|c|k)|c.?u.?n.?t";
		String pattern = getAndSet("RejectedHorseNamePattern", defPattern, String.class);
		
		Pattern testPattern = null;
		try
		{
			testPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		}
		catch (IllegalArgumentException e)
		{
			getPlugin().log(Level.WARNING, "Invalid pattern for name rejection", e);
			testPattern = Pattern.compile(defPattern);
		}
		finally
		{
			rejectedHorseNamePattern = testPattern;
		}
		
		fixZeroJumpStrength = getAndSet("FixZeroJumpStrength", true, Boolean.class);
		
		// Finally setup the global config
		globalCfg = new HorsesWorldConfig(plugin, cfg);
		
		
		// Delete the reference to the config
		this.saveConfiguration();
	}
	
	public HorsesWorldConfig getWorldConfig(World world)
	{
		if (world == null)
			return globalCfg;
		
		
		HorsesWorldConfig cfg = worldConfigs.get(world.getName().toLowerCase());
		return cfg != null ? cfg : globalCfg;
	}
	
	public HorsesPermissionConfig getPermConfig(Player player)
	{
		HorsesWorldConfig cfg = player != null ? getWorldConfig(player.getWorld()) : null;
		
		if (cfg == null)
			cfg = globalCfg;
		
		return cfg.getPermConfig(player);
	}
	
	public HorseTypeConfig getHorseTypeConfig(Player player, HorseType type)
	{
		return getPermConfig(player).getHorseTypeConfig(type);
	}
	
	public HorseTypeConfig getHorseTypeConfigLike(Player player, String like)
	{
		return getPermConfig(player).getHorseTypeConfigLike(like);
	}
	
	public HorseTypeConfig getHorseTypeConfig(Player player, String typeStr)
	{
		return getPermConfig(player).getHorseTypeConfig(typeStr);
	}
	
	public String getStableGroup(World world)
	{
		return getWorldConfig(world).stableGroup;
	}
	
	public boolean isProtecting()
	{
		for (HorsesWorldConfig worldCfg : worldConfigs.values())
		{
			if (worldCfg.isProtecting())
				return true;
		}
		
		return globalCfg.isProtecting();
	}

	/**
	 * @return true if players movements should be tracked
	 */
	public boolean trackMovements()
	{
		for (HorsesWorldConfig worldCfg : worldConfigs.values())
		{
			for (HorsesPermissionConfig permCfg : worldCfg.permissionConfigs.values())
			{
				if (permCfg.allowSummonCommand)
				{
					return true;
				}
			}
		}
		
		return false;
	}
}
