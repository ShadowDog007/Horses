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

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

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
		super(plugin);
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
			double maxHealth = horseSect.getDouble("maxhealth");
			double health = horseSect.getDouble("health");
			boolean saddle = horseSect.getBoolean("saddle", getPlugin().getHorsesConfig().startWithSaddle);
			
			Material armour = Material.getMaterial(horseSect.getString("armour", "null"));
			
			PlayerHorse horseData = new PlayerHorse(getPlugin(), stable, horse, type, maxHealth, health);
			horseData.setHasSaddle(saddle);
			horseData.setArmour(armour);
			
			stable.addHorse(horseData);
		}
	}

	@Override
	public void saveStable(Stable stable)
	{
		YamlConfiguration cfg = new YamlConfiguration();
		
		ConfigurationSection sect = BukkitConfigUtil.getAndSetConfigurationSection(cfg, "Horses");
		
		for (PlayerHorse horse : stable)
		{
			ConfigurationSection horseSect = BukkitConfigUtil.getAndSetConfigurationSection(sect, horse.getName());
			
			horseSect.set("type", horse.getType().toString());
			horseSect.set("maxhealth", horse.getMaxHealth());
			horseSect.set("health", horse.getHealth());
			horseSect.set("saddle", horse.hasSaddle());
			horseSect.set("armour", horse.hasArmour() ? horse.getArmour().toString() : "none");
		}
		
		try
		{
			cfg.save(getPlayersConfigFile(stable.getOwner()));
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
	public void deleteHorse(PlayerHorse horse)
	{
		saveStable(horse.getStable());
	}
}
