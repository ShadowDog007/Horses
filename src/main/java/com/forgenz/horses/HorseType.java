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

import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;


public enum HorseType
{
	// Plain horses
	White(Color.WHITE),
	Creamy(Color.CREAMY),
	Chestnut(Color.CHESTNUT),
	Brown(Color.BROWN),
	Black(Color.BLACK),
	Gray(Color.GRAY),
	DarkBrown(Color.DARK_BROWN),
	
	// White Socks/Blaze
	BlazeWhite(Color.WHITE, Style.WHITE),
	BlazeCreamy(Color.CREAMY, Style.WHITE),
	BlazeChestnut(Color.CHESTNUT, Style.WHITE),
	BlazeBrown(Color.BROWN, Style.WHITE),
	BlazeBlack(Color.BLACK, Style.WHITE),
	BlazeGray(Color.GRAY, Style.WHITE),
	BlazeDarkBrown(Color.DARK_BROWN, Style.WHITE),
	
	// White field ?
	PaintWhite(Color.WHITE, Style.WHITEFIELD),
	PaintCREAMY(Color.CREAMY, Style.WHITEFIELD),
	PaintChestnut(Color.CHESTNUT, Style.WHITEFIELD),
	PaintBrown(Color.BROWN, Style.WHITEFIELD),
	PaintBlack(Color.BLACK, Style.WHITEFIELD),
	PaintGray(Color.GRAY, Style.WHITEFIELD),
	PaintDarkBrown(Color.DARK_BROWN, Style.WHITEFIELD),
	
	// White dots
	LeopardWhite(Color.WHITE, Style.WHITE_DOTS),
	LeopardCreamy(Color.CREAMY, Style.WHITE_DOTS),
	LeopardChestnut(Color.CHESTNUT, Style.WHITE_DOTS),
	LeopardBrown(Color.BROWN, Style.WHITE_DOTS),
	LeopardBlack(Color.BLACK, Style.WHITE_DOTS),
	LeopardGray(Color.GRAY, Style.WHITE_DOTS),
	LeopardDarkBrown(Color.DARK_BROWN, Style.WHITE_DOTS),
	
	// Black dots
	SootyWhite(Color.WHITE, Style.BLACK_DOTS),
	SootyCreamy(Color.CREAMY, Style.BLACK_DOTS),
	SootyChestnut(Color.CHESTNUT, Style.BLACK_DOTS),
	SootyBrown(Color.BROWN, Style.BLACK_DOTS),
	SootyBlack(Color.BLACK, Style.BLACK_DOTS),
	SootyGray(Color.GRAY, Style.BLACK_DOTS),
	SootyDarkBrown(Color.DARK_BROWN, Style.BLACK_DOTS),
	
	// Special
	Donkey(Variant.DONKEY),
	Mule(Variant.MULE),
	Undead(Variant.UNDEAD_HORSE),
	Skeleton(Variant.SKELETON_HORSE);
	
	private final String permission;
	private final Variant variant;
	private final Color colour;
	private final Style style;
	
	private HorseType(Variant variant)
	{
		this(variant, null, null);
	}
	
	private HorseType(Color colour)
	{
		this(Variant.HORSE, colour, Style.NONE);
	}
	
	private HorseType(Color colour, Style style)
	{
		this(Variant.HORSE, colour, style);
	}
	
	private HorseType(Variant variant, Color colour, Style style)
	{
		this.variant = variant;
		this.colour = colour;
		this.style = style;
		
		this.permission = "horses.types." + toString().toLowerCase();
	}
	
	public Variant getVariant()
	{
		return variant;
	}
	
	public Color getColour()
	{
		return colour; 
	}
	
	public Style getStyle()
	{
		return style;
	}
	
	public String getPermission()
	{
		return permission;
	}
	
	public void setHorseType(Horse horse)
	{
		horse.setVariant(getVariant());
		
		if (getVariant() == Variant.HORSE)
		{
			horse.setColor(getColour());
			horse.setStyle(getStyle());
		}
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
		switch (horse.getVariant())
		{
			case HORSE:
				HorseType[] a = values();
				org.bukkit.entity.Horse.Color colour = horse.getColor();
				for (int i = horse.getStyle().ordinal() * Color.values().length; i < a.length; ++i)
					if (a[i].getColour() == colour)
						return a[i];
			case DONKEY:
				return Donkey;
			case MULE:
				return Mule;
			case UNDEAD_HORSE:
				return Undead;
			case SKELETON_HORSE:
				return Skeleton;
			default:	
				return null;
		}
	}
}
