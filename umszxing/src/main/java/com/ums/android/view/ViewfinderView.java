/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ums.android.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.ums.android.camera.CameraManager;
import com.ums.umszxing.R;
import com.ums.zxing.ResultPoint;

import java.util.Collection;
import java.util.HashSet;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 *
 */
public final class ViewfinderView extends View {
	private static final String TAG = "log";
	private static final long ANIMATION_DELAY = 10L;
	private static final int OPAQUE = 0xFF;

	private int ScreenRate;


	private static final int CORNER_WIDTH = 10;

	private static final int MIDDLE_LINE_WIDTH = 6;


	private static final int MIDDLE_LINE_PADDING = 5;


	private static final int SPEEN_DISTANCE = 5;


	private static float density;


	private static final int TEXT_SIZE = 16;

	private static final int TEXT_PADDING_TOP = 30;

	private Paint paint;
	private int slideTop;

	private int slideBottom;
	private Bitmap resultBitmap;
	private final int maskColor;
	private final int resultColor;
	private final int statusColor;

	private final int resultPointColor;
	private Collection<ResultPoint> possibleResultPoints;
	private Collection<ResultPoint> lastPossibleResultPoints;

	boolean isFirst;

	public ViewfinderView(Context context, AttributeSet attrs) {
		super(context, attrs);

		density = context.getResources().getDisplayMetrics().density;
		ScreenRate = (int)(20 * density);

		paint = new Paint();
		Resources resources = getResources();
		maskColor = resources.getColor(R.color.viewfinder_mask);
		resultColor = resources.getColor(R.color.result_view);
		statusColor = resources.getColor(R.color.status_text);
		resultPointColor = resources.getColor(R.color.possible_result_points);
		possibleResultPoints = new HashSet<ResultPoint>(5);
		scanLight = BitmapFactory.decodeResource(resources,
				R.mipmap.zxing_ic_scan_light);
	}

	@Override
	public void onDraw(Canvas canvas) {
		Rect frame = CameraManager.get().getFramingRect();
		if (frame == null) {
			return;
		}

		if(!isFirst){
			isFirst = true;
			slideTop = frame.top;
			slideBottom = frame.bottom;
		}


		int width = canvas.getWidth();
		int height = canvas.getHeight();

		paint.setColor(resultBitmap != null ? resultColor : maskColor);

		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
				paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);



		if (resultBitmap != null) {
			// Draw the opaque result bitmap over the scanning rectangle
			paint.setAlpha(OPAQUE);
			canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
		} else {
			drawFrameBounds(canvas,frame);
			drawStatusText(canvas, frame, width);
			drawScanLight(canvas,frame);
//			paint.setColor(Color.BLUE);
//			canvas.drawRect(frame.left, frame.top, frame.left + ScreenRate,
//					frame.top + CORNER_WIDTH, paint);
//			canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH, frame.top
//					+ ScreenRate, paint);
//			canvas.drawRect(frame.right - ScreenRate, frame.top, frame.right,
//					frame.top + CORNER_WIDTH, paint);
//			canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right, frame.top
//					+ ScreenRate, paint);
//			canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left
//					+ ScreenRate, frame.bottom, paint);
//			canvas.drawRect(frame.left, frame.bottom - ScreenRate,
//					frame.left + CORNER_WIDTH, frame.bottom, paint);
//			canvas.drawRect(frame.right - ScreenRate, frame.bottom - CORNER_WIDTH,
//					frame.right, frame.bottom, paint);
//			canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom - ScreenRate,
//					frame.right, frame.bottom, paint);

//			slideTop += SPEEN_DISTANCE;
//			if(slideTop >= frame.bottom){
//				slideTop = frame.top;
//			}
//			canvas.drawRect(frame.left + MIDDLE_LINE_PADDING, slideTop - MIDDLE_LINE_WIDTH/2, frame.right - MIDDLE_LINE_PADDING,slideTop + MIDDLE_LINE_WIDTH/2, paint);

//			paint.setColor(Color.WHITE);
//			paint.setTextSize(TEXT_SIZE * density);
//			paint.setAlpha(0x40);
//			paint.setTypeface(Typeface.create("System", Typeface.BOLD));
//			canvas.drawText(getResources().getString(R.string.viewfinderview_status_text1), frame.left, (float) (frame.bottom + (float)TEXT_PADDING_TOP *density), paint);


			Collection<ResultPoint> currentPossible = possibleResultPoints;
			Collection<ResultPoint> currentLast = lastPossibleResultPoints;
			if (currentPossible.isEmpty()) {
				lastPossibleResultPoints = null;
			} else {
				possibleResultPoints = new HashSet<ResultPoint>(5);
				lastPossibleResultPoints = currentPossible;
				paint.setAlpha(OPAQUE);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentPossible) {
					canvas.drawCircle(frame.left + point.getX(), frame.top
							+ point.getY(), 6.0f, paint);
				}
			}
			if (currentLast != null) {
				paint.setAlpha(OPAQUE / 2);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentLast) {
					canvas.drawCircle(frame.left + point.getX(), frame.top
							+ point.getY(), 3.0f, paint);
				}
			}

			postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
					frame.right, frame.bottom);

		}
	}

	public void drawViewfinder() {
		resultBitmap = null;
		invalidate();
	}


	public void addPossibleResultPoint(ResultPoint point) {
		possibleResultPoints.add(point);
	}

	private void drawFrameBounds(Canvas canvas, Rect frame) {

		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(2);
		paint.setStyle(Paint.Style.STROKE);

		canvas.drawRect(frame, paint);

		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.FILL);

		int corWidth = 15;
		int corLength = 45;


		canvas.drawRect(frame.left - corWidth, frame.top, frame.left, frame.top
				+ corLength, paint);
		canvas.drawRect(frame.left - corWidth, frame.top - corWidth, frame.left
				+ corLength, frame.top, paint);

		canvas.drawRect(frame.right, frame.top, frame.right + corWidth,
				frame.top + corLength, paint);
		canvas.drawRect(frame.right - corLength, frame.top - corWidth,
				frame.right + corWidth, frame.top, paint);

		canvas.drawRect(frame.left - corWidth, frame.bottom - corLength,
				frame.left, frame.bottom, paint);
		canvas.drawRect(frame.left - corWidth, frame.bottom, frame.left
				+ corLength, frame.bottom + corWidth, paint);

		canvas.drawRect(frame.right, frame.bottom - corLength, frame.right
				+ corWidth, frame.bottom, paint);
		canvas.drawRect(frame.right - corLength, frame.bottom, frame.right
				+ corWidth, frame.bottom + corWidth, paint);
	}

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
	private void drawStatusText(Canvas canvas, Rect frame, int width) {

		String statusText1 = getResources().getString(
				R.string.viewfinderview_status_text1);
		String statusText2 = getResources().getString(
				R.string.viewfinderview_status_text2);
		int statusTextSize = dip2px(getContext(),15);
		int statusPaddingTop = 180;

		paint.setColor(statusColor);
		paint.setTextSize(statusTextSize);

		int textWidth1 = (int) paint.measureText(statusText1);
		canvas.drawText(statusText1, (width - textWidth1) / 2, frame.top
				- statusPaddingTop, paint);

		int textWidth2 = (int) paint.measureText(statusText2);
		canvas.drawText(statusText2, (width - textWidth2) / 2, frame.top
				- statusPaddingTop + 60, paint);
	}

	private int scanLineTop;
	private final int SCAN_VELOCITY = 5;
	private Bitmap scanLight;
	private void drawScanLight(Canvas canvas, Rect frame) {

		if (scanLineTop == 0) {
			scanLineTop = frame.top;
		}

		if (scanLineTop >= frame.bottom) {
			scanLineTop = frame.top;
		} else {
			scanLineTop += SCAN_VELOCITY;
		}
		Rect scanRect = new Rect(frame.left, scanLineTop, frame.right,
				scanLineTop + 30);
		canvas.drawBitmap(scanLight, null, scanRect, paint);
	}

}
