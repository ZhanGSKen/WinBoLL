package cc.winboll.studio.gallery;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
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
import cc.winboll.studio.gallery.AlbumAdapter.OnAlbumClickListener;
import cc.winboll.studio.gallery.utils.BackgroundUtils;
import cc.winboll.studio.libappbase.LogActivity;
import cc.winboll.studio.libappbase.LogUtils;
import com.a4455jkjh.colorpicker.ColorPickerDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int MANAGE_PERMISSION_REQUEST_CODE = 101;
    private RecyclerView recyclerView;
    private AlbumAdapter adapter;
    private Preferences prefs;
    private FloatingActionButton fabScrollTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogUtils.d(TAG, "onCreate");

        View content = findViewById(android.R.id.content);
        if (content != null) {
            content.setBackground(BackgroundUtils.getInstance().getDrawable());
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = new Preferences(this);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new AlbumAdapter();
        adapter.setContext(this);
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

        adapter.setOnAlbumClickListener(new OnAlbumClickListener() {
				@Override
				public void onAlbumClick(Album album) {
					Intent intent = new Intent(MainActivity.this, AlbumActivity.class);
					intent.putExtra(AlbumActivity.EXTRA_ALBUM_PATH, album.getPath());
					intent.putExtra(AlbumActivity.EXTRA_ALBUM_NAME, album.getName());
					startActivity(intent);
				}
			});

        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        LogUtils.i(TAG, "checkAndRequestPermissions");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, MANAGE_PERMISSION_REQUEST_CODE);
                } catch (Exception e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, MANAGE_PERMISSION_REQUEST_CODE);
                }
                return;
            }
        }

        if (checkPermission()) {
            loadAlbums();
        } else {
            requestPermission();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MANAGE_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    loadAlbums();
                } else {
                    Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
            == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, 
										  new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 
										  PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
										   @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadAlbums();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

	private void loadAlbums() {
        LogUtils.d(TAG, "loadAlbums");
        String folderPath = prefs.getFolderPath();
        File baseFolder = new File(folderPath);
        LogUtils.d(TAG, "baseFolder: " + baseFolder.getAbsolutePath() + ", exists=" + baseFolder.exists());

        if (!baseFolder.exists() || !baseFolder.isDirectory()) {
            folderPath = Preferences.getDefaultPath();
            baseFolder = new File(folderPath);
            LogUtils.d(TAG, "try default: " + baseFolder.getAbsolutePath() + ", exists=" + baseFolder.exists());
            if (!baseFolder.exists()) {
                folderPath = Environment.getExternalStorageDirectory() + "/Pictures";
                baseFolder = new File(folderPath);
                LogUtils.d(TAG, "try Pictures: " + baseFolder.getAbsolutePath() + ", exists=" + baseFolder.exists());
            }
        }

        AlbumCoverDbHelper coverDbHelper = AlbumCoverDbHelper.getInstance(this);
        ArrayList<Album> albums = new ArrayList<>();

        FileFilter directoryFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };
        File[] subfolders = baseFolder.listFiles(directoryFilter);
        LogUtils.d(TAG, "subfolders: " + (subfolders != null ? subfolders.length : 0));
        if (subfolders != null) {
            for (File subfolder : subfolders) {
                LogUtils.d(TAG, "scanning folder: " + subfolder.getName());
                String albumPath = subfolder.getAbsolutePath();
                String coverPath = coverDbHelper.getCover(albumPath);
                LogUtils.d(TAG, "loadAlbums: album=" + albumPath + ", coverPath=" + coverPath);
                Uri coverUri = null;
                if (coverPath != null) {
                    File coverFile = new File(coverPath);
                    if (coverFile.exists()) {
                        coverUri = Uri.fromFile(coverFile);
                        LogUtils.d(TAG, "loadAlbums: cover from file=" + coverFile.getAbsolutePath());
                    } else {
                        coverUri = getUriFromPath(coverPath);
                        LogUtils.d(TAG, "loadAlbums: cover from media store path=" + coverPath);
                    }
                }
                if (coverUri == null) {
                    ArrayList<Uri> images = getImagesInFolder(albumPath);
                    if (!images.isEmpty()) {
                        coverUri = images.get(0);
                    }
                }
                ArrayList<Uri> allImages = getImagesInFolder(albumPath);
                if (coverUri != null || !allImages.isEmpty()) {
                    if (coverUri == null && !allImages.isEmpty()) {
                        coverUri = allImages.get(0);
                    }
                    int imageCount = allImages.size();
                    albums.add(new Album(subfolder.getName(), albumPath, coverUri, imageCount));
                    LogUtils.d(TAG, "album added: " + subfolder.getName() + ", " + imageCount + " images");
                }
            }
        }

        if (albums.isEmpty()) {
            Toast.makeText(this, R.string.no_images_found, Toast.LENGTH_SHORT).show();
            LogUtils.i(TAG, "No albums found");
        }
        adapter.setData(albums);
        LogUtils.d(TAG, "Loaded " + albums.size() + " albums");
    }

    private Uri getUriFromPath(String path) {
        String[] projection = { MediaStore.Images.Media._ID };
        String selection = MediaStore.Images.Media.DATA + " = ?";
        String[] selectionArgs = { path };
        try (Cursor cursor = getContentResolver().query(
			MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			projection, selection, selectionArgs, null)) {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                }
            }
        }
        return null;
    }

    private ArrayList<Uri> getImagesInFolder(String folderPath) {
        ArrayList<Uri> imageUrls = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String selection = MediaStore.Images.Media.DATA + " LIKE ?";
        String[] selectionArgs = new String[]{folderPath + "/%"};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        LogUtils.d(TAG, "getImagesInFolder: " + folderPath);

        try (Cursor cursor = contentResolver.query(collection, null, selection, selectionArgs, sortOrder)) {
            if (cursor != null) {
                LogUtils.d(TAG, "cursor count: " + cursor.getCount());
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                while (cursor.moveToNext()) {
                    String path = cursor.getString(dataColumn);
                    if (path != null) {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                        Uri contentUri = Uri.withAppendedPath(collection, String.valueOf(id));
                        LogUtils.d(TAG, "image: id=" + id + ", path=" + path);
                        imageUrls.add(contentUri);
                    }
                }
            }
        }
        LogUtils.d(TAG, "found " + imageUrls.size() + " images");
        return imageUrls;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_mi_gallery) {
            Toast.makeText(this, "Gallery clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_GALLERY);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_change_bg_color) {
            //Toast.makeText(this, "修改背景颜色", Toast.LENGTH_SHORT).show();
			if (BackgroundUtils.DrawableType.COLOR == BackgroundUtils.getInstance().getDrawableType()) {

				ColorPickerDialog dlg = new ColorPickerDialog(this, BackgroundUtils.getInstance().getColor());
				dlg.setOnColorChangedListener(new com.a4455jkjh.colorpicker.view.OnColorChangedListener() {

						@Override
						public void beforeColorChanged() {
						}

						@Override
						public void onColorChanged(int color) {
							BackgroundUtils.getInstance().initFromColor(MainActivity.this, color);
							View content = findViewById(android.R.id.content);
							if (content != null) {
								content.setBackground(BackgroundUtils.getInstance().getDrawable());
							}
						}

						@Override
						public void afterColorChanged() {
						}
					});
				dlg.show();
			}
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (id == R.id.action_trash) {
            startActivity(new Intent(this, TrashActivity.class));
            return true;
        } else if (id == R.id.action_debug) {
            LogActivity.startLogActivity(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private BroadcastReceiver coverUpdatedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Preferences.ACTION_COVER_UPDATED.equals(intent.getAction())) {
                loadAlbums();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(coverUpdatedReceiver, new IntentFilter(Preferences.ACTION_COVER_UPDATED));
        if (checkPermission()) {
            scanMediaStore();
            loadAlbums();
        }
        if (adapter != null) {
            adapter.refreshBg();
            adapter.refreshPinned();
            adapter.refreshCover();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(coverUpdatedReceiver);
    }

    private void scanMediaStore() {
        String folderPath = prefs.getFolderPath();
        File baseFolder = new File(folderPath);
        if (baseFolder.exists() && baseFolder.isDirectory()) {
            File[] subfolders = baseFolder.listFiles(new FileFilter() {
					@Override
					public boolean accept(File file) {
						return file.isDirectory();
					}
				});
            if (subfolders != null) {
                ArrayList<String> paths = new ArrayList<>();
                for (File subfolder : subfolders) {
                    File[] images = subfolder.listFiles(new FilenameFilter() {
							@Override
							public boolean accept(File dir, String name) {
								String lower = name.toLowerCase();
								return lower.endsWith(".jpg") || lower.endsWith(".jpeg") 
									|| lower.endsWith(".png") || lower.endsWith(".gif")
									|| lower.endsWith(".webp") || lower.endsWith(".bmp");
							}
						});
                    if (images != null) {
                        for (File img : images) {
                            paths.add(img.getAbsolutePath());
                        }
                    }
                }
                if (!paths.isEmpty()) {
                    LogUtils.d(TAG, "scanning " + paths.size() + " files to MediaStore");
                    String[] pathArray = paths.toArray(new String[0]);
                    MediaScannerConnection.scanFile(this, pathArray, null, new MediaScannerConnection.OnScanCompletedListener() {
							@Override
							public void onScanCompleted(String path, Uri uri) {
								LogUtils.d(TAG, "scanCompleted: " + path + " -> " + uri);
							}
						});
                }
            }
        }
    }
}
