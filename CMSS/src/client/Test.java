package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Test {
	public static void main(String[] args) {
		File file = new File("C:\\Users\\User\\Desktop\\a\\b");
		if (!file.exists()){
			file.mkdirs();
		}
	}
}
