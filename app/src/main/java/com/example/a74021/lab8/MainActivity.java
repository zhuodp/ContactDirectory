package com.example.a74021.lab8;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.ParcelUuid;
import android.provider.ContactsContract;
import android.support.annotation.IntegerRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class MainActivity extends AppCompatActivity {
    public Button add;
    public  Button quit;

    public ListView listView;
    public SimpleAdapter adapter;

    public TextView nameTV;
    public EditText birthEdit;
    public EditText giftEdit;
    public TextView phone;

    private static  final String TABLE_NAME="Info";
    public List<Map<String,String>> datas= new ArrayList<Map<String, String>>();

    //根据数据库更新UI
    private void dataUpdate(){
        try{
            MyDataBase db=new MyDataBase(getBaseContext());
            SQLiteDatabase sqLiteDatabase=db.getWritableDatabase();
            Cursor cursor=sqLiteDatabase.rawQuery("select * from "+TABLE_NAME,null);
            datas = new ArrayList<Map<String,String>>();
            if (cursor==null)
            {
                Toast.makeText(getApplicationContext(),"Cursor is null",Toast.LENGTH_SHORT).show();
            }
            else
            {
                while(cursor.moveToNext()){
                    String name_1=cursor.getString(0);
                    String birth_1=cursor.getString(1);
                    String gift_1=cursor.getString(2);
                    Map<String,String> map=new HashMap<String, String>();
                    map.put("name",name_1);
                    map.put("birth",birth_1);
                    map.put("gift",gift_1);
                    datas.add(map);
                }
                adapter =new SimpleAdapter(MainActivity.this,datas,R.layout.item,
                        new String[]{"name","birth","gift"},
                        new int[]{R.id.nameLV,R.id.birthLV,R.id.giftLV});

                listView.setAdapter(adapter);
            }
        }
        catch (SQLException e)
        {
            Toast.makeText(getApplicationContext(),"更新不了",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=(ListView)findViewById(R.id.Start);

        //在刚打开时将保存在数据库的数据更新到UI中
        dataUpdate();




        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //自定义对话框
                LayoutInflater layoutInflater=LayoutInflater.from(MainActivity.this);
                View newView=layoutInflater.inflate(R.layout.dialoglayout,null);
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);

                nameTV=(TextView)newView.findViewById(R.id.nameXG1);
                birthEdit=(EditText)newView.findViewById(R.id.birthXG1);
                giftEdit=(EditText)newView.findViewById(R.id.giftXG1);
                phone=(TextView)newView.findViewById(R.id.phone);

                nameTV.setText(datas.get(position).get("name"));
                birthEdit.setText(datas.get(position).get("birth"));
                giftEdit.setText(datas.get(position).get("gift"));
                //在读取通信录之前进行api23的动态权限申请
              if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED)
              {
                     Toast.makeText(getApplicationContext(),"正在申请权限",Toast.LENGTH_SHORT);
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_CONTACTS},0);
                }
                //使用getContentResolver方法读取联系人列表
                String str1="";
                Cursor cursor1=getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
                str1="";
                while(cursor1.moveToNext()){
                    String str2=cursor1.getString(cursor1.getColumnIndex("_id"));
                    if (cursor1.getString(cursor1.getColumnIndex("display_name")).equals(nameTV.getText().toString()))
                    {
                        //判断某联系人的信息中，是否有电话号码
                        if (Integer.parseInt(cursor1.getString(cursor1.getColumnIndex("has_phone_number")))>0)
                        {
                            //取出该条联系人信息中的电话号码
                            Cursor cursor2=getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                     null,
                                    "contact_id="+str2,
                                    null,
                                    null
                            );

                            while(cursor2.moveToNext())
                            {
                                str1=str1+cursor2.getString(cursor2.getColumnIndex("data1"))+"\n";
                            }
                            cursor2.close();
                        }
                    }
                }
                cursor1.close();
                //如果手机通讯录中没有对应的联系人则将手机设为无
                if (str1.equals(""))
                {
                    str1="无";
                }
                phone.setText(str1);

                //自定义对话框的实现
                builder.setView(newView);
                builder.setTitle("修改信息");
                builder.setPositiveButton("确认修改", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (birthEdit.length() != 0) {
                            MyDataBase db = new MyDataBase(getBaseContext());
                            SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();
                            sqLiteDatabase.execSQL("update " + TABLE_NAME + " set birth=? where name=? ", new Object[]{
                                    birthEdit.getText().toString(), nameTV.getText().toString()});
                            sqLiteDatabase.execSQL("update " + TABLE_NAME + " set gift=? where name=? ", new Object[]{
                                    giftEdit.getText().toString(), nameTV.getText().toString()});
                            sqLiteDatabase.close();
                        }
                        dataUpdate();
                    }
                });
                builder.setNegativeButton("取消修改",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int which)
                    {

                    }
                });
                builder.show();
            }
        });




        add=(Button)findViewById(R.id.add);
        quit=(Button)findViewById(R.id.quit);

        //增加条目的按钮事件（跳转到编辑界面）
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,AddActivity.class);
                startActivityForResult(intent,9);
            }
        });
        //退出按钮
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                MainActivity.this.finish();}
                catch (Exception e){

                }
            }
        });
        //长按删除
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder message = new AlertDialog.Builder(MainActivity.this);
                message.setTitle("删除联系人");
                message.setMessage("是否删除该联系人");
                message.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //在数据库中进行删除
                        MyDataBase db = new MyDataBase(getBaseContext());
                        SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();
                        sqLiteDatabase.execSQL("delete from " + TABLE_NAME + " where name=? ", new String[]{datas.get(position).get("name")});
                        sqLiteDatabase.close();
                        //删除listview中的对应条目
                        datas.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                });
                message.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                message.create().show();
                return true;
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode==9&&resultCode==99)
        {
            dataUpdate();
        }
    }
}
