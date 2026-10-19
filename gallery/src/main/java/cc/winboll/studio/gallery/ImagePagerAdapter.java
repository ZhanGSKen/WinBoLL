package cc.winboll.studio.gallery;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.ArrayList;

import cc.winboll.studio.libappbase.LogUtils;

public class ImagePagerAdapter extends PagerAdapter {
    public static final String TAG = "ImagePagerAdapter";
    private ArrayList<Uri> imageUrls;

    public ImagePagerAdapter(ArrayList<Uri> imageUrls) {
        this.imageUrls = imageUrls;
        LogUtils.d(TAG, "ImagePagerAdapter created with " + imageUrls.size() + " images");
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext())
            .inflate(R.layout.item_image_pager, container, false);
        view.setBackgroundResource(R.color.black);
        ImageView imageView = view.findViewById(R.id.image);
        
        Glide.with(imageView.getContext())
            .load(imageUrls.get(position))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(imageView);
        
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}