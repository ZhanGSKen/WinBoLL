package cc.winboll.studio.gallery;

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

import cc.winboll.studio.libappbase.LogUtils;

public class TrashAdapter extends RecyclerView.Adapter<TrashAdapter.ViewHolder> {
    public static final String TAG = "TrashAdapter";
    private ArrayList<TrashActivity.TrashItem> trashItems = new ArrayList<TrashActivity.TrashItem>();
    private ArrayList<Uri> imageUrls = new ArrayList<Uri>();
    private OnTrashClickListener listener;

    public interface OnTrashClickListener {
        void onRestoreClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnTrashClickListener(OnTrashClickListener listener) {
        this.listener = listener;
    }

    public void setData(ArrayList<TrashActivity.TrashItem> items, ArrayList<Uri> uris) {
        this.trashItems = items;
        this.imageUrls = uris;
        LogUtils.d(TAG, "setData: " + items.size() + " items");
        notifyDataSetChanged();
    }

    public long getItemId(int position) {
        if (position >= 0 && position < trashItems.size()) {
            return trashItems.get(position).id;
        }
        return -1;
    }
    
    public String getFileName(int position) {
        if (position >= 0 && position < trashItems.size()) {
            return trashItems.get(position).fileName;
        }
        return "";
    }
    
    public String getOriginalPath(int position) {
        if (position >= 0 && position < trashItems.size()) {
            return trashItems.get(position).originalPath;
        }
        return "";
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_trash, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        if (position < imageUrls.size()) {
            Glide.with(holder.imageView.getContext())
                .load(imageUrls.get(position))
                .centerCrop()
                .into(holder.imageView);
        }
        
        holder.btnRestore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onRestoreClick(position);
                }
            }
        });
        
        holder.btnDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDeleteClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return trashItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView btnRestore;
        ImageView btnDelete;
        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            btnRestore = itemView.findViewById(R.id.btn_restore);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}