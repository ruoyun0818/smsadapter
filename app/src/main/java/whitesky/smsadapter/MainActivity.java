package whitesky.smsadapter;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final int CONTINUE = 3;//继续输入
    private final int CHANGE = 0;//修改号码
    private final int SAVE = 1;//保存号码
    private final int INPUT = 2;//输入号码
    private Button button,button2,button3;
    private EditText number,keywords;
    private TextView curNumber;
    private static TextView logText;
    private boolean flag;
    private int state;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        number = (EditText) findViewById(R.id.number);
        keywords = (EditText) findViewById(R.id.keywords);
        curNumber = (TextView) findViewById(R.id.cur_number);
        logText = (TextView) findViewById(R.id.log_text);
        String saveNumber = getSettingNote(this,"number");
        String saveKeywords = getSettingNote(this,"keywords");
        if("".equals(saveKeywords)){
            saveKeywords = "验证码";
            saveSettingNote(this, "keywords", saveKeywords);
        }

        flag = saveNumber.equals("");//判断是否为第一次进入软件

        if(flag){
            state = INPUT;
            buttonState(state);
        }else {
            state = CHANGE;
            buttonState(state);
        }

        keywords.setText(saveKeywords);
        number.setText(saveNumber);//显示已经保存了的号码
        curNumber.setText(saveNumber);
        appendLog("获取当前保存号码:"+saveNumber);
        appendLog("获取当前关键字:"+saveKeywords);

        number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int count = s.length();
                Log.i("noco",count+"");
                if(count > 0 && count <11){
                    state = CONTINUE;
                    buttonState(state);
                }else if (count == 11){
                    state = SAVE;
                    buttonState(state);
                }else {
                    button.setEnabled(false);
                }
                if (getSettingNote(MainActivity.this,"number").equals(s.toString())){
                    state = CHANGE;
                    buttonState(state);
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String numberStr = number.getText().toString();
                if(numberStr.length() == 11){
                    if (getSettingNote(MainActivity.this,"number").equals(numberStr)){
                        number.setText("");
                        state = INPUT;
                        buttonState(state);
                    }else {
                        saveSettingNote(MainActivity.this,"number",numberStr);
                        state = CHANGE;
                        buttonState(state);
                        Toast.makeText(MainActivity.this,"保存号码成功！",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this,"输入号码有误，请重新输入！",Toast.LENGTH_SHORT).show();
                }
                curNumber.setText(getSettingNote(MainActivity.this,"number"));
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String numberStr = number.getText().toString();
                SmsManager manager = SmsManager.getDefault();
                manager.sendTextMessage(numberStr, null, "测试短信"+System.currentTimeMillis(), null, null);
                appendLog("发送测试短信->"+numberStr);
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettingNote(MainActivity.this, "keywords", keywords.getText().toString());
                Toast.makeText(MainActivity.this,"保存关键字成功！",Toast.LENGTH_SHORT).show();
            }
        });

        checkPermission();
    }

    public static void appendLog(String log){
        if(logText != null){
            logText.append(log + "\n");
        }
    }

    private void buttonState(int state){
        switch (state){
            case INPUT:
                number.setText("");
                button.setText("输入号码");
                button.setEnabled(false);
                break;
            case SAVE:
                button.setText("保存号码");
                button.setEnabled(true);
                break;
            case CHANGE:
                button.setText("修改号码");
                button.setEnabled(true);
                break;
            case CONTINUE:
                button.setText("继续输入");
                button.setEnabled(false);
                break;
        }
    }

    private void  checkPermission()
    {
        // 检查权限是否获取（android6.0及以上系统可能默认关闭权限，且没提示）
        PackageManager pm = getPackageManager();
        boolean permission_readsms = (PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.READ_SMS", this.getPackageName()));
        boolean permission_sendsms = (PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.SEND_SMS", this.getPackageName()));

        if (!(
                permission_readsms && permission_sendsms
        )) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_SMS,
                    Manifest.permission.SEND_SMS,
            }, 0x01);
        }
    }

    public static void saveSettingNote(Context context,String key,String saveData){//保存设置
        SharedPreferences.Editor note = context.getSharedPreferences("number_save", Activity.MODE_PRIVATE).edit();
        note.putString(key, saveData);
        note.commit();
    }
    public static String getSettingNote(Context context, String key){//获取保存设置
        SharedPreferences read = context.getSharedPreferences("number_save", Activity.MODE_PRIVATE);
        return read.getString(key, "");
    }
}
