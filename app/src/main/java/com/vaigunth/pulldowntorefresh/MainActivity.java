package com.vaigunth.pulldowntorefresh;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;

import com.vaigunth.cardprinter.PrinterRecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList arrayList = new ArrayList();
        PrinterRecyclerView printerRecyclerView = (PrinterRecyclerView) findViewById(R.id.prv);
        printerRecyclerView.setPrinterCardImage(R.drawable.printer_card);
        printerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        printerRecyclerView.setAdapter(new MyRecyclerAdapter(arrayList, printerRecyclerView));

    }


    /**
     * Restarts the activity
     */
    public void reset() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reset:
                reset();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
