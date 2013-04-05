
package com.hiiragi.med.dev;
import java.io.File;
import java.io.FileFilter;

public class DicomFileFilter implements FileFilter{

	public boolean accept(File pathname)
	{
		if (pathname.isFile() && !pathname.isHidden())
		{
			// Get the file name
			String fileName = pathname.getName();
			fileName = fileName.toLowerCase();
			// If the file is a dicomdir return false
			if (fileName.equals("dicomdir"))
				return false;
			// file must end with ".dcm" or no extension
			if(fileName.lastIndexOf(".") == -1)return true;
			if(fileName.endsWith("dcm"))return true;
		}
		return false;
	}

}
