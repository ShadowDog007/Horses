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

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.forgenz.horses.HorseType;
import com.forgenz.horses.Horses;

public class HorsesWorldConfig extends AbstractConfig
{
	private static final String WORLDS_FOLDER = "worlds" + File.separator;
	
	protected final HorsesPermissionConfig worldCfg;
	private final Map<String, HorsesPermissionConfig> permissionConfigs;
	
	public HorsesWorldConfig(Horses plugin, YamlConfiguration cfg)
	{
		this(plugin, cfg, null, false);
	}
	
	public HorsesWorldConfig(Horses plugin, String world)
	{
		this(plugin, null, world, true);
	}
	
	private HorsesWorldConfig(Horses plugin, YamlConfiguration cfg, String world, final boolean standalone)
	{
		super(plugin, cfg, null,  WORLDS_FOLDER + world, "config", standalone);
		

		cfg = this.loadConfiguration();
		
		this.addResourseToHeader("header_world.txt");
		
		LinkedHashMap<String, HorsesPermissionConfig> permissionConfigs = new LinkedHashMap<String, HorsesPermissionConfig>();
		this.permissionConfigs = Collections.unmodifiableMap(permissionConfigs);
		
		List<String> permissions = cfg.getStringList("PermissionConfigs");
		for (String permission : cfg.getStringList("PermissionConfigs"))
		{
			permissionConfigs.put(permission, new HorsesPermissionConfig(plugin, this, permission));
		}
		set(cfg, "PermissionConfigs", permissions);
		
		worldCfg = new HorsesPermissionConfig(plugin, cfg);
		
		this.saveConfiguration();
	}
	
	protected HorsesPermissionConfig getPermConfig(Player player)
	{
		if (player != null)
		{
			for (Entry<String, HorsesPermissionConfig> e : permissionConfigs.entrySet())
			{
				if (player.hasPermission(e.getKey()))
				{
					return e.getValue();
				}
			}
		}
		
		return worldCfg;
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
	
	public boolean isProtecting()
	{
		for (HorsesPermissionConfig permCfg : permissionConfigs.values())
		{
			if (permCfg.isProtecting())
				return true;
		}
		
		return worldCfg.isProtecting();
	}
}
