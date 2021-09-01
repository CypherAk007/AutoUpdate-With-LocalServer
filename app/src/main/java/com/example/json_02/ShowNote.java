package com.example.json_02;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.security.Provider;

public class ShowNote extends Activity {
    private BroadcastReceiver receiver;
    private long enqueue;
    private DownloadManager dm;
    boolean isDeleted;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Update");
        builder.setIcon(R.drawable.ic_launcher_background);
        builder.setMessage("Latest Version is Available. Click on OK to update");
        builder.getContext().setTheme(R.style.AppTheme_NoActionBar);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "App Downloading...Please Wait", Toast.LENGTH_LONG).show();
                dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+"rn-update-apk-example-3.0.1.apk");
                if(file.exists()){

                    isDeleted = file.delete();
                    deleteAndInstall();
//                    firstTimeInstall();
                }
                else{
                    firstTimeInstall();
                }
            }
        });
        builder.setNegativeButton("Remind Me Later",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ShowNote.this.finish();
            }
        });
        builder.show();
    }

    private void firstTimeInstall() {
        Log.d("May be 1st Update:","OR deleted from folder" );
        downloadAndInstall();
    }

    private void deleteAndInstall() {
        if(isDeleted){
            Log.d("Deleted Existance file:", String.valueOf(isDeleted));
            downloadAndInstall();

        }else {
            Log.d("NOT DELETED:", String.valueOf(isDeleted));
            Toast.makeText(getApplicationContext(), "Error in Updating...Please try Later", Toast.LENGTH_LONG).show();
        }
    }

    private void downloadAndInstall() {
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse("https://mbhospital.live247.ai/mobileApk/rn-update-apk-example-3.0.1.apk"));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "rn-update-apk-example-3.0.1.apk");

        enqueue = dm.enqueue(request);


        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    Toast.makeText(getApplicationContext(), "Download Completed", Toast.LENGTH_LONG).show();

                    long downloadId = intent.getLongExtra(
                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueue);
                    Cursor c = dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

                            Log.d("ainfo", uriString);

                            if(downloadId == c.getInt(0)) {
                                Log.d("DOWNLOAD PATH:", c.getString(c.getColumnIndex("local_uri")));


                                Log.d("isRooted:",String.valueOf(isRooted()));
                                if(isRooted()==false){
                                    Log.d("sudks","entered the not rooted");

//                                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+"rn-update-apk-example-3.0.1.apk");
                                    File file = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+"rn-update-apk-example-3.0.1.apk");
//                                    Log.d("sudks", "installApk: file doe snot exist '" + file + "'");
                                    if (!file.exists()) {
                                        Log.e("sudks", "installApk: file does not exist '" + file + "'");
                                        // FIXME this should take a promise and fail it
                                        return;
                                    }
                                    if (Build.VERSION.SDK_INT >= 24) {//There was a problem while parsing the package.

                                        Log.d("sudks","entered Build.VERSION.SDK_INT >= 24");
                                        // API24 and up has a package installer that can handle FileProvider content:// URIs
                                        Uri contentUri;
                                        try {
//                                            contentUri = FileProvider.getUriForFile(getApplicationContext(), file.toURI().getAuthority(), file);//Authority error?
                                            contentUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);//?
                                            Log.d("sudks", "contentUri '" + contentUri + "'");
//                                            Log.d("sudks", "onProvideReferrer().getAuthority()'" +onProvideReferrer().getAuthority() + "'");
//                                            Log.d("sudks", "onProvideReferrer().getAuthority()'" + file.toURI().getAuthority() + "'");
                                            Log.d("sudks", "installApk: file does exist '" + file + "'");
                                        } catch (Exception e) {
                                            // FIXME should be a Promise.reject really
                                            Log.e("sudks", "installApk exception with authority name '" +  BuildConfig.APPLICATION_ID + "'", e);
                                            throw e;
                                        }
                                        Intent installApp = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                                        installApp.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        installApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        installApp.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        installApp.setData(contentUri);
                                        Log.d("sudks", "Intent contentUri '" + contentUri + "'");
                                        installApp.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, BuildConfig.APPLICATION_ID);
                                        Log.d("sudks", "context.getApplicationInfo().packageName '" + context.getApplicationInfo().packageName + "'");
                                        Log.d("sudks", "context.getApplicationInfo().packageName '" + BuildConfig.APPLICATION_ID +"'");

                                        context.startActivity(installApp);
                                    } else {
                                        // Old APIs do not handle content:// URIs, so use an old file:// style
                                        Log.d("sudks", "rooted");
                                        String cmd = "chmod 777 " + file;
                                        try {
                                            Runtime.getRuntime().exec(cmd);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        intent = new Intent(Intent.ACTION_VIEW);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.setDataAndType(Uri.parse("file://" +Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+"rn-update-apk-example-3.0.1.apk" ), "application/vnd.android.package-archive");
                                        context.startActivity(intent);
                                    }

//                                    //if your device is not rooted
//                                    Intent intent_install = new Intent(Intent.ACTION_VIEW);

//                                    1st
//                                    intent_install.setDataAndType(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+"rn-update-apk-example-3.0.1.apk")), "application/vnd.android.package-archive");
//
////                                    2nd(Parsing error)
//                                    intent_install.setDataAndType(FileProvider.getUriForFile(context,
//                                            BuildConfig.APPLICATION_ID + ".provider",new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+"rn-update-apk-example-3.0.1.apk")), "application/vnd.android.package-archive");
//                                    Log.d("phone path","/storage/emulated/0/Download/rn-update-apk-example-3.0.1.apk");
//                                    startActivity(intent_install);
//                                    Toast.makeText(getApplicationContext(), "App Installing", Toast.LENGTH_LONG).show();
                                }
                                    else{
                                    //if your device is rooted then you can install or update app in background directly
                                    Log.d("sudks:", "downloadId != c.getInt(0)");
                                    Toast.makeText(getApplicationContext(), "App Installing...Please Wait", Toast.LENGTH_LONG).show();
                                    File file = new File("/storage/emulated/0/Download/rn-update-apk-example-3.0.1.apk");
                                    Log.d("sudks:", "/storage/emulated/0/Download/rn-update-apk-example-3.0.1.apk"+"  in downloadId != c.getInt(0) ");
                                    if(file.exists()){
                                        try {
                                            String command;
                                            Log.d("sudks:","/mnt/sdcard/Download/rn-update-apk-example-3.0.1.apk");

                                            command = "pm install -r " + "/mnt/sdcard/Download/rn-update-apk-example-3.0.1.apk";
                                            Log.d("COMMAND:",command);
                                            Process proc = Runtime.getRuntime().exec(new String[] { "su", "-c", command });
                                            proc.waitFor();
                                            Toast.makeText(getApplicationContext(), "App Installed Successfully", Toast.LENGTH_LONG).show();

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    c.close();
                    unregisterReceiver(this);
                    finish();

                }
            }
        };

        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        Log.d("sudks", "installApk: file doe snot exist '"  + "'");
    }

    private static boolean isRooted() {
        return findBinary("su");
    }
    public static boolean findBinary(String binaryName) {
        boolean found = false;
        if (!found) {
            String[] places = {"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/","/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};
            for (String where : places) {
                if ( new File( where + binaryName ).exists() ) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
//    public  void deletePackage(){Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts("package",
//            getPackageManager().getPackageArchiveInfo(FileProvider.getUriForFile(,
//                                            BuildConfig.APPLICATION_ID + ".provider",new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+"rn-update-apk-example-3.0.1.apk")), 0).packageName,null));
//        startActivity(intent);}

}

