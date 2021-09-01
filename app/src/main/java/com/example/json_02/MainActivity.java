package com.example.json_02;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static android.content.ContentValues.TAG;

public class MainActivity<Main> extends AppCompatActivity {

    private static final int REQUEST_WRITE_PERMISSION = 786;

    private TextView mTextViewResult;
    private RequestQueue mQueue;
    private static int newVersionCode = 2;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mTextViewResult = findViewById(R.id.text_view_result);
        Button buttonParse = findViewById(R.id.button_parse);
        Button install = findViewById(R.id.install);

        mQueue = Volley.newRequestQueue(this);


        buttonParse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                jsonParse();
//                UpdateApp();
                getAppVersion();
                Intent myIntent = new Intent(context, ShowNote.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                myIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(myIntent);
            }
        });

        install.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_install = new Intent(Intent.ACTION_VIEW);
//                intent_install.setDataAndType(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+"Json_02.apk")), "application/vnd.android.package-archive");
                intent_install.setDataAndType(FileProvider.getUriForFile(context,
                                                            BuildConfig.APPLICATION_ID + ".provider",new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+"rn-update-apk-example-3.0.1.apk")), "application/vnd.android.package-archive");Log.d("phone path","/storage/emulated/0/Download/rn-update-apk-example-3.0.1");
                startActivity(intent_install);

                Toast.makeText(getApplicationContext(), "App Installing", Toast.LENGTH_LONG).show();
            }
        });
        getAppVersion();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
    }

    private boolean canReadWriteExternal() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    public int getAppVersion() {
        int version = 0;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionCode;
            compareVersionCodes(version, newVersionCode);
        } catch (Exception e) {
            Log.e(TAG, "getAppVersion : Application Package is not found.: " + e.toString());
        }
        Log.d("sudks", "Version code : " + version);
        Toast.makeText(context, "Version code : " + version, Toast.LENGTH_SHORT).show();
        return version;
    }


    private void jsonParse() {
//        String url = "{\n" +
//                "  \"versionName\":\"999.0.0\",\n" +
//                "  \"versionCode\": \"998\",\n" +
//                "  \"apkUrl\":\"https://mbhospital.live247.ai/mobileApk/rn-update-apk-example-3.0.1.apk\",\n" +
//                "  \"forceUpdate\": false\n" +
//                "}";

        String url = "https://mbhospitals.live247.ai/liveapi/gateway/gateway_keepalive";


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {//is called if request is sucessful
                        try {
                            Log.d("sudks", "onResponse try called");

                            String versionCode = response.getString("versionCode");
                            String versionName = response.getString("versionName");
                            String title = response.getString("title");
                            String url = response.getString("apkUrl");
                            String forceUpdate = response.getString("forceUpdate");

                            mTextViewResult.setText("versionCode");

                            Log.d("sudks", versionCode + ", " + title);
//                            mTextViewResult.append(String.valueOf(version) + ", "+name + ", "+title);
                            Toast.makeText(MainActivity.this, versionName + ", " + versionCode + ", " + title, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Log.d("sudks", "onResponse Exception called");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {//is called if request is not sucessful
                error.printStackTrace();
            }
        });

        mQueue.add(request);
    }

    public int compareVersionCodes(int oldVersionCode, int newVersionCode) {
        int res = 0;
        // To avoid IndexOutOfBounds
        if (oldVersionCode < newVersionCode) {
            res = -1;
            Toast.makeText(this, "need to update is : " + (newVersionCode - oldVersionCode), Toast.LENGTH_SHORT).show();
            Log.d("sudks", "need to update is :" + (oldVersionCode - newVersionCode));
        } else if (oldVersionCode == newVersionCode) {// If versions are the same so far
            res = 1;
        }
        return res;
    }


    public void UpdateApp() {
//        //get destination to update file and set Uri
//        //TODO: First I wanted to store my update .apk file on internal storage for my app but apparently android does not allow you to open and install
//        //aplication with existing package from there. So for me, alternative solution is Download directory in external storage. If there is better
//        //solution, please inform us in comment
//        String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
//        String fileName = "AppName.apk";
//        destination += fileName;
//        final Uri uri = Uri.parse("file://" + destination);
//
//        //Delete update file if exists
//        File file = new File(destination);
//        if (file.exists())
//            //file.delete() - test this, I think sometimes it doesnt work
//            file.delete();
//
//        //get url of app on server
//        String url =
//
//        //set downloadmanager
//        DownloadManager.Request request= new DownloadManager.Request(Uri.parse(url));
//        request.setDescription(MainActivity.this.getString(R.string.notification_description));
//        request.setTitle(MainActivity.this.getString(R.string.app_name));
//
//        //set destination
//        request.setDestinationUri(uri);
//
//        // get download service and enqueue file
//        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//        final long downloadId = manager.enqueue(request);
//
//        //set BroadcastReceiver to install app when .apk is downloaded
//        BroadcastReceiver onComplete = new BroadcastReceiver() {
//            public void onReceive(Context ctxt, Intent intent) {
//                Intent install = new Intent(Intent.ACTION_VIEW);
//                install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                install.setDataAndType(uri,
//                        manager.getMimeTypeForDownloadedFile(downloadId));
//                startActivity(install);
//
//                unregisterReceiver(this);
//                finish();
//            }
//        };
//        //register receiver for when .apk download is compete
//        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


//        -------------------------------------------------------------------------------------------------------

        //get destination to update file and set Uri
        //TODO: First I wanted to store my update .apk file on internal storage for my app but apparently android does not allow you to open and install
        //aplication with existing package from there. So for me, alternative solution is Download directory in external storage. If there is better
        //solution, please inform us in comment
        String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
        String fileName = String.valueOf(R.string.app_name);
        destination += fileName;
        final Uri uri = Uri.parse("file://" + destination);

        //Delete update file if exists
        File file = new File(destination);
        if (file.exists())
            //file.delete() - test this, I think sometimes it doesnt work
            file.delete();

        //get url of app on server
        String url = "https://mbhospital.live247.ai/mobileApk/rn-update-apk-example-3.0.1.apk";

        //set downloadmanager
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(MainActivity.this.getString(R.string.notification_description));
        request.setTitle(MainActivity.this.getString(R.string.app_name));

        //set destination
        request.setDestinationUri(uri);

        // get download service and enqueue file
        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);

        //set BroadcastReceiver to install app when .apk is downloaded
        Log.d("sudks", "About to enter the broadcast");
        String finalDestination = destination;
        String finalDestination1 = destination;
        BroadcastReceiver onComplete = new BroadcastReceiver() {

            public void onReceive(Context ctxt, Intent intent) {
                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                install.setDataAndType(uri,
                        manager.getMimeTypeForDownloadedFile(downloadId));
                startActivity(install);
//--------------------------------------------------------------------------------------
//                intent = new Intent(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.fromFile(new File(finalDestination)), "application/vnd.android.package-archive");
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//--------------------------------------------------------------------------------------
//                File toInstall = new File("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/", R.string.app_name + ".apk");
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//
//                    Uri apkUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", toInstall);
//                    intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
//                    intent.setData(apkUri);
//                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                } else {
//                    Uri apkUri = Uri.fromFile(toInstall);
//                    intent = new Intent(Intent.ACTION_VIEW);
//                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                }
//                MainActivity.this.startActivity(intent);
//--------------------------------------------------------------------------------------

//                Intent install = new Intent(Intent.ACTION_VIEW);
//                install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//
//// New Approach
//                Uri apkURI = FileProvider.getUriForFile(
//                        context,
//                        context.getApplicationContext()
//                                .getPackageName() + ".provider", file);
//                install.setDataAndType(apkURI,"application/vnd.android.package-archive");
//                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//// End New Approach
//                context.startActivity(install);
//                installApk(finalDestination1,BuildConfig.APPLICATION_ID + ".provider");
  //--------------------------------------------------------------------------------------
                Log.d("sudks", " download is compete -ON recive");
                Toast.makeText(MainActivity.this, "download is compete -ON recive", Toast.LENGTH_SHORT).show();
                unregisterReceiver(this);
                finish();


            }
        };
        //register receiver for when .apk download is compete
        Log.d("sudks", "register receiver for when .apk download is compete");
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }


/*
need to call in mainfun for update app class
atualizaApp = new UpdateApp();
        atualizaApp.setContext(getApplicationContext());
        atualizaApp.execute("http://serverurl/appfile.apk");
//Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
*/


    public void installApk(String destination, String fileProviderAuthority) {

        Log.d("sudks","entered install apk");
        File file = new File(destination);
        if (!file.exists()) {
            Log.e("RNUpdateAPK", "installApk: file doe snot exist '" + destination + "'");
            // FIXME this should take a promise and fail it
            return;
        }

        if (Build.VERSION.SDK_INT >= 24) {
            // API24 and up has a package installer that can handle FileProvider content:// URIs
            Uri contentUri;
            try {
//                Uri apkUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", toInstall);
                contentUri = FileProvider.getUriForFile(getApplicationContext(), fileProviderAuthority, file);
            } catch (Exception e) {
                // FIXME should be a Promise.reject really
                Log.e("RNUpdateAPK", "installApk exception with authority name '" + fileProviderAuthority + "'", e);
                throw e;
            }
            Intent installApp = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            installApp.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            installApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            installApp.setData(contentUri);
            installApp.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.getApplicationInfo().packageName);
            context.startActivity(installApp);
        } else {
            // Old APIs do not handle content:// URIs, so use an old file:// style
            String cmd = "chmod 777 " + file;
            try {
                Runtime.getRuntime().exec(cmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.parse("file://" + file), "application/vnd.android.package-archive");
            context.startActivity(intent);
        }
    }
}

