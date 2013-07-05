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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.forgecore.v1_0.util.BukkitConfigUtil;
import com.forgenz.horses.Horses;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardConfig implements ForgeCore
{
	private final Horses plugin;
	
	public final Set<String> commandSummonAllowedRegions, commandDismissAllowedRegions, commandBuyAllowedRegions, commandSellAllowedRegions;
	
	@SuppressWarnings("unchecked")
	public WorldGuardConfig(Horses plugin)
	{
		this.plugin = plugin;
		
		ConfigurationSection cfg = BukkitConfigUtil.getAndSetConfigurationSection(plugin.getConfig(), "WorldGuard");
		cfg = BukkitConfigUtil.getAndSetConfigurationSection(cfg, "CommandAllowedRegions");
		
		Set<String> set;
		List<String> list;
		
		
		// Summon command
		set = new HashSet<String>();
		if (cfg.isList("Summon"))
		{
			list = cfg.getStringList("Summon");
			for (String region : list)
				set.add(region);
		}
		else
		{
			list = Collections.emptyList();
		}
		commandSummonAllowedRegions = (Set<String>) (set.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(set));
		cfg.set("Summon", list);
		
		
		// Buy command
		set = new HashSet<String>();
		commandBuyAllowedRegions = Collections.unmodifiableSet(set);
		if (cfg.isList("Buy"))
		{
			list = cfg.getStringList("Buy");
			for (String region : list)
				set.add(region);
		}
		else
		{
			list = Collections.emptyList();
		}
		cfg.set("Buy", list);
		
		
		// Dismiss command
		set = new HashSet<String>();
		if (cfg.isList("Dismiss"))
		{
			list = cfg.getStringList("Dismiss");
			for (String region : list)
				set.add(region);
		}
		else
		{
			list = Collections.emptyList();
		}
		commandDismissAllowedRegions = (Set<String>) (set.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(set));
		cfg.set("Dismiss", list);
		
		
		// Sell command
		set = new HashSet<String>();
		if (cfg.isList("Sell"))
		{
			list = cfg.getStringList("Sell");
			for (String region : list)
				set.add(region);
		}
		else
		{
			list = Collections.emptyList();
		}
		commandSellAllowedRegions = (Set<String>) (set.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(set));
		cfg.set("Sell", list);
	}
	
	@Override
	public Horses getPlugin()
	{
		return plugin;
	}

	public boolean allowCommand(Set<String> allowedRegions, Location location)
	{
		if (getPlugin().getWorldGuard() == null)
			return true;
			
		if (allowedRegions.isEmpty())
			return false;
		
		RegionManager rm = getPlugin().getWorldGuard().getRegionManager(location.getWorld());
		
		if (rm == null)
			return false;
		
		for (ProtectedRegion region : rm.getApplicableRegions(location))
		{
			if (allowedRegions.contains(region.getId()))
			{
				return true;
			}
		}
		
		return false;
	}
}
