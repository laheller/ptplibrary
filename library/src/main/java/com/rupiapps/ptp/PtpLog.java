package com.rupiapps.ptp;

public class PtpLog
{
	public static void debug(Object msg) 
	{
		if(!BuildConfig.DEBUG)
			return;
		try
		{
			android.util.Log.d("PTPLib", msg.toString());
		}
		catch(Error e)
		{
			System.out.println(msg);
		}
	}
	
	public static void error(Object msg) 
	{
		if(!BuildConfig.DEBUG)
			return;
		try
		{
			android.util.Log.e("PTPLib", msg.toString());
		}
		catch(Error e)
		{
			System.err.println(msg);
		}
	}
}
