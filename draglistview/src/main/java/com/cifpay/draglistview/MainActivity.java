package com.cifpay.draglistview;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cifpay.draglistview.view.DragListView;
import com.cifpay.draglistview.view.MyListView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private MyListView listView;
    private List<String> mList;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initView();

    }

    private void initView() {
        listView = (MyListView) findViewById(R.id.listView);
//        imageView = (ImageView) findViewById(R.id.imageView);

        mList = new ArrayList<String>();
        for (int i = 0; i < 30; i++) {
            mList.add("数据: " + i);
        }

        listView.setAdapter(new MyAdapter());
        listView.setData(mList);

    }


    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(MainActivity.this);
            textView.setText(mList.get(position));
            textView.setTextSize(28);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(0, 10, 0, 10);
            return textView;
        }
    }

}
