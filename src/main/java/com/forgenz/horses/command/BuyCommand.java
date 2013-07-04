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

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.forgecore.v1_0.command.ForgeCommandArgument;
import com.forgenz.horses.HorseType;
import com.forgenz.horses.Horses;
import com.forgenz.horses.Stable;
import com.forgenz.horses.config.HorseTypeConfig;
import com.forgenz.horses.config.HorsesConfig;

public class BuyCommand extends ForgeCommand
{
	public BuyCommand(ForgePlugin plugin)
	{
		super(plugin);
		
		registerAlias("buy", true);
		registerPermission("horses.command.buy");
		
		registerArgument(new ForgeCommandArgument("^[a-z0-9_]{0,16}$", Pattern.CASE_INSENSITIVE, false, Misc_Command_Error_NameValidCharacters.toString()));
		registerArgument(new ForgeCommandArgument("^[a-z]{0,21}$", Pattern.CASE_INSENSITIVE, false, Command_Buy_Error_Type.toString()));
		
		setAllowOp(true);
		setAllowConsole(false);
		setArgumentString(String.format("<%1$s%2$s> <%1$s%3$s>", Misc_Words_Horse, Misc_Words_Name, Misc_Words_Type));
		setDescription(Command_Buy_Description.toString());
	}

	@Override
	protected void onCommand(CommandSender sender, ForgeArgs args)
	{
		Player player = (Player) sender;
		
		HorsesConfig cfg = getPlugin().getHorsesConfig();
		
		HorseType type = HorseType.closeValueOf(args.getArg(1));
		
		if (type == null)
		{
			Command_Buy_Error_InvalidHorseType.sendMessage(player, args.getArg(1));
			return;
		}
		
		// Check the horses name is valid
		if (cfg.rejectedHorseNamePattern.matcher(args.getArg(0)).find())
		{
			Misc_Command_Error_IllegalHorseNamePattern.sendMessage(player);
			return;
		}
		
		Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player);
		
		// Check if the player has too many horses
		if (stable.getHorseCount() >= cfg.maxHorses)
		{
			Command_Buy_Error_TooManyHorses.sendMessage(player, cfg.maxHorses);
			return;
		}
		
		if (stable.findHorse(args.getArg(0), true) != null)
		{
			Command_Buy_Error_AlreadyHaveAHorseWithThatName.sendMessage(player, args.getArg(0));
			return;
		}
		
		HorseTypeConfig typeCfg = cfg.getHorseTypeConfig(type);
		
		// Check if the player can afford to buy the horse
		if (getPlugin().getEconomy() != null)
		{
			EconomyResponse responce = getPlugin().getEconomy().withdrawPlayer(player.getName(), typeCfg.buyCost);
			
			if (!responce.transactionSuccess())
			{
				Command_Buy_Error_CantAffordHorse.sendMessage(player, typeCfg.buyCost);
				return;
			}
			
			Command_Buy_Success_BoughtHorse.sendMessage(player, typeCfg.buyCost);
		}
		
		// Create the horse for the player
		stable.createHorse(args.getArg(0), type, player.hasPermission("horses.vip"));
		
		Command_Buy_Success_Completion.sendMessage(player, args.getCommandUsed(), args.getArg(0));
	}
	
	@Override
	public Horses getPlugin()
	{
		return (Horses) super.getPlugin();
	}
}