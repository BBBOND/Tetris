package com.bbbond.tetris

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Thread.sleep
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var view: View? = null
    private var xWidth: Int = 0
    private var xHeight: Int = 0

    private var maps: Array<Array<Boolean>>? = null
    private var box: Array<Point>? = null
    private var boxSize: Int = 0
    private var boxType: Int = 0

    private var linePaint: Paint? = null
    private var boxPaint: Paint? = null
    private var mapPaint: Paint? = null

    private var isPause: Boolean = true
    private var isOver: Boolean = true
    private var downThread: Thread? = null
    private var handler: Handler = Handler(Handler.Callback {
        view?.invalidate()
        true
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)

        initData()
        initView()
        initListener()
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        val width = getScreenWidth(this)
        // 设置游戏区域宽度 = 屏幕宽度 * 2/3
        xWidth = width * 2 / 3
        // 设置游戏区域高度 = 高度 * 2
        xHeight = xWidth * 2

        // 初始化地图
        maps = Array(10) { Array(20, { false }) }
        // 初始化方块大小
        boxSize = xWidth / maps?.size as Int
    }

    /**
     * 初始化方块
     */
    private fun newBoxes() {
        val random: Random = Random()
        boxType = random.nextInt(7)
        when (boxType) {
            0 -> {
                // 田
                box = arrayOf(Point(4, 0), Point(5, 0), Point(5, 1), Point(4, 1))
            }
            1 -> {
                // L
                box = arrayOf(Point(4, 1), Point(3, 0), Point(3, 1), Point(5, 1))
            }
            2 -> {
                // 反L
                box = arrayOf(Point(4, 0), Point(3, 1), Point(3, 0), Point(5, 0))
            }
            3 -> {
                // 一
                box = arrayOf(Point(5, 0), Point(4, 0), Point(6, 0), Point(7, 0))
            }
            4 -> {
                // 土
                box = arrayOf(Point(5, 1), Point(5, 0), Point(4, 1), Point(6, 1))
            }
            5 -> {
                // Z
                box = arrayOf(Point(5, 1), Point(4, 0), Point(4, 1), Point(6, 1))
            }
            6 -> {
                // 反Z
                box = arrayOf(Point(5, 1), Point(5, 0), Point(4, 1), Point(4, 2))
            }
        }
    }

    /**
     * 初始化界面
     */
    private fun initView() {
        linePaint = Paint()
        linePaint?.color = 0xff666666.toInt()
        linePaint?.isAntiAlias = true

        boxPaint = Paint()
        boxPaint?.color = 0xff000000.toInt()
        boxPaint?.isAntiAlias = true

        mapPaint = Paint()
        mapPaint?.color = 0xa0000000.toInt()
        mapPaint?.isAntiAlias = true

        // 实例化游戏区域
        view = object : View(this) {
            override fun onDraw(canvas: Canvas?) {
                super.onDraw(canvas)
                // 绘制地图
                for (x in maps?.indices!!) {
                    maps?.get(0)?.indices!!
                            .filter { maps!![x][it] == true }
                            .forEach {
                                canvas?.drawRect(
                                        (x * boxSize).toFloat(),
                                        (it * boxSize).toFloat(),
                                        (x * boxSize + boxSize).toFloat(),
                                        (it * boxSize + boxSize).toFloat(), mapPaint)
                            }
                }

                // 绘制方块
                if (box != null)
                    for (i in box!!) {
                        canvas?.drawRect(
                                (i.x * boxSize).toFloat(),
                                (i.y * boxSize).toFloat(),
                                (i.x * boxSize + boxSize).toFloat(),
                                (i.y * boxSize + boxSize).toFloat(), boxPaint)
                    }

                // 绘制辅助线
                for (x in maps?.indices!!) {
                    canvas?.drawLine((x * boxSize).toFloat(), 0f, (x * boxSize).toFloat(), (view?.height as Int).toFloat(), linePaint)
                }
                for (y in maps?.get(0)?.indices!!) {
                    canvas?.drawLine(0f, (y * boxSize).toFloat(), (view?.width as Int).toFloat(), (y * boxSize).toFloat(), linePaint)
                }
            }
        }
        // 设置游戏区域大小
        view?.layoutParams = ViewGroup.LayoutParams(xWidth, xHeight)
        // 设置背景色
        view?.setBackgroundColor(0x10000000)
        // 添加至父容器
        flGame.addView(view)
    }

    private fun initListener() {
        btnDown.setOnClickListener(this)
        btnLeft.setOnClickListener(this)
        btnRight.setOnClickListener(this)
        btnUp.setOnClickListener(this)
        btnStart.setOnClickListener(this)
        btnPause.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnDown -> {
                if (isPause || isOver) return
                // 快速下落
                while (true) {
                    if (!moveBottom())
                        break
                }
            }
            R.id.btnLeft -> {
                if (isPause || isOver) return
                move(-1, 0)
            }
            R.id.btnRight -> {
                if (isPause || isOver) return
                move(1, 0)
            }
            R.id.btnUp -> {
                if (isPause || isOver) return
                rotate()
            }
            R.id.btnStart -> {
                startGame()
            }
            R.id.btnPause -> {
                pauseGame()
            }
        }
        view?.invalidate()
    }

    /**
     * 开始游戏
     */
    private fun startGame() {
        if (downThread == null) {
            downThread = Thread(Runnable {
                while (true) {
                    try {
                        sleep(500)
                    } catch(ignore: Exception) {
                    }
                    if (isOver || isPause)
                        continue
                    moveBottom()
                    handler.sendEmptyMessage(0)
                }
            })
            downThread?.start()
        }
        isPause = false
        isOver = false
        maps = Array(10) { Array(20, { false }) }
        newBoxes()
    }

    /**
     * 暂停游戏
     */
    private fun pauseGame() {
        isPause = !isPause
    }

    /**
     * 下落
     */
    private fun moveBottom(): Boolean {
        // 执行移动
        if (move(0, 1)) return true
        // 移动失败 堆积处理
        for (b in box!!) maps?.get(b.x)?.set(b.y, true)
        // 消行处理
        cleanLine()
        newBoxes()
        isOver = checkOver()
        return false
    }

    /**
     * 消行
     */
    private fun cleanLine() {
        val length: Int = maps?.get(0)?.size as Int - 1
        var y = length
        while (y > 0) {
            if (checkLine(y)) {
                deleteLine(y)
                y++
            }
            y--
        }
    }

    /**
     *
     */
    private fun deleteLine(dy: Int) {
        maps?.get(0)?.indices!!
                .reversed()
                .filter { it <= dy }
                .forEach {
                    if (it > 0)
                        for (x in maps?.indices!!) {
                            maps!![x][it] = maps!![x][it - 1]
                        }
                    else
                        for (x in maps?.indices!!) {
                            maps!![x][it] = false
                        }
                }
    }

    /**
     * 检查行
     */
    private fun checkLine(y: Int): Boolean {
        return !maps?.indices!!.any { maps?.get(it)?.get(y) as Boolean }
    }

    /**
     * 结束判断
     */
    private fun checkOver(): Boolean {
        return box!!.any { maps?.get(it.x)?.get(it.y) as Boolean }
    }

    /**
     * 移动
     */
    private fun move(x: Int, y: Int): Boolean {
        box!!
                .filter { checkBoundary(it.x + x, it.y + y) }
                .forEach { return false }
        for (b in box!!) {
            b.x += x
            b.y += y
        }
        return true
    }

    /**
     * 旋转
     */
    private fun rotate(): Boolean {
        if (boxType == 0) return false
        box!!
                .filter {
                    val checkX = -it.y + box?.get(0)?.y as Int + box?.get(0)?.x as Int
                    val checkY = it.x - box?.get(0)?.x as Int + box?.get(0)?.y as Int
                    checkBoundary(checkX, checkY)
                }
                .forEach { return false }
        for (x in box!!) {
            val checkX = -x.y + box?.get(0)?.y as Int + box?.get(0)?.x as Int
            val checkY = x.x - box?.get(0)?.x as Int + box?.get(0)?.y as Int
            x.x = checkX
            x.y = checkY
        }
        return true
    }

    /**
     * 检查边界
     */
    private fun checkBoundary(x: Int, y: Int): Boolean {
        return (x < 0 || y < 0 || x >= maps?.size as Int || y >= maps?.get(0)?.size as Int || maps!![x][y])
    }

    /**
     * 获取屏幕宽度
     */
    private fun getScreenWidth(context: Context): Int {
        val manager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics: DisplayMetrics = DisplayMetrics()
        manager.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.widthPixels
    }
}
