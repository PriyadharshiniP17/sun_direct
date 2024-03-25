package com.myplex.myplex.utils;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.myplex.myplex.utils.SuperEllipse.Shape.SQUIRCLE;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.myplex.myplex.R;

/** TODO(RJ):
 *  - Glyph caching!
 */
public class SuperEllipseCardView extends LinearLayout
{ private final static SuperEllipse SUPER_ELLIPSE = new SuperEllipse();

  public SuperEllipseCardView(Context context)
  { super(context);
    init(null);
  }
  public SuperEllipseCardView(Context context, @Nullable AttributeSet attrs)
  { super(context, attrs);
    init(attrs);
  }
  public SuperEllipseCardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
  { super(context, attrs, defStyleAttr);
    init(attrs);
  }
  
  private int viewWidth;
  private int viewHeight;
  
  private boolean isReady;
 
  public int shapeBackgroundColor;
  public int shapeForegroundColor;
  public float shapeBorderWidth;
  public float shapeCurveFactor;
  public float shapeRadius;
  public float shapeScale;
  
  public void setShapeBackgroundColor(int backgroundColor)
  { this.shapeBackgroundColor = backgroundColor;
  }
  public void setShapeForegroundColor(int foregroundColor)
  { this.shapeForegroundColor = foregroundColor;
  }
  public void setShapeBorderWidth(float borderWidth)
  { this.shapeBorderWidth = borderWidth;
  }
  public void setShapeCurveFactor(float curveFactor)
  { this.shapeCurveFactor = curveFactor;
  }
  public void setShapeRadius(float shapeRadius)
  { this.shapeRadius = shapeRadius;
  }
  public void setShapeScale(float shapeScale)
  { this.shapeScale = shapeScale;
  }
  public void resetShape()
  { shapeBackgroundColor = 0xFFFFFFFF;
    shapeForegroundColor = 0xFFDDDDDD;
    shapeBorderWidth = 4.f;
    shapeCurveFactor = SQUIRCLE.getCurveFactor();
    shapeRadius      = MATCH_PARENT;
    shapeScale       = 1.f;
  }
  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh)
  { super.onSizeChanged(w, h, oldw, oldh);
    Log.d("SuperEllipse", "onSizeChanged: w "+ w + " h "+ h + " oldw "+oldw + " oldh " + oldh);
    if (w != 0 && h != 0)
    { viewWidth  = w;
      viewHeight = h;
      isReady    = true;
      invalidate();
    }
  }
  public void init(AttributeSet attrs)
  {  resetShape();
    if (attrs != null)
    { TypedArray styleArray = getContext().obtainStyledAttributes(attrs, R.styleable.SuperEllipseImageView);
      shapeBackgroundColor = styleArray.getColor(R.styleable.SuperEllipseImageView_shapeBackgroundColor, shapeBackgroundColor);
      shapeForegroundColor = styleArray.getColor(R.styleable.SuperEllipseImageView_shapeForegroundColor, shapeForegroundColor);
      shapeBorderWidth = styleArray.getDimension(R.styleable.SuperEllipseImageView_shapeBorderWidth, shapeBorderWidth);
      shapeCurveFactor = styleArray.getFloat(R.styleable.SuperEllipseImageView_shapeCurveFactor, shapeCurveFactor);
      shapeRadius = styleArray.getDimension(R.styleable.SuperEllipseImageView_shapeRadius, shapeRadius);
      shapeScale = styleArray.getFloat(R.styleable.SuperEllipseImageView_shapeScale, shapeScale);
      styleArray.recycle();
    }
  }
  @Override
  public void draw(Canvas canvas)
  { super.draw(canvas);
  }
  @Override
  protected void onDraw(Canvas canvas)
  {
    if (isReady)
    { if (shapeRadius == MATCH_PARENT)
      {
        Log.d("SuperEllipse", "onDraw: " + getPaddingTop() +" getPaddingBottom() "+getPaddingBottom() );
        final int realViewWidth = viewWidth-(getPaddingLeft()+getPaddingRight());
        Log.d("SuperEllipse", "onDraw: viewWidth " + viewWidth);
        Log.d("SuperEllipse", "onDraw: realViewWidth "+ realViewWidth);

        final int realViewHeight = viewHeight-(getPaddingTop()-getPaddingBottom());
        Log.d("SuperEllipse", "onDraw: viewHeight "+ viewHeight);
        Log.d("SuperEllipse", "onDraw: realViewHeight "+ realViewHeight);
        shapeRadius = Math.min(realViewWidth, realViewHeight);
        Log.d("SuperEllipse", "onDraw: shapeRadius 1 "+ shapeRadius);
//        shapeRadius /= 2.f;
        Log.d("SuperEllipse", "onDraw: shapeRadius 2 "+ shapeRadius);
        Log.d("SuperEllipse", "onDraw: shapeScale "+ shapeScale);

        shapeRadius *= shapeScale;
      }
      assert shapeRadius > 0;
      canvas.save();
      canvas.translate(viewWidth/2.f, viewHeight/2.f);

      SUPER_ELLIPSE.drawSuperEllipse(canvas, shapeCurveFactor, shapeRadius, shapeRadius, shapeBackgroundColor, shapeForegroundColor, shapeBorderWidth);
      canvas.restore();
    }
    // NOTE(RJ):
    // ;
    super.onDraw(canvas);
  }
  @Override
  protected void onDetachedFromWindow()
  { super.onDetachedFromWindow();
  }
}
