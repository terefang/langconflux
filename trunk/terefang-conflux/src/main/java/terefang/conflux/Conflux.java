/***********************************************************************************
 * 
 *	Copyright (c) 2006-2015, Alfred Reibenschuh
 *	All rights reserved.
 *
 *	Redistribution and use in source and binary forms, with or without 
 *	modification, are permitted provided that the following conditions 
 *	are met:
 *
 *	1. Redistributions of source code must retain the above copyright 
 *	notice, this list of conditions and the following disclaimer.
 *
 *	2. Redistributions in binary form must reproduce the above copyright 
 *	notice, this list of conditions and the following disclaimer in the 
 *	documentation and/or other materials provided with the distribution.
 *
 *	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 *	"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 *	LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 *	A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 *	HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 *	SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 *	TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 *	PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 *	LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 *	NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 *	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ***********************************************************************************/

package terefang.conflux;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;


public class Conflux extends Hashtable<String,Collection<Character>>
{

	Random rand = new Random(System.currentTimeMillis());
	int tableSize = 0;

	boolean restrictedMode = false;
	boolean debug = false;
	int loopBreaker = 1000;
	int fudge = 3;
	boolean allowSpecialChars = false;
	boolean allowExtendedChars = false;

	public boolean isAllowSpecialChars() 
	{
		return allowSpecialChars;
	}

	public void setAllowSpecialChars(boolean allowSpecialChars) 
	{
		this.allowSpecialChars = allowSpecialChars;
	}

	public boolean isAllowExtendedChars() 
	{
		return allowExtendedChars;
	}

	public void setAllowExtendedChars(boolean allowExtendedChars) 
	{
		this.allowExtendedChars = allowExtendedChars;
	}

	public int getFudge() 
	{
		return fudge;
	}

	public void setFudge(int fudge) 
	{
		this.fudge = fudge;
	}
	
	public int getLoopBreaker() 
	{
		return loopBreaker;
	}

	public void setLoopBreaker(int loopBreaker) 
	{
		this.loopBreaker = loopBreaker;
	}

	public boolean isRestrictedMode() 
	{
		return restrictedMode;
	}

	public void setRestrictedMode(boolean restrictedMode) 
	{
		this.restrictedMode = restrictedMode;
	}
	
	public boolean isDebug()
	{
		return debug;
	}

	public void setDebug(boolean debug) 
	{
		this.debug = debug;
	}

	public Conflux() 
	{
		super();
	}

	public Conflux(File fn) throws IOException 
	{
		this();
		load(fn);
	}

	public Conflux(File fn, int ts) throws IOException 
	{
		this();
		load(fn, ts);
	}

	public void setSeed(long seed) 
	{
		rand.setSeed(seed);
	}

	public void load(File fn) throws IOException
	{
		load(fn, 3);
	}

	public void load(File fn, int ts) throws IOException
	{
		FileReader fin = new FileReader(fn);
		StreamTokenizer st = new StreamTokenizer(fin);
		load(st, ts);
		fin.close();
	}
	
	public void load(StreamTokenizer st) throws IOException
	{
		load(st, 3);
	}
	
	public void load(StreamTokenizer st, int ts) throws IOException
	{
		tableSize = (ts>tableSize) ? ts : tableSize;

		int tokenType;
		
		st.resetSyntax();
		st.whitespaceChars(0, 0x40);
		st.slashSlashComments(true);
		st.slashStarComments(true);
		st.commentChar('#');
		st.commentChar(';');
		st.lowerCaseMode(true);
		st.wordChars('A', 'Z');
		st.wordChars('a', 'z');
		
		if(allowSpecialChars)
		{
			st.wordChars('\'', '\'');
			st.wordChars('`', '`');
			st.wordChars('-', '-');
		}
		
		if(allowExtendedChars)
		{
			st.wordChars(0x7e, 0xffff);
		}
		
		while((tokenType = st.nextToken()) !=  StreamTokenizer.TT_EOF)
		{
			if(tokenType == StreamTokenizer.TT_WORD)
			{
				if(debug) System.out.println("S = "+st.sval);
				String buf = st.sval;
				for(int j=1; j<=ts && j<buf.length(); j++)
				{
					for(int i=j; i<buf.length(); i++)
					{
						String key = buf.substring(i-j, i);
						char v = buf.charAt(i);
						this.putTable(key, v);
					}
				}
				for(int i= ((ts>=buf.length()) ? buf.length() : ts); i>0; i--)
				{
					String key = buf.substring(buf.length()-i, buf.length());
					this.putTable(key, ' ');
				}
				this.putTable(" ", buf.charAt(0));
			}
		}
		
		//for(java.util.Map.Entry<String, List<Character>> entry : this.entrySet())
		//{
		//	Collections.sort(entry.getValue(), new Comparator<Character>() { public int compare(Character c1, Character c2){ return c1.compareTo(c2); } });
		//}
	}

	public void putTable(String key, char v) 
	{
		if(!this.containsKey(key))
		{
			if(this.isRestrictedMode())
			{
				this.put(key, new TreeSet(new Comparator<Character>() { public int compare(Character c1, Character c2){ return c1.compareTo(c2); } }));
			}
			else
			{
				this.put(key, new Vector());
			}
		}
		this.get(key).add(v);
	}

	public Collection<Character> getTable(String key) 
	{
		String lookup = key;
		
		if(lookup.length()>tableSize)
		{
			lookup = lookup.substring(lookup.length()-tableSize);
		}
		
		while(!this.containsKey(lookup) && lookup.length()>0)
		{
			lookup = lookup.substring(1);
		}

		if(!this.containsKey(lookup))
		{
			return null;
		}
		else
		{
			return this.get(lookup);
		}
	}
	
	public char getChar(String key)
	{
		Collection<Character> v = getTable(key);
		if(v==null)
		{
			//return ' ';
			return (char)('a'+rand.nextInt(26));
		}
		else
		{
			int i = rand.nextInt(v.size());
			return (Character) v.toArray()[i];
		}
	}

	public char getStartChar()
	{
		return getChar(" ");
	}

	public Collection<String> generateWords(Collection<String> w, int size, int num)
	throws Exception
	{
		StringBuilder word = new StringBuilder();
		word.append(this.getStartChar());
		int itr = 0;
		while(w.size() < num)
		{
			if(itr>loopBreaker*num*size)
			{
				throw new RuntimeException("loop detected");
			}
			
			char c = this.getChar(word.toString());
			
			// break loop if size gets too big
			if(word.length()>(size+fudge))
			{
				c=' ';
			}

			if(c==' ')
			{
				if(word.length()>=size && !w.contains(word.toString()))
				{
					w.add(word.toString());
					itr=0;
					word.setLength(0);
				}
				word.append(this.getStartChar());
			}
			else
			if(c=='\'' || c=='-' || c=='`')
			{
				if(word.length()>=size && !w.contains(word.toString()))
				{
					w.add(word.toString());
					itr=0;
					word.setLength(0);
					word.append(this.getStartChar());
				}
				else
				{
					word.append(c);
				}
			}
			else
			{
				word.append(c);
			}
			
			itr++;
						
			if(debug) System.out.println(word.toString()+"\t"+w);
		}
		return w;
	}

	public void printTable()
	{
		Vector<String> k = new Vector(this.keySet());
		Collections.sort(k, new Comparator<String>() { public int compare(String s1, String s2){ return s1.compareTo(s2); } });
		
		for(String key : k)
		{
			System.out.println("\""+key+"\" = "+this.get(key));
		}
	}

	public static void main(String [] args) throws IOException
	{
		Conflux cf = new Conflux();
		cf.setRestrictedMode(true);
		cf.setSeed(1);
		//cf.setDebug(true);
		cf.setLoopBreaker(1000);
		cf.setFudge(0);
		cf.setAllowExtendedChars(true);
		cf.setAllowSpecialChars(true);
		cf.load(new File("./src/test/resources/test.txt"), 2);
		
		Vector<String> w = new Vector();
		for(int i=5; i<9; i++)
		{
			try 
			{
				cf.generateWords(w, i, w.size()+10);
			} 
			catch (Exception xe) 
			{
				System.err.println("I="+i+" W="+w.size()+": "+xe);
			}
		}
		Collections.sort(w, new Comparator<String>() 
		{ 
			public int compare(String s1, String s2)
			{
				return s1.compareTo(s2);
				//return Integer.compare(s1.length(), s2.length())==0 ? s1.compareTo(s2) : Integer.compare(s1.length(), s2.length()); 
			} 
		});
		
		for(int i=0; i<w.size(); i++)
		{
			System.out.println(w.elementAt(i));
		}
		cf.printTable();
	}

}