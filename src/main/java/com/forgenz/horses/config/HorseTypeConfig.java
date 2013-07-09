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

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.util.BukkitConfigUtil;
import com.forgenz.horses.HorseType;

public class HorseTypeConfig implements ForgeCore
{
	private ForgePlugin plugin;
	
	// Health related stuff
	public final String displayName;
	public final float defaultHorseHp, defaultHorseMaxHp, vipHorseHp, vipHorseMaxHp, horseMaximumHpUpgrade;
	
	// Economy related stuff
	public final double buyCost, healCost, hpUpgradeCost, renameCost;
	
	public HorseTypeConfig(ForgePlugin plugin, ConfigurationSection cfg, HorseType type)
	{
		this.plugin = plugin;
		
		displayName = ChatColor.translateAlternateColorCodes('&', BukkitConfigUtil.getAndSet(cfg, "DisplayName", String.class, type.toString()));
		
		defaultHorseHp = BukkitConfigUtil.getAndSet(cfg, "DefaultHealth", Number.class, 12.0).floatValue();
		defaultHorseMaxHp = BukkitConfigUtil.getAndSet(cfg, "DefaultMaxHealth", Number.class, 12.0).floatValue();
		vipHorseHp = BukkitConfigUtil.getAndSet(cfg, "VIPHealth", Number.class, 20.0).floatValue();
		vipHorseMaxHp = BukkitConfigUtil.getAndSet(cfg, "VIPMaxHealth", Number.class, 20.0).floatValue();
		
		horseMaximumHpUpgrade = BukkitConfigUtil.getAndSet(cfg, "MaxHpUpgrade", Number.class, 30.0).floatValue();
		
		
		// Only setup economy settings if economy is enabled
		if (getPlugin().getEconomy() != null)
		{
			buyCost = BukkitConfigUtil.getAndSet(cfg, "BuyCost", Number.class, 10.0).doubleValue();
			healCost = BukkitConfigUtil.getAndSet(cfg, "HealCost", Number.class, 10.0).doubleValue();
			hpUpgradeCost = BukkitConfigUtil.getAndSet(cfg, "HpUpgradeCost", Number.class, 10.0).doubleValue();
			renameCost = BukkitConfigUtil.getAndSet(cfg, "RenameCost", Number.class, 0.0).doubleValue();
		}
		else
		{
			buyCost = healCost = hpUpgradeCost = renameCost = 0.0;
		}
	}

	@Override
	public ForgePlugin getPlugin()
	{
		return plugin;
	}
}
