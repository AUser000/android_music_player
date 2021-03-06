package lk.dhanuhmd.letsworkwithaudio;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import lk.dhanuhmd.letsworkwithaudio.model.Song;

public class MainActivity extends AppCompatActivity {

    private Button pauseBtn,stopBtn ;
    ImageButton playButton, forwordButton, backwordButton;
    MediaPlayer player;
    ListView listView;
    ArrayList<Song> songsList;
    ArrayList<String> arrayNameList;
    private boolean permissionGranted;
    private static final int REQUEST_PERMISSION_WRITE = 101;///
    SwipeRefreshLayout swipeLayout;
    public static final String ENTRY = "entry";
    Intent serviceIntent;
    private boolean isBound = false;
    MyService myService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songsList = new ArrayList<>();
        arrayNameList = new ArrayList<>();
        listView = (ListView) findViewById(R.id.listView);
        playButton = (ImageButton) findViewById(R.id.plyBtn);
        //forwordButton = (ImageButton) findViewById(R.id.fowordBtn);


        if (android.os.Build.VERSION.SDK_INT > 23) {
            if(!permissionGranted) {
                checkPermission();
            }
        }


        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.ARTIST + " ASC";
        Cursor cur = contentResolver.query(uri, null, selection, null, sortOrder);
        int count = 0;
        if(cur != null) {
            count = cur.getCount();
            if(count > 0) {
                while(cur.moveToNext()) {
                    String data = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
                    arrayNameList.add(data);
                }
            }
        }
        cur.close();
        ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, arrayNameList );
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(MainActivity.this, "still fine", Toast.LENGTH_SHORT).show();

                String entry= (String) parent.getAdapter().getItem(position);
                //start service
                serviceIntent = new Intent(MainActivity.this, MyService.class);
                startService(serviceIntent);
                Toast.makeText(MainActivity.this, "still fine", Toast.LENGTH_SHORT).show();
                // bind to the service.
                bindService(serviceIntent,
                        serviceConnection,
                        Context.BIND_AUTO_CREATE);

                if (isBound) {
                    myService.stopMusic();
                    myService.doPlayerNull();
                    myService.playMusic(entry);
                    Toast.makeText(MainActivity.this, "fuck", Toast.LENGTH_SHORT).show();
                }
            }
        });



//        forwordButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent1 = new Intent();
//                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent1, 0);
//                Notification notification = new Notification.Builder(MainActivity.this)
//                        .setTicker("Tiker TITLe")
//                        .setContentTitle("df")
//                        .setContentText("fuck")
//                        .setSmallIcon(R.mipmap.ic_launcher)
//                        .setContentIntent(pendingIntent).getNotification();
//
//                notification.flags = Notification.FLAG_AUTO_CANCEL;
//                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                notificationManager.notify(0, notification);
//            }
//        });  this keep as comment

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(serviceIntent != null) {

                }
                myService.stopMusic();
                stopService(serviceIntent);
                playButton.setImageResource(R.drawable.ic_play);
            }
        });
//
//        forwordButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyService.LocalBinder binder = (MyService.LocalBinder) service;
            myService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) || Environment.MEDIA_MOUNTED.equals(state));
    }

    private boolean checkPermission() {
        if(!isExternalStorageReadable() || !isExternalStorageWritable()) {
            return  false;
        }
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_WRITE
            );
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_WRITE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = true;
                    Toast.makeText(this, "External storage permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "You must grant permission!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

//    public void showNotification() {
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setSmallIcon(R.drawable.notification_icon)
//                .setContentTitle(textTitle)
//                .setContentText(textContent)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//    }

}