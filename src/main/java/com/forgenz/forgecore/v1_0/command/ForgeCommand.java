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

package com.forgenz.forgecore.v1_0.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.command.ForgeArgumentError.ErrorType;

public abstract class ForgeCommand implements ForgeCore
{
	private final ForgePlugin plugin;
	
	private String mainCommand;
	private final ArrayList<String> aliases = new ArrayList<String>();
	private String aliasString = "";
	
	private final ArrayList<ForgeCommandArgument> args = new ArrayList<ForgeCommandArgument>();
	
	private int minArgs = 0;
	private int maxArgs = 0;
	
	private List<String> permissions;
	private boolean allowOp, allowConsole = true;
	
	private String argString = "";
	private String desc = "No Description";
	
	protected ForgeCommand(ForgePlugin plugin)
	{
		this.plugin = plugin;
		
		// We only create a list for this if a permission is registered
		permissions = null;
		// Default to all op's allowed to use the command
		allowOp = true;
	}
	
	/**
	 * Registers the alias with the command.
	 * If this is the first call to this method the alias is set as the main command.
	 * 
	 * @param alias The string to register with the command
	 * @param main If true the main command is set
	 */
	protected final void registerAlias(String alias, boolean main)
	{
		alias = alias.trim();
		
		if (aliasString.length() != 0)
		{
			aliasString += ",";
		}
		
		aliasString += alias;
		
		alias = alias.toLowerCase();
		
		if (main || mainCommand == null)
		{
			mainCommand = alias;
		}
		
		aliases.add(alias);
	}
	
	/**
	 * Registers the next argument for the command
	 * @param arg The next argument
	 */
	protected final void registerArgument(ForgeCommandArgument arg)
	{
		if (!arg.isOptional() && minArgs != maxArgs)
		{
			throw new IllegalArgumentException("All required arguments must be before any optional arguments");
		}
		
		if (arg.isOptional())
		{
			++maxArgs;
		}
		else
		{
			maxArgs = ++minArgs;
		}
		
		args.add(arg);
	}
	
	/**
	 * Registers the permission with the command.
	 * A player with any of the registered permissions is allowed access to this command
	 * @param perm The permission being registered
	 */
	protected final void registerPermission(String perm)
	{
		if (permissions == null)
		{
			permissions = new ArrayList<String>(1);
		}
		
		permissions.add(perm);
	}
	
	/**
	 * Sets the flag which lets all op's use this command
	 * @param allowOp True to allow op's to use this command without permissions
	 */
	protected final void setAllowOp(boolean allowOp)
	{
		this.allowOp = allowOp;
	}
	
	/**
	 * Sets the flag which allows console to use this command
	 */
	protected final void setAllowConsole(boolean allowConsole)
	{
		this.allowConsole = allowConsole;
	}
	
	/**
	 * Sets the string shown in help to display command arguments
	 * @param argString The string
	 */
	protected final void setArgumentString(String argString)
	{
		this.argString = argString;
	}
	
	/**
	 * Sets the description of the command
	 * @param desc The description
	 */
	protected final void setDescription(String desc)
	{
		this.desc = desc;
	}
	
	/**
	 * Checks if the given player has any of the permissions
	 * @param player The player being checked
	 * @return True if the player has any of the registered permissions for this command
	 */
	protected final boolean checkPermissions(Player player)
	{
		if (permissions == null || allowOp && player.isOp())
		{
			return true;
		}
			
		for (String perm : permissions)
		{
			if (player.hasPermission(perm))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Validates the arguments and sends the sender an error if they are invalid
	 * @param sender The command sender involved
	 * @param args The arguments used
	 * @return True if the arguments are valid
	 */
	protected final boolean validateArguments(CommandSender sender, ForgeArgs args)
	{
		int length = args.getNumArgs();
		
		if (length < minArgs)
		{
			sender.sendMessage(ForgeArgumentError.buildError(ErrorType.TOO_FEW_ARGS, null).getMessage());
			return false;
		}
		
		if (length > maxArgs)
		{
			sender.sendMessage(ForgeArgumentError.buildError(ErrorType.TOO_MANY_ARGS, null).getMessage());
			return false;
		}
		
		for (int i = 0; i < length; ++i)
		{
			ForgeCommandArgument argument = this.args.get(i);
			String value = args.getArg(i);
			
			if (!argument.argumentMatches(value))
			{
				sender.sendMessage(argument.getError());
				return false;
			}
		}
		
		return true;
	}
	
	protected abstract void onCommand(CommandSender sender, ForgeArgs args);
	
	public final String getMainCommand()
	{
		return mainCommand;
	}
	
	public final String[] getAliases()
	{
		return aliases.toArray(new String[aliases.size()]);
	}
	
	public final String getAliasString()
	{
		return aliasString;
	}
	
	public final String getArgString()
	{
		return argString;
	}
	
	public final boolean allowConsole()
	{
		return allowConsole;
	}
	
	public final String getDescription()
	{
		return desc;
	}
	
	@Override
	public ForgePlugin getPlugin()
	{
		return plugin;
	}
}
