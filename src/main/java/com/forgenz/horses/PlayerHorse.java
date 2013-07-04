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
 
package com.forgenz.horses;

import java.lang.reflect.Field;

import net.minecraft.server.v1_6_R1.EntityHorse;
import net.minecraft.server.v1_6_R1.NBTTagCompound;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R1.CraftServer;
import org.bukkit.craftbukkit.v1_6_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R1.entity.CraftHorse;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.horses.util.ClassUtil;

public class PlayerHorse implements ForgeCore
{
	private static final String OWNERSHIP_METADATA_KEY = "Horses.Ownership"; 
	private static final Location cacheLoc = new Location(null, 0.0, 0.0, 0.0);
	
	private final Horses plugin;
	private final Stable stable;
	
	private Horse horse;
	
	private String name;
	private HorseType type;
	private double maxHealth;
	private double health;
	
	private boolean hasSaddle = false;
	
	public PlayerHorse(Horses plugin, Stable stable, String name, HorseType type, double maxHealth, double health)
	{
		this.plugin = plugin;
		this.stable = stable;
		
		this.name = name;
		this.type = type;
		this.maxHealth = maxHealth;
		this.health = health;
	}
	
	public Stable getStable()
	{
		return stable;
	}

	@Override
	public Horses getPlugin()
	{
		return plugin;
	}
	
	public Horse getHorse()
	{
		return horse;
	}
	
	public String getName()
	{
		return name;
	}
	
	public HorseType getType()
	{
		return type;
	}
	
	public double getMaxHealth()
	{
		if (horse != null && horse.isValid())
		{
			maxHealth = horse.getMaxHealth();
		}
		return maxHealth;
	}
	
	public double getHealth()
	{
		if (horse != null && horse.isValid())
		{
			health = horse.getHealth();
		}
		return health;
	}
	
	public double getHealEstimate(float amount)
	{
		if (getMaxHealth() < getHealth() + amount)
		{
			return maxHealth - health;
		}
		
		return amount;
	}
	
	public void setMaxHealth(double amount)
	{
		maxHealth = amount;
		
		if (horse != null)
		{
			horse.setMaxHealth(amount);
		}
	}
	
	public double addHealth(double amount)
	{
		if (getMaxHealth() < getHealth() + amount)
		{
			amount = maxHealth - health;
		}
		
		health += amount;
		
		if (horse != null && horse.isValid())
		{
			horse.setHealth(health);
		}
		
		return amount;
	}
	
	public void addMaxHealth(double amount)
	{
		maxHealth = getMaxHealth() + amount;
		health = getHealth() + amount;
		
		if (horse != null && horse.isValid())
		{
			horse.setMaxHealth(maxHealth);
			horse.setHealth(health);
		}
	}
	
	public void setHasSaddle(boolean hasSaddle)
	{
		this.hasSaddle = hasSaddle;
	}
	
	public boolean hasSaddle()
	{
		if (horse != null && horse.isValid())
		{
			EntityHorse h = (EntityHorse) ((CraftHorse) horse).getHandle();
			NBTTagCompound nbt = new NBTTagCompound();
			h.b(nbt);
			
			this.hasSaddle = nbt.hasKey("SaddleItem");
		}
		
		return hasSaddle;
	}
	
	public void removeHorse()
	{
		if (horse != null)
		{
			if (!horse.isDead())
			{
				maxHealth = (float) horse.getMaxHealth();
				health = (float) horse.getHealth();
			}
			
			horse.remove();
			
			stable.removeActiveHorse(this);
		}
		
		saveChanges();
	}
	
	public boolean spawnHorse(Player player)
	{
		if (!player.getName().equals(getStable().getOwner()))
		{
			return false;
		}
		
		if (horse != null && horse.isValid())
		{
			horse.teleport(player);
			return true;
		}
		
		// Remove the old horse
		if (getStable().getActiveHorse() != null)
		{
			getStable().getActiveHorse().removeHorse();
		}
		
		Location loc = player.getLocation(cacheLoc);
		
		// TODO Use method from CraftBukkit when they fix it
		// horse = (Horse) loc.getWorld().spawnEntity(loc, EntityType.HORSE);
		CraftWorld world = (CraftWorld) loc.getWorld();
		EntityHorse ehorse = new EntityHorse(world.getHandle());
		
		ehorse.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getPitch(), loc.getYaw());
		world.getHandle().addEntity(ehorse, SpawnReason.CUSTOM);
		
		// We have to setup the horses bukkit entity properly
		try
		{
			Field bukkitEntity = ClassUtil.getField(ehorse.getClass(), "bukkitEntity");
			bukkitEntity.setAccessible(true);
			bukkitEntity.set(ehorse, new CraftHorse((CraftServer) getPlugin().getServer(), ehorse));
		}
		catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}

		NBTTagCompound nbt = new NBTTagCompound();
		ehorse.b(nbt);
		nbt.setBoolean("Tame", true);
		nbt.setInt("Type", type.getType());
		nbt.setInt("Variant", type.getVariant());
		
		// Create the saddle
		if (hasSaddle())
		{
			NBTTagCompound saddle = new NBTTagCompound();
			saddle.setShort("id", (short) 329);
			saddle.setByte("count", (byte) 1);
			nbt.setCompound("SaddleItem", saddle);
		}
		ehorse.a(nbt);
		
		horse = (Horse) ehorse.getBukkitEntity();
		
		if (horse != null)
		{
			// Set the horses name
			horse.setCustomName(name);
			horse.setCustomNameVisible(true);
			
			// Set the horses HP
			horse.setMaxHealth(maxHealth);
			horse.setHealth(health);
			
			// Make the horse follow the player
			horse.setTarget(player);
			
			// Setup the horses metadata
			horse.setMetadata(OWNERSHIP_METADATA_KEY, new FixedMetadataValue(getPlugin(), this));
			
			getStable().setActiveHorse(this);
			
			return true;
		}
		
		return false;
	}
	
	public void saveChanges()
	{
		getPlugin().getHorseDatabase().saveHorse(this);
	}
	
	public void deleteHorse()
	{
		if (horse != null)
		{
			horse.remove();
			getStable().removeActiveHorse(this);
		}
		
		stable.deleteHorse(this);
	}
	
	public static PlayerHorse getFromEntity(Horse horse)
	{
		for (MetadataValue meta : horse.getMetadata(OWNERSHIP_METADATA_KEY))
		{
			if (meta.getOwningPlugin() == Horses.getInstance() && meta.value().getClass() == PlayerHorse.class)
			{
				return (PlayerHorse) meta.value();
			}
		}
		
		return null;
	}

	public void rename(String name)
	{
		this.name = name;
		
		if (horse != null && horse.isValid())
		{
			horse.setCustomName(name);//ChatColor.translateAlternateColorCodes('&', name));
		}
		
		saveChanges();
	}
}
