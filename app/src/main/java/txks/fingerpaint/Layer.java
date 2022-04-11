package txks.fingerpaint;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Layer {
    public Bitmap bmp;
    public String name;
    public int opacity;
    public Canvas canvas;
    public Paint layerPaint;

    //Construct a layer.
    // TODO: For now, the rectangle used by the DrawingView sets the size of the bitmap, so I'll need to make that more robust in the future.
    public Layer(String name, Rect baseRect){
        this.name = name;
        opacity = 255;
        bmp = Bitmap.createBitmap(baseRect.width(), baseRect.height(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bmp);
        layerPaint = new Paint(Paint.DITHER_FLAG);
    }

    public void setOpacity(int opacity){
        //simple bounds check
        this.opacity = opacity;
        if (opacity > 255){
            opacity = 255;
        } else if (opacity < 0){
            opacity = 0;
        }
        layerPaint.setAlpha(opacity);
    }

    public String toString(){
        return name + "\t" + opacity + "%";
    }
}
