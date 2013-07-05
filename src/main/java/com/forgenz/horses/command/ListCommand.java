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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.forgecore.v1_0.command.ForgeCommandArgument;
import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;

public class ListCommand extends ForgeCommand
{
	public ListCommand(ForgePlugin plugin)
	{
		super(plugin);
		
		registerAlias("list", true);
		registerPermission("horses.command.list");
		
		registerArgument(new ForgeCommandArgument("^[a-z0-9_]{0,16}$", Pattern.CASE_INSENSITIVE, true, Command_List_Error_InvalidCharactersPlayerName.toString()));
		
		setAllowOp(true);
		setArgumentString(String.format("[%s]", Misc_Words_Player));
		setDescription(Command_List_Description.toString());
	}

	@Override
	protected void onCommand(CommandSender sender, ForgeArgs args)
	{
		Player player = null;
		if (args.getNumArgs() >= 1)
		{
			if (!sender.hasPermission("horses.command.list.all"))
			{
				Command_List_Error_NoPermissionToListPlayersHorses.sendMessage(sender);
				return;
			}
			
			player = Bukkit.getPlayer(args.getArg(0));
			
			if (player == null)
			{
				Command_List_Error_CouldNotFindPlayer.sendMessage(sender, args.getArg(0));
				return;
			}
		}
		else if (sender instanceof Player)
		{
			player = (Player) sender;
		}
		else
		{
			Misc_Command_Error_CantBeUsedFromConsole.sendMessage(sender);
			return;
		}
		
		Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player);
		
		StringBuilder horses = new StringBuilder();
		
		for (PlayerHorse horse : stable)
		{
			if (horses.length() > 0)
				horses.append(Command_List_Success_HorseListSeparator);
			// Horses name
			horses.append(Command_List_Success_HorseNamePrefix).append(horse.getDisplayName());
			// Horses type
			horses.append(":").append(Command_List_Success_HorseTypePrefix).append(horse.getType());
		}
		
		if (horses.length() == 0)
		{
			Command_List_Error_NoHorses.sendMessage(sender);
		}
		else
		{
			if (player != sender)
				horses.insert(0, String.format(Command_List_Success_InitialMessageOtherPlayer.toString(), player.getName()));
			else
				horses.insert(0, Command_List_Success_InitialMessage.toString());
			sender.sendMessage(horses.toString());
		}
	}
	
	@Override
	public Horses getPlugin()
	{
		return (Horses) super.getPlugin();
	}
}
