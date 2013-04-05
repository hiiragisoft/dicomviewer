
package com.hiiragi.med.dev;

import java.text.DateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.io.DicomInputStream;

import android.util.Log;

public class DicomReader {


	BasicDicomObject bdo;
	DicomInputStream dis;
	int pixelData[] = null;
	int width, height;

	String PatientName = "";
	String PatientPrename = "";
	Date PatientBirth = null;
	String PatientBirthString = "";

	public DicomReader(String fileName)
	{
		this.init(fileName);
	}


	private void init(String fileName)
	{
		try
		{
			bdo = new BasicDicomObject();
			dis = new DicomInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(fileName)));
			dis.readDicomObject(bdo, -1);
			height = bdo.getInt(org.dcm4che2.data.Tag.Rows);
	    	width = bdo.getInt(org.dcm4che2.data.Tag.Columns);
/*
	    	String completeName = bdo.getString(org.dcm4che2.data.Tag.PatientName);
System.out.println("aaa");
//System.out.println(completeName);
System.out.println("bbb");
    	StringTokenizer tokenizer = new StringTokenizer(completeName, "^");
	    	int counter = 0;

	    	while(tokenizer.hasMoreElements())
	    	{
	    		if(counter == 0)
	    			PatientName = tokenizer.nextToken();
	    		else if(counter == 1)
	    			PatientPrename = tokenizer.nextToken();
	    		counter++;
	    	}

*/
	    	PatientBirth = bdo.getDate(org.dcm4che2.data.Tag.PatientBirthDate);
	    	if(PatientBirth != null)
	    	{
	    		PatientBirthString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(PatientBirth);
	    	}
	    	int bitsAllocated = bdo.getInt(org.dcm4che2.data.Tag.BitsAllocated);
	    	if(bitsAllocated == 8 || bitsAllocated == 12 || bitsAllocated == 16)
	    	{
	    		byte bytePixels[] = DicomHelper.readPixelData(bdo);
	    		pixelData = DicomHelper.convertToIntPixelData(bytePixels, bitsAllocated, width, height);
	    	}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new IllegalArgumentException(ex.getCause());
		}
	}


	public int[] getPixelData()
	{
		return pixelData;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public String getPatientName()
	{
		return PatientName;
	}

	public String getPatientPrename()
	{
		return PatientPrename;
	}
	public String getPatientBirthString()
	{
		return PatientBirthString;
	}
}
