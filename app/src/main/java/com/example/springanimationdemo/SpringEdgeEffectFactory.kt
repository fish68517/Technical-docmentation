import android.widget.EdgeEffect
import androidx.recyclerview.widget.RecyclerView

/**
 * 一个 EdgeEffectFactory，用于创建 SpringEdgeEffect 实例。
 * 这个工厂的作用就是告诉 RecyclerView 在需要创建边缘效果时，使用我们自己的 SpringEdgeEffect 类。
 */
class SpringEdgeEffectFactory : RecyclerView.EdgeEffectFactory() {
    override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
        // 根据方向创建我们的自定义 SpringEdgeEffect
        return SpringEdgeEffect(view, direction)
    }
}
