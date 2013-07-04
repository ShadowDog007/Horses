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

package com.forgenz.forgecore.v1_0.component;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.forgenz.forgecore.v1_0.ForgeCoreEntity;
import com.forgenz.forgecore.v1_0.bukkit.ForgeListener;
import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.util.RandomUtil;


public abstract class ForgeComponent implements ForgeCoreEntity
{
	private final ForgePlugin plugin;
	private final String name;
	
	private final ForgeComponentLogger logger;
	
	private final String listenerKey;
	
	private boolean isEnabled = false;
	
	protected ForgeComponent(ForgePlugin plugin, String name)
	{		
		this.plugin = plugin;
		this.name = name;
		
		logger = new ForgeComponentLogger(plugin, getClass(), name);
		
		byte[] chars = new byte[8];
		RandomUtil.get().nextBytes(chars);
		
		listenerKey = String.format("%s-%s", new String(chars), name);
		
		onLoad();
	}
	
	public abstract boolean onLoad();
	
	public abstract boolean onEnable();
	
	public abstract boolean onDisable();
	
	protected final boolean setEnabled(boolean enabled)
	{
		if (isEnabled != enabled)
		{
			isEnabled = enabled;
			
			try
			{
				if (enabled)
				{
					if (!onEnable())
					{
						isEnabled = false;
					}
				}	
				else
				{
					if (!onDisable())
					{
						isEnabled = true;
					}
				}
			}
			catch (Throwable e)
			{
				log(Level.SEVERE, "Error occured when %s component", e, enabled ? "enabling" : "disabling");
				isEnabled = !enabled;
			}
		}
		
		return isEnabled == enabled;
	}
	
	public final ForgePlugin getPlugin()
	{
		return plugin;
	}
	
	public final void registerListener(ForgeListener listener)
	{
		listener.register(name);
	}
	
	/**
	 * Unregisters all listeners which belong to this component
	 */
	public final void unregisterListeners()
	{
		ForgeListener.unregisterAll(listenerKey);
	}
	
	public final Logger getLogger()
	{
		return logger;
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
}
