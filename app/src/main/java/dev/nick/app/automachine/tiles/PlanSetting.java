package dev.nick.app.automachine.tiles;

import android.content.Context;

import java.util.List;

import dev.nick.tiles.tile.Category;
import dev.nick.tiles.tile.DashboardFragment;

public class PlanSetting extends DashboardFragment {

    private Callback mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (Callback) getActivity();
    }

    @Override
    protected void onCreateDashCategories(List<Category> categories) {

        super.onCreateDashCategories(categories);

        Category toggle = new Category();
        toggle.addTile(new PlanNameTile(getContext(), mCallback));
        toggle.addTile(new PlanCntTile(getContext(), mCallback));
        categories.add(toggle);

        Category packages = new Category();
        packages.title = "Actions";
        categories.add(packages);
    }

    public interface Callback extends PlanNameTile.Callback, PlanCntTile.Callback {
    }

}