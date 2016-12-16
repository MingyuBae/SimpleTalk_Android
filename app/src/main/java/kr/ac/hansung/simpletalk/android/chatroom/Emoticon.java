package kr.ac.hansung.simpletalk.android.chatroom;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import kr.ac.hansung.simpletalk.android.R;

public class Emoticon extends Activity {
    private int[] emoticonList = new int[]{R.drawable.r01, R.drawable.r02, R.drawable.r03, R.drawable.r04, R.drawable.r05, R.drawable.r06, R.drawable.r07};

    private ImageButton r01;
    private ImageButton r02;
    private ImageButton r03;
    private ImageButton r04;
    private ImageButton r05;
    private ImageButton r06;
    private ImageButton r07;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emoticon);

        r01 =(ImageButton)findViewById(R.id.r01);
        r02 =(ImageButton)findViewById(R.id.r02);
        r03 =(ImageButton)findViewById(R.id.r03);
        r04 =(ImageButton)findViewById(R.id.r04);
        r05 =(ImageButton)findViewById(R.id.r05);
        r06 =(ImageButton)findViewById(R.id.r06);
        r07 =(ImageButton)findViewById(R.id.r07);

        r01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("fileid", emoticonList[0]);

                setResult(200, returnIntent);
                finish();
            }
        });
        r02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("fileid", emoticonList[1]);

                setResult(200, returnIntent);
                finish();
            }
        });
        r03.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("fileid", emoticonList[2]);

                setResult(200, returnIntent);
                finish();
            }
        });
        r04.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("fileid", emoticonList[3]);

                setResult(200, returnIntent);
                finish();
            }
        });
        r05.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("fileid", emoticonList[4]);

                setResult(200, returnIntent);
                finish();
            }
        });
        r06.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("fileid", emoticonList[5]);

                setResult(200, returnIntent);
                finish();
            }
        });
        r07.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("fileid", emoticonList[6]);

                setResult(200, returnIntent);
                finish();
            }
        });
    }
}

