package dev.nick.app.automachine.tiles;

import android.content.Context;
import android.support.annotation.NonNull;

import dev.nick.app.automachine.R;
import dev.nick.tiles.tile.EditTextTileView;
import dev.nick.tiles.tile.QuickTile;

public class PlanNameTile extends QuickTile {

    public PlanNameTile(@NonNull Context context, final Callback callback) {
        super(context, null);

        this.title = "Plan name";
        this.summary = "Undefined";
        this.iconRes = R.drawable.ic_place_black_24dp;

        this.tileView = new EditTextTileView(context) {
            @Override
            protected void onNegativeButtonClick() {
                super.onNegativeButtonClick();
            }

            @Override
            protected void onPositiveButtonClick() {
                super.onPositiveButtonClick();
                String name = getEditText().getText().toString();
                callback.onNaming(name);

                getSummaryTextView().setText(name);
            }
        };
    }

    public interface Callback {
        void onNaming(String name);
    }
}
