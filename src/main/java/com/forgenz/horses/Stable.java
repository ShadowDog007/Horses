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

package com.forgenz.horses;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.horses.config.HorseTypeConfig;

public class Stable implements ForgeCore, Iterable<PlayerHorse>
{
	private final Horses plugin;
	private final String group;
	private final String player;
	private int id;
	
	private List<PlayerHorse> horses = Collections.synchronizedList(new LinkedList<PlayerHorse>());
	
	private PlayerHorse activeHorse;
	private PlayerHorse lastActiveHorse;
	
	public Stable(Horses plugin, String group, String player)
	{
		this(plugin, group, player, -1);
	}
	
	public Stable(Horses plugin, String group, String player, int id)
	{
		this.plugin = plugin;
		this.group = group;
		this.player = player;
		this.id = id;
	}

	@Override
	public Horses getPlugin()
	{
		return plugin;
	}
	
	public String getOwner()
	{
		return player;
	}
	
	public Player getPlayerOwner()
	{
		return Bukkit.getPlayerExact(getOwner());
	}
	
	/**
	 * Fetches the ID of this stable for the given database<br />
	 * 
	 * @return The stables ID, -1 If the active database does not use ID's or has not been saved yet 
	 */
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	public String getGroup()
	{
		return group;
	}
	
	public int getHorseCount()
	{
		return horses.size();
	}
	
	public void addHorse(PlayerHorse horse)
	{
		horses.add(horse);
	}
	
	protected void setActiveHorse(PlayerHorse horseData)
	{
		if (activeHorse != null)
		{
			horseData.removeHorse();
			horseData.saveChanges();
			lastActiveHorse = horseData;
		}
		
		activeHorse = horseData;
	}
	
	public PlayerHorse getLastActiveHorse()
	{
		return lastActiveHorse != null || horses.isEmpty() ? lastActiveHorse : horses.get(0);
	}
	
	public void setLastActiveHorse(PlayerHorse horse)
	{
		if (!horses.contains(horse))
			return;
		
		lastActiveHorse = horse;
	}
	
	/**
	 * Returns the horse with the given name
	 * @param name The name we are looking for
	 * @param exact True if we are looking to the exact name
	 * @return
	 */
	public PlayerHorse findHorse(String name, boolean exact)
	{
		PlayerHorse bestMatch = null;
		
		synchronized (horses)
		{
			Iterator<PlayerHorse> it = horses.iterator();
			
			name = name.toLowerCase();
			int length = 0;
			boolean startsWith = false;
			
			while (it.hasNext())
			{
				PlayerHorse horse = it.next();
				
				String horseName = horse.getName().toLowerCase();
				
				if (horseName.equals(name))
					return horse;
				else if (exact)
					continue;
				else if (horseName.startsWith(name))
				{
					if (length < name.length())
					{
						length = name.length();
						bestMatch = horse;
						startsWith = true;
					}
				}
				else if (!startsWith && bestMatch != null && horseName.contains(name))
					bestMatch = horse;
			}
		}
		
		return bestMatch;
	}
	
	public PlayerHorse createHorse(String name, HorseTypeConfig typecfg, boolean saddle)
	{
		return createHorse(name, typecfg, null, saddle);
	}
	
	public PlayerHorse createHorse(String name, HorseTypeConfig typecfg, Horse horse)
	{		
		return createHorse(name, typecfg, horse, false);
	}
	
	private PlayerHorse createHorse(String name, HorseTypeConfig typecfg, Horse horse, boolean saddle)
	{
		PlayerHorse horseData = new PlayerHorse(plugin, this, name, typecfg.type, typecfg.horseHp, typecfg.horseMaxHp, typecfg.speed, typecfg.jumpStrength, horse);
		
		horses.add(horseData);
		
		if (horse == null && saddle)
		{
			horseData.setSaddle(Material.SADDLE);
		}
		
		getPlugin().getHorseDatabase().saveHorse(horseData);
		
		return horseData;
	}

	@Override
	public Iterator<PlayerHorse> iterator()
	{
		return horses.iterator();
	}

	public PlayerHorse getActiveHorse()
	{
		if (activeHorse != null)
		{
			if (activeHorse.getHorse() == null || !activeHorse.getHorse().isValid())
			{
				activeHorse = null;
			}
		}
		
		return activeHorse;
	}

	protected void removeActiveHorse(PlayerHorse horseData)
	{
		if (activeHorse == horseData)
		{
			activeHorse = null;
		}
	}
	
	public boolean deleteHorse(PlayerHorse playerHorse)
	{
		if (lastActiveHorse == playerHorse)
			lastActiveHorse = null;
		
		if (getPlugin().getHorseDatabase().deleteHorse(playerHorse))
		{
			horses.remove(playerHorse);
			return true;
		}
		
		return false;
	}
}
