/*
 * Copyright (C) 2014 The Android Open Source Project
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

package ie.yesequality.yesequality;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Analog watch face with a ticking second hand. In ambient mode, the second hand isn't shown. On
 * devices with low-bit ambient mode, the hands are drawn without anti-aliasing in ambient mode.
 */
public class YesEqualityAnalog extends CanvasWatchFaceService {

    /**
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {

        private static final float STROKE_WIDTH = 3f;
        private static final float HAND_END_CAP_RADIUS = 4f;
        private static final float SHADOW_RADIUS = 6f;
        /* Handler to update the time once a second in interactive mode. */
        private final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (R.id.message_update == message.what) {
                    invalidate();
                    if (shouldTimerBeRunning()) {
                        long timeMs = System.currentTimeMillis();
                        long delayMs = INTERACTIVE_UPDATE_RATE_MS
                                - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                        mUpdateTimeHandler.sendEmptyMessageDelayed(R.id.message_update, delayMs);
                    }
                }
            }
        };
        boolean mIsRound;
        int mChinSize;
        private boolean mRegisteredTimeZoneReceiver = false;
        private Time mTime;
        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };
        private Paint mBackgroundPaint;
        private Paint mHourHandPaint;
        private Paint mMinuteHandPaint;
        private Paint mSecondHandPaint;
        private boolean mAmbient;
        private float mHourHandLength;
        private float mMinuteHandLength;
        private float mSecondHandLength;
        private int mWidth;
        private int mHeight;
        private float mCenterX;
        private float mCenterY;
        private Bitmap mBackgroundBitmap;
        private Rect mCardBounds = new Rect();
        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;
        private Bitmap mDarkBackgroundBitmap;
        private Bitmap mForegroundBitmap;
        private Bitmap mDarkForegroundBitmap;
        private Bitmap foregroundBitmap;
        private Bitmap darkForegroundBitmap;

        private void scaleForegroundBitmaps() {
            int foregroundHeight, foregroundWidth;
            if (mCardBounds.height() > mHeight / 4) {
                foregroundHeight = (int) (mHeight - (mCardBounds.height() * 1.5));
                foregroundWidth = mWidth - (mWidth / 4);
            } else {
                foregroundHeight = mHeight - (mHeight / 4);
                foregroundWidth = mWidth - (mWidth / 4);
            }

            float foregroundHeightScale = ((float) foregroundHeight) / ((float) foregroundBitmap.getHeight());
            float foregroundWidthScale = ((float) foregroundWidth) / ((float) foregroundBitmap.getWidth());
            float foregroundScale = foregroundHeightScale < foregroundWidthScale ? foregroundHeightScale : foregroundWidthScale;

            float foregroundScaledHeight = foregroundScale * foregroundBitmap.getHeight();
            float foregroundScaledWidth = foregroundScale * foregroundBitmap.getWidth();

            mForegroundBitmap = Bitmap.createScaledBitmap(foregroundBitmap, (int) foregroundScaledWidth, (int) foregroundScaledHeight, true);
            mDarkForegroundBitmap = Bitmap.createScaledBitmap(darkForegroundBitmap, (int) foregroundScaledWidth, (int) foregroundScaledHeight, true);
        }

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(YesEqualityAnalog.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setStatusBarGravity(Gravity.START | Gravity.TOP)
                    .setHotwordIndicatorGravity(Gravity.END | Gravity.TOP)
                    .setViewProtection(WatchFaceStyle.PROTECT_STATUS_BAR | WatchFaceStyle.PROTECT_HOTWORD_INDICATOR)
                    .build());

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(Color.BLACK);
            mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_analog_white);
            mDarkBackgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_analog_black);

            foregroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.foreground_analog_white);
            darkForegroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.foreground_analog_black);

            mMinuteHandPaint = new Paint();
            mMinuteHandPaint.setColor(getResources().getColor(R.color.red));
            mMinuteHandPaint.setStrokeWidth(STROKE_WIDTH);
            mMinuteHandPaint.setAntiAlias(true);
            mMinuteHandPaint.setStrokeCap(Paint.Cap.ROUND);
            mMinuteHandPaint.setShadowLayer(4f, 0, 0, Color.BLACK);
            mMinuteHandPaint.setStyle(Paint.Style.STROKE);
            mHourHandPaint = new Paint(mMinuteHandPaint);
            mHourHandPaint.setShadowLayer(6f, 0, 0, Color.BLACK);
            mHourHandPaint.setColor(getResources().getColor(R.color.blue));
            mSecondHandPaint = new Paint(mMinuteHandPaint);
            mSecondHandPaint.setShadowLayer(8f, 0, 0, Color.BLACK);
            mSecondHandPaint.setColor(getResources().getColor(R.color.green));

            mTime = new Time();
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTime(new Date());
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(R.id.message_update);
            super.onDestroy();
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                invalidate();
            }

            if (mAmbient) {
                mMinuteHandPaint.setAntiAlias(false);
                mHourHandPaint.setAntiAlias(false);
                mSecondHandPaint.setAntiAlias(false);
                mMinuteHandPaint.setColor(getResources().getColor(android.R.color.white));
                mHourHandPaint.setColor(getResources().getColor(android.R.color.white));
                mSecondHandPaint.setColor(getResources().getColor(android.R.color.white));
            } else {
                mHourHandPaint.setAntiAlias(true);
                mMinuteHandPaint.setAntiAlias(true);
                mSecondHandPaint.setAntiAlias(true);
                mMinuteHandPaint.setColor(getResources().getColor(R.color.red));
                mHourHandPaint.setColor(getResources().getColor(R.color.blue));
                mSecondHandPaint.setColor(getResources().getColor(R.color.green));
            }

            /*
             * Whether the timer should be running depends on whether we're visible (as well as
             * whether we're in ambient mode), so we may need to start or stop the timer.
             */
            updateTimer();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mWidth = width;
            mHeight = height;
            /*
             * Find the coordinates of the center point on the preview_analog.
             * Ignore the window insets so that, on round watches
             * with a "chin", the watch face is centered on the entire preview_analog,
             * not just the usable portion.
             */
            mCenterX = mWidth / 2f;
            mCenterY = mHeight / 2f;
            /*
             * Calculate the lengths of the watch hands and store them in member variables.
             */
            mHourHandLength = 0.5f * width / 2;
            mMinuteHandLength = 0.7f * width / 2;
            mSecondHandLength = 0.9f * width / 2;

            float widthScale = ((float) width) / (float) mBackgroundBitmap.getWidth();
            float heightScale = ((float) height) / (float) mBackgroundBitmap.getHeight();


            float scaledWidth = (mBackgroundBitmap.getWidth() * widthScale);
            float scaledHeight = (mBackgroundBitmap.getHeight() * heightScale);
            //scaledHeight = scaledHeight - (2 * (mIsRound ? 1 : 0) * mChinSize);

            mBackgroundBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                    (int) (scaledWidth),
                    (int) (scaledWidth), true);
            mDarkBackgroundBitmap = Bitmap.createScaledBitmap(mDarkBackgroundBitmap,
                    (int) (scaledWidth),
                    (int) (scaledWidth), true);

            scaleForegroundBitmaps();
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            mIsRound = insets.isRound();
            mChinSize = insets.getSystemWindowInsetBottom();

//            if (mIsRound) {
//                float scaledWidth = mWidth - (2 * mChinSize);
//                float scaledHeight = mHeight - (2 * mChinSize);
//
//                mBackgroundBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
//                        (int) (scaledWidth),
//                        (int) (scaledWidth), true);
//                mDarkBackgroundBitmap = Bitmap.createScaledBitmap(mDarkBackgroundBitmap,
//                        (int) (scaledWidth),
//                        (int) (scaledWidth), true);
//            }
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            mTime.setToNow();


            if (mAmbient && (mLowBitAmbient || mBurnInProtection)) {
                canvas.drawColor(Color.BLACK);
            } else if (mAmbient) {
                canvas.drawBitmap(mDarkBackgroundBitmap, 0, 0, mBackgroundPaint);
                canvas.drawBitmap(mDarkForegroundBitmap, (mWidth - mDarkForegroundBitmap.getWidth()) / 2, (mHeight - mCardBounds.height() - mDarkForegroundBitmap.getHeight()) / 2, mBackgroundPaint);
                canvas.drawRect(mCardBounds, mBackgroundPaint);
            } else {
                canvas.drawBitmap(mBackgroundBitmap, 0, 0, mBackgroundPaint);
                canvas.drawBitmap(mForegroundBitmap, (mWidth - mForegroundBitmap.getWidth()) / 2, (mHeight - mCardBounds.height() - mForegroundBitmap.getHeight()) / 2, mBackgroundPaint);

            }

            /*
             * These calculations reflect the rotation in degrees per unit of
             * time, e.g. 360 / 60 = 6 and 360 / 12 = 30
             */
            final float secondsRotation = mTime.second * 6f;
            final float minutesRotation = mTime.minute * 6f;
            // account for the offset of the hour hand due to minutes of the hour.
            final float hourHandOffset = mTime.minute / 2f;
            final float hoursRotation = (mTime.hour * 30) + hourHandOffset;

            // save the canvas state before we begin to rotate it
            canvas.save();


            canvas.rotate(minutesRotation, mCenterX, mCenterY);
            drawHand(canvas, mMinuteHandLength, mMinuteHandPaint);

            canvas.rotate(hoursRotation - minutesRotation, mCenterX, mCenterY);
            drawHand(canvas, mHourHandLength, mHourHandPaint);

            if (!mAmbient) {
                canvas.rotate(secondsRotation - hoursRotation, mCenterX, mCenterY);
                canvas.drawLine(mCenterX, mCenterY - HAND_END_CAP_RADIUS, mCenterX,
                        mCenterY - mSecondHandLength, mSecondHandPaint);
            }

            canvas.drawCircle(mCenterX, mCenterY, HAND_END_CAP_RADIUS, mSecondHandPaint);

            // restore the canvas' original orientation.
            canvas.restore();
        }

        @Override
        public void onPeekCardPositionUpdate(Rect rect) {
            super.onPeekCardPositionUpdate(rect);
            mCardBounds.set(rect);

            scaleForegroundBitmaps();
            invalidate();
        }

        private void drawHand(Canvas canvas, float handLength, Paint handPaint) {
            canvas.drawRoundRect(mCenterX - HAND_END_CAP_RADIUS,
                    mCenterY - handLength, mCenterX + HAND_END_CAP_RADIUS,
                    mCenterY + HAND_END_CAP_RADIUS, HAND_END_CAP_RADIUS,
                    HAND_END_CAP_RADIUS, handPaint);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            /*
            * Whether the timer should be running depends on whether we're visible
            * (as well as whether we're in ambient mode),
            * so we may need to start or stop the timer.
            */
            updateTimer();
        }


        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            YesEqualityAnalog.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            YesEqualityAnalog.this.unregisterReceiver(mTimeZoneReceiver);
        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(R.id.message_update);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(R.id.message_update);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer
         * should only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }
    }
}
