package dev.nick.app.automachine.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.InputType;

import dev.nick.app.automachine.R;
import dev.nick.tiles.tile.EditTextTileView;
import dev.nick.tiles.tile.QuickTile;

public class PlanCntTile extends QuickTile {

    public PlanCntTile(@NonNull Context context, final Callback callback) {
        super(context, null);

        this.title = "Plan cnt";
        this.summary = "1";
        this.iconRes = R.drawable.ic_cake_black_24dp;

        this.tileView = new EditTextTileView(context) {
            @Override
            protected void onNegativeButtonClick() {
                super.onNegativeButtonClick();
            }

            @Override
            protected void onPositiveButtonClick() {
                super.onPositiveButtonClick();
                String c = getEditText().getText().toString();
                callback.onCnting(Integer.parseInt(c));
                getSummaryTextView().setText(c);
            }

            @Override
            protected int getInputType() {
                return InputType.TYPE_CLASS_NUMBER;
            }
        };
    }

    public interface Callback {
        void onCnting(int c);
    }
}
