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

package com.forgenz.forgecore.v1_0.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class BukkitConfigUtil
{
	private BukkitConfigUtil() {}
	
	public static YamlConfiguration getConfig(File file)
	{
		YamlConfiguration cfg = new YamlConfiguration();
		
		try
		{
			cfg.load(file);
		}
		catch (FileNotFoundException e) {}
		catch (IOException e) {}
		catch (InvalidConfigurationException e)
		{
			file.renameTo(new File(file.getPath() + ".broken"));
		}
		
		return cfg;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getAndSet(ConfigurationSection cfg, String path, Class<T> clazz, T def)
	{
		Object obj = cfg.get(path, def);
		
		if (obj == null || !clazz.isAssignableFrom(obj.getClass()))
		{
			return null;
		}
		
		cfg.set(path, obj);
		
		return (T) obj;
	}
	
	public static ConfigurationSection getAndSetConfigurationSection(ConfigurationSection cfg, String path)
	{
		ConfigurationSection sect = cfg.getConfigurationSection(path);
		
		if (sect != null)
		{
			cfg.set(path, sect);
		}
		else
		{
			sect = cfg.createSection(path);
		}
		
		return sect;
	}
}
