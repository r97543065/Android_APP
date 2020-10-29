package com.gingytech.gtm.app.hybrid;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.gingytech.gtm.app.handprint.Protocol.Defined;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

/*

 */
public class fileHelp {

	String TAG = "fileHelp";
	Context context;
	String path;
	String pathDate;
//	String templatePath;

	private FileInputStream templateFIS = null;
	private FileOutputStream templateFOS = null;
	
	public fileHelp(Context c) {
		context = c;
		String SDPATH = Environment.getExternalStorageDirectory().getAbsolutePath();
		path = SDPATH + "/" + Defined.FilePath + "/";
		isExist(path);
		setLog();
		Log.e(TAG, path);
	}

	/**
	 * 
	 * @param path
	 */
	public void isExist(String path) {
		File file = new File(path);

		if (!file.exists()) {
			file.mkdir();
		}
	}

	/**
	 * 
	 * @param path
	 * @file file
	 */
	public void WriteFile(String name, byte[] buffer) {
		File file = new File(path, name);
		try {
			FileOutputStream outStream = new FileOutputStream(file);
			outStream.write(buffer);
			outStream.close();
		} catch (Exception er) {
			Log.e(TAG, "" + er.toString());
		}
	}
	
	// for temporary file name and destination file name.
	private File getTemplateDBFile(String fileName) {
		File gtmTemplatePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Defined.GTM_TEMPLATE_PATH);
		if(!gtmTemplatePath.exists()) {
			if(!gtmTemplatePath.mkdirs()) {
				Log.d(TAG, "can not create directory...");
			}
		}
		
		File file = new File(gtmTemplatePath.getPath() + File.separator + fileName);
		
		return file;
	}
	
	public void beginWriteTemplateDBFile() {
		// open temporary file as create
		// allocate 4 bytes as template count
		int templateCount = 0;
		
		File file = getTemplateDBFile(Defined.GTM_TEMPORARY_FILENAME);
		
		try {	
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			// cleanup previous data as new file.
			raf.setLength(0);
			// write a integer as template count
			// In beginWriteTemplateDBFile, it is 0.
			raf.writeInt(templateCount);
			raf.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeTemplateDBFile(GtTemplate gtTemplate) throws NullPointerException {
		
		File file = getTemplateDBFile(Defined.GTM_TEMPORARY_FILENAME);
		
		int templateId = gtTemplate.getId();
		byte[] templateData = gtTemplate.getTemplate();
		
		try {
			// write each template data as (id, template data)
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			long currentLength = raf.length();
			raf.seek(currentLength);
			raf.writeInt(templateId);
			raf.write(templateData);
			raf.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void endWriteTemplateDBFile(int templateCount) {
		// replace begin 4 bytes as template count
		// replace original db file if exist.
		
		File file = getTemplateDBFile(Defined.GTM_TEMPORARY_FILENAME);
		File destFile = getTemplateDBFile(Defined.GTM_TEMPLATE_DB_FILENAME);
		
		try {
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			// seek file pointer to the head
			raf.seek(0);
			// write the finally template count.
			raf.writeInt(templateCount);
			raf.close();
			
			file.renameTo(destFile);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// get template count and return it.
	// return current file pointer
	public long beginReadTemplateDBFile(int[] retValue) throws FileNotFoundException, IOException {
		long currentPos = 0;
		File file = getTemplateDBFile(Defined.GTM_TEMPLATE_DB_FILENAME);
		
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		// read first 4 bytes as int which is template count.
		retValue[0] = raf.readInt();
		// get current file pointer and return it.
		currentPos = raf.getFilePointer();
		raf.close();
		
//		try {
//			
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		return currentPos;
	}
	
	public long readTemplateDBFile(GtTemplate gtTemplate, int length, long pos) throws FileNotFoundException, IOException {
		long currentPos = 0;
		File file = getTemplateDBFile(Defined.GTM_TEMPLATE_DB_FILENAME);
		
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		// seek to properly file pointer
		raf.seek(pos);
		// read data as (id, template data)
		int id = raf.readInt();
		byte[] b = new byte[length];
		raf.read(b, 0, length);
		// set to GtTemplate Object.
		gtTemplate.setId(id);
		gtTemplate.setTemplate(b);
		// get current filepointer and return it.
		currentPos = raf.getFilePointer();
		raf.close();
		
//		try {
//			
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		return currentPos;
	}

	// set message file
	public void setLog() {
		Date date = new Date();
		// yyyy-MM-dd'T'HH:mm:ss.SSSZ
		SimpleDateFormat dateForm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		// SimpleDateFormat dateForm = new SimpleDateFormat("yyyy-MM-dd");
		pathDate = path + dateForm.format(date);

		File file = new File(pathDate + "-log.txt");

		if (file.exists()) {
			file.delete();
		}

		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Write message to file
	public void writeLog(String context) {
		File logFile = new File(pathDate + "-log.txt");
		Date time = new Date();
		SimpleDateFormat form = new SimpleDateFormat("HH:mm:ss");

		String output = form.format(time) + " " + context;

		if (!logFile.exists()) {
			setLog();
		}

		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
			buf.append(output);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// get LogCat file (only E, W, and I)
	public File getLogCat() {
		// set a file
		String fullName = pathDate + "-logcat.txt";
		File file = new File(fullName);

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// get logcat to file
		int pid = android.os.Process.myPid();
		try {
			String command = String.format("logcat -d -v threadtime *:*");
			Process process = Runtime.getRuntime().exec(command);

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder result = new StringBuilder();
			String currentLine = null;

			while ((currentLine = reader.readLine()) != null) {
				if (currentLine != null && currentLine.contains(String.valueOf(pid))) {
					if (currentLine.contains("W ") || currentLine.contains("E ") || currentLine.contains("I ")) {
						result.append(currentLine);
						result.append("\n");
					}
				}
			}

			FileWriter out = new FileWriter(file);
			out.write(result.toString());
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return file;
	}
	
	
	


	
	
}


