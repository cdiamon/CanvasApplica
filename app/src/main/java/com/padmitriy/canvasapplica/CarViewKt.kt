package com.padmitriy.canvasapplica

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tan


class CarViewKt : View {

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

    private lateinit var myMatrix: Matrix

    constructor(context: Context) : super(context) {
        initMyView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initMyView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
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
        animPath.moveTo(-50f, -50f)
        animPath.lineTo(200f, 200f)
//        animPath.lineTo(400f, 300f)
//        animPath.lineTo(500f, 500f)
//        animPath.lineTo(200f, 800f)
//        animPath.lineTo(700f, 800f)
//        animPath.lineTo(200f, 400f)
//        animPath.close()

        pathMeasure = PathMeasure(animPath, false)
        pathLength = pathMeasure.length

//        Toast.makeText(context, "pathLength: $pathLength", Toast.LENGTH_LONG).show()

        step = 7f
        distance = 0f
        position = FloatArray(2)
        tangent = FloatArray(2)

        myMatrix = Matrix()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {

        canvas.drawPath(animPath, paint)

        canvas.drawRect(testRectF, paintTest)

        if (distance < pathLength) {
            pathMeasure.getPosTan(distance, position, tangent)

            myMatrix.reset()
            val degrees = (atan2(tangent[1].toDouble(), tangent[0].toDouble()) * 180.0 / Math.PI).toFloat()
            myMatrix.postRotate(degrees, bmOffsetX.toFloat(), bmOffsetY.toFloat())
            myMatrix.postTranslate(position[0] - bmOffsetX, position[1] - bmOffsetY)

            currentCarX = position[0]
            currentCarY = position[1]
            currentCarAngle = degrees

            canvas.drawBitmap(bm, myMatrix, null)

            distance += step
            invalidate()
        } else {
            distance += step
            canvas.drawBitmap(bm, myMatrix, null)
            invalidate()
//            distance = 0f
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//            }
//            MotionEvent.ACTION_MOVE -> {
//            }
            MotionEvent.ACTION_UP -> {

                testRectF = RectF(
                    min(position[0], event.x),
                    min(position[1], event.y),
                    max(position[0], event.x),
                    max(position[1], event.y)
                )



                animPath = Path()
                animPath.moveTo(position[0], position[1])
//                animPath.lineTo(event.x, event.y)
//                animPath.quadTo(100f, 100f, event.x, event.y)
                println(
                    "${min(position[0], event.x)} " +
                            "${max(position[1], event.y)} " +
                            "${max(position[0], event.x)} " +
                            "${min(position[1], event.y)} " +
                            "$currentCarAngle"
                )
//                animPath.addArc( // shit
//                    min(position[0], event.x),
//                    min(position[1], event.y),
//                    max(position[0], event.x),
//                    max(position[1], event.y),
//                    currentCarAngle, 10f
//                )
//                animPath.quadTo(    // mediocre
//                    position[0] + 100,
//                    position[1] + 100,
//                    event.x,
//                    event.y
//                )

                val x0 = position[0]
                val y0 = position[1]
                val x2 = event.x
                val y2 = event.y
                val x1 = x0 - 50

                val y1 = y0 + ((x1 - x0) * tan(Math.toRadians(currentCarAngle.toDouble()).toFloat()))

                animPath.quadTo(
                    x1,
                    y1,
                    event.x,
                    event.y
                )
//                animPath.rQuadTo(
//                    position[0] + 100,
//                    position[1] + 100,
//                    event.x,
//                    event.y
//                )
//                animPath.arcTo(
//                    min(position[0], event.x),
//                    min(position[1], event.y),
//                    max(position[0], event.x),
//                    max(position[1], event.y),
//                    currentCarAngle, 180f, false
//                )
//                animPath.quadTo(event.x - position[0], event.y - position[1], event.x, event.y)
//                animPath.close()

                distance = 0f
                pathMeasure = PathMeasure(animPath, false)
                pathLength = pathMeasure.length

//                invalidate()
            }
        }
        return true

    }

}
