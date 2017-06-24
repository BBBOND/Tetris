package com.bbbond.tetris.presenter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import com.bbbond.tetris.MainActivity
import com.bbbond.tetris.R
import com.bbbond.tetris.contract.MainContract
import com.bbbond.tetris.model.MainModel
import com.bbbond.tetris.utils.SPUtils
import java.util.*

/**
 * Created by bbbond on 2017/6/23.
 */

class MainPresenter(mView: MainActivity) : MainContract.Presenter {

    private val MAX_SCORE: String = "MAX_SCORE"

    private var view: MainContract.View = mView
    private var model: MainContract.Model = MainModel()
    private var context: Context = mView

    private var maps: Array<Array<Boolean>>? = null
    private var boxes: Array<Point>? = null
    private var nextBoxes: Array<Point>? = null
    private var boxSize: Int = 0
    private var nextBoxSize: Int = 0
    private var boxType: Int = 0
    private var nextBoxType: Int = 0

    private var currentScore: Int = 0
    private var maxScore: Int = 0

    private var isPause: Boolean = false
    private var isOver: Boolean = false
    private var isStart: Boolean = false
    private var downThread: Thread? = null

    override fun initDate(xWidth: Int) {
        // 初始化地图
        maps = Array(10) { Array(20, { false }) }
        // 初始化方块大小
        boxSize = xWidth / maps?.size as Int
        updateMaxScore(context)
    }

    /**
     * 初始化方块
     */
    override fun newBoxes() {
        // 如果下一块为空则生成下一块
        if (nextBoxes == null)
        // 生成下一块
            newNextBox()
        // 当前块 = 下一块
        boxes = nextBoxes
        // 当前块类型 = 下一块类型
        boxType = nextBoxType

        // 生成下一块
        newNextBox()
    }

    override fun isPause(): Boolean {
        return isPause
    }

    override fun isOver(): Boolean {
        return isOver
    }

    override fun getCurrentScore(): Int {
        return currentScore
    }

    override fun getMaxScore(): Int {
        return maxScore
    }

    override fun drawMap(canvas: Canvas?, mapPaint: Paint?) {
        // 绘制地图
        for (x in maps?.indices!!) {
            maps?.get(0)?.indices!!
                    .filter { maps!![x][it] }
                    .forEach {
                        canvas?.drawRect(
                                (x * boxSize).toFloat(),
                                (it * boxSize).toFloat(),
                                (x * boxSize + boxSize).toFloat(),
                                (it * boxSize + boxSize).toFloat(), mapPaint)
                    }
        }
    }

    override fun drawBox(canvas: Canvas?, boxPaint: Paint?) {
        // 绘制方块
        if (boxes != null)
            for (i in boxes!!) {
                canvas?.drawRect(
                        (i.x * boxSize).toFloat(),
                        (i.y * boxSize).toFloat(),
                        (i.x * boxSize + boxSize).toFloat(),
                        (i.y * boxSize + boxSize).toFloat(), boxPaint)
            }
    }

    override fun drawLine(canvas: Canvas?, height: Float, width: Float, linePaint: Paint?) {
        // 绘制辅助线
        for (x in maps?.indices!!) {
            canvas?.drawLine((x * boxSize).toFloat(), 0f, (x * boxSize).toFloat(), height, linePaint)
        }
        for (y in maps?.get(0)?.indices!!) {
            canvas?.drawLine(0f, (y * boxSize).toFloat(), width, (y * boxSize).toFloat(), linePaint)
        }
    }

    override fun drawState(context: Context, canvas: Canvas?, height: Float, width: Float, statePaint: Paint?) {
        // 绘制状态
        var text: String = ""
        if (isOver) {
            text = context.resources.getString(R.string.game_over)
        } else if (isPause) {
            text = context.resources.getString(R.string.pause)
        }
        canvas?.drawText(
                text,
                (width / 2.0 - (statePaint?.measureText(text) as Float / 2.0)).toFloat(),
                (height / 2.0).toFloat(),
                statePaint
        )
    }

    override fun drawNext(canvas: Canvas?, width: Int, nextPaint: Paint?) {
        if (nextBoxes != null) {
            if (nextBoxSize == 0)
                nextBoxSize = width / 6
            nextBoxes!!.forEach {
                canvas?.drawRect(
                        ((it.x - 3) * nextBoxSize).toFloat(),
                        ((it.y + 2) * nextBoxSize).toFloat(),
                        ((it.x - 3) * nextBoxSize + nextBoxSize).toFloat(),
                        ((it.y + 2) * nextBoxSize + nextBoxSize).toFloat(), nextPaint)
            }
        }
    }

    override fun handleClick(id: Int) {
        when (id) {
            R.id.btnDown -> {
                if (isPause || isOver || !isStart) return
                moveBottom()
            }
            R.id.btnLeft -> {
                if (isPause || isOver || !isStart) return
                move(-1, 0)
            }
            R.id.btnRight -> {
                if (isPause || isOver || !isStart) return
                move(1, 0)
            }
            R.id.btnUp -> {
                if (isPause || isOver || !isStart) return
                rotate()
            }
            R.id.btnStart -> {
                view.toast(R.string.long_press_to_start)
            }
            R.id.btnPause -> {
                pauseGame()
            }
            R.id.btnFastDown -> {
                if (isPause || isOver || !isStart) return
                // 快速下落
                while (true) {
                    if (!moveBottom())
                        break
                }
            }
        }
    }

    override fun handleLongClick(id: Int): Boolean {
        when (id) {
            R.id.btnStart -> {
                startGame()
                return true
            }
        }
        return false
    }

    override fun pauseGame(isPause: Boolean) {
        this.isPause = isPause
        view.changePauseButtonText(this.isPause)
    }

    /**
     * 生成下一块
     */
    private fun newNextBox() {
        val random: Random = Random()
        nextBoxType = random.nextInt(7)
        when (nextBoxType) {
            0 -> {
                // 田
                nextBoxes = arrayOf(Point(4, 0), Point(5, 0), Point(5, 1), Point(4, 1))
            }
            1 -> {
                // L
                nextBoxes = arrayOf(Point(4, 1), Point(3, 0), Point(3, 1), Point(5, 1))
            }
            2 -> {
                // 反L
                nextBoxes = arrayOf(Point(4, 0), Point(3, 1), Point(3, 0), Point(5, 0))
            }
            3 -> {
                // 一
                nextBoxes = arrayOf(Point(5, 0), Point(4, 0), Point(6, 0), Point(7, 0))
            }
            4 -> {
                // 土
                nextBoxes = arrayOf(Point(5, 1), Point(5, 0), Point(4, 1), Point(6, 1))
            }
            5 -> {
                // Z
                nextBoxes = arrayOf(Point(5, 1), Point(4, 0), Point(4, 1), Point(5, 2))
            }
            6 -> {
                // 反Z
                nextBoxes = arrayOf(Point(5, 1), Point(5, 0), Point(4, 1), Point(4, 2))
            }
        }
    }

    /**
     * 开始游戏
     */
    private fun startGame() {
        if (downThread == null) {
            downThread = Thread(Runnable {
                while (true) {
                    try {
                        Thread.sleep(500)
                    } catch(ignore: Exception) {
                    }
                    if (isOver || isPause)
                        continue
                    moveBottom()
                    view.refreshView()
                }
            })
            downThread?.start()
        }
        isPause = false
        isOver = false
        isStart = true
        maps = Array(10) { Array(20, { false }) }
        currentScore = 0
        newBoxes()
    }

    /**
     * 下落
     */
    private fun moveBottom(): Boolean {
        // 执行移动
        if (move(0, 1)) return true
        // 移动失败 堆积处理
        for (b in boxes!!) maps?.get(b.x)?.set(b.y, true)
        // 消行处理
        val lines: Int = cleanLine()
        if (lines > 0) {
            currentScore += lines + lines - 1
            updateMaxScore(context)
            view.refreshView()
        }
        newBoxes()
        isOver = checkOver()
        isStart = if (isOver) false else isStart
        return false
    }

    /**
     * 更新分数
     */
    private fun updateMaxScore(context: Context) {
        if (maxScore == 0) {
            maxScore = SPUtils.get(context, MAX_SCORE, 0) as Int
        }
        if (currentScore > maxScore) {
            maxScore = currentScore
            SPUtils.put(context, MAX_SCORE, maxScore)
        }
    }

    /**
     * 暂停游戏
     */
    private fun pauseGame() {
        isPause = !isPause
        view.changePauseButtonText(isPause)
    }

    /**
     * 消行
     */
    private fun cleanLine(): Int {
        var lines: Int = 0
        val length: Int = maps?.get(0)?.size as Int - 1
        var y = length
        while (y > 0) {
            if (checkLine(y)) {
                deleteLine(y)
                y++
                lines++
            }
            y--
        }
        return lines
    }

    /**
     * 清除行
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
        return !maps?.indices!!.any { !(maps?.get(it)?.get(y) as Boolean) }
    }

    /**
     * 结束判断
     */
    private fun checkOver(): Boolean {
        return boxes!!.any { maps?.get(it.x)?.get(it.y) as Boolean }
    }

    /**
     * 移动
     */
    private fun move(x: Int, y: Int): Boolean {
        if (boxes == null) return false
        boxes!!
                .filter { checkBoundary(it.x + x, it.y + y) }
                .forEach { return false }
        for (b in boxes!!) {
            b.x += x
            b.y += y
        }
        return true
    }

    /**
     * 旋转
     */
    private fun rotate(): Boolean {
        if (boxes == null) return false
        if (boxType == 0) return false
        boxes!!
                .filter {
                    val checkX = -it.y + boxes?.get(0)?.y as Int + boxes?.get(0)?.x as Int
                    val checkY = it.x - boxes?.get(0)?.x as Int + boxes?.get(0)?.y as Int
                    checkBoundary(checkX, checkY)
                }
                .forEach { return false }
        for (x in boxes!!) {
            val checkX = -x.y + boxes?.get(0)?.y as Int + boxes?.get(0)?.x as Int
            val checkY = x.x - boxes?.get(0)?.x as Int + boxes?.get(0)?.y as Int
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
}
