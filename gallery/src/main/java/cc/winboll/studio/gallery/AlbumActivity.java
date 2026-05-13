package cc.winboll.studio.gallery;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

import cc.winboll.studio.libappbase.LogUtils;
import cc.winboll.studio.gallery.ImageAdapter.OnImageClickListener;

public class AlbumActivity extends AppCompatActivity {
    public static final String TAG = "AlbumActivity";
    private static final int PERMISSION_REQUEST_CODE = 101;
    public static final String EXTRA_ALBUM_PATH = "album_path";
    public static final String EXTRA_ALBUM_NAME = "album_name";
    private RecyclerView recyclerView;
    private ImageAdapter adapter;
    private String albumPath;
    private String albumName;
    private FloatingActionButton fabScrollTop;
    private Preferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogUtils.d(TAG, "onCreate");
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        albumPath = getIntent().getStringExtra(EXTRA_ALBUM_PATH);
        albumName = getIntent().getStringExtra(EXTRA_ALBUM_NAME);
        
        getSupportActionBar().setTitle(albumName);
        
        prefs = new Preferences(this);
        
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new ImageAdapter();
        adapter.setContext(this);
        adapter.setAlbumPath(albumPath);
        recyclerView.setAdapter(adapter);
        
        fabScrollTop = findViewById(R.id.fab_scroll_top);
        fabScrollTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.scrollToPosition(0);
            }
        });
        
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int firstVisible = layoutManager.findFirstVisibleItemPosition();
                    if (firstVisible > 0) {
                        fabScrollTop.setVisibility(View.VISIBLE);
                    } else {
                        fabScrollTop.setVisibility(View.GONE);
                    }
                }
            }
        });
        
        adapter.setOnImageClickListener(new OnImageClickListener() {
            @Override
            public void onImageClick(int position, ArrayList<Uri> urls, ArrayList<String> paths) {
                Intent intent = new Intent(AlbumActivity.this, ImageViewerActivity.class);
                intent.putParcelableArrayListExtra(ImageViewerActivity.EXTRA_IMAGE_URLS, urls);
                intent.putStringArrayListExtra(ImageViewerActivity.EXTRA_POSITIONS, paths);
                intent.putExtra(ImageViewerActivity.EXTRA_POSITION, position);
                startActivity(intent);
            }
        });
        
        if (checkPermission()) {
            loadImages();
        } else {
            requestPermission();
        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) 
            == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, 
            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 
            PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImages();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

private String getSortOrder(int sortMode) {
        switch (sortMode) {
            case Preferences.SORT_TIME_ASC:
                return android.provider.MediaStore.Images.Media.DATE_ADDED + " ASC";
            case Preferences.SORT_NAME_DESC:
                return android.provider.MediaStore.Images.Media.DISPLAY_NAME + " DESC";
            case Preferences.SORT_NAME_ASC:
                return android.provider.MediaStore.Images.Media.DISPLAY_NAME + " ASC";
            case Preferences.SORT_TIME_DESC:
            default:
                return android.provider.MediaStore.Images.Media.DATE_ADDED + " DESC";
        }
    }
    
    private void loadImages() {
        LogUtils.d(TAG, "loadImages");
        ArrayList<Uri> imageUrls = new ArrayList<>();
        ArrayList<String> imagePaths = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Uri collection = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        
        String selection = android.provider.MediaStore.Images.Media.DATA + " LIKE ?";
        String[] selectionArgs = new String[]{albumPath + "%"};
        int sortMode = prefs.getAlbumSortMode();
        String sortOrder = getSortOrder(sortMode);
        
        try (Cursor cursor = contentResolver.query(collection, null, selection, selectionArgs, sortOrder)) {
            if (cursor != null) {
                int dataColumn = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA);
                while (cursor.moveToNext()) {
                    String path = cursor.getString(dataColumn);
                    if (path != null && path.startsWith(albumPath + "/")) {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media._ID));
                        Uri contentUri = Uri.withAppendedPath(collection, String.valueOf(id));
                        imageUrls.add(contentUri);
                        imagePaths.add(path);
                    }
                }
            }
        }
        
        if (imageUrls.isEmpty()) {
            Toast.makeText(this, R.string.no_images_found, Toast.LENGTH_SHORT).show();
            LogUtils.i(TAG, "No images found");
        }
        adapter.setData(imageUrls, imagePaths);
        LogUtils.d(TAG, "Loaded " + imageUrls.size() + " images");
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album, menu);
        int sortMode = prefs.getAlbumSortMode();
        int menuId = getSortMenuId(sortMode);
        MenuItem item = menu.findItem(menuId);
        if (item != null) {
            item.setChecked(true);
        }
        MenuItem sortItem = menu.findItem(R.id.action_sort);
        if (sortItem != null && sortItem.getSubMenu() != null) {
            sortItem.getSubMenu().setGroupCheckable(0, true, true);
        }
        return true;
    }
    
    private int getSortMenuId(int sortMode) {
        switch (sortMode) {
            case Preferences.SORT_TIME_ASC:
                return R.id.sort_time_asc;
            case Preferences.SORT_NAME_DESC:
                return R.id.sort_name_desc;
            case Preferences.SORT_NAME_ASC:
                return R.id.sort_name_asc;
            case Preferences.SORT_TIME_DESC:
            default:
                return R.id.sort_time_desc;
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.sort_time_desc) {
            prefs.setAlbumSortMode(Preferences.SORT_TIME_DESC);
            item.setChecked(true);
            loadImages();
            return true;
        } else if (itemId == R.id.sort_time_asc) {
            prefs.setAlbumSortMode(Preferences.SORT_TIME_ASC);
            item.setChecked(true);
            loadImages();
            return true;
        } else if (itemId == R.id.sort_name_desc) {
            prefs.setAlbumSortMode(Preferences.SORT_NAME_DESC);
            item.setChecked(true);
            loadImages();
            return true;
        } else if (itemId == R.id.sort_name_asc) {
            prefs.setAlbumSortMode(Preferences.SORT_NAME_ASC);
            item.setChecked(true);
            loadImages();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission()) {
            loadImages();
        }
        if (adapter != null) {
            adapter.refreshBg();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            loadImages();
        }
    }
}