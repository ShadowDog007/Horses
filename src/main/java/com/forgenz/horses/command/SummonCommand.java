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

package com.forgenz.horses.command;

import static com.forgenz.horses.Messages.*;

import java.util.HashMap;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.forgecore.v1_0.command.ForgeCommandArgument;
import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import com.forgenz.horses.config.HorsesConfig;

public class SummonCommand extends ForgeCommand
{
	private final HashMap<String, BukkitRunnable> summonTasks = new HashMap<String, BukkitRunnable>();
	private final Location cacheLoc = new Location(null, 0.0, 0.0, 0.0);
	
	public SummonCommand(ForgePlugin plugin)
	{
		super(plugin);
		
		registerAlias("summon", true);
		registerPermission("horses.command.summon");
		
		registerArgument(new ForgeCommandArgument("^[a-z0-9_]{0,16}$", Pattern.CASE_INSENSITIVE, false, Misc_Command_Error_InvalidName.toString()));
		
		setAllowOp(true);
		setAllowConsole(false);
		setArgumentString(String.format("<%1$s%2$s>", Misc_Words_Horse, Misc_Words_Name));
		setDescription(Command_Summon_Description.toString());
	}

	@Override
	protected void onCommand(CommandSender sender, ForgeArgs args)
	{
		final Player player = (Player) sender;
		
		final String playerName = player.getName();
		
		if (summonTasks.containsKey(playerName))
		{
			Command_Summon_Error_AlreadySummoning.sendMessage(player);
			return;
		}
		
		Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player);
		
		final PlayerHorse horse = stable.findHorse(args.getArg(0), false);
		
		if (horse == null)
		{
			Misc_Command_Error_NoHorseNamed.sendMessage(player, args.getArg(0));
			return;
		}
		
		HorsesConfig cfg = getPlugin().getHorsesConfig();
		
		// Check if the horse is on a death cooldown
		long timeDiff = System.currentTimeMillis() - horse.getLastDeath();
		if (cfg.deathCooldown > timeDiff)
		{
			Command_Summon_Error_OnDeathCooldown.sendMessage(player, horse.getDisplayName(), (cfg.deathCooldown - timeDiff) / 1000);
			return;
		}
		
		// Check if the player is in the correct region to use this command
		if (cfg.worldGuardCfg != null && !cfg.worldGuardCfg.allowCommand(cfg.worldGuardCfg.commandSummonAllowedRegions, player.getLocation(cacheLoc)))
		{
			Command_Summon_Error_WorldGuard_CantUseSummonHere.sendMessage(player);
			return;
		}
		
		int tickDelay = getPlugin().getHorsesConfig().summonTickDelay;
		if (tickDelay <= 0)
		{
			horse.spawnHorse(player);
			Command_Summon_Success_SummonedHorse.sendMessage(player, horse.getDisplayName());
		}
		else
		{
			BukkitRunnable task = new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if (player.isValid())
					{
						horse.spawnHorse(player);
						Command_Summon_Success_SummonedHorse.sendMessage(player, horse.getDisplayName());
					}

					summonTasks.remove(playerName);
				}
			};

			task.runTaskLater(getPlugin(), tickDelay);
			summonTasks.put(playerName, task);
			Command_Summon_Success_SummoningHorse.sendMessage(player, horse.getDisplayName(), tickDelay / 20);
		}
	}

	@Override
	public Horses getPlugin()
	{
		return (Horses) super.getPlugin();
	}
}
