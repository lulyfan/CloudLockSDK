package com.ut.unilink.util.crc;


public abstract class CrcCheckBase {
	public abstract boolean Check(byte[] checkData);

	public abstract boolean GetCheckCode(byte[] checkData);
}