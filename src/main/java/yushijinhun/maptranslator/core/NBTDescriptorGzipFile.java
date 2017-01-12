package yushijinhun.maptranslator.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import yushijinhun.maptranslator.nbt.NBTIO;
import yushijinhun.maptranslator.nbt.NBTCompound;

class NBTDescriptorGzipFile extends NBTDescriptorFile {

	public NBTDescriptorGzipFile(File file) {
		super(file);
	}

	@Override
	public NBTCompound read() {
		try (FileInputStream in = new FileInputStream(file)) {
			return NBTIO.readCompressed(in);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void write(NBTCompound nbt) {
		try (FileOutputStream out = new FileOutputStream(file)) {
			NBTIO.writeCompressed(nbt, out);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
