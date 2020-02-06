package com.rupiapps.ptp.exceptions;

public class InitFailException extends Exception
{
	/**
	 * 
	 */
    private static final long serialVersionUID = 1L;
	
    private InitFailReason reason;
	
	public InitFailException(InitFailReason reason)
	{
		super();		
		this.reason = reason;
	}
	
	public InitFailReason getReason()
	{
		return reason;
	}

	public enum InitFailReason
	{
		NoConnection, HostNotFound, Disconnected, InvalidPacket, Timeout, NotAcknowledged, ClaimFailed, NoEndpoints;
	}
}


