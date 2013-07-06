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

package com.forgenz.horses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.command.ForgeCommandHandler;
import com.forgenz.forgecore.v1_0.locale.ForgeLocale;
import com.forgenz.horses.command.BuyCommand;
import com.forgenz.horses.command.DeleteCommand;
import com.forgenz.horses.command.DismissCommand;
import com.forgenz.horses.command.HealCommand;
import com.forgenz.horses.command.ListCommand;
import com.forgenz.horses.command.ReloadCommand;
import com.forgenz.horses.command.RenameCommand;
import com.forgenz.horses.command.SummonCommand;
import com.forgenz.horses.command.TypeCommand;
import com.forgenz.horses.config.HorsesConfig;
import com.forgenz.horses.database.HorseDatabase;
import com.forgenz.horses.database.YamlDatabase;
import com.forgenz.horses.listeners.DamageListener;
import com.forgenz.horses.listeners.HorseDeathListener;
import com.forgenz.horses.listeners.InteractListener;
import com.forgenz.horses.listeners.PlayerListener;
import com.forgenz.horses.listeners.TeleportListener;
import com.forgenz.horses.metrics.Metrics;
import com.forgenz.horses.tasks.HorseDismissTask;

public class Horses extends ForgePlugin
{
	private static Horses plugin;
	
	private ForgeLocale locale;
	private HorsesConfig config;
	private HorseDatabase database;
	private ForgeCommandHandler commandHandler;
	
	private HorseDismissTask horseDismissTask;
	
	public static Horses getInstance()
	{
		return plugin;
	}
	
	@Override
	public void onLoad()
	{
	}

	@Override
	public void onEnable()
	{
		plugin = this;
		
		// Try setup Economy
		setupEconomy();
		
		File configurationFile = new File(getDataFolder(), "config.yml");
		try
		{
			YamlConfiguration cfg = new YamlConfiguration();
			
			cfg.load(configurationFile);
		}
		catch (InvalidConfigurationException e)
		{
			configurationFile.renameTo(new File(getDataFolder(), "config.yml.broken"));
		}
		catch (FileNotFoundException e) {}
		catch (IOException e) {}
		
		reloadConfig();
		
		// Setup messages
		locale = new ForgeLocale(this);
		locale.registerEnumMessages(Messages.class);
		locale.updateMessages();
		
		// Setup commands
		commandHandler = new ForgeCommandHandler(this);
		getCommand("horses").setExecutor(commandHandler);
		commandHandler.setNumCommandsPerHelpPage(5);
		
		// Register each command
		commandHandler.registerCommand(new BuyCommand(this));
		commandHandler.registerCommand(new DeleteCommand(this));
		commandHandler.registerCommand(new DismissCommand(this));
		commandHandler.registerCommand(new HealCommand(this));
		commandHandler.registerCommand(new ListCommand(this));
		commandHandler.registerCommand(new RenameCommand(this));
		commandHandler.registerCommand(new SummonCommand(this));
		commandHandler.registerCommand(new TypeCommand(this));
		
		// Admin commands
		commandHandler.registerCommand(new ReloadCommand(this));
		
		// Setup the config
		config = new HorsesConfig(this);
		
		if (config.showAuthor)
		{
			commandHandler.setHeaderFormat(String.format("%1$s%3$s %2$sv%1$s%4$s %2$sby %1$s%5$s", ChatColor.DARK_GREEN, ChatColor.YELLOW, "Horses", ForgeCommandHandler.HEADER_REPLACE_VERSION, "ShadowDog007"));
		}
		else
		{
			commandHandler.setHeaderFormat(String.format("%1$s%3$s %2$sv%1$s%4$s", ChatColor.DARK_GREEN, ChatColor.YELLOW, "Horses", ForgeCommandHandler.HEADER_REPLACE_VERSION));
		}
		
		// Try setup WorldGuard
		setupWorldGuard(config.worldGuardCfg != null);
		
		// Setup the database
		database = new YamlDatabase(this);
		
		// Register the Listeners
		if (config.invincibleHorses || config.protectFromOwner || config.protectFromPlayers || config.protectFromMobs || config.protectFromBurning)
			new DamageListener(this);
		new HorseDeathListener(this);
		new InteractListener(this);
		new PlayerListener(this);
		new TeleportListener(this);
		
		horseDismissTask = new HorseDismissTask(this);
		horseDismissTask.runTaskTimer(this, 20L, 10L);
		
		// Start metrics
		try
		{
			Metrics metrics = new Metrics(this);
			
			metrics.start();
		}
		catch (IOException e)
		{
			log(Level.WARNING, "Oh noes, Metrics failed to start :(", e);
		}
		
		saveConfig();
	}

	@Override
	public void onDisable()
	{
		unregisterListeners();
		
		database.saveAll();
		horseDismissTask.cancel();
		commandHandler = null;
		database = null;
		config = null;
		locale = null;
		plugin = null;
	}
	
	public HorsesConfig getHorsesConfig()
	{
		return config;
	}
	
	public HorseDatabase getHorseDatabase()
	{
		return database;
	}
}
