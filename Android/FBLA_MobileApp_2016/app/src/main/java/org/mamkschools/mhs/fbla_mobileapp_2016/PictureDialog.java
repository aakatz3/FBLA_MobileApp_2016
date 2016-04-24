package org.mamkschools.mhs.fbla_mobileapp_2016;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Displays a larger version of the picture
 * Created by Andrew A. Katz on 4/17/2016.
 */
public class PictureDialog extends Dialog implements View.OnClickListener{
    private Bitmap image;
    private String imageTitleText;
    public PictureDialog(Context context, Bitmap image, String imageTitle) {
        super(context);
        this.image = image;
        this.imageTitleText = imageTitle;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_picture);
        final Window window = getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setBackgroundDrawable(new ColorDrawable(Color.argb(0x80,0,0,0)));
    }

    public void show(){
        super.show();

        if(image == null) return;

        ImageView bigPicture = (ImageView) findViewById(R.id.big_picture);
        TextView imageTitle = (TextView) findViewById(R.id.picture_label);
        FrameLayout imageLayout = (FrameLayout) findViewById(R.id.big_picture_frame);
        bigPicture.setImageBitmap(image);
        imageTitle.setText(imageTitleText);
        imageLayout.setOnClickListener(this);
        Toast.makeText(getContext(), R.string.tap_exit, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}
