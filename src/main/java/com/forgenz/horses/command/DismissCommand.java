package com.forgenz.horses.command;

import static com.forgenz.horses.Messages.*;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;

public class DismissCommand extends ForgeCommand
{
	public DismissCommand(Horses plugin)
	{
		super(plugin);
		
		registerAlias("dismiss", true);
		registerPermission("horses.command.dismiss");
		
		setAllowOp(true);
		setAllowConsole(false);
		setDescription(Command_Dismiss_Description.toString());
	}

	@Override
	protected void onCommand(CommandSender sender, ForgeArgs args)
	{
		Player player = (Player) sender;
		
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
		
		// Remove the horse from the world
		stable.getActiveHorse().removeHorse();
		// Notify the player
		Command_Dismiss_Success_DismissedHorse.sendMessage(player, horse.getName());
	}
	
	@Override
	public Horses getPlugin()
	{
		return (Horses) super.getPlugin();
	}
}
