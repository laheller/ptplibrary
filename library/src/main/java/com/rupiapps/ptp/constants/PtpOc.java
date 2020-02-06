package com.rupiapps.ptp.constants;

public class PtpOc
{
	public final static short GetDeviceInfo = (short)0x1001;
	public final static short OpenSession = (short)0x1002;
	public final static short CloseSession = (short)0x1003;
	public final static short GetStorageIDs = (short)0x1004;
	public final static short GetStorageInfo = (short)0x1005;
	public final static short GetNumObjects = (short)0x1006;
	public final static short GetObjectHandles = (short)0x1007;
	public final static short GetObjectInfo = (short)0x1008;
	public final static short GetObject = (short)0x1009;
	public final static short GetThumb = (short)0x100a;
	public final static short DeleteObject = (short)0x100B;
	public final static short GetPartialObject = (short)0x101b;
	public final static short InitiateCapture = (short)0x100e;
	
	public final static short GetDevicePropDesc = (short)0x1014;
	public final static short GetDevicePropValue = (short)0x1015;
	public final static short SetDevicePropValue = (short)0x1016;
	public final static short ResetDevicePropValue = (short)0x1017;
	public final static short TerminateOpenCapture = (short)0x1018;	
	public final static short InitiateOpenCapture = (short)0x101C;
	
	
	public final static short Canon_CheckEvent = (short)0x9013;
	public final static short Canon_SetRemoteMode = (short)0x9114;
	public final static short Canon_SetEventMode = (short)0x9115;
	public final static short Canon_GetEvent = (short)0x9116;
	public final static short Canon_RequestGetEvent = (short)0xc101;
	public final static short Canon_GetObjectInfo = (short)0x9103;
	public final static short Canon_GetObjectInfoEx = (short)0x9109;
	public final static short Canon_GetThumbEx = (short)0x910a;
	public final static short Canon_Capture = (short)0x910f;
	public final static short Canon_GetObjectInfoEx64 = (short)0x9173;
	public final static short Canon_ResetUILock = (short)0x911C;

	public final static short Canon_StartShootingMode = (short)0x9008;
	public final static short Canon_EndShootingMode = (short)0x9009;
	public final static short Canon_GetViewfinderData = (short)0x9153;
	public final static short Canon_SetDevicePropValue = (short)0x9110;
	public final static short Canon_GetDevicePropValue = (short)0x9127;
	public final static short Canon_RemoteReleaseOn = (short)0x9128;
	public final static short Canon_RemoteReleaseOff = (short)0x9129;

	public final static short Canon_DriveLens = (short)0x9155;
	public final static short Canon_DoAf = (short)0x9154;
	public final static short Canon_Zoom = (short)0x9158;
	public final static short Canon_ZoomPosition = (short)0x9159;
	public final static short Canon_AfCancel = (short) 0x9160;
	public final static short Canon_SetLiveAfFrame = (short)0x915A;
	public final static short Canon_SetRequestOLCInfoGroup = (short)0x913D;
	public final static short Canon_SetRequestRollingPitchingLevel = (short)0x913E;

	public final static short Canon_BulbStart = (short) 0x9125;
	public final static short Canon_BulbEnd = (short) 0x9126;
	public final static short Canon_GetCTGInfo = (short)0x9135;
	public final static short Canon_GetStorageIDs = (short) 0x9101;
	public final static short Canon_GetStorageInfo = (short) 0x9102;
	public final static short Canon_KeepDeviceOn = (short)0x911D;
    public final static short Canon_902F = (short)0x902f;
	public final static short Canon_PCHDDCapacity = (short)0x911A;
	public final static short Canon_GetPartialObjectInfo = (short)0x9001;
	public final static short Canon_SetRating = (short)0x9140;

	public final static short Nikon_InitiateCaptureRecInMedia = (short)0x9207;
	public final static short Nikon_AfAndCaptureInSdram = (short)0x90cb;
	public final static short Nikon_InitiateCaptureRecInSdram = (short)0x90C0;
	public final static short Nikon_SetControlMode = (short)0x90C2;
	public final static short Nikon_CheckEvent = (short)0x90C7;
	public final static short Nikon_DeviceReady = (short)0x90C8;
	public final static short Nikon_GetPreviewImg = (short)0x9200;
	public final static short Nikon_StartLiveView = (short)0x9201;
	public final static short Nikon_EndLiveVew = (short)0x9202;
	public final static short Nikon_GetLiveViewImage = (short)0x9203;
	public final static short Nikon_MfDrive = (short)0x9204;
	public final static short Nikon_AfDrive = (short)0x90C1;
	public final static short Nikon_AfDriveCancel = (short)0x9206;
	public final static short Nikon_ChangeAfArea = (short)0x9205;
	public final static short Nikon_GetLargeThumb = (short)0x90c4;
	public final static short Nikon_GetPartialObjectHiSpeed = (short)0x9400;
	public final static short Nikon_GetFileInfoInBlock = (short)0x9011;
	public final static short Nikon_StartMovieRecInCard = (short)0x920a;
	public final static short Nikon_EndMovieRec = (short)0x920b;
	public final static short Nikon_TerminateCapture = (short)0x920c;
	
}
