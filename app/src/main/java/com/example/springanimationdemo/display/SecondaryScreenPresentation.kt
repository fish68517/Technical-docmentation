
package com.example.springanimationdemo.display
import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.view.Display
import android.widget.TextView
import com.example.springanimationdemo.R

/**
 * 这是管理副屏UI的类。
 *  就是一个投屏功能
 *    1：单一音频焦点 (Single Audio Focus)：这是最致命的限制。由于Presentation和创建它的Activity属于同一个应用进程，它们共享同一个音频焦点。
 *    这意味着，如果副驾屏用Presentation播放电影，
 *    它的声音会立刻抢占并打断主驾正在收听的导航语音或音乐。这使得它无法满足现代智能座舱中多用户独立享受影音娱乐的需求。
 *
 *    2： 无法运行独立应用：Presentation本质上是一个特殊的Dialog（对话框），它只能显示一个View布局，无法承载一个完整的Activity。
 *    这意味着你不能在副屏上运行一个功能完整的、有自己复杂逻辑和生命周期的独立应用。
 *
 *    3： 交互能力缺失：Presentation默认不处理触摸事件。如果你想在副屏上添加按钮点击、滑动等交互，需要自己实现一套非常复杂的事件捕获和注入流程，开发成本高且体验不佳。
 *
 *    4： 总而言之，Presentation API是一个轻量级的工具，适用于简单的、非交互式的“信息投射”场景。对于追求独立应用、独立音源和复杂交互的现代车载副屏系统，
 *    开发者必须采用如 VirtualDisplay 深度定制、
 *    修改Android框架层，或采用鸿蒙座舱那样的分布式操作系统架构等更高级的技术方案。
 *
 *
 *
 *    5：仪表盘/HUD显示：这是最经典的车载场景。主驾中控屏上的导航应用，可以使用Presentation将简化的转向箭头、距离、下一路口名称等关键信息投射到仪表盘或HUD（抬头显示）上。
 *       媒体信息展示：在中控屏播放音乐时，将歌曲名、歌手、专辑封面等信息同步到仪表盘或副驾屏。
 *
 * @param outerContext The context of the parent Activity.
 * @param display The target external display.
 */
class SecondaryScreenPresentation(outerContext: Context, display: Display) :
    Presentation(outerContext, display) {

    private lateinit var secondaryScreenText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 加载副屏专属的布局
        setContentView(R.layout.presentation_secondary_screen)

        // 初始化副屏上的UI组件
        secondaryScreenText = findViewById(R.id.secondary_screen_text)
    }

    /**
     * 提供一个公共方法，让MainActivity可以调用它来更新文本。
     */
    fun updateText(text: String) {
        // 确保UI组件已初始化
        // Make sure the UI component is initialized.
        if (::secondaryScreenText.isInitialized) {
            secondaryScreenText.text = text
        }
    }
}