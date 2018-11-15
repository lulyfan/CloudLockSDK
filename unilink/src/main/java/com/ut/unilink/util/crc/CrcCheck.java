package com.ut.unilink.util.crc;


public class CrcCheck extends CrcCheckBase {
	CRCValue crcValue = null;
	int BitCount = 0;
	long Polynominal = 0;
	boolean ShiftRight = false;
	long Initial = 0;
	long FinalMask = 0x0;
	int start = 4;

	public static boolean check(byte[] data, int crcCode) {

		CrcCheck check = new CrcCheck(16, 0x1021, false, 0, 0x00, 0);
		return crcCode == check.CountCheckAllCode(data);
	}

	public static short getCheckCode(byte[] data) {
		CrcCheck check = new CrcCheck(16, 0x1021, false, 0, 0x00, 0);
		return (short) check.CountCheckAllCode(data);
	}

	public CrcCheck() {
	}

	public CrcCheck(int bitCount, long polynominal, boolean shiftRight, long initial, long finalMask) {
		BitCount = bitCount;
		Polynominal = polynominal;
		ShiftRight = shiftRight;
		Initial = initial;
		FinalMask = finalMask;
		crcValue = new CRCValue(BitCount, Polynominal, ShiftRight, Initial, FinalMask);
	}

	public CrcCheck(int bitCount, long polynominal, boolean shiftRight, long initial, int finalMask, long startCheck) {
		BitCount = bitCount;
		Polynominal = polynominal;
		ShiftRight = shiftRight;
		Initial = initial;
		FinalMask = finalMask;
		start = (int) startCheck;
		crcValue = new CRCValue(BitCount, Polynominal, ShiftRight, Initial, FinalMask);
	}

	public @Override
    boolean GetCheckCode(byte[] checkData) {
		int len = checkData.length;
		CheckData re = CountCheckCode(checkData);
		checkData[len - 1] = re.low;
		checkData[len - 2] = re.high;
		return true;
	}

	public @Override
    boolean Check(byte[] checkData) {
		CheckData re = CountCheckCode(checkData);
        return (re.low == checkData[checkData.length - 1] && re.high == checkData[checkData.length - 2]);
    }

	// protected CheckData CountCheckCode(byte[] checkData, byte high, byte low)
	// {
	protected CheckData CountCheckCode(byte[] checkData) {
		// 锟斤拷取锟斤拷校锟斤拷锟斤拷锟斤拷锟�
		byte[] dd = new byte[checkData.length - (start + 2)];
		System.arraycopy(checkData, start, dd, 0, checkData.length - (start + 2));
		// 锟斤拷锟斤拷校锟斤拷锟斤拷
		long ret32bit = crcValue.GetCrc(dd, dd.length);
		// 锟斤拷取CRC锟竭★拷锟斤拷位校锟斤拷锟斤拷
		CheckData re = new CheckData();
		re.low = (byte) (0xFF & (byte) ret32bit);
		re.high = (byte) (0xFF & (byte) (ret32bit >> 8));
		return re;
	}

	public long CountCheckAllCode(byte[] checkData) {
		// 锟斤拷锟斤拷校锟斤拷锟斤拷
		return crcValue.GetCrc32(checkData, start, checkData.length - start);
	}

	class CheckData {
		public byte low = -1;
		public byte high = -1;

		public CheckData() {
		}

		public CheckData(byte nlow, byte nhigh) {
			low = nlow;
			high = nhigh;
		}
	}
}