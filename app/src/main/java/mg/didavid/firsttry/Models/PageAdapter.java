package mg.didavid.firsttry.Models;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import mg.didavid.firsttry.Controllers.Fragments.GMapFragment;
import mg.didavid.firsttry.Controllers.Fragments.ActuFragment;
import mg.didavid.firsttry.Controllers.Fragments.Page2Fragment;
import mg.didavid.firsttry.Controllers.Fragments.Page3Fragment;
import mg.didavid.firsttry.Controllers.Fragments.Page4Fragment;

public class PageAdapter extends FragmentPagerAdapter {
    //Default Constructor
    public PageAdapter(FragmentManager mgr) {
        super(mgr);
    }

    @Override
    public int getCount()
    {
        return(5);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: //Page number 1
                return ActuFragment.newInstance();
            case 1: //Page number 2
                return Page2Fragment.newInstance();
            case 2: //Page number 3
                return GMapFragment.newInstance();
            case 3: //Page number 3
                return Page3Fragment.newInstance();
            case 4: //Page number 3
                return Page4Fragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0: //Page number 1
                return "Page 1";
            case 1: //Page number 2
                return "Page 2";
            case 2: //Page number 3
                return "Map";
            case 3: //Page number 1
                return "Page 3";
            case 4: //Page number 2
                return "Page 4";
            default:
                return null;
        }
    }
}
