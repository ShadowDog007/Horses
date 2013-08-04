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

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.forgecore.v1_0.command.ForgeCommandArgument;
import com.forgenz.horses.HorseType;
import com.forgenz.horses.Horses;
import com.forgenz.horses.config.HorseTypeConfig;
import com.forgenz.horses.config.HorsesPermissionConfig;

public class TypeCommand extends ForgeCommand
{
	public TypeCommand(ForgePlugin plugin)
	{
		super(plugin);
		
		registerAlias("types", true);
		registerAlias("type", false);
		registerAlias("t", false);
		registerPermission("horses.command.types");
		
		registerArgument(new ForgeCommandArgument("^.+$", 0, true, ChatColor.RED + "[Horses] This should never be seen"));
		
		setAllowOp(true);
		setArgumentString(String.format("[%s%s]", Misc_Words_Horse, Misc_Words_Type));
		setDescription(Command_Type_Description.toString());
	}

	@Override
	protected void onCommand(CommandSender sender, ForgeArgs args)
	{
		boolean player = sender instanceof Player;
		
		HorsesPermissionConfig pcfg = getPlugin().getHorsesConfig().getPermConfig((Player) (player ? sender : null));
		
		if (!pcfg.allowTypesCommand)
		{
			Misc_Command_Error_ConfigDenyPerm.sendMessage(sender, getMainCommand());
			return;
		}
		
		if (args.getNumArgs() > 0)
		{
			HorseTypeConfig cfg = pcfg.getHorseTypeConfigLike(args.getArg(0));
			
			if (cfg == null)
			{
				Command_Buy_Error_InvalidHorseType.sendMessage(sender, args.getArg(0));
			}
			else if (player && !sender.hasPermission(cfg.type.getPermission()))
			{
				Command_Type_Error_NoPermForHorse.sendMessage(sender);
			}
			else
			{
				sender.sendMessage(String.format((getPlugin().getEconomy() != null ? Command_Type_SingleTypeFormatEco : Command_Type_SingleTypeFormat).toString(), cfg.displayName, cfg.horseHp, cfg.horseMaxHp, cfg.jumpStrength, cfg.buyCost));
			}
			return;
		}
		
		StringBuilder bldr = new StringBuilder();
		int size = bldr.append(Command_Type_BeginWith).length();
		
		for (HorseType type : HorseType.values())
		{
			if (player && !sender.hasPermission(type.getPermission()))
			{
				continue;
			}
				
			if (bldr.length() != 0)
			{
				bldr.append(Command_Type_TypeSeparator);
			}
			
			HorseTypeConfig cfg = pcfg.getHorseTypeConfig(type);
			
			bldr.append(Command_Type_HorseTypePrefix).append(cfg.displayName);
		}
		
		if (size == bldr.length())
			Command_Type_Error_NoHorsePerms.sendMessage(sender);
		else
			sender.sendMessage(bldr.toString());
	}

	@Override
	public Horses getPlugin()
	{
		return (Horses) super.getPlugin();
	}
}
