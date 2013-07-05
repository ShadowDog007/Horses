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

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.horses.config.HorseTypeConfig;

public class Stable implements ForgeCore, Iterable<PlayerHorse>
{
	private final Horses plugin;
	private final String player;
	
	private List<PlayerHorse> horses = Collections.synchronizedList(new LinkedList<PlayerHorse>());
	
	private PlayerHorse activeHorse;
	
	public Stable(Horses plugin, String player)
	{
		this.plugin = plugin;
		this.player = player;
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
		}
		
		activeHorse = horseData;
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
			
			while (it.hasNext())
			{
				PlayerHorse horse = it.next();
				
				if (horse.getName().equalsIgnoreCase(name))
					return horse;
				else if (exact)
					continue;
				else if (horse.getName().startsWith(name))
					bestMatch = horse;
				else if (bestMatch != null && horse.getName().contains(name))
					bestMatch = horse;
			}
		}
		
		return bestMatch;
	}
	
	public PlayerHorse createHorse(String name, HorseType type, boolean vip)
	{
		HorseTypeConfig cfg = getPlugin().getHorsesConfig().getHorseTypeConfig(type);
		
		float hp = vip ? cfg.vipHorseHp : cfg.defaultHorseHp;
		float maxHp = vip ? cfg.vipHorseMaxHp : cfg.defaultHorseMaxHp;
		
		PlayerHorse horse = new PlayerHorse(plugin, this, name, type, hp, maxHp);
		
		horses.add(horse);
		
		getPlugin().getHorseDatabase().saveHorse(horse);
		
		return horse;
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
	
	public void deleteHorse(PlayerHorse playerHorse)
	{
		horses.remove(playerHorse);
		getPlugin().getHorseDatabase().deleteHorse(playerHorse);
	}
}
