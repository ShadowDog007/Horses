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

package com.forgenz.forgecore.v1_0.command;

import org.bukkit.ChatColor;

public final class ForgeArgumentError
{
	public enum ErrorType
	{
		GOOD(""),
		
		TOO_FEW_ARGS(String.format("%sToo few arguments", ChatColor.AQUA)),
		
		TOO_MANY_ARGS(String.format("%sToo many arguments", ChatColor.AQUA)),
		
		INVALID_ARG(null);
		
		private final ForgeArgumentError error;
		
		private ErrorType(String error)
		{
			if (error != null)
			{
				this.error = new ForgeArgumentError(error, this);
			}
			else
			{
				this.error = null;
			}
		}
		
		private ForgeArgumentError getError()
		{
			return error;
		}
	}

	private final String errorMessage;
	private final ErrorType type;
	
	private ForgeArgumentError(String errorMessage, ErrorType type)
	{
		this.errorMessage = errorMessage;
		this.type = type;
	}
	
	public String getMessage()
	{
		return errorMessage;
	}
	
	public ErrorType getType()
	{
		return type;
	}
	
	/**
	 * Sets up error data
	 * @param type The type of error
	 * @param arg The argument involved (Only valid for 'INVALID_ARG')
	 * @return The error
	 */
	public static ForgeArgumentError buildError(ErrorType type, ForgeCommandArgument arg)
	{
		switch (type)
		{
			case GOOD:
			case TOO_FEW_ARGS:
			case TOO_MANY_ARGS:
				return type.getError();
			case INVALID_ARG:
			default:
				return new ForgeArgumentError(arg.getError(), ErrorType.INVALID_ARG);	
		}
	}
}
