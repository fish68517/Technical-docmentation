package com.example.springanimationdemo.display
import android.content.Context
import android.hardware.display.DisplayManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Display
import android.widget.Button
import android.widget.EditText
import com.example.springanimationdemo.R
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {

    private lateinit var displayManager: DisplayManager
    private var secondaryPresentation: SecondaryScreenPresentation? = null
    private var secondaryDisplay: Display? = null

    private lateinit var textInput: EditText
    private lateinit var sendButton: Button
    private lateinit var presentationSwitch: SwitchMaterial

    // 创建一个DisplayListener来监听屏幕的连接和断开，确保应用的健壮性。
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {
            checkForSecondaryDisplay()
        }
        override fun onDisplayRemoved(displayId: Int) {
            checkForSecondaryDisplay()
        }
        override fun onDisplayChanged(displayId: Int) {
            checkForSecondaryDisplay()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_display)

        // 初始化UI组件
        textInput = findViewById(R.id.main_text_input)
        sendButton = findViewById(R.id.send_button)
        presentationSwitch = findViewById(R.id.presentation_switch)

        // 获取系统显示服务
        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        // 注册监听器以接收屏幕变化通知

        displayManager.registerDisplayListener(displayListener, null)
        // 立即检查一次副屏状态

        checkForSecondaryDisplay()
    }

    override fun onPause() {
        super.onPause()
        // 取消监听器，防止内存泄漏
        // Unregister the listener to prevent memory leaks.
        displayManager.unregisterDisplayListener(displayListener)
        // 隐藏并销毁Presentation实例，防止窗口泄漏

        hideSecondaryDisplay()
    }

    private fun checkForSecondaryDisplay() {
        // 获取所有显示设备
        val displays = displayManager.displays
        // 寻找第一个非主屏的外部显示器
        secondaryDisplay = displays.find { it.displayId != Display.DEFAULT_DISPLAY }

        // 根据是否找到副屏，更新UI状态
        presentationSwitch.isEnabled = secondaryDisplay != null
        if (secondaryDisplay == null) {
            presentationSwitch.isChecked = false
            hideSecondaryDisplay()
        }
    }

    private fun setupListeners() {
        presentationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showSecondaryDisplay()
            } else {
                hideSecondaryDisplay()
            }
        }

        sendButton.setOnClickListener {
            val textToSend = textInput.text.toString()
            if (textToSend.isNotBlank()) {
                // 只有当presentation实例存在时才发送
                // 副屏显示数据
                secondaryPresentation?.updateText(textToSend)
            }
        }
    }

    private fun showSecondaryDisplay() {
        secondaryDisplay?.let { display ->
            // 如果实例已存在，直接返回
            // If an instance already exists and is showing, do nothing.
            if (secondaryPresentation?.isShowing == true) return

            // 创建Presentation实例并显示
            // Create a new instance of our presentation and show it.
            secondaryPresentation = SecondaryScreenPresentation(this, display)
            secondaryPresentation?.show()
        }
    }

    private fun hideSecondaryDisplay() {
        secondaryPresentation?.dismiss()
        secondaryPresentation = null
    }
}