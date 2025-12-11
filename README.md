# -----------------------------------------------------------
# 全局 Gradle 编译性能优化配置 (针对 16GB 内存设备优化版)
# -----------------------------------------------------------

# 1. Gradle 守护进程内存设置
# 建议：16G内存电脑设置 4g 或 5g 即可。设置 8g 会挤占 Android Studio 和系统的空间，导致死机。
org.gradle.jvmargs=-Xmx5120m -XX:MaxMetaspaceSize=1g -Dfile.encoding=UTF-8

# 2. 开启守护进程 (Gradle 3.0后默认开启，写上也无妨)
org.gradle.daemon=true

# 3. 开启并行编译 (多核 CPU 必备)
org.gradle.parallel=true

# 4. 开启构建缓存 (极重要，第二次编译速度提升的关键)
org.gradle.caching=true

# 5. 按需配置 (对于大型多模块项目有效，现在的 AS 版本通常默认开启 Configuration Cache，此项可选)
org.gradle.configureondemand=true

# 6. Kotlin 编译后台进程内存
# 建议：不要给太大，2g-3g 足够，给 6g 太浪费
kotlin.daemon.jvmargs=-Xmx3g

# -----------------------------------------------------------
# Android 项目通用标准配置 (如果你想所有新项目默认生效)
# -----------------------------------------------------------

# 使用 AndroidX 库 (现在的项目必须项)
android.useAndroidX=true

# 开启非传递性 R 类 (大幅提升编译速度，推荐开启)
# 注意：如果打开非常老的项目报错，可以在那个项目的单独配置里把它改成 false
android.nonTransitiveRClass=true

# 代码风格
kotlin.code.style=official
