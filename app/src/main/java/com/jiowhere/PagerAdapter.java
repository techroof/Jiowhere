package com.jiowhere;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.Toast;

public class PagerAdapter extends FragmentPagerAdapter {
    private Context context;

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                IndividualNearestPlacesFragment
                        individualNearestPlacesFragment = new IndividualNearestPlacesFragment();

                return individualNearestPlacesFragment;
            case 1:
                DashboardFragment dashboardFragment = new DashboardFragment();
                return dashboardFragment;
            case 2:
                GroupNearestPlaceFragment groupNearestPlaceFragment=new GroupNearestPlaceFragment();
                return groupNearestPlaceFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
    public CharSequence getPageTitle(int position){

        switch (position) {
            case 0:

                return "Suggested";

            case 1:
                return "NEARBY";

            case 2:
                return "CHATS";

            default:
                return null;
        }

    }
}
