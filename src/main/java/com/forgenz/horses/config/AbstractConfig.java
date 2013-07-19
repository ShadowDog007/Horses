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

package com.forgenz.horses.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;
import org.bukkit.plugin.PluginDescriptionFile;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.horses.Horses;

public abstract class AbstractConfig implements ForgeCore
{
	private final Horses plugin;
	private final AbstractConfig parentCfg;
	private final String folder;
	private final String fileName;
	
	
	private YamlConfiguration cfg;
	
	protected AbstractConfig(Horses plugin, YamlConfiguration cfg)
	{
		this.plugin = plugin;
		this.cfg = cfg;
		
		parentCfg = null;
		folder = fileName = null;
	}
	
	protected AbstractConfig(Horses plugin, AbstractConfig parentCfg, String folder, String fileName)
	{
		this.plugin = plugin;
		this.parentCfg = parentCfg;
		this.folder = folder;
		this.fileName = fileName + ".yml";
	}
	
	protected AbstractConfig(Horses plugin, YamlConfiguration cfg, AbstractConfig parent, String folder, String fileName, boolean standalone)
	{
		this.plugin = plugin;
		
		if (standalone)
		{
			this.parentCfg = parent;
			this.folder = folder;
			this.fileName = fileName + ".yml";
		}
		else
		{
			this.cfg = cfg;
			parentCfg = null;
			this.folder = this.fileName = null;
		}
	}
	
	private String getFolder()
	{
		String folder = parentCfg != null ? parentCfg.getFolder() : "";
		
		if (this.folder != null)
		{
			folder += File.separatorChar + this.folder;
		}
		
		return folder;
	}
	
	private String getPath()
	{		
		return String.format("%s%s%s", getFolder(), File.separator, fileName);
	}
	
	protected void addResourseToHeader(String resourse)
	{
		if (cfg == null)
			return;
		
		addStringToHeader(getPlugin().getResourseString(resourse));
	}
	
	protected void addStringToHeader(String header)
	{
		if (cfg == null)
			return;
		
		YamlConfigurationOptions options = cfg.options();
		
		if (options.header() != null)
			header = options.header() + header;
		
		options.header(header);
		options.copyHeader(true);
	}
	
	protected YamlConfiguration loadConfiguration()
	{
		if (cfg == null)
		{
			cfg = new YamlConfiguration();
			
			String path = getPath();
			
			File cfgFile = new File(getPlugin().getDataFolder(), path);
			
			// Try load the configuration
			if (cfgFile.exists())
			{
				try
				{
					cfg.load(cfgFile);
				}
				catch (FileNotFoundException e) {}
				catch (IOException e) {}
				catch (InvalidConfigurationException e)
				{
					getPlugin().log(Level.SEVERE, "Failed to load configuration %1$s. Saving as %1$s.broken", e, path);
					cfgFile.renameTo(new File(getPlugin().getDataFolder(), path + ".broken"));
				}
			}
			
			// Clear the current header
			cfg.options().header(null);
			
			// Add the Top part of the header to the file
			PluginDescriptionFile pdf = getPlugin().getDescription();
			addStringToHeader(String.format("%s v%s by %s\n%s\n\n", getPlugin().getName(), pdf.getVersion(), getPlugin().getAuthors(), pdf.getWebsite()));
		}		
		
		return cfg;
	}
	
	protected void saveConfiguration()
	{
		if (cfg == null)
			return;
		
		if (fileName == null)
		{
			cfg = null;
			return;
		}
		
		String path = getPath();
		
		File cfgFile = new File(getPlugin().getDataFolder(), path);
		
		try
		{
			cfg.save(cfgFile);
		}
		catch (IOException e)
		{
			getPlugin().log(Level.WARNING, "Error when attempting to save the config file %s to disk", e, path);
		}
		
		cfg = null;
	}
	
	protected <T> T getAndSet(String path, T def, Class<T> clazz)
	{
		return getAndSet(cfg, path, def, clazz);
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T getAndSet(ConfigurationSection cfg, String path, T def, Class<T> clazz)
	{
		Object obj = cfg.get(path);
		
		if (obj == null || !clazz.isAssignableFrom(obj.getClass()))
		{
			obj = def;
		}
		
		set(cfg, path, obj);
		
		return (T) obj;
	}
	
	protected ConfigurationSection getConfigSect(String path)
	{
		return getConfigSect(cfg, path);
	}
	
	protected ConfigurationSection getConfigSect(ConfigurationSection cfg, String path)
	{
		ConfigurationSection sect = cfg.getConfigurationSection(path);
		
		if (sect == null)
			sect = cfg.createSection(path);
		
		set(cfg, path, sect);
		
		return sect;
	}
	
	protected void set(String path, Object value)
	{
		set(cfg, path, value);
	}
	
	protected void set(ConfigurationSection sect, String path, Object value)
	{
		sect.set(path, null);
		sect.set(path, value);
	}
	
	@Override
	public Horses getPlugin()
	{
		return plugin;
	}
}
