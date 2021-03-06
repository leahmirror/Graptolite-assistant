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
    private MeituView mv_content; // ??????????????????????????????
    private BitmapView bv_content; // ??????????????????????????????
    private Bitmap mBitmap = null; // ????????????????????????
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
        toolbar.setTitle("????????????");
        toolbar.setOverflowIcon(getResources().getDrawable(R.drawable.lklk));
        //toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
//        mTitleArray.add("????????????");
//        mTitleArray.add("???????????????");
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
// ??????????????????????????????mv_content???????????????
        mv_content = findViewById(R.id.mv_content);
        // ??????????????????????????????????????????
        mv_content.setImageChangetListener(this);
        //tv_intro = findViewById(R.id.tv_intro);
        // ??????????????????????????????bv_content???????????????
        bv_content = findViewById(R.id.bv_content);
        // ??????????????????bv_content???????????????
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
     * ????????????
     * @param angle ???????????????
     * @param bitmap ????????????
     * @return ??????????????????
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        Log.e("TAG","angle==="+angle);
        Bitmap returnBm = null;
        // ???????????????????????????????????????
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        try {
            // ?????????????????????????????????????????????????????????????????????
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
     * ????????????????????????
     *
     * @param path ????????????
     * @return ??????
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Log.e("TAG", "???????????????????????? ========== " + orientation );
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
    public static Bitmap ResizeBitmap(Bitmap bitmap, int newWidth) {//??????????????????????????????????????????
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
    //??????native??????
    public native String stringFromJNI(AssetManager am,Bitmap bitmap);
    public String getImgFromCamra() {
        String state = Environment.getExternalStorageState();
        File mFolder = null;
        String mImgName = null;
        // ?????????????????????????????????
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            mFolder = new File(Environment.getExternalStorageDirectory(),
                    "??????????????????");
            // ????????????????????????????????????????????????????????????
            if (!mFolder.exists()) {
                mFolder.mkdirs();
            }
            // ???????????????????????????????????????????????????????????????
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
            // ??????????????????
            if(ContextCompat.checkSelfPermission(this
                    , Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat. requestPermissions( this, new String[]{
                       Manifest.permission.CAMERA},1) ;
           //     Toast.makeText(this, "????????????????????????", Toast.LENGTH_LONG).show();
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
        }else if (v.getId() == R.id.btn_save_image) { // ???????????????????????????
            if (mBitmap == null) {
                Toast.makeText(this, "?????????????????????????????????", Toast.LENGTH_LONG).show();
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
//                Toast.makeText(this, "????????????????????????", Toast.LENGTH_LONG).show();
//                //HintDialogFragment.popup(this, "????????????????????????????????????????????????");
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
                Toast.makeText(this, "??????????????????"+path, Toast.LENGTH_LONG).show();
            }
        else{
                Toast.makeText(this, "????????????", Toast.LENGTH_LONG).show();
            }
            //checkNeedPermissions();
            // ???????????????????????????
          // FileSaveFragment.show(this, "jpg");
        }else if (v.getId() == R.id.btn_cut_end) { // ???????????????????????????
            // civ_over.setVisibility(View.GONE);
            // ??????????????????????????????????????????
            mBitmap = mv_content.getCropBitmap();
            // ??????????????????iv_new???????????????
            img.setImageBitmap(mBitmap);
        }else if (v.getId() == R.id.btn_predict) { // ???????????????*/
            if (mBitmap == null) {
                Toast.makeText(this, "????????????????????????", Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(MainActivity.this,PredictActivity.class);
            Bundle bundle = new Bundle();
            Log.i("MainActivity", "bitmap??????: " + mBitmap.getByteCount() / 1024 + " kb");
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
            originMat_Addr = mat.getNativeObjAddr();//??????mat??????
            AssetManager am = getAssets();
            String predictResult=stringFromJNI(am,bm);
            intent.putExtra("predictResult",predictResult);
            startActivity(intent);
           // tv.setText("???????????????"+stringFromJNI(am,bm));
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
            Camera.Parameters mParameters = mCamera.getParameters(); //??????????????????
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

        //???????????????AndroidN?????????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //??????SDK??????>=24?????????Build.VERSION.SDK_INT >= 24
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
                //com.xxx.xxx.fileprovider?????????manifest???provider???????????????
                Context mContext = null;
                uri = FileProvider.getUriForFile(mContext, "com.example.event.fileprovider", new File(fileName));
                intent.setAction(Intent.ACTION_INSTALL_PACKAGE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//7.0?????????????????????????????????uri?????????????????????????????????????????????????????????????????????????????????????????????
            } else {//7.0??????
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
    private boolean needExit = false; // ??????????????????App
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { // ???????????????
            if (needExit) {
                finish(); // ??????????????????
            }
            needExit = true;
            Toast.makeText(this, "???????????????????????????!", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
    // ??????????????????
    private void refreshImage(boolean is_first) {
        // ???????????????bv_content????????????????????????????????????
        Bitmap bitmap = bv_content.getDrawingCache();
        // ?????????????????????????????????
        mv_content.setOrigBitmap(bitmap);
        if (is_first) { // ????????????
            int left = bitmap.getWidth() / 4;
            int top = bitmap.getHeight() / 4;
            // ?????????????????????????????????
            mv_content.setBitmapRect(new Rect(left, top, left * 2, top * 2));
        } else { // ???????????????
            // ?????????????????????????????????
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
                    System.out.println("?????????"+mFilePath);
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
                    System.out.println("?????????"+path+"?????????"+angle);
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
                Toast.makeText(this, "????????????SD????????????????????????????????????", Toast.LENGTH_SHORT).show();
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
        // ?????????????????????????????????
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
        // ???????????????????????????
        String path = String.format("%s/%s", absolutePath, fileName);
        // ??????????????????????????????????????????????????????
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
        // ???????????????????????????
        String path = String.format("%s/%s", absolutePath, fileName);
        // ????????????????????????????????????
        BitmapUtil.saveBitmap(path, mBitmap, "jpg", 80);
        Toast.makeText(this, "???????????????????????????" + path, Toast.LENGTH_LONG).show();
    }
}