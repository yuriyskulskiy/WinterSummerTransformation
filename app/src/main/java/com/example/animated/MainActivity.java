package com.example.animated;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.animated.article.custom.CustomLinearLayoutManager;
import com.example.animated.article.custom.RecViewAdapter;
import com.example.animated.article.custom.RegularData;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecView = findViewById(R.id.demoRV);
        mRecView.setHasFixedSize(true);
        layoutManager = new CustomLinearLayoutManager(this);
        mRecView.setLayoutManager(layoutManager);
        mAdapter = new RecViewAdapter(RegularData.createMockDataSet());
        mRecView.setAdapter(mAdapter);
        DividerItemDecoration divider = new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL);
        mRecView.addItemDecoration(divider);
    }
}
