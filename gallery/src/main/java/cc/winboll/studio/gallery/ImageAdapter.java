package cc.winboll.studio.gallery;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cc.winboll.studio.libappbase.LogUtils;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    public static final String TAG = "ImageAdapter";
    private ArrayList<Uri> imageUrls = new ArrayList<>();
    private ArrayList<String> imagePaths = new ArrayList<>();
    private OnImageClickListener listener;
    private int bgType = 0;
    private Preferences prefs;
    private PinnedImageDbHelper pinnedDbHelper;
    private AlbumCoverDbHelper coverDbHelper;
    private String albumPath;
    
    private int getBgRes() {
        switch (bgType) {
            case 0:
                return R.drawable.bg_checkerboard;
            case 1:
                return R.drawable.bg_white;
            case 2:
                return R.drawable.bg_black;
            default:
                return R.drawable.bg_checkerboard;
        }
    }

    public interface OnImageClickListener {
        void onImageClick(int position, ArrayList<Uri> urls, ArrayList<String> paths);
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.listener = listener;
    }

    public void setData(ArrayList<Uri> urls, ArrayList<String> paths) {
        this.imageUrls = urls;
        this.imagePaths = paths;
        sortPinnedFirst();
        LogUtils.d(TAG, "setData: " + urls.size() + " images");
        notifyDataSetChanged();
    }
    
    private void sortPinnedFirst() {
        if (pinnedDbHelper == null || imagePaths.isEmpty()) {
            return;
        }
        ArrayList<Uri> pinnedUrls = new ArrayList<>();
        ArrayList<String> pinnedPaths = new ArrayList<>();
        ArrayList<Uri> unpinnedUrls = new ArrayList<>();
        ArrayList<String> unpinnedPaths = new ArrayList<>();
        for (int i = 0; i < imagePaths.size(); i++) {
            String path = imagePaths.get(i);
            if (pinnedDbHelper.isPinned(path)) {
                pinnedUrls.add(imageUrls.get(i));
                pinnedPaths.add(path);
            } else {
                unpinnedUrls.add(imageUrls.get(i));
                unpinnedPaths.add(path);
            }
        }
        
        imageUrls.clear();
        imagePaths.clear();
        imageUrls.addAll(pinnedUrls);
        imageUrls.addAll(unpinnedUrls);
        imagePaths.addAll(pinnedPaths);
        imagePaths.addAll(unpinnedPaths);
    }
    
    public void setContext(android.content.Context context) {
        prefs = new Preferences(context);
        bgType = prefs.getBgType();
        pinnedDbHelper = PinnedImageDbHelper.getInstance(context);
        coverDbHelper = AlbumCoverDbHelper.getInstance(context);
    }
    
    public void setAlbumPath(String albumPath) {
        this.albumPath = albumPath;
    }
    
    public int getCropWidth() {
        return prefs != null ? prefs.getCoverWidth() : 240;
    }
    
    public int getCropHeight() {
        return prefs != null ? prefs.getCoverHeight() : 120;
    }
    
    public void refreshBg() {
        if (prefs != null) {
            bgType = prefs.getBgType();
            notifyDataSetChanged();
        }
    }
    
    public void refreshPinned() {
        if (pinnedDbHelper == null || imagePaths.isEmpty()) {
            return;
        }
        sortPinnedFirst();
        notifyDataSetChanged();
    }
    
    private void showContextMenu(final View view, final int position) {
        final String imagePath = imagePaths.get(position);
        final Uri imageUri = imageUrls.get(position);
        final boolean[] isPinned = {pinnedDbHelper != null && pinnedDbHelper.isPinned(imagePath)};
        final boolean[] isCover = {false};
        if (coverDbHelper != null && albumPath != null) {
            String originalPath = coverDbHelper.getOriginalImagePath(albumPath);
            isCover[0] = originalPath != null && originalPath.equals(imagePath);
        }
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(view.getContext());
        builder.setTitle("Image");
        
        String[] items;
        if (isPinned[0]) {
            if (isCover[0]) {
                items = new String[]{"取消置顶", "调整封面", "取消封面"};
            } else {
                items = new String[]{"取消置顶", "设置为封面"};
            }
        } else {
            if (isCover[0]) {
                items = new String[]{"置顶", "调整封面", "取消封面"};
            } else {
                items = new String[]{"置顶", "设置为封面"};
            }
        }
        
        builder.setItems(items, new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                if (which == 0) {
                    if (pinnedDbHelper != null) {
                        if (isPinned[0]) {
                            pinnedDbHelper.unpinImage(imagePath);
                        } else {
                            pinnedDbHelper.pinImage(imagePath);
                        }
                        refreshPinned();
                    }
                } else if (which == 1) {
                    if (coverDbHelper != null && albumPath != null) {
                        Intent cropIntent = new Intent(view.getContext(), CropActivity.class);
                        cropIntent.putExtra(CropActivity.EXTRA_IMAGE_URI, imageUri);
                        cropIntent.putExtra(CropActivity.EXTRA_IMAGE_PATH, imagePath);
                        cropIntent.putExtra(CropActivity.EXTRA_ALBUM_PATH, albumPath);
                        cropIntent.putExtra(CropActivity.EXTRA_CROP_WIDTH, getCropWidth());
                        cropIntent.putExtra(CropActivity.EXTRA_CROP_HEIGHT, getCropHeight());
                        ((AlbumActivity) view.getContext()).startActivityForResult(cropIntent, 100);
                    }
                } else if (which == 2) {
                    if (coverDbHelper != null && albumPath != null && isCover[0]) {
                        coverDbHelper.deleteCover(albumPath);
                        notifyDataSetChanged();
                    }
                }
            }
        });
        builder.show();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_gallery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.imageView.setBackgroundResource(getBgRes());
        
        Glide.with(holder.imageView.getContext())
            .load(imageUrls.get(position))
            .centerCrop()
            .into(holder.imageView);
        
        final ArrayList<Uri> urls = imageUrls;
        final ArrayList<String> paths = imagePaths;
        final String imagePath = imagePaths.get(position);
        
        boolean isPinned = pinnedDbHelper != null && pinnedDbHelper.isPinned(imagePath);
        holder.pinIcon.setVisibility(isPinned ? View.VISIBLE : View.GONE);
        
        boolean isCover = false;
        if (coverDbHelper != null && albumPath != null) {
            String originalPath = coverDbHelper.getOriginalImagePath(albumPath);
            isCover = originalPath != null && originalPath.equals(imagePath);
        }
        holder.coverIcon.setVisibility(isCover ? View.VISIBLE : View.GONE);
        
        holder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onImageClick(position, urls, paths);
                }
            }
        });
        
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showContextMenu(holder.itemView, position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView pinIcon;
        ImageView coverIcon;
        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            pinIcon = itemView.findViewById(R.id.pin_icon);
            coverIcon = itemView.findViewById(R.id.cover_icon);
        }
    }
}
