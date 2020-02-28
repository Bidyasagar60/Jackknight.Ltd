package com.knight.knightmusicplayer;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class MySongList extends AppCompatActivity {
    private ListView SongsListView;
    private String[] musicNames;
    public final int REQUEST_CODE=2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allsong);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        SongsListView=findViewById(R.id.allSongListView);
        showAllSong();


    }

    private ArrayList<File> findsong(File file)
    {
        ArrayList<File> arrayList=new ArrayList<>();
        File[] files=file.listFiles();
        for(File singlefile : files)
        {
            if(singlefile.isDirectory() && !singlefile.isHidden())
            {
                arrayList.addAll(findsong(singlefile));
            }
            else
            if (singlefile.getName().endsWith(".mp3")  && !singlefile.getName().startsWith("._") )
            {
                arrayList.add(singlefile);
            }
        }
        return arrayList;
    }
    private void showAllSong() {



        final ArrayList<File> mySongs=findsong(Environment.getExternalStorageDirectory());
        musicNames=new String[mySongs.size()];
        for(int i=0;i<mySongs.size();i++)
        {
            musicNames[i]=mySongs.get(i).getName().toString().replace(".mp3","").replace(".wav","");

           // Log.d("Songs", "Name "+musicNames[i]);
        }

        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(this,R.layout.mybg,musicNames);
        SongsListView.setAdapter(arrayAdapter);

        SongsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String Songname=SongsListView.getItemAtPosition(position).toString();

                Intent intent=getIntent();
               intent.putExtra("songs",mySongs).putExtra("songname",Songname).putExtra("pos",position);
               setResult(RESULT_OK,intent);
               finish();
            }
        });

    }

}
