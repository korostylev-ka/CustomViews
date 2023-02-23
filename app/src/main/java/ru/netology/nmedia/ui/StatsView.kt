package ru.netology.nmedia.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import ru.netology.nmedia.R
import ru.netology.nmedia.util.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    //набор аттрибутов, которые можно передать через xml
    attrs: AttributeSet? = null,
    //стиль аттрибутов по умолчанию
    defStyleAttr: Int = 0,
    //стиль по умолчанию
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {
    //радиус круга индикатора загрузки
    private var radius = 0F
    //точка центра
    private var center = PointF(0F, 0F)
    private var oval = RectF(0F, 0F, 0F, 0F)
    //AndroidUtils.dp для перевода dp в px
    private var lineWidth = AndroidUtils.dp(context, 5F).toFloat()
    private var fontSize = AndroidUtils.dp(context, 40F).toFloat()
    private var colors = emptyList<Int>()

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
            val resId = getResourceId(R.styleable.StatsView_colors, 0)
            colors = resources.getIntArray(resId).toList()
        }
    }
    //кисть для линии
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        //стиль отрисовки
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        //скругление краев
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        //заливка
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
    }
    //создаем данные в формате списка пар: требуемое значение (100%) и фактическое
    var data: List<Pair<Float, Float>> = emptyList()
        set(value) {
            field = value
            //custom view перерисуется при условии видимости
            invalidate()
        }
    //функция для получения данных для 100% загрузки из 1-х элементов Pair
    fun getDataNeed(data: List<Pair<Float, Float>>): Float {
        var total = 0f
        data.map {
            total += it.first
        }
        return total
    }

    //переопределяем метод изменения размеров
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        //делим на 2, чтобы получить радиус ,а не диаметр. Далее делаем отступ
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            //рассчитываем грани
            center.x - radius, center.y - radius,
            center.x + radius, center.y + radius,
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }
        //чтобы начинать отрисовку окружности сверху
        var startFrom = -90F
        //выясним суммарное значение элементов, для вычисления доли каждого
        val total = getDataNeed(data)
        //переменная для вывода текста процентов
        var textPercent = 0f
        //переменная для цвета точки
        var dotColor: Int = 0

        for ((index, value) in data.withIndex()) {
            val angle = 360F * (value.second/total)
            //добавляем к переменной процент от сектора
            textPercent += value.second / total
            //каждому сектору случайный цвет
            paint.color = colors.getOrNull(index) ?: randomColor()
            //рисуем дугу
            canvas.drawArc(oval, startFrom, angle, false, paint)
            startFrom += angle
            dotColor = colors.get(0)


        }
        canvas.drawText(
            "%.2f%%".format(textPercent *100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint,
        )
        //присваиваем цвет певого сектора для точки
        paint.color = dotColor
        //рисуем точку для корректировки верхней точки окружности
        canvas.drawPoint(center.x,center.y - radius,paint)

    }
    private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}