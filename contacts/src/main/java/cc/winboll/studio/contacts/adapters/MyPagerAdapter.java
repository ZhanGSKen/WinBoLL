package cc.winboll.studio.contacts.adapters;

/**
 * @Author ZhanGSKen@AliYun.Com
 * @Date 2025/02/20 13:33:04
 * @Describe MyPagerAdapter
 */
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import cc.winboll.studio.contacts.fragments.CallFragment;
import cc.winboll.studio.contacts.fragments.ContactsFragment;
import cc.winboll.studio.contacts.fragments.LogFragment;

public class MyPagerAdapter extends FragmentPagerAdapter {
    public static final String TAG = "MyPagerAdapter";

    private static final int PAGE_COUNT = 3;

    public MyPagerAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if(position == 1) {
            return ContactsFragment.newInstance(position);
        } else if(position == 2) {
            return LogFragment.newInstance(position);
        } else {
            return CallFragment.newInstance(position);
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}

