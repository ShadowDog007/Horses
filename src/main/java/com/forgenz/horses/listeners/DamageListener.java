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

package com.forgenz.horses.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import com.forgenz.forgecore.v1_0.bukkit.ForgeListener;
import com.forgenz.horses.Horses;
import com.forgenz.horses.Messages;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.config.HorsesConfig;

/**
 * Protects owned horses from being killed by players
 * Including their owners.
 */
public class DamageListener extends ForgeListener
{
	public DamageListener(Horses plugin)
	{
		super(plugin);
		
		register();
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event)
	{
		// Ignore any entities which are not horses
		if (event.getEntityType() != EntityType.HORSE)
		{
			return;
		}

		// Fetch our lovely horse :)
		Horse horse = (Horse) event.getEntity();

		// Check if the Horse is owned
		PlayerHorse horseData = PlayerHorse.getFromEntity(horse); 

		// If the horse has player data then they are owned and managed by Horses
		if (horseData == null)
		{
			// Else let the horse die
			return;
		}
		
		// Fetch the config
		HorsesConfig cfg = getPlugin().getHorsesConfig();
		
		// Invincible!!
		if (cfg.invincibleHorses)
		{
			event.setCancelled(true);
			return;
		}
		
		if (cfg.protectFromBurning)
		{
			switch (event.getCause())
			{
				case FIRE:
				case FIRE_TICK:
					event.setCancelled(true);
					return;
				default:
			}
		}
		
		if (event.getClass() == EntityDamageByEntityEvent.class)
		{
			onEntityDamageByEntity((EntityDamageByEntityEvent) event, horse, horseData);
		}
		
		if (!event.isCancelled() && cfg.onlyHurtHorseIfOwnerCanBeHurt)
		{
			Player owner = Bukkit.getPlayerExact(horseData.getStable().getOwner());
			if (owner == null)
			{
				return;
			}
			
			EntityDamageEvent e = null;
			
			// Create a copy of the Damage event (But with 0 damage)
			if (event.getClass() == EntityDamageEvent.class)
				e = new EntityDamageEvent(owner, event.getCause(), 0.0);
			else if (event.getClass() == EntityDamageByEntityEvent.class)
				e = new EntityDamageByEntityEvent(((EntityDamageByEntityEvent) event).getDamager(), owner, event.getCause(), 0.0);
			else if (event.getClass() == EntityDamageByBlockEvent.class)
				e = new EntityDamageByBlockEvent(((EntityDamageByBlockEvent) event).getDamager(), owner, event.getCause(), 0.0);
			
			Bukkit.getPluginManager().callEvent(e);
			
			event.setCancelled(e.isCancelled());
		}
	}
	
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event, Horse horse, PlayerHorse horseData)
	{		
		// Fetch the config
		HorsesConfig cfg = getPlugin().getHorsesConfig();
		
		// Find a player which tried to hurt the horse
		Player player = getPlayerDamager(event.getDamager());
				
		// Don't let any players hurt the poor horsey
		if (player != null)
		{
			// If we are protecting horses from their owners, check if the damager is their owner
			if (cfg.protectFromOwner && horseData.getStable().getOwner().equals(player.getName()))
			{
				event.setCancelled(true);
			}
			// If this is set we don't let any players hurt the horse
			else if (cfg.protectFromPlayers)
			{
				Messages.Event_Damage_Error_CantHurtOthersHorses.sendMessage(player);
				event.setCancelled(true);
			}
		}
		else
		{
			// If set, don't let mobs hurt the horse
			if (cfg.protectFromMobs)
			{
				event.setCancelled(true);
			}
		}
	}
	
	public static Player getPlayerDamager(Entity entity)
	{
		if (entity == null)
			return null;
		
		if (entity.getType() == EntityType.PLAYER)
			return (Player) entity;
		
		if (entity.getType() == EntityType.PRIMED_TNT)
			return castPlayer(((TNTPrimed) entity).getSource());
		
		if (entity instanceof Projectile)
			return castPlayer(((Projectile) entity).getShooter());
		
		return null;
	}
	
	public static Player castPlayer(Entity entity)
	{
		if (entity == null)
			return null;
		
		if (entity.getType() == EntityType.PLAYER)
			return (Player) entity;
		
		return null;
	}
	
	@Override
	public Horses getPlugin()
	{
		return (Horses) super.getPlugin();
	}
}