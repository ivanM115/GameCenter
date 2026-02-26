package com.example.gamecenter;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridLayout;

public class SquareGridLayout extends GridLayout {

    public SquareGridLayout(Context context) {
        super(context);
    }

    public SquareGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Obtener el ancho disponible
        int width = MeasureSpec.getSize(widthMeasureSpec);

        // Hacer que la altura sea igual al ancho (cuadrado)
        int squareSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);

        // Llamar al super con las mismas dimensiones para ambos lados
        super.onMeasure(squareSpec, squareSpec);
    }
}
