package com.example.sql_failure;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.sql_failure.home_fragment.AllFragment;
import com.example.sql_failure.home_fragment.ScadaFragment;
import com.example.sql_failure.home_fragment.SssFragment;
import com.example.sql_failure.home_fragment.WayFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new AllFragment();
            case 1:
                return new SssFragment();
            case 2:
                return new WayFragment();
            case 3:
                return new ScadaFragment();
            default:
                return new AllFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;//4 tab items
    }
}
