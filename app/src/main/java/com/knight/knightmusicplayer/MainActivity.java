package com.knight.knightmusicplayer;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private TextView songnameView;
    private ImageButton next, previous, PlayPaush;
    private Button allSongView;
    private MediaPlayer mediaPlayer;
    private SeekBar musicbar;
    public final int REQUEST_CODE = 2;
    private String CurrentSong;
    private List<File> AllsongsList;
    private int position;
    private Runnable runnable;
    private Handler handler;
    private int progressmusic=0;
    private Uri url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        runtimepermission();

        //mycheckpermissoon();

        AllsongsList=new ArrayList<>();
        songnameView = findViewById(R.id.songNameview);
        next = findViewById(R.id.next);
        previous = findViewById(R.id.previous);
        PlayPaush = findViewById(R.id.play);
        allSongView = findViewById(R.id.allsong);
        musicbar = findViewById(R.id.musicbar);





        musicbar.getProgressDrawable().setColorFilter(ContextCompat.getColor(this, R.color.buttonbg), PorterDuff.Mode.MULTIPLY);
        musicbar.getThumb().setColorFilter(ContextCompat.getColor(this, R.color.buttonbg), PorterDuff.Mode.SRC_IN);

        next.setOnClickListener(this);
        previous.setOnClickListener(this);
        PlayPaush.setOnClickListener(this);
        allSongView.setOnClickListener(this);

        musicbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });






    }
    private void  ChangeSeekbar()
    {
       musicbar.setProgress(mediaPlayer.getCurrentPosition());
       if(mediaPlayer.isPlaying())
       {
           runnable= new Runnable() {
               @Override
               public void run() {
                   ChangeSeekbar();
               }
           };
           handler=new Handler();
           handler.postDelayed(runnable,1000);
       }
    }

    public void runtimepermission() {
        Dexter.withActivity(MainActivity.this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if(mediaPlayer==null) {
                            AllsongsList = findsong(Environment.getExternalStorageDirectory());

                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                        finish();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    @Override
    public void onClick(View v) {
        Animation animation= AnimationUtils.loadAnimation(this,R.anim.blink_animation);


              switch (v.getId()) {
            case R.id.previous:
                if(url!=null) { playprevious(); }
                previous.startAnimation(animation);
                break;

            case R.id.next:
               if(url!=null){ playnext();}

                 next.startAnimation(animation);

                break;

            case R.id.play:
                playPaushmusic();
                PlayPaush.startAnimation(animation);
                break;

            case R.id.allsong:

                Intent intents = new Intent(MainActivity.this, MySongList.class);
                startActivityForResult(intents, REQUEST_CODE);
                break;

        }

    }

    private void playPaushmusic() {
        if (mediaPlayer!= null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            progressmusic=mediaPlayer.getCurrentPosition();
            PlayPaush.setImageResource(R.drawable.play_button);


        } else {
            playmusic();
            if (url!=null) {
                PlayPaush.setImageResource(R.drawable.paush_button);
            }

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                Bundle bundle = data.getExtras();
                AllsongsList = (ArrayList) bundle.getParcelableArrayList("songs");
                position = bundle.getInt("pos");
                CurrentSong = bundle.getString("songname");

                if (mediaPlayer !=null && mediaPlayer.isPlaying()) {
                    progressmusic=0;
                    mediaPlayer.stop();
                    mediaPlayer.reset();


                }


                 url = Uri.parse(AllsongsList.get(position).toString());
                songnameView.setText(CurrentSong);
                songnameView.setSelected(true);
                playmusic();


            }


        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer!= null)
        {
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                mediaPlayer.release();

            }
        }
    }


    @Override
    protected void onStart() {

        super.onStart();
        if(mediaPlayer==null) {

            AllsongsList = findsong(Environment.getExternalStorageDirectory());

            position = 0;
            if(! AllsongsList.isEmpty()) {

                CurrentSong = AllsongsList.get(position).getName().toString().replace(".mp3", "").replace(".wav", "");

                url = Uri.parse(AllsongsList.get(position).toString());
                songnameView.setText(CurrentSong);
                songnameView.setSelected(true);


            }
        }



    }



    private ArrayList<File> findsong(File file)
    {
        ArrayList<File> arrayList=new ArrayList<>();
        File[] files=file.listFiles();
        if(files!=null) {
            for (File singlefile : files) {
                if (singlefile.isDirectory() && !singlefile.isHidden()) {
                    arrayList.addAll(findsong(singlefile));
                } else if (singlefile.getName().endsWith(".mp3") && !singlefile.getName().startsWith("._")) {
                    arrayList.add(singlefile);
                }
            }
        }
            return arrayList;

    }


    private void playmusic()
    {
        songnameView.setText(CurrentSong);
        songnameView.setSelected(true);
        if(url!=null) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), url);

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    PlayPaush.setImageResource(R.drawable.paush_button);
                    musicbar.setMax(mediaPlayer.getDuration());
                    musicbar.setVisibility(View.VISIBLE);
                    mediaPlayer.seekTo(progressmusic);
                    mediaPlayer.start();
                    ChangeSeekbar();


                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playnext();
                }
            });
        }
    }

    private void playnext()
    {
        position=(position+1)%AllsongsList.size();
        CurrentSong = AllsongsList.get(position).getName().toString().replace(".mp3", "").replace(".wav", "");
        url = Uri.parse(AllsongsList.get(position).toString());
        if (mediaPlayer !=null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();

        }
            playmusic();

    }
    private void playprevious()
    {

        if(position>1)
        {
            position=(position-1);
        }
        else {
            position=0;
        }
        CurrentSong = AllsongsList.get(position).getName().toString().replace(".mp3", "").replace(".wav", "");
        url = Uri.parse(AllsongsList.get(position).toString());
        if (mediaPlayer !=null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();

        }
        playmusic();
    }


}
