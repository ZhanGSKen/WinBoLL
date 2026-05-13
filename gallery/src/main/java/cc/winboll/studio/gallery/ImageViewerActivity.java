package cc.winboll.studio.gallery;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.viewpager.widget.ViewPager;
import java.io.File;
import java.util.ArrayList;

import cc.winboll.studio.libappbase.LogUtils;

public class ImageViewerActivity extends Activity implements ViewPager.OnPageChangeListener {
    public static final String TAG = "ImageViewerActivity";
    public static final String EXTRA_IMAGE_URLS = "image_urls";
    public static final String EXTRA_POSITIONS = "image_positions";
    public static final String EXTRA_POSITION = "position";
    
    private ArrayList<Uri> imageUrls;
    private ArrayList<String> imagePaths;
    private int currentPosition;
    private ViewPager viewPager;
    private View toolbar;
    private ImageButton btnBack;
    private ImageButton btnDelete;
    private ImageButton btnShare;
    private ImageButton btnInfo;
    private ImageButton btnGallery;
    private GestureDetector gestureDetector;
    private TrashManager trashManager;
    private Preferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image_viewer);
        LogUtils.d(TAG, "onCreate");
        
        imageUrls = getIntent().getParcelableArrayListExtra(EXTRA_IMAGE_URLS);
        imagePaths = getIntent().getStringArrayListExtra(EXTRA_POSITIONS);
        currentPosition = getIntent().getIntExtra(EXTRA_POSITION, 0);
        
        trashManager = new TrashManager(this);
        prefs = new Preferences(this);
        
        viewPager = findViewById(R.id.view_pager);
        toolbar = findViewById(R.id.toolbar);
        btnBack = findViewById(R.id.btn_back);
        btnDelete = findViewById(R.id.btn_delete);
        btnShare = findViewById(R.id.btn_share);
        btnInfo = findViewById(R.id.btn_info);
        
        btnGallery = findViewById(R.id.btn_gallery);
        
        ImagePagerAdapter adapter = new ImagePagerAdapter(imageUrls);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition);
        viewPager.addOnPageChangeListener(this);
        
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                toggleToolbar();
                return true;
            }
        });
        
        viewPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });
        
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareCurrentImage();
            }
        });
        
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageInfo();
            }
        });
        
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ImageViewerActivity.this, "Gallery", Toast.LENGTH_SHORT).show();
                if (imageUrls != null && currentPosition >= 0 && currentPosition < imageUrls.size()) {
                    Uri imageUri = imageUrls.get(currentPosition);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(imageUri, "image/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, "打开相册"));
                }
            }
        });
    }
    
    private void toggleToolbar() {
        if (toolbar.getVisibility() == View.VISIBLE) {
            toolbar.setVisibility(View.GONE);
        } else {
            toolbar.setVisibility(View.VISIBLE);
        }
    }
    
    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
            .setMessage("Delete to trash?")
            .setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    moveToTrash();
                }
            })
            .setNegativeButton("No", null)
            .show();
    }
    
    private void moveToTrash() {
        LogUtils.d(TAG, "moveToTrash");
        if (currentPosition >= 0 && currentPosition < imageUrls.size()) {
            String imagePath = "";
            if (imagePaths != null && currentPosition < imagePaths.size()) {
                imagePath = imagePaths.get(currentPosition);
            } else {
                imagePath = getPathFromUri(imageUrls.get(currentPosition));
            }
            
            Uri imageUri = imageUrls.get(currentPosition);
            long result = -1;
            
            if (!imagePath.isEmpty()) {
                result = trashManager.addToTrash(imagePath);
            }
            
            if (result > 0) {
                try {
                    getContentResolver().delete(imageUri, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                android.widget.Toast.makeText(this, "Moved to trash", android.widget.Toast.LENGTH_SHORT).show();
                LogUtils.i(TAG, "Moved to trash");
                removeCurrentImage();
            } else {
                try {
                    int deleted = getContentResolver().delete(imageUri, null, null);
                    android.widget.Toast.makeText(this, "Deleted: " + deleted, android.widget.Toast.LENGTH_SHORT).show();
                    removeCurrentImage();
                } catch (Exception e) {
                    e.printStackTrace();
                    removeCurrentImage();
                }
            }
        }
    }
    
    private void removeCurrentImage() {
        imageUrls.remove(currentPosition);
        if (imagePaths != null) {
            imagePaths.remove(currentPosition);
        }
        
        if (imageUrls.isEmpty()) {
            finish();
        } else {
            if (currentPosition >= imageUrls.size()) {
                currentPosition = imageUrls.size() - 1;
            }
            viewPager.setAdapter(new ImagePagerAdapter(imageUrls));
            viewPager.setCurrentItem(currentPosition);
        }
    }
    
    private String getPathFromUri(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        android.database.Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    return cursor.getString(columnIndex);
                }
            } finally {
                cursor.close();
            }
        }
        return uri.getPath();
    }
    
    private void shareCurrentImage() {
        if (currentPosition >= 0 && currentPosition < imageUrls.size()) {
            Uri imageUri = imageUrls.get(currentPosition);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Image"));
        }
    }
    
    private void showImageInfo() {
        if (currentPosition < 0 || currentPosition >= imageUrls.size()) {
            return;
        }
        
        String imagePath = "";
        if (imagePaths != null && currentPosition < imagePaths.size()) {
            imagePath = imagePaths.get(currentPosition);
        } else {
            imagePath = getPathFromUri(imageUrls.get(currentPosition));
        }
        
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            imageFile = new File(imagePath);
        }
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);
        
        android.widget.TextView labelPath = new android.widget.TextView(this);
        labelPath.setText("Path:");
        labelPath.setTextColor(getColor(android.R.color.darker_gray));
        labelPath.setTextSize(14);
        labelPath.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(labelPath);
        
        android.widget.TextView valuePath = new android.widget.TextView(this);
        valuePath.setText(imagePath);
        valuePath.setTextColor(getColor(android.R.color.black));
        valuePath.setTextSize(14);
        valuePath.setTextIsSelectable(true);
        layout.addView(valuePath);
        
        if (imageFile.exists()) {
            long sizeBytes = imageFile.length();
            String size;
            if (sizeBytes < 1024) {
                size = sizeBytes + " B";
            } else if (sizeBytes < 1024 * 1024) {
                size = String.format("%.2f KB", sizeBytes / 1024.0);
            } else {
                size = String.format("%.2f MB", sizeBytes / (1024.0 * 1024.0));
            }
            
            android.widget.TextView labelSize = new android.widget.TextView(this);
            labelSize.setText("Size:");
            labelSize.setTextColor(getColor(android.R.color.darker_gray));
            labelSize.setTextSize(14);
            labelSize.setTypeface(null, android.graphics.Typeface.BOLD);
            layout.addView(labelSize);
            
            android.widget.TextView valueSize = new android.widget.TextView(this);
            valueSize.setText(size);
            valueSize.setTextColor(getColor(android.R.color.black));
            valueSize.setTextSize(14);
            valueSize.setTextIsSelectable(true);
            layout.addView(valueSize);
        }
        
        try {
            android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            android.graphics.BitmapFactory.decodeFile(imagePath, options);
            if (options.outWidth > 0 && options.outHeight > 0) {
                android.widget.TextView labelPixels = new android.widget.TextView(this);
                labelPixels.setText("Pixels:");
                labelPixels.setTextColor(getColor(android.R.color.darker_gray));
                labelPixels.setTextSize(14);
                labelPixels.setTypeface(null, android.graphics.Typeface.BOLD);
                layout.addView(labelPixels);
                
                android.widget.TextView valuePixels = new android.widget.TextView(this);
                valuePixels.setText(options.outWidth + " x " + options.outHeight);
                valuePixels.setTextColor(getColor(android.R.color.black));
                valuePixels.setTextSize(14);
                valuePixels.setTextIsSelectable(true);
                layout.addView(valuePixels);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "get pixels error: " + e.getMessage());
        }
        
        try {
            String[] projection = {
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.DATE_TAKEN
            };
            android.database.Cursor cursor = getContentResolver().query(
                imageUrls.get(currentPosition), projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int dateAddedCol = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
                    int dateTakenCol = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                    
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    
                    if (dateTakenCol >= 0) {
                        long dateTaken = cursor.getLong(dateTakenCol);
                        if (dateTaken > 0) {
                            android.widget.TextView labelTaken = new android.widget.TextView(this);
                            labelTaken.setText("Date Taken:");
                            labelTaken.setTextColor(getColor(android.R.color.darker_gray));
                            labelTaken.setTextSize(14);
                            labelTaken.setTypeface(null, android.graphics.Typeface.BOLD);
                            layout.addView(labelTaken);
                            
                            android.widget.TextView valueTaken = new android.widget.TextView(this);
                            valueTaken.setText(sdf.format(new java.util.Date(dateTaken)));
                            valueTaken.setTextColor(getColor(android.R.color.black));
                            valueTaken.setTextSize(14);
                            valueTaken.setTextIsSelectable(true);
                            layout.addView(valueTaken);
                        }
                    }
                    if (dateAddedCol >= 0) {
                        long dateAdded = cursor.getLong(dateAddedCol);
                        if (dateAdded > 0) {
                            android.widget.TextView labelAdded = new android.widget.TextView(this);
                            labelAdded.setText("Date Added:");
                            labelAdded.setTextColor(getColor(android.R.color.darker_gray));
                            labelAdded.setTextSize(14);
                            labelAdded.setTypeface(null, android.graphics.Typeface.BOLD);
                            layout.addView(labelAdded);
                            
                            android.widget.TextView valueAdded = new android.widget.TextView(this);
                            valueAdded.setText(sdf.format(new java.util.Date(dateAdded * 1000)));
                            valueAdded.setTextColor(getColor(android.R.color.black));
                            valueAdded.setTextSize(14);
                            valueAdded.setTextIsSelectable(true);
                            layout.addView(valueAdded);
                        }
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "get date error: " + e.getMessage());
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Image Info")
            .setView(layout)
            .setPositiveButton("OK", null)
            .show();
    }
    
    @Override
    public void onPageSelected(int position) {
        currentPosition = position;
    }
    
    @Override
    public void onPageScrollStateChanged(int state) {}
    
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
    
    @Override
    public void onBackPressed() {
        finish();
    }
}