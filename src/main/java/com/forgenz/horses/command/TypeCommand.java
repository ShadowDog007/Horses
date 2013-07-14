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

import static com.forgenz.horses.Messages.Command_Type_BeginWith;
import static com.forgenz.horses.Messages.Command_Type_Description;
import static com.forgenz.horses.Messages.Command_Type_HorseCostPrefix;
import static com.forgenz.horses.Messages.Command_Type_HorseTypePrefix;
import static com.forgenz.horses.Messages.Command_Type_TypeCostSeparator;
import static com.forgenz.horses.Messages.Command_Type_TypeSeparator;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.horses.HorseType;
import com.forgenz.horses.Horses;
import com.forgenz.horses.config.HorseTypeConfig;

public class TypeCommand extends ForgeCommand
{
	public TypeCommand(ForgePlugin plugin)
	{
		super(plugin);
		
		registerAlias("types", true);
		registerAlias("type", false);
		registerAlias("t", false);
		registerPermission("horses.command.types");
		
		setAllowOp(true);
		setDescription(Command_Type_Description.toString());
	}

	@Override
	protected void onCommand(CommandSender sender, ForgeArgs args)
	{
		boolean player = sender instanceof Player;
		
		StringBuilder bldr = new StringBuilder();
		
		for (HorseType type : HorseType.values())
		{
			if (player && !sender.hasPermission(type.getPermission()))
			{
				continue;
			}
				
			if (bldr.length() == 0)
			{
				bldr.append(Command_Type_BeginWith);
			}
			else
			{
				bldr.append(Command_Type_TypeSeparator);
			}
			
			HorseTypeConfig cfg = getPlugin().getHorsesConfig().getHorseTypeConfig(player ? (Player) sender : null, type);
			
			bldr.append(Command_Type_HorseTypePrefix).append(cfg.displayName);
			
			if (getPlugin().getEconomy() != null)
			{
				bldr.append(Command_Type_TypeCostSeparator).append(Command_Type_HorseCostPrefix).append(cfg.buyCost);
			}
		}
		
		sender.sendMessage(bldr.toString());
	}

	@Override
	public Horses getPlugin()
	{
		return (Horses) super.getPlugin();
	}
}
