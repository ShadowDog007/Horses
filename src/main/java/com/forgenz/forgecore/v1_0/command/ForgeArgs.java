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

import java.util.Iterator;

public final class ForgeArgs implements Iterable<String>
{
	private final String command;
	private final String subCommandAlias;
	private final String[] args;
	
	public ForgeArgs(String command, String[] args)
	{
		this.command = command;
		this.subCommandAlias = args[0].trim().toLowerCase();
		this.args = new String[args.length-1];
		
		for (int i = 1; i < args.length; ++i)
		{
			this.args[i-1] = args[i].trim();
		}
	}
	
	public String getCommandUsed()
	{
		return command;
	}
	
	public String getSubCommandAlias()
	{
		return subCommandAlias;
	}
	
	public int getNumArgs()
	{
		return args.length;
	}
	
	public String getArg(int i)
	{
		return args[i];
	}

	@Override
	public Iterator<String> iterator()
	{
		return new ArgIterator();
	}
	
	public class ArgIterator implements Iterator<String>
	{
		private int index = 0;
		
		@Override
		public boolean hasNext()
		{
			return index < ForgeArgs.this.getNumArgs();
		}

		@Override
		public String next()
		{
			return ForgeArgs.this.getArg(index++);
		}

		@Override
		public void remove()
		{
		}
	}
}
