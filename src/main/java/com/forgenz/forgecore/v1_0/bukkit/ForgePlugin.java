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

package com.forgenz.forgecore.v1_0.bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.forgenz.forgecore.v1_0.ForgeCoreEntity;
import com.forgenz.forgecore.v1_0.util.RandomUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public abstract class ForgePlugin extends JavaPlugin implements ForgeCoreEntity
{
	private String listenerKey;
	
	private WorldGuardPlugin worldGuard;
	private Economy econ;
	
	protected ForgePlugin()
	{
		byte[] chars = new byte[16];
		RandomUtil.get().nextBytes(chars);
		listenerKey = new String(chars);
	}
	
	@Override
	public abstract void onLoad();
	
	@Override
	public abstract void onEnable();
	
	public abstract void onDisable();
	
	protected boolean setupWorldGuard(boolean setup)
	{
		if (!setup)
		{
			worldGuard = null;
			return false;
		}
		
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
		
		if (plugin == null || !(plugin instanceof WorldGuardPlugin))
		{
			worldGuard = null;
			return false;
		}
		
		worldGuard = (WorldGuardPlugin) plugin;
		return true;
	}
	
	public WorldGuardPlugin getWorldGuard()
	{
		return worldGuard;
	}
	
	protected boolean setupEconomy()
	{
		try
		{
			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
			
			if (economyProvider != null)
			{
				econ = economyProvider.getProvider();
			}
			
			return econ != null;
		}
		catch (Throwable e)
		{
			return false;
		}
	}
	
	public Economy getEconomy()
	{
		return econ;
	}
	
	public void getResourseString(String resourse, StringBuilder resourseStr)
	{
		if (resourse == null)
		{
			getPlugin().severe("Resourse file name was not given", new NullPointerException());
			return;
		}
		
		InputStream resourseStream = getPlugin().getResource(resourse);
		
		if (resourseStream == null)
		{
			getPlugin().severe("The resourse '%s' was not found inside the %s's jar", resourse, getPlugin().getName());
			return;
		}
		
		try
		{
			copyStream(resourseStream, resourseStr);
		}
		catch (IOException e)
		{
			getPlugin().severe("Error when trying to read resourse '%s'", e, resourse);
		}
	}
	
	public String getResourseString(String resourse)
	{
		if (resourse == null)
		{
			getPlugin().severe("Resourse file name was not given", new NullPointerException());
			return "";
		}
		
		InputStream headerStream = getPlugin().getResource(resourse);
		
		if (headerStream == null)
		{
			getPlugin().severe("The resourse '%s' was not found inside the %s's jar", resourse, getPlugin().getName());
			return "";
		}
		
		StringBuilder resourseStr = new StringBuilder();
		try
		{
			copyStream(headerStream, resourseStr);
		}
		catch (IOException e)
		{
			getPlugin().severe("Error when trying to read resourse '%s'", e, resourse);
		}
		
		return resourseStr.toString();
	}
	
	public void copyStream(InputStream stream, StringBuilder copyTo) throws IOException
	{
		byte[] bytes = new byte[64];
		int size = 0;
		
		while ((size = stream.read(bytes)) != -1)
		{
			copyTo.ensureCapacity(copyTo.length() + size);
				
				for (int i = 0; i < size; ++i)
				{
					copyTo.append((char) bytes[i]);
				}
		}
	}
	
	@Override
	public ForgePlugin getPlugin()
	{
		return this;
	}
	
	@Override
	public void registerListener(ForgeListener listener)
	{
		listener.register(listenerKey);
	}

	@Override
	public void unregisterListeners()
	{
		ForgeListener.unregisterAll(listenerKey);
	}
	
	@Override
	public String getAuthors()
	{
		StringBuilder b = new StringBuilder();
		
		for (String author : getDescription().getAuthors())
		{
			if (b.length() != 0)
			{
				b.append(", ");
			}
			
			b.append(author);
		}
		
		return b.toString();
	}
	
	public void log(Level level, String message)
	{
		getLogger().log(level, message);
	}
	
	public void log(Level level, String message, Object ...args)
	{
		log(level, String.format(message, args));
	}
	
	public void log(Level level, String message, Throwable e)
	{
		getLogger().log(level, message, e);
	}
	
	public void log(Level level, String message, Throwable e, Object ...args)
	{
		log(level, String.format(message, args), e);
	}
	
	public void info(String message)
	{
		log(Level.INFO, message);
	}
	
	public void info(String message, Object ...args)
	{
		log(Level.INFO, message, args);
	}
	
	public void warning(String message)
	{
		log(Level.WARNING, message);
	}
	
	public void warning(String message, Object ...args)
	{
		log(Level.WARNING, message, args);
	}
	
	public void severe(String message)
	{
		log(Level.SEVERE, message);
	}
	
	public void severe(String message, Object ...args)
	{
		log(Level.SEVERE, message, args);
	}
	
	public void severe(String message, Throwable e, Object ...args)
	{
		log(Level.SEVERE, message, e, args);
	}
}
