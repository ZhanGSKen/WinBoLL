package cc.winboll.studio.gallery;

import android.net.Uri;
import android.provider.MediaStore;
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
import java.util.Collections;
import java.util.Comparator;

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
        this.albums = sortAlbums(albums);
        LogUtils.d(TAG, "setData: " + albums.size() + " albums");
        notifyDataSetChanged();
    }
    
    private ArrayList<Album> sortAlbums(ArrayList<Album> list) {
        if (pinnedDbHelper == null || list == null || list.isEmpty()) {
            return list;
        }
        ArrayList<Album> pinned = new ArrayList<>();
        ArrayList<Album> unpinned = new ArrayList<>();
        for (Album album : list) {
            if (pinnedDbHelper.isPinned(album.getPath())) {
                pinned.add(album);
            } else {
                unpinned.add(album);
            }
        }
        pinned.addAll(unpinned);
        return pinned;
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
            ArrayList<Album> pinned = new ArrayList<>();
            ArrayList<Album> unpinned = new ArrayList<>();
            for (Album album : albums) {
                if (pinnedDbHelper.isPinned(album.getPath())) {
                    pinned.add(album);
                } else {
                    unpinned.add(album);
                }
            }
            Comparator<Album> nameComparator = new Comparator<Album>() {
                @Override
                public int compare(Album a1, Album a2) {
                    return a1.getName().compareToIgnoreCase(a2.getName());
                }
            };
            Collections.sort(pinned, nameComparator);
            Collections.sort(unpinned, nameComparator);
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
        String[] items = isPinned ? new String[]{"取消置顶"} : new String[]{"置顶"};
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
                        refreshPinned();
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
            .inflate(R.layout.item_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
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
