package io.ionic.starter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.nordnetab.chcp.main.HCPHelper;
import com.nordnetab.chcp.main.HCPResult;
import com.nordnetab.chcp.main.model.ChcpError;



public class MainActivity extends Activity implements HCPHelper.FetchUpdateCallback
{
    HCPHelper helper;
    TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView)findViewById(R.id.textView);

        helper = HCPHelper.getInstance(this);
//        helper.setListener(this);


            helper.setWebUrl("http://172.20.70.80/poc");

        if (helper.loadFromExternalStorageFolder())
        {
            //从外部加载
            textView.setText("从外部加载");
            helper.fetchUpdate(this);

        }
        else
        {
            textView.setText("从内部加载");
        }




    }

    @Override
    public void fetchUpdateCallback(boolean needUpdate, ChcpError error) {

    }
}