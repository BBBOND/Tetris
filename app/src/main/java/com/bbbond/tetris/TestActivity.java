package com.bbbond.tetris;

import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bbbond on 2017/6/21.
 */

public class TestActivity extends AppCompatActivity {

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = new View(this) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);

            }
        };

        List<Integer> integers = new ArrayList<>();
        integers.add(1);
        integers.add(1);
        integers.add(1);
        int size = integers.size();
        for (int i = size; i > 0; i--) {
            i++;
        }

        Toast.makeText(TestActivity.this, "", Toast.LENGTH_SHORT).show();
    }
}
