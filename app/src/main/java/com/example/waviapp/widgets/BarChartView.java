package com.example.waviapp.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom BarChart View for Admin Statistics.
 * Draws a simple vertical bar chart with labels and values.
 */
public class BarChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private LinkedHashMap<String, Integer> data = new LinkedHashMap<>();
    private int maxValue = 0;

    private final int barColor1 = Color.parseColor("#1565C0");
    private final int barColor2 = Color.parseColor("#42A5F5");

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        barPaint.setStyle(Paint.Style.FILL);

        textPaint.setColor(Color.parseColor("#333333"));
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        labelPaint.setColor(Color.parseColor("#757575"));
        labelPaint.setTextSize(26f);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        gridPaint.setColor(Color.parseColor("#E0E0E0"));
        gridPaint.setStrokeWidth(1f);
        gridPaint.setStyle(Paint.Style.STROKE);
    }

    public void setData(LinkedHashMap<String, Integer> data) {
        this.data = data;
        this.maxValue = 0;
        for (int value : data.values()) {
            if (value > maxValue) maxValue = value;
        }
        if (maxValue == 0) maxValue = 1;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data.isEmpty()) {
            textPaint.setTextSize(36f);
            canvas.drawText("Chưa có dữ liệu", getWidth() / 2f, getHeight() / 2f, textPaint);
            textPaint.setTextSize(28f);
            return;
        }

        int paddingLeft = getPaddingLeft() + 20;
        int paddingRight = getPaddingRight() + 20;
        int paddingTop = getPaddingTop() + 20;
        int paddingBottom = getPaddingBottom() + 60;

        float chartWidth = getWidth() - paddingLeft - paddingRight;
        float chartHeight = getHeight() - paddingTop - paddingBottom;

        int barCount = data.size();
        float barSpacing = chartWidth / (barCount * 2f);
        float barWidth = barSpacing * 0.9f;

        // Draw grid lines
        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            float y = paddingTop + (chartHeight * i / gridLines);
            canvas.drawLine(paddingLeft, y, getWidth() - paddingRight, y, gridPaint);
        }

        // Draw bars
        List<String> labels = new ArrayList<>(data.keySet());
        List<Integer> values = new ArrayList<>(data.values());

        for (int i = 0; i < barCount; i++) {
            float x = paddingLeft + (chartWidth * (2 * i + 1)) / (2f * barCount);
            float barHeight = (values.get(i) * chartHeight) / maxValue;

            float left = x - barWidth / 2;
            float top = paddingTop + chartHeight - barHeight;
            float right = x + barWidth / 2;
            float bottom = paddingTop + chartHeight;

            // Gradient bar
            LinearGradient gradient = new LinearGradient(
                    left, top, left, bottom,
                    barColor1, barColor2,
                    Shader.TileMode.CLAMP);
            barPaint.setShader(gradient);

            RectF rect = new RectF(left, top, right, bottom);
            canvas.drawRoundRect(rect, 8f, 8f, barPaint);
            barPaint.setShader(null);

            // Value on top
            canvas.drawText(String.valueOf(values.get(i)),
                    x, top - 10, textPaint);

            // Label at bottom
            canvas.drawText(labels.get(i),
                    x, paddingTop + chartHeight + 40, labelPaint);
        }
    }
}
