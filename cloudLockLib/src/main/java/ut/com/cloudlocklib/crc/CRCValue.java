package ut.com.cloudlocklib.crc;


public class CRCValue {
	int MaxByteValues = 256;
	int BitSperByte = 8;
	public long[] bits = { 0x00000001, 0x00000002, 0x00000004, 0x00000008, 0x00000010, 0x00000020, 0x00000040,
			0x00000080, 0x00000100, 0x00000200, 0x00000400, 0x00000800, 0x00001000, 0x00002000, 0x00004000, 0x00008000,
			0x00010000, 0x00020000, 0x00040000, 0x00080000, 0x00100000, 0x00200000, 0x00400000, 0x00800000, 0x01000000,
			0x02000000, 0x04000000, 0x08000000, 0x10000000, 0x20000000, 0x40000000, 0x80000000 };
	long[] values = new long[MaxByteValues];
	int BITCOUNT;
	long POLYNOMINAL;
	boolean FREVERSE;
	long INITIAL;
	long FINALMASK;
	long mask;
	long crc_register;

	public CRCValue(int BitCount, long Polynominal, boolean ShiftRight, long Initial, long FinalMask) {
		// BitCount : CRC Size
		// Polynominal : CRC Polynomial
		// Reverse : Reversed (means shift right)
		// Initial : Initial CRC Register Value
		// FinalMask : Final CRC XOR Value
		BITCOUNT = BitCount;
		POLYNOMINAL = Polynominal;
		INITIAL = Initial;
		FINALMASK = FinalMask;
		int l = 1;
		mask = ((l << (BITCOUNT - 1)) - 1) | (l << (BITCOUNT - 1));
		this.setREVERSE(ShiftRight);
	}

	long Reverse(long value) {
		// This function returns the reversed bit patter from its input.
		// For example, 1010 becomes 0101.
		//
		// Parameters:
		//
		// value: The value to reverse
		int result = 0;
		for (int jj = 0; jj < BITCOUNT; ++jj) {
			if ((value & bits[jj]) != 0)
				result |= bits[BITCOUNT - jj - 1];
		}
		return result;
	}

	long ForwardTableEntry(int entryindex) {
		// This function creates a CRC table entry for a non-reversed
		// CRC function.
		//
		// Parameters:
		//
		// entryindex: The index of the CRC table entry.
		//
		// Return Value:
		//
		// The value for the specified CRC table entry.
		//
		long result = entryindex << (BITCOUNT - BitSperByte);
		for (int ii = 0; ii < BitSperByte; ++ii) {
			if ((result & bits[BITCOUNT - 1]) == 0)
				result <<= 1;
			else
				result = (result << 1) ^ POLYNOMINAL;
		}
		result = result & mask;
		return result;
	}

	long ReverseTableEntry(int entryindex) {
		// This function creates a CRC table entry for a reversed
		// CRC function.
		//
		// Parameters:
		//
		// entryindex: The index of the CRC table entry.
		//
		// Return Value:
		//
		// The value for the specified CRC table entry.
		//
		long result = entryindex;
		for (int ii = 0; ii < BitSperByte; ++ii) {
			if ((result & 1) == 0)
				result >>= 1;
			else
				result = (result >> 1) ^ Reverse(POLYNOMINAL);
		}
		result = result & mask;
		return result;
	}

	void reset() {
		crc_register = INITIAL;
	}

	long value() {
		long result = crc_register ^ FINALMASK;
		result &= mask;
		return result;
	}

	void update(byte[] buffer,int Start, int length) {
		// This function updates the value of the CRC register based upon
		// the contents of a buffer.
		//
		// Parameters:
		//
		// buffer: The input buffer
		// length: The length of the input buffer.
		//
		// The process for updating depends upon whether or not we are using
		// the reversed CRC form.
		int end = length+Start;
		if (getREVERSE()) {
			for (int ii = Start; ii < end; ++ii) {
				crc_register = values[(int) ((crc_register ^ buffer[ii]) & 0xFF)] ^ (crc_register >> 8);
			}
		} else {
			for (int ii = Start; ii < end; ++ii) {
				long index = ((crc_register >> (BITCOUNT - BitSperByte)) ^ buffer[ii]);
				crc_register = values[(int) (index & 0xFF)] ^ (crc_register << BitSperByte);
			}
		}
	}

	public long GetCrc32(byte[] buffer, int length)
	{
		return GetCrc32(buffer,0,length);
	}
	public long GetCrc32(byte[] buffer, int Start,int length) {
		reset();
		update(buffer, Start,length);
		return value();
	}

	public long GetCrc(byte[] buffer, int Start,int length)
	{
		return GetCrc32(buffer,Start, length);
	}
	public long GetCrc(byte[] buffer, int length) {
		return GetCrc(buffer,0, length);
	}

	public short GetCrc16(byte[] buffer,  int Start,int length) {
		return (short) GetCrc32(buffer, Start,length);
	}
	public short GetCrc16(byte[] buffer, int length) 
	{
		return GetCrc16(buffer,0,length);
	}

	public byte GetCrc8(byte[] buffer, int Start, int length) {
		return (byte) (GetCrc32(buffer, Start,length));
	}
	
	public byte GetCrc8(byte[] buffer, int length) 
	{
		return GetCrc8(buffer,0,length);
	}

	public long[] getBits() {
		return bits;
	}

	boolean getREVERSE() {
		return FREVERSE;
	}

	void setREVERSE(boolean value) {
		FREVERSE = value;
		if (FREVERSE) {
			for (int ii = 0; ii < MaxByteValues; ++ii)
				values[ii] = ReverseTableEntry(ii);
		} else {
			for (int ii = 0; ii < MaxByteValues; ++ii)
				values[ii] = ForwardTableEntry(ii);
		}
	}
}