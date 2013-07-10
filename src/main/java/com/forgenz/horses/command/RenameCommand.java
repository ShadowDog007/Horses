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

package com.forgenz.horses.command;

import static com.forgenz.horses.Messages.*;

import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.forgecore.v1_0.command.ForgeCommandArgument;
import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import com.forgenz.horses.config.HorsesConfig;

public class RenameCommand extends ForgeCommand
{

	public RenameCommand(ForgePlugin plugin)
	{
		super(plugin);

		registerAlias("rename", true);
		registerPermission("horses.command.rename");
		
		registerArgument(new ForgeCommandArgument("^[a-z0-9_]{0,16}$", Pattern.CASE_INSENSITIVE, false, Misc_Command_Error_InvalidName.toString()));
		registerArgument(new ForgeCommandArgument("^[a-z0-9_&]{0,16}$", Pattern.CASE_INSENSITIVE, false, Misc_Command_Error_NameValidCharacters.toString()));
		
		setAllowOp(true);
		setAllowConsole(false);
		setArgumentString(String.format("<%1%s%2$s> <%3$s%4$s>", Misc_Words_Horse, Misc_Words_Name, Misc_Words_New, Misc_Words_Name));
		setDescription(Command_Rename_Description.toString());
	}

	@Override
	protected void onCommand(CommandSender sender, ForgeArgs args)
	{
		Player player = (Player) sender;
		
		Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player);
		
		String name = args.getArg(1);
		
		// Find the horse!
		PlayerHorse horse = stable.findHorse(args.getArg(0), false);
		
		// Check if the horse exists
		if (horse == null)
		{
			Misc_Command_Error_NoHorseNamed.sendMessage(player, args.getArg(0));
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
			
			name = ChatColor.translateAlternateColorCodes('&', name);
			name = ChatColor.stripColor(name);
		}
		
		// Make sure the name is more than one character
		if (name.length() == 0)
		{
			Misc_Command_Error_HorseNameEmpty.sendMessage(player);
			return;
		}
			
		
		HorsesConfig cfg = getPlugin().getHorsesConfig();
		
		// Check the horses name is valid
		if (cfg.rejectedHorseNamePattern.matcher(name).find())
		{
			Misc_Command_Error_IllegalHorseNamePattern.sendMessage(player);
			return;
		}
		
		if (cfg.requireNameTagForRenaming)
		{
			if (player.getItemInHand().getType() != Material.NAME_TAG)
			{
				Command_Rename_Error_RequireNametag.sendMessage(player);
				return;
			}
			else
			{
				player.setItemInHand(new ItemStack(Material.AIR));
			}
		}
		
		String oldDisplayName = horse.getDisplayName();
		horse.rename(args.getArg(1));
		Command_Rename_Success_Renamed.sendMessage(player, oldDisplayName, horse.getDisplayName());
	}

	@Override
	public Horses getPlugin()
	{
		return (Horses) super.getPlugin();
	}
}
