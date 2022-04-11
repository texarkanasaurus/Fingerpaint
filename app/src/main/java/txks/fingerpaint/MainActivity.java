package txks.fingerpaint;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.slider.Slider;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
//public class MainActivity extends AppCompatActivity {
    private boolean startInKidMode = true;
    private DrawingView drawView;
    private LayerMenuView layerMenuView;
    private int screenHeight;
    private int screenWidth;
    private float topThird;
    private float bottomThird;

    //private ImageButton colorPicker;
    private Slider rSlider;
    private Slider gSlider;
    private Slider bSlider;
    private Slider aSlider;
    private boolean setColor = false;

    private float rValue = 255f;
    private float gValue = 255f;
    private float bValue = 255f;
    private float aValue = 60f;

    private boolean erasing = false;
    private ImageButton erase_btn;

    private LinearLayout top_menu;

    Dialog layersDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawView = (DrawingView)findViewById(R.id.drawing);
        //layerMenuView = (LayerMenuView)findViewById(R.id.layer_menu);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;
        topThird = screenHeight / 3;
        bottomThird = (screenHeight / 3) * 2;
        Log.d("debug", "screenHeight = " + screenHeight);

        erase_btn = (ImageButton)findViewById(R.id.erase_btn);
        top_menu = (LinearLayout)findViewById(R.id.top_menu);

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                if (drawView.toggleCount++ > 9) {
                    drawView.toggleKidMode(top_menu);
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        if (startInKidMode) {
            drawView.toggleKidMode(top_menu);
        }

        layersDialog = new Dialog(this);
        layersDialog.setTitle("Layers");
        layersDialog.setContentView(R.layout.layers_menu);
    }


    public void layersClicked(View view) {
        Log.d("debug", "layersClicked");
        layersDialog.show();

        ArrayAdapter adapter = new ArrayAdapter<Layer>(this, R.layout.temp_layer_row, drawView.layers); //This is what layer_row.xml is for.

        ListView listView = (ListView) layersDialog.findViewById(R.id.layer_listview);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> listView, View itemView, int itemPosition, long itemId)
            {
                for (int i = 0; i < listView.getCount(); i++){
                    listView.getChildAt(i).setBackgroundColor(0xFF222222);
                }
                itemView.setBackgroundColor(0xFF888888);
                drawView.activeLayer = itemPosition;
            }
        });
        //drawView.setupLayerMenu();

        //TODO: Probably need to attach listeners here.  See colorClicked() a couple methods down
    }
    public void addLayerClicked(View view){
        Log.i("LayerMenuView", "addLayerClicked");
        String name = "New Layer";
        drawView.layers.add(new Layer(name, drawView.baseRect));
        layersDialog.dismiss();
        layersDialog.show();
    }
    public void deleteLayerClicked(View view){
        Log.i("LayerMenuView", "deleteLayerClicked");
        drawView.deleteLayer(getApplicationContext());
        drawView.invalidate();
        layersDialog.dismiss();
        layersDialog.show();
    }
    public void layerUpClicked(View view){
        Log.i("LayerMenuView", "layerUpClicked");
        drawView.moveLayerUp();
        layersDialog.dismiss();
        layersDialog.show();
    }
    public void layerDownClicked(View view){
        Log.i("LayerMenuView", "layerDownClicked");
        drawView.moveLayerDown();
        layersDialog.dismiss();
        layersDialog.show();
    }


    public void eraseClicked(View view){
        Log.d("debug", "eraseClicked, erasing being set to " + !erasing);
        if (!erasing){
            erasing = true;
            erase_btn.setBackgroundColor(0xFFFFFFFF);
        } else{
            erasing = false;
            erase_btn.setBackgroundColor(0xFF888888);
        }
        drawView.setErase(erasing);
    }

    public void colorClicked(View view){
        Log.d("debug", "colorClicked");
        final Dialog brushDialog = new Dialog(this);
        brushDialog.setTitle("Brush Options");
        brushDialog.setContentView(R.layout.color_picker);

        rSlider = (Slider) brushDialog.findViewById(R.id.rSlider);
        gSlider = (Slider) brushDialog.findViewById(R.id.gSlider);
        bSlider = (Slider) brushDialog.findViewById(R.id.bSlider);
        aSlider = (Slider) brushDialog.findViewById(R.id.aSlider);

        rSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                assignColor();
            }
        });
        rSlider.setValue(rValue);
        gSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                assignColor();
            }
        });
        gSlider.setValue(gValue);
        bSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                assignColor();
            }
        });
        bSlider.setValue(bValue);
        aSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                assignColor();
            }
        });
        aSlider.setValue(aValue);
        brushDialog.show();
        setColor = true;
    }

    public void newClicked(View view){
        Log.d("debug", "newClicked");
        //drawView.newFile();
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setTitle("Destroy Drawing");
        newDialog.setMessage("Blow up all of your hard work and start over?!?");
        newDialog.setPositiveButton("Yes, Chaos Is King", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                drawView.newFile(getApplicationContext());
                dialog.dismiss();
            }
        });
        newDialog.setNegativeButton("No, I Didn't Think This Through", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });
        newDialog.show();
    }

    public void saveClicked(View view){
        Log.d("debug", "saveClicked");
        checkStoragePermissions();
        AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
        saveDialog.setTitle("Save drawing");
        saveDialog.setMessage("Save drawing to device Gallery?");
        saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                drawView.saveFile(getContentResolver(), getApplicationContext());
            }
        });
        saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });
        saveDialog.show();
    }

    public void babyClicked(View view){
        Log.d("debug", "saveClicked");
        //drawView.kidMode = true;
        //top_menu.setVisibility(LinearLayout.GONE);
        //top_menu.setVisibility(LinearLayout.INVISIBLE);
        drawView.toggleKidMode(top_menu);
    }



    @Override
    public void onClick(View view) {
        Log.d("debug", "onClick");
        if (view.getId() == R.id.drawing) {
            //drawing clicked
            if (setColor){
                assignColor();
            }
        }
    }

    private void assignColor(){
        Log.d("assignColor", "TODO: implement");
        String hex = "#";
        String aStr = Integer.toHexString((int)aSlider.getValue());
        String rStr = Integer.toHexString((int)rSlider.getValue());
        String gStr = Integer.toHexString((int)gSlider.getValue());
        String bStr = Integer.toHexString((int)bSlider.getValue());
        if (aStr.length() == 1)
            aStr = "0" + aStr;
        if (rStr.length() == 1)
            rStr = "0" + rStr;
        if (gStr.length() == 1)
            gStr = "0" + gStr;
        if (bStr.length() == 1)
            bStr = "0" + bStr;
        hex += aStr + rStr + gStr + bStr;
        Log.d("assignColor", hex);
        drawView.setPaintColor(Color.parseColor(hex));

        rValue = rSlider.getValue();
        gValue = gSlider.getValue();
        bValue = bSlider.getValue();
        aValue = aSlider.getValue();
    }

    public void checkStoragePermissions(){
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 1);
    }

    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
        else {
            Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }
}