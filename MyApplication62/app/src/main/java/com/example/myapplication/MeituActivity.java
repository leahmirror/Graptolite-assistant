package com.example.myapplication;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aqi00.lib.dialog.FileSaveFragment;
import com.aqi00.lib.dialog.FileSaveFragment.FileSaveCallbacks;
import com.aqi00.lib.dialog.FileSelectFragment;
import com.aqi00.lib.dialog.FileSelectFragment.FileSelectCallbacks;
import com.example.myapplication.util.BitmapUtil;
import com.example.myapplication.widget.BitmapView;
import com.example.myapplication.widget.MeituView;
import com.example.myapplication.widget.MeituView.ImageChangetListener;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

//import android.support.v7.app.AppCompatActivity;

public class MeituActivity extends AppCompatActivity implements
        FileSelectCallbacks, FileSaveCallbacks, ImageChangetListener ,OnClickListener {
    private final static String TAG = "MeituActivity";
    private TextView tv;
    private MeituView mv_content; // 声明一个美图视图对象
    //private TextView tv_intro;
    private ImageView iv_new;
    private BitmapView bv_content; // 声明一个位图视图对象
    private Bitmap mBitmap = null; // 声明一个位图对象
    public static final int REQ = 1;
   // private Mat mat = new Mat();
    private long originMat_Addr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meitu);
        // 从布局文件中获取名叫mv_content的美图视图
        mv_content = findViewById(R.id.mv_content);
        // 设置美图视图的图像变更监听器
        mv_content.setImageChangetListener(this);
        //tv_intro = findViewById(R.id.tv_intro);
        // 从布局文件中获取名叫bv_content的位图视图
        bv_content = findViewById(R.id.bv_content);
        // 开启位图视图bv_content的绘图缓存
        bv_content.setDrawingCacheEnabled(true);
        iv_new = findViewById(R.id.iv_new);
        tv = findViewById(R.id.txt);
        findViewById(R.id.btn_open_image).setOnClickListener(this);
        findViewById(R.id.btn_save_image).setOnClickListener(this);
        findViewById(R.id.btn_cut_end).setOnClickListener(this);
        findViewById(R.id.btn_predict).setOnClickListener(this);
    }

    // 在判断文件能否保存时触发
    public boolean onCanSave(String absolutePath, String fileName) {
        return true;
    }

    // 点击文件保存对话框的确定按钮后触发
    public void onConfirmSave(String absolutePath, String fileName) {
        // 拼接文件的完整路径
        String path = String.format("%s/%s", absolutePath, fileName);
        // 把位图数据保存为图片文件
        BitmapUtil.saveBitmap(path, mBitmap, "jpg", 80);
        Toast.makeText(this, "成功保存图片文件：" + path, Toast.LENGTH_LONG).show();
    }

    // 点击文件选择对话框的确定按钮后触发
    public void onConfirmSelect(String absolutePath, String fileName, Map<String, Object> map_param) {
        //tv_intro.setVisibility(View.GONE);
        // 拼接文件的完整路径
        String path = String.format("%s/%s", absolutePath, fileName);
        // 从指定路径的图片文件中获取位图数据
        Bitmap bitmap = BitmapUtil.openBitmap(path);
        // 设置位图视图的位图对象
        bv_content.setImageBitmap(bitmap);
        refreshImage(true);
    }

    // 检查文件是否合法时触发
    public boolean isFileValid(String absolutePath, String fileName, Map<String, Object> map_param) {
        return true;
    }

    // 刷新图像展示
    private void refreshImage(boolean is_first) {
        // 从位图视图bv_content的绘图缓存中获取位图对象
        Bitmap bitmap = bv_content.getDrawingCache();
        // 设置美图视图的原始位图
        mv_content.setOrigBitmap(bitmap);
        if (is_first) { // 首次打开
            int left = bitmap.getWidth() / 4;
            int top = bitmap.getHeight() / 4;
            // 设置美图视图的位图边界
            mv_content.setBitmapRect(new Rect(left, top, left * 2, top * 2));
        } else { // 非首次打开
            // 设置美图视图的位图边界
            mv_content.setBitmapRect(mv_content.getBitmapRect());
        }
    }

    // 在图片平移时触发
    public void onImageTraslate(int offsetX, int offsetY, boolean bReset) {
        // 设置位图视图的偏移距离
        bv_content.setOffset(offsetX, offsetY, bReset);
        refreshImage(false);
    }

    // 在图片缩放时触发
    public void onImageScale(float ratio) {
        // 设置位图视图的缩放比率
        bv_content.setScaleRatio(ratio, false);
        refreshImage(false);
    }

    // 在图片旋转时触发
    public void onImageRotate(int degree) {
        bv_content.setRotateDegree(degree, false);
        refreshImage(false);
    }

    // 在图片点击时触发
    public void onImageClick() {}

    // 在图片长按时触发
    public void onImageLongClick() {
    }

    private void savebitmap(Bitmap bitmap)
    {
        //创建文件，因为不存在2级目录，所以不用判断exist，要保存png，这里后缀就是png，要保存jpg，后缀就用jpg
        File file=new File(Environment.getExternalStorageDirectory() +"/mfw.png");
        try {
            //文件输出流
            FileOutputStream fileOutputStream=new FileOutputStream(file);
            //压缩图片，如果要保存png，就用Bitmap.CompressFormat.PNG，要保存jpg就用Bitmap.CompressFormat.JPEG,质量是100%，表示不压缩
            bitmap.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream);
            //写入，这里会卡顿，因为图片较大
            fileOutputStream.flush();
            //记得要关闭写入流
            fileOutputStream.close();
            //成功的提示，写入成功后，请在对应目录中找保存的图片
            Toast.makeText(MeituActivity.this,"写入成功！目录"+Environment.getExternalStorageDirectory()+"/mfw.png",Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //失败的提示
            Toast.makeText(MeituActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            //失败的提示
            Toast.makeText(MeituActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }

    }
    //设置native连接
    public native String stringFromJNI(AssetManager am,Bitmap bitmap);
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_open_image) { // 点击了打开文件按钮
//            // 打开文件选择对话框
//            FileSelectFragment.show(MeituActivity.this, new String[]{"jpg", "png"}, null);
            startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"),REQ);
            //startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE),REQ);
        } else if (v.getId() == R.id.btn_save_image) { // 点击了保存文件按钮
            if (mBitmap == null) {
                Toast.makeText(this, "请先打开并裁剪图片文件", Toast.LENGTH_LONG).show();
                return;
            }
            // 打开文件保存对话框
            FileSaveFragment.show(this, "jpg");
        } else if (v.getId() == R.id.btn_cut_end) { // 点击了结束裁剪按钮
           // civ_over.setVisibility(View.GONE);
            // 获取裁剪图像视图处理后的位图
            mBitmap = mv_content.getCropBitmap();
            //mBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
            System.out.println("高："+mBitmap.getHeight());
            System.out.println("宽："+mBitmap.getWidth());
            // 设置图像视图iv_new的位图对象
            iv_new.setImageBitmap(mBitmap);
        }else if (v.getId() == R.id.btn_predict) { // 点击了预测
            if (mBitmap == null) {
                Toast.makeText(this, "请先裁剪图片文件", Toast.LENGTH_LONG).show();
                return;
            }
           // Bitmap bm = Bitmap.createBitmap(iv_new.getDrawingCache());
            mBitmap = mv_content.getCropBitmap();
            //Bitmap bm =((BitmapDrawable) ((ImageView) iv_new).getDrawable()).getBitmap();
            //Utils.bitmapToMat(mBitmap,mat);
           // originMat_Addr = mat.getNativeObjAddr();//获取mat指针
            AssetManager am = getAssets();
            String filename1 = "lite.bin";
            String filename2 = "lite.param";
            tv.setText(stringFromJNI(am,mBitmap));
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQ) {
                ContentResolver contentResolver = getContentResolver();
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(data.getData()));
                    Log.i("TAG", "从相册回传bitmap：" + bitmap);
                    bv_content.setImageBitmap(bitmap);
                    refreshImage(true);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
