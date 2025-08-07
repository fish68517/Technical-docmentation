package com.example.springanimationdemo.surfaceview

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.hardware.input.InputManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.annotation.RequiresApi
import com.example.springanimationdemo.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class LauncherActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var surfaceView: SurfaceView
    private lateinit var displayManager: DisplayManager
    private lateinit var inputManager: InputManager

    private var virtualDisplay: VirtualDisplay? = null
    private val TAG = "LauncherActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        surfaceView = findViewById(R.id.map_surface_view)
        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        inputManager = getSystemService(Context.INPUT_SERVICE) as InputManager

        // 关键步骤：确保叠加的 UI (如 FAB) 能够显示在 SurfaceView 内容之上。 [12]
        surfaceView.setZOrderMediaOverlay(false)

        // 注册 SurfaceHolder 回调以管理 Surface 的生命周期。 [9]
        surfaceView.holder.addCallback(this)

        // 为触摸事件转发设置监听器。
        setupTouchForwarding()

        findViewById<FloatingActionButton>(R.id.fab_recenter).setOnClickListener {
            // 这个按钮属于宿主 Activity，可以正常交互。
        }
    }

    // --- SurfaceHolder.Callback 实现 ---

    @RequiresApi(Build.VERSION_CODES.O)
    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "Surface created. Width: ${surfaceView.width}, Height: ${surfaceView.height}")
        // 确保只在 VirtualDisplay 未创建时才创建
        if (virtualDisplay == null) {
            createVirtualDisplayAndLaunchActivity(holder)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "Surface changed. New size: $width x $height")
        // 调整 VirtualDisplay 的大小以匹配 Surface 的变化。 [22]
        virtualDisplay?.resize(width, height, resources.displayMetrics.densityDpi)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "Surface destroyed. Releasing VirtualDisplay.")
        // 这是释放 VirtualDisplay 的唯一安全和确定的时机。 [21]
        releaseVirtualDisplay()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 作为最后的保障措施，在 Activity 销毁时也尝试释放。
        Log.d(TAG, "Activity onDestroy. Final release check.")
        releaseVirtualDisplay()
    }

    // --- 核心功能方法 ---

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createVirtualDisplayAndLaunchActivity(holder: SurfaceHolder) {
        val width = surfaceView.width
        val height = surfaceView.height
        if (width == 0 || height == 0) {
            Log.e(TAG, "SurfaceView dimensions are zero. Cannot create VirtualDisplay.")
            return
        }

        val densityDpi = resources.displayMetrics.densityDpi

        // 1. 创建 VirtualDisplay，将其输出重定向到 SurfaceView 的 Surface。 [14, 16]
        virtualDisplay = displayManager.createVirtualDisplay(
            "MapVirtualDisplay",
            width,
            height,
            densityDpi,
            holder.surface,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION
        )
        Log.d(TAG, "VirtualDisplay created: ${virtualDisplay?.display?.name}")

        // 2. 将目标 Activity 启动到新创建的 VirtualDisplay 上。
        launchMapActivity()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun launchMapActivity() {
        virtualDisplay?.let { vd ->
            val intent = Intent(this, LauncherMapActivity::class.java).apply {
                // 必须在新任务中启动以实现隔离。
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }

            // 使用 ActivityOptions 指定启动的 displayId。
            val options = ActivityOptions.makeBasic()
            options.launchDisplayId = vd.display.displayId

            Log.d(TAG, "Attempting to launch LauncherMapActivity on displayId: ${vd.display.displayId}")
            try {
                startActivity(intent, options.toBundle())
                Log.d(TAG, "startActivity call succeeded.")
            } catch (e: SecurityException) {
                Log.e(TAG, "!!! FAILED TO LAUNCH ACTIVITY !!!", e)
                Log.e(TAG, "这是预期的异常，因为应用没有 INTERNAL_SYSTEM_WINDOW 权限。")
            }
        } ?: Log.e(TAG, "VirtualDisplay is null. Cannot launch activity.")
    }

    private fun releaseVirtualDisplay() {
        virtualDisplay?.release()
        virtualDisplay = null
        Log.d(TAG, "VirtualDisplay released.")
    }

    // --- 触摸事件转发 ---

/*    private fun setupTouchForwarding() {
        surfaceView.setOnTouchListener { _, event ->
            virtualDisplay?.let {
                try {
                    // 将从 SurfaceView 捕获的 MotionEvent 注入到输入系统。 [28]
                    // 系统会自动将其路由到拥有焦点的虚拟显示器。
                    inputManager.injectInputEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC)
                } catch (e: SecurityException) {
                    Log.e(TAG, "!!! FAILED TO INJECT EVENT !!!", e)
                    Log.e(TAG, "这是预期的异常，因为应用没有 INJECT_EVENTS 权限。")
                }
            }
            // 返回 true 表示事件已被消费。
            true
        }
    }*/

    private fun setupTouchForwarding() {
        surfaceView.setOnTouchListener { _, event ->
            virtualDisplay?.let {
                try {
                    // --- 使用反射来调用 @hide API ---

                    // 1. 获取 InputManager 的 Class 对象
                    val inputManagerClass = InputManager::class.java

                    // 2. 按名称和参数类型获取隐藏的 injectInputEvent 方法
                    //    它的签名是 injectInputEvent(InputEvent, int)
                    val injectInputEventMethod = inputManagerClass.getMethod(
                        "injectInputEvent",
                        MotionEvent::class.java, // MotionEvent 是 InputEvent 的子类
                        Int::class.javaPrimitiveType // 使用 Int::class.javaPrimitiveType 获取基本类型 int
                    )

                    // 3. 调用该方法，并直接使用常量 0
                    //    INJECT_INPUT_EVENT_MODE_ASYNC 的实际值就是 0。
                    val injectModeAsyncValue = 0

                    injectInputEventMethod.invoke(
                        inputManager, // 在哪个对象上调用此方法
                        event,        // 第一个参数
                        injectModeAsyncValue // 第二个参数
                    )

                } catch (e: Exception) {
                    // 反射可能会抛出多种异常 (NoSuchMethodException, SecurityException, etc.)
                    // 在这里统一捕获
                    Log.e(TAG, "!!! FAILED TO INJECT EVENT (via Reflection) !!!", e)

                    // 检查根本原因是否为安全异常，这仍然是预期行为
                    if (e.cause is SecurityException || e is SecurityException) {
                        Log.e(TAG, "这是预期的运行时异常，因为应用没有 INJECT_EVENTS 权限。")
                    } else {
                        Log.e(TAG, "发生了非预期的异常，请检查代码逻辑。", e)
                    }
                }
            }
            // 返回 true 表示事件已被消费。
            true
        }
    }
}