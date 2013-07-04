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
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;

public final class ForgeCommandHandler extends ForgeCommand implements ForgeCore, CommandExecutor
{
	private static final Pattern NUMBER = Pattern.compile("\\d+");
	
	public static final String HEADER_REPLACE_PLUGIN_NAME = "%NAME%";
	public static final String HEADER_REPLACE_VERSION = "%VERSION%";
	public static final String HEADER_REPLACE_AUTHORS = "%AUTHORS%";
	
	private static final Pattern HEADER_REPLACE_PATTERN_PLUGIN_NAME = Pattern.compile(HEADER_REPLACE_PLUGIN_NAME, Pattern.LITERAL);
	private static final Pattern HEADER_REPLACE_PATTERN_VERSION = Pattern.compile(HEADER_REPLACE_VERSION, Pattern.LITERAL);
	private static final Pattern HEADER_REPLACE_PATTERN_AUTHORS = Pattern.compile(HEADER_REPLACE_AUTHORS, Pattern.LITERAL);
	
	public static final String HELP_REPLACE_CMD_ALIAS = "%1$s";
	public static final String HELP_REPLACE_SUBCOMMAND  = "%2$s";
	public static final String HELP_REPLACE_SUBCOMMAND_ALIASES  = "%3$s";
	public static final String HELP_REPLACE_ARGUMENTS  = "%4$s";
	public static final String HELP_REPLACE_DESCRIPTION  = "%5$s";
	
	public static final String HELP_MISSING_COMMAND_REPLACE_ARGUMENT = "%1$s";
	
	private final ArrayList<ForgeCommand> registeredCommands = new ArrayList<ForgeCommand>();
	private final HashMap<String, ForgeCommand> aliases = new HashMap<String, ForgeCommand>();
	
	private int numCommandsPerHelpPage = 8;
	private String header;
	private String helpCommandFormat;
	private String helpMissingCommand;
	private String noPermission;
	private boolean showAllCommands = false;
	
	public ForgeCommandHandler(ForgePlugin plugin)
	{
		super(plugin);
		
		setHeaderFormat(String.format("%1$s%3$s %2$sv%1$s%4$s %2$sby %1$s%5$s", ChatColor.DARK_GREEN, ChatColor.YELLOW, HEADER_REPLACE_PLUGIN_NAME, HEADER_REPLACE_VERSION, HEADER_REPLACE_AUTHORS));
		setHelpCommandFormat(String.format("%1$s/%4$s %5$s %2$s%6$s %3$s%7$s", ChatColor.AQUA, ChatColor.DARK_AQUA, ChatColor.YELLOW, HELP_REPLACE_CMD_ALIAS, HELP_REPLACE_SUBCOMMAND_ALIASES, HELP_REPLACE_ARGUMENTS, HELP_REPLACE_DESCRIPTION));
		setHelpMissingCommandFormat(String.format("%sNo sub-commands like %s", ChatColor.RED, HELP_MISSING_COMMAND_REPLACE_ARGUMENT));
		setNoPermissionMessage(String.format("%sNo permission to use this sub-command", ChatColor.RED));
		
		// Setup the help command
		registerAlias("help", true);
		registerAlias("h", false);
		registerAlias("?", false);
		
		registerArgument(new ForgeCommandArgument("^.+$", true, ""));
		
		setArgumentString("[subcommand]");
		setDescription("Shows information about subcommands");
		
		registerCommand(this);
	}
	
	/**
	 * Sets the number of commands shown on each page 
	 */
	public void setNumCommandsPerHelpPage(int count)
	{
		if (count <= 0)
		{
			return;
		}
		
		numCommandsPerHelpPage = count;
	}
	
	/**
	 * Sets the format for the header when running the command without arguments
	 */
	public void setHeaderFormat(String headerFormat)
	{
		if (headerFormat == null)
		{
			return;
		}
		
		header = HEADER_REPLACE_PATTERN_PLUGIN_NAME.matcher(headerFormat).replaceAll(getPlugin().getName());
		header = HEADER_REPLACE_PATTERN_VERSION.matcher(header).replaceAll(getPlugin().getDescription().getVersion());
		header = HEADER_REPLACE_PATTERN_AUTHORS.matcher(header).replaceAll(getPlugin().getAuthors());
	}
	
	/**
	 * Sets the format used for displaying sub-command information
	 */
	public void setHelpCommandFormat(String helpCommandFormat)
	{
		if (helpCommandFormat == null)
		{
			return;
		}
		
		this.helpCommandFormat = helpCommandFormat;
	}
	
	/**
	 * Sets the format for telling a user that the sub-command they were looking for does not exist
	 */
	public void setHelpMissingCommandFormat(String helpMissingCommand)
	{
		if (helpMissingCommand == null)
		{
			return;
		}
		
		this.helpMissingCommand = helpMissingCommand;
	}
	
	/**
	 * Sets the message which is sent to players when they have no permissions to use a sub-command
	 */
	public void setNoPermissionMessage(String noPermission)
	{
		if (noPermission == null)
		{
			return;
		}
		
		this.noPermission = noPermission;
	}
	
	/**
	 * Determines if all commands should be shown in help
	 * If false, only commands which players have permission to use are shown
	 */
	public void setShowAllCommands(boolean showAll)
	{
		showAllCommands = showAll;
	}
	
	/**
	 * Registers the command to its aliases.
	 * Overwrites any aliases already registered by other commands.
	 * @param command The command to register
	 * @throws IllegalArgumentException when a command is given which is already been registered
	 */
	public void registerCommand(ForgeCommand command) throws IllegalArgumentException
	{
		if (!registeredCommands.add(command))
		{
			throw new IllegalArgumentException(String.format("The command '%s' is already been registered", command.getMainCommand()));
		}
		
		for (String alias : command.getAliases())
		{
			aliases.put(alias, command);
		}
	}
	
	private final ForgeCommand findCommand(String like)
	{
		ForgeCommand command = aliases.get(like);
		
		if (command == null)
		{
			for (Entry<String, ForgeCommand> e : aliases.entrySet())
			{
				if (e.getKey().startsWith(like))
				{
					command = e.getValue();
					break;
				}
			}
		}
		
		return command;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (args.length == 0)
		{
			sender.sendMessage(header);
			if (!registeredCommands.isEmpty())
			{
				sender.sendMessage(String.format("%sType /%s help to see subcommands", ChatColor.AQUA, label));
			}
			return true;
		}
		
		// Find the command
		ForgeCommand subCommand = findCommand(args[0]);
		
		// Check if we found the sub-command
		if (subCommand == null)
		{
			sender.sendMessage(String.format(helpMissingCommand, args[0]));
			return true;
		}
		
		// Make sure the first argument contains the main command
		args[0] = subCommand.getMainCommand();
		
		boolean isPlayer = sender instanceof Player;
		
		if (!isPlayer && !subCommand.allowConsole())
		{
			sender.sendMessage(String.format("%1$sOnly players can use this sub-command", ChatColor.RED));
			return true;
		}
		
		// If the sender is a player and does not have permission for the command we return
		if (isPlayer && !subCommand.checkPermissions((Player) sender))
		{
			sender.sendMessage(noPermission);
			return true;
		}
		
		// Create the argument object
		ForgeArgs arguments = new ForgeArgs(label, args);
		
		// Validate the arguments
		if (!subCommand.validateArguments(sender, arguments))
		{
			sender.sendMessage(String.format("%1$sType %2$s/%s help %3$s%s %1$sto see how to use this command", ChatColor.YELLOW, ChatColor.AQUA, ChatColor.DARK_AQUA, label, arguments.getSubCommandAlias()));
			return true;
		}
		
		subCommand.onCommand(sender, arguments);
		
		return true;
	}
	
	/**
	 * The handler for the help command
	 */
	@Override
	protected final void onCommand(CommandSender sender, ForgeArgs args)
	{
		boolean player = sender instanceof Player;
		
		sender.sendMessage(header);
		
		boolean hasPage = false;
		// Shows help for one command
		if (args.getNumArgs() == 1 && !(hasPage = NUMBER.matcher(args.getArg(0)).matches()))
		{
			ForgeCommand command = findCommand(args.getArg(0));
			
			if (command != null)
			{
				if (!player || showAllCommands || command.checkPermissions((Player) sender))
				{
					sender.sendMessage(String.format(helpCommandFormat, args.getCommandUsed(), args.getSubCommandAlias(), command.getAliasString(), command.getArgString(), command.getDescription()));
				}
				else
				{
					sender.sendMessage(noPermission);
				}
			}
			else
			{
				sender.sendMessage(String.format(helpMissingCommand, args.getArg(0)));
			}
			return;
		}
		
		int page = 1;
		if (hasPage)
		{
			page = Integer.valueOf(args.getArg(0));
		}
		
		sender.sendMessage(String.format("%1$sShowing page %2$s%3$d %1$sof %2$s%4$d", ChatColor.YELLOW, ChatColor.AQUA, page, registeredCommands.size() / numCommandsPerHelpPage + 1));
		
		// Show help for all commands
		for (int i = (page - 1) * numCommandsPerHelpPage; i < registeredCommands.size() && i < page * numCommandsPerHelpPage; ++i)
		{
			ForgeCommand command = registeredCommands.get(i);
			
			// Only show the command to the player if they are allowed to see it
			if (!player || showAllCommands || command.checkPermissions((Player) sender))
			{
				sender.sendMessage(String.format(helpCommandFormat, args.getCommandUsed(), command.getMainCommand(), command.getAliasString(), command.getArgString(), command.getDescription()));
			}
		}
	}
}
