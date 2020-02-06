package com.rupiapps.ptp.exceptions;

public class ResponseNotOkException extends Exception
{
	/**
	 * 
	 */
    private static final long serialVersionUID = 1L;
	private short code;
	
	public ResponseNotOkException(short code)
	{
		this.code = code;		
	}
	
	public short getResponseCode()
	{
		return code;
	}
	

}
