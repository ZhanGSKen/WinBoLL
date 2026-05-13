package cc.winboll.studio.gallery;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;

import cc.winboll.studio.libappbase.LogUtils;

public class TrashActivity extends AppCompatActivity {
    public static final String TAG = "TrashActivity";
    private static final int PERMISSION_REQUEST_CODE = 102;
    private RecyclerView recyclerView;
    private TrashAdapter adapter;
    private TrashManager trashManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogUtils.d(TAG, "onCreate");
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Trash");
        
        trashManager = new TrashManager(this);
        
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new TrashAdapter();
        recyclerView.setAdapter(adapter);
        
        adapter.setOnTrashClickListener(new TrashAdapter.OnTrashClickListener() {
            @Override
            public void onRestoreClick(int position) {
                restoreImage(position);
            }
            
            @Override
            public void onDeleteClick(int position) {
                permanentlyDelete(position);
            }
        });
        
        if (checkPermission()) {
            loadTrash();
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
                loadTrash();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadTrash() {
        LogUtils.d(TAG, "loadTrash");
        Cursor cursor = trashManager.getTrashList();
        ArrayList<TrashItem> items = new ArrayList<TrashItem>();
        ArrayList<Uri> uris = new ArrayList<Uri>();
        
        String trashPath = TrashDbHelper.getTrashPath();
        File trashDir = new File(trashPath);
        
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                try {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                    String fileName = cursor.getString(cursor.getColumnIndexOrThrow("file_name"));
                    String originalPath = cursor.getString(cursor.getColumnIndexOrThrow("original_path"));
                    String originalFolder = cursor.getString(cursor.getColumnIndexOrThrow("original_folder"));
                    
                    TrashItem item = new TrashItem();
                    item.id = id;
                    item.fileName = fileName;
                    item.originalPath = originalPath;
                    item.originalFolder = originalFolder;
                    
                    File trashFile = new File(trashDir, fileName);
                    if (trashFile.exists()) {
                        items.add(item);
                        uris.add(Uri.fromFile(trashFile));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        adapter.setData(items, uris);
        
        if (items.isEmpty()) {
            Toast.makeText(this, "Trash is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void restoreImage(int position) {
        LogUtils.d(TAG, "restoreImage: " + position);
        long id = adapter.getItemId(position);
        String fileName = adapter.getFileName(position);
        String originalPath = adapter.getOriginalPath(position);
        
        if (trashManager.restore(id, fileName, originalPath)) {
            Toast.makeText(this, "Image restored", Toast.LENGTH_SHORT).show();
            LogUtils.i(TAG, "Image restored");
            loadTrash();
        } else {
            Toast.makeText(this, "Restore failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void permanentlyDelete(int position) {
        long id = adapter.getItemId(position);
        String fileName = adapter.getFileName(position);
        
        if (trashManager.deletePermanently(id, fileName)) {
            Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
            loadTrash();
        } else {
            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_trash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_trash) {
            trashManager.clearTrash();
            Toast.makeText(this, "Trash cleared", Toast.LENGTH_SHORT).show();
            loadTrash();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission()) {
            loadTrash();
        }
    }
    
    public static class TrashItem {
        public long id;
        public String fileName;
        public String originalPath;
        public String originalFolder;
    }
}