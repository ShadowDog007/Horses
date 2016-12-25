package com.forgenz.horses.util;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.AbstractHorse;

public class HorseSpeedUtil
{
	public static double getHorseSpeed(AbstractHorse horse)
	{
		AttributeInstance movementSpeed = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
		if (movementSpeed != null)
			return movementSpeed.getBaseValue();
		return 0.225;
	}
	
	public static void setHorseSpeed(AbstractHorse horse, double speed)
	{
		AttributeInstance movementSpeed = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
		if (movementSpeed != null)
			movementSpeed.setBaseValue(speed);
	}

	private HorseSpeedUtil() {}
}
