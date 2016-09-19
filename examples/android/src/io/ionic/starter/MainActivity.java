package io.ionic.starter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.nordnetab.hcp.main.HCPHelper;
import com.nordnetab.hcp.main.HCPResult;
import com.nordnetab.hcp.main.model.HCPError;



public class MainActivity extends Activity implements HCPHelper.FetchUpdateCallback,HCPHelper.DownloadUpdateCallback
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


            helper.setWebUrl("http://172.20.70.80/poc/");

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
    public void fetchUpdateCallback(boolean needUpdate, HCPError error) {
        Log.d("HCP", "强制更新");
        helper.downloadUpdate();
    }

    @Override
    public void downloadUpdateCallback(boolean success, int totalFiles, int fileDownloaded, HCPError error) {

    }
}