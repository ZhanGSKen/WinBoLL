package cc.winboll.studio.positions.views;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/04/03 21:09:50
 * @Describe 位置处理工具集
 */
import android.content.Context;
import cc.winboll.studio.positions.models.PostionModel;
import java.util.ArrayList;
import android.location.Location;

public class PostionUtils {

    public static final String TAG = "PostionUtils";

    static volatile PostionUtils _PostionUtils;

    Context mContext;
    ArrayList<PostionModel> mPostionModelList = new ArrayList<PostionModel>();

    PostionUtils(Context context) {
        mContext = context;
        PostionModel.loadBeanList(mContext, mPostionModelList, PostionModel.class);
    }
    
    public synchronized static PostionUtils getInstance(Context context) {
        if (_PostionUtils == null) {
            _PostionUtils = new PostionUtils(context);
        }
        return _PostionUtils;
    }
    
    public void addPostion(PostionModel item) {
        mPostionModelList.add(item);
        PostionModel.saveBeanList(mContext, mPostionModelList, PostionModel.class);
    }
    
    public void addPostion(Location location) {
        PostionModel item = new PostionModel();
        item.setLatitude(location.getLatitude());
        item.setLongitude(location.getLongitude());
        item.setTimestamp(location.getTime());
        item.setAccuracy(location.getAccuracy());
        item.setProvider(location.getProvider());
        
        mPostionModelList.add(item);
        PostionModel.saveBeanList(mContext, mPostionModelList, PostionModel.class);
    }
}
