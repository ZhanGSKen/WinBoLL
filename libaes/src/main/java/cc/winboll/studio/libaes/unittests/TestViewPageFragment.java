package cc.winboll.studio.libaes.unittests;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2024/07/16 01:35:56
 * @Describe TestViewPageFragment
 */
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import cc.winboll.studio.libaes.ImagePagerAdapter;
import cc.winboll.studio.libaes.R;
import cc.winboll.studio.libaes.views.AOHPCTCSeekBar;
import cc.winboll.studio.libappbase.LogView;
import com.hjq.toast.ToastUtils;
import java.util.ArrayList;
import java.util.List;

public class TestViewPageFragment extends Fragment implements ViewPager.OnPageChangeListener, View.OnClickListener {

    public static final String TAG = "TestViewPageFragment";
    
    Context mContext;
    LogView mLogView;

    private ViewPager viewPager;
    private List<View> views; //用来存放放进ViewPager里面的布局
    //实例化存储imageView（导航原点）的集合
    ImageView[] imageViews;
    private ImagePagerAdapter adapter;//适配器
    private LinearLayout linearLayout;//下标所在在LinearLayout布局里
    private int currentPoint = 0;//当前被选中中页面的下标
    View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_viewpage, container, false);
        mContext = getActivity();
        
        mLogView = mView.findViewById(R.id.logview);
        mLogView.start();

        //viewPager = findViewById(R.id.activitymainViewPager1);
        initData();
        initView();//调用初始化视图方法
        initPoint();//调用初始化导航原点的方法
        viewPager.addOnPageChangeListener(this);//滑动事件
        //viewPager.setAdapter(new MyAdapter());

        // 获取屏幕参数
        //ScreenUtil.ScreenSize ss = ScreenUtil.getScreenSize(MainActivity.this);
        //Toast.makeText(getApplication(), Integer.toString(ss.getHeightPixels())+" "+Integer.toString(ss.getWidthPixels()), Toast.LENGTH_SHORT).show();

        return mView;
    }

    //初始化view，即显示的图片
    void initView() {
        adapter = new ImagePagerAdapter(views);
        viewPager = mView.findViewById(R.id.fragmentviewpageViewPager1);
        viewPager.setAdapter(adapter);
        linearLayout = mView.findViewById(R.id.fragmentviewpageLinearLayout1);
        initPoint();//初始化页面下方的点
        viewPager.setOnPageChangeListener(this);
        initAOHPCTCSeekBar();
        initAOHPCTCSeekBar2();
    }

    //初始化所要显示的布局
    void initData() {
        ViewPager viewPager = mView.findViewById(R.id.fragmentviewpageViewPager1);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view1 = inflater.inflate(R.layout.viewpage_atickprogressbar, viewPager, false);
        View view2 = inflater.inflate(R.layout.viewpage_acard, viewPager, false);
        View view3 = inflater.inflate(R.layout.viewpage_aohpctccard, viewPager, false);
        View view4 = inflater.inflate(R.layout.viewpage_aohpctcsb, viewPager, false);

        views = new ArrayList<>();
        views.add(view1);
        views.add(view2);
        views.add(view3);
        views.add(view4);
    }

    //setTag注释
    /*
     //View中的setTag（Onbect）表示给View添加一个格外的数据，以后可以用getTag()将这个数据取出来。来
     代表这个数据，即实例化
     Tag是标签的bai意识，这里的tag是object类型。所以通常会使用setTag()设置不同的Object子类对象，
     然后使用强制转换getTag()获得对象。
     //可以用在多个Button添加一个监听器，每个Button都设置不同的setTag。
     这个监听器就通过getTag来分辨是哪个Button 被按下。
     public class Main extends Activity {

     @Override
     public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
     Button button1 = (Button) findViewById(R.id.Button01);
     Button button2 = (Button) findViewById(R.id.Button02);
     Button button3 = (Button) findViewById(R.id.Button03);
     Button button4 = (Button) findViewById(R.id.Button04);
     MyListener listener = new MyListener();
     button1.setTag(1);
     button1.setOnClickListener(listener);
     button2.setTag(2);
     button2.setOnClickListener(listener);
     button3.setTag(3);
     button3.setOnClickListener(listener);
     button4.setTag(4);
     button4.setOnClickListener(listener);
     }

     public class MyListener implements View.OnClickListener {

     @Override
     public void onClick(View v) {
     int tag = (Integer) v.getTag();
     switch (tag) {
     case 1:
     System.out.println(“button1 click”);
     break;
     case 2:
     System.out.println(“button2 click”);
     break;
     case 3:
     System.out.println(“button3 click”);
     break;
     case 4:
     System.out.println(“button4 click”);
     break;
     }

     */

    private void initPoint() {

        imageViews = new ImageView[5];//实例化5个图片
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            imageViews[i] = (ImageView) linearLayout.getChildAt(i);
            imageViews[i].setImageResource(R.drawable.ic_arrow_left_right_bold);
            imageViews[i].setOnClickListener(this);//点击导航点，即可跳转
            imageViews[i].setTag(i);//重复利用实例化的对象
        }
        currentPoint = 0;//默认第一个坐标
        imageViews[currentPoint].setImageResource(R.drawable.ic_arrow_up_circle_outline);
    }

    //OnPageChangeListener接口要实现的三个方法
    /*    onPageScrollStateChanged(int state)
     此方法是在状态改变的时候调用，其中state这个参数有三种状态：
     SCROLL_STATE_DRAGGING（1）表示用户手指“按在屏幕上并且开始拖动”的状态
     （手指按下但是还没有拖动的时候还不是这个状态，只有按下并且手指开始拖动后log才打出。）
     SCROLL_STATE_IDLE（0）滑动动画做完的状态。
     SCROLL_STATE_SETTLING（2）在“手指离开屏幕”的状态。*/
    @Override
    public void onPageScrollStateChanged(int state) {

    }
    /*    onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
     当页面在滑动的时候会调用此方法，在滑动被停止之前，此方法回一直得到调用。其中三个参数的含义分别为：

     position :当前页面，即你点击滑动的页面（从A滑B，则是A页面的position。
     positionOffset:当前页面偏移的百分比
     positionOffsetPixels:当前页面偏移的像素位置*/
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }
    /*    onPageSelected(int position)
     此方法是页面滑动完后得到调用，position是你当前选中的页面的Position（位置编号）
     (从A滑动到B，就是B的position)*/
    public void onPageSelected(int position) {

        ImageView preView = imageViews[currentPoint];
        preView.setImageResource(R.drawable.ic_arrow_left_right_bold);
        ImageView currView = imageViews[position];
        currView.setImageResource(R.drawable.ic_arrow_up_circle_outline);
        currentPoint = position;
    }

    //小圆点点击事件
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        //通过getTag(),可以判断是哪个控件
        int i = (Integer) v.getTag();
        viewPager.setCurrentItem(i);//直接跳转到某一个页面的情况
    }

    void initAOHPCTCSeekBar() {
        AOHPCTCSeekBar seekbar = views.get(3).findViewById(R.id.fragmentviewpageAOHPCTCSeekBar1);
        seekbar.setThumb(mContext.getDrawable(R.drawable.ic_launcher));
        //seekbar.setThumbOffset(200);
        //seekbar.setThumbOffset(1);
        seekbar.setBlurRightDP(50);
        seekbar.setOnOHPCListener(new AOHPCTCSeekBar.OnOHPCListener() {

                @Override
                public void onOHPCommit() {
                    ToastUtils.show("onOHPCommit");
                }
            });
    }
    
    void initAOHPCTCSeekBar2() {
        AOHPCTCSeekBar seekbar = views.get(3).findViewById(R.id.fragmentviewpageAOHPCTCSeekBar2);
        seekbar.setThumb(mContext.getDrawable(R.drawable.ic_call));
        //seekbar.setThumbOffset(200);
        //seekbar.setThumbOffset(1);
        seekbar.setBlurRightDP(50);
        seekbar.setOnOHPCListener(new AOHPCTCSeekBar.OnOHPCListener() {

                @Override
                public void onOHPCommit() {
                    ToastUtils.show("onOHPCommit 2");
                }
            });
    }
}
