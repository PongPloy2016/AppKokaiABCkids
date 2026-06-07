package com.pongploydev.priyankvex.paintapp.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pongploydev.priyankvex.paintapp.drawingView.DrawingView;
import com.pongploydev.priyankvex.paintapp.confixdata.File_Confix_Data;
import com.pongploydev.priyankvex.paintapp.R;

import java.util.UUID;


public class MainActivity4 extends AppCompatActivity implements View.OnClickListener{
    int[] ButtonColors = {Color.parseColor("#eeeeee"),Color.parseColor("#ffffff")};
    private DrawingView mDrawingView;
    private ImageButton currPaint, drawButton, eraseButton, newButton, saveButton;
    private float smallBrush, mediumBrush, largeBrush;
    private int Image_next_black=0;
    private MediaPlayer playimgButton;
    private MediaPlayer playimgS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thai_letters);



        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, ButtonColors);
        gradientDrawable.setCornerRadius(0f);

        playimgButton = MediaPlayer.create(MainActivity4.this, R.raw.button);


        mDrawingView = (DrawingView)findViewById(R.id.drawing);
        Drawable myDrawable = getResources().getDrawable(File_Confix_Data.imgMenu4[Image_next_black]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mDrawingView.setBackground(myDrawable);
        }
        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
        // 0th child is white color, so selecting first child to give black as initial color.
        currPaint = (ImageButton)paintLayout.getChildAt(1);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.pallet_pressed));
        drawButton = (ImageButton) findViewById(R.id.buttonBrush);

        drawButton.setOnClickListener(this);
        eraseButton = (ImageButton) findViewById(R.id.buttonErase);

        eraseButton.setOnClickListener(this);
        newButton = (ImageButton) findViewById(R.id.buttonNew);

        newButton.setOnClickListener(this);
        saveButton = (ImageButton) findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(this);

        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);

        // Set the initial brush size
        mDrawingView.setBrushSize(mediumBrush);

        ImageButton smallBtn = (ImageButton) findViewById(R.id.button_black);

        smallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playimgButton.start();
                if(Image_next_black>0){
                    Image_next_black--;
                    File_Confix_Data.position_id_vdo1=Image_next_black;
                    mDrawingView.setBrushSize(Image_next_black);
                    Drawable myDrawable = getResources().getDrawable(File_Confix_Data.imgMenu4[Image_next_black]);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mDrawingView.setBackground(myDrawable);
                    }
                    mDrawingView.startNew();


                }else{
                    Toast savedToast = Toast.makeText(getApplicationContext(), "รูปแรกแล้วครับ", Toast.LENGTH_SHORT);
                    savedToast.show();
                }

            }
        });

        ImageButton button_next = (ImageButton) findViewById(R.id.button_next);

        button_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playimgButton.start();
                if(Image_next_black<File_Confix_Data.imgMenu4.length-1){
                    Image_next_black++;
                    File_Confix_Data.position_id_vdo1=Image_next_black;
                    mDrawingView.setBrushSize(Image_next_black);
                    Drawable myDrawable = getResources().getDrawable(File_Confix_Data.imgMenu4[Image_next_black]);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mDrawingView.setBackground(myDrawable);
                    }
                    mDrawingView.startNew();
                }else{
                    Toast savedToast = Toast.makeText(getApplicationContext(),"รูปสุดท้ายแล้วครับ", Toast.LENGTH_SHORT);
                    savedToast.show();
                }

            }
        });
        ImageButton button_VDO = (ImageButton) findViewById(R.id.buttonVDO);
        button_VDO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playimgButton.start();
                Intent myIntent = new Intent(getApplicationContext(), VideoStream4.class);
               startActivity(myIntent);

            }
        });
        ImageButton button_sound = (ImageButton) findViewById(R.id.buttonsound);
        button_sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playimgS = MediaPlayer.create(MainActivity4.this, File_Confix_Data.imgMenu1MediaPlayer3[File_Confix_Data.position_id_vdo1]);
                playimgS.start();

            }
        });

    }

    /**
     * Method is called when color is clicked from pallet.
     * @param view ImageButton on which click took place.
     */
    public void paintClicked(View view){
        if (view != currPaint){
            // Update the color
            ImageButton imageButton = (ImageButton) view;
            String colorTag = imageButton.getTag().toString();
            mDrawingView.setColor(colorTag);
            // Swap the backgrounds for last active and currently active image button.
            imageButton.setImageDrawable(getResources().getDrawable(R.drawable.pallet_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.pallet));
            currPaint = (ImageButton)view;
            mDrawingView.setErase(false);
            mDrawingView.setBrushSize(mDrawingView.getLastBrushSize());
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){
            case R.id.buttonBrush:
                // Show brush size chooser dialog
                playimgButton.start();
                showBrushSizeChooserDialog();
                break;
            case R.id.buttonErase:
                // Show eraser size chooser dialog
                showEraserSizeChooserDialog();
                break;
            case R.id.buttonNew:
                // Show new painting alert dialog
                playimgButton.start();
                showNewPaintingAlertDialog();
                break;
            case R.id.buttonSave:
                // Show save painting confirmation dialog.
                playimgButton.start();
                showSavePaintingConfirmationDialog();
                break;
        }
    }

    private void showBrushSizeChooserDialog(){
        final Dialog brushDialog = new Dialog(this);
        brushDialog.setContentView(R.layout.dialog_brush_size);
        brushDialog.setTitle("Brush size:");
        ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
        smallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawingView.setBrushSize(smallBrush);
                mDrawingView.setLastBrushSize(smallBrush);
                brushDialog.dismiss();
            }
        });
        ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
        mediumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawingView.setBrushSize(mediumBrush);
                mDrawingView.setLastBrushSize(mediumBrush);
                brushDialog.dismiss();
            }
        });

        ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
        largeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mDrawingView.setBrushSize(largeBrush);
                mDrawingView.setLastBrushSize(largeBrush);
                brushDialog.dismiss();
            }
        });
        mDrawingView.setErase(false);
        brushDialog.show();
    }

    private void showEraserSizeChooserDialog(){
        final Dialog brushDialog = new Dialog(this);
        brushDialog.setTitle("Eraser size:");
        brushDialog.setContentView(R.layout.dialog_brush_size);
        ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
        smallBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mDrawingView.setErase(true);
                mDrawingView.setBrushSize(smallBrush);
                brushDialog.dismiss();
            }
        });
        ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
        mediumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawingView.setErase(true);
                mDrawingView.setBrushSize(mediumBrush);
                brushDialog.dismiss();
            }
        });
        ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
        largeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawingView.setErase(true);
                mDrawingView.setBrushSize(largeBrush);
                brushDialog.dismiss();
            }
        });
        brushDialog.show();
    }

    private void showNewPaintingAlertDialog(){
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setTitle("New drawing");
        newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
        newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mDrawingView.startNew();
                dialog.dismiss();
            }
        });
        newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        newDialog.show();
    }

    private void showSavePaintingConfirmationDialog(){
        AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
        saveDialog.setTitle("Save drawing");
        saveDialog.setMessage("Save drawing to device Gallery?");
        saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                //save drawing
                mDrawingView.setDrawingCacheEnabled(true);
                String imgSaved = MediaStore.Images.Media.insertImage(
                        getContentResolver(), mDrawingView.getDrawingCache(),
                        UUID.randomUUID().toString()+".png", "drawing");
                if(imgSaved!=null){
                    Toast savedToast = Toast.makeText(getApplicationContext(),
                            "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                    savedToast.show();
                }
                else{
                    Toast unsavedToast = Toast.makeText(getApplicationContext(),
                            "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                    unsavedToast.show();
                }
                // Destroy the current cache.
                mDrawingView.destroyDrawingCache();
            }
        });
        saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });
        saveDialog.show();
    }

}
