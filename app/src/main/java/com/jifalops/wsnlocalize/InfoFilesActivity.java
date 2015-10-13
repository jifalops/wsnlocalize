package com.jifalops.wsnlocalize;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 *
 */
public class InfoFilesActivity extends Activity {
    RecyclerView recyclerView;
    InfoFileAdapter adapter;
    ViewGroup cardOptions;
    TextView selectedCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infofiles);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        cardOptions = (ViewGroup) findViewById(R.id.cardOptions);
        selectedCount = (TextView) findViewById(R.id.selectedCount);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InfoFileAdapter(this);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] ids = adapter.getSelectedPositions();
                selectedCount.setText(ids.length+"");
                cardOptions.setVisibility(ids.length > 0 ? View.VISIBLE : View.GONE);
            }
        });
        cardOptions.setVisibility(View.GONE);
    }

    public void doTraining(View v) {

    }
}
