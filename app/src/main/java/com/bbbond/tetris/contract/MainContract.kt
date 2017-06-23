package com.bbbond.tetris.contract

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint

/**
 * Created by bbbond on 2017/6/23.
 */

interface MainContract {
    interface Model

    interface View {
        fun toast(msg: String)
        fun toast(msgId: Int)
        fun refreshView()
        fun changePauseButtonText(isPause: Boolean)
    }

    interface Presenter {
        fun newBoxes()
        fun initDate(xWidth: Int)
        fun isPause(): Boolean
        fun isOver(): Boolean
        fun drawMap(canvas: Canvas?, mapPaint: Paint?)
        fun drawBox(canvas: Canvas?, boxPaint: Paint?)
        fun drawLine(canvas: Canvas?, height: Float, width: Float, linePaint: Paint?)
        fun drawState(context: Context, canvas: Canvas?, height: Float, width: Float, statePaint: Paint?)
        fun handleClick(id: Int)
        fun handleLongClick(id: Int): Boolean
        fun pauseGame(isPause: Boolean)
        fun drawNext(canvas: Canvas?, width: Int, nextPaint: Paint?)
        fun getCurrentScore(): Int
        fun getMaxScore(): Int
    }
}
