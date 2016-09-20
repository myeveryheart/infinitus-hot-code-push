package io.ionic.starter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.nordnetab.hcp.main.HCPHelper;
import com.nordnetab.hcp.main.HCPResult;
import com.nordnetab.hcp.main.model.HCPError;



public class MainActivity extends Activity implements HCPHelper.FetchUpdateCallback,HCPHelper.DownloadUpdateCallback,HCPHelper.InstallUpdateCallback
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
        if (needUpdate)
        {
            Log.d("HCP", "强制更新");
            helper.downloadUpdate(this);
        }
        else
        {
            Log.d("HCP", "没有强制更新");
            textView.setText("没有强制更新");
        }
    }

    @Override
    public void downloadUpdateCallback(boolean success, int totalFiles, int fileDownloaded, HCPError error) {
        String progressString = "正在下载第"+fileDownloaded+"个，共"+totalFiles+"个";
        textView.setText(progressString);
        if (success)
        {
            helper.installUpdate(this);
        }
        else if (error != null)
        {
            textView.setText(error.getErrorDescription());
        }
    }

    @Override
    public void installUpdateCallback(boolean success, HCPError error) {
        if (success)
        {
            textView.setText("更新成功");
        }
        else if (error != null)
        {
            textView.setText(error.getErrorDescription());
        }
    }
}