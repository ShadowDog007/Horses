package com.forgenz.horses.command;

import static com.forgenz.horses.Messages.Command_Delete_Description;
import static com.forgenz.horses.Messages.Command_Delete_Success_DeletedHorse;
import static com.forgenz.horses.Messages.Misc_Command_Error_InvalidName;
import static com.forgenz.horses.Messages.Misc_Command_Error_NoHorseNamed;
import static com.forgenz.horses.Messages.Misc_Words_Horse;
import static com.forgenz.horses.Messages.Misc_Words_Name;

import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.forgecore.v1_0.command.ForgeCommandArgument;
import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;

public class DeleteCommand extends ForgeCommand
{
	public DeleteCommand(Horses plugin)
	{
		super(plugin);

		registerAlias("delete", true);
		registerAlias("del", true);
		registerPermission("horses.command.delete");
		
		registerArgument(new ForgeCommandArgument(getPlugin().getHorsesConfig().forceEnglishCharacters ? "^[a-z0-9_&]+$" : "^[^ ]+$", Pattern.CASE_INSENSITIVE, false, Misc_Command_Error_InvalidName.toString()));
		
		setAllowOp(true);
		setAllowConsole(false);
		setArgumentString(String.format("<%1%s%2$s>", Misc_Words_Horse, Misc_Words_Name));
		setDescription(Command_Delete_Description.toString());
	}

	@Override
	protected void onCommand(CommandSender sender, ForgeArgs args)
	{
		Player player = (Player) sender;
		
		Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player);
		
		// Find the horse!
		PlayerHorse horse = stable.findHorse(args.getArg(0), false);
		
		// Check if the horse exists
		if (horse == null)
		{
			Misc_Command_Error_NoHorseNamed.sendMessage(player, args.getArg(0));
			return;
		}
		
		Command_Delete_Success_DeletedHorse.sendMessage(player, horse.getDisplayName());
		horse.deleteHorse();	
	}
	
	@Override
	public Horses getPlugin()
	{
		return (Horses) super.getPlugin();
	}
}
