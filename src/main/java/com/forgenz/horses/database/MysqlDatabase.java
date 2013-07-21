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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import com.forgenz.horses.HorseType;
import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import com.forgenz.horses.config.AbstractConfig;

public class MysqlDatabase  extends HorseDatabase
{
	private final YamlConfiguration cacheCfg = new YamlConfiguration();
	private final ArrayList<?> cacheItemList = new ArrayList<Object>();
	
	private final MysqlSettings settings;
	
	private Connection conn;
	private long lastCheck;
	private int spam;
	
	public MysqlDatabase(Horses plugin) throws DatabaseConnectException, SQLException
	{
		super(plugin, HorseDatabaseStorageType.MYSQL);
		
		settings = new MysqlSettings(plugin);
		
		// Check if we can connect to the database
		if (!connect())
		{
			throw new DatabaseConnectException("Failed to connect to MySQL database");
		}
		
		try
		{
			createTables("Horses", "Stables");
			
			checkColumn("Stables", "user", "VARCHAR(16) NOT NULL");
			checkColumn("Stables", "lastactive", "VARCHAR(30) NOT NULL");
			addUniqueIndex("Stables", "user");
			
			checkColumn("Horses", "stableid", "INT NOT NULL DEFAULT '0' AFTER `id`");
			checkColumn("Horses", "stablegroup", "VARCHAR(30) NOT NULL DEFAULT '" + HorseDatabase.DEFAULT_GROUP + "' COLLATE utf8_general_ci AFTER `stableid`");
			checkColumn("Horses", "name", "VARCHAR(30) NOT NULL DEFAULT '' COLLATE utf8_general_ci AFTER `stablegroup`");
			checkColumn("Horses", "type", "VARCHAR(16) NOT NULL DEFAULT '' COLLATE utf8_general_ci AFTER `name`");
			checkColumn("Horses", "lastDeath", "BIGINT NOT NULL DEFAULT '0' AFTER `type`");
			checkColumn("Horses", "maxhealth", "DOUBLE NOT NULL DEFAULT '20' AFTER `lastDeath`");
			checkColumn("Horses", "health", "DOUBLE NOT NULL DEFAULT '20' AFTER `maxhealth`");
			checkColumn("Horses", "jumpstrength", "DOUBLE NOT NULL DEFAULT '0.7' AFTER `health`");
			checkColumn("Horses", "chested", "TINYINT NOT NULL DEFAULT '0' AFTER `jumpstrength`");
			checkColumn("Horses", "inventory", "VARCHAR(10000) NOT NULL DEFAULT 'i: []' COLLATE utf8_general_ci AFTER `chested`");
			
		}
		catch (SQLException e)
		{
			plugin.severe("Failed to create MySQL Tables");
			throw e;
		}
	}
	
	private void createTables(String ...tables) throws SQLException
	{
		for (String table : tables)
		{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + table + "` ("
					+ "`id` INT NOT NULL AUTO_INCREMENT,"
					+ "PRIMARY KEY (`id`)) ENGINE=InnoDB");
		}
	}
	
	private void checkColumn(String table, String column, String settings) throws SQLException
	{
		try
		{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(String.format("ALTER TABLE `%1$s` CHANGE `%2$s` `%2$s` %3$s", table, column, settings));
		}
		catch (SQLException e)
		{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(String.format("ALTER TABLE `%1$s` ADD `%2$s` %3$s", table, column, settings));
		}
	}
	
	private void addUniqueIndex(String table, String column) throws SQLException
	{
		Statement stmt = conn.createStatement();
		ResultSet result = stmt.executeQuery(String.format("SELECT * FROM INFORMATION_SCHEMA.STATISTICS WHERE table_schema=DATABASE() AND table_name='%s' AND index_name='%s'", table, column));
		
		if (result.next())
			return;
		
		stmt = conn.createStatement();
		stmt.executeUpdate(String.format("ALTER TABLE `%s` ADD UNIQUE (`$s`)", table, column));
	}
	
	private boolean connect()
	{
		// Reuse the old connection
		if (conn != null)
		{
			try
			{
				// Only check if it is closed if it hasn't been used in the last minute
				if (System.currentTimeMillis() - lastCheck > 60000)
				{
					if (!conn.isClosed())
					{
						lastCheck = System.currentTimeMillis();
						return true;
					}
				}
			}
			catch (SQLException e){}
		}
		
		String connection = String.format("jdbc:mysql://%s/%s", settings.host, settings.database);
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			
			conn = DriverManager.getConnection(connection, settings.user, settings.password);
		}
		catch (SQLException e)
		{
			if (spam++ < 20)
				getPlugin().severe("Failed to connect to the MySQL database '%s'", e, connection);
			else
				getPlugin().severe("Failed to connect to the MySQL database '%s'. See above for error", connection);
		}
		catch (ClassNotFoundException e)
		{
			if (spam++ < 20)
				getPlugin().severe("Couldn't find MySQL driver", e);
			else
				getPlugin().severe("Couldn't find MySQL driver. See above for error");
		}
		
		lastCheck = System.currentTimeMillis();
		return conn != null;
	}

	@Override
	protected Stable loadStable(String player, String stableGroup)
	{
		if (!connect())
			return null;
		
		try
		{
			Statement stmt = conn.createStatement();
			
			ResultSet result = stmt.executeQuery(String.format("SELECT * FROM `Stables` WHERE `user`='%s'", player));
			
			int id = -1;
			String lastActive = null;
			
			// Fetch the stables id
			while (result.next())
			{
				id = result.getInt("id");
				lastActive = result.getString("lastactive");
			}
				
			Stable stable = new Stable(getPlugin(), stableGroup, player, id);
			
			// Load the horses
			loadHorses(stable, stableGroup);
			
			// Try to find the last active horse
			if (lastActive != null)
			{
				PlayerHorse horse = stable.findHorse(lastActive, true);
				stable.setLastActiveHorse(horse);
			}
			
			return stable;
		}
		catch (SQLException e)
		{
			getPlugin().severe("Failed to load players Stable: '%s'", e, player);
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void loadHorses(Stable stable, String stableGroup)
	{
		// Assume we have a connection as this is only called from loadStable
		
		try
		{
			// Query the SQL server for horse data for the stable
			Statement stmt = conn.createStatement();
			ResultSet result = stmt.executeQuery(String.format("SELECT * FROM `Horses` WHERE `stableid`='%d' AND `stablegroup`='%s'", stable.getId(), stableGroup));
			
			// Create each horse
			while (result.next())
			{
				try
				{
					// Fetch data for the horse
					int horseId = result.getInt("id");
					
					String name = result.getString("name");
					
					HorseType type = HorseType.exactValueOf(result.getString("type"));
					
					long lastDeath = result.getLong("lastdeath");
					double maxHealth = result.getDouble("maxhealth");
					double health = result.getDouble("health");
					double jumpStrength = result.getDouble("jumpstrength");
					boolean hasChest = type == HorseType.Mule || type == HorseType.Donkey ? result.getBoolean("chested") : false;
					
					// Create a configuration from the inventory string
					YamlConfiguration itemCfg = cacheCfg;
					ArrayList<ItemStack> items = null;
					try
					{
						itemCfg.loadFromString(result.getString("inventory"));
					}
					catch (InvalidConfigurationException e)
					{
						getPlugin().severe("Error when loading player %s's horses inventory", e, stable.getOwner());
					}
					
					// Create ItemStacks for the horses inventory
					for (Map<?, ?> itemMap : itemCfg.getMapList("i"))
					{
						int slot = -1;
						
						try
						{
							slot = (Integer) itemMap.get("slot");
							
						}
						catch (NullPointerException e)
						{
							getPlugin().log(Level.SEVERE, "Player '%s' mysql data is corrupt: Inventory slot number was missing", e, stable.getOwner());
							continue;
						}
						catch (ClassCastException e)
						{
							getPlugin().log(Level.SEVERE, "Player '%s' mysql data is corrupt: Inventory slot number was not a number", e, stable.getOwner());
							continue;
						}
						
						ItemStack item = ItemStack.deserialize((Map<String, Object>) itemMap);
						
						if (items == null)
						{
							items = (ArrayList<ItemStack>) cacheItemList;
							items.clear();
						}
						
						// Fill in the gaps with nothing
						while (items.size() <= slot)
							items.add(null);
						
						items.set(slot, item);
					}
					
					// Create the horse
					PlayerHorse horseData = new PlayerHorse(getPlugin(), stable, name, type, maxHealth, health,jumpStrength, null, horseId);
					// Set aditional data
					horseData.setLastDeath(lastDeath);
					
					if (items != null)
					{
						horseData.setItems(items.toArray(new ItemStack[items.size()]));
						items.clear();
					}
					
					horseData.setHasChest(hasChest);
					
					// Add the horse to the stable
					stable.addHorse(horseData);
				}
				catch (SQLException e)
				{
					getPlugin().severe("Failed to load one of the player %s's Horses", e, stable.getOwner());
				}
			}
		}
		catch (SQLException e)
		{
			getPlugin().severe("Failed to load the player %s's Horses", e, stable.getOwner());
		}
	}

	@Override
	protected void saveStable(Stable stable)
	{
		// If the stable has not been inserted into the database and it has no horses we do nothing
		if (stable.getHorseCount() == 0 && stable.getId() == -1)
			return;
		
		// Try connect to the database
		if (!connect())
			return;
		
		
		// Create the statement
		Statement stmt = null;
		try
		{
			stmt = conn.createStatement();
		}
		catch (SQLException e)
		{
			getPlugin().severe("Failed to save the player %s's stable data", e, stable.getOwner());
			return;
		}
		
		// Check if we need to insert the stable into the database
		if (stable.getId() == -1)
		{
			// NOTE: We must have at least one horse due to the check at the top
			try
			{
				String query = String.format("INSERT INTO `Stables` (`user`, `lastactive`) VALUES ('%s', '%s')", stable.getOwner(), stable.getLastActiveHorse() != null ? stable.getLastActiveHorse().getName() : "");
				stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
				
				// Save the stables ID
				ResultSet result = stmt.getGeneratedKeys();
				while (result.next())
				{
					stable.setId(result.getInt(1));
				}
			}
			catch (SQLException e)
			{
				getPlugin().severe("Failed to insert the player %s's stable into the Database", stable.getOwner());
			}
		}
		// If the player has any horses we update the data
		else if (stable.getHorseCount() > 0)
		{
			try
			{
				String query = String.format("UPDATE `Stables` SET `lastactive`='%s' WHERE `id`='%d'", stable.getLastActiveHorse() != null ? stable.getLastActiveHorse().getName() : "", stable.getId());
				stmt.executeUpdate(query);
			}
			catch (SQLException e)
			{
				getPlugin().severe("Failed to update the player %s's stable in the database", e, stable.getOwner());
			}
		}
		// Else we delete the stable
		else
		{
			try
			{
				String query = String.format("DELETE FROM `Stables` WHERE `id`='%d'", stable.getId());
				stmt.executeUpdate(query);
			}
			catch (SQLException e)
			{
				getPlugin().severe("Failed to delete the player %s's stable in the database", e, stable.getOwner());
			}
		}
		
	}

	@Override
	public void saveHorse(PlayerHorse horse)
	{
		if (!connect())
			return;
		
		// Create the statement
		Statement stmt;
		try
		{
			stmt = conn.createStatement();
		}
		catch (SQLException e)
		{
			getPlugin().severe("Failed to save the player %s's horse data to the database", e, horse.getStable().getOwner());
			return;
		}
		
		// Check if the players stable has been saved first
		if (horse.getStable().getId() == -1)
		{
			saveStable(horse.getStable());
		}
		
		// Fetch data we need to save the horses state
		String name = COLOUR_CHAR_REPLACE.matcher(horse.getDisplayName()).replaceAll("&");
		HorseType type = horse.getType();
		long lastDeath = horse.getLastDeath();
		double maxhealth = horse.getMaxHealth();
		double health = horse.getHealth();
		double jumpstrength = horse.getJumpStrength();
		boolean chested = horse.hasChest();
		String inventoryString = getInventoryString(horse);
		
		// Check if we need to insert or update the horse's data
		if (horse.getId() == -1)
		{
			try
			{
				// Insert the horses data into the database
				String query = String.format("INSERT INTO `Horses` (`stableid`, `stablegroup`, `name`, `type`, `lastdeath`, `maxhealth`, `health`, `jumpstrength`, `chested`, `inventory`) VALUES ('%d', '%s', '%s', '%s', '%d', '%f', '%f', '%f', '%d', '%s')",
						horse.getStable().getId(), horse.getStable().getGroup(), name, type.toString(), lastDeath, maxhealth, health, jumpstrength, chested ? 1 : 0, inventoryString);
				
				stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
				
				// Set the horses ID
				ResultSet result = stmt.getGeneratedKeys();
				while (result.next())
				{
					horse.setId(result.getInt(1));
				}
			}
			catch (SQLException e)
			{
				getPlugin().severe("Failed to insert the player %s's horse '%s' into the database", e, horse.getStable().getOwner(), horse.getName());
			}
		}
		else
		{
			try
			{
				// Update existing values
				String query = String.format("UPDATE `Horses` SET `name`='%s', `lastdeath`='%d', `maxhealth`='%f', `health`='%f', `jumpstrength`='%f', `chested`='%d', `inventory`='%s' WHERE `id`='%d'",
						name, lastDeath, maxhealth, health, jumpstrength, chested ? 1 : 0, inventoryString, horse.getId());
				
				stmt.executeUpdate(query);
			}
			catch (SQLException e)
			{
				getPlugin().severe("Failed to update the player %s's horse '%s' in the database", e, horse.getStable().getOwner(), horse.getName());
			}
		}
	}

	@Override
	public boolean deleteHorse(PlayerHorse horse)
	{
		// If the horse has not been added to the database yet we return
		if (horse.getId() == -1)
			return true;
		
		if (!connect())
			return false;
		
		try
		{
			Statement stmt = conn.createStatement();
			
			String query = String.format("DELETE FROM `Horses` WHERE `id`='%d'", horse.getId());
			stmt.executeUpdate(query);
			return true;
		}
		catch (SQLException e)
		{
			getPlugin().severe("Failed to delete the player %s's horse '%s' from the database", horse.getStable().getOwner(), horse.getName());
			return false;
		}
	}
	
	/**
	 * Serialises the item stacks. Saves them into a yaml configuration then dumps to a string
	 * @param horse The horse we want an inventory string for
	 * @return
	 */
	private String getInventoryString(PlayerHorse horse)
	{
		@SuppressWarnings("unchecked")
		ArrayList<Map<String, Object>> itemList = (ArrayList<Map<String, Object>>) cacheItemList;
		itemList.clear();
		
		ItemStack[] items = horse.getItems();
		for (int i = 0; i < items.length; ++i)
		{
			if (items[i] == null)
				continue;
			
			Map<String, Object> itemMap = items[i].serialize();
			itemMap.put("slot", i);
			itemList.add(itemMap);
		}
		
		YamlConfiguration itemCfg = cacheCfg;
		
		itemCfg.set("i", itemList);
		String inventoryString = itemCfg.saveToString();
		itemCfg.set("i", null);
		
		return inventoryString;
	}
	
	private class MysqlSettings extends AbstractConfig
	{
		public final String host;
		public final String database;
		public final String user;
		public final String password;
		
		protected MysqlSettings(Horses plugin)
		{
			super(plugin, null, null, "mysql");
			
			this.loadConfiguration();
			
			this.addResourseToHeader("header_mysql.txt");
			
			host = getAndSet("Host", "localhost", String.class);
			database = getAndSet("Database", "default", String.class);
			
			user = getAndSet("User", "root", String.class);
			password = getAndSet("Password", "", String.class);
			
			this.saveConfiguration();
		}
		
	}
}
