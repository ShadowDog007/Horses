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
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;

public abstract class HorseDatabase implements ForgeCore
{
	public static final String DEFAULT_GROUP = "default";
	protected static final Pattern COLOUR_CHAR_REPLACE = Pattern.compile(Character.toString(ChatColor.COLOR_CHAR));
	
	private final Horses plugin;
	private final HorseDatabaseStorageType dbType;
	
	private final HashMap<String, Stable> playerStables = new HashMap<String, Stable>();
	
	public HorseDatabase(Horses plugin, HorseDatabaseStorageType dbType)
	{
		this.plugin = plugin;
		this.dbType = dbType;
	}
	
	protected abstract List<Stable> loadEverything();
	
	protected abstract void importStables(List<Stable> stables);
	
	protected abstract Stable loadStable(String player, String stableGroup);
	
	protected abstract void loadHorses(Stable stable, String stableGroup);
	
	protected abstract void saveStable(Stable stable);
	
	public abstract void saveHorse(PlayerHorse horse);
	
	public abstract boolean deleteHorse(PlayerHorse horse);
	
	public void importHorses(HorseDatabaseStorageType type)
	{
		if (type == HorseDatabaseStorageType.DUMMY)
			type = null;
		
		if (type == null)
			return;
		
		getPlugin().info("Attempting import of %s database into %s database", type, getType());
		
		HorseDatabase db = type.create(getPlugin(), false);
		if (db == null)
			return;
		
		importStables(db.loadEverything());
	}
	
	public Stable getPlayersStable(Player player)
	{
		return getPlayersStable(player, true);
	}
	
	public Stable getPlayersStable(Player player, boolean load)
	{
		Stable stable = playerStables.get(player.getName());
		String stableGroup = getPlugin().getHorsesConfig().getStableGroup(player.getWorld());
		
		// Check if a stable is loaded
		if (stable != null && !stableGroup.equals(stable.getGroup()))
		{
			// If the stable is not in the same group as the one we want we unload it
			// And load the new horses
				unload(stable);
				stable = null;
				playerStables.remove(player.getName());
		}
		
		if (stable == null && load)
		{
			stable = loadStable(player.getName(), stableGroup);
			playerStables.put(player.getName(), stable);
		}
		
		return stable;
	}
	
	public void saveAll()
	{
		for (Stable stable : playerStables.values().toArray(new Stable[playerStables.size()]))
		{
			unload(stable);
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
		if (stable.getActiveHorse() != null)
		{
			stable.getActiveHorse().removeHorse();
		}
		
		saveStable(stable);
		playerStables.remove(stable.getOwner());
	}
}
