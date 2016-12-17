package yushijinhun.maptranslator.core;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import yushijinhun.maptranslator.nbt.NBTIO;
import yushijinhun.maptranslator.nbt.NBTCompound;

public class NBTDescriptorPlainFile extends NBTDescriptorFile {

	public NBTDescriptorPlainFile(File file) {
		super(file);
	}

	@Override
	public NBTCompound read() {
		try {
			return NBTIO.read(file);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void write(NBTCompound nbt) {
		try {
			NBTIO.write(nbt, file);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
