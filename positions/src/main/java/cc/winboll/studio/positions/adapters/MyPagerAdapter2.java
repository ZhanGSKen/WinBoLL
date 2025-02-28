package cc.winboll.studio.positions.adapters;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/20 13:33:04
 * @Describe MyPagerAdapter
 */
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import cc.winboll.studio.positions.fragments.TasksFragment;
import cc.winboll.studio.positions.fragments.PositionsFragment;
import cc.winboll.studio.positions.fragments.LogFragment;

public class MyPagerAdapter2 extends FragmentPagerAdapter {
    public static final String TAG = "MyPagerAdapter2";

    private static final int PAGE_COUNT = 3;

    public MyPagerAdapter2(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
//        if(position == 0) {
//            return PositionsFragment.newInstance(position);
//        } else if(position == 1) {
//            return TasksFragment.newInstance(position);
//        } else {
//            return LogFragment.newInstance(position);
//        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}

