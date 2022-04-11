package txks.fingerpaint;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

//This is the view that contains multiple rows.
//I don't have an xml for this yet, but honestly, a vertical linearLayout or a ListView might be all I need.
//Still working on that one.
public class LayerMenuView extends View {
    public LayerMenuView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LayerMenuView(Context context) {
        this(context, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {

    }


}
