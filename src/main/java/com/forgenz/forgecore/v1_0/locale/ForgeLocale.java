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

package com.forgenz.forgecore.v1_0.locale;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.util.BukkitConfigUtil;

public final class ForgeLocale implements ForgeCore
{
	public static final String LOCALE_CONFIG_LOCATION = String.format("locale%s%s.yml", File.separatorChar, "%s");
	
	private ForgeMessage[] messages; 
	
	private final ForgePlugin plugin;
	
	public ForgeLocale(ForgePlugin plugin)
	{
		this.plugin = plugin;
	}
	
	public final void registerEnumMessages(Class<? extends Enum<?>> clazz)
	{
		Enum<?>[] constants = clazz.getEnumConstants();
		
		if (!(constants[0] instanceof ForgeLocaleEnum))
		{
			throw new IllegalArgumentException("Enum must implement ForgeLocaleEnum");
		}
		
		ForgeLocaleEnum[] locale = (ForgeLocaleEnum[]) constants;
		
		messages = new ForgeMessage[locale.length];
		
		for (int i = 0; i < messages.length; ++i)
		{
			messages[i] = locale[i].getMessage();
		}
	}
	
	public final void updateMessages()
	{
		String localeStr = BukkitConfigUtil.getAndSet(plugin.getConfig(), "locale", String.class, "en");
		
		File locale = new File(plugin.getDataFolder(), String.format(LOCALE_CONFIG_LOCATION, localeStr));
		
		FileConfiguration cfg = new YamlConfiguration();
		
		try
		{
			if (!locale.exists())
			{
				try
				{
					InputStream fin = plugin.getResource(localeStr);
					
					if (fin != null)
					{
						cfg.load(fin);
					}
					
					cfg.save(locale);
				}
				catch (InvalidConfigurationException e)
				{
					plugin.log(Level.WARNING, "Locale embeded in plugin is invalid", e);
					return;
				}
			}
			else
			{
				try
				{
					cfg.load(locale);
				}
				catch (IllegalArgumentException e)
				{
					
				}
				catch (InvalidConfigurationException e)
				{
					plugin.log(Level.WARNING, "Your Locale file %s is invalid", e, locale.getName());
					locale.delete();
					updateMessages();
					return;
				}
				
				InputStream fin = plugin.getResource(localeStr);
				
				if (fin != null)
				{
					FileConfiguration defaults = new YamlConfiguration();
					defaults.load(fin);
					cfg.addDefaults(defaults);
					cfg.options().copyDefaults(true);
				}
			}
			
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InvalidConfigurationException e)
		{
			plugin.log(Level.WARNING, "Locale embeded in plugin is invalid", e);
			return;
		}
		
		for (ForgeMessage m : messages)
		{
			m.updateMessage(cfg);
		}
		
		try
		{
			cfg.save(locale);
		}
		catch (IOException e)
		{
			getPlugin().log(Level.WARNING, "Failed to save locale file %s", e, locale.getName());
		}
	}
	
	public final String getMessage(Enum<?> instance)
	{
		return messages[instance.ordinal()].getMessage();
	}
	
	@Override
	public ForgePlugin getPlugin()
	{
		return plugin;
	}
}
