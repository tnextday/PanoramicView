package com.voole.PanoramicView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.*;
import java.util.Random;

public class DemoMain extends Activity implements View.OnClickListener{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button lBtn =(Button)findViewById(R.id.button1);
        lBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        setProgressBarIndeterminateVisibility(true);
//        //判断sd卡是否插入
//        String status = Environment.getExternalStorageState();
//        if (!status.equals(Environment.MEDIA_MOUNTED)) {
//            Toast.makeText(this, "SDCard is unmount!", Toast.LENGTH_LONG).show();
//            return ;
//        }
//        String SDPATH = Environment.getExternalStorageDirectory() + "/";
//        //判断SD卡上的文件是否存在，如果不存在就是将程序自带的资源释放到sd卡上面
//        File destDir = new File(SDPATH + "Panoramic/");
//        if (!destDir.exists()) {
//            destDir.mkdirs();
//        }
//        Random random = new Random();
//        String baseName = String.format("pano%d.jpg", random.nextInt(4));
//        String fileName = SDPATH + "Panoramic/" + baseName;
//        File texFile = new File(fileName);
//        if(!texFile.exists()){
//            try {
//                texFile.createNewFile();
//                InputStream is = getResources().getAssets().open(baseName);
//                byte[] by = new byte[is.available()];
//                is.read(by);
//                FileOutputStream fos = new FileOutputStream(texFile);
//                fos.write(by);
//                is.close();
//                fos.close();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
//        }
        Random random = new Random();
        String baseName = String.format("pano%d.jpg", random.nextInt(4));
        //路径以 "assets://"开头则读取assets目录下面的文件
        String fileName = "assets://" + baseName;
        Intent intent =new Intent(DemoMain.this,PanoramicView.class);
        //将图片路径传递给PanoramicView
        intent.putExtra("texturePath", fileName);
        startActivity(intent);
        setProgressBarIndeterminateVisibility(false);
    }
}