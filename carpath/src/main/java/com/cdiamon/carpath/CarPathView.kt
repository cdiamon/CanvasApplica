package com.cdiamon.carpath

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tan


class CarPathView : View {

    private lateinit var paint: Paint
    private lateinit var bm: Bitmap
    private var bmOffsetX: Int = 0
    private var bmOffsetY: Int = 0

    private var currentCarX: Float = 0f
    private var currentCarY: Float = 0f
    private var currentCarAngle: Float = 0f

    private var testRectF = RectF(1f, 1f, 1f, 1f)
    private lateinit var paintTest: Paint

    private lateinit var animPath: Path
    private lateinit var pathMeasure: PathMeasure
    private var pathLength: Float = 0.toFloat()

    private var step: Float = 0.toFloat()   //distance each step
    private var distance: Float = 0.toFloat()  //distance moved

    private lateinit var position: FloatArray
    private lateinit var tangent: FloatArray

    private var debugDrawingEnable: Boolean = false
    private lateinit var myMatrix: Matrix

    constructor(context: Context) : super(context) {
        initMyView()
    }

    /**
     * @param attrs: атрибуты этой вью:
     * debugDrawingEnable: Boolean (true отображает путь и дополнительную графику для отладки)
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        val ta = context.obtainStyledAttributes(attrs, R.styleable.CarPathView, 0, 0)
        try {
            debugDrawingEnable = ta.getBoolean(R.styleable.CarPathView_debugDrawingEnable, false)
        } finally {
            ta.recycle()
        }

        initMyView()
    }

    private fun initMyView() {
        paint = Paint()
        paint.color = Color.BLUE
        paint.strokeWidth = 0.3f
        paint.style = Paint.Style.STROKE
        paintTest = Paint()
        paintTest.color = Color.GREEN
        paintTest.strokeWidth = 1f
        paintTest.style = Paint.Style.STROKE

        bm = BitmapFactory.decodeResource(resources, R.drawable.car_top_view_red_invert)
        bmOffsetX = bm.width / 2
        bmOffsetY = bm.height / 2

        animPath = Path()
        animPath.moveTo(-150f, -150f)
        animPath.lineTo(200f, 200f)

        pathMeasure = PathMeasure(animPath, false)
        pathLength = pathMeasure.length

        step = 7f
        distance = 0f
        position = FloatArray(2)
        tangent = FloatArray(2)

        myMatrix = Matrix()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {

        if (debugDrawingEnable) {
            canvas.drawPath(animPath, paint)
            canvas.drawRect(testRectF, paintTest)
        }

        if (distance < pathLength) {
            pathMeasure.getPosTan(distance, position, tangent)

            myMatrix.reset()
            //рассчитываем угол поворота машинки для отрисовки исходя из касательной пути
            val degrees = (atan2(tangent[1].toDouble(), tangent[0].toDouble()) * 180.0 / Math.PI).toFloat()
            myMatrix.postRotate(degrees, bmOffsetX.toFloat(), bmOffsetY.toFloat())
            myMatrix.postTranslate(position[0] - bmOffsetX, position[1] - bmOffsetY)

            currentCarX = position[0]
            currentCarY = position[1]
            currentCarAngle = degrees

            canvas.drawBitmap(bm, myMatrix, null)

            //todo увеличивать step до половины pathLength, потом уменьшать
            pathLength
            step

            distance += step
            invalidate()
        } else {
            distance += step
            canvas.drawBitmap(bm, myMatrix, null)
            invalidate()
        }
    }

    /**
     * по тапу на экран рассчитываем путь машины и отправляем на отрисовку движения
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {

                if (debugDrawingEnable) {
                    testRectF = RectF(
                        min(position[0], event.x),
                        min(position[1], event.y),
                        max(position[0], event.x),
                        max(position[1], event.y)
                    )

                    Log.d(
                        CAR_PATH_TAG,
                        "${min(position[0], event.x)} " +
                                "${max(position[1], event.y)} " +
                                "${max(position[0], event.x)} " +
                                "${min(position[1], event.y)} " +
                                "$currentCarAngle"
                    )
                }

                animPath = Path()
                animPath.moveTo(position[0], position[1])

                //исходная координата 0 (где находилась до тапа)
                val x0 = position[0]
                val y0 = position[1]
                //промежуточная координата 1 для кривой Безье
                val x1 =
                    x0 - 50 //50 это просто магическая константа для расчета вектора через тангенс, придумать вариант лучше
                val y1: Float
                //конечная координата 2 пути авто (куда тапнули)
                val x2 = event.x
                val y2 = event.y

                y1 = calculateMiddlePoint(x0, y0, x1)

                //строим кривую Безье по расчетной траектории
                animPath.quadTo(
                    x1,
                    y1,
                    x2,
                    y2
                )

                distance = 0f
                pathMeasure = PathMeasure(animPath, false)
                pathLength = pathMeasure.length
            }
        }
        return true

    }


    /**
     * @param x0 абсцисса исходной точки
     * @param y0 ордината исходной точки
     * @param x1 абсцисса промежуточной точки
     * @return возвращает ординату промежуточной точки y1
     */
    private fun calculateMiddlePoint(x0: Float, y0: Float, x1: Float): Float {
        //todo разобраться с углами близкими к 90 (infinity), отрицательные углы
        return y0 + ((x1 - x0) * tan(Math.toRadians(currentCarAngle.toDouble()).toFloat()))
    }

    fun setDebugMode(drawDebugInfo: Boolean) {
        debugDrawingEnable = drawDebugInfo
    }

    companion object {
        const val CAR_PATH_TAG = "CAR_PATH_TAG"
    }
}
