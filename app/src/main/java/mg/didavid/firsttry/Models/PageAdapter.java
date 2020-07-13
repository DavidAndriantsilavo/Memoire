package mg.didavid.firsttry.Models;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import mg.didavid.firsttry.Controllers.Fragments.GMapFragment;
import mg.didavid.firsttry.Controllers.Fragments.ActuFragment;
import mg.didavid.firsttry.Controllers.Fragments.RestoFragment;
import mg.didavid.firsttry.Controllers.Fragments.ParametreFragment;
import mg.didavid.firsttry.Controllers.Fragments.MessageFragment;

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
                return RestoFragment.newInstance();
            case 2: //Page number 3
                return GMapFragment.newInstance();
            case 3: //Page number 3
                return ParametreFragment.newInstance();
            case 4: //Page number 3
                return MessageFragment.newInstance();
            default:
                return null;
        }
    }
}