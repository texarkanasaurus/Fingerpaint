package txks.fingerpaint;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.slider.Slider;

// This should be the individual row.  Each layer gets one of these.
// See layer_row.xml
public class LayerRowView extends View {
    String name;
    int opacity;

    public LayerRowView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
    }

    public LayerRowView(Context context){
        this(context, null);
    }

    public LayerRowView(Context context, String name, int opacity){
        this(context, null);
        this.name = name;
        this.opacity = opacity;
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        TextView nameView = findViewById(R.id.text_view);
        Slider opacitySlider = findViewById(R.id.opacity_slider);

        nameView.setText(name);
        opacitySlider.setValue(opacity);
    }
}
