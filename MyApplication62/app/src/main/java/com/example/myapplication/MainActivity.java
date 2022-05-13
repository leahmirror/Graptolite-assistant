package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;

import com.aqi00.lib.dialog.FileSaveFragment;
import com.aqi00.lib.dialog.HintDialogFragment;
import com.aqi00.lib.util.DirUtil;
import com.example.myapplication.util.BitmapUtil;
import com.example.myapplication.util.PermissionUtil;
import com.example.myapplication.widget.MeituView.ImageChangetListener;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.aqi00.lib.dialog.FileSaveFragment.FileSaveCallbacks;
import com.aqi00.lib.dialog.FileSelectFragment.FileSelectCallbacks;
import com.example.myapplication.widget.BitmapView;
import com.example.myapplication.widget.MeituView;
import com.google.android.material.tabs.TabLayout;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        ImageChangetListener,FileSelectCallbacks, FileSaveCallbacks {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("myapplication");
    }
    public Bitmap ZBitmap=null;
    public static final int REQ = 1;
    public static final int REQ2 = 2;
    private Uri imageUri;
    private String mFilePath;
    private MeituView mv_content; // 声明一个美图视图对象
    private BitmapView bv_content; // 声明一个位图视图对象
    private Bitmap mBitmap = null; // 声明一个位图对象
    private TextView tv;
    private ImageView img;
    private String pic_str;
    private Mat mat = new Mat();
    private long originMat_Addr;
    public static MainActivity mactivity;
    private ViewPager vp_content;
    private TabLayout tab_title;
    private ArrayList<String> mTitleArray = new ArrayList<String>();
    private String ImgName;
    private Canvas canvas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundResource(R.drawable.ll);
        toolbar.setTitle("笔石助手");
        toolbar.setOverflowIcon(getResources().getDrawable(R.drawable.lklk));
        //toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
//        mTitleArray.add("拍照识别");
//        mTitleArray.add("从相册选择");
//        initTabLayout();
//        initTabViewPager()

        mactivity = this;
        // setContentView(R.layout.activity_main);
        findViewById(R.id.btn_image_cut).setOnClickListener(this);
        //  findViewById(R.id.btn_image_cut).getBackground().setAlpha(0);
        findViewById(R.id.btn_image_click).setOnClickListener(this);
        findViewById(R.id.btn_save_image).setOnClickListener(this);
        findViewById(R.id.btn_cut_end).setOnClickListener(this);
        findViewById(R.id.btn_predict).setOnClickListener(this);
// 从布局文件中获取名叫mv_content的美图视图
        mv_content = findViewById(R.id.mv_content);
        // 设置美图视图的图像变更监听器
        mv_content.setImageChangetListener(this);
        //tv_intro = findViewById(R.id.tv_intro);
        // 从布局文件中获取名叫bv_content的位图视图
        bv_content = findViewById(R.id.bv_content);
        // 开启位图视图bv_content的绘图缓存
        bv_content.setDrawingCacheEnabled(true);
        img = findViewById(R.id.img);
        img.setDrawingCacheEnabled(true);
        mFilePath = getApplicationContext().getFilesDir().getAbsolutePath();
        //Environment.getExternalStorageDirectory().getPath();
        mFilePath = mFilePath + "/" + "tem.jpg";

        // Example of a call to a native method
        tv = findViewById(R.id.txt);
       // checkNeedPermissions();
    }
    @SuppressLint("ResourceType")
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.layout.menu,menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_search:
                Intent intent = new Intent(MainActivity.this,BaikedetailActivity.class);
                startActivity(intent);
                break;
            case R.id.action_kepu:
                Intent intent2 = new Intent(MainActivity.this,baikeActivity.class);
                startActivity(intent2);
                break;
            default:
        }
        return true;
    }
    public void checkNeedPermissions() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this
                , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this
                , Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat. requestPermissions( this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE},1) ;
    }

}
    /**
     * 旋转图片
     * @param angle 被旋转角度
     * @param bitmap 图片对象
     * @return 旋转后的图片
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        Log.e("TAG","angle==="+angle);
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bitmap;
        }
        if (bitmap != returnBm) {
            bitmap.recycle();
        }
        return returnBm;
    }

    /**
     * 读取照片旋转角度
     *
     * @param path 照片路径
     * @return 角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Log.e("TAG", "原图被旋转角度： ========== " + orientation );
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }
    public static Bitmap ResizeBitmap(Bitmap bitmap, int newWidth) {//拍照的图片太大，设置格式大小
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float temp = ((float) height) / ((float) width);
        int newHeight = (int) ((newWidth) * temp);
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // matrix.postRotate(45);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        bitmap.recycle();
        return resizedBitmap;
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //Pictures/
    //设置native连接
    public native String stringFromJNI(AssetManager am,Bitmap bitmap);
    public String getImgFromCamra() {
        String state = Environment.getExternalStorageState();
        File mFolder = null;
        String mImgName = null;
        // 先检测是不是有内存卡。
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            mFolder = new File(Environment.getExternalStorageDirectory(),
                    "笔石化石裁剪");
            // 判断手机中有没有这个文件夹，没有就新建。
            if (!mFolder.exists()) {
                mFolder.mkdirs();
            }
            // 自定义图片名字，这里是以毫秒数作为图片名。
            mImgName = System.currentTimeMillis() + ".jpg";
            Uri uri = Uri.fromFile(new File(mFolder, mImgName));
        }
        ImgName=mImgName;
        return mFolder + File.separator + mImgName;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_image_cut) {
//            Bitmap bitmap = BitmapUtil.openBitmap(mFilePath);
//            Intent intent=new Intent(MainActivity.this,MeituActivity.class);
//            intent.putExtra("mFilePath",mFilePath);
//            startActivity(intent);

            startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"),REQ2);
        }else if (v.getId() == R.id.btn_image_click) {
            // 启动相机程序
            if(ContextCompat.checkSelfPermission(this
                    , Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat. requestPermissions( this, new String[]{
                       Manifest.permission.CAMERA},1) ;
           //     Toast.makeText(this, "请先打开相机权限", Toast.LENGTH_LONG).show();
                return;
            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
//                }}
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = getUriForFile(MainActivity.this,new File(mFilePath));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, REQ);
        }else if (v.getId() == R.id.btn_save_image) { // 点击了保存文件按钮
            if (mBitmap == null) {
                Toast.makeText(this, "请先打开并裁剪图片文件", Toast.LENGTH_LONG).show();
                return;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this
                    , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat. requestPermissions( this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},1) ;
                return;
            }
//            if (!DirUtil.isStorageAdmit(this)) {
//                Toast.makeText(this, "请先打开存储权限", Toast.LENGTH_LONG).show();
//                //HintDialogFragment.popup(this, "请先给该应用开启手机存储读写权限");
//                return;
//            }
            //checkNeedPermissions();
            String path=getImgFromCamra();
            FileSaveFragment.saveImage(path,mBitmap);

            File file=new File(path);
          //  MediaStore.Images.Media.insertImage(getContentResolver(), BitmapFactory.decodeFile(file.getAbsolutePath()), file.getName(), null);
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(file);
            intent.setData(uri);
            sendBroadcast(intent);

            if(file.exists()){
                Toast.makeText(this, "成功保存至："+path, Toast.LENGTH_LONG).show();
            }
        else{
                Toast.makeText(this, "保存失败", Toast.LENGTH_LONG).show();
            }
            //checkNeedPermissions();
            // 打开文件保存对话框
          // FileSaveFragment.show(this, "jpg");
        }else if (v.getId() == R.id.btn_cut_end) { // 点击了结束裁剪按钮
            // civ_over.setVisibility(View.GONE);
            // 获取裁剪图像视图处理后的位图
            mBitmap = mv_content.getCropBitmap();
            // 设置图像视图iv_new的位图对象
            img.setImageBitmap(mBitmap);
        }else if (v.getId() == R.id.btn_predict) { // 点击了预测*/
            if (mBitmap == null) {
                Toast.makeText(this, "请先裁剪图片文件", Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(MainActivity.this,PredictActivity.class);
            Bundle bundle = new Bundle();
            Log.i("MainActivity", "bitmap大小: " + mBitmap.getByteCount() / 1024 + " kb");
            bundle.putBinder("bitmap", new BitmapBinder(mBitmap));
            intent.putExtras(bundle);
           /* ByteArrayOutputStream baos = new ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();
            intent.putExtra("picture", b);*/
            /*Uri uril = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), mBitmap, null,null));
            intent.setData(uril);*/
            Bitmap bm =((BitmapDrawable) ((ImageView) img).getDrawable()).getBitmap();
            Utils.bitmapToMat(bm,mat);
            originMat_Addr = mat.getNativeObjAddr();//获取mat指针
            AssetManager am = getAssets();
            String predictResult=stringFromJNI(am,bm);
            intent.putExtra("predictResult",predictResult);
            startActivity(intent);
           // tv.setText("预测结果："+stringFromJNI(am,bm));
        }
    }
    public class BitmapBinder extends Binder {
        private Bitmap bitmap;

        BitmapBinder(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        Bitmap getBitmap() {
            return bitmap;
        }
    }
    private boolean cameraIsCanUse() {
        boolean isCanUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            Camera.Parameters mParameters = mCamera.getParameters(); //针对魅族手机
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            isCanUse = false;
        }
        if (mCamera != null) {
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
                return isCanUse;
            }
        }
        return isCanUse;
    }

    private static Uri getUriForFile(Context context, File file) {
        if (context == null || file == null) {
            throw new NullPointerException();
        }
        Uri uri;

        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //如果SDK版本>=24，即：Build.VERSION.SDK_INT >= 24
            // uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);
            uri = FileProvider.getUriForFile(context, "com.example.myapplication.fileprovider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }
    private Intent getInstallIntent() {
        String savePath = "null";
        String appName = "event";
        String fileName = savePath + appName + ".apk";
        Uri uri = null;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            if (Build.VERSION.SDK_INT >= 24) {//7.0 Android N
                //com.xxx.xxx.fileprovider为上述manifest中provider所配置相同
                Context mContext = null;
                uri = FileProvider.getUriForFile(mContext, "com.example.event.fileprovider", new File(fileName));
                intent.setAction(Intent.ACTION_INSTALL_PACKAGE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//7.0以后，系统要求授予临时uri读取权限，安装完毕以后，系统会自动收回权限，该过程没有用户交互
            } else {//7.0以下
                uri = Uri.fromFile(new File(fileName));
                intent.setAction(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            startActivity(intent);
            return intent;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            e.printStackTrace();
        }catch (Exception e){

        }
        return intent;
    }
    private boolean needExit = false; // 是否需要退出App
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { // 按下返回键
            if (needExit) {
                finish(); // 关闭当前页面
            }
            needExit = true;
            Toast.makeText(this, "再按一次返回键退出!", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQ) {
                FileInputStream fis=null;
                try {
                    //fis=new FileInputStream(mFilePath);
                    File file=new File(mFilePath);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    fis=new FileInputStream(file);
                    int angle = readPictureDegree(mFilePath);
                    System.out.println("路径："+mFilePath);
                    Log.e("TAG","degree===="+angle);
                    Bitmap bitmap=BitmapFactory.decodeStream(fis);
                    Bitmap rBitamp = rotaingImageView(angle, bitmap);
                    Bitmap take= ResizeBitmap(rBitamp, 900);
                    bv_content.setImageBitmap(take);
                    refreshImage(true);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
           }
            else if(requestCode == REQ2){
                ContentResolver contentResolver = getContentResolver();
                Bitmap bitmap = null;
                try {
                    Uri mImageCaptureUri = data.getData();
                    String path=getPath(mImageCaptureUri);
                    int angle = readPictureDegree(path);
                    System.out.println("路径："+path+"角度："+angle);
                    bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(mImageCaptureUri));
                    Bitmap rBitamp = rotaingImageView(angle, bitmap);
                    Bitmap take= ResizeBitmap(rBitamp, 900);
                    bv_content.setImageBitmap(take);
                    refreshImage(true);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == R.id.btn_image_cut % 4096) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PermissionUtil.goActivity(this, ImageCutActivity.class);
            } else {
                Toast.makeText(this, "需要允许SD卡权限才能处理图片文件噢", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onImageClick() {

    }

    @Override
    public void onImageLongClick() {

    }

    @Override
    public void onImageTraslate(int offsetX, int offsetY, boolean bReset) {
        // 设置位图视图的偏移距离
        bv_content.setOffset(offsetX, offsetY, bReset);
        refreshImage(false);
    }

    @Override
    public void onImageScale(float ratio) {
        bv_content.setScaleRatio(ratio, false);
        refreshImage(false);
    }

    @Override
    public void onImageRotate(int degree) {
        bv_content.setRotateDegree(degree, false);
        refreshImage(false);
    }

    @Override
    public void onConfirmSelect(String absolutePath, String fileName, Map<String, Object> map_param) {
        // 拼接文件的完整路径
        String path = String.format("%s/%s", absolutePath, fileName);
        // 把要打开的图片文件显示在图像视图上面
        img.setImageURI(Uri.parse(path));
    }

    @Override
    public boolean isFileValid(String absolutePath, String fileName, Map<String, Object> map_param) {
        return true;
    }

    @Override
    public boolean onCanSave(String absolutePath, String fileName) {
        return true;
    }

    @Override
    public void onConfirmSave(String absolutePath, String fileName) {
        // 拼接文件的完整路径
        String path = String.format("%s/%s", absolutePath, fileName);
        // 把位图数据保存为图片文件
        BitmapUtil.saveBitmap(path, mBitmap, "jpg", 80);
        Toast.makeText(this, "成功保存图片文件：" + path, Toast.LENGTH_LONG).show();
    }
}