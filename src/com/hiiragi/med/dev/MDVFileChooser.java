/*
  copyright (C) 2013 (株)柊ソフト開発
  Dicom Viewer Prototypeは、Dicomフォーマットファイルの「画像」及び「タグ情報」を表示するソフトウェアです
  GPLv3(General Public License) に準じています

  リリース日: 28-02-2013
  Version: 1.0
 */
package com.hiiragi.med.dev;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
//import java.util.Locale;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.io.DicomInputStream;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
//import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MDVFileChooser extends ListActivity {

	private static final String TOP_DIRECTORY = "top_directory";

	private File topDirectoryFile;

	private static final short MENU_ABOUT = 1;

	private int totalFiles = 0;

	ArrayAdapter<String> mAdapter;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// Set the content view
		// Check if the external storage is available
		if (ExternalDevice.isExternalDeviceAvailable()) {

			if (savedInstanceState != null)
			{
				String topDirectoryString = savedInstanceState.getString(TOP_DIRECTORY);
				topDirectoryFile = (topDirectoryString == null) ? Environment.getExternalStorageDirectory(): new File(savedInstanceState.getString("top_directory"));
			}
			else
			{
				// Set the top directory
				topDirectoryFile = Environment.getExternalStorageDirectory();
			}
		}

		// 「ファイル選択」の表示
		Toast toast = Toast.makeText(this, getString(R.string.select_file), Toast.LENGTH_SHORT);
		toast .setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	protected void onResume() {

		// If there is no external storage available, quit the application
		if (!ExternalDevice.isExternalDeviceAvailable()) {

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			String no_external_device_found = getString(R.string.no_external_device_found);
			String app_will_quit_now = getString(R.string.app_will_quit_now);
			builder.setMessage(no_external_device_found + "\n" + app_will_quit_now )
					.setTitle(no_external_device_found)
					.setCancelable(false)
					.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							MDVFileChooser.this.finish();
						}
					});

			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		}
		else
		{
			fill();
		}

		super.onResume();
	}

	/* (non-Javadoc)
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
		String itemName = mAdapter.getItem(position);
		// If it is a directory, display its content
		if (itemName.charAt(0) == '/')
		{
			 topDirectoryFile = new File(topDirectoryFile.getPath() + itemName);
			 fill();
		// If itemNam = ".." go to parent directory
		}
		else if (itemName.equals(".."))
		{
			topDirectoryFile = topDirectoryFile.getParentFile();
			fill();
		// If it is a file.
		}
		else
		{
			try
			{
				// Create a DICOMReader to parse meta information
				BasicDicomObject bdo = new BasicDicomObject();
		    	DicomInputStream dis = new DicomInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(topDirectoryFile.getPath() + "/" + itemName)));
		    	dis.readDicomObject(bdo, -1);
		    	dis.close();
		    	String strMetaInformation = bdo.getString(0x00020002); // MediaStorageSOPClassUID

				if(strMetaInformation != null && strMetaInformation.equals("1.2.840.10008.1.3.10")) {
					String media_storage_device_not_supported = getString(R.string.media_storage_device_not_supported);
					String error_loading_file = getString(R.string.error_loading_file);

					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage(media_storage_device_not_supported)
					   .setTitle(error_loading_file + itemName)
				       .setCancelable(false)
				       .setPositiveButton("Close", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                // Do nothing
				           }
				       });

					AlertDialog alertDialog = builder.create();
					alertDialog.show();
				}
				else
				{
					// Open the dicom Viewer
					Intent intent = new Intent(this, DicomViewerPrototype.class);
					intent.putExtra("DicomFileName", topDirectoryFile.getPath() + "/" + itemName);
					intent.putExtra("FileCount", totalFiles);
					startActivity(intent);
				}
			}
			catch (Exception ex)
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				String error_opening_file = getString(R.string.error_opening_file);
				builder.setMessage(error_opening_file + itemName
						+ ". \n" + ex.getMessage())
					   .setTitle(error_opening_file + itemName)
				       .setCancelable(false)
				       .setPositiveButton("Close", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                // Do nothing
				           }
				       });
				AlertDialog alertDialog = builder.create();
				alertDialog.show();
			}
		}
	}

	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putString(TOP_DIRECTORY, topDirectoryFile.getAbsolutePath());
	}

	protected Dialog onCreateDialog(int id)
	{
		return super.onCreateDialog(id);
	}


	public void onBackPressed()
	{
		// If the directory is the external storage directory or there is no parent,
		// super.onBackPressed(). Else go to parent directory.
		if (topDirectoryFile.getParent() == null || topDirectoryFile.equals(Environment.getExternalStorageDirectory()))
		{
			super.onBackPressed();
		}
		else
		{
			topDirectoryFile = topDirectoryFile.getParentFile();
			fill();
		}
	}


	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		String menu_about = getString(R.string.menu_about);
		menu.add(0, MENU_ABOUT, 1, menu_about);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		switch (item.getItemId())
		{
		case MENU_ABOUT:
			showMenuAbout();
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


	private void fill()
	{
		// If the external storage is not available, we cannot
		// fill the view
		if (!ExternalDevice.isExternalDeviceAvailable())return;

		// Get the children directories and the files of top directories
		File[] childrenFiles = topDirectoryFile.listFiles();

		// Declare the directories and the files array
		List<String> directoryList = new ArrayList<String>();
		List<String> fileList = new ArrayList<String>();

		// Loop on all children
		for (File child: childrenFiles)
		{
			// If it is a directory
			if (child.isDirectory())
			{
				String directoryName = child.getName();
				if (directoryName.charAt(0) != '.')
					directoryList.add("/" + child.getName());
			}
			else
			{
				String[] fileName = child.getName().split("\\.");
				if (!child.isHidden())
				{
					if (fileName.length > 1)
					{
						// dicom files have no extension or dcm extension
						if (fileName[fileName.length-1].equalsIgnoreCase("dcm"))
						{
							fileList.add(child.getName());
						}
					}
					else
					{
						fileList.add(child.getName());
					}
				}
			}
		}
		// Sort both list
		Collections.sort(directoryList, String.CASE_INSENSITIVE_ORDER);
		Collections.sort(fileList, String.CASE_INSENSITIVE_ORDER);

		totalFiles = fileList.size();
		// Output list will be files before directories
		// then we add the directoryList to the fileList
		fileList.addAll(directoryList);
		if (!topDirectoryFile.equals(Environment.getExternalStorageDirectory()))fileList.add(0, "..");
		mAdapter = new ArrayAdapter<String>(this, R.layout.file_chooser_item, R.id.fileName, fileList);
		setListAdapter(mAdapter);
	}

}
