import android.util.Log
import android.view.View
import android.widget.EdgeEffect
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView.EdgeEffectFactory.DIRECTION_LEFT
import kotlin.math.abs

/**
 * 自定义 EdgeEffect，用于实现 RecyclerView 的弹簧回弹效果。
 *
 * @param view 应用效果的 RecyclerView 实例。
 * @param direction 效果的方向，用于判断是列表左端还是右端。
 * - DIRECTION_LEFT: 左边缘
 * - DIRECTION_RIGHT: 右边缘
 */
class SpringEdgeEffect(
    private val view: View,
    private val direction: Int
) : EdgeEffect(view.context) {

    // 将 60px 的最大回弹距离转换为浮点数。
    // 注意：在不同屏幕密度的设备上，60px 的物理尺寸会不同。如果需要统一的物理尺寸，
    // 应使用 DP 并转换为 PX: (60 * Resources.getSystem().displayMetrics.density)
    private val maxOverscrollPx = 60f

    private val springAnimation: SpringAnimation by lazy {
        // 创建一个作用于 RecyclerView TranslationX 属性的 SpringAnimation
        val animation = SpringAnimation(view, DynamicAnimation.TRANSLATION_X)
        val springForce = SpringForce().apply {
            // 设置最终静止位置为0（即无偏移）
            finalPosition = 0f
            stiffness =1380f       // 刚度 (Stiffness)
            dampingRatio = 0.51f   // 阻尼比 (Damping Ratio)
        }
        animation.spring = springForce
        animation
    }

    /**
     * 当用户拖动列表越过边缘时调用。
     * @param deltaDistance 每次回调产生的拖动距离增量。
     */
    private fun handlePull(deltaDistance: Float) {
        if (false) return
        // 如果动画正在运行，先取消它，以响应用户的拖动操作
        Log.d("SpringEdgeEffect", "handlePull: deltaDistance = $deltaDistance, translationX = ${view.translationX}")
        if (springAnimation.isRunning) {
            springAnimation.cancel()
        }

        // 在列表头部向右拖动（DIRECTION_LEFT），视图应该向右移动（translationX为正）。
        val sign = if (direction == DIRECTION_LEFT) 1 else -1
        view.translationX += sign * deltaDistance * view.width * 0.5f
        if (abs(view.translationX) > maxOverscrollPx) {
            view.translationX = sign * maxOverscrollPx
        }
    }

    /**
     * 当用户松手后调用。
     */
    override fun onRelease() {
        super.onRelease()
        // 如果视图存在偏移，则启动弹簧动画让 RecyclerView 回到原位
        if (abs(view.translationX) > 0) {
            springAnimation.start()
        }
    }

    /**
     * 当 Fling（快速滑动）到达边缘时，系统会调用此方法。
     * @param velocity 系统计算出的 Fling 速度。
     */
    override fun onAbsorb(velocity: Int) {
        super.onAbsorb(velocity)
        // 将系统提供的速度作为初始速度启动弹簧动画，实现惯性回弹
        val sign = if (direction == DIRECTION_LEFT) -1 else 1
        springAnimation.setStartVelocity((sign * velocity).toFloat()).start()
    }

    // --- 重写父类方法 ---

    // 在 API 31+ 上，推荐重写 onPullDistance
    // 【已修复】添加了 Float 返回类型并返回 deltaDistance
    override fun onPullDistance(deltaDistance: Float, displacement: Float): Float {
        super.onPullDistance(deltaDistance, displacement)
        handlePull(deltaDistance)
        return deltaDistance
    }

    // 兼容 API 31 以下的版本
    override fun onPull(deltaDistance: Float) {
        super.onPull(deltaDistance)
        handlePull(deltaDistance)
    }



    // 由于我们是通过移动整个 View 来实现效果，所以不需要绘制任何内容（如传统的辉光）
    override fun draw(canvas: android.graphics.Canvas?): Boolean {
        return false
    }

    // 当视图没有偏移且动画没有在执行时，我们认为效果已结束
    override fun isFinished(): Boolean {
        return !springAnimation.isRunning && view.translationX == 0f
    }
}
