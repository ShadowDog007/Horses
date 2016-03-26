package com.forgenz.horses.util;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.bukkit.entity.Horse;

import com.forgenz.horses.Horses;

public class HorseSpeedUtil
{
	private static int version = 1;

	private static boolean init = false;
	private static boolean spam = true;
	private static Object speedAttribute;
	private static Method getHandle;
	private static Method getAttributeInstance;
	
	private static Method setValue;
	private static Method getValue;
	
	public static double getHorseSpeed(Horse horse)
	{
		setup(horse);
		try
		{
			return (Double) getValue.invoke(getAttributeInstance(horse));
		}
		catch (Exception e)
		{
			if (spam)
				Horses.getInstance().severe("Error getting horse speed. This version of minecraft may not be fully supported: {init="+init+"}", e);
			spam = false;
			return 0.225;
		}
	}
	
	public static void setHorseSpeed(Horse horse, double speed)
	{
		setup(horse);
		try
		{
			setValue.invoke(getAttributeInstance(horse), speed);
		}
		catch (Exception e)
		{
			if (spam)
				Horses.getInstance().severe("Error setting horse speed. This version of minecraft may not be fully supported: {init="+init+"}", e);
			spam = false;
		}
	}
	
	private static Object getAttributeInstance(Horse horse)
	{
		try
		{
			return getAttributeInstance.invoke(getHandle.invoke(horse), speedAttribute);
		}
		catch (Throwable e)
		{
			if (spam)
				Horses.getInstance().severe("Error getting/setting horse speed. This version of minecraft may not be fully supported: {init="+init+"}", e);
			spam = false;
			return null;
		}
	}
	
	private static void setup(Horse horse)
	{
		if (init)
			return;
		
		try
		{
			String horseClassName = horse.getClass().getName();
			String mcVersion = Pattern.compile("\\.(v.+)\\.").matcher(horseClassName).group(1);
			String mcPackage = "net.minecraft.server." + mcVersion;

			Class<?> genAttributes = null;
			try
			{
				genAttributes = Class.forName(mcPackage+".GenericAttributes");
			}
			catch (Throwable e)
			{
				if (++version > 5)
					return;
					
				setup(horse);
				return;
			}
			
			speedAttribute = genAttributes.getField("MOVEMENT_SPEED").get(null);
			
			getHandle = ClassUtil.getMethod(horse.getClass(), "getHandle");
			
			Object mcHorse = getHandle.invoke(horse); 
			Class<?> iattribute = Class.forName(mcPackage+".IAttribute");
			getAttributeInstance = ClassUtil.getMethod(mcHorse.getClass(), "getAttributeInstance", iattribute);
			
			Object attributeRanged = getAttributeInstance.invoke(mcHorse, speedAttribute);
			
			setValue = ClassUtil.getMethod(attributeRanged.getClass(), "setValue", double.class);
			getValue = ClassUtil.getMethod(attributeRanged.getClass(), "getValue");

			init = true;
		}
		catch (Throwable e)
		{
			Horses.getInstance().severe("Error when setting up HorseSpeedUtil. This version of minecraft may not be fully supported: {init="+init+"}", e);
		}
	}
	
	private HorseSpeedUtil() {}
}
