package com.forgenz.horses.command;

import static com.forgenz.horses.Messages.*;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import com.forgenz.horses.config.HorsesConfig;
import com.forgenz.horses.config.HorsesPermissionConfig;

public class DismissCommand extends ForgeCommand
{
	private final Location cacheLoc = new Location(null, 0.0, 0.0, 0.0);
	
	public DismissCommand(Horses plugin)
	{
		super(plugin);
		
		registerAlias("dismiss", true);
		registerAlias("d", false);
		registerPermission("horses.command.dismiss");
		
		setAllowOp(true);
		setAllowConsole(false);
		setDescription(Command_Dismiss_Description.toString());
	}

	@Override
	protected void onCommand(CommandSender sender, ForgeArgs args)
	{
		Player player = (Player) sender;
		
		HorsesConfig cfg = getPlugin().getHorsesConfig();
		HorsesPermissionConfig pcfg = cfg.getPermConfig(player);
		
		if (!pcfg.allowDismissCommand)
		{
			Misc_Command_Error_ConfigDenyPerm.sendMessage(sender, getMainCommand());
			return;
		}
		
		// Fetch the players stable
		Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player);
		
		// Fetch the players active horse
		PlayerHorse horse = stable.getActiveHorse();
		
		// Check if they have an active horse
		if (horse == null)
		{
			Command_Dismiss_Error_NoActiveHorses.sendMessage(player);
			return;
		}
		
		// Check if the player is in the correct region to use this command
		if (cfg.worldGuardCfg != null && !cfg.worldGuardCfg.allowCommand(cfg.worldGuardCfg.commandDismissAllowedRegions, player.getLocation(cacheLoc)))
		{
			Command_Dismiss_Error_WorldGuard_CantUseDismissHere.sendMessage(player);
			return;
		}
		
		// Remove the horse from the world
		stable.getActiveHorse().removeHorse();
		// Notify the player
		Command_Dismiss_Success_DismissedHorse.sendMessage(player, horse.getDisplayName());
	}
	
	@Override
	public Horses getPlugin()
	{
		return (Horses) super.getPlugin();
	}
}
