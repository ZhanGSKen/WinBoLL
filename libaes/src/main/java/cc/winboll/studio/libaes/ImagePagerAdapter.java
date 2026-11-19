package cc.winboll.studio.libaes;

import android.view.View;
import android.view.ViewGroup;
import androidx.viewpager.widget.PagerAdapter;
import java.util.List;

public class ImagePagerAdapter extends PagerAdapter {

    /*
     * 四个必须重写的方法，否则会报错
     *
     */

    private List<View> views;

    //构造方法，拿到views
    public ImagePagerAdapter(List<View> views) {
        this.views = views;
    }

    //以下四个是重写的方法
    // 获取要滑动的控件的数量，在这里我们以滑动的广告栏为例，那么这里就应该是展示的广告图片的ImageView数量
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return this.views.size();
    }


    // 来判断显示的是否是同一张图片，这里我们将两个参数相比较返回即可
    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        // TODO Auto-generated method stub
        return arg0 == arg1;
    }


    /**
     * position是在viewPager中显示图片的下标
     * 把对应的图片放到对应的位置就好了
     * instantiateItem和destroyItem是对应的
     * 一个是创建item，一个是销毁item
     *      当要显示的图片可以进行缓存的时候，会调用instantiateItem（）进行显示图片的初始化，
     *     我们将要显示的ImageView加入到ViewGroup中，然后作为返回值返回即可
     *
     *     ViewPager 是扩展于 ViewGroup，container参数是当前的ViewPager对象，
     *     所有的item都会被加入到ViewPager中，
     *     position就是 每个item对应的下标
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(views.get(position));
        return views.get(position);
    }

    //如果出现IllegalStateException: The specified child already has a parent. 这样的错误：则可替换为以下的try catch 代码
    /*try{
     　　　　if(views.get(position).getParent()==null){
     　             container.addView(views.get(position));
     　　　　}else{
     ((ViewGroup)views.get(position).getParent()).removeView(views.get(position));
     container.addView(views.get(position));
     }
     }catch(Exception e){
     e.printStackTrace();
     }*/

    // PagerAdapter只缓存5张要显示的图片，如果滑动的图片超出了缓存的范围，就会调用destroyItem（），将图片销毁
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views.get(position));
    }
}

