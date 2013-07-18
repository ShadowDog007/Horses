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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import com.forgenz.forgecore.v1_0.util.BukkitConfigUtil;
import com.forgenz.horses.HorseType;
import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;

public class YamlDatabase extends HorseDatabase
{
	private static final String PLAYER_DATA_LOCATION = String.format("playerdata%s%s.yml", File.separator, "%s");
	
	public YamlDatabase(Horses plugin)
	{
		super(plugin, HorseDatabaseStorageType.YAML);
	}
	
	private File getPlayersConfigFile(String player)
	{
		return new File(getPlugin().getDataFolder(), String.format(PLAYER_DATA_LOCATION, player));
	}
	
	private YamlConfiguration getPlayerConfig(String player)
	{
		YamlConfiguration cfg = new YamlConfiguration();
		
		File file = getPlayersConfigFile(player);
		
		if (file.exists())
		{
			try
			{
				cfg.load(file);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (InvalidConfigurationException e)
			{
				e.printStackTrace();
			}
		}
		
		return cfg;
	}

	@Override
	protected Stable loadStable(String player)
	{
		Stable stable = new Stable(getPlugin(), player);
		
		loadHorses(stable);
		
		return stable;
	}

	@Override
	protected void loadHorses(Stable stable)
	{
		YamlConfiguration cfg = getPlayerConfig(stable.getOwner());
		
		ConfigurationSection sect = BukkitConfigUtil.getAndSetConfigurationSection(cfg, "Horses");
		
		for (String horse : sect.getKeys(false))
		{
			ConfigurationSection horseSect = sect.getConfigurationSection(horse);
			
			HorseType type = HorseType.exactValueOf(horseSect.getString("type", HorseType.White.toString()));
			long lastDeath = horseSect.getLong("lastdeath") * 1000;
			double maxHealth = horseSect.getDouble("maxhealth");
			double health = horseSect.getDouble("health");
			double jumpStrength = horseSect.getDouble("jumpstrength");
			boolean hasChest = type == HorseType.Mule || type == HorseType.Donkey ? horseSect.getBoolean("chest", false) : false;
			
			// Tempory Hack to fix old storage
			boolean saddle = false;
			if (horseSect.isBoolean("saddle"))
				saddle = horseSect.getBoolean("saddle", false);
			
			// Tempory hack for old storage
			Material armour = null;
			if (horseSect.isString("armour"))
				armour = Material.getMaterial(horseSect.getString("armour", "null"));
			
			ArrayList<ItemStack> items = new ArrayList<ItemStack>();
			
			for (Map<?, ?> itemMap : horseSect.getMapList("inventory"))
			{
				int slot = -1;
				
				try
				{
					slot = (Integer) itemMap.get("slot");
					
				}
				catch (NullPointerException e)
				{
					getPlugin().log(Level.SEVERE, "Player '%s' data file is corrupt: Inventory slot number was missing", e, stable.getOwner());
					continue;
				}
				catch (ClassCastException e)
				{
					getPlugin().log(Level.SEVERE, "Player '%s' data file is corrupt: Inventory slot number was not a number", e, stable.getOwner());
					continue;
				}
				
				@SuppressWarnings("unchecked")
				ItemStack item = ItemStack.deserialize((Map<String, Object>) itemMap);
				
				// Fill in the gaps with nothing
				while (items.size() <= slot)
					items.add(null);
				
				items.set(slot, item);
			}
			
			PlayerHorse horseData = new PlayerHorse(getPlugin(), stable, horse, type, maxHealth, health, jumpStrength, null);
			horseData.setLastDeath(lastDeath);
			
			horseData.setItems(items.toArray(new ItemStack[items.size()]));
			if (saddle)
				horseData.setSaddle(Material.SADDLE);
			if (armour != null)
				horseData.setArmour(armour);
			horseData.setHasChest(hasChest);
			
			
			stable.addHorse(horseData);
		}
		
		if (cfg.isString("lastactive"))
		{
			PlayerHorse horse = stable.findHorse(cfg.getString("lastactive"), true);
			stable.setLastActiveHorse(horse);
		}
	}

	@Override
	public void saveStable(Stable stable)
	{
		// Fetch the file to save data to
		File playerDataFile = getPlayersConfigFile(stable.getOwner());
		
		// Delete the players config file if the player has no horses
		if (stable.getHorseCount() == 0)
		{
			if (playerDataFile.exists())
				playerDataFile.delete();
			return;
		}
		
		YamlConfiguration cfg = new YamlConfiguration();
		
		if (stable.getLastActiveHorse() != null)
			cfg.set("lastactive", stable.getLastActiveHorse().getName());
		else
			cfg.set("lastactive", null);
		
		ConfigurationSection sect = BukkitConfigUtil.getAndSetConfigurationSection(cfg, "Horses");
		
		for (PlayerHorse horse : stable)
		{
			String colourCodedDisplayName = COLOUR_CHAR_REPLACE.matcher(horse.getDisplayName()).replaceAll("&");
			ConfigurationSection horseSect = BukkitConfigUtil.getAndSetConfigurationSection(sect, colourCodedDisplayName);
			
			horseSect.set("type", horse.getType().toString());
			horseSect.set("lastdeath", horse.getLastDeath() / 1000);
			horseSect.set("maxhealth", horse.getMaxHealth());
			horseSect.set("health", horse.getHealth());
			horseSect.set("jumpstrength", horse.getJumpStrength());
			if (horse.getType() == HorseType.Mule || horse.getType() == HorseType.Donkey)
			{
				horseSect.set("chest", horse.hasChest());
			}
			else
			{
				horseSect.set("chest", null);
			}
			
			// Remove old config nodes
			horseSect.set("saddle", null);
			horseSect.set("armour", null);
			
			// Save the inventory contents
			ArrayList<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
			
			ItemStack[] items = horse.getItems();
			for (int i = 0; i < items.length; ++i)
			{
				if (items[i] == null)
					continue;
				
				Map<String, Object> item = items[i].serialize();
				
				item.put("slot", i);
				
				itemList.add(item);
			}
			horseSect.set("inventory", itemList);
		}
		
		try
		{
			cfg.save(playerDataFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void saveHorse(PlayerHorse horse)
	{
		saveStable(horse.getStable());
	}

	@Override
	public boolean deleteHorse(PlayerHorse horse)
	{
		saveStable(horse.getStable());
		return true;
	}
}
