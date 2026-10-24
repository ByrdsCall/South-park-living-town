package android.widget;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
// Desktop-mirror shim (internal QA tooling only).
public class ImageView extends View {
    public enum ScaleType { MATRIX, FIT_XY, FIT_START, FIT_CENTER, FIT_END, CENTER, CENTER_CROP, CENTER_INSIDE }
    private Drawable drawable;
    private Bitmap bitmap;
    private ScaleType scaleType = ScaleType.FIT_CENTER;
    public ImageView(Context context) { super(context); }
    public ImageView(Context context, AttributeSet attrs) { super(context, attrs); }
    public void setImageBitmap(Bitmap bm) { bitmap = bm; drawable = null; }
    public void setImageDrawable(Drawable d) { drawable = d; bitmap = null; }
    public void setImageResource(int resId) {}
    public void setImageURI(Uri uri) {}
    public Drawable getDrawable() { return drawable; }
    public Bitmap getBitmapForMirror() { return bitmap; }
    public ScaleType getScaleType() { return scaleType; }
    public void setScaleType(ScaleType st) { scaleType = st; }
    private boolean adjustViewBounds = false;
    public void setAdjustViewBounds(boolean b) { adjustViewBounds = b; }
    public boolean getAdjustViewBounds() { return adjustViewBounds; }
    public void setColorFilter(int color) {}
    public void setColorFilter(ColorFilter cf) {}
    public void setMaxWidth(int w) {}
    public void setMaxHeight(int h) {}
    public void setCropToPadding(boolean b) {}
    public void setContentDescription2(CharSequence cd) { setContentDescription(cd); }
}
