package org.to2mbn.maptranslator.impl.nbt.parse;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class NBTIntArray extends NBT {

	public static final byte ID = 11;

	private int[] data;

	protected NBTIntArray() {}

	public NBTIntArray(int[] data) {
		this.data = data;
	}

	@Override
	public NBT clone() {
		int[] copy = new int[data.length];
		System.arraycopy(data, 0, copy, 0, data.length);
		return new NBTIntArray(copy);
	}

	@Override
	public boolean equals(Object another) {
		return super.equals(another) ? Arrays.equals(data, ((NBTIntArray) another).data) : false;
	}

	@Override
	public byte getId() {
		return (byte) 11;
	}

	public int[] getIntArray() {
		return data;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ Arrays.hashCode(data);
	}

	@Override
	protected void read(DataInput in) throws IOException {
		int len = in.readInt();
		data = new int[len];

		for (int i = 0; i < len; ++i) {
			data[i] = in.readInt();
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		if (data.length > 0) {
			for (int d : data)
				sb.append(d).append(',');
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append(']');
		return sb.toString();
	}

	@Override
	protected void write(DataOutput out) throws IOException {
		out.writeInt(data.length);

		for (int element : data) {
			out.writeInt(element);
		}
	}

}