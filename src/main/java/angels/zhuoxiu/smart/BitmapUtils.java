package angels.zhuoxiu.smart;

import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.util.SparseArray;
import android.view.View;

@SuppressLint("NewApi")
public class BitmapUtils {
	static SparseArray<Drawable> maskArray = new SparseArray<Drawable>(), frameArray = new SparseArray<Drawable>();

	public static Bitmap convert(Context context, Bitmap srcBitmap, View containerView, int width, int height, int maskResId, int frameResId, boolean isGray) {
		if (srcBitmap == null) {
			return null;
		}
		Bitmap tempSrcBitmap = srcBitmap.copy(Bitmap.Config.ARGB_8888, true);
		if (isGray) {
			tempSrcBitmap = bitmap2Gray(tempSrcBitmap);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
			tempSrcBitmap.setHasAlpha(true);
		}
		Bitmap mutableBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Paint paint = new Paint();
		paint.setFilterBitmap(false);

		Drawable mask = maskArray.get(maskResId), frame = frameArray.get(frameResId);
		if (maskResId != 0 && mask == null) {
			try {
				mask = context.getResources().getDrawable(maskResId);
				maskArray.put(maskResId, mask);
			} catch (NotFoundException e) {
				e.printStackTrace();
				return srcBitmap;
			}
		}
		if (frameResId != 0 && frame == null) {
			frame = context.getResources().getDrawable(frameResId);
			frameArray.put(frameResId, frame);
			if (frame != null && containerView != null) {
				frame.setBounds(0, 0, containerView.getRight() - containerView.getLeft(), containerView.getBottom() - containerView.getTop());
			}
		}
		
		Canvas canvas = new Canvas(mutableBitmap);
		if (mask != null) {
				if (containerView != null) {
					mask.setBounds(containerView.getLeft(), containerView.getTop(), containerView.getRight() - containerView.getLeft(),
							containerView.getBottom() - containerView.getTop());
				}else{
					mask.setBounds(0,0,width,height);
				}
				Bitmap maskBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), maskResId), 100, 100, false);
				int miLeft = maskBitmap.getWidth(), miTop = maskBitmap.getHeight(), miRight = 0, miBottom = 0;
				for (int x = 0; x < maskBitmap.getWidth(); x++) {
					for (int y = 0; y < maskBitmap.getHeight(); y++) {
						if (maskBitmap.getPixel(x, y) != Color.TRANSPARENT) {
							miTop = Math.min(miTop, y);
							miLeft = Math.min(miLeft, x);
							break;
						}
					}
				}
				for (int x = maskBitmap.getWidth() - 1; x >= 0; x--) {
					for (int y = maskBitmap.getHeight() - 1; y >= 0; y--) {
						if (maskBitmap.getPixel(x, y) != Color.TRANSPARENT) {
							miBottom = Math.max(miBottom, y);
							miRight = Math.max(miRight, x);
							break;
						}

					}
				}
				int miWidth = miRight - miLeft;
				int miHeight = miBottom - miTop;
				int left = width * miLeft / maskBitmap.getWidth();
				int top = height * miTop / maskBitmap.getHeight();
				int size = Math.max(width * miWidth / maskBitmap.getWidth(), height * miHeight / maskBitmap.getHeight());
				canvas.drawBitmap(Bitmap.createScaledBitmap(tempSrcBitmap, size, size, true), left, top, paint);
				maskBitmap.recycle();
				if (mask instanceof BitmapDrawable) {
					((BitmapDrawable) mask).getPaint().setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
					((BitmapDrawable) mask).draw(canvas);
				} else if (mask instanceof NinePatchDrawable) {
					((NinePatchDrawable) mask).getPaint().setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
					((NinePatchDrawable) mask).draw(canvas);
				}
		} else {
			canvas.drawBitmap(Bitmap.createScaledBitmap(tempSrcBitmap, width, height, true), 0, 0, paint);
		}

		if (frame != null) {
			if (frame instanceof BitmapDrawable) {
				((BitmapDrawable) frame).draw(canvas);
			} else if (frame instanceof NinePatchDrawable) {
				((NinePatchDrawable) frame).draw(canvas);
			}
		}
		return mutableBitmap;
	}

	public static Bitmap bitmap2Gray(Bitmap bmSrc) {
		int width = bmSrc.getWidth();
		int height = bmSrc.getHeight();
		Bitmap bmpGray = null;
		bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
		Canvas c = new Canvas(bmpGray);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmSrc, 0, 0, paint);
		return bmpGray;
	}
}
