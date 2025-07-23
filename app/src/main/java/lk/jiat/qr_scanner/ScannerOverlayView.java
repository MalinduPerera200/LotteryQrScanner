package lk.jiat.qr_scanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ScannerOverlayView extends View {
    private Paint borderPaint;
    private Paint backgroundPaint;
    private RectF scanRect;
    private float cornerLength = 60f;
    private float borderWidth = 8f;

    public ScannerOverlayView(Context context) {
        super(context);
        init();
    }

    public ScannerOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScannerOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        borderPaint = new Paint();
        borderPaint.setColor(Color.RED);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#80000000"));
        backgroundPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float scanSize = Math.min(width, height) * 0.6f;
        float left = (width - scanSize) / 2f;
        float top = (height - scanSize) / 2f;
        float right = left + scanSize;
        float bottom = top + scanSize;

        scanRect = new RectF(left, top, right, bottom);

        canvas.drawRect(0, 0, left, height, backgroundPaint);
        canvas.drawRect(right, 0, width, height, backgroundPaint);
        canvas.drawRect(left, 0, right, top, backgroundPaint);
        canvas.drawRect(left, bottom, right, height, backgroundPaint);

        canvas.drawRect(scanRect, borderPaint);

        drawCorners(canvas);
    }

    private void drawCorners(Canvas canvas) {
        Paint cornerPaint = new Paint();
        cornerPaint.setColor(Color.RED);
        cornerPaint.setStrokeWidth(borderWidth * 2);
        cornerPaint.setStyle(Paint.Style.STROKE);

        float left = scanRect.left;
        float top = scanRect.top;
        float right = scanRect.right;
        float bottom = scanRect.bottom;

        canvas.drawLine(left, top, left + cornerLength, top, cornerPaint);
        canvas.drawLine(left, top, left, top + cornerLength, cornerPaint);

        canvas.drawLine(right - cornerLength, top, right, top, cornerPaint);
        canvas.drawLine(right, top, right, top + cornerLength, cornerPaint);

        canvas.drawLine(left, bottom - cornerLength, left, bottom, cornerPaint);
        canvas.drawLine(left, bottom, left + cornerLength, bottom, cornerPaint);

        canvas.drawLine(right - cornerLength, bottom, right, bottom, cornerPaint);
        canvas.drawLine(right, bottom - cornerLength, right, bottom, cornerPaint);
    }

    public RectF getScanRect() {
        return scanRect;
    }
}