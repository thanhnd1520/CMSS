package bussiness;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import config.Configuration;
import model.MessageDefs;
import model.TLVAttribute;
import model.TLVMessage;

public class FileProcessReceive {
	private File file;
	private FileOutputStream fou;
	private static final String clientFolder = Configuration.getProperties("client.folderDirectory");
	private static final int fragementSize = Integer.parseInt(Configuration.getProperties("fragementSize"));
	
	public int createFile(TLVMessage tlv) {
		try {
			String fileName = new String(tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_NAME).getData());
			file = new File(clientFolder + "/" + fileName);
			fou = new FileOutputStream(file);
			byte[] data = tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_FRAGMENT).getData();
			fou.write(data);
			return 1; 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(NullPointerException e) {
			return 1; 
		}
		return 0;
	}
	
	public int appendFile(TLVMessage tlv) {
		try {
			byte[] data = tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_FRAGMENT).getData();
			fou.write(data);
			return 1; 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(NullPointerException e) {
			return 1; 
		}
		return 0;
	}
	public int closeFile(TLVMessage tlv) {
		try {
			byte[] data = tlv.getAttribute(MessageDefs.FieldTypes.FT_FILE_FRAGMENT).getData();
			fou.write(data);
			fou.close();
			return 1; 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(NullPointerException e) {
			try {
				fou.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return 1;
		}
		return 0;
	}
	
	public String getFileName() {
		return file.getName();
	}
	
	
	public FileProcessReceive() {
		super();
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public FileOutputStream getFou() {
		return fou;
	}

	public void setFou(FileOutputStream fou) {
		this.fou = fou;
	}
}
