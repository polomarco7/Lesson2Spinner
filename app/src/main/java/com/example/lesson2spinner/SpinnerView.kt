package com.example.lesson2spinner

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.RectF
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ArrayRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt
import kotlin.random.Random

class SpinningWheelView : View, WheelRotation.RotationListener {
    @ColorInt
    private var wheelStrokeColor = 0
    private var wheelStrokeWidth = 0f
    private var wheelStrokeRadius = 0f
    private var wheelTextColor = 0
    private var wheelTextSize = 0f
    private var wheelArrowColor = 0
    private var wheelArrowWidth = 0f
    private var wheelArrowHeight = 0f
    private var wheelRotation: WheelRotation? = null
    private var circle: Circle? = null
    private var angle = 0f
    private var previousX = 0f
    private var previousY = 0f
    private var items: List<*>? = null
    private var points: Array<Point?>? = null

    @ColorInt
    private lateinit var colors: IntArray
    private var onRotationListener: OnRotationListener<String?>? = null
    private var onRotationListenerTicket = false
    private var onRotation = false
    private var textPaint: Paint? = null
    private var strokePaint: Paint? = null
    private var trianglePaint: Paint? = null
    private var itemPaint: Paint? = null
    private var text = ""

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initAttrs(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttrs(attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        initCircle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (circle == null) {
            initCircle()
        }
        if (hasData() && (points == null || points!!.size != getItemSize())) {
            initPoints()
        }
        drawCircle(canvas)
        drawWheel(canvas)
        drawWheelItems(canvas)
        drawTriangle(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (circle == null || !isEnabled || onRotation) {
            return false
        }
        val x = event.x
        val y = event.y
        if (!circle!!.contains(x, y)) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> onRotationListenerTicket = true
            MotionEvent.ACTION_MOVE -> {
                var dx = x - previousX
                var dy = y - previousY

                if (y > circle!!.getCy()) {
                    dx *= -1
                }

                if (x < circle!!.getCx()) {
                    dy *= -1
                }
                rotate((dx + dy) * TOUCH_SCALE_FACTOR)
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> onRotationListenerTicket = false
        }
        previousX = x
        previousY = y
        return true
    }

    override fun onRotate(angle: Float) {
        rotate(angle)
    }

    override fun onStop() {
        onRotation = false
        if (onRotationListener != null) {
            onRotationListener!!.onStopRotation(getSelectedItem())
        }
    }

    fun setText(newText: String){
        text = newText
        invalidate()
    }

    private fun rotate(angle: Float) {
        this.angle += angle
        this.angle %= ANGLE
        invalidate()
        if (onRotationListenerTicket && angle != 0f && onRotationListener != null) {
            onRotationListener!!.onRotation()
            onRotationListenerTicket = false
        }
    }

    fun rotate(maxAngle: Float, duration: Long, interval: Long) {
        if (maxAngle == 0f) {
            return
        }
        onRotationListenerTicket = true
        onRotation = true
        if (wheelRotation != null) {
            wheelRotation!!.cancel()
        }
        wheelRotation = WheelRotation
            .init(duration, interval)
            .setMaxAngle(maxAngle)
            .setListener(this)
        wheelRotation!!.start()
    }

    private fun setWheelStrokeColor(wheelStrokeColor: Int) {
        this.wheelStrokeColor = wheelStrokeColor
        invalidate()
    }

    private fun setWheelStrokeWidth(wheelStrokeWidth: Float) {
        this.wheelStrokeWidth = wheelStrokeWidth
        initWheelStrokeRadius()
        invalidate()
    }

    private fun setWheelTextSize(wheelTextSize: Float) {
        this.wheelTextSize = wheelTextSize
        invalidate()
    }

    private fun setWheelTextColor(wheelTextColor: Int) {
        this.wheelTextColor = wheelTextColor
        invalidate()
    }

    private fun setWheelArrowColor(wheelArrowColor: Int) {
        this.wheelArrowColor = wheelArrowColor
        invalidate()
    }

    private fun setWheelArrowWidth(wheelArrowWidth: Float) {
        this.wheelArrowWidth = wheelArrowWidth
        invalidate()
    }

    private fun setWheelArrowHeight(wheelArrowHeight: Float) {
        this.wheelArrowHeight = wheelArrowHeight
        invalidate()
    }

    private fun setColors(colors: IntArray) {
        this.colors = colors
        invalidate()
    }

    private fun setColors(@ArrayRes colorsResId: Int) {
        if (colorsResId == 0) {
            setColors(COLORS_RES)
            return
        }
        val typedArray: IntArray

        if (isInEditMode) {
            val sTypeArray = resources.getStringArray(colorsResId)
            typedArray = IntArray(sTypeArray.size)
            for (i in sTypeArray.indices) {
                typedArray[i] = Color.parseColor(sTypeArray[i])
            }
        } else {
            typedArray = resources.getIntArray(colorsResId)
        }
        if (typedArray.size < MIN_COLORS) {
            setColors(COLORS_RES)
            return
        }
        val colors = IntArray(typedArray.size)
        for (i in typedArray.indices) {
            colors[i] = typedArray[i]
        }
        setColors(colors)
    }

    fun setItems(items: List<*>?) {
        this.items = items
        initPoints()
        invalidate()
    }

    fun setItems(@ArrayRes itemsResId: Int) {
        if (itemsResId == 0) {
            return
        }
        val typedArray = resources.getStringArray(itemsResId)
        val items = ArrayList<Any?>()
        for (i in typedArray.indices) {
            items.add(typedArray[i])
        }
        setItems(items)
    }

    fun setOnRotationListener(onRotationListener: OnRotationListener<String?>?) {
        this.onRotationListener = onRotationListener
    }

    private fun <T> getSelectedItem(): T? {
        if (circle == null || points == null) {
            return null
        }
        val itemSize = getItemSize()
        val cx = circle!!.getCx()
        for (i in points!!.indices) {
            if (points!![i]!!.x <= cx && cx <= points!![(i + 1) % itemSize]!!.x) {
                return items!![i] as T
            }
        }
        return null
    }

    @SuppressLint("CustomViewStyleable")
    private fun initAttrs(attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.Wheel, 0, 0)
        try {
            val colorsResId = typedArray.getResourceId(R.styleable.Wheel_wheel_colors, 0)
            setColors(colorsResId)
            val wheelStrokeColor = typedArray.getColor(
                R.styleable.Wheel_wheel_stroke_color,
                ContextCompat.getColor(context, android.R.color.transparent)
            )
            setWheelStrokeColor(wheelStrokeColor)
            val wheelStrokeWidth = typedArray.getDimension(R.styleable.Wheel_wheel_stroke_width, 0f)
            setWheelStrokeWidth(wheelStrokeWidth)
            val itemsResId = typedArray.getResourceId(R.styleable.Wheel_wheel_items, 0)
            setItems(itemsResId)
            val wheelTextSize =
                typedArray.getDimension(R.styleable.Wheel_wheel_text_size, TEXT_SIZE.toFloat())
            setWheelTextSize(wheelTextSize)
            val wheelTextColor = typedArray.getColor(R.styleable.Wheel_wheel_text_color, TEXT_COLOR)
            setWheelTextColor(wheelTextColor)
            val wheelArrowColor =
                typedArray.getColor(R.styleable.Wheel_wheel_arrow_color, ARROW_COLOR)
            setWheelArrowColor(wheelArrowColor)
            val wheelArrowWidth = typedArray.getDimension(
                R.styleable.Wheel_wheel_arrow_width, dpToPx(
                    ARROW_SIZE
                ).toFloat()
            )
            setWheelArrowWidth(wheelArrowWidth)
            val wheelArrowHeight = typedArray.getDimension(
                R.styleable.Wheel_wheel_arrow_height, dpToPx(
                    ARROW_SIZE
                ).toFloat()
            )
            setWheelArrowHeight(wheelArrowHeight)
        } finally {
            typedArray.recycle()
        }
        init()
    }

    private fun init() {
        textPaint = Paint()
        textPaint!!.style = Paint.Style.FILL
        textPaint!!.color = wheelTextColor
        textPaint!!.textSize = wheelTextSize
        strokePaint = Paint()
        strokePaint!!.style = Paint.Style.STROKE
        strokePaint!!.color = wheelStrokeColor
        strokePaint!!.strokeWidth = wheelStrokeWidth
        strokePaint!!.strokeCap = Paint.Cap.ROUND
        trianglePaint = Paint()
        trianglePaint!!.color = wheelArrowColor
        trianglePaint!!.style = Paint.Style.FILL_AND_STROKE
        trianglePaint!!.isAntiAlias = true
        itemPaint = Paint()
        itemPaint!!.style = Paint.Style.FILL
    }

    private fun initWheelStrokeRadius() {
        wheelStrokeRadius = wheelStrokeWidth / 2
        wheelStrokeRadius = if (wheelStrokeRadius == 0f) 1F else wheelStrokeRadius
    }

    private fun initCircle() {
        val width = if (measuredWidth == 0) width else measuredWidth
        val height = if (measuredHeight == 0) height else measuredHeight
        circle = Circle(width.toFloat(), height.toFloat())
    }

    private fun initPoints() {
        if (items != null && items!!.isNotEmpty()) {
            points = arrayOfNulls(items!!.size)
        }
    }

    private fun drawCircle(canvas: Canvas) {
        canvas.drawCircle(circle!!.getCx(), circle!!.getCy(), circle!!.getRadius(), Paint())
        drawCircleStroke(canvas)
    }

    private fun drawCircleStroke(canvas: Canvas) {
        canvas.drawCircle(
            circle!!.getCx(), circle!!.getCy(), circle!!.getRadius() - wheelStrokeRadius,
            strokePaint!!
        )
    }

    private fun drawWheel(canvas: Canvas) {
        if (!hasData()) {
            return
        }

        val cx = circle!!.getCx()
        val cy = circle!!.getCy()
        val radius = circle!!.getRadius()
        val endOfRight = cx + radius
        val left = cx - radius + wheelStrokeRadius * 2
        val top = cy - radius + wheelStrokeRadius * 2
        val right = cx + radius - wheelStrokeRadius * 2
        val bottom = cy + radius - wheelStrokeRadius * 2

        canvas.rotate(angle, cx, cy)

        val rectF = RectF(left, top, right, bottom)
        var angle = 0f
        for (i in 0 until getItemSize()) {
            canvas.save()
            canvas.rotate(angle, cx, cy)
            canvas.drawArc(rectF, 0f, getAnglePerItem(), true, getItemPaint(i)!!)
            canvas.restore()
            points!![i] = circle!!.rotate(angle + this.angle, endOfRight, cy)
            angle += getAnglePerItem()
        }
    }

    private fun drawWheelItems(canvas: Canvas) {
        val cx = circle!!.getCx()
        val cy = circle!!.getCy()
        val radius = circle!!.getRadius()
        val x = cx - radius + wheelStrokeRadius * 5
        val textWidth = radius - wheelStrokeRadius * 10
        val textPaint = TextPaint()
        textPaint.set(this.textPaint)

            val item = TextUtils
                .ellipsize(text, textPaint, textWidth, TextUtils.TruncateAt.END)
            canvas.save()
            canvas.rotate(angle + 180, cx, cy)
            canvas.drawText(item.toString(), x, cy + Random.nextInt(0, 20), this.textPaint!!)
            canvas.restore()
    }

    private fun drawTriangle(canvas: Canvas) {
        val cx = circle!!.getCx()
        val cy = circle!!.getCy()
        val radius = circle!!.getRadius()

        canvas.rotate(-angle, cx, cy)
        drawTriangle(canvas, trianglePaint, cx, cy - radius, wheelArrowWidth, wheelArrowHeight)
    }

    private fun drawTriangle(
        canvas: Canvas,
        paint: Paint?,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        val halfWidth = width / 2
        val halfHeight = height / 2
        val path = Path()
        path.moveTo(x - halfWidth, y - halfHeight)
        path.lineTo(x + halfWidth, y - halfHeight)
        path.lineTo(x, y + halfHeight)
        path.lineTo(x - halfWidth, y - halfHeight)
        path.close()
        canvas.drawPath(path, paint!!)
    }

    private fun getItemPaint(position: Int): Paint? {
        var i = position % colors.size

        if (getItemSize() - 1 == position && position % colors.size == 0) {
            i = colors.size / 2
        }
        itemPaint!!.color = colors[i]
        return itemPaint
    }

    private fun getItemSize(): Int {
        return if (items == null) 0 else items!!.size
    }

    private fun getAnglePerItem(): Float {
        return ANGLE / getItemSize().toFloat()
    }
    private fun hasData(): Boolean {
        return items != null && items!!.isNotEmpty()
    }

    private fun dpToPx(dp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    interface OnRotationListener<T> {
        fun onRotation()
        fun onStopRotation(item: T?)
    }

    companion object {
        private const val MIN_COLORS = 3
        private const val ANGLE = 360f
        private val COLORS_RES: Int = R.array.rainbow_dash
        private const val TOUCH_SCALE_FACTOR = 180.0f / 320 / 2
        private const val TEXT_SIZE = 25
        private const val TEXT_COLOR = Color.BLACK
        private const val ARROW_COLOR = Color.BLACK
        private const val ARROW_SIZE = 50
    }
}