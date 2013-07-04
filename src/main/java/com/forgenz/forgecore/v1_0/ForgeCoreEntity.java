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

package com.forgenz.forgecore.v1_0;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.forgenz.forgecore.v1_0.bukkit.ForgeListener;

public interface ForgeCoreEntity extends ForgeCore
{	
	/**
	 * Registers a listener with this entity
	 * @param listener The listener to register
	 */
	public void registerListener(ForgeListener listener);
	
	/**
	 * Unregisters all listeners which belong to this entity
	 */
	public void unregisterListeners();
	
	/**
	 * Creates a string list of people which created the entity
	 * @return List of peoples names
	 */
	public String getAuthors();
	
	/**
	 * Fetches the logger for this entity
	 * @return The logger
	 */
	public Logger getLogger();
	
	/**
	 * Logs a message to console
	 * @param level The level of the message
	 * @param message The message
	 */
	public void log(Level level, String message);

	/**
	 * Logs a message to console
	 * @param level The level of the message
	 * @param message The message with replace tags
	 * @param args The arguments to add to the message
	 */
	public void log(Level level, String message, Object ...args);
	
	/**
	 * Logs a message to console along with an exception
	 * @param level The level of the message
	 * @param message The message
	 * @param e The exception
	 */
	public void log(Level level, String message, Throwable e);
	
	/**
	 * Logs a message to console along with an exception
	 * @param level The level of the message
	 * @param message The message with replace tags
	 * @param e The exception
	 * @param args The arguments to add to the message
	 */
	public void log(Level level, String message, Throwable e, Object ...args);
	
	/**
	 * Logs info to console
	 * @param message The message
	 */
	public void info(String message);
	
	/**
	 * Logs info to console
	 * @param message The message with replace tags
	 * @param args The arguments to add to the message
	 */
	public void info(String message, Object ...args);
	
	/**
	 * Logs a warning to console
	 * @param message The message
	 */
	public void warning(String message);
	
	/**
	 * Logs a warning to console
	 * @param message The message with replace tags
	 * @param args The arguments to add to the message
	 */
	public void warning(String message, Object ...args);
	
	/**
	 * Logs a severe error to console
	 * @param message The message
	 */
	public void severe(String message);
	
	/**
	 * Logs a severe error to console
	 * @param message The message with replace tags
	 * @param args The arguments to add to the message
	 * @param args
	 */
	public void severe(String message, Object ...args);
}
