package org.to2mbn.maptranslator.impl.nbt.parse;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NBTList extends NBT {

	public static final byte ID = 9;

	private List<NBT> tagList = new ArrayList<>();
	Set<Integer> _noIndex = new HashSet<>();

	/**
	 * The type byte for the tags in the list - they must all be of the same
	 * type.
	 */
	protected byte tagType = 0;

	public void add(NBT nbt) {
		if (tagType == 0) {
			tagType = nbt.getId();
		} else if (tagType != nbt.getId()) {
			throw new IllegalArgumentException("expected type: " + tagType + ", actual type: " + nbt.getId());
		}

		tagList.add(nbt);
	}

	@Override
	public NBT clone() {
		NBTList copy = new NBTList();
		copy.tagType = tagType;
		tagList.forEach(child -> copy.tagList.add(child.clone()));
		return copy;
	}

	@Override
	public boolean equals(Object another) {
		if (super.equals(another)) {
			NBTList casted = (NBTList) another;
			return tagType == casted.tagType && tagList.equals(casted.tagList);
		}

		return false;
	}

	public NBT get(int idx) {
		return tagList.get(idx);
	}

	@Override
	public byte getId() {
		return ID;
	}

	public int getTagType() {
		return tagType;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ tagList.hashCode();
	}

	@Override
	protected void read(DataInput input) throws IOException {
		tagType = input.readByte();
		int len = input.readInt();

		for (int i = 0; i < len; i++) {
			NBT child = NBT.createNewByType(tagType);
			child.read(input);
			tagList.add(child);
		}
	}

	public NBT remove(int i) {
		return tagList.remove(i);
	}

	public void set(int idx, NBT nbt) {
		if (tagType == 0) {
			tagType = nbt.getId();
		} else if (tagType != nbt.getId()) {
			return;
		}

		tagList.set(idx, nbt);
	}

	public int size() {
		return tagList.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		if (!tagList.isEmpty()) {
			for (int i = 0; i < tagList.size(); i++) {
				if (NBTVersion.getCurrentConfig().getOutputVersion() == NBTVersion.MC_OLD) {
					if (!_noIndex.contains(i)) {
						sb.append(i).append(':');
					}
				}
				sb.append(tagList.get(i)).append(',');
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append(']');
		return sb.toString();
	}

	@Override
	protected void write(DataOutput output) throws IOException {
		if (!tagList.isEmpty()) {
			tagType = tagList.get(0).getId();
		} else {
			tagType = 0;
		}

		output.writeByte(tagType);
		output.writeInt(tagList.size());

		for (int var2 = 0; var2 < tagList.size(); ++var2) {
			tagList.get(var2).write(output);
		}
	}

}
