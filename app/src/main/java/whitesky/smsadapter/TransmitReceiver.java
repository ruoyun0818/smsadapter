package whitesky.smsadapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class TransmitReceiver extends BroadcastReceiver {

    private static AtomicLong AID = new AtomicLong(0);

    @Override
    public void onReceive(Context context, Intent intent) {
        Long id = AID.incrementAndGet();
        MainActivity.appendLog("拦截到短信->"+id);
        Bundle bundle = intent.getExtras();
        SmsMessage msg = null;
        if (null != bundle) {
            Object[] smsObj = (Object[]) bundle.get("pdus");
            for (Object object : smsObj) {
                msg = SmsMessage.createFromPdu((byte[]) object);
                Date date = new Date(msg.getTimestampMillis());//时间
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String receiveTime = format.format(date);
                String number = msg.getOriginatingAddress();
                String message = msg.getDisplayMessageBody();
                message = "@"+number+"@" + message;
                Log.i("noco", message);
                MainActivity.appendLog(message+"->"+id);
                String transmitNunmber = MainActivity.getSettingNote(context, "number");
                if (transmitNunmber.equals("")) {//第一次安装软件时，在没有设置转发号码的时候不转发

                } else {//添加了号码
                    transmitMessageTo(transmitNunmber, message);
                }
            }
        }
    }

    public void transmitMessageTo(String phoneNumber, String message) {//转发短信
        SmsManager manager = SmsManager.getDefault();
        /** 切分短信，每七十个汉字切一个，短信长度限制不足七十就只有一个：返回的是字符串的List集合*/
        List<String> texts = manager.divideMessage(message);//这个必须有
        for (String text : texts) {
            manager.sendTextMessage(phoneNumber, null, text, null, null);
        }
    }
}