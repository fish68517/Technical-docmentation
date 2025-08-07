package com.example.springanimationdemo.display

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.Toast
import com.example.springanimationdemo.R
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.cos
import kotlin.math.sin

/***
 * 1：  只使用一个 SurfaceView 来显示 VirtualDisplay 的内容。
 * 创建一个 VirtualDisplay，并将其输出指向这个 SurfaceView。
 * 启动一个独立的绘图线程，在这个线程里，我们直接在 VirtualDisplay 的 Surface 上进行绘制（画一个不断变化的计数器和一个移动的圆圈）。
 * 这样，就可以非常清晰地看到：我们在一个后台的、虚拟的 "屏幕" (VirtualDisplay) 上画图，而画出的内容实时地呈现在了我们主界面的 SurfaceView 上。
 *
 *   整个流程就是：DrawingThread -> virtualDisplay 的输入 Surface -> VirtualDisplay 内部处理 -> SurfaceView 的 Surface -> 屏幕显示。
 *
 *   2： 后续将这个虚拟屏幕 的内容可以： 投射到其他显示设备上（如副屏），
 */

class VirtualDisplayMainActivity : AppCompatActivity() {

    private lateinit var displayManager: DisplayManager
    private lateinit var surfaceView: SurfaceView
    private lateinit var toggleButton: Button

    private var virtualDisplay: VirtualDisplay? = null
    private var drawingThread: DrawingThread? = null
    private var surface: Surface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_virtual_display)

        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        surfaceView = findViewById(R.id.surface_view)
        toggleButton = findViewById(R.id.button_toggle)

        toggleButton.setOnClickListener {
            if (drawingThread?.isAlive == true) {
                stopDrawing()
            } else {
                startDrawing()
            }
        }

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                surface = holder.surface
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                // Surface 尺寸变化，如果正在绘制，需要重启以适应新尺寸
                if (drawingThread?.isAlive == true) {
                    stopDrawing()
                    startDrawing()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                stopDrawing()
                surface = null
            }
        })
    }

    private fun startDrawing() {
        if (surface == null || !surface!!.isValid) {
            Toast.makeText(this, "Surface 不可用", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. 创建 VirtualDisplay
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val width = surfaceView.width
        val height = surfaceView.height
        val densityDpi = metrics.densityDpi

        virtualDisplay = displayManager.createVirtualDisplay(
            "MyVirtualDisplay",
            width,
            height,
            densityDpi,
            surface, // 将 SurfaceView 的 surface 作为 VirtualDisplay 的输出目标
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
        )

        // 2. 从创建的 VirtualDisplay 中获取用于绘制的 Surface
        //    任何绘制在这个 inputSurface 上的内容，都会被系统渲染到我们上一步设置的 output surface (即 surfaceView.holder.surface)
        val inputSurface = virtualDisplay?.surface ?: run {
            Toast.makeText(this, "无法获取 VirtualDisplay 的 Surface", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. 启动绘图线程
        drawingThread = DrawingThread(inputSurface, width, height)
        drawingThread?.start()

        toggleButton.text = "停止 VirtualDisplay"
        Toast.makeText(this, "VirtualDisplay 已启动", Toast.LENGTH_SHORT).show()
    }

    private fun stopDrawing() {
        drawingThread?.stopDrawing()
        drawingThread = null

        virtualDisplay?.release()
        virtualDisplay = null

        toggleButton.text = "启动 VirtualDisplay"
        Toast.makeText(this, "VirtualDisplay 已停止", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        // 确保 Activity 不可见时停止绘图
        stopDrawing()
    }

    /**
     * 一个独立的线程，负责在给定的 Surface 上持续绘制内容。
     */
    private class DrawingThread(
        private val surface: Surface,
        private val width: Int,
        private val height: Int
    ) : Thread() {

        private val isRunning = AtomicBoolean(true)
        private var frameCount = 0

        private val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 50f
            isAntiAlias = true
        }

        private val circlePaint = Paint().apply {
            color = Color.YELLOW
            isAntiAlias = true
        }

        fun stopDrawing() {
            isRunning.set(false)
        }

        override fun run() {
            while (isRunning.get()) {
                var canvas: Canvas? = null
                try {
                    // 锁定 Surface 的画布，准备绘制
                    canvas = surface.lockCanvas(null)
                    if (canvas != null) {
                        // 绘制背景
                        canvas.drawColor(Color.DKGRAY)

                        // 绘制帧计数器
                        val text = "帧: ${frameCount++}"
                        canvas.drawText(text, 50f, 80f, textPaint)

                        // 绘制一个移动的圆
                        val centerX = width / 2f
                        val centerY = height / 2f
                        val radius = height / 4f
                        val angle = frameCount * 0.05 // 改变角度使其运动
                        val circleX = centerX + radius * cos(angle).toFloat()
                        val circleY = centerY + radius * sin(angle).toFloat()
                        canvas.drawCircle(circleX, circleY, 30f, circlePaint)
                    }
                } finally {
                    if (canvas != null) {
                        // 解锁画布，并提交绘制内容
                        surface.unlockCanvasAndPost(canvas)
                    }
                }

                // 控制帧率，避免 CPU 占用过高
                sleep(16) // 大约 60 FPS
            }
        }
    }
}