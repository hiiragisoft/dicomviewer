
package com.hiiragi.med.dev;

public class ImageGray16Bit {

	private int width = 0;

	/**
	 * Image height that correspond to
	 * the number of row.
	 */
	private int height = 0;

	/**
	 * Image data.
	 */
	private int[] imageData = null;
	private int[] originalImageData = null;

	private String patientName;
	private String patientPrename;
	private String patientBirth;


	public void setImageData(int[] imageData)
	{
		this.imageData = imageData;
	}

	public int[] getImageData()
	{
		return imageData;
	}

	// make a safe copy of the image data
	public void setOriginalImageData(int[] imageData)
	{
		if(imageData == null)
			this.originalImageData = imageData;
		this.originalImageData = new int[imageData.length];
		System.arraycopy(imageData, 0, this.originalImageData, 0, imageData.length);
	}

	// return only a copy of the original image data - leave the original image data unchanged
	public int[] getOriginalImageData()
	{
		if(this.originalImageData == null)
			return originalImageData;
		int result[] = new int[this.originalImageData.length];
		System.arraycopy(originalImageData, 0, result, 0, this.originalImageData.length);
		return result;
	}


	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getWidth()
	{
		return width;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	public int getHeight()
	{
		return height;
	}

	public void setPatientName(String value)
	{
		this.patientName = value;
	}
	public void setPatientPrename(String value)
	{
		this.patientPrename = value;
	}
	public void setPatientBirth(String value)
	{
		this.patientBirth = value;
	}

	public String getPatientName()
	{
		return patientName;
	}
	public String getPatientPrename()
	{
		return patientPrename;
	}
	public String getPatientBirth()
	{
		return patientBirth;
	}
}
