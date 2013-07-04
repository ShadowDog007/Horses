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

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.forgenz.forgecore.v1_0.bukkit.ForgeListener;
import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.horses.PlayerHorse;

/**
 * Protects owned horses from being killed by players
 * Including their owners.
 */
public class DamageListener extends ForgeListener
{
	public DamageListener(ForgePlugin plugin)
	{
		super(plugin);
		
		register();
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
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
		
		// Don't let any players hurt the poor horsey
		if (event.getDamager().getType() == EntityType.PLAYER)
		{
			//Player player = (Player) event.getDamager();
			// TODO send the player a message to tell them whats happening
			
			event.setCancelled(true);
			return;
		}
		
		// Make sure the damager isn't a projectile fired by a player
		if (event.getDamager() instanceof Projectile)
		{
			Projectile p = (Projectile) event.getDamager();
			
			if (p.getShooter().getType() == EntityType.PLAYER)
			{
				// TODO send the player a message to tell them whats happening
				event.setCancelled(true);
				return;
			}
		}
		// Make sure the damager isn't a player blowing up TNT
		else if (event.getDamager().getType() == EntityType.PRIMED_TNT)
		{
			TNTPrimed tnt = (TNTPrimed) event.getDamager();
			
			if (tnt.getSource() != null && tnt.getSource().getType() == EntityType.PLAYER)
			{
				// TODO send the player a message to tell them whats happening
				event.setCancelled(true);
				return;
			}
		}
	}
}