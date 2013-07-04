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

package com.forgenz.horses.listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import com.forgenz.forgecore.v1_0.bukkit.ForgeListener;
import com.forgenz.horses.Horses;
import com.forgenz.horses.Messages;
import com.forgenz.horses.PlayerHorse;

public class InteractListener extends ForgeListener
{
	public InteractListener(Horses plugin)
	{
		super(plugin);
		
		register();
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEntityEvent event)
	{
		// We are only interested in interactions with horses
		if (event.getRightClicked().getType() != EntityType.HORSE)
		{
			return;
		}
		
		// Fetch our lovely horse :)
		Horse horse = (Horse) event.getRightClicked();
		
		// Check if the Horse is owned
		PlayerHorse horseData = PlayerHorse.getFromEntity(horse); 
		
		// If the horse has player data then they are owned and managed by Horses
		if (horseData == null)
		{
			// Else let the player have their way with the horse
			return;
		}
		
		// Check if the player owns the horse
		if (!event.getPlayer().getName().equals(horseData.getStable().getOwner()))
		{
			// If not we deny access to the horse
			Messages.Event_Interact_Error_CantInteractWithThisHorse.sendMessage(event.getPlayer(), horseData.getStable().getOwner());
			event.setCancelled(true);
			return;
		}
		
		// Stop players from renaming their horses with name tags
		if (event.getPlayer().getItemInHand().getType() == Material.NAME_TAG)
		{
			if (false && getPlugin().getHorsesConfig().allowRenameFromNameTag)
			{
				if (getPlugin().getHorsesConfig().rejectedHorseNamePattern.matcher("new name").find())
				{
					Messages.Misc_Command_Error_IllegalHorseNamePattern.sendMessage(event.getPlayer());
				}
				else
				{
					Messages.Command_Rename_Success_Renamed.sendMessage(event.getPlayer(), horseData.getName(), "new name");
					horseData.rename("new name");
				}
			}
			else
			{
				Messages.Event_Interact_Error_CantRenameWithTag.sendMessage(event.getPlayer());
			}
			event.setCancelled(true);
		}
	}
	
	@Override
	public Horses getPlugin()
	{
		return (Horses) super.getPlugin();
	}
}
