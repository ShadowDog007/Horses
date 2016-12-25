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

import static com.forgenz.horses.Messages.Command_Buy_Error_CantAffordHorse;
import static com.forgenz.horses.Messages.Command_Buy_Error_TooManyHorses;
import static com.forgenz.horses.Messages.Command_Buy_Success_BoughtHorse;
import static com.forgenz.horses.Messages.Misc_Command_Error_CantUseColor;
import static com.forgenz.horses.Messages.Misc_Command_Error_CantUseFormattingCodes;
import static com.forgenz.horses.Messages.Misc_Command_Error_HorseNameTooLong;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.meta.ItemMeta;

import com.forgenz.forgecore.v1_0.bukkit.ForgeListener;
import com.forgenz.horses.HorseType;
import com.forgenz.horses.Horses;
import com.forgenz.horses.Messages;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import com.forgenz.horses.config.HorseTypeConfig;
import com.forgenz.horses.config.HorsesPermissionConfig;

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
		if (!(event.getRightClicked() instanceof AbstractHorse))
		{
			return;
		}
		
		// Fetch our lovely horse :)
		AbstractHorse horse = (AbstractHorse) event.getRightClicked();
		
		// Check if the Horse is owned
		PlayerHorse horseData = PlayerHorse.getFromEntity(horse); 
		
		// Check how we should handle the event
		if (horseData != null)
		{
			handleOwnedHorse(event, horseData, event.getPlayer());
		}
		else
		{
			handleUnownedHorse(event, horse, event.getPlayer());
		}
	}
	
	/**
	 * Handles interaction with an owned horse
	 */
	private void handleOwnedHorse(PlayerInteractEntityEvent event, PlayerHorse horseData, Player player)
	{
		// Check if the player owns the horse
		if (!event.getPlayer().getName().equals(horseData.getStable().getOwner()))
		{
			// If not we deny access to the horse
			Messages.Event_Interact_Error_CantInteractWithThisHorse.sendMessage(event.getPlayer(), horseData.getStable().getOwner());
			event.setCancelled(true);
			return;
		}
		
		HorsesPermissionConfig cfg = getPlugin().getHorsesConfig().getPermConfig(event.getPlayer());
		
		// Can the player rename their horse??
		if (event.getPlayer().getItemInHand().getType() == Material.NAME_TAG)
		{
			// Check if they are allowed to rename their horse
			if (cfg.allowRenameFromNameTag)
			{
				ItemMeta meta = event.getPlayer().getItemInHand().getItemMeta();
				String name = meta.getDisplayName();
				
				if (name == null)
				{
					Messages.Event_Interact_Error_RenameWithTagMustSetAName.sendMessage(player);
					event.setCancelled(true);
				}
				else if (getPlugin().getHorsesConfig().rejectedHorseNamePattern.matcher("new name").find())
				{
					Messages.Misc_Command_Error_IllegalHorseNamePattern.sendMessage(event.getPlayer());
					event.setCancelled(true);
				}
				else
				{
					String oldName = horseData.getDisplayName();
					horseData.rename(name);
					Messages.Command_Rename_Success_Renamed.sendMessage(event.getPlayer(), oldName, horseData.getDisplayName());
				}
				return;
			}
			else
			{
				Messages.Event_Interact_Error_CantRenameWithTag.sendMessage(event.getPlayer());
			}
			event.setCancelled(true);
		}
	}
	
	private void handleUnownedHorse(PlayerInteractEntityEvent event, AbstractHorse horse, Player player)
	{
		HorsesPermissionConfig cfg = getPlugin().getHorsesConfig().getPermConfig(event.getPlayer());
		
		if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.NAME_TAG)
		{
			if (cfg.allowClaimingWithNameTag)
			{
				ItemMeta meta = event.getPlayer().getItemInHand().getItemMeta();
				String name , displayName;
				name = displayName = meta.getDisplayName();
				
				event.setCancelled(true);
				
				if (name == null)
				{
					Messages.Event_Interact_Error_ClaimWithTagMustSetAName.sendMessage(player);
				}
				else
				{
					if (name.length() > cfg.maxHorseNameLength)
					{
						Misc_Command_Error_HorseNameTooLong.sendMessage(player, cfg.maxHorseNameLength);
						return;
					}
					
					Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player);
					
					// Check if the player has too many horses
					if (stable.getHorseCount() >= cfg.maxHorses)
					{
						Command_Buy_Error_TooManyHorses.sendMessage(player, cfg.maxHorses);
						return;
					}
					
					// Check if the player is allowed to use coloured names
					if (name.contains("&"))
					{
						if (!player.hasPermission("horses.colour"))
						{
							Misc_Command_Error_CantUseColor.sendMessage(player);
							return;
						}
						else if (!player.hasPermission("horses.formattingcodes") && PlayerHorse.FORMATTING_CODES_PATTERN.matcher(name).find())
						{
							Misc_Command_Error_CantUseFormattingCodes.sendMessage(player);
							return;
						}
						
						// Filter out colour for the final check
						name = ChatColor.translateAlternateColorCodes('&', name);
						name = ChatColor.stripColor(name);
					}
					
					if (stable.findHorse(name, true) != null)
					{
						Messages.Command_Buy_Error_AlreadyHaveAHorseWithThatName.sendMessage(player, name);
						return;
					}
					

					// Make sure we have no naughty words
					if (getPlugin().getHorsesConfig().rejectedHorseNamePattern.matcher(name).find())
					{
						Messages.Misc_Command_Error_IllegalHorseNamePattern.sendMessage(event.getPlayer());
						return;
					}
					
					HorseType type = HorseType.valueOf(horse);
					HorseTypeConfig typecfg = cfg.getHorseTypeConfig(type);
					
					// Check if the player can afford to buy the horse
					if (getPlugin().getEconomy() != null && typecfg.wildClaimCost > 0.0)
					{
						EconomyResponse responce = getPlugin().getEconomy().withdrawPlayer(player.getName(), typecfg.wildClaimCost);
						
						if (!responce.transactionSuccess())
						{
							Command_Buy_Error_CantAffordHorse.sendMessage(player, typecfg.wildClaimCost);
							return;
						}
						
						Command_Buy_Success_BoughtHorse.sendMessage(player, typecfg.wildClaimCost);
					}
					
					PlayerHorse horseData = stable.createHorse(displayName, typecfg, horse);
					
					Messages.Command_Buy_Success_Completion.sendMessage(player, "horses", horseData.getDisplayName());
				}
			}
			else if (cfg.blockRenamingOnWildHorses)
			{
				Messages.Event_Interact_Error_RenamingNaturalHorsesBlocked.sendMessage(player);
				event.setCancelled(true);
			}
			return;
		}
	}
	
	@Override
	public Horses getPlugin()
	{
		return (Horses) super.getPlugin();
	}
}
