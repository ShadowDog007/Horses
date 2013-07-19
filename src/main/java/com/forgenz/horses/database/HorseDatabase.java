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

package com.forgenz.horses.database;

import java.util.HashMap;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;

public abstract class HorseDatabase implements ForgeCore
{
	protected static final Pattern COLOUR_CHAR_REPLACE = Pattern.compile(Character.toString(ChatColor.COLOR_CHAR));
	
	private final Horses plugin;
	private final HorseDatabaseStorageType dbType;
	
	private final HashMap<String, Stable> playerStables = new HashMap<String, Stable>();
	
	public HorseDatabase(Horses plugin, HorseDatabaseStorageType dbType)
	{
		this.plugin = plugin;
		this.dbType = dbType;
	}
	
	protected abstract Stable loadStable(String player);
	
	protected abstract void loadHorses(Stable stable);
	
	protected abstract void saveStable(Stable stable);
	
	public abstract void saveHorse(PlayerHorse horse);
	
	public abstract boolean deleteHorse(PlayerHorse horse);
	
	public Stable getPlayersStable(Player player)
	{
		return getPlayersStable(player, true);
	}
	
	public Stable getPlayersStable(Player player, boolean load)
	{
		Stable stable = playerStables.get(player.getName());
		
		if (stable == null && load)
		{
			stable = loadStable(player.getName());
			playerStables.put(player.getName(), stable);
		}
		
		return stable;
	}
	
	public void saveAll()
	{
		for (Stable stable : playerStables.values())
		{
			if (stable.getActiveHorse() != null)
			{
				stable.getActiveHorse().removeHorse();
			}
		}
	}
	
	@Override
	public Horses getPlugin()
	{
		return plugin;
	}
	
	public HorseDatabaseStorageType getType()
	{
		return dbType;
	}

	public void unload(Stable stable)
	{
		saveStable(stable);
		playerStables.remove(stable.getOwner());
	}
}
