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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.forgecore.v1_0.command.ForgeCommandArgument;
import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import com.forgenz.horses.config.HorseTypeConfig;
import com.forgenz.horses.config.HorsesConfig;
import com.forgenz.horses.config.HorsesPermissionConfig;

public class GiveCommand extends ForgeCommand
{
	public GiveCommand(ForgePlugin plugin)
	{
		super(plugin);
		
		registerAlias("give", true);
		registerPermission("horses.command.give");
		
		registerArgument(new ForgeCommandArgument("^[a-z0-9_&]+$", Pattern.CASE_INSENSITIVE, false, Command_List_Error_InvalidCharactersPlayerName.toString()));
		registerArgument(new ForgeCommandArgument(getPlugin().getHorsesConfig().forceEnglishCharacters ? "^[a-z0-9_&]+$" : "^[^ ]+$", Pattern.CASE_INSENSITIVE, false, Misc_Command_Error_NameValidCharacters.toString()));
		registerArgument(new ForgeCommandArgument(getPlugin().getHorsesConfig().forceEnglishCharacters ? "^[a-z0-9_&]+$" : "^[^ ]+$", Pattern.CASE_INSENSITIVE, false, Command_Buy_Error_Type.toString()));
		
		setAllowOp(true);
		setAllowConsole(true);
		setArgumentString(String.format("<%1$s%3$s> <%2$s%3$s> <%2$s%4$s>", Misc_Words_Player, Misc_Words_Horse, Misc_Words_Name, Misc_Words_Type));
		setDescription(Command_Give_Description.toString());
	}

	@Override
	protected void onCommand(CommandSender sender, ForgeArgs args)
	{
		Player player = Bukkit.getPlayerExact(args.getArg(0));
		
		if (player == null || !player.isOnline())
		{
			Command_List_Error_InvalidCharactersPlayerName.sendMessage(sender);
			return;
		}
		
		HorsesConfig cfg = getPlugin().getHorsesConfig();
		HorsesPermissionConfig pcfg = cfg.getPermConfig(player);
		
		// Fetch the horse type
		HorseTypeConfig typecfg = pcfg.getHorseTypeConfigLike(args.getArg(2));
		
		// Check if it is a valid horse type
		if (typecfg == null)
		{
			Command_Buy_Error_InvalidHorseType.sendMessage(sender, args.getArg(2));
			return;
		}
		
		String name = args.getArg(1);
		
		if (name.length() > pcfg.maxHorseNameLength)
		{
			Misc_Command_Error_HorseNameTooLong.sendMessage(sender, pcfg.maxHorseNameLength);
			return;
		}
		
		// Check if the player is allowed to use coloured names
		if (name.contains("&"))
		{
			// Filter out colour for use elsewhere
			name = ChatColor.translateAlternateColorCodes('&', name);
			name = ChatColor.stripColor(name);
		}
		
		// Make sure the name is more than one character
		if (name.length() == 0)
		{
			Misc_Command_Error_HorseNameEmpty.sendMessage(sender);
			return;
		}
		
		Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player);
		
		// Check if the player already has a horse with this name
		if (stable.findHorse(name, true) != null)
		{
			Command_Give_Error_AlreadyHaveAHorseWithThatName.sendMessage(sender, player.getName(), name);
			return;
		}
		
		// Check the horses name is valid
		if (cfg.rejectedHorseNamePattern.matcher(name).find())
		{
			Misc_Command_Error_IllegalHorseNamePattern.sendMessage(sender);
			return;
		}
		
		// Create the horse for the player
		PlayerHorse horse = stable.createHorse(name, typecfg, pcfg.startWithSaddle);
		
		Command_Give_Success_Completion.sendMessage(sender, player.getName(), horse.getName());
		Command_Give_Success_Completion_Player.sendMessage(player, args.getCommandUsed(), horse.getName());
	}
	
	@Override
	public Horses getPlugin()
	{
		return (Horses) super.getPlugin();
	}
}
