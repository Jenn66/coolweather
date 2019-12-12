package com.coolweather.android;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class DBctivity extends AppCompatActivity {
    private ListView listView;
    private ChooseAreaFragment.MyDatabaseHelper myDatabaseHelper;
    private ArrayList<String> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbctivity);
//        myDatabaseHelper =  new ChooseAreaFragment.MyDatabaseHelper(DBctivity instanceof ChooseAreaFragment ,"CareDatabase.db", null, 1);

        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
        Cursor cursor = db.query("CareTable",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String weatherid = cursor.getString(cursor.getColumnIndex("weatherid"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                arrayList.add(name);
            }while (cursor.moveToNext());
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);

        listView = (ListView) findViewById(R.id.list_view);listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);

    }
}
