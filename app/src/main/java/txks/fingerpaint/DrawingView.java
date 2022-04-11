package txks.fingerpaint;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

//
//First, I need to change the buttons change the mode
//
//I can edit this to allow multiple line strokes, but for now, the pinch mode affects the stroke width
//
//Throwing a size/color indicator at the top would be nice
//
//
public class DrawingView extends View{
    public boolean kidMode = false;
    public int toggleCount = 0;
    //public boolean circleMode = true; public boolean drawMode = false;
    public boolean circleMode = false; public boolean drawMode = true;

    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private int paintColor = 0x60FFFFFF;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap; //TODO: Remove canvasBitmap after layers are in place

    //private BitmapDrawable tiles;
    //private Canvas baseCanvas;
    public Rect baseRect;
    private Paint basePaint;

    private float strokeWidth = 20f;
    private float circleSize = 110;
    private float prevX = -1f;
    private float prevY = -1f;

    //private ArrayList touches;
    //private HashMap touchLineMap;
    //private static final int SIZE = 60;
    private SparseArray<PointF> mActivePointers;
    private int[] colors = {Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED, Color.GRAY, Color.DKGRAY, Color.LTGRAY, Color.YELLOW, Color.BLACK,};

    //private Paint mPaint;
    private Paint textPaint;

    private float strokeScale = 1.0f;
    private ScaleGestureDetector scaleGestureDetector;
    public boolean scaling = false;

    //Layers////////////////////////////////////////////////////////////////////////////////////////
    public ArrayList<Layer> layers;
    public int activeLayer = 0;
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupDrawing(context);
    }

    public void setPaintColor(int color){
        paintColor = color;
        drawPaint.setColor(paintColor);
    }

    private void setupDrawing(Context context){
        drawPath = new Path();
        mActivePointers = new SparseArray<PointF>();

        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(strokeWidth);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);

        //touchLineMap = new HashMap<Integer, Path>();
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

        Bitmap tilePattern = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.tiles);
        basePaint = new Paint();
        basePaint.setShader(new BitmapShader(tilePattern, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
        baseRect = new Rect(0, 0, getRight(), getBottom());

        //Layers////////////////////////////////////////////////////////////////////////////////////
        layers = new ArrayList<Layer>();
        ////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        baseRect = new Rect(0, 0, w, h);

        //if (kidMode == false) {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            drawCanvas = new Canvas(canvasBitmap);

            //Layers////////////////////////////////////////////////////////////////////////////////
            //TODO: Remove example layers
            layers.add(new Layer("Example 0", baseRect));
            layers.get(0).setOpacity(255);

            //Layers////////////////////////////////////////////////////////////////////////////////


        //}
        //else{
            //baseRect = new Rect(0, 0, getRight(), getBottom());
            //baseRect = new Rect(0, 0, w, h);
            //drawCanvas.drawBitmap(canvasBitmap, null, baseRect, canvasPaint);
        //}

    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawPaint.setStrokeWidth(strokeWidth * strokeScale);

        baseRect = new Rect(0, 0, getRight(), getBottom());
        if (!kidMode) {
            canvas.drawRect(baseRect, basePaint);
        }
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        //Layers////////////////////////////////////////////////////////////////////////////////////
        for (Layer l : layers){
            canvas.drawBitmap(l.bmp, 0, 0,l.layerPaint);
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        canvas.drawPath(drawPath, drawPaint);

        super.onDraw(canvas);
        for (int size = mActivePointers.size(), i=0; i < size; i++){
            PointF point = mActivePointers.valueAt(i);
            if (point != null) {
                //Log.i("Drawing", i + 1 + " of " + size);
                drawPaint.setColor(colors[i]);
                canvas.drawCircle(point.x, point.y, circleSize * strokeScale, drawPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        toggleCount = 0;
        scaleGestureDetector.onTouchEvent(event);
        if (circleMode) {
            return circleModeTouchEvent(event);
        }
        else if (drawMode) {
            return drawModeTouchEvent(event);
        } else return false;

    }

    private boolean circleModeTouchEvent(MotionEvent event){
        //Log.i("Event.getAction ", event.getAction() + "");
        int pointerIndex = event.getActionIndex();
        int id = event.getPointerId(pointerIndex);

        //Get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();

        float touchX = event.getX();
        float touchY = event.getY();

        //Log.i(id + "", ": " + touchX + ",  " + touchY);
        //Log.i("before", touchLineMap.size() + "");
        switch (event.getAction()){
            case MotionEvent.ACTION_POINTER_2_DOWN:  //... Look into implementing the mask suggested
            case MotionEvent.ACTION_POINTER_3_DOWN:  //... Look into implementing the mask suggested
            case 773:          //4th pointer up on Fire... Look into implementing the mask suggested
            case 1029:         //5th pointer up on Fire... Look into implementing the mask suggested
            case MotionEvent.ACTION_DOWN:
                //Log.i("case", "ACTION_DOWN");
                //if (!touchLineMap.containsKey(id)){
                //    touchLineMap.put(id, new Path());
                //    path(id).moveTo(touchX, touchY);
                //}
                //break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //Log.i("case", "ACTION_DOWN or ACTION_POINTER_DOWN");
                PointF f = new PointF();
                f.x = event.getX(pointerIndex);
                f.y = event.getY(pointerIndex);
                mActivePointers.put(id, f);
                break;
            case MotionEvent.ACTION_MOVE:
                //Log.i("Number of pointers :" ,"" + event.getPointerCount());
                for (int size = event.getPointerCount(), i = 0; i < size; i++){
                    PointF point = mActivePointers.get(event.getPointerId(i));
                    if (point != null){
                        point.x = event.getX(i);
                        point.y = event.getY(i);
                    }
                }
                break;
            case 1030:        //5th pointer up on Fire... Look into implementing the mask suggested
            case 774:         //4th pointer up on Fire... Look into implementing the mask suggested
            case MotionEvent.ACTION_POINTER_3_UP:   //... Look into implementing the mask suggested
            case MotionEvent.ACTION_POINTER_2_UP:   //... Look into implementing the mask suggested
            case MotionEvent.ACTION_UP:
                //Log.i("case", "ACTION_UP");
                //for (int i = 0; i < touchLineMap.size(); i++){
                //    if (touchLineMap.containsKey(id)){
                //        touchLineMap.remove(id);
                //    }
                //}
                //break;
            case MotionEvent.ACTION_POINTER_UP:
                //Log.i("case", "ACTION_POINTER_UP");
                //for (int i = 0; i < touchLineMap.size(); i++){
                //    if (touchLineMap.containsKey(id)){
                //        touchLineMap.remove(id);
                //    }
               // }
                //break;
            case MotionEvent.ACTION_CANCEL:
                //Log.i("case", "ACTION_CANCEL, ACTION_UP, or ACTION_POINTER_UP");
                mActivePointers.remove((id));
                break;
            default :
                return false;
        }
        //Log.i("after", touchLineMap.size() + "");
        invalidate();
        return true;
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent event) {*/
    private boolean drawModeTouchEvent(MotionEvent event){

        //detect user touch
        float touchX = event.getX();
        float touchY = event.getY();
        float pressure = event.getPressure();

        //Log.d("debug", "touch=(" + touchX + "," + touchY + ")");

        if (event.getPointerId(event.getActionIndex()) == mActivePointers.keyAt(0)){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    drawPath.moveTo(touchX, touchY);
                    prevX = touchX;
                    prevY = touchY;
                    break;
                case MotionEvent.ACTION_MOVE:
                    // Only one pressure is saved for the whole line this way,
                    // which isn't great when you consider it approaches 0 as you lift.
                    // Also doesn't work with fingers.
                    //drawPaint.setStrokeWidth(strokeWidth * pressure);

                    //drawPath.quadTo(prevX, prevY, (touchX + prevX)/2, (touchY + prevY)/2);
                    //drawPath.quadTo(prevX, prevY, touchX, touchY);
                    if (!scaling) {
                        if (prevX > 0 && prevY > 0) {
                            drawPath.quadTo(prevX, prevY, touchX, touchY);
                        } else {
                            drawPath.lineTo(touchX, touchY);
                        }
                        prevX = touchX;
                        prevY = touchY;
                    }
                    //drawPath.lineTo(touchX, touchY);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL:
                    //drawCanvas.drawPath(drawPath, drawPaint);
                    //LAYERS////////////////////////////////////////////////////////////////////////
                    if (checkActiveLayerIndex()) {
                        layers.get(activeLayer).canvas.drawPath(drawPath, drawPaint);
                    }
                    else{
                        Log.e("drawModeTouchEvent", "Not drawing line due to active layer pointer error.  Try again?");
                    }
                    //LAYERS////////////////////////////////////////////////////////////////////////
                    drawPath.reset();
                    prevX = -1f;
                    prevY = -1f;
                    scaling = false;

                    break;
                default:
                    return false;
            }
        }

        invalidate();
        return true;
    }

    private boolean checkActiveLayerIndex(){
        if (layers.size() < 1){
            Log.e("checkActiveLayerIndex", "LAYER SIZE ERROR: No layers found. Setting to -1");
            activeLayer = -1;
            return false;
        }
        else if (activeLayer >= layers.size()) {
            Log.e("checkActiveLayerIndex", "LAYER SIZE ERROR: Active Layer Index higher than number of layers. Setting to 0");
            activeLayer = 0;
            return false;
        }
        else{
            return true;
        }
    }

    public void cycleLayersUp(){
        activeLayer++;
        if (activeLayer >= layers.size()) {
            activeLayer = 0;
        }
    }

    //swap the active layer with the one above it
    public void moveLayerUp(){
        Log.d("moveLayerUp", "Active Layer = " + activeLayer + ": " + layers.get(activeLayer).name);
        if (activeLayer < layers.size() - 1) {
            Layer tmp = layers.get(activeLayer);
            layers.set(activeLayer, layers.get(activeLayer + 1));
            layers.set(activeLayer + 1, tmp);
            activeLayer++;
        }
        Log.d("moveLayerUp", "Active Layer = " + activeLayer + ": " + layers.get(activeLayer).name);
    }

    //swap the active layer with the one below it
    public void moveLayerDown(){
        Log.d("moveLayerDown", "Active Layer = " + activeLayer + ": " + layers.get(activeLayer).name);
        if (activeLayer > 0) {
            Layer tmp = layers.get(activeLayer);
            layers.set(activeLayer, layers.get(activeLayer - 1));
            layers.set(activeLayer - 1, tmp);
            activeLayer--;
        }
        Log.d("moveLayerDown", "Active Layer = " + activeLayer + ": " + layers.get(activeLayer).name);
    }
    public void deleteLayer(Context applicationContext){
        if (layers.size() > 1) {
            Log.d("deleteLayer", "Active Layer = " + activeLayer + ": " + layers.get(activeLayer).name);
            layers.remove(activeLayer);
            if (activeLayer >= layers.size()) {
                activeLayer = layers.size() - 1;
            }
            Log.d("deleteLayer", "Active Layer = " + activeLayer + ": " + layers.get(activeLayer).name);
        }
        else {
            Toast notDoingItToast = Toast.makeText(applicationContext,
                    "I'm not deleting your only layer, man.  Come on...", Toast.LENGTH_SHORT);
            notDoingItToast.show();
        }
    }

    /*public void setupLayerMenu(){
        LinearLayout ll = findViewById(R.id.layer_layout);
        ll.removeAllViews();
        for (int i = 0; i < layers.size(); i++){
            TextView tv = new TextView(getContext());
            tv.setText(i + ") " + layers.get(i).name);
            ll.addView(tv);
        }
    }*/

    private void startPath(float x, float y){
        drawPath.moveTo(x, y);
    }

    private void endPath(){
        drawCanvas.drawPath(drawPath, drawPaint);
        drawPath.reset();
    }

    private void drawLine(float x, float y){
        //Log.i("DrawLine", x + "," + y);
        //Path newPath = new Path();
        if (prevX > 0 && prevY > 0) {
            drawPath.quadTo(prevX, prevY, x, y);
        }
        else{
            drawPath.lineTo(x, y);
        }
        prevX = x;
        prevY = y;
    }

    //private void path(int id){
    //    drawPath.quadTo(prevX, prevY, touchX, touchY);
    //    prevX = touchX;
    //    prevY = touchY;
    //}

    public void setErase(boolean erasing){
        Log.d("debug", "in setErase() with erasing = " + erasing);
        if(erasing) {
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        }
        else {
            drawPaint.setXfermode(null);
        }
    }

    public void toggleKidMode(LinearLayout top_menu){
        if (kidMode) {
            kidMode = false;
            top_menu.setVisibility(LinearLayout.VISIBLE);
        } else {
            kidMode = true;
            top_menu.setVisibility(LinearLayout.INVISIBLE);
            toggleCount = 0;
        }
        invalidate();

    }

    public void newFile(Context applicationContext){
        layers.clear();
        layers.add(new Layer("New Drawing", baseRect));
        activeLayer = 0;

        invalidate();
    }

    //Setup the directory if it doesn't exist, and return the name of the full path.
    private String directoryName(){
        String fullPath = Environment.getExternalStorageDirectory().toString() + getContext().getResources().getString(R.string.directory);
        File dir = new File(fullPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return fullPath;
    }

    //use createCompositeImage() to combine the layers into a bitmap, and save that file.
    public void saveFile(ContentResolver cr, Context applicationContext){
        try {
            String path = directoryName();
            OutputStream fOut = null;
            String filename = "Fingerpaint" + new Date().getTime() + ".png";
            File file = new File(path, filename);
            fOut = new FileOutputStream(file);

            createCompositeImage().compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();

            //MediaStore.Images.Media.InsertImage(file.getAbsolutePath(),file.getName(),file.getName());
            String imgSaved = MediaStore.Images.Media.insertImage(cr, file.getAbsolutePath(),file.getName(),file.getName());
            if(imgSaved!=null){
                Toast savedToast = Toast.makeText(applicationContext,
                    "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                savedToast.show();
            }
            else{
                Toast unsavedToast = Toast.makeText(applicationContext,
                        "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                unsavedToast.show();
            }
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            Toast fnfeToast = Toast.makeText(applicationContext,
                    "FILE NOT FOUND:  Check permissions or something, I dunno.", Toast.LENGTH_SHORT);
            fnfeToast.show();
        } catch (IOException ioe){
            ioe.printStackTrace();
            Toast ioeToast = Toast.makeText(applicationContext, "IO Exception.  I don't know what to tell you here...", Toast.LENGTH_SHORT);
            ioeToast.show();
        }
    }

    //Add all of the layers onto a canvas, and use that to return a bitmap.  Note that a similar technique can be used for merging.
    private Bitmap createCompositeImage(){
        Bitmap result = Bitmap.createBitmap(layers.get(0).bmp.getWidth(), layers.get(0).bmp.getHeight(), layers.get(0).bmp.getConfig());
        Canvas canvas = new Canvas(result);
        for (Layer l : layers){
            canvas.drawBitmap(l.bmp, 0f, 0f, l.layerPaint);
        }
        return result;
    }

    private class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            //Log.i("strokeScale", strokeScale + "");
            strokeScale *= detector.getScaleFactor();

            // don't let the object get too small or too large.
            strokeScale = Math.max(0.2f, Math.min(strokeScale, 3.0f));

            if (drawMode){
                drawPath.reset();
                scaling = true;
            }

            invalidate();
            return true;
        }
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            //Log.i("strokeScale", "On Scale Begin");
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            //Log.i("strokeScale", "On Scale End");
        }
    }
}
