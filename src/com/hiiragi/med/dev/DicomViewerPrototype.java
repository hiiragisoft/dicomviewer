/*
  copyright (C) 2013 (株)柊ソフト開発
  Dicom Viewer Prototypeは、Dicomフォーマットファイルの「画像」及び「タグ情報」を表示するソフトウェアです
  GPLv3(General Public License) に準じています

  リリース日: 28-02-2013
  Version: 1.0
 */

package com.hiiragi.med.dev;

//import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
//import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
//import java.util.Vector;

//import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.VRMap;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.util.TagUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
//import android.content.SharedPreferences;
//import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;
//import android.widget.Toast;
import android.widget.Button;

import android.widget.ToggleButton;
import android.widget.CompoundButton;


public class DicomViewerPrototype extends Activity implements SeekBar.OnSeekBarChangeListener
{

	public static final String FILE_NAME 					= "file_name";
//	public static final String SEEKBAR_VISIBILITY 			= "SeekBar_Visibility";
//	public static final String DISCLAIMER_ACCEPTED 			= "Disclaimer_Accepted";
//	public static final String PATIENTDATA_VISIBILITY 		= "PatientData_Visibility";


	public static final short OUT_OF_MEMORY = 0;

	/**
	 * The thread is started.
	 */
	public static final short STARTED = 1;

	/**
	 * The thread is finished.
	 */
	public static final short FINISHED = 2;

	/**
	 * The thread progression update.
	 */
	public static final short PROGRESSION_UPDATE = 3;

	/**
	 * An error occurred while the thread running that cannot
	 * be managed.
	 */
	public static final short UNCATCHABLE_ERROR_OCCURRED = 4;

	private DicomImageView imageView;
	private DicomFileLoader dicomFileLoader;
	private File[] fileArray = null;
	private int currentFileIndex = -1;
	private String actualFileName = "";

	private boolean isInitialized = false;

	private static final short MENU_ABOUT = 4;
//	private static final short MENU_CONFIGURE_PATIENT_DATA = 6;

	private static final short PROGRESS_IMAGE_LOAD = 0;
	private ProgressDialog imageLoadingDialog;

	private SeekBar brightnessSeekBar;
	private TextView brightnessValue;
	private TextView brightnessLabel;

	private boolean allowEvaluateProgressValue = true;
//	private boolean seekBarVisibility = true;
//	private boolean patientDataVisibility = false;
//private static final String TAG = "MinimalDicomViewer";
	private static final String TAG = "DicomViewerPrototype";

//	public static final String PREFERENCES_NAME = "MDVPreferencesFile";
	Context context;

	private DicomObject object;

//	private BasicDicomObject object;

	// Dicomタグ表示
	private String message;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        context = this;
        VRMap.getVRMap();
        VRMap.loadVRMap( "org/dcm4che2/data/VRMap.ser" );
        setContentView(R.layout.main);
        imageView = (DicomImageView)findViewById(R.id.imageView);
        brightnessSeekBar = (SeekBar)findViewById(R.id.brightnessSeekBar);
        brightnessValue = (TextView)findViewById(R.id.brightnessValue);
        brightnessLabel = (TextView)findViewById(R.id.brightnessLabel);
        brightnessLabel.setText(getString(R.string.brightness));

        // Set the seek bar change index listener
        brightnessSeekBar.setOnSeekBarChangeListener(this);
        brightnessSeekBar.setMax(255);

        String fileName = null;
/*
        SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, 0);
        if(settings != null)
        {
        	boolean value = settings.getBoolean(SEEKBAR_VISIBILITY, true);
        	if(!value)
        	{
        		brightnessSeekBar.setVisibility(View.INVISIBLE);
				brightnessLabel.setVisibility(View.INVISIBLE);
				brightnessValue.setVisibility(View.INVISIBLE);
				seekBarVisibility = false;
        	}
        }
*/
		// If the saved instance state is not null get the file name
		if (savedInstanceState != null)
		{
Log.d(TAG, "ログメッセージA：savedInstanceState not null");
			fileName = savedInstanceState.getString(FILE_NAME);

		}
		else // Get the intent
		{
Log.d(TAG, "ログメッセージB：intent is not null");
			Intent intent = getIntent();
			if (intent != null)
			{
				Bundle extras = intent.getExtras();

				fileName = extras == null ? null : extras.getString("DicomFileName");

			}
		}
		if (fileName == null)
		{
			showExitAlertDialog(getString(R.string.error_loading_file),
					getString(R.string.error_unable_loading_file)+"\n\n" +
					getString(R.string.cannot_retrieve_name));
		}
		else
		{
			// Get the File object for the current file
			File currentFile = new File(fileName);

			// Start the loading thread to load the DICOM image
			actualFileName = fileName;
			dicomFileLoader = new DicomFileLoader(loadingHandler, fileName);
			dicomFileLoader.start();
			//busy = true;

//			DicomObject ds = null;
			try {
/*
				DicomInputStream dis = new DicomInputStream(currentFile);
				object = dis.readDicomObject();
				dis.close();
*/

				FileInputStream fis = new FileInputStream(currentFile);
				BufferedInputStream bis = new BufferedInputStream(fis);

				DicomInputStream dis = null;
				dis = new DicomInputStream(bis);
				object = dis.readDicomObject();
				dis.close();

			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} finally {
//				dis.close();

			}

			try {

				this.message = getListHeader(object);

			} catch (UnsupportedEncodingException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			Log.d(TAG, "Tag_Disp_start");
			Log.d(TAG, this.message);
			Log.d(TAG, "Tag_Disp_end");

			// Get the files array = get the files contained
			// in the parent of the current file
			fileArray = currentFile.getParentFile().listFiles(new DicomFileFilter());

			// Sort the files array
			Arrays.sort(fileArray);

			// If the files array is null or its length is less than 1,
			// there is an error because it must at least contain 1 file:
			// the current file
			if (fileArray == null || fileArray.length < 1)
			{
				showExitAlertDialog(getString(R.string.error_loading_file),
						getString(R.string.error_unable_loading_file)+"\n\n" +
						getString(R.string.no_dicom_files_in_directory));
			}
			else
			{
				// Get the file index in the array
				currentFileIndex = getIndex(currentFile);

				// If the current file index is negative
				// or greater or equal to the files array
				// length there is an error
				if (currentFileIndex < 0 || currentFileIndex >= fileArray.length)
				{
					showExitAlertDialog(getString(R.string.error_loading_file),
							getString(R.string.error_unable_loading_file)+"\n\n" +
							getString(R.string.file_is_not_in_directory));
				// Else initialize views and navigation bar
				}
			}

			Button b2 = (Button) findViewById(R.id.button2);

			b2.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
Log.d(TAG, "ログメッセージA：onClickでintent");
					// Open the FileChooser
					Intent intent = new Intent(getApplicationContext(), MDVFileChooser.class);
//					intent.putExtra("DicomFileName", topDirectoryFile.getPath() + "/" + itemName);
//					intent.putExtra("FileCount", totalFiles);
Log.d(TAG, "ログメッセージ：onClickでintent");
					startActivity(intent);
				}
			});

			// Tag情報表示ボタン
			Button b1 = (Button) findViewById(R.id.button1);
			b1.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					// Tag情報をアラートで表示
			        AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
			        alert.setTitle("タグ情報");
			        alert.setMessage(message);
			        alert.setPositiveButton("OK", null);
			        alert.show();
				}
			});

			// イメージ反転トグルボタン
			ToggleButton tb = (ToggleButton) findViewById(R.id.ToggleButton01);

			tb.setTextOff(getString(R.string.toggle_off));
			tb.setTextOn(getString(R.string.toggle_on));
			tb.setChecked(false);		//OFFへ変更

			//ToggleのCheckが変更したタイミングで呼び出されるリスナー
			tb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					//トグルキーが変更された際に呼び出される
					paintInvert();
					imageView.updateMatrix();
				}
			});
		}
    }


	/*
	 *  Dicomタグ取得
	 * @param DicomObject object.
	 * @return  String message
	 */

	public String getListHeader(DicomObject object) throws UnsupportedEncodingException {

		List<String> taglist = new ArrayList<String>();
		Iterator<DicomElement> iter = object.datasetIterator();
		while(iter.hasNext()) {
			DicomElement element = (DicomElement) iter.next();
			int tag = element.tag();
			try {
//String tagName = object.nameOf(tag);
					// タグネームが取得できないので、代わりに応急的に取得
					String tagName = DicomTagNameList.getTagName(tag);

Log.d(TAG, "tag： " + tag);
Log.d(TAG, "tagName： " + tagName);


				String tagAddr = TagUtils.toString(tag);
//tagAddr = new String(tagAddr.getBytes("UTF-8"));
Log.d(TAG, "tagAddr： " + tagAddr);

				String tagVR = object.vrOf(tag).toString();
//tagVR = new String(tagVR.getBytes("UTF-8"));
Log.d(TAG, "tagVR： " + tagVR);

				if (tagVR.equals("SQ")) {
					if (element.hasItems()) {
//System.out.println(tagAddr +" ["+  tagVR +"] "+ tagName);
						getListHeader(element.getDicomObject());
						continue;
					}
				}

				// 2byteコードが取得できない（例：patient's name)
				String tagValue = object.getString(tag);
//String tagValue = "tagValue";
//Log.d(TAG, "tagValue： " + tagValue.toString());
//tagValue = new String(tagValue.getBytes("UTF-8"));

				taglist.add(tagAddr +" ["+ tagVR +"] "+ tagName +" ["+ tagValue+"]");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for ( int i = 0; i < taglist.size(); ++i ) {
//System.out.println( taglist.get( i ) );
			message += taglist.get( i ) + "\n";
		}

		return message;
	}

    private void setFilenameLabel(TextView textView, String text)
    {
    	String toPrint = text.substring(text.lastIndexOf("/") + 1);
    	textView.setTextColor(Color.rgb(204, 204, 204));
    	textView.setTextSize(20);
    	textView.setText(getString(R.string.file) +": " + toPrint);
    }

    @Override
	protected void onPause()
    {
		// We wait until the end of the loading thread
		// before putting the activity in pause mode
		if (dicomFileLoader != null)
		{
			// Wait until the loading thread die
			while (dicomFileLoader.isAlive())
			{
				try
				{
					synchronized(this)
					{
						wait(10);
					}
				}
				catch (InterruptedException e){}
			}
		}
		super.onPause();
	}

    @Override
	protected void onStop()
    {
    	super.onStop();
    }

    @Override
	protected void onDestroy()
    {
		super.onDestroy();
		fileArray = null;
		dicomFileLoader = null;

		// Free the drawable callback
		if (imageView != null)
		{
			Drawable drawable = imageView.getDrawable();
			if (drawable != null)drawable.setCallback(null);
		}
	}

    @Override
	protected void onSaveInstanceState(Bundle outState)
    {
		super.onSaveInstanceState(outState);
		// Save the current file name
		String currentFileName = fileArray[currentFileIndex].getAbsolutePath();
		outState.putString(FILE_NAME, currentFileName);
	}

    @Override
	protected Dialog onCreateDialog(int id)
    {
		switch(id)
		{
         // Create image load dialog
        case PROGRESS_IMAGE_LOAD:
        	imageLoadingDialog = new ProgressDialog(this);
        	imageLoadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        	imageLoadingDialog.setMessage(getString(R.string.cannot_retrieve_name));
        	imageLoadingDialog.setCancelable(false);
        	return imageLoadingDialog;

        default:
            return null;
        }
    }

    @Override
	public void onLowMemory()
    {
		// Hint the garbage collector
		System.gc();
		// Show the exit alert dialog
		showExitAlertDialog(getString(R.string.low_memory), getString(R.string.low_memory));
		super.onLowMemory();
	}


    @Override
	public boolean onCreateOptionsMenu(Menu menu)
    {
		super.onCreateOptionsMenu(menu);
		String menu_about = getString(R.string.menu_about);
		menu.add(0, MENU_ABOUT, 1, menu_about);
//		menu.add(4, MENU_CONFIGURE_APP, MENU_CONFIGURE_APP, Messages.getLabel(Messages.MENU_CONFIGURE_APP, Messages.Language));

		return true;
    }


    @Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
 //   	int visibility;
		switch (item.getItemId())
		{
		case MENU_ABOUT:
			 showMenuAbout();
/*
			Dialog dialog = new Dialog(this);
        	dialog.setContentView(R.layout.dialog_about);
        	dialog.setTitle(getString(R.string.about_header));
        	TextView text = (TextView)dialog.findViewById(R.id.AboutText);
        	text.setText(getString(R.string.about_message));
        	dialog.show();
			return true;
*/
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	public boolean showMenuAbout()
	{
		Dialog dialog = new Dialog(this);
    	dialog.setContentView(R.layout.dialog_about);
    	dialog.setTitle(getString(R.string.about_header));
    	TextView text = (TextView)dialog.findViewById(R.id.AboutText);
    	text.setText(getString(R.string.about_message));

    	// Dialogの外側タッチで閉じる
    	dialog.setCanceledOnTouchOutside(true);
    	dialog.show();

		return true;
	}

    String resultPathFromFileDialog = null;
    Intent fileDialogIntent;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	resultPathFromFileDialog = data.getStringExtra(FileDialog.RESULT_PATH);
    	super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
	protected void onResume()
    {
		// If there is no external storage available, quit the application
		if (!ExternalDevice.isExternalDeviceAvailable())
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.message_external_storage_error))
				   .setTitle(getString(R.string.message_external_storage_header_error))
			       .setCancelable(false)
			       .setPositiveButton("Exit", new DialogInterface.OnClickListener()
			       {
			           public void onClick(DialogInterface dialog, int id)
			           {
			                DicomViewerPrototype.this.finish();
			           }
			       });
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		}
		super.onResume();

    }

    boolean paintInverted = false;
    private void paintInvert()
    {
    	allowEvaluateProgressValue = false;
		brightnessSeekBar.setProgress(0);
		ImageGray16Bit imageGray16Bit = imageView.getImage();
		int imageData[] = imageGray16Bit.getOriginalImageData();
		if(imageData == null)
		{
			return;
		}
		imageData = DicomHelper.invertPixels(imageData);
		imageGray16Bit.setImageData(imageData);
		imageGray16Bit.setOriginalImageData(imageData);
		imageView.setImage(imageGray16Bit);
		imageView.draw();
//		imageView.paintCachedSize();
		allowEvaluateProgressValue = true;
		if(!paintInverted)paintInverted = true;
		else paintInverted = false;
    }


    /* (non-Javadoc)
	 * @see android.widget.SeekBar.OnSeekBarChangeListener#onProgressChanged(android.widget.SeekBar, int, boolean)
	 */
	public synchronized void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{

		brightnessValue.setText("" + progress);
		// on creation image on imageView may be null
		if(allowEvaluateProgressValue && imageView.getImage() != null)
		{
			ImageGray16Bit imageGray16Bit = imageView.getImage();
			int imageData[] = imageGray16Bit.getOriginalImageData();
			if(imageData == null)
			{
				return;
			}
			imageData = DicomHelper.setBrightness(imageData, progress);
			imageGray16Bit.setImageData(imageData);
			imageView.setImage(imageGray16Bit);
			imageView.draw();
		}
	}


	// Needed to implement the SeekBar.OnSeekBarChangeListener
	public void onStartTrackingTouch(SeekBar seekBar)
	{
		// nothing to do.
	}


	// Needed to implement the SeekBar.OnSeekBarChangeListener
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		System.gc(); // TODO needed ?
	}


	/**
	 * Get the index of the file in the files array.
	 * @param file
	 * @return Index of the file in the files array
	 * or -1 if the files is not in the list.
	 */
	private int getIndex(File file) {

		if (fileArray == null)
			throw new NullPointerException("The files array is null.");

		for (int i = 0; i < fileArray.length; i++)
		{
			if (fileArray[i].getName().equals(file.getName()))return i;
		}
		return -1;
	}


	private void setImage(ImageGray16Bit image)
	{
		if (image == null)
			throw new NullPointerException("The 16-Bit grayscale image is null");

		try
		{
			// Set the image
			imageView.setImage(image);

			// If it is not initialized, set the window width and center
			// as the value set in the LISA 16-Bit grayscale image
			// that comes from the DICOM image file.
			if (!isInitialized)
			{
				isInitialized = true;
			}
			imageView.draw();
		}
		catch (OutOfMemoryError ex)
		{
			System.gc();
			showExitAlertDialog(getString(R.string.header_out_of_memory_error),
					getString(R.string.out_of_memory_error));
		}
		catch (ArrayIndexOutOfBoundsException ex)
		{
			showExitAlertDialog(getString(R.string.header_index_out_of_bounds),
					getString(R.string.index_out_of_bounds));
		}
	}


	private void showExitAlertDialog(String title, String message)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
			   .setTitle(title)
		       .setCancelable(false)
		       .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               DicomViewerPrototype.this.finish();
		           }
		       });
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}


	private final Handler loadingHandler = new Handler()
	{
		public void handleMessage(Message message)
		{
			switch (message.what)
			{

			case STARTED:
				showDialog(PROGRESS_IMAGE_LOAD);
				break;

			case FINISHED:
				try
				{
					dismissDialog(PROGRESS_IMAGE_LOAD);
				}
				catch (IllegalArgumentException ex)
				{
					// Do nothing
				}
				if (message.obj instanceof ImageGray16Bit)
				{
					setImage((ImageGray16Bit) message.obj);
/*
					((TextView)findViewById(R.id.PatientNameValue)).setText( ((ImageGray16Bit) message.obj).getPatientName());
					((TextView)findViewById(R.id.PatientPrenameValue)).setText( ((ImageGray16Bit) message.obj).getPatientPrename());
					((TextView)findViewById(R.id.PatientBirthValue)).setText( ((ImageGray16Bit) message.obj).getPatientBirth());
*/
				}
				setFilenameLabel((TextView)findViewById(R.id.currentFileLabel), actualFileName);

				break;

			case UNCATCHABLE_ERROR_OCCURRED:
				try
				{
					dismissDialog(PROGRESS_IMAGE_LOAD);
				}
				catch (IllegalArgumentException ex){}

				// Get the error message
				String errorMessage;

				if (message.obj instanceof String)
					errorMessage = (String) message.obj;
				else
					errorMessage = "Unknown error";

				// Show an alert dialog
				showExitAlertDialog("[ERROR] Loading file",
						"An error occured during the file loading.\n\n"
						+ errorMessage);
				break;

			case OUT_OF_MEMORY:
				try
				{
					dismissDialog(PROGRESS_IMAGE_LOAD);
				}
				catch (IllegalArgumentException ex){}

				// Show an alert dialog
				showExitAlertDialog(getString(R.string.header_out_of_memory_error),
						getString(R.string.out_of_memory_error));
				break;
			}
		}
	};


	private static final class DicomFileLoader extends Thread
	{
		// The handler to send message to the parent thread
		private final Handler mHandler;

		// The file to load
		private final String fileName;

		public DicomFileLoader(Handler handler, String fileName)
		{

			if (handler == null)
				throw new NullPointerException("The handler is null while calling the loading thread.");

			mHandler = handler;

			if (fileName == null)
				throw new NullPointerException("The file is null while calling the loading thread.");

			this.fileName = fileName;
		}

		public void run()
		{
			// If the image data is null, do nothing.
			File f = new File(fileName);
			if (!f.exists())
			{
				Message message = mHandler.obtainMessage();
				message.what = UNCATCHABLE_ERROR_OCCURRED;
				message.obj = "The file doesn't exist.";
				mHandler.sendMessage(message);
				return;
			}

			mHandler.sendEmptyMessage(STARTED);
			// If image exists show image
			try {
				ImageGray16Bit image = null;

				DicomReader reader = new DicomReader(fileName);
				int pixelData[] = reader.getPixelData();


				if(pixelData != null)
				{

Log.d(TAG, "ログメッセージ：PixelData not null");
					image = new ImageGray16Bit();
		    		image.setImageData(pixelData);
		    		image.setOriginalImageData(pixelData);
		    		image.setWidth(reader.getWidth());
		    		image.setHeight(reader.getHeight());
		    		image.setPatientName(reader.getPatientName());
		    		image.setPatientPrename(reader.getPatientPrename());
		    		image.setPatientBirth(reader.getPatientBirthString());
				}
				// Send the LISA 16-Bit grayscale image
				Message message = mHandler.obtainMessage();
				message.what = FINISHED;
				message.obj = image;
				mHandler.sendMessage(message);
				return;

			}
			catch (Exception ex)
			{
				mHandler.sendEmptyMessage(FINISHED);
			}
		}
	}

}