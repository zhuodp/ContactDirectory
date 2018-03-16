package com.example.a74021.lab8;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by 74021 on 2017/12/18.
 */

public class AddActivity extends AppCompatActivity {
    public Button addBtn;
    public Button clear;
    public Button BtnC;
    public EditText addET;
    public EditText birthET;
    public  EditText giftET;
    public static final String TABLE_NAME="Info";
    public void find()
    {
        addBtn =(Button)findViewById(R.id.BtnAdd);
        clear =(Button)findViewById(R.id.BtnClear);
        BtnC = (Button)findViewById(R.id.BtnC);
        addET = (EditText)findViewById(R.id.nameET);
        birthET = (EditText) findViewById(R.id.birthET);
        giftET = (EditText) findViewById(R.id.giftET);
    }
    @Override
    protected void onCreate(final Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.new_info);
        find();


        addBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                find();
                if (addET.length()==0)
                {
                    //当编辑框为空时，发出提示
                    Toast.makeText(AddActivity.this,"名字不能为空",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    find();
                    String namet=addET.getText().toString();
                    String birtht=birthET.getText().toString();
                    String giftt=giftET.getText().toString();

                    MyDataBase db =new MyDataBase(getBaseContext());
                    SQLiteDatabase sqLiteDatabase=db.getWritableDatabase();
                    Cursor cursor=sqLiteDatabase.rawQuery("select * from "+
                        TABLE_NAME +" where name like ?",new String[]{namet});
                    if (cursor.moveToFirst()==true)
                    {
                        //当姓名编辑框与数据库中已经存在的数据相同时，输出相应的Toast信息
                        Toast.makeText(AddActivity.this,"列表中已存在同名联系人",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        ContentValues contentValues=new ContentValues();
                        contentValues.put("name",namet);
                        contentValues.put("birth",birtht);
                        contentValues.put("gift",giftt);
                        sqLiteDatabase.insert(TABLE_NAME,null,contentValues);
                        sqLiteDatabase.close();
                        setResult(99,new Intent());
                        finish();
                    }
                }
            }
        });
        //清楚输入框中的内容事件
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                find();
                addET.setText("");
                birthET.setText("");
                giftET.setText("");
            }
        });

        //取消当前编辑按钮事件
        BtnC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AddActivity.this,MainActivity.class);
                startActivity(intent);
                AddActivity.this.finish();
            }
        });
    }
}
