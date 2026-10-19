package cc.winboll.studio.gallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import cc.winboll.studio.libappbase.LogUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class CropActivity extends AppCompatActivity {
    public static final String TAG = "CropActivity";
    public static final String EXTRA_IMAGE_URI = "image_uri";
    public static final String EXTRA_IMAGE_PATH = "image_path";
    public static final String EXTRA_ALBUM_PATH = "album_path";
    public static final String EXTRA_CROP_WIDTH = "crop_width";
    public static final String EXTRA_CROP_HEIGHT = "crop_height";
    
    private CropCanvasView cropCanvasView;
    private ZoomContainerView zoomContainer;
    private Bitmap originalBitmap;
    private String imagePath;
    private String albumPath;
    private int cropWidth = 240;
    private int cropHeight = 120;
    private float cropRatio = 2.0f;
    private Preferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        
        imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        albumPath = getIntent().getStringExtra(EXTRA_ALBUM_PATH);
        cropWidth = getIntent().getIntExtra(EXTRA_CROP_WIDTH, 240);
        cropHeight = getIntent().getIntExtra(EXTRA_CROP_HEIGHT, 120);
        
        prefs = new Preferences(this);
        int bgType = prefs.getBgType();
        if (cropWidth > 0 && cropHeight > 0) {
            cropRatio = (float) cropWidth / cropHeight;
        } else {
            cropRatio = prefs.getCoverRatio();
            cropWidth = (int) (120 * cropRatio);
            cropHeight = 120;
        }
        
        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        findViewById(R.id.btn_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCroppedCover();
            }
        });
        
        findViewById(R.id.btn_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCropInfoDialog();
            }
        });
        
        findViewById(R.id.btn_change_bg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CropActivity.this, "修改剪裁背景颜色", Toast.LENGTH_SHORT).show();
            }
        });
        
        zoomContainer = findViewById(R.id.zoom_container);
        
        SeekBar seekBarZoom = findViewById(R.id.seekbar_zoom);
        seekBarZoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (zoomContainer != null && fromUser) {
                    float scale = 0.1f + (progress / 100f) * 4.9f;
                    zoomContainer.setScaleFactor(scale);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        cropCanvasView = findViewById(R.id.crop_canvas_view);
        
        cropCanvasView.setBackgroundType(bgType);
        
        cropCanvasView.setOnBackgroundColorChangedListener(new CropCanvasView.OnBackgroundColorChangedListener() {
            @Override
            public void onBackgroundColorChanged(int color) {
            }
        });
        
        loadImage();
    }
    
    private void loadImage() {
        try {
            if (imagePath != null && new File(imagePath).exists()) {
                originalBitmap = BitmapFactory.decodeFile(imagePath);
            } else {
                Uri imageUri = getIntent().getParcelableExtra(EXTRA_IMAGE_URI);
                if (imageUri != null) {
                    originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                }
            }
            
            if (originalBitmap != null) {
                cropCanvasView.setImageBitmap(originalBitmap);
                cropCanvasView.post(new Runnable() {
                    @Override
                    public void run() {
                        initCrop();
                    }
                });
            } else {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "loadImage error: " + e.getMessage());
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void initCrop() {
        cropCanvasView.initCanvas(originalBitmap.getWidth(), originalBitmap.getHeight(), cropRatio);
        
        int viewW = cropCanvasView.getWidth();
        int viewH = cropCanvasView.getHeight();
        if (viewW > 0 && viewH > 0) {
            cropCanvasView.scaleToView(viewW, viewH);
        }
    }
    
    private void saveCroppedCover() {
        if (originalBitmap == null || originalBitmap.isRecycled()) {
            Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            Bitmap canvasBitmap = cropCanvasView.getCanvasBitmap();
            if (canvasBitmap == null || canvasBitmap.isRecycled()) {
                Toast.makeText(this, "Failed to get canvas bitmap", Toast.LENGTH_SHORT).show();
                return;
            }
            
            RectF cropRect = cropCanvasView.getCropRect();
            
            int bmpX = (int) cropRect.left;
            int bmpY = (int) cropRect.top;
            int bmpW = (int) cropRect.width();
            int bmpH = (int) cropRect.height();
            
            bmpX = Math.max(0, Math.min(bmpX, canvasBitmap.getWidth() - 1));
            bmpY = Math.max(0, Math.min(bmpY, canvasBitmap.getHeight() - 1));
            bmpW = Math.max(1, Math.min(bmpW, canvasBitmap.getWidth() - bmpX));
            bmpH = Math.max(1, Math.min(bmpH, canvasBitmap.getHeight() - bmpY));
            
            LogUtils.d(TAG, "saveCroppedCover: cropRect=" + cropRect);
            LogUtils.d(TAG, "saveCroppedCover: crop=(" + bmpX + "," + bmpY + "," + bmpW + "," + bmpH + ")");
            LogUtils.d(TAG, "saveCroppedCover: canvas size=" + canvasBitmap.getWidth() + "x" + canvasBitmap.getHeight());
            
            Bitmap cropped = Bitmap.createBitmap(canvasBitmap, bmpX, bmpY, bmpW, bmpH);
            canvasBitmap.recycle();
            
            LogUtils.d(TAG, "saveCroppedCover: cropped size=" + cropped.getWidth() + "x" + cropped.getHeight());
            
            File coverDir = new File(getFilesDir(), "covers");
            if (!coverDir.exists()) {
                coverDir.mkdirs();
            }
            File coverFile = new File(coverDir, "cover_" + System.currentTimeMillis() + ".png");
            LogUtils.d(TAG, "saveCroppedCover: cover file=" + coverFile.getAbsolutePath());
            
            FileOutputStream fos = new FileOutputStream(coverFile);
            cropped.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            
            LogUtils.d(TAG, "saveCroppedCover: file exists=" + coverFile.exists() + ", length=" + coverFile.length());
            
            cropped.recycle();
            
            AlbumCoverDbHelper coverDbHelper = AlbumCoverDbHelper.getInstance(this);
            coverDbHelper.setCoverWithCrop(albumPath, imagePath, coverFile.getAbsolutePath());
            LogUtils.d(TAG, "saveCroppedCover: cover saved to db, albumPath=" + albumPath);
            
            Intent broadcastIntent = new Intent(Preferences.ACTION_COVER_UPDATED);
            sendBroadcast(broadcastIntent);
            
            Toast.makeText(this, "封面已保存", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } catch (Exception e) {
            LogUtils.e(TAG, "saveCroppedCover error: " + e.getMessage());
            Toast.makeText(this, "Failed to save cover", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showCropInfoDialog() {
        StringBuilder info = new StringBuilder();
        info.append("=== 画布信息 ===\n");
        info.append("画布宽度: ").append(cropCanvasView.getCanvasWidth()).append("px\n");
        info.append("画布高度: ").append(cropCanvasView.getCanvasHeight()).append("px\n");
        
        info.append("\n=== 背景类型 ===\n");
        String[] bgNames = {"灰白相间", "全白", "全黑"};
        info.append("背景: ").append(bgNames[cropCanvasView.getBackgroundType()]).append("\n");
        int bgColor = cropCanvasView.getBackgroundColor();
        info.append("背景颜色: #").append(String.format("%06X", bgColor & 0xFFFFFF)).append("\n");
        info.append("拾取坐标: ").append(cropCanvasView.getLastPickImageX()).append(",")
            .append(cropCanvasView.getLastPickImageY()).append("\n");
        
        info.append("\n=== 裁剪结果 ===\n");
        RectF cropRect = cropCanvasView.getCropRect();
        if (cropRect != null) {
            info.append("裁剪区域: ").append((int)cropRect.left).append(",")
                .append((int)cropRect.top).append(",")
                .append((int)cropRect.right).append(",")
                .append((int)cropRect.bottom).append("\n");
            info.append("裁剪宽度: ").append((int)cropRect.width()).append("px\n");
            info.append("裁剪高度: ").append((int)cropRect.height()).append("px\n");
        }
        
        Bitmap canvasBitmap = cropCanvasView.getCanvasBitmap();
        final Bitmap[] previewHolder = new Bitmap[1];
        if (canvasBitmap != null && !canvasBitmap.isRecycled() && cropRect != null) {
            int bmpX = Math.max(0, (int) cropRect.left);
            int bmpY = Math.max(0, (int) cropRect.top);
            int bmpW = Math.min((int) cropRect.width(), canvasBitmap.getWidth() - bmpX);
            int bmpH = Math.min((int) cropRect.height(), canvasBitmap.getHeight() - bmpY);
            if (bmpW > 0 && bmpH > 0) {
                previewHolder[0] = Bitmap.createBitmap(canvasBitmap, bmpX, bmpY, bmpW, bmpH);
            }
        }
        
        final Bitmap previewBitmap = previewHolder[0];
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_crop_info, null);
        TextView infoText = dialogView.findViewById(R.id.info_text);
        ImageView previewImage = dialogView.findViewById(R.id.preview_image);
        final LinearLayout previewImageContainer = dialogView.findViewById(R.id.preview_image_container);
        infoText.setText(info.toString());
        if (previewBitmap != null) {
            previewImage.setImageBitmap(previewBitmap);
        }
        previewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int randomColor = 0xFF000000 | new Random().nextInt(0x00FFFFFF);
                previewImageContainer.setBackgroundColor(randomColor);
            }
        });
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("裁剪信息")
            .setView(dialogView)
            .setPositiveButton("确定", null)
            .create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (previewBitmap != null && !previewBitmap.isRecycled()) {
                    previewBitmap.recycle();
                }
            }
        });
        dialog.show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (originalBitmap != null) {
            originalBitmap.recycle();
            originalBitmap = null;
        }
    }
}
