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

import org.bukkit.command.CommandSender;

import com.forgenz.forgecore.v1_0.locale.ForgeLocaleEnum;
import com.forgenz.forgecore.v1_0.locale.ForgeMessage;

public enum Messages implements ForgeLocaleEnum
{
	// Command BUY messages
	Command_Buy_Error_Type("Type &b/horses &etypes to see valid horse types"),
	Command_Buy_Error_InvalidHorseType("&cThere is no horse type like %1$s"),
	Command_Buy_Error_NoPermissionForThisType("&cYou don't have permission to buy %1$s horses"),
	Command_Buy_Error_WorldGuard_CantUseBuyHere("&cYou can't buy horses in this area"),
	Command_Buy_Error_TooManyHorses("&cYou can't have more than &b%1$d &chorses"),
	Command_Buy_Error_AlreadyHaveAHorseWithThatName("&cYou already have a horse named &b%s"),
	Command_Buy_Error_CantAffordHorse("&cYou can't afford to buy a horse of this type. You need $%.2f"),
	Command_Buy_Success_BoughtHorse("&eYou purchased your horse for $%1$.2f"),
	Command_Buy_Success_Completion("&eType &b/%1$s summon &3%2$s &eto summon your new horse"),
	Command_Buy_Description("Buys a horse of the given type"),
	
	// Command Delete messages
	Command_Delete_Success_DeletedHorse("&eDeleted &b%1$s &efrom your stable"),
	Command_Delete_Description("Deletes the given horse from your stable"),
	
	// Command Dismiss messages
	Command_Dismiss_Error_NoActiveHorses("&cYou have to have an active horse to dismiss one"),
	Command_Dismiss_Error_WorldGuard_CantUseDismissHere("&cYou can't dismiss your horse in this area"),
	Command_Dismiss_Description("Dismisses your currently active horse"),
	Command_Dismiss_Success_DismissedHorse("&eDismissed &b%1$s"),
	
	// Command Heal messages
	Command_Heal_Error_HealAmountInvalid("Must provide a whole number"),
	Command_Heal_Error_NoActiveHorses("&cMust have an active horse to heal one"),
	Command_Heal_Error_CantAffordHeal("&cYou can't afford to heal this horse by &b%1$.2f&c. &eYou need &b$%2$.2f"),
	Command_Heal_Success_HealedForCost("&eHealed &b%1$s &eby &b%2$.2f &efor &b$%3$.2f"),
	Command_Heal_Success_HealedWithoutCost("&eHealed &b%1$s &eby &b%2$.2f"),
	Command_Heal_Description("Lets you heal your currently summoned horse"),
	
	// Command List messages
	Command_List_Error_InvalidCharactersPlayerName("This player is offline or does not exist"),
	Command_List_Error_NoPermissionToListPlayersHorses("&cYou do not have permission to view other players horses"),
	Command_List_Error_CouldNotFindPlayer("&cCould not find a player named like %s"),
	Command_List_Error_NoHorses("&cYou have no horses :("),
	Command_List_Success_InitialMessage("&eYour horses:"),
	Command_List_Success_InitialMessageOtherPlayer("&e%1$s horses:"),
	Command_List_Success_HorseNamePrefix("&b"),
	Command_List_Success_HorseTypePrefix("&3"),
	Command_List_Success_HorseListSeparator("&e, "),
	Command_List_Description("Lists all the horses you/the player have in the stables"),
	
	// Command Reload messages
	Command_Reload_Error_FailedToReload("&c~Failed to reload Horses, see console for details"),
	Command_Reload_Success("&e~Reloaded successfully"),
	Command_Reload_Description("Reloads Horses Configuration"),
	
	// Command Rename Messages
	Command_Rename_Error_RequireNametag("&cYou need to be holding a nametag to rename your horse"),
	Command_Rename_Success_Renamed("&eRenamed &b%1$s &eto &b%2$s"),
	Command_Rename_Description("Lets you rename one of your horses"),
	
	// Command Summon messages
	Command_Summon_Error_NoLastActiveHorse("&cYou don't have an avaliable last active horse."),
	Command_Summon_Error_OnDeathCooldown("&b%1$s &cdied too recently, wait &b%2$d seconds"),
	Command_Summon_Error_WorldGuard_CantUseSummonHere("&cYou can't summon horses in this area"),
	Command_Summon_Error_AlreadySummoning("&cYou are already summoning a horse"),
	Command_Summon_Success_SummoningHorse("&eYou are summoning &b%1$s&e. Wait &b%2$d &eseconds"),
	Command_Summon_Success_SummonedHorse("&eSummoned &b%1$s"),
	Command_Summon_Description("Summons the given horse to your side"),
	
	// Command Type messages
	Command_Type_Error_NoHorsePerms("&cYou don't have permissions to use any horse types"),
	Command_Type_Error_NoPermForHorse("&cYou don't have permissions to use any horse types"),
	Command_Type_BeginWith("&eHorseTypes: "),
	Command_Type_HorseTypePrefix("&b"),
	Command_Type_TypeSeparator("&e, "),
	Command_Type_SingleTypeFormat("&eType: &b%1$s &eHP: &b%2$.0f &eMaxHP: &b%3$.0f &eJumpStrength: &b%4$.2f"),
	Command_Type_SingleTypeFormatEco("&eType: &b%1$s &eHP: &b%2$.0f &eMaxHP: &b%3$.0f &eJumpStrength: &b%4$.2f &eBuyCost: &b%5$.2f"),
	Command_Type_Description("Lists all the types of horses you can buy"),
	
	// EventListener messages
	Event_Interact_Error_CantInteractWithThisHorse("&cYou can not interact with &b%1$s's &chorse"),
	Event_Interact_Error_RenameWithTagMustSetAName("&cYou have to set a name on your Name Tag to rename this horse"),
	Event_Interact_Error_ClaimWithTagMustSetAName("&cYou have to set a name on your Name Tag to claim this horse"),
	Event_Interact_Error_CantRenameWithTag("&cYou are not allowed to rename your horses with name tags"),
	Event_Interact_Error_RenamingNaturalHorsesBlocked("&cYou can only rename owned horses"),
	Event_Damage_Error_CantHurtOthersHorses("&cYou can not hurt other peoples horses"),
	Event_Death_HorseDiedAndWasDeleted("&b%1$s &edied and was removed from your stable"),
	Event_MovedTooFarAway("&b%1$s &ewandered off and was dismissed"),
	
	// Misc Command messages
	Misc_Command_Error_ConfigDenyPerm("&cYou don't have permission to use the %1$s command"),
	Misc_Command_Error_HorseNameEmpty("&cYou can't have horses with only a colour code in their name"),
	Misc_Command_Error_HorseNameTooLong("&cHorses names can't be more than &b%1$d &ccharacters long"),
	Misc_Command_Error_CantUseColor("&cYou don't have permission to use coloured horse names"),
	Misc_Command_Error_CantUseFormattingCodes("&cYou don't have permission to use formatting codes in your horses name"),
	Misc_Command_Error_CantBeUsedFromConsole("&cCan only use this command as a player"),
	Misc_Command_Error_NoHorseNamed("&cYou don't have a horse named like &e%1$s"),
	Misc_Command_Error_InvalidName("You don't have a horse named this"),
	Misc_Command_Error_NameValidCharacters("Your horses name can only contain letters, numbers and underscores"),
	Misc_Command_Error_IllegalHorseNamePattern("&cThis name contains an illegal pattern try another one"),
	
	// Misc Words
	Misc_Words_Horse("Horse"),
	Misc_Words_Name("Name"),
	Misc_Words_Type("Type"),
	Misc_Words_Player("Player"),
	Misc_Words_New("New"),
	Misc_Words_Amount("Amount");
	
	private final ForgeMessage message;
	
	private Messages(String defaultMessage)
	{
		String configLoc = super.toString().replaceAll("_", ".");
		
		this.message = new ForgeMessage(configLoc, defaultMessage);
	}
	
	@Override
	public ForgeMessage getMessage()
	{
		return message;
	}
	
	public void sendMessage(CommandSender sender)
	{
		if (sender == null || message.getMessage().length() == 0)
		{
			return;
		}
		
		sender.sendMessage(message.getMessage());
	}
	
	public void sendMessage(CommandSender sender, Object ...args)
	{
		if (sender == null || message.getMessage().length() == 0)
		{
			return;
		}
		
		sender.sendMessage(String.format(message.getMessage(), args));
	}
	
	/**
	 * Returns the message belonging to this enumeration
	 */
	@Override
	public String toString()
	{
		return message.getMessage();
	}
}
