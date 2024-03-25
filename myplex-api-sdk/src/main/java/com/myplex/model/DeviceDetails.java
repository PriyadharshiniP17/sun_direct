package com.myplex.model;

public class DeviceDetails {

	private static String devOs;
	private static String devOsVersion;
	private static String devMake;
	private static String devModel;
	private static String devResolution;
	private static String devSerialNumber;
	private static String deviceId;
	private static String devOperatorName;
	private static String devMccMnc;
	private static String devSimSerialNo;
	private static String devImsiNo;
	private static int    devSimState;
	private static String devClientKey;
	private static String devClientDevId;
	private static String devClientKeyExp;

	public void setDeviceOs(String aOs)
	{
		devOs=aOs;
	}

	public String getDeviceOs()
	{
		if(devOs == null){
			devOs = new String();
		}
		return devOs;
	}

	public void setDeviceOsVer(String aOsVersion)
	{
		devOsVersion=aOsVersion;
	}

	public String getDeviceOsVer()
	{
		if(devOsVersion == null){
			devOsVersion = new String();
		}
		return devOsVersion;
	}

	public void setDeviceMake(String aMake)
	{
		devMake=aMake;
	}

	public String getDeviceMake()
	{
		if(devMake == null){
			devMake = new String();
		}
		return devMake;
	}

	public void setDeviceModel(String aModel)
	{
		devModel=aModel;
	}

	public String getDeviceModel()
	{
		if(devModel == null){
			devModel = new String();
		}
		return devModel;
	}

	public void setDeviceRes(String aRes)
	{
		devResolution=aRes;
	}

	public String getDeviceRes()
	{
		if(devResolution == null){
			devResolution = new String();
		}
		return devResolution;
	}

	public void setDeviceSNo(String aSerialNo)
	{
		devSerialNumber=aSerialNo;
	}

	public String getDeviceSNo()
	{
		if(devSerialNumber == null){
			devSerialNumber = new String();
		}
		return devSerialNumber;
	}

	public void setDeviceId(String aDeviceId)
	{
		deviceId=aDeviceId;
	}

	public String getDeviceId()
	{
		if(deviceId == null){
			deviceId = new String();
		}
		return deviceId;
	}

	public void setOperatorName(String networkOperatorName) {
		devOperatorName=networkOperatorName;
	}
	public String getOperatorName() {
		if(devOperatorName == null){
			devOperatorName = new String();
		}
		return devOperatorName;
	}

	public void setMccMnc(String simOperator) {
		devMccMnc=simOperator;
	}

	public String getMccMnc() {
		if(devMccMnc == null){
			devMccMnc = new String();
		}
		return devMccMnc;
	}

	public void setSimSNo(String simSerialNumber) {
		devSimSerialNo=simSerialNumber;
	}

	public String getSimSNo() {
		if(devSimSerialNo == null){
			devSimSerialNo = new String();
		}
		return devSimSerialNo;
	}

	public void setImsiNo(String subscriberId) {
		devImsiNo=subscriberId;
	}
	
	public String getImsiNo() {
		if(devImsiNo == null){
			devImsiNo = new String();
		}
		return devImsiNo;
	}
	
	public void setSimState(int aSimState) {
		devSimState=aSimState;
	}
	
	public boolean isSimReady() {
		if(devSimState==5)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public void setClientKey(String aClientKey) {
		devClientKey=aClientKey;
	}

	public String getClientKey() {
		return devClientKey;
	}

	public void setClientDeviceId(String aClientDevId) {
		devClientDevId=aClientDevId;
	}

	public String getClientDeviceId() {
		return devClientDevId;
	}
	public void setClientKeyExp(String aClientKeyExp) {
		devClientKeyExp=aClientKeyExp;
	}

	public String getClientKeyExp() {
		return devClientKeyExp;
	}
}
