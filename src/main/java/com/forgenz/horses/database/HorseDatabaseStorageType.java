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
 
package com.forgenz.horses.database;

import java.lang.reflect.InvocationTargetException;

import com.forgenz.horses.Horses;

public enum HorseDatabaseStorageType
{
	/**
	 * Does not store any data</br>
	 * 
	 * Used for testing purposes and also as a fallback for incorrectly setup databases
	 */
	DUMMY(DummyDatabase.class),
	
	/**
	 * Uses YAML Files to store horse/stable data for each player
	 */
	YAML(YamlDatabase.class),
	
	/**
	 * Uses a MySQL database to store Horse/Stable data for each player
	 */
	MYSQL(MysqlDatabase.class);
	
	private final Class<? extends HorseDatabase> clazz;
	
	private HorseDatabaseStorageType(Class<? extends HorseDatabase> clazz)
	{
		this.clazz = clazz;
	}
	
	public HorseDatabase create(Horses plugin, boolean fallback)
	{
		try
		{
			try
			{
				return clazz.getConstructor(Horses.class).newInstance(plugin);
			}
			catch (NoSuchMethodException e)
			{
				plugin.severe("Failed to find constructor for the %s database type", e, toString());
				throw e;
			}
			catch (InvocationTargetException e)
			{
				plugin.severe("Error occured when attempting to create the database of type %s", e.getTargetException(), toString());
				throw e;
			}
		}
		catch (Throwable e)
		{
			if (fallback)
			{
				plugin.severe("#################################");
				plugin.severe("Falling back to a dummy database");
				plugin.severe("WARNING: No data will be saved");
				plugin.severe("#################################");
			}
			return fallback ? DUMMY.create(plugin, false) : null;
		}
	}
	
	public static HorseDatabaseStorageType getFromString(String str)
	{
		for (HorseDatabaseStorageType type : values())
		{
			if (type.toString().equals(str))
				return type;
		}
		
		return null;
	}
}
