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

import net.minecraft.server.v1_6_R1.EntityHorse;

import org.bukkit.craftbukkit.v1_6_R1.entity.CraftHorse;
import org.bukkit.entity.Horse;


public enum HorseType
{
	// Plain horses
	White(0),
	Creamy(1),
	Chestnut(2),
	Brown(3),
	Black(4),
	Gray(5),
	DarkBrown(6),
	
	// White Socks/Blaze
	BlazeWhite(256),
	BlazeCreamy(257),
	BlazeChestnut(258),
	BlazeBrown(259),
	BlazeBlack(260),
	BlazeGray(261),
	BlazeDarkBrown(262),
	
	// White field ?
	PaintWhite(512),
	PaintCreamy(513),
	PaintChestnut(514),
	PaintBrown(515),
	PaintBlack(516),
	PaintGray(517),
	PaintDarkBrown(518),
	
	// White dots
	LeopardWhite(768),
	LeopardCreamy(769),
	LeopardChestnut(770),
	LeopardBrown(771),
	LeopardBlack(772),
	LeopardGray(773),
	LeopardDarkBrown(774),
	
	// Black dots
	SootyWhite(1024),
	SootyCreamy(1025),
	SootyChestnut(1026),
	SootyBrown(1027),
	SootyBlack(1028),
	SootyGray(1029),
	SootyDarkBrown(1030),
	
	// Special
	Donkey(1, true),
	Mule(2, true),
	Undead(3, true),
	Skeleton(4, true);
	
	private final String permission;
	private final int type;
	private final int variant;
	
	private HorseType(int variant)
	{
		this.type = 0;
		this.variant = variant;
		
		this.permission = "horses.type." + toString().toLowerCase();
	}
	
	private HorseType(int type, boolean dummy)
	{
		this.type = type;
		this.variant = 0;
		
		this.permission = "horses.type." + toString().toLowerCase();
	}
	
	public int getType()
	{
		return type; 
	}
	
	public int getVariant()
	{
		return variant;
	}
	
	public String getPermission()
	{
		return permission;
	}
	
	public static HorseType closeValueOf(String like)
	{
		like = like.toLowerCase();
		
		for (HorseType type : values())
		{
			if (type.toString().toLowerCase().startsWith(like))
			{
				return type;
			}
		}
		return null;
	}
	
	public static HorseType exactValueOf(String typeStr)
	{		
		for (HorseType type : values())
		{
			if (type.toString().equalsIgnoreCase(typeStr))
			{
				return type;
			}
		}
		return null;
	}
	
	public static HorseType valueOf(Horse horse)
	{
		CraftHorse chorse = (CraftHorse) horse;
		EntityHorse mhorse = (EntityHorse) chorse.getHandle();
		
		switch (mhorse.bP())
		{
			case 0:
				HorseType[] a = values();
				int var = mhorse.bQ();
				for (int i = 0; i < a.length; ++i)
					if (a[i].getVariant() == var)
						return a[i];
			case 1:
				return Donkey;
			case 2:
				return Mule;
			case 3:
				return Undead;
			case 4:
				return Skeleton;
			default:	
				return null;
		}
	}
}
