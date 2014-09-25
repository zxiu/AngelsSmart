package angels.zhuoxiu.smart;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class SmartImageView extends ImageView {
	String tag = this.getClass().getSimpleName();
	private static final int LOADING_THREADS = 4;
	private static ExecutorService threadPool = Executors.newFixedThreadPool(LOADING_THREADS);
	private SmartImageTask currentTask;

	int maskResId, frameResId;
	Drawable mask, frame;
	int maskPaddingLeft, maskPaddingTop, maskPaddingRight, maskPaddingBottom;

	@SuppressLint("NewApi")
	public SmartImageView(Context context) {
		this(context, null);
	}

	@SuppressLint("NewApi")
	public SmartImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@SuppressLint("NewApi")
	public SmartImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setLayerType(LAYER_TYPE_SOFTWARE, null);
		if (attrs != null) {
			TypedArray styled = context.obtainStyledAttributes(attrs, R.styleable.SmartImageView);
			maskResId = styled.getResourceId(R.styleable.SmartImageView_srcMaskImage, 0);
			frameResId = styled.getResourceId(R.styleable.SmartImageView_srcFrameImage, 0);
			styled.recycle();
		}
		setMaskFrameResource(maskResId, frameResId);
	}

	public void setMaskFrameResource(int maskResId, int frameResId) {
		if (this.maskResId != maskResId) {
			mask = null;
		}
		if (this.frameResId != frameResId) {
			frame = null;
		}

		this.maskResId = maskResId;
		this.frameResId = frameResId;
		invalidate();
	}

	// Helpers to set image by URL
	public void setImageUrl(String url) {
		setImage(new WebImage(url));
	}

	public void setImageUrl(String url, SmartImageTask.OnCompleteListener completeListener) {
		setImage(new WebImage(url), completeListener);
	}

	public void setImageUrl(String url, final Integer fallbackResource) {
		setImage(new WebImage(url), fallbackResource);
	}

	public void setImageUrl(String url, final Integer fallbackResource, SmartImageTask.OnCompleteListener completeListener) {
		setImage(new WebImage(url), fallbackResource, completeListener);
	}

	public void setImageUrl(String url, final Integer fallbackResource, final Integer loadingResource) {
		setImage(new WebImage(url), fallbackResource, loadingResource);
	}

	public void setImageUrl(String url, final Integer fallbackResource, final Integer loadingResource, SmartImageTask.OnCompleteListener completeListener) {
		setImage(new WebImage(url), fallbackResource, loadingResource, completeListener);
	}

	// Helpers to set image by contact address book id
	public void setImageContact(long contactId) {
		setImage(new ContactImage(contactId));
	}

	public void setImageContact(long contactId, final Integer fallbackResource) {
		setImage(new ContactImage(contactId), fallbackResource);
	}

	public void setImageContact(long contactId, final Integer fallbackResource, final Integer loadingResource) {
		setImage(new ContactImage(contactId), fallbackResource, fallbackResource);
	}

	// Set image using SmartImage object
	public void setImage(final SmartImage image) {
		setImage(image, null, null, null);
	}

	public void setImage(final SmartImage image, final SmartImageTask.OnCompleteListener completeListener) {
		setImage(image, null, null, completeListener);
	}

	public void setImage(final SmartImage image, final Integer fallbackResource) {
		setImage(image, fallbackResource, fallbackResource, null);
	}

	public void setImage(final SmartImage image, final Integer fallbackResource, SmartImageTask.OnCompleteListener completeListener) {
		setImage(image, fallbackResource, fallbackResource, completeListener);
	}

	public void setImage(final SmartImage image, final Integer fallbackResource, final Integer loadingResource) {
		setImage(image, fallbackResource, loadingResource, null);
	}

	public void setImage(final SmartImage image, final Integer fallbackResource, final Integer loadingResource,
			final SmartImageTask.OnCompleteListener completeListener) {
		// Set a loading resource
		if (loadingResource != null) {
			setImageResource(loadingResource);
		}

		// Cancel any existing tasks for this image view
		if (currentTask != null) {
			currentTask.cancel();
			currentTask = null;
		}

		// Set up the new task
		currentTask = new SmartImageTask(getContext(), image);
		currentTask.setOnCompleteHandler(new SmartImageTask.OnCompleteHandler() {
			@Override
			public void onComplete(final Bitmap bitmap) {
				if (bitmap != null) {
					setImageBitmap(bitmap);
				} else {
					// Set fallback resource
					if (fallbackResource != null) {
						setImageResource(fallbackResource);
					}
				}

				if (completeListener != null) {
					completeListener.onComplete(bitmap);
				}
			}
		});

		// Run the task in a threadpool
		threadPool.execute(currentTask);
	}


	public static void cancelAllTasks() {
		threadPool.shutdownNow();
		threadPool = Executors.newFixedThreadPool(LOADING_THREADS);
	}

	Bitmap srcBitmap;
	private Drawable foregroundDrawable;
	static Path path;
	static Paint paint;
	static PaintFlagsDrawFilter mPaintFlagsDrawFilter;

	// @SuppressLint({ "DrawAllocation", "NewApi" })
	// @Override
	// protected void onDraw(Canvas canvas) {
	// super.onDraw(canvas);
	// }

	@SuppressLint({ "DrawAllocation", "NewApi" })
	@Override
	protected void onDraw(Canvas onDrawCanvas) {
		if (srcBitmap == null) {
			super.onDraw(onDrawCanvas);
		} else {
			srcBitmap.setHasAlpha(true);
			Bitmap mutableBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
			Paint paint = new Paint();
			paint.setFilterBitmap(false);

			Canvas canvas = new Canvas(mutableBitmap);
			if (maskResId != 0 || mask != null) {
				if (maskResId != 0 && mask == null) {
					try {
						mask = getResources().getDrawable(maskResId);
					} catch (NotFoundException e) {
						super.onDraw(onDrawCanvas);
						return;
					}
				}
				if (mask != null) {
					mask.setBounds(0, 0, getRight() - getLeft(), getBottom() - getTop());
					Bitmap maskBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), maskResId), 100, 100, false);
					int miLeft = maskBitmap.getWidth(), miTop = maskBitmap.getHeight(), miRight = 0, miBottom = 0;
					Date time0 = new Date();
					Log.i(tag, "left time 0 =" + (new Date().getTime() - time0.getTime()) + " maskBitmap.getWidth()=" + maskBitmap.getWidth());
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
					Log.i(tag, "left time 1 =" + (new Date().getTime() - time0.getTime()));
					int miWidth = miRight - miLeft;
					int miHeight = miBottom - miTop;
					int width = getWidth() * miWidth / maskBitmap.getWidth();
					int height = getHeight() * miHeight / maskBitmap.getHeight();
					int left = getWidth() * miLeft / maskBitmap.getWidth();
					int top = getHeight() * miTop / maskBitmap.getHeight();
					Log.i(tag, "left=" + left + " width=" + width + "  getWidth()=" + getWidth());
					int size=Math.max(width, height);
					canvas.drawBitmap(Bitmap.createScaledBitmap(srcBitmap, size, size, true), left, top, paint);
					maskBitmap.recycle();
					if (mask instanceof BitmapDrawable) {
						((BitmapDrawable) mask).getPaint().setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
						((BitmapDrawable) mask).draw(canvas);
					} else if (mask instanceof NinePatchDrawable) {
						((NinePatchDrawable) mask).getPaint().setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
						((NinePatchDrawable) mask).draw(canvas);
					}
				} else {
					canvas.drawBitmap(Bitmap.createScaledBitmap(srcBitmap, getWidth(), getHeight(), true), 0, 0, paint);
				}

			} else {
				canvas.drawBitmap(Bitmap.createScaledBitmap(srcBitmap, getWidth(), getHeight(), true), 0, 0, paint);
			}

			if (frameResId != 0 || frame != null) {
				if (frameResId != 0 && frame == null) {
					frame = getResources().getDrawable(frameResId);
					if (frame != null) {
						frame.setBounds(0, 0, getRight() - getLeft(), getBottom() - getTop());
					}
				}
				if (frame instanceof BitmapDrawable) {
					((BitmapDrawable) frame).draw(canvas);
				} else if (frame instanceof NinePatchDrawable) {
					((NinePatchDrawable) frame).draw(canvas);
				}
			}

			// final Drawable foreground = getForeground();
			// if (foreground != null) {
			// foreground.setBounds(0, 0, getRight() - getLeft(), getBottom() -
			// getTop());
			//
			// final int scrollX = getScrollX();
			// final int scrollY = getScrollY();
			//
			// if ((scrollX | scrollY) == 0) {
			// foreground.draw(canvas);
			// } else {
			// canvas.translate(scrollX, scrollY);
			// foreground.draw(canvas);
			// canvas.translate(-scrollX, -scrollY);
			// }
			// }
			onDrawCanvas.drawBitmap(mutableBitmap, 0, 0, paint);
		}
	}

	@Override
	public void setImageBitmap(Bitmap bitmap) {
		super.setImageBitmap(bitmap);
		srcBitmap = bitmap;
		invalidate();
	}

	private Drawable getForeground() {
		return foregroundDrawable;
	}

	public void setForegroundResource(int resId) {
		setForegroundDrawable(getContext().getResources().getDrawable(resId));
	}

	public void setForegroundDrawable(Drawable d) {
		d.setCallback(this);
		d.setVisible(getVisibility() == VISIBLE, false);

		foregroundDrawable = d;

		requestLayout();
		invalidate();
	}
}