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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.forgenz.forgecore.v1_0.ForgeCore;

public abstract class ForgeListener implements ForgeCore, Listener
{
	private static final HashMap<String, Set<ForgeListener>> registeredListeners = new HashMap<String, Set<ForgeListener>>();
	
	protected final ForgePlugin plugin;

	private String registeredKey = null;
	
	public ForgeListener(ForgePlugin plugin)
	{
		this(plugin, false);
	}
	
	public ForgeListener(ForgePlugin plugin, boolean register)
	{
		if (plugin == null)
		{
			throw new IllegalArgumentException("Listener requires a valid plugin reference");
		}
		
		this.plugin = plugin;
		
		if (register)
		{
			register();
		}
	}
	
	@Override
	public ForgePlugin getPlugin()
	{
		return plugin;
	}
	
	/**
	 * Registers the listener with the key being the plugins name
	 */
	public final void register()
	{
		plugin.registerListener(this);
	}
	
	/**
	 * Registers the listener with the given key
	 * @param key
	 */
	public final void register(String key)
	{
		// Make sure the listener is not already registered
		if (registeredKey != null)
		{
			throw new IllegalStateException("Listener is already registered");
		}
		
		// Register the listener
		Bukkit.getPluginManager().registerEvents(this, plugin);
		
		// Add the listener to the registered listener map
		getListeners(key).add(this);
		
		// Update the state of the Listener
		registeredKey = key;
	}
	
	/**
	 * Unregisters the current listener
	 */
	public final void unregister()
	{
		// Check if we are actually registered
		if (registeredKey == null)
		{
			throw new IllegalStateException("Listener is not registered");
		}
		
		// Unregister the listener
		HandlerList.unregisterAll(this);
		
		// Remove the listener from the registered listener map
		Set<ForgeListener> listeners = getListeners(registeredKey);
		listeners.remove(this);
		if (listeners.isEmpty())
		{
			registeredListeners.remove(registeredKey);
		}
		
		// Update the listeners state
		registeredKey = null;
	}
	
	private final static Set<ForgeListener> getListeners(String key)
	{
		Set<ForgeListener> listeners = registeredListeners.get(key);
		
		if (listeners == null)
		{
			listeners = new HashSet<ForgeListener>();
			registeredListeners.put(key, listeners);
		}
	
		return listeners;
	}
	
	public final static void unregisterAll(String key)
	{
		Set<ForgeListener> listeners = getListeners(key);
		
		for (ForgeListener listener : listeners)
		{
			HandlerList.unregisterAll(listener);
		}
		
		listeners.clear();
		registeredListeners.remove(key);
	}
}
