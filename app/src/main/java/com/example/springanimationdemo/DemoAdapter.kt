import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DemoAdapter(private val items: List<String>) : RecyclerView.Adapter<DemoAdapter.DemoViewHolder>() {

    companion object {
        // 帮助函数，用于将 dp 单位转换为像素
        fun dpToPx(context: Context, dp: Int): Int {
            return (dp * context.resources.displayMetrics.density).toInt()
        }
    }

    class DemoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // itemView 本身是卡片，我们需要找到里面的 TextView
        val textView: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DemoViewHolder {
        val context = parent.context

        // 【已修改】动态创建卡片视图，而不是从XML加载
        // 定义卡片属性
        val cardWidth = dpToPx(context, 372)
        val cardHeight = dpToPx(context, 120)
        val margin = dpToPx(context, 8)

        // 创建卡片根视图 (FrameLayout)
        val cardView = FrameLayout(context).apply {
            layoutParams = RecyclerView.LayoutParams(cardWidth, cardHeight).apply {
                setMargins(margin, margin, margin, margin)
            }
        }

        // 创建用于显示文本的 TextView
        val textView = TextView(context).apply {
            id = android.R.id.text1 // 设置一个ID，方便在ViewHolder中查找
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            setTextColor(Color.WHITE)
            textSize = 18f
        }

        // 将 TextView 添加到卡片中
        cardView.addView(textView)

        return DemoViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: DemoViewHolder, position: Int) {
        holder.textView.text = items[position]

        // 【已修改】为卡片动态创建带圆角的背景
        val cornerRadius = dpToPx(holder.itemView.context, 16).toFloat()
        val background = GradientDrawable().apply {
            // 使用和之前一样的颜色逻辑，让卡片五彩斑斓
            val colors = listOf(0xFF_ef4444, 0xFF_f97316, 0xFF_84cc16, 0xFF_10b981, 0xFF_06b6d4, 0xFF_8b5cf6)
            setColor(colors[position % colors.size].toInt())
            setCornerRadius(cornerRadius)
        }
        holder.itemView.background = background
    }

    override fun getItemCount(): Int = items.size
}