/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is collection of files collectively known as Open Camera.

The Initial Developer of the Original Code is Almalence Inc.
Portions created by Initial Developer are Copyright (C) 2013 
by Almalence Inc. All Rights Reserved.
*/

/* <!-- +++
package com.almalence.opencam_plus;
+++ --> */
// <!-- -+-
package com.almalence.opencamhalv3;
//-+- -->


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.media.AudioManager;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.almalence.util.AppWidgetNotifier;
import com.almalence.util.Util;

//<!-- -+-
import com.almalence.opencamhalv3.billing.IabHelper;
import com.almalence.opencamhalv3.billing.IabResult;
import com.almalence.opencamhalv3.billing.Inventory;
import com.almalence.opencamhalv3.billing.Purchase;
import com.almalence.opencamhalv3.ui.AlmalenceGUI;
import com.almalence.opencamhalv3.ui.GLLayer;
import com.almalence.opencamhalv3.ui.GUI;
import com.almalence.util.AppRater;
//-+- -->
/* <!-- +++
import com.almalence.opencam_plus.ui.AlmalenceGUI;
import com.almalence.opencam_plus.ui.GLLayer;
import com.almalence.opencam_plus.ui.GUI;
+++ --> */

/***
 * MainScreen - main activity screen with camera functionality
 * 
 * Passes all main events to PluginManager
 ***/

public class MainScreen extends Activity implements View.OnClickListener,
		View.OnTouchListener, SurfaceHolder.Callback, Handler.Callback, Camera.ShutterCallback
{
	// >>Description
	// section with different global parameters available for everyone
	//
	// Camera parameters and possibly access to camera instance
	//
	// Global defines and others
	//
	// Description<<
	public static MainScreen thiz;
	public static Context mainContext;
	public static Handler H;

	public static boolean isHALv3 = false;

	private static final int MSG_RETURN_CAPTURED = -1;

	// public static boolean FramesShot = false;

	public static File ForceFilename = null;

//	private static Camera camera = null;
//	private static Camera.Parameters cameraParameters = null;
	
	//Interface to HALv3 camera and Old style camera
	public static CameraController cameraController = null;
	
	//HALv3 camera's objects
//	CameraManager manager = null;
//	private static CameraCharacteristics camCharacter=null;
//	cameraAvailableListener availListener = null;
//	private static CameraDevice camDevice = null;
//	CaptureRequest.Builder previewRequestBuilder = null;
	public static ImageReader mImageReaderPreviewYUV;
	public static ImageReader mImageReaderYUV;
	public static ImageReader mImageReaderJPEG;
//	String[] cameraIdList={""};
//	
//	public static boolean cameraConfigured = false;
	

	public static GUI guiManager = null;

	// OpenGL layer. May be used to allow capture plugins to draw overlaying
	// preview, such as night vision or panorama frames.
	private static GLLayer glView;

	public boolean mPausing = false;

	Bundle msavedInstanceState;
	// private. if necessary?!?!?
	public SurfaceHolder surfaceHolder;
	public SurfaceView preview;
	private Surface mCameraSurface = null;
	private OrientationEventListener orientListener;
	private boolean landscapeIsNormal = false;
	private boolean surfaceJustCreated = false;
	private boolean surfaceCreated = false;	

	// shared between activities
	public static int surfaceWidth, surfaceHeight;
	public static int imageWidth, imageHeight;
	public static int previewWidth, previewHeight;
	public static int saveImageWidth, saveImageHeight;
//	public static PowerManager pm = null;

	private CountDownTimer ScreenTimer = null;
	private boolean isScreenTimerRunning = false;

//	public static int CameraIndex = 0;
//	private static boolean CameraMirrored = false;
	private static boolean wantLandscapePhoto = false;
	public static int orientationMain = 0;
	public static int orientationMainPrevious = 0;

	private SoundPlayer shutterPlayer = null;

	// Common preferences
	public static String ImageSizeIdxPreference;
	public static boolean ShutterPreference = true;
	public static boolean ShotOnTapPreference = false;
	
	public static boolean showHelp = false;
	// public static boolean FullMediaRescan;
	public static final String SavePathPref = "savePathPref";

	private boolean keepScreenOn = false;
	
	public static String SaveToPath;
	public static String SaveToPreference;
	public static boolean SortByDataPreference;
	
	public static boolean MaxScreenBrightnessPreference;
	
	public static boolean mAFLocked = false;

	// >>Description
	// section with initialize, resume, start, stop procedures, preferences
	// access
	//
	// Initialize, stop etc depends on plugin type.
	//
	// Create main GUI controls and plugin specific controls.
	//
	// Description<<

	public static boolean isCreating = false;
	public static boolean mApplicationStarted = false;
	public static long startTime = 0;
	
	public static final String EXTRA_ITEM = "WidgetModeID"; //Clicked mode id from widget.
	public static final String EXTRA_TORCH = "WidgetTorchMode";
	
	public static final String EXTRA_SHOP = "WidgetGoShopping";
	
	public static boolean launchTorch = false;
	public static boolean goShopping = false;

	public static final int VOLUME_FUNC_SHUTTER = 0;
	public static final int VOLUME_FUNC_ZOOM 	= 1;
	public static final int VOLUME_FUNC_EXPO 	= 2;
	public static final int VOLUME_FUNC_NONE	= 3;
	
	public static String deviceSS3_01;
	public static String deviceSS3_02;
	public static String deviceSS3_03;
	public static String deviceSS3_04;
	public static String deviceSS3_05;
	public static String deviceSS3_06;
	public static String deviceSS3_07;
	public static String deviceSS3_08;
	public static String deviceSS3_09;
	public static String deviceSS3_10;
	public static String deviceSS3_11;
	public static String deviceSS3_12;
	public static String deviceSS3_13;
	
	public static List<Area> mMeteringAreaMatrix5 = new ArrayList<Area>();	
	public static List<Area> mMeteringAreaMatrix4 = new ArrayList<Area>();	
	public static List<Area> mMeteringAreaMatrix1 = new ArrayList<Area>();	
	public static List<Area> mMeteringAreaCenter = new ArrayList<Area>();	
	public static List<Area> mMeteringAreaSpot = new ArrayList<Area>();
	
//	public static String meteringModeMatrix = "Matrix";
//	public static String meteringModeCenter = "Center-weighted";
//	public static String meteringModeSpot = "Spot";
//	public static String meteringModeAuto = "Auto";
	
	public final static int meteringModeAuto = 0;
	public final static int meteringModeMatrix = 1;	
	public final static int meteringModeCenter = 2;
	public final static int meteringModeSpot = 3;
	
	
	public static int currentMeteringMode = -1;
	
	
	public final static String sEvPref = "EvCompensationValue";
	public final static String sSceneModePref = "SceneModeValue";
	public final static String sWBModePref = "WBModeValue";
	public final static String sFrontFocusModePref = "FrontFocusModeValue";
	public final static String sRearFocusModePref = "RearFocusModeValue";
	public final static String sFlashModePref = "FlashModeValue";
	public final static String sISOPref = "ISOValue";
	public final static String sMeteringModePref = "MeteringModeValue";
	
	public static int sDefaultValue = CameraParameters.SCENE_MODE_AUTO;
	public static int sDefaultFocusValue = CameraParameters.AF_MODE_CONTINUOUS_PICTURE;
	public static int sDefaultFlashValue = CameraParameters.FLASH_MODE_OFF;


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		deviceSS3_01 = getResources().getString(R.string.device_name_ss3_01);
		deviceSS3_02 = getResources().getString(R.string.device_name_ss3_02);
		deviceSS3_03 = getResources().getString(R.string.device_name_ss3_03);
		deviceSS3_04 = getResources().getString(R.string.device_name_ss3_04);
		deviceSS3_05 = getResources().getString(R.string.device_name_ss3_05);
		deviceSS3_06 = getResources().getString(R.string.device_name_ss3_06);
		deviceSS3_07 = getResources().getString(R.string.device_name_ss3_07);
		deviceSS3_08 = getResources().getString(R.string.device_name_ss3_08);
		deviceSS3_09 = getResources().getString(R.string.device_name_ss3_09);
		deviceSS3_10 = getResources().getString(R.string.device_name_ss3_10);
		deviceSS3_11 = getResources().getString(R.string.device_name_ss3_11);
		deviceSS3_12 = getResources().getString(R.string.device_name_ss3_12);
		deviceSS3_13 = getResources().getString(R.string.device_name_ss3_13);

		Intent intent = this.getIntent();
		String mode = intent.getStringExtra(EXTRA_ITEM);
		launchTorch = intent.getBooleanExtra(EXTRA_TORCH, false);
		goShopping = intent.getBooleanExtra(EXTRA_SHOP, false);
		
		startTime = System.currentTimeMillis();
		msavedInstanceState = savedInstanceState;
		mainContext = this.getBaseContext();
		H = new Handler(this);
		thiz = this;

		mApplicationStarted = false;

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// ensure landscape orientation
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// set to fullscreen
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN|WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		// set some common view here
		setContentView(R.layout.opencamera_main_layout);
		
		//reset or save settings
		ResetOrSaveSettings();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainScreen.mainContext);
		
		if(null != mode)
			prefs.edit().putString("defaultModeName", mode).commit();
		
		if(launchTorch)
			prefs.edit().putString(sFlashModePref, getResources().getString(R.string.flashTorchSystem)).commit();
		
		// <!-- -+-
		
//		/**** Billing *****/
//		if (true == prefs.contains("unlock_all_forever")) {
//			unlockAllPurchased = prefs.getBoolean("unlock_all_forever", false);
//		}
//		if (true == prefs.contains("plugin_almalence_hdr")) {
//			hdrPurchased = prefs.getBoolean("plugin_almalence_hdr", false);
//		}
//		if (true == prefs.contains("plugin_almalence_panorama")) {
//			panoramaPurchased = prefs.getBoolean("plugin_almalence_panorama", false);
//		}
//		if (true == prefs.contains("plugin_almalence_moving_burst")) {
//			objectRemovalBurstPurchased = prefs.getBoolean("plugin_almalence_moving_burst", false);
//		}
//		if (true == prefs.contains("plugin_almalence_groupshot")) {
//			groupShotPurchased = prefs.getBoolean("plugin_almalence_groupshot", false);
//		}
//		
//		createBillingHandler();
//		/**** Billing *****/
//		
//		//application rating helper
//		AppRater.app_launched(this);
		//-+- -->
		
		AppWidgetNotifier.app_launched(this);
		
		
		try{
		cameraController = CameraController.getInstance();
		}
		catch(VerifyError exp)
		{
			Log.e("MainScreen", exp.getMessage());
		}
		cameraController.onCreate();	
		
		
		// set preview, on click listener and surface buffers
		preview = (SurfaceView) this.findViewById(R.id.SurfaceView01);
		preview.setZOrderMediaOverlay(true);
		preview.setOnClickListener(this);
		preview.setOnTouchListener(this);
		preview.setKeepScreenOn(true);

		surfaceHolder = preview.getHolder();
		if(!isHALv3)
		{
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		orientListener = new OrientationEventListener(this) {
			@Override
			public void onOrientationChanged(int orientation) {
				// figure landscape or portrait
				if (MainScreen.thiz.landscapeIsNormal) {
					// Log.e("MainScreen",
					// "landscapeIsNormal = true. Orientation " + orientation +
					// "+90");
					orientation += 90;
				}
				// else
				// Log.e("MainScreen", "landscapeIsNormal = false. Orientation "
				// + orientation);

				if ((orientation < 45)
						|| (orientation > 315 && orientation < 405)
						|| ((orientation > 135) && (orientation < 225))) {
					if (MainScreen.wantLandscapePhoto == true) {
						MainScreen.wantLandscapePhoto = false;
						// Log.e("MainScreen", "Orientation = " + orientation);
						// Log.e("MainScreen","Orientation Changed. wantLandscapePhoto = "
						// + String.valueOf(MainScreen.wantLandscapePhoto));
						//PluginManager.getInstance().onOrientationChanged(false);
					}
				} else {
					if (MainScreen.wantLandscapePhoto == false) {
						MainScreen.wantLandscapePhoto = true;
						// Log.e("MainScreen", "Orientation = " + orientation);
						// Log.e("MainScreen","Orientation Changed. wantLandscapePhoto = "
						// + String.valueOf(MainScreen.wantLandscapePhoto));
						//PluginManager.getInstance().onOrientationChanged(true);
					}
				}

				// orient properly for video
				if ((orientation > 135) && (orientation < 225))
					orientationMain = 270;
				else if ((orientation < 45) || (orientation > 315))
					orientationMain = 90;
				else if ((orientation < 325) && (orientation > 225))
					orientationMain = 0;
				else if ((orientation < 135) && (orientation > 45))
					orientationMain = 180;
				
				if(orientationMain != orientationMainPrevious)
				{
					orientationMainPrevious = orientationMain;
					//PluginManager.getInstance().onOrientationChanged(orientationMain);
				}
			}
		};

		//pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

		keepScreenOn = prefs.getBoolean("keepScreenOn", false);
		// prevent power drain
//		if (!keepScreenOn)
		{
			ScreenTimer = new CountDownTimer(180000, 180000) {
				public void onTick(long millisUntilFinished) {
				}
	
				public void onFinish() {
					boolean isVideoRecording = PreferenceManager.getDefaultSharedPreferences(MainScreen.mainContext).getBoolean("videorecording", false);
					if (isVideoRecording || keepScreenOn)
					{
						//restart timer
						ScreenTimer.start();
						isScreenTimerRunning = true;
						preview.setKeepScreenOn(true);
						return;
					}
					preview.setKeepScreenOn(false);
					isScreenTimerRunning = false;
				}
			};
			ScreenTimer.start();
			isScreenTimerRunning = true;
		}

		PluginManager.getInstance().setupDefaultMode();
		// Description
		// init gui manager
		guiManager = new AlmalenceGUI();
		guiManager.createInitialGUI();
		this.findViewById(R.id.mainLayout1).invalidate();
		this.findViewById(R.id.mainLayout1).requestLayout();
		guiManager.onCreate();

		// init plugin manager
		PluginManager.getInstance().onCreate();

		if (this.getIntent().getAction() != null) {
			if (this.getIntent().getAction()
					.equals(MediaStore.ACTION_IMAGE_CAPTURE)) {
				try {
					MainScreen.ForceFilename = new File(
							((Uri) this.getIntent().getExtras()
									.getParcelable(MediaStore.EXTRA_OUTPUT))
									.getPath());
					if (MainScreen.ForceFilename.getAbsolutePath().equals(
							"/scrapSpace")) {
						MainScreen.ForceFilename = new File(Environment
								.getExternalStorageDirectory()
								.getAbsolutePath()
								+ "/mms/scrapSpace/.temp.jpg");
						new File(MainScreen.ForceFilename.getParent()).mkdirs();
					}
				} catch (Exception e) {
					MainScreen.ForceFilename = null;
				}
			} else {
				MainScreen.ForceFilename = null;
			}
		} else {
			MainScreen.ForceFilename = null;
		}
		
		// <!-- -+-
//		if(goShopping)
//		{
//			MainScreen.thiz.showUnlock = true;
//			if (MainScreen.thiz.titleUnlockAll == null || MainScreen.thiz.titleUnlockAll.endsWith("check for sale"))
//			{
//				Toast.makeText(MainScreen.mainContext, "Error connecting to Google Play. Check internet connection.", Toast.LENGTH_LONG).show();
//				return;
//			}
//			Intent shopintent = new Intent(MainScreen.thiz, Preferences.class);
//			MainScreen.thiz.startActivity(shopintent);
//		}
		//-+- -->
	}

	
	public void onPreferenceCreate(PreferenceFragment prefActivity)
	{
		CharSequence[] entries;
		CharSequence[] entryValues;

		if (CameraController.ResolutionsIdxesList != null) {
			entries = CameraController.ResolutionsNamesList
					.toArray(new CharSequence[CameraController.ResolutionsNamesList.size()]);
			entryValues = CameraController.ResolutionsIdxesList
					.toArray(new CharSequence[CameraController.ResolutionsIdxesList.size()]);

			
			ListPreference lp = (ListPreference) prefActivity
					.findPreference("imageSizePrefCommonBack");
			ListPreference lp2 = (ListPreference) prefActivity
					.findPreference("imageSizePrefCommonFront");
			
			if(CameraController.CameraIndex == 0 && lp2 != null && lp != null)
			{
				prefActivity.getPreferenceScreen().removePreference(lp2);
				lp.setEntries(entries);
				lp.setEntryValues(entryValues);
			}
			else if(lp2 != null && lp != null)
			{
				prefActivity.getPreferenceScreen().removePreference(lp);
				lp2.setEntries(entries);
				lp2.setEntryValues(entryValues);
			}
			else
				return;

			// set currently selected image size
			int idx;
			for (idx = 0; idx < CameraController.ResolutionsIdxesList.size(); ++idx) {
				if (Integer.parseInt(CameraController.ResolutionsIdxesList.get(idx)) == CameraController.CapIdx) {
					break;
				}
			}
			if (idx < CameraController.ResolutionsIdxesList.size()) {
				if(CameraController.CameraIndex == 0)
					lp.setValueIndex(idx);
				else
					lp2.setValueIndex(idx);
			}
			if(CameraController.CameraIndex == 0)
			lp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				// @Override
				public boolean onPreferenceChange(Preference preference,
						Object newValue) {
					int value = Integer.parseInt(newValue.toString());
					CameraController.CapIdx = value;
					return true;
				}
			});
			else
				lp2.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					// @Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						int value = Integer.parseInt(newValue.toString());
						CameraController.CapIdx = value;
						return true;
					}
				});
				
		}
	}

	public void queueGLEvent(final Runnable runnable)
	{
		final GLSurfaceView surfaceView = glView;

		if (surfaceView != null && runnable != null) {
			surfaceView.queueEvent(runnable);
		}
	}

	
	@Override
	protected void onStart()
	{
		super.onStart();
		MainScreen.cameraController.onStart();
		MainScreen.guiManager.onStart();
		PluginManager.getInstance().onStart();
	}

	
	@Override
	protected void onStop()
	{
		super.onStop();
		mApplicationStarted = false;
		orientationMain = 0;
		orientationMainPrevious = 0;
		MainScreen.guiManager.onStop();
		PluginManager.getInstance().onStop();
		MainScreen.cameraController.onStop();
		
		if(isHALv3)
			stopImageReaders();
	}
	
	@TargetApi(19)
	private void stopImageReaders()
	{
		// IamgeReader should be closed
		if (mImageReaderPreviewYUV != null)
		{
			mImageReaderPreviewYUV.close();
			mImageReaderPreviewYUV = null;
		}
		if (mImageReaderYUV != null)
		{
			mImageReaderYUV.close();
			mImageReaderYUV = null;
		}
		if (mImageReaderJPEG != null)
		{
			mImageReaderJPEG.close();
			mImageReaderJPEG = null;
		}
	}

	
	@Override
	protected void onDestroy()
	{	
		super.onDestroy();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainScreen.mainContext);
		if(launchTorch && prefs.getString(sFlashModePref, "").contains(getResources().getString(R.string.flashTorchSystem)))
		{
			prefs.edit().putString(sFlashModePref, getResources().getString(R.string.flashAutoSystem)).commit();
		}
		MainScreen.guiManager.onDestroy();
		PluginManager.getInstance().onDestroy();
		MainScreen.cameraController.onDestroy();

		// <!-- -+-
//		/**** Billing *****/
//		destroyBillingHandler();
//		/**** Billing *****/
		//-+- -->
		
		glView = null;
	}

	
	@Override
	protected void onResume()
	{		
		super.onResume();
		
		if (!isCreating)
			onResumeMain(isHALv3);
	}
	
	public void onResumeMain(boolean isHALv3)
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(MainScreen.mainContext);
		CameraController.CameraIndex = prefs.getBoolean("useFrontCamera", false) == false ? 0
				: 1;
		ShutterPreference = prefs.getBoolean("shutterPrefCommon",
				false);
		ShotOnTapPreference = prefs.getBoolean("shotontapPrefCommon",
				false);
		ImageSizeIdxPreference = prefs.getString(CameraController.CameraIndex == 0 ?
				"imageSizePrefCommonBack" : "imageSizePrefCommonFront", "-1");
		Log.e("MainScreen", "ImageSizeIdxPreference = " + ImageSizeIdxPreference);
		// FullMediaRescan = prefs.getBoolean("mediaPref", true);
		SaveToPath = prefs.getString(SavePathPref, Environment
				.getExternalStorageDirectory().getAbsolutePath());
		SaveToPreference = prefs.getString("saveToPref", "0");
		SortByDataPreference = prefs.getBoolean("sortByDataPref",
				false);
		
		MaxScreenBrightnessPreference = prefs.getBoolean("maxScreenBrightnessPref", false);
		setScreenBrightness(MaxScreenBrightnessPreference);

		MainScreen.guiManager.onResume();
		PluginManager.getInstance().onResume();
		MainScreen.thiz.mPausing = false;
		
		MainScreen.thiz.findViewById(R.id.mainLayout2).setVisibility(View.VISIBLE);
		
		if(isHALv3)
		{
			cameraController.setupCamera(null);
		}
		else
		{
			if (surfaceCreated && (!CameraController.isCameraCreated())) {
				MainScreen.thiz.findViewById(R.id.mainLayout2)
						.setVisibility(View.VISIBLE);
				cameraController.setupCamera(surfaceHolder);
			}
		}

		if (glView != null)
		{
			glView.onResume();
			Log.e("GL", "glView onResume");
		}
		
		PluginManager.getInstance().onGUICreate();
		MainScreen.guiManager.onGUICreate();
		orientListener.enable();
		

		shutterPlayer = new SoundPlayer(this.getBaseContext(), getResources()
				.openRawResourceFd(R.raw.plugin_capture_tick));

		if (ScreenTimer != null) {
			if (isScreenTimerRunning)
				ScreenTimer.cancel();
			ScreenTimer.start();
			isScreenTimerRunning = true;
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		mApplicationStarted = false;
		CameraController.cameraConfigured = false;

		MainScreen.guiManager.onPause();
		PluginManager.getInstance().onPause(true);

		orientListener.disable();

		// initiate full media rescan
		// if (FramesShot && FullMediaRescan)
		// {
		// // using MediaScannerConnection.scanFile(this, paths, null, null);
		// instead
		// sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
		// Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
		// FramesShot = false;
		// }

		if (ShutterPreference) {
			AudioManager mgr = (AudioManager) MainScreen.thiz
					.getSystemService(MainScreen.mainContext.AUDIO_SERVICE);
			mgr.setStreamMute(AudioManager.STREAM_SYSTEM, false);
		}

		this.mPausing = true;

		if (glView != null) {
			glView.onPause();
		}

		if (ScreenTimer != null) {
			if (isScreenTimerRunning)
				ScreenTimer.cancel();
			isScreenTimerRunning = false;
		}

		cameraController.onPause();
		
		this.findViewById(R.id.mainLayout2).setVisibility(View.INVISIBLE);

		if (shutterPlayer != null) {
			shutterPlayer.release();
			shutterPlayer = null;
		}
	}

	public void PauseMain() {
		onPause();
	}

	public void StopMain() {
		onStop();
	}

	public void StartMain() {
		onStart();
	}

	public void ResumeMain() {
		onResume();
	}

	@Override
	public void surfaceChanged(final SurfaceHolder holder, final int format,
			final int width, final int height) {

		if (!isCreating)
		{
			if(!isHALv3)
			{
				onSurfaceChangedMain(holder, width, height);
//			new CountDownTimer(50, 50) 
//			{
//				public void onTick(long millisUntilFinished)
//				{
//				}
//				public void onFinish()
//				{
//					onSurfaceChangedMain(holder, width, height);
//				}
//			}.start();
			}
			else
			{
				onSurfaceChangedMain(holder, width, height);
			}
			
//			SharedPreferences prefs = PreferenceManager
//					.getDefaultSharedPreferences(MainScreen.mainContext);
//			CameraController.CameraIndex = prefs.getBoolean("useFrontCamera", false) == false ? 0
//					: 1;
//			ShutterPreference = prefs.getBoolean("shutterPrefCommon",
//					false);
//			ShotOnTapPreference = prefs.getBoolean("shotontapPrefCommon",
//					false);
//			ImageSizeIdxPreference = prefs.getString(CameraController.CameraIndex == 0 ?
//					"imageSizePrefCommonBack" : "imageSizePrefCommonFront", "-1");
//			// FullMediaRescan = prefs.getBoolean("mediaPref", true);
//
//			if (!MainScreen.thiz.mPausing && surfaceCreated
//					&& (CameraController.camera == null)) {
//				surfaceWidth = width;
//				surfaceHeight = height;
//				MainScreen.thiz.findViewById(R.id.mainLayout2)
//						.setVisibility(View.VISIBLE);
//				
//				H.sendEmptyMessage(PluginManager.MSG_SURFACE_READY);
////				setupCamera(holder);
////				PluginManager.getInstance().onGUICreate();
////				MainScreen.guiManager.onGUICreate();
//			}
		}
		else {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(MainScreen.mainContext);
			CameraController.CameraIndex = prefs.getBoolean("useFrontCamera", false) == false ? 0
					: 1;
			ShutterPreference = prefs.getBoolean("shutterPrefCommon", false);
			ShotOnTapPreference = prefs.getBoolean("shotontapPrefCommon",false);
			ImageSizeIdxPreference = prefs.getString(CameraController.CameraIndex == 0 ?
					"imageSizePrefCommonBack" : "imageSizePrefCommonFront",
					"-1");
			// FullMediaRescan = prefs.getBoolean("mediaPref", true);

			if (!MainScreen.thiz.mPausing && surfaceCreated) {
				surfaceWidth = width;
				surfaceHeight = height;
			}
		}
	}
	
	public void onSurfaceChangedMain(final SurfaceHolder holder, final int width, final int height)
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(MainScreen.mainContext);
		CameraController.CameraIndex = prefs.getBoolean("useFrontCamera", false) == false ? 0
				: 1;
		ShutterPreference = prefs.getBoolean("shutterPrefCommon",
				false);
		ShotOnTapPreference = prefs.getBoolean("shotontapPrefCommon",
				false);
		ImageSizeIdxPreference = prefs.getString(CameraController.CameraIndex == 0 ?
				"imageSizePrefCommonBack" : "imageSizePrefCommonFront", "-1");
		// FullMediaRescan = prefs.getBoolean("mediaPref", true);

		if (!MainScreen.thiz.mPausing && surfaceCreated
				&& (!CameraController.isCameraCreated())) {
			surfaceWidth = width;
			surfaceHeight = height;
			MainScreen.thiz.findViewById(R.id.mainLayout2)
					.setVisibility(View.VISIBLE);			
			
			if(isHALv3)
				H.sendEmptyMessage(PluginManager.MSG_SURFACE_READY);
			else
			{
				cameraController.setupCamera(holder);
				PluginManager.getInstance().onGUICreate();
				MainScreen.guiManager.onGUICreate();
			}
		}
	}	
	
	
	public void configureCamera()
	{
		Log.e("MainScreen", "configureCamera()");
		// prepare list of surfaces to be used in capture requests
		if(isHALv3)
			configureHALv3Camera();
		else
		{
			// ----- Select preview dimensions with ratio correspondent to full-size
			// image
			PluginManager.getInstance().SetCameraPreviewSize(CameraController.getInstance().getCameraParameters());
	
			Camera.Size sz = CameraController.getInstance().getCameraParameters().getPreviewSize();
			
			guiManager.setupViewfinderPreviewSize(cameraController.new Size(sz.width, sz.height));
			CameraController.getInstance().pviewBuffer = new byte[sz.width * sz.height
			                                                      * ImageFormat.getBitsPerPixel(CameraController.getInstance().getCameraParameters().getPreviewFormat()) / 8];
		}

		if (PluginManager.getInstance().isGLSurfaceNeeded()) {
			if (glView == null) {
				glView = new GLLayer(MainScreen.mainContext);// (GLLayer)findViewById(R.id.SurfaceView02);
				glView.setLayoutParams(new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				((RelativeLayout) findViewById(R.id.mainLayout2)).addView(
						glView, 1);
				Log.e("GL", "glView added");
				glView.setZOrderMediaOverlay(true);
				glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
			}
		} else {
			((RelativeLayout) findViewById(R.id.mainLayout2))
					.removeView(glView);
			glView = null;
		}

		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) preview
				.getLayoutParams();
		if (glView != null) {
			glView.setVisibility(View.VISIBLE);
			glView.setLayoutParams(lp);
		} else {
			if (glView != null)
				glView.setVisibility(View.GONE);
		}

////				pviewBuffer = new byte[previewSize.width
////						* previewSize.height
////						* ImageFormat.getBitsPerPixel(cameraParameters
////								.getPreviewFormat()) / 8];
//
////				camera.setErrorCallback(MainScreen.thiz);

		CameraController.supportedSceneModes = cameraController.getSupportedSceneModes();
		CameraController.supportedWBModes = cameraController.getSupportedWhiteBalance();
		CameraController.supportedFocusModes = cameraController.getSupportedFocusModes();
		CameraController.supportedFlashModes = cameraController.getSupportedFlashModes();
		CameraController.supportedISOModes = cameraController.getSupportedISO();
		
		CameraController.maxRegionsSupported = cameraController.getMaxNumFocusAreas();

		PluginManager.getInstance().SetCameraPictureSize();
		PluginManager.getInstance().SetupCameraParameters();
		//cp = cameraParameters;

		if(!isHALv3)
		{
			try {
				//Log.i("CameraTest", Build.MODEL);
				if (Build.MODEL.contains("Nexus 5"))
				{
					Camera.Parameters params = CameraController.getInstance().getCameraParameters();
					params.setPreviewFpsRange(7000, 30000);
					cameraController.setCameraParameters(params);
				}
				
				//Log.i("CameraTest", "fps ranges "+range.size()+" " + range.get(0)[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] + " " + range.get(0)[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
				//cameraParameters.setPreviewFpsRange(range.get(0)[Camera.Parameters.PREVIEW_FPS_MIN_INDEX], range.get(0)[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
				//cameraParameters.setPreviewFpsRange(7000, 30000);
				// an obsolete but much more reliable way of setting preview to a reasonable fps range
				// Nexus 5 is giving preview which is too dark without this
				//cameraParameters.setPreviewFrameRate(30);
			
				
			} catch (RuntimeException e) {
				Log.e("CameraTest", "MainScreen.setupCamera unable setParameters "
						+ e.getMessage());
			}

//				previewWidth = cameraParameters.getPreviewSize().width;
//				previewHeight = cameraParameters.getPreviewSize().height;
			previewWidth = CameraController.getInstance().getCameraParameters().getPreviewSize().width;
			previewHeight = CameraController.getInstance().getCameraParameters().getPreviewSize().height;
		}

				Util.initialize(mainContext);
				Util.initializeMeteringMatrix();
//				
				prepareMeteringAreas();

		guiManager.onCameraCreate();
		PluginManager.getInstance().onCameraParametersSetup();
		guiManager.onPluginsInitialized();

		// ----- Start preview and setup frame buffer if needed

		// ToDo: call camera release sequence from onPause somewhere ???
		new CountDownTimer(10, 10)
		{
			@Override
			public void onFinish() 
			{
				if(!isHALv3)
				{
					try // exceptions sometimes happen here when resuming after
						// processing
					{
						CameraController.startCameraPreview();
					} catch (RuntimeException e) {
						Toast.makeText(MainScreen.thiz, "Unable to start camera", Toast.LENGTH_LONG).show();
						return;
					}
	
					CameraController.getCamera().setPreviewCallbackWithBuffer(CameraController.getInstance());
					CameraController.getCamera().addCallbackBuffer(CameraController.getInstance().pviewBuffer);
				}

				PluginManager.getInstance().onCameraSetup();
				guiManager.onCameraSetup();
				MainScreen.mApplicationStarted = true;
				CameraController.cameraConfigured = true;
			}

			@Override
			public void onTick(long millisUntilFinished) {
			}
		}.start();		
	}
	
	
	@TargetApi(19)
	private void configureHALv3Camera()
	{
		List<Surface> sfl = new ArrayList<Surface>();
		
		sfl.add(mCameraSurface);				// surface for viewfinder preview
		sfl.add(mImageReaderPreviewYUV.getSurface());	// surface for preview yuv images
		sfl.add(mImageReaderYUV.getSurface());		// surface for yuv image capture
//		sfl.add(mImageReaderJPEG.getSurface());		// surface for jpeg image capture
		
		cameraController.setPreviewSurface(mImageReaderPreviewYUV.getSurface());

		guiManager.setupViewfinderPreviewSize(cameraController.new Size(1280, 720));
		//guiManager.setupViewfinderPreviewSize(cameraController.new Size(previewWidth, previewWidth));
		// configure camera with all the surfaces to be ever used
		try {
			HALv3.getInstance().camDevice.configureOutputs(sfl);
		} catch (Exception e)	{
			Log.e("MainScreen", "configureOutputs failed. CameraAccessException");
			e.printStackTrace();
		}		
		
		try
		{
			HALv3.getInstance().configurePreviewRequest();
			
		}
		catch (Exception e)
		{
			Log.d("MainScreen", "setting up preview failed");
			e.printStackTrace();
		}
		// ^^ HALv3 code -------------------------------------------------------------------		
	}
		
	
	private void prepareMeteringAreas()
	{
		Rect centerRect = Util.convertToDriverCoordinates(new Rect(previewWidth/4, previewHeight/4, previewWidth - previewWidth/4, previewHeight - previewHeight/4));
		Rect topLeftRect = Util.convertToDriverCoordinates(new Rect(0, 0, previewWidth/2, previewHeight/2));
		Rect topRightRect = Util.convertToDriverCoordinates(new Rect(previewWidth/2, 0, previewWidth, previewHeight/2));
		Rect bottomRightRect = Util.convertToDriverCoordinates(new Rect(previewWidth/2, previewHeight/2, previewWidth, previewHeight));
		Rect bottomLeftRect = Util.convertToDriverCoordinates(new Rect(0, previewHeight/2, previewWidth/2, previewHeight));
		Rect spotRect = Util.convertToDriverCoordinates(new Rect(previewWidth/2 - 10, previewHeight/2 - 10, previewWidth/2 + 10, previewHeight/2 + 10));
		
		mMeteringAreaMatrix5.clear();
		mMeteringAreaMatrix5.add(new Area(centerRect, 600));
		mMeteringAreaMatrix5.add(new Area(topLeftRect, 200));
		mMeteringAreaMatrix5.add(new Area(topRightRect, 200));
		mMeteringAreaMatrix5.add(new Area(bottomRightRect, 200));
		mMeteringAreaMatrix5.add(new Area(bottomLeftRect, 200));
		
		mMeteringAreaMatrix4.clear();
		mMeteringAreaMatrix4.add(new Area(topLeftRect, 250));
		mMeteringAreaMatrix4.add(new Area(topRightRect, 250));
		mMeteringAreaMatrix4.add(new Area(bottomRightRect, 250));
		mMeteringAreaMatrix4.add(new Area(bottomLeftRect, 250));
		
		mMeteringAreaMatrix1.clear();
		mMeteringAreaMatrix1.add(new Area(centerRect, 1000));
		
		mMeteringAreaCenter.clear();
		mMeteringAreaCenter.add(new Area(centerRect, 1000));
		
		mMeteringAreaSpot.clear();
		mMeteringAreaSpot.add(new Area(spotRect, 1000));
	}

	

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// ----- Find 'normal' orientation of the device

		Display display = ((WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int rotation = display.getRotation();
		if ((rotation == Surface.ROTATION_90)
				|| (rotation == Surface.ROTATION_270))
			landscapeIsNormal = true; // false; - if landscape view orientation
										// set for MainScreen
		else
			landscapeIsNormal = false;

		surfaceCreated = true;
		surfaceJustCreated = true;
		
		mCameraSurface = surfaceHolder.getSurface();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceCreated = false;
		surfaceJustCreated = false;
	}
	
	
	//SURFACES (preview, image readers)
	public Surface getCameraSurface()
	{
		return mCameraSurface;
	}
	
	@TargetApi(19)
	public Surface getPreviewYUVSurface()
	{
		return mImageReaderPreviewYUV.getSurface();
	}
	
	

	@TargetApi(14)
	public boolean isFaceDetectionAvailable(Camera.Parameters params) {
		if (params.getMaxNumDetectedFaces() > 0)
			return true;
		else
			return false;
	}

	public CameraController.Size getPreviewSize() {
		LayoutParams lp = preview.getLayoutParams();
		if (lp == null)
			return null;

		return cameraController.new Size(lp.width, lp.height);
	}

	public int getPreviewWidth() {
		LayoutParams lp = preview.getLayoutParams();
		if (lp == null)
			return 0;

		return lp.width;

	}

	public int getPreviewHeight() {
		LayoutParams lp = preview.getLayoutParams();
		if (lp == null)
			return 0;

		return lp.height;
	}
	
	public ImageReader getImageReaderPreviewYUV() {
		return mImageReaderPreviewYUV;
	}
	
	public ImageReader getImageReaderYUV() {
		return mImageReaderYUV;
	}
	
	public ImageReader getImageReaderJPEG() {
		return mImageReaderJPEG;
	}

	/*
	 * CAMERA PARAMETERS SECTION Supplementary methods for those plugins that
	 * need an icons of supported camera parameters (scene, iso, wb, flash,
	 * focus) Methods return id of drawable icon
	 */
	public int getSceneIcon(int sceneMode) {
		return guiManager.getSceneIcon(sceneMode);
	}

	public int getWBIcon(int wb) {
		return guiManager.getWBIcon(wb);
	}

	public int getFocusIcon(int focusMode) {
		return guiManager.getFocusIcon(focusMode);
	}

	public int getFlashIcon(int flashMode) {
		return guiManager.getFlashIcon(flashMode);
	}

	public int getISOIcon(String isoMode) {
		return guiManager.getISOIcon(isoMode);
	}	

	
	
	public void setCameraMeteringMode(int mode)
	{
//		if (camera != null)
//		{
//			Camera.Parameters params = cameraParameters;
//			if(meteringModeAuto.contains(mode))
//				setCameraMeteringAreas(null);
//			else if(meteringModeMatrix.contains(mode))
//			{				
//				int maxAreasCount = params.getMaxNumMeteringAreas();
//				if(maxAreasCount > 4)
//					setCameraMeteringAreas(mMeteringAreaMatrix5);
//				else if(maxAreasCount > 3)
//					setCameraMeteringAreas(mMeteringAreaMatrix4);
//				else if(maxAreasCount > 0)
//					setCameraMeteringAreas(mMeteringAreaMatrix1);
//				else
//					setCameraMeteringAreas(null);					
//			}
//			else if(meteringModeCenter.contains(mode))
//				setCameraMeteringAreas(mMeteringAreaCenter);
//			else if(meteringModeSpot.contains(mode))
//				setCameraMeteringAreas(mMeteringAreaSpot);
//			
//			currentMeteringMode = mode;
//		}
		
		if(meteringModeAuto == mode)
			cameraController.setCameraMeteringAreas(null);
		else if(meteringModeMatrix == mode)
		{				
			int maxAreasCount = CameraController.getInstance().getMaxNumMeteringAreas();
			if(maxAreasCount > 4)
				cameraController.setCameraMeteringAreas(mMeteringAreaMatrix5);
			else if(maxAreasCount > 3)
				cameraController.setCameraMeteringAreas(mMeteringAreaMatrix4);
			else if(maxAreasCount > 0)
				cameraController.setCameraMeteringAreas(mMeteringAreaMatrix1);
			else
				cameraController.setCameraMeteringAreas(null);					
		}
		else if(meteringModeCenter == mode)
			cameraController.setCameraMeteringAreas(mMeteringAreaCenter);
		else if(meteringModeSpot == mode)
			cameraController.setCameraMeteringAreas(mMeteringAreaSpot);
		
		currentMeteringMode = mode;
	}

	

	/*
	 * 
	 * CAMERA parameters access function ended
	 */

	// >>Description
	// section with user control procedures and main capture functions
	//
	// all events translated to PluginManager
	// Description<<

	
	
	public static void setAutoFocusLock(boolean locked)
	{
		mAFLocked = locked;
	}
	
	public static boolean getAutoFocusLock()
	{
		return mAFLocked;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!mApplicationStarted)
			return true;

		if (keyCode == KeyEvent.KEYCODE_MENU) {
			menuButtonPressed();
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_CAMERA
				|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			MainScreen.guiManager.onHardwareShutterButtonPressed();
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_FOCUS) {
			MainScreen.guiManager.onHardwareFocusButtonPressed();
			return true;
		}
		
		//check if volume button has some functions except Zoom-ing
		if ( keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP )
		{
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(MainScreen.mainContext);
			int buttonFunc = Integer.parseInt(prefs.getString("volumeButtonPrefCommon", "0"));
			if (buttonFunc == VOLUME_FUNC_SHUTTER)
			{
				MainScreen.guiManager.onHardwareFocusButtonPressed();
				MainScreen.guiManager.onHardwareShutterButtonPressed();
				return true;
			}
			else if (buttonFunc == VOLUME_FUNC_EXPO)
			{
				MainScreen.guiManager.onVolumeBtnExpo(keyCode);
				return true;
			}
			else if (buttonFunc == VOLUME_FUNC_NONE)
				return true;
		}
		
		
		if (PluginManager.getInstance().onKeyDown(true, keyCode, event))
			return true;
		if (guiManager.onKeyDown(true, keyCode, event))
			return true;

		// <!-- -+-
//		if (keyCode == KeyEvent.KEYCODE_BACK)
//    	{
//    		if (AppRater.showRateDialogIfNeeded(this))
//    		{
//    			return true;
//    		}
//    		if (AppWidgetNotifier.showNotifierDialogIfNeeded(this))
//    		{
//    			return true;
//    		}
//    	}
		//-+- -->
		
		if (super.onKeyDown(keyCode, event))
			return true;
		return false;
	}

	@Override
	public void onClick(View v) {
		if (mApplicationStarted)
			MainScreen.guiManager.onClick(v);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (mApplicationStarted)
			return MainScreen.guiManager.onTouch(view, event);
		return true;
	}

	public boolean onTouchSuper(View view, MotionEvent event) {
		return super.onTouchEvent(event);
	}

	public void onButtonClick(View v) {
		MainScreen.guiManager.onButtonClick(v);
	}
	
	@Override
	public void onShutter()
	{
		PluginManager.getInstance().onShutter();
	}

	

	// >>Description
	// message processor
	//
	// processing main events and calling active plugin procedures
	//
	// possible some additional plugin dependent events.
	//
	// Description<<
	@Override
	public boolean handleMessage(Message msg) {

		switch(msg.what)
		{
			case MSG_RETURN_CAPTURED:
				this.setResult(RESULT_OK);
				this.finish();
				break;		
			case PluginManager.MSG_CAMERA_OPENED:
			case PluginManager.MSG_SURFACE_READY:
	
					// if both surface is created and camera device is opened
					// - ready to set up preview and other things
					if (surfaceCreated && (HALv3.getInstance().camDevice != null))
					{
						configureCamera();
						PluginManager.getInstance().onGUICreate();
						MainScreen.guiManager.onGUICreate();
					}
					break;			
			default:
			PluginManager.getInstance().handleMessage(msg); break;
		}

		return true;
	}

	public void menuButtonPressed() {
		PluginManager.getInstance().menuButtonPressed();
	}

	public void disableCameraParameter(GUI.CameraParameter iParam,
			boolean bDisable, boolean bInitMenu) {
		guiManager.disableCameraParameter(iParam, bDisable, bInitMenu);
	}

	public void showOpenGLLayer() {
		if (glView != null && glView.getVisibility() == View.GONE) {
			glView.setVisibility(View.VISIBLE);
			glView.onResume();
			Log.e("GL", "glView show");
		}
	}

	public void hideOpenGLLayer() {
		if (glView != null && glView.getVisibility() == View.VISIBLE) {
			glView.setVisibility(View.GONE);
			glView.onPause();
			Log.e("GL", "glView hide");
		}
	}

	public void PlayShutter(int sound) {
		if (!MainScreen.ShutterPreference) {
			MediaPlayer mediaPlayer = MediaPlayer
					.create(MainScreen.thiz, sound);
			mediaPlayer.start();
		}
	}

	public void PlayShutter() {
		if (!MainScreen.ShutterPreference) {
			if (shutterPlayer != null)
				shutterPlayer.play();
		}
	}

	// set TRUE to mute and FALSE to unmute
	public void MuteShutter(boolean mute) {
		if (MainScreen.ShutterPreference) {
			AudioManager mgr = (AudioManager) MainScreen.thiz
					.getSystemService(MainScreen.mainContext.AUDIO_SERVICE);
			mgr.setStreamMute(AudioManager.STREAM_SYSTEM, mute);
		}
	}

	public static int getImageWidth() {
		return imageWidth;
	}

	public static void setImageWidth(int setImageWidth) {
		imageWidth = setImageWidth;
	}

	public static int getImageHeight() {
		return imageHeight;
	}

	public static void setImageHeight(int setImageHeight) {
		imageHeight = setImageHeight;
	}

	public static int getSaveImageWidth() {
		return saveImageWidth;
	}

	public static void setSaveImageWidth(int setSaveImageWidth) {
		saveImageWidth = setSaveImageWidth;
	}

	public static int getSaveImageHeight() {
		return saveImageHeight;
	}

	public static void setSaveImageHeight(int setSaveImageHeight) {
		saveImageHeight = setSaveImageHeight;
	}

	public static boolean getCameraMirrored() {
		return CameraController.CameraMirrored;
	}

	public static void setCameraMirrored(boolean setCameraMirrored) {
		CameraController.CameraMirrored = setCameraMirrored;
	}

	public static boolean getWantLandscapePhoto() {
		return wantLandscapePhoto;
	}

	public static void setWantLandscapePhoto(boolean setWantLandscapePhoto) {
		wantLandscapePhoto = setWantLandscapePhoto;
	}
	
	public void setScreenBrightness(boolean setMax)
	{
		//ContentResolver cResolver = getContentResolver();
		Window window = getWindow();
		
		WindowManager.LayoutParams layoutpars = window.getAttributes();
		
        //Set the brightness of this window	
		if(setMax)
			layoutpars.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
		else
			layoutpars.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;

        //Apply attribute changes to this window
        window.setAttributes(layoutpars);
	}

	/*******************************************************/
	/************************ Billing ************************/
// <!-- -+-
//	IabHelper mHelper;
//	
//	private boolean bOnSale = false;
//
//	private boolean unlockAllPurchased = false;
//	private boolean hdrPurchased = false;
//	private boolean panoramaPurchased = false;
//	private boolean objectRemovalBurstPurchased = false;
//	private boolean groupShotPurchased = false;
//
//	public boolean isUnlockedAll()
//	{
//		return unlockAllPurchased;
//	}
//	
//	private void createBillingHandler() 
//	{
//		try {
//			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainScreen.mainContext);
//			if ((isInstalled("com.almalence.hdr_plus")) || (isInstalled("com.almalence.pixfix")))
//			{
//				hdrPurchased = true;
//				Editor prefsEditor = prefs.edit();
//				prefsEditor.putBoolean("plugin_almalence_hdr", true);
//				prefsEditor.commit();
//			}
//			if (isInstalled("com.almalence.panorama.smoothpanorama"))
//			{
//				panoramaPurchased = true;
//				Editor prefsEditor = prefs.edit();
//				prefsEditor.putBoolean("plugin_almalence_panorama", true);
//				prefsEditor.commit();
//			}
//	
//			String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnztuXLNughHjGW55Zlgicr9r5bFP/K5DBc3jYhnOOo1GKX8M2grd7+SWeUHWwQk9lgQKat/ITESoNPE7ma0ZS1Qb/VfoY87uj9PhsRdkq3fg+31Q/tv5jUibSFrJqTf3Vmk1l/5K0ljnzX4bXI0p1gUoGd/DbQ0RJ3p4Dihl1p9pJWgfI9zUzYfvk2H+OQYe5GAKBYQuLORrVBbrF/iunmPkOFN8OcNjrTpLwWWAcxV5k0l5zFPrPVtkMZzKavTVWZhmzKNhCvs1d8NRwMM7XMejzDpI9A7T9egl6FAN4rRNWqlcZuGIMVizJJhvOfpCLtY971kQkYNXyilD40fefwIDAQAB";
//			// Create the helper, passing it our context and the public key to
//			// verify signatures with
//			Log.v("Main billing", "Creating IAB helper.");
//			mHelper = new IabHelper(this, base64EncodedPublicKey);
//	
//			mHelper.enableDebugLogging(true);
//	
//			Log.v("Main billing", "Starting setup.");
//			mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
//				public void onIabSetupFinished(IabResult result) 
//				{
//					try {
//						Log.v("Main billing", "Setup finished.");
//		
//						if (!result.isSuccess()) {
//							Log.v("Main billing", "Problem setting up in-app billing: "
//									+ result);
//							return;
//						}
//		
//						List<String> additionalSkuList = new ArrayList<String>();
//						additionalSkuList.add("plugin_almalence_hdr");
//						additionalSkuList.add("plugin_almalence_panorama");
//						additionalSkuList.add("unlock_all_forever");
//						additionalSkuList.add("plugin_almalence_moving_burst");
//						additionalSkuList.add("plugin_almalence_groupshot");
//						
//						//for sale
//						additionalSkuList.add("abc_sale_controller1");
//						additionalSkuList.add("abc_sale_controller2");
//		
//						Log.v("Main billing", "Setup successful. Querying inventory.");
//						mHelper.queryInventoryAsync(true, additionalSkuList,
//								mGotInventoryListener);
//					} catch (Exception e) {
//						e.printStackTrace();
//						Log.e("Main billing",
//								"onIabSetupFinished exception: " + e.getMessage());
//					}
//				}
//			});
//		} catch (Exception e) {
//			e.printStackTrace();
//			Log.e("Main billing",
//					"createBillingHandler exception: " + e.getMessage());
//		}
//	}
//
//	private void destroyBillingHandler() {
//		try {
//			if (mHelper != null)
//				mHelper.dispose();
//			mHelper = null;
//		} catch (Exception e) {
//			e.printStackTrace();
//			Log.e("Main billing",
//					"destroyBillingHandler exception: " + e.getMessage());
//		}
//	}
//
//	public String titleUnlockAll = "$6.95";
//	public String titleUnlockHDR = "$2.99";
//	public String titleUnlockPano = "$2.99";
//	public String titleUnlockMoving = "$2.99";
//	public String titleUnlockGroup = "$2.99";
//	
//	public String summaryUnlockAll = "";
//	public String summaryUnlockHDR = "";
//	public String summaryUnlockPano = "";
//	public String summaryUnlockMoving = "";
//	public String summaryUnlockGroup = "";
//	
//	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
//		public void onQueryInventoryFinished(IabResult result,
//				Inventory inventory) {
//			if (inventory == null)
//			{
//				Log.e("Main billing", "mGotInventoryListener inventory null ");
//				return;
//			}
//
//			SharedPreferences prefs = PreferenceManager
//					.getDefaultSharedPreferences(MainScreen.mainContext);
//			
//			if (inventory.hasPurchase("plugin_almalence_hdr")) {
//				hdrPurchased = true;
//				Editor prefsEditor = prefs.edit();
//				prefsEditor.putBoolean("plugin_almalence_hdr", true);
//				prefsEditor.commit();
//			}
//			if (inventory.hasPurchase("plugin_almalence_panorama")) {
//				panoramaPurchased = true;
//				Editor prefsEditor = prefs.edit();
//				prefsEditor.putBoolean("plugin_almalence_panorama", true);
//				prefsEditor.commit();
//			}
//			if (inventory.hasPurchase("unlock_all_forever")) {
//				unlockAllPurchased = true;
//				Editor prefsEditor = prefs.edit();
//				prefsEditor.putBoolean("unlock_all_forever", true);
//				prefsEditor.commit();
//			}
//			if (inventory.hasPurchase("plugin_almalence_moving_burst")) {
//				objectRemovalBurstPurchased = true;
//				Editor prefsEditor = prefs.edit();
//				prefsEditor.putBoolean("plugin_almalence_moving_burst", true);
//				prefsEditor.commit();
//			}
//			if (inventory.hasPurchase("plugin_almalence_groupshot")) {
//				groupShotPurchased = true;
//				Editor prefsEditor = prefs.edit();
//				prefsEditor.putBoolean("plugin_almalence_groupshot", true);
//				prefsEditor.commit();
//			}
//			
//			try{
//			//if (inventory.hasPurchase("abc_sale_controller1") && inventory.hasPurchase("abc_sale_controller2")) {
//				
//				String[] separated = inventory.getSkuDetails("abc_sale_controller1").getPrice().split(",");
//				int price1 = Integer.valueOf(separated[0]);
//				String[] separated2 = inventory.getSkuDetails("abc_sale_controller2").getPrice().split(",");
//				int price2 = Integer.valueOf(separated2[0]);
//				
//				if(price1<price2)
//					bOnSale = true;
//				else
//					bOnSale = false;
//				
//				Editor prefsEditor = prefs.edit();
//				prefsEditor.putBoolean("bOnSale", bOnSale);
//				prefsEditor.commit();
//				
//				Log.e("Main billing SALE", "Sale status is " + bOnSale);
//			}
//			catch(Exception e)
//			{
//				Log.e("Main billing SALE", "No sale data available");
//				bOnSale = false;
//			}
//			
//			titleUnlockAll = inventory.getSkuDetails("unlock_all_forever").getPrice();
//			titleUnlockHDR = inventory.getSkuDetails("plugin_almalence_hdr").getPrice();
//			titleUnlockPano = inventory.getSkuDetails("plugin_almalence_panorama").getPrice();
//			titleUnlockMoving = inventory.getSkuDetails("plugin_almalence_moving_burst").getPrice();
//			titleUnlockGroup = inventory.getSkuDetails("plugin_almalence_groupshot").getPrice();
//			
//			summaryUnlockAll = inventory.getSkuDetails("unlock_all_forever").getDescription();
//			summaryUnlockHDR = inventory.getSkuDetails("plugin_almalence_hdr").getDescription();
//			summaryUnlockPano = inventory.getSkuDetails("plugin_almalence_panorama").getDescription();
//			summaryUnlockMoving = inventory.getSkuDetails("plugin_almalence_moving_burst").getDescription();
//			summaryUnlockGroup = inventory.getSkuDetails("plugin_almalence_groupshot").getDescription();
//		}
//	};
//
//	private int HDR_REQUEST = 100;
//	private int PANORAMA_REQUEST = 101;
//	private int ALL_REQUEST = 102;
//	private int OBJECTREM_BURST_REQUEST = 103;
//	private int GROUPSHOT_REQUEST = 104;
//	Preference hdrPref, panoramaPref, allPref, objectremovalPref,
//			groupshotPref;
//
//	public void onBillingPreferenceCreate(final PreferenceFragment prefActivity) {
//		allPref = prefActivity.findPreference("purchaseAll");
//		
//		if (titleUnlockAll!=null && titleUnlockAll != "")
//		{
//			String title = getResources().getString(R.string.Pref_Upgrde_All_Preference_Title) + ": " + titleUnlockAll;
//			allPref.setTitle(title);
//		}
//		if (summaryUnlockAll!=null && summaryUnlockAll != "")
//		{
//			String summary = summaryUnlockAll + " " + getResources().getString(R.string.Pref_Upgrde_All_Preference_Summary);
//			allPref.setSummary(summary);
//		}
//		
//		allPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//			public boolean onPreferenceClick(Preference preference) {
//				// generate payload to identify user....????
//				String payload = "";
//				try {
//					mHelper.launchPurchaseFlow(MainScreen.thiz,
//							"unlock_all_forever", ALL_REQUEST,
//							mPreferencePurchaseFinishedListener, payload);
//				} catch (Exception e) {
//					e.printStackTrace();
//					Log.e("Main billing", "Purchase result " + e.getMessage());
//					Toast.makeText(MainScreen.thiz,
//							"Error during purchase " + e.getMessage(),
//							Toast.LENGTH_LONG).show();
//				}
//
//				prefActivity.getActivity().finish();
//				Preferences.closePrefs();
//				return true;
//			}
//		});
//		if (unlockAllPurchased) {
//			allPref.setEnabled(false);
//			allPref.setSummary(R.string.already_unlocked);
//
//			hdrPurchased = true;
//			panoramaPurchased = true;
//			objectRemovalBurstPurchased = true;
//			groupShotPurchased = true;
//		}
//
//		hdrPref = prefActivity.findPreference("hdrPurchase");
//		
//		if (titleUnlockHDR!=null && titleUnlockHDR != "")
//		{
//			String title = getResources().getString(R.string.Pref_Upgrde_HDR_Preference_Title) + ": " + titleUnlockHDR;
//			hdrPref.setTitle(title);
//		}
//		if (summaryUnlockHDR!=null && summaryUnlockHDR != "")
//		{
//			String summary = summaryUnlockHDR + " " + getResources().getString(R.string.Pref_Upgrde_HDR_Preference_Summary);
//			hdrPref.setSummary(summary);
//		}
//		
//		hdrPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//			public boolean onPreferenceClick(Preference preference) {
//				// generate payload to identify user....????
//				String payload = "";
//				try {
//					mHelper.launchPurchaseFlow(MainScreen.thiz,
//							"plugin_almalence_hdr", HDR_REQUEST,
//							mPreferencePurchaseFinishedListener, payload);
//				} catch (Exception e) {
//					e.printStackTrace();
//					Log.e("Main billing", "Purchase result " + e.getMessage());
//					Toast.makeText(MainScreen.thiz,
//							"Error during purchase " + e.getMessage(),
//							Toast.LENGTH_LONG).show();
//				}
//
//				prefActivity.getActivity().finish();
//				Preferences.closePrefs();
//				return true;
//			}
//		});
//
//		if (hdrPurchased) {
//			hdrPref.setEnabled(false);
//			hdrPref.setSummary(R.string.already_unlocked);
//		}
//
//		panoramaPref = prefActivity.findPreference("panoramaPurchase");
//		
//		if (titleUnlockPano!=null && titleUnlockPano != "")
//		{
//			String title = getResources().getString(R.string.Pref_Upgrde_Panorama_Preference_Title) + ": " + titleUnlockPano;
//			panoramaPref.setTitle(title);
//		}
//		if (summaryUnlockPano!=null && summaryUnlockPano != "")
//		{
//			String summary = summaryUnlockPano + " " + getResources().getString(R.string.Pref_Upgrde_Panorama_Preference_Summary) ;
//			panoramaPref.setSummary(summary);
//		}
//		
//		panoramaPref
//				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//					public boolean onPreferenceClick(Preference preference) {
//						// generate payload to identify user....????
//						String payload = "";
//						try {
//							mHelper.launchPurchaseFlow(MainScreen.thiz,
//									"plugin_almalence_panorama",
//									PANORAMA_REQUEST,
//									mPreferencePurchaseFinishedListener,
//									payload);
//						} catch (Exception e) {
//							e.printStackTrace();
//							Log.e("Main billing",
//									"Purchase result " + e.getMessage());
//							Toast.makeText(MainScreen.thiz,
//									"Error during purchase " + e.getMessage(),
//									Toast.LENGTH_LONG).show();
//						}
//
//						prefActivity.getActivity().finish();
//						Preferences.closePrefs();
//						return true;
//					}
//				});
//		if (panoramaPurchased) {
//			panoramaPref.setEnabled(false);
//			panoramaPref.setSummary(R.string.already_unlocked);
//		}
//
//		objectremovalPref = prefActivity.findPreference("movingPurchase");
//		
//		if (titleUnlockMoving!=null && titleUnlockMoving != "")
//		{
//			String title = getResources().getString(R.string.Pref_Upgrde_Moving_Preference_Title) + ": " + titleUnlockMoving;
//			objectremovalPref.setTitle(title);
//		}
//		if (summaryUnlockMoving!=null && summaryUnlockMoving != "")
//		{
//			String summary = summaryUnlockMoving + " " + getResources().getString(R.string.Pref_Upgrde_Moving_Preference_Summary);
//			objectremovalPref.setSummary(summary);
//		}
//		
//		objectremovalPref
//				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//					public boolean onPreferenceClick(Preference preference) {
//						// generate payload to identify user....????
//						String payload = "";
//						try {
//							mHelper.launchPurchaseFlow(MainScreen.thiz,
//									"plugin_almalence_moving_burst",
//									OBJECTREM_BURST_REQUEST,
//									mPreferencePurchaseFinishedListener,
//									payload);
//						} catch (Exception e) {
//							e.printStackTrace();
//							Log.e("Main billing",
//									"Purchase result " + e.getMessage());
//							Toast.makeText(MainScreen.thiz,
//									"Error during purchase " + e.getMessage(),
//									Toast.LENGTH_LONG).show();
//						}
//
//						prefActivity.getActivity().finish();
//						Preferences.closePrefs();
//						return true;
//					}
//				});
//		if (objectRemovalBurstPurchased) {
//			objectremovalPref.setEnabled(false);
//			objectremovalPref.setSummary(R.string.already_unlocked);
//		}
//
//		groupshotPref = prefActivity.findPreference("groupPurchase");
//		
//		if (titleUnlockGroup!=null && titleUnlockGroup != "")
//		{
//			String title = getResources().getString(R.string.Pref_Upgrde_Groupshot_Preference_Title) + ": " + titleUnlockGroup;
//			groupshotPref.setTitle(title);
//		}
//		if (summaryUnlockGroup!=null && summaryUnlockGroup != "")
//		{
//			String summary = summaryUnlockGroup + " " + getResources().getString(R.string.Pref_Upgrde_Groupshot_Preference_Summary);
//			groupshotPref.setSummary(summary);
//		}
//		
//		groupshotPref
//				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//					public boolean onPreferenceClick(Preference preference) {
//						String payload = "";
//						try {
//							mHelper.launchPurchaseFlow(MainScreen.thiz,
//									"plugin_almalence_groupshot",
//									GROUPSHOT_REQUEST,
//									mPreferencePurchaseFinishedListener,
//									payload);
//						} catch (Exception e) {
//							e.printStackTrace();
//							Log.e("Main billing",
//									"Purchase result " + e.getMessage());
//							Toast.makeText(MainScreen.thiz,
//									"Error during purchase " + e.getMessage(),
//									Toast.LENGTH_LONG).show();
//						}
//
//						prefActivity.getActivity().finish();
//						Preferences.closePrefs();
//						return true;
//					}
//				});
//		if (groupShotPurchased) {
//			groupshotPref.setEnabled(false);
//			groupshotPref.setSummary(R.string.already_unlocked);
//		}
//	}
//
//	public boolean showUnlock = false;
//	// Callback for when purchase from preferences is finished
//	IabHelper.OnIabPurchaseFinishedListener mPreferencePurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
//		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
//			Log.v("Main billing", "Purchase finished: " + result
//					+ ", purchase: " + purchase);
//			if (result.isFailure()) {
//				Log.v("Main billing", "Error purchasing: " + result);
//				new CountDownTimer(100, 100) {
//					public void onTick(long millisUntilFinished) {
//					}
//
//					public void onFinish() {
//						showUnlock = true;
//						Intent intent = new Intent(MainScreen.thiz,
//								Preferences.class);
//						startActivity(intent);
//					}
//				}.start();
//				return;
//			}
//
//			SharedPreferences prefs = PreferenceManager
//					.getDefaultSharedPreferences(MainScreen.mainContext);
//			
//			Log.v("Main billing", "Purchase successful.");
//
//			if (purchase.getSku().equals("plugin_almalence_hdr")) {
//				Log.v("Main billing", "Purchase HDR.");
//
//				hdrPurchased = true;
//				hdrPref.setEnabled(false);
//				hdrPref.setSummary(R.string.already_unlocked);
//				
//				Editor prefsEditor = prefs.edit();
//				prefsEditor.putBoolean("plugin_almalence_hdr", true);
//				prefsEditor.commit();
//			}
//			if (purchase.getSku().equals("plugin_almalence_panorama")) {
//				Log.v("Main billing", "Purchase Panorama.");
//
//				panoramaPurchased = true;
//				panoramaPref.setEnabled(false);
//				panoramaPref.setSummary(R.string.already_unlocked);
//				
//				Editor prefsEditor = prefs.edit();
//				prefsEditor.putBoolean("plugin_almalence_panorama", true);
//				prefsEditor.commit();
//			}
//			if (purchase.getSku().equals("unlock_all_forever")) {
//				Log.v("Main billing", "Purchase all.");
//
//				unlockAllPurchased = true;
//				allPref.setEnabled(false);
//				allPref.setSummary(R.string.already_unlocked);
//
//				groupshotPref.setEnabled(false);
//				groupshotPref.setSummary(R.string.already_unlocked);
//
//				objectremovalPref.setEnabled(false);
//				objectremovalPref.setSummary(R.string.already_unlocked);
//
//				panoramaPref.setEnabled(false);
//				panoramaPref.setSummary(R.string.already_unlocked);
//
//				hdrPref.setEnabled(false);
//				hdrPref.setSummary(R.string.already_unlocked);
//				
//				Editor prefsEditor = prefs.edit();
//				prefsEditor.putBoolean("unlock_all_forever", true);
//				prefsEditor.commit();
//			}
//			if (purchase.getSku().equals("plugin_almalence_moving_burst")) {
//				Log.v("Main billing", "Purchase object removal.");
//
//				objectRemovalBurstPurchased = true;
//				objectremovalPref.setEnabled(false);
//				objectremovalPref.setSummary(R.string.already_unlocked);
//				
//				Editor prefsEditor = prefs.edit();
//				prefsEditor.putBoolean("plugin_almalence_moving_burst", true);
//				prefsEditor.commit();
//			}
//			if (purchase.getSku().equals("plugin_almalence_groupshot")) {
//				Log.v("Main billing", "Purchase groupshot.");
//
//				groupShotPurchased = true;
//				groupshotPref.setEnabled(false);
//				groupshotPref.setSummary(R.string.already_unlocked);
//				
//				Editor prefsEditor = prefs.edit();
//				prefsEditor.putBoolean("plugin_almalence_groupshot", true);
//				prefsEditor.commit();
//			}
//		}
//	};
//
//	public void launchPurchase(String SKU, int requestID) {
//		String payload = "";
//		try {
//			mHelper.launchPurchaseFlow(MainScreen.thiz, SKU, requestID,
//					mPurchaseFinishedListener, payload);
//		} catch (Exception e) {
//			e.printStackTrace();
//			Log.e("Main billing", "Purchase result " + e.getMessage());
//			Toast.makeText(this, "Error during purchase " + e.getMessage(),
//					Toast.LENGTH_LONG).show();
//		}
//	}
//
//	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
//		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
//			Log.v("Main billing", "Purchase finished: " + result
//					+ ", purchase: " + purchase);
//			if (result.isFailure()) {
//				Log.v("Main billing", "Error purchasing: " + result);
//				return;
//			}
//
//			Log.v("Main billing", "Purchase successful.");
//
//			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainScreen.mainContext);
//			
//			if (purchase.getSku().equals("plugin_almalence_hdr")) {
//				Log.v("Main billing", "Purchase HDR.");
//				hdrPurchased = true;
//				
//				Editor prefsEditor = prefs.edit();
//				prefsEditor.putBoolean("plugin_almalence_hdr", true);
//				prefsEditor.commit();
//			}
//			if (purchase.getSku().equals("plugin_almalence_panorama")) {
//				Log.v("Main billing", "Purchase Panorama.");
//				panoramaPurchased = true;
//				
//				Editor prefsEditor = prefs.edit();
//				prefsEditor.putBoolean("plugin_almalence_panorama", true);
//				prefsEditor.commit();
//			}
//			if (purchase.getSku().equals("unlock_all_forever")) {
//				Log.v("Main billing", "Purchase unlock_all_forever.");
//				unlockAllPurchased = true;
//				
//				Editor prefsEditor = prefs.edit();
//				prefsEditor.putBoolean("unlock_all_forever", true);
//				prefsEditor.commit();
//			}
//			if (purchase.getSku().equals("plugin_almalence_moving_burst")) {
//				Log.v("Main billing", "Purchase plugin_almalence_moving_burst.");
//				objectRemovalBurstPurchased = true;
//				
//				Editor prefsEditor = prefs.edit();
//				prefsEditor.putBoolean("plugin_almalence_moving_burst", true);
//				prefsEditor.commit();
//			}
//			if (purchase.getSku().equals("plugin_almalence_groupshot")) {
//				Log.v("Main billing", "Purchase plugin_almalence_groupshot.");
//				groupShotPurchased = true;
//				
//				Editor prefsEditor = prefs.edit();
//				prefsEditor.putBoolean("plugin_almalence_groupshot", true);
//				prefsEditor.commit();
//			}
//
//			showUnlock = true;
//			Intent intent = new Intent(MainScreen.thiz, Preferences.class);
//			startActivity(intent);
//		}
//	};
//
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		Log.v("Main billing", "onActivityResult(" + requestCode + ","
//				+ resultCode + "," + data);
//
//		// Pass on the activity result to the helper for handling
//		if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
//			// not handled, so handle it ourselves (here's where you'd
//			// perform any handling of activity results not related to in-app
//			// billing...
//			super.onActivityResult(requestCode, resultCode, data);
//		} else {
//			Log.v("Main billing", "onActivityResult handled by IABUtil.");
//		}
//	}
//
//	// next methods used to store number of free launches.
//	// using files to store this info
//
//	// returns number of launches left
//	public int getLeftLaunches(String modeID) {
//		String dirPath = getFilesDir().getAbsolutePath() + File.separator
//				+ modeID;
//		File projDir = new File(dirPath);
//		if (!projDir.exists()) {
//			projDir.mkdirs();
//			WriteLaunches(projDir, 30);
//		}
//		int left = ReadLaunches(projDir);
//		return left;
//	}
//
//	// decrements number of launches left
//	public void decrementLeftLaunches(String modeID) {
//		String dirPath = getFilesDir().getAbsolutePath() + File.separator
//				+ modeID;
//		File projDir = new File(dirPath);
//		if (!projDir.exists()) {
//			projDir.mkdirs();
//			WriteLaunches(projDir, 30);
//		}
//
//		int left = ReadLaunches(projDir);
//		if (left > 0)
//			WriteLaunches(projDir, left - 1);
//	}
//
//	// writes number of launches left into memory
//	private void WriteLaunches(File projDir, int left) {
//		FileOutputStream fos = null;
//		try {
//			fos = new FileOutputStream(projDir + "/left");
//			fos.write(left);
//			fos.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	// reads number of launches left from memory
//	private int ReadLaunches(File projDir) {
//		int left = 0;
//		FileInputStream fis = null;
//		try {
//			fis = new FileInputStream(projDir + "/left");
//			left = fis.read();
//			fis.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return left;
//	}
//
//	public boolean checkLaunches(Mode mode) {
//		// if mode free
//		if (mode.SKU == null)
//			return true;
//		if (mode.SKU.isEmpty())
//			return true;
//
//		// if all unlocked
//		if (unlockAllPurchased == true)
//			return true;
//
//		// if current mode unlocked
//		if (mode.SKU.equals("plugin_almalence_hdr")) {
//			if (hdrPurchased == true)
//				return true;
//		} else if (mode.SKU.equals("plugin_almalence_panorama_augmented")) {
//			if (panoramaPurchased == true)
//				return true;
//		} else if (mode.SKU.equals("plugin_almalence_moving_burst")) {
//			if (objectRemovalBurstPurchased == true)
//				return true;
//		} else if (mode.SKU.equals("plugin_almalence_groupshot")) {
//			if (groupShotPurchased == true)
//				return true;
//		}
//
//		// if (!mode.purchased)
//		{
//			int launchesLeft = MainScreen.thiz.getLeftLaunches(mode.modeID);
//			if (0 == launchesLeft)// no more launches left
//			{
//				// show appstore for this mode
//				launchPurchase(mode.SKU, 100);
//				return false;
//			} else if ((10 == launchesLeft) || (20 == launchesLeft)
//					|| (5 >= launchesLeft)) {
//				// show appstore button and say that it cost money
//				int id = MainScreen.thiz.getResources().getIdentifier(
//						mode.modeName, "string",
//						MainScreen.thiz.getPackageName());
//				String modename = MainScreen.thiz.getResources().getString(id);
//
//				String left = String.format(getResources().getString(R.string.Pref_Billing_Left),
//						modename, launchesLeft);
//				Toast toast = Toast.makeText(this, left, Toast.LENGTH_LONG);
//				toast.setGravity(Gravity.CENTER, 0, 0);
//				toast.show();
//			}
//		}
//		return true;
//	}
//
//	private boolean isInstalled(String packageName) {
//		PackageManager pm = getPackageManager();
//		boolean installed = false;
//		try {
//			pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
//			installed = true;
//		} catch (PackageManager.NameNotFoundException e) {
//			installed = false;
//		}
//		return installed;
//	}
	
// -+- -->
	
	/************************ Billing ************************/
	/*******************************************************/
	
// <!-- -+-
	
//	//Application rater code
//	public static void CallStoreFree(Activity act)
//    {
//    	try
//    	{
//        	Intent intent = new Intent(Intent.ACTION_VIEW);
//       		intent.setData(Uri.parse("market://details?id=com.almalence.opencam"));
//	        act.startActivity(intent);
//    	}
//    	catch(ActivityNotFoundException e)
//    	{
//    		return;
//    	}
//    }
// -+- -->
	
	//widget ad code
	public static void CallStoreWidgetInstall(Activity act)
    {
    	try
    	{
        	Intent intent = new Intent(Intent.ACTION_VIEW);
       		intent.setData(Uri.parse("market://details?id=com.almalence.opencamwidget"));
	        act.startActivity(intent);
    	}
    	catch(ActivityNotFoundException e)
    	{
    		return;
    	}
    }
	
	private void ResetOrSaveSettings()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainScreen.mainContext);
		Editor prefsEditor = prefs.edit();
		boolean isSaving = prefs.getBoolean("SaveConfiguration_Mode", true);
		if (false == isSaving)
		{
			prefsEditor.putString("defaultModeName", "single");
			prefsEditor.commit();
		}
		
		isSaving = prefs.getBoolean("SaveConfiguration_ImageSize", true);
		if (false == isSaving)
		{			
			//general settings - image size
			prefsEditor.putString("imageSizePrefCommonBack", "-1");
			prefsEditor.putString("imageSizePrefCommonFront", "-1");
			//night hi sped image size
			prefsEditor.putString("imageSizePrefNightBack", "-1");
			prefsEditor.putString("pref_plugin_capture_panoramaaugmented_imageheight", "0");
			prefsEditor.commit();
		}
		
		isSaving = prefs.getBoolean("SaveConfiguration_SceneMode", true);
		if (false == isSaving)
		{			
			prefsEditor.putInt(sSceneModePref, sDefaultValue);
			prefsEditor.commit();
		}
		
		isSaving = prefs.getBoolean("SaveConfiguration_FocusMode", true);
		if (false == isSaving)
		{			
			prefsEditor.putInt("sRearFocusModePref", sDefaultFocusValue);
			prefsEditor.putInt(sFrontFocusModePref, sDefaultFocusValue);
			prefsEditor.commit();
		}
		
		isSaving = prefs.getBoolean("SaveConfiguration_WBMode", true);
		if (false == isSaving)
		{			
			prefsEditor.putInt(sWBModePref, sDefaultValue);
			prefsEditor.commit();
		}
		
		isSaving = prefs.getBoolean("SaveConfiguration_ISOMode", true);
		if (false == isSaving)
		{			
			prefsEditor.putInt(sISOPref, sDefaultValue);
			prefsEditor.commit();
		}
		
		isSaving = prefs.getBoolean("SaveConfiguration_FlashMode", true);
		if (false == isSaving)
		{			
			prefsEditor.putInt(sFlashModePref, sDefaultValue);
			prefsEditor.commit();
		}
		
		isSaving = prefs.getBoolean("SaveConfiguration_FrontRearCamera", true);
		if (false == isSaving)
		{			
			prefsEditor.putBoolean("useFrontCamera", false);
			prefsEditor.commit();
		}
	}
}
