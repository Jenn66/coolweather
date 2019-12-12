package com.coolweather.android;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class ChooseAreaFragment extends Fragment {
    //初始化
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    //private ProgressDialog progressDialog;
    private ProgressBar progressDialog ;
    private TextView titleText;
    private Button backbutton;
    private Button createDatabase;
    private Button careButton;
    private MyDatabaseHelper careDatabase;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    //省列表
    private List<Province> provinceList;
    //市列表
    private List<City> cityList;
    //县列表
    private List<County> countyList;
    //选中的省份
    private Province selectedProvince;
    //选中的城市
    private City selectedCity;
    //当前选中的级别
    private int currentLevel;
    private static final String TAG = "aaa";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backbutton = (Button) view.findViewById(R.id.back_button);
        careButton= (Button) view.findViewById(R.id.showCare);
//       createDatabase = (Button)view.findViewById(R.id.create_database);

        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "queryCities/currentLevel: " + currentLevel);
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = countyList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity) {


                        Log.d("abbb", "onItemClick: 222");
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }


                }

            }
        });


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {


                    listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                        @Override
                        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

                            contextMenu.add(0,0,0,"收藏");

                        }
                    });


                return false;

            }
        });

          careDatabase = new MyDatabaseHelper(this.getActivity() ,"CareDatabase.db", null, 1);

//        Button createDatabase = (Button) findViewById(R.id.create_database);//第一次点击时，会自动检测是否有WordDatabase.db这个数据库，没有则自动创建（表也被创建出来）
//        createDatabase.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                careDatabase.getWritableDatabase();
//            }
//        });

        careButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),DBctivity.class);
                startActivity(intent);

            }
        });


        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }
    //查询全国所有省，优先从数据库中查询，如果没有查询到再去服务器查询
    private void queryProvinces(){
        titleText.setText("中国");
        backbutton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }
    //查询省内所有市，优先从数据库中查询，如果没有查询到再去服务器查询
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backbutton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+ provinceCode;
            queryFromServer(address,"city");
        }
    }
    //查询市内所有县，优先从数据库中查询，如果没有查询到再去服务器查询
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backbutton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            Log.d(TAG, "queryCounties: "+provinceCode +" " + cityCode);
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }

    //根据传入的地址和类型服务器上查询省市县数据
    private void queryFromServer(String address, final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "onResponse: 111");
                String responseText = response.body().string();
                boolean result = false;
                if("province".equals(type)){
                    result = Utility.handleProviceResponse(responseText);
                    Log.d(TAG, "onResponse: 0");
                    Log.d(TAG, "onResponse: "+result);
                }else if("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                    Log.d(TAG, "onResponse: 1");
                    Log.d(TAG, "onResponse: "+result);
                }else if("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                    Log.d(TAG, "onResponse: 2");
                    Log.d(TAG, "onResponse: "+result);
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }

            }
        });
    }
    //显示进度对话框
    private void showProgressDialog() {
        if(progressDialog ==null){
//            progressDialog = new ProgressDialog(getActivity());
            //progressDialog = new ProgressBar(getActivity());
            progressDialog = getActivity().findViewById(R.id.progress_bar);
//            progressDialog.setMessage("正在加载...");
            progressDialog.setVisibility(View.VISIBLE);
//            progressDialog.setCanceledOnTouchOutside(false);
            //progressDialog.setVisibility(View.GONE);
        }
//        progressDialog.show();
        progressDialog.isShown();
    }
    //关闭进度对话框
    private void closeProgressDialog(){
        if(progressDialog!=null){
//            progressDialog.dismiss();
//            progressDialog.stopNestedScroll();
            progressDialog.setVisibility(View.GONE);
        }
    }
//    private void showProgressDialog() {
//        if(progressDialog ==null){
//            progressDialog = new ProgressDialog(getActivity());
//
//            progressDialog.setMessage("正在加载...");
////            progressDialog.setVisibility(View.VISIBLE);
//            progressDialog.setCanceledOnTouchOutside(false);
////            progressDialog.setVisibility(View.GONE);
//        }
//        progressDialog.show();
////        progressDialog.isShown();
//    }
//    //关闭进度对话框
//    private void closeProgressDialog(){
//        if(progressDialog!=null){
//            progressDialog.dismiss();
////            progressDialog.stopNestedScroll();
//        }
//    }


    public  boolean onContextItemSelected(MenuItem item){//点击收藏

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        int wordid = (int) info.id;
        String s = info.toString();

        switch (item.getItemId()){
            case 0:
//                String a=wordid
                SQLiteDatabase db = careDatabase.getWritableDatabase();
                ContentValues values=new ContentValues();

                values.put("word",s);
                //添加插入
                db.insert("CareTable",null,values);
//                wordlist.add(a);
//                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,R.layout.support_simple_spinner_dropdown_item,wordlist);
//                ListView listView = (ListView) findViewById(R.id.list_view);
//                listView.setAdapter(adapter);



                break;

            default:
                break;

        }
        return super.onContextItemSelected(item);
    }
    //创建数据库的同时创建表

    public class MyDatabaseHelper extends SQLiteOpenHelper {
        final String CREATE_CARE = "create table CareTable ("//创建表并定义为字符串常量
                + "id int,"
                +"weatherid String,"
                + "name String)";

        private Context mContext;
        public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);

            mContext = context;

        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_CARE);
            Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();//提示创建成功
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists WordTable");
            onCreate(db);

        }
    }

}



