package com.rupiapps.ptp.constants;

public class PtpRc
{
	public final static short OK = (short)0x2001;
	public final static short General_Error = (short)0x2002;
	public final static short Session_Not_Open = (short)0x2003;
	public final static short Invalid_TransactionID = (short)0x2004;
	public final static short Operation_Not_Supported = (short)0x2005;
	public final static short Parameter_Not_Supported = (short)0x2006;
	public final static short Incomplete_Transfer = (short)0x2007;
	public final static short Invalid_StorageID = (short)0x2008;
	public final static short Invalid_ObjectHandle = (short)0x2009;
	public final static short InvalidDevicePropFormat = (short) 0x201B;
	public final static short InvalidDevicePropValue = (short) 0x201C;
	public final static short DevicePropNotSupported = (short) 0x200A;
	public final static short ObjectWriteProtected = (short) 0x200D;
	public final static short StoreFull = (short)0x200C;
	public final static short Access_Denied = (short)0x200F;
	public final static short NoThumbnailPresent = (short)0x2010;
	public final static short Invalid_Parameter = (short)0x201d;
	public final static short SessionAlreadyOpened = (short)0x201E;
	public final static short Transaction_Canceled = (short)0x201f;
	public final static short StoreNotAvailable = (short)0x2013;
	public final static short Busy = (short)0x2019;
	public final static short Canon_Not_Ready = (short)0xA102;

	public final static short Canon_MemoryStatusNotReady =(short)0xA106;



	public final static short Nikon_HardwareError	= (short)0xA001;
	public final static short Nikon_OutOfFocus	= (short)0xA002;
	public final static short Nikon_ChangeCameraModeFailed	= (short)0xA003;
	public final static short Nikon_InvalidStatus = (short)0xA004;
	public final static short Nikon_SetPropertyNotSupported	= (short)0xA005;
	public final static short Nikon_WbResetError = (short)0xA006;
	public final static short Nikon_DustReferenceError = (short)0xA007;
	public final static short Nikon_ShutterSpeedBulb = (short)0xA008;
	public final static short Nikon_MirrorUpSequence = (short)0xA009;
	public final static short Nikon_CameraModeNotAdjustFNumber = (short)0xA00A;
	public final static short Nikon_NotLiveView = (short)0xA00B;
	public final static short Nikon_MfDriveStepEnd = (short)0xA00C;
	public final static short Nikon_MfDriveStepInsufficiency = (short)0xA00E;
	public final static short Nikon_AdvancedTransferCancel = (short)0xA022;

	public final static short ShutterSpeedTime = (short) 0xa204;
	public final static short SetPropertyNotSupported = (short) 0xA005;

	
}
