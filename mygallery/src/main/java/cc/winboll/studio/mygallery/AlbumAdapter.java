package cc.winboll.studio.mygallery;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import cc.winboll.studio.libappbase.LogUtils;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
    public static final String TAG = "AlbumAdapter";
    private ArrayList<Album> albums = new ArrayList<>();
    private OnAlbumClickListener listener;
    private Preferences prefs;
    private int bgType = 0;
    private PinnedAlbumDbHelper pinnedDbHelper;
    private OnCoverSizeListener coverSizeListener;
    private boolean coverSizeReported = false;
    
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

    public interface OnAlbumClickListener {
        void onAlbumClick(Album album);
    }
    
    public interface OnCoverSizeListener {
        void onCoverSize(int width, int height);
    }
    
    public static final int DEFAULT_COVER_WIDTH = 240;
    public static final int DEFAULT_COVER_HEIGHT = 120;
    
    public void setOnAlbumClickListener(OnAlbumClickListener listener) {
        this.listener = listener;
    }
    
    public void setOnCoverSizeListener(OnCoverSizeListener listener) {
        this.coverSizeListener = listener;
    }

    public void setData(ArrayList<Album> albums) {
        this.albums = albums;
        LogUtils.d(TAG, "setData: " + albums.size() + " albums");
        notifyDataSetChanged();
    }
    
    public void setContext(android.content.Context context) {
        prefs = new Preferences(context);
        bgType = prefs.getBgType();
        pinnedDbHelper = PinnedAlbumDbHelper.getInstance(context);
    }
    
    public void refreshBg() {
        if (prefs != null) {
            bgType = prefs.getBgType();
        }
        notifyDataSetChanged();
    }
    
    public void refreshCover() {
        notifyDataSetChanged();
    }
    
    public void refreshPinned() {
        if (pinnedDbHelper != null && albums != null && !albums.isEmpty()) {
            java.util.LinkedHashMap<String, Integer> pinnedPathMap = pinnedDbHelper.getPinnedPathsWithSortOrder();
            Set<String> pinnedPaths = pinnedPathMap.keySet();
            ArrayList<Album> pinned = new ArrayList<Album>();
            ArrayList<Album> unpinned = new ArrayList<Album>();
            
            // 按照排序号顺序添加置顶相册
            for (String pinnedPath : pinnedPathMap.keySet()) {
                for (Album album : albums) {
                    if (album.getPath().equals(pinnedPath)) {
                        pinned.add(album);
                        break;
                    }
                }
            }
            
            // 添加非置顶相册
            for (Album album : albums) {
                if (!pinnedPaths.contains(album.getPath())) {
                    unpinned.add(album);
                }
            }
            
            albums.clear();
            albums.addAll(pinned);
            albums.addAll(unpinned);
            notifyDataSetChanged();
        }
    }
    
private void showContextMenu(View view, final Album album) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(view.getContext());
        builder.setTitle(album.getName());
        final boolean isPinned = pinnedDbHelper != null && pinnedDbHelper.isPinned(album.getPath());
        String[] items = null;
        if (isPinned) {
            items = new String[]{"取消置顶", "置顶"};
        } else {
            items = new String[]{"置顶"};
        }
        builder.setItems(items, new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                if (pinnedDbHelper != null) {
                    if (which == 0) {
                        if (isPinned) {
                            pinnedDbHelper.unpinAlbum(album.getPath());
                        } else {
                            pinnedDbHelper.pinAlbum(album.getPath());
                        }
                    } else if (which == 1 && isPinned) {
                        pinnedDbHelper.repinAlbumToFront(album.getPath());
                    }
                    refreshPinned();
                }
            }
        });
        builder.show();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Album album = albums.get(position);
        LogUtils.d(TAG, "bind: " + album.getName() + ", cover=" + album.getCoverUri());
        
        holder.coverImage.setBackgroundResource(getBgRes());
        
        boolean isPinned = pinnedDbHelper != null && pinnedDbHelper.isPinned(album.getPath());
        holder.pinIcon.setVisibility(isPinned ? View.VISIBLE : View.GONE);
        
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showContextMenu(holder.itemView, album);
                return true;
            }
        });
        
        holder.albumName.setText(album.getName());
        holder.imageCount.setText(album.getImageCount() + " photos");
        
        holder.itemView.post(new Runnable() {
            @Override
            public void run() {
                if (!coverSizeReported && prefs != null) {
                    int savedWidth = prefs.getCoverWidth();
                    if (savedWidth == AlbumAdapter.DEFAULT_COVER_WIDTH) {
                        int width = holder.coverImage.getWidth();
                        int height = holder.coverImage.getHeight();
                        if (width > 0 && height > 0) {
                            prefs.setCoverWidth(width);
                            prefs.setCoverHeight(height);
                            float ratio = (float) width / height;
                            prefs.setCoverRatio(ratio);
                            coverSizeReported = true;
                            if (coverSizeListener != null) {
                                coverSizeListener.onCoverSize(width, height);
                            }
                        }
                    }
                }
            }
        });
        
        holder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onAlbumClick(album);
                }
            }
        });
        
        Uri coverUri = album.getCoverUri();
        if (coverUri != null) {
            String uriString = coverUri.toString();
            LogUtils.d(TAG, "uri scheme: " + coverUri.getScheme() + ", path: " + uriString);
            
            if ("file".equals(coverUri.getScheme())) {
                File coverFile = new File(coverUri.getPath());
                if (coverFile.exists()) {
                    Glide.with(holder.coverImage.getContext())
                        .load(coverFile)
                        .fitCenter()
                        .into(holder.coverImage);
                    return;
                }
            }
            
            Glide.with(holder.coverImage.getContext())
                .load(coverUri)
                .fitCenter()
                .into(holder.coverImage);
        }
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImage;
        ImageView pinIcon;
        TextView albumName;
        TextView imageCount;
        ViewHolder(View itemView) {
            super(itemView);
            coverImage = itemView.findViewById(R.id.album_cover);
            pinIcon = itemView.findViewById(R.id.pin_icon);
            albumName = itemView.findViewById(R.id.album_name);
            imageCount = itemView.findViewById(R.id.image_count);
        }
    }
}
