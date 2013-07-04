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
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.util.BukkitConfigUtil;
import com.forgenz.horses.HorseType;

public class HorsesConfig implements ForgeCore
{
	private ForgePlugin plugin;
	
	public final int summonTickDelay;
	public final int maxHorses;
	public final boolean allowRenameFromNameTag, requireNameTagForRenaming, deleteHorseOnDeath;
	
	public final boolean startWithSaddle;
	
	public final Pattern rejectedHorseNamePattern;
	
	
	public final Map<String, HorseTypeConfig> horseTypeConfigs;
	
	public HorsesConfig(ForgePlugin plugin)
	{
		this.plugin = plugin;
		
		// Fetch the main config
		FileConfiguration cfg = plugin.getConfig();
		
		summonTickDelay = BukkitConfigUtil.getAndSet(cfg, "SummonTickDelay", Number.class, 200).intValue();
		maxHorses = BukkitConfigUtil.getAndSet(cfg, "MaxHorses", Number.class, 5).intValue();
		
		allowRenameFromNameTag = BukkitConfigUtil.getAndSet(cfg, "AllowRenamedFromNameTag", Boolean.class, false);
		requireNameTagForRenaming = BukkitConfigUtil.getAndSet(cfg, "RequireNameTagForRenaming", Boolean.class, false);
		deleteHorseOnDeath = BukkitConfigUtil.getAndSet(cfg, "DeleteHorseOnDeath", Boolean.class, true);
		
		startWithSaddle = BukkitConfigUtil.getAndSet(cfg, "StartWithSaddle", Boolean.class, true);
		
		String defPattern = "f.?u.?c.?k|d.?[1i].?(c.?k?|c|k)|c.?u.?n.?t";
		String pattern = BukkitConfigUtil.getAndSet(cfg, "RejectedHorseNamePattern", String.class, defPattern);
		
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
		
		// Create the map for storing Horse Settings
		Map<String, HorseTypeConfig> horseTypeConfigs = new HashMap<String, HorseTypeConfig>();
		this.horseTypeConfigs = Collections.unmodifiableMap(horseTypeConfigs);
		
		// Iterate through each type and setup type configs
		// TODO Use the actual horse types
		for (HorseType type : HorseType.values())
		{
			ConfigurationSection sect = BukkitConfigUtil.getAndSetConfigurationSection(cfg, "Horses.Types." + type);
			
			horseTypeConfigs.put(type.toString(), new HorseTypeConfig(plugin, sect));			
		}
	}
	
	public HorseTypeConfig getHorseTypeConfig(HorseType type)
	{
		return horseTypeConfigs.get(type.toString());
	}

	@Override
	public ForgePlugin getPlugin()
	{
		return plugin;
	}
}
