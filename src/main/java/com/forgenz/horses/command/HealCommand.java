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
import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import com.forgenz.horses.config.HorseTypeConfig;
import com.forgenz.horses.config.HorsesPermissionConfig;

public class HealCommand extends ForgeCommand
{
	public HealCommand(ForgePlugin plugin)
	{
		super(plugin);
		
		registerAlias("heal", true);
		registerPermission("horses.command.heal");
		
		registerArgument(new ForgeCommandArgument("^[0-9]+$", Pattern.CASE_INSENSITIVE, false, Command_Heal_Error_HealAmountInvalid.toString()));
		
		setAllowOp(true);
		setAllowConsole(false);
		setArgumentString(String.format("[%s]", Misc_Words_Amount));
		setDescription(Command_Heal_Description.toString());
	}

	@Override
	protected void onCommand(CommandSender sender, ForgeArgs args)
	{
		Player player = (Player) sender;
		
		HorsesPermissionConfig pcfg = getPlugin().getHorsesConfig().getPermConfig(player);
		
		if (!pcfg.allowHealCommand)
		{
			Misc_Command_Error_ConfigDenyPerm.sendMessage(sender, getMainCommand());
			return;
		}
		
		Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player, false);
		
		PlayerHorse horse = null;
		
		if (stable != null)
		{
			horse = stable.getActiveHorse();
		}
		
		if (horse == null)
		{
			Command_Heal_Error_NoActiveHorses.sendMessage(player);
			return;
		}
		
		int healAmount = Integer.valueOf(args.getArg(0));
		double actualHealAmount = horse.getHealEstimate(healAmount);
		
		if (getPlugin().getEconomy() != null)
		{
			HorseTypeConfig cfg = pcfg.getHorseTypeConfig(horse.getType());
			
			double cost = cfg.healCost * actualHealAmount;
			
			EconomyResponse result = getPlugin().getEconomy().withdrawPlayer(player.getName(), cost);
			
			if (!result.transactionSuccess())
			{
				Command_Heal_Error_CantAffordHeal.sendMessage(player, actualHealAmount, cost);
				return;
			}
			
			Command_Heal_Success_HealedForCost.sendMessage(player, horse.getDisplayName(), actualHealAmount, cost);
		}
		else
		{
			Command_Heal_Success_HealedWithoutCost.sendMessage(player, horse.getDisplayName(), actualHealAmount);
		}
		
		horse.addHealth(actualHealAmount);
	}
	
	@Override
	public Horses getPlugin()
	{
		return (Horses) super.getPlugin();
	}
}
