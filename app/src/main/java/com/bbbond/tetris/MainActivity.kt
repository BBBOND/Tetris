package com.bbbond.tetris

import android.app.Service
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.bbbond.tetris.contract.MainContract
import com.bbbond.tetris.presenter.MainPresenter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener, MainContract.View {

    private var gamePanel: View? = null
    private var nextPanel: View? = null
    private var xWidth: Int = 0
    private var xHeight: Int = 0
    private var nHeight: Int = 0

    private var linePaint: Paint? = null
    private var boxPaint: Paint? = null
    private var mapPaint: Paint? = null
    private var statePaint: Paint? = null

    private var presenter: MainPresenter? = null

    private var handler: Handler = Handler(Handler.Callback {
        gamePanel?.invalidate()
        nextPanel?.invalidate()
        tvCurrentScore.text = presenter?.getCurrentScore().toString()
        tvMaxScore.text = presenter?.getMaxScore().toString()
        true
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)
        presenter = MainPresenter(this@MainActivity)

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

        nHeight = width * 1 / 3

        presenter?.initDate(xWidth)
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

        statePaint = Paint()
        statePaint?.color = 0xff000000.toInt()
        statePaint?.isAntiAlias = true
        statePaint?.textSize = 100f

        // 实例化游戏区域
        gamePanel = object : View(this) {
            override fun onDraw(canvas: Canvas?) {
                super.onDraw(canvas)
                // 绘制地图
                presenter?.drawMap(canvas, mapPaint)

                // 绘制方块
                presenter?.drawBox(canvas, boxPaint)

                // 绘制辅助线
                presenter?.drawLine(canvas, (gamePanel?.height as Int).toFloat(), (gamePanel?.width as Int).toFloat(), linePaint)

                // 绘制状态
                presenter?.drawState(this@MainActivity, canvas, (gamePanel?.height as Int).toFloat(), (gamePanel?.width as Int).toFloat(), statePaint)
            }
        }
        // 设置游戏区域大小
        gamePanel?.layoutParams = ViewGroup.LayoutParams(xWidth, xHeight)
        // 设置背景色
        gamePanel?.setBackgroundColor(0x10000000)
        // 添加至父容器
        flGame.addView(gamePanel)

        // 实例化预览区域
        nextPanel = object : View(this) {
            override fun onDraw(canvas: Canvas?) {
                super.onDraw(canvas)
                presenter?.drawNext(canvas, nextPanel?.width as Int, boxPaint)
            }
        }
        // 设置大小
        nextPanel?.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, nHeight)
        // 设置背景色
        nextPanel?.setBackgroundColor(0x20000000)
        // 添加至父容器
        flNext.addView(nextPanel)

        tvCurrentScore.text = presenter?.getCurrentScore().toString()
        tvMaxScore.text = presenter?.getMaxScore().toString()
    }

    /**
     * 初始化事件
     */
    private fun initListener() {
        (btnDown as TextView).setOnClickListener(this)
        (btnLeft as TextView).setOnClickListener(this)
        (btnRight as TextView).setOnClickListener(this)
        (btnUp as TextView).setOnClickListener(this)
        (btnStart as TextView).setOnClickListener(this)
        (btnPause as TextView).setOnClickListener(this)
        (btnFastDown as TextView).setOnClickListener(this)
        (btnStart as TextView).setOnLongClickListener(this)
    }

    override fun onClick(v: View?) {
        presenter?.handleClick(v?.id as Int)
        gamePanel?.invalidate()
        nextPanel?.invalidate()
    }

    override fun onLongClick(v: View?): Boolean {
        (getSystemService(Service.VIBRATOR_SERVICE) as Vibrator).vibrate(50)
        return presenter?.handleLongClick(v?.id as Int) as Boolean
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

    override fun onPause() {
        super.onPause()
        presenter?.pauseGame(true)
    }

    override fun toast(msg: String) {
        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
    }

    override fun toast(msgId: Int) {
        Toast.makeText(this@MainActivity, resources.getString(msgId), Toast.LENGTH_SHORT).show()
    }

    override fun refreshView() {
        handler.sendEmptyMessage(0)
    }

    override fun changePauseButtonText(isPause: Boolean) {
        btnPause.text = resources.getString(if (isPause) R.string.resume else R.string.pause)
    }
}
