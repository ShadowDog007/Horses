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

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.horses.Horses;
import com.forgenz.horses.Messages;
import com.forgenz.horses.database.HorseDatabase;
import com.forgenz.horses.database.HorseDatabaseStorageType;
import com.forgenz.horses.database.YamlDatabase;

public class RebuildCommand extends ForgeCommand
{
	public RebuildCommand(ForgePlugin plugin)
	{
		super(plugin);
		
		registerAlias("rebuild", true);
		registerPermission("horses.command.rebuild");
		
		setAllowOp(true);
		setAllowConsole(true);
		setDescription("Rebuilds the database to use UUID's");
	}

	@Override
	protected void onCommand(CommandSender sender, ForgeArgs args)
	{
		if (!Bukkit.getOnlineMode()) {
			sender.sendMessage("This command is not supported on offline servers");
			return;
		}
		
		HorseDatabase database = this.getPlugin().getHorseDatabase();
		
		if (database.getType() != HorseDatabaseStorageType.YAML) {
			sender.sendMessage("Rebuild only supports YAML database");
			return;
		}
		
		YamlDatabase yamlDb = (YamlDatabase) database;
		boolean success = false;
		
		try
		{
			getPlugin().onDisable();
			
			success = yamlDb.migrateToUuidDb();
			
			getPlugin().onEnable();
		}
		catch (Throwable e)
		{
			Messages.Command_Reload_Error_FailedToReload.sendMessage(sender);
			getPlugin().log(Level.SEVERE, "Failed to reload Horses", e);
			return;
		}
		
		sender.sendMessage("Rebuilt database with " + (success ? "no errors" : "errors"));
		Messages.Command_Reload_Success.sendMessage(sender);
	}
	
	@Override
	public Horses getPlugin()
	{
		return (Horses) super.getPlugin();
	}
}
