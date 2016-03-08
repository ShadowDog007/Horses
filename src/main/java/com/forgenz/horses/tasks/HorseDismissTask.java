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

package com.forgenz.horses.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.horses.Horses;
import com.forgenz.horses.Messages;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;

public class HorseDismissTask extends BukkitRunnable implements ForgeCore
{
	private final Location horseLocCache = new Location(null, 0.0, 0.0, 0.0);
	private final Location playerLocCache = new Location(null, 0.0, 0.0, 0.0);
	
	private final Horses plugin;
	private Player[] players;
	private int index;
	
	public HorseDismissTask(Horses plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public Horses getPlugin()
	{
		return plugin;
	}

	@Override
	public void run()
	{
		if (players == null)
		{
			players = Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]);
			index = 0;
		}
		
		long start = System.currentTimeMillis();
		
		while (index < players.length && System.currentTimeMillis() - start <= 2)
		{
			Player player = players[index++];
			
			if (!player.isValid())
				continue;
			
			Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player, false);
			
			if (stable == null)
				continue;
			
			PlayerHorse horseData = stable.getActiveHorse();
			
			if (horseData == null)
				continue;
			
			if (horseData.getHorse().getWorld() != player.getWorld())
			{
				horseData.removeHorse();
				continue;
			}
			
			if (horseData.getHorse().getLocation(horseLocCache).distanceSquared(player.getLocation(playerLocCache)) > 1024)
			{
				Messages.Event_MovedTooFarAway.sendMessage(player, horseData.getDisplayName());
				horseData.removeHorse();
			}
		}
		
		if (players.length == index)
		{
			players = null;
		}
	}
}
