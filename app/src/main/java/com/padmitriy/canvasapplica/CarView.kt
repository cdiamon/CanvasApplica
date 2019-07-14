package com.padmitriy.canvasapplica

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import kotlin.math.atan2


class CarView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {


    lateinit var paint: Paint

    lateinit var animPath: Path
    lateinit var bm: Bitmap
    lateinit var pathMeasure: PathMeasure
    private lateinit var myMatrix: Matrix
    var bm_offsetX: Int = 0
    var bm_offsetY: Int = 0

    var pathLength: Float = 0.toFloat()
    var step: Float = 0.toFloat()   //distance each step
    var distance: Float = 0.toFloat()  //distance moved

    lateinit var pos: FloatArray
    lateinit var tan: FloatArray

    init {
        initMyView()
    }

    private fun initMyView() {
        paint = Paint()
        paint.color = (Color.BLUE)
        paint.setStrokeWidth(1f)
        paint.setStyle(Paint.Style.STROKE)

        bm = BitmapFactory.decodeResource(resources, R.drawable.car_top_view_red)
        bm_offsetX = bm.getWidth() / 2
        bm_offsetY = bm.getHeight() / 2

        animPath = Path()
        animPath.moveTo(200f, 200f)
        animPath.lineTo(400f, 200f)
        animPath.lineTo(600f, 100f)
        animPath.lineTo(800f, 300f)
        animPath.lineTo(200f, 300f)
        animPath.lineTo(1200f, 600f)
        animPath.lineTo(200f, 200f)
        animPath.close()

        pathMeasure = PathMeasure(animPath, false)
        pathLength = pathMeasure.getLength()

        Toast.makeText(context, "pathLength: $pathLength", Toast.LENGTH_LONG).show()

        step = 1f
        distance = 0f
        pos = FloatArray(2)
        tan = FloatArray(2)

        myMatrix = Matrix()
    }

    override fun onDraw(canvas: Canvas) {


        canvas.drawPath(animPath, paint)

        if (distance < pathLength) {
            pathMeasure.getPosTan(distance, pos, tan)

            matrix.reset()
            val degrees = (atan2(tan[1], tan[0]) * 180.0 / Math.PI).toFloat()
            matrix.postRotate(degrees, bm_offsetX.toFloat(), bm_offsetY.toFloat())
            matrix.postTranslate(pos[0] - bm_offsetX, pos[1] - bm_offsetY)

            canvas.drawBitmap(bm, matrix, null)

            distance += step
        } else {
            distance = 0f
        }
        println("moving")
        Thread.sleep(100)
        invalidate()

    }

}