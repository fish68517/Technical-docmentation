
package com.example.springanimationdemo.surfaceview
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.springanimationdemo.R

class LauncherMapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher_map)

        val button: Button = findViewById(R.id.map_button)
        button.setOnClickListener {
            // 如果触摸事件转发成功，这个 Toast 将会显示
            Toast.makeText(this, "地图内的按钮被点击！", Toast.LENGTH_SHORT).show()
            Log.d("LauncherMapActivity", "按钮被成功点击！交互已转发。")
        }
    }
}