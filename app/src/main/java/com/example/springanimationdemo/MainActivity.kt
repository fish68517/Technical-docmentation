package com.example.springanimationdemo

import DemoAdapter
import SpringEdgeEffectFactory
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 【已修改】创建一个 RelativeLayout 作为根布局，以实现底部对齐
        val rootLayout = RelativeLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        setContentView(rootLayout)
        // 创建 RecyclerView
        val recyclerView = RecyclerView(this).apply {
            id = View.generateViewId()
            // 使用 RelativeLayout.LayoutParams 并设置底部对齐规则
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }

            // 为 RecyclerView 添加内边距，并设置 clipToPadding 为 false
            // 这可以防止两端的卡片紧贴屏幕边缘
            val padding = (16 * resources.displayMetrics.density).toInt()
            setPadding(padding, 0, padding, 0)
            clipToPadding = false
        }
        // 【已修改】将 RecyclerView 添加到根布局中
        rootLayout.addView(recyclerView)

        // 1. 创建一个包含10个项目的虚拟数据列表
        val data = List(10) { "列表项 ${it + 1}" }

        // 2. 设置布局管理器为横向
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // 3. 设置 Adapter
        recyclerView.adapter = DemoAdapter(data)

        // 4. 【核心步骤】应用我们自定义的 EdgeEffectFactory
        recyclerView.edgeEffectFactory = SpringEdgeEffectFactory()

        val animator = ValueAnimator.ofInt(0, 1000)
        animator.setDuration(1000000)
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            // 打印动画value
            Log.d("onAnimationUpdate:", "value = $value")
            Log.d("onAnimationUpdate:", "--------------")
        }
        animator.start()
    }


}
