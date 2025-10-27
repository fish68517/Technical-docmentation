### 创建一个孤立干净的 git 分支
git checkout --orphan adb工具命令
git rm -rf .  # 删除所有跟踪的文件（让目录完全空）
git commit --allow-empty -m "Initial empty commit for adb工具命令"  # 创建一个空提交，作为起点

### gitbash 无法通过 https协议连接 githubu
Administrator@WIN-MAB9DRR1MTC MINGW64 /d/Acode/Android/complete/AA项目归档/作业_Android_Sqlite_普通项目/Android项目/HomeWork_Android_Sqlite_Repository2 (Android基础中级高级项目)
$ git push github -u Android基础中级高级项目 -f
fatal: unable to access 'https://github.com/fish68517/Android_sqlite.git/': OpenSSL SSL_read: SSL_ERROR_SYSCALL, errno 0
在gitbash 中提交代码报错了，但是我可以直接登录 github 网站

好的，这是一个非常经典并且有点棘手的 Git 报错。我们来详细分析一下。

你观察到的现象非常关键：“我可以**直接登录 github 网站**”。这说明：
*   你的网络连接是通的。
*   DNS 解析 `github.com` 是正常的。
*   你的 IP 没有被 GitHub 的主站防火墙封禁。

这直接将问题范围缩小到了 **你的 Git 客户端与 GitHub 服务器之间建立安全连接（HTTPS/SSL）的过程中**。

### 错误分析

**`fatal: unable to access 'https://github.com/fish68517/Android_sqlite.git/'`**
这部分很明确，Git 客户端无法访问这个远程 HTTPS 地址。

**`OpenSSL SSL_read: SSL_ERROR_SYSCALL, errno 0`**
这是问题的核心，也是最棘手的部分。我们来分解它：
*   **`OpenSSL`**: 说明错误发生在处理 SSL/TLS 加密连接的环节。
*   **`SSL_read`**: 表示在读取服务器返回的加密数据时，连接出错了。
*   **`SSL_ERROR_SYSCALL`**: 这是一个非常底层的错误。它意味着问题不是 SSL 协议本身（比如证书错误），而是在执行 SSL 操作时，底层的系统调用（System Call）失败了。
*   **`errno 0`**: 通常意味着 “End of File (EOF)”，可以理解为在你期望接收数据的时候，对方（GitHub 服务器或中间的网络设备）**意外地、提前关闭了连接**。

**综合起来，这个错误的本质是：你的 Git 客户端正在和 GitHub 服务器进行加密通信，但话还没说完，连接就被不明原因地切断了。**

### 主要原因及解决方案

根据经验，这个问题 99% 的可能性出在你的**本地网络环境**或 **Git 配置**上，而不是 GitHub 服务器本身。请按以下顺序排查和解决，可能性从高到低排列：

#### 方案一：检查代理、VPN 和防火墙 (最常见原因)

这是导致连接被意外中断的首要原因。
1.  **公司/学校网络代理**：如果你在公司或学校的网络环境中，几乎肯定有一个网络代理在中间。这个代理可能会干扰或重置它无法正确解析的 SSL 连接。
    *   **解决方案**：为 Git 配置正确的 HTTP/HTTPS 代理。打开 Git Bash 执行：
        ```bash
        # 替换 your_proxy_address 和 port 为你的代理服务器地址和端口
        git config --global http.proxy http://your_proxy_address:port
        git config --global https.proxy https://your_proxy_address:port

        # 如果代理需要用户名和密码
        # git config --global http.proxy http://username:password@your_proxy_address:port
        # git config --global https.proxy https://username:password@your_proxy_address:port
        ```
    *   如果你不需要代理了，记得取消设置：
        ```bash
        git config --global --unset http.proxy
        git config --global --unset https.proxy
        ```

2.  **VPN 或加速器**：如果你正在使用 VPN 或任何网络加速器，请**先关闭它们**再试一次。它们的路由节点可能不稳定，导致连接中断。

3.  **本地防火墙/杀毒软件**：Windows Defender 防火墙或任何第三方杀毒软件（如 360、火绒、Avast 等）可能会拦截 Git 的网络请求。
    *   **解决方案**：**暂时禁用**你的防火墙和杀毒软件，然后再次执行 `git push`。如果成功了，说明就是它们的问题。之后，你应该在防火墙和杀毒软件中为 Git 添加例外规则，而不是长期关闭它们。

#### 方案二：调整 Git 的 HTTP 缓冲区大小 (非常有效)

如果你的项目比较大，或者本次提交的文件比较多，Git 在打包和发送数据时可能会因为默认的缓冲区太小而导致连接超时被服务器切断。
*   **解决方案**：在 Git Bash 中执行以下命令，将缓冲区调大（例如调到 500MB）。
    ```bash
    git config --global http.postBuffer 524288000
    ```
    (524288000 字节 = 500 * 1024 * 1024)
    这个命令只需要执行一次。设置完成后，再次尝试 `git push`。

#### 方案三：切换 Git 的 SSL 后端 (简单易行)

Git for Windows 可以使用不同的库来处理 SSL 连接。默认是 OpenSSL，但有时切换到 Windows 自带的 Secure Channel (SChannel) 库可以解决问题。
*   **解决方案**：
    ```bash
    # 切换到 Windows Secure Channel
    git config --global http.sslBackend schannel

    # 然后再试一次 git push
    ```
    如果不行，你随时可以切换回来：
    ```bash
    # 切换回 OpenSSL
    git config --global http.sslBackend openssl
    ```

#### 方案四：更换协议，使用 SSH 代替 HTTPS (一劳永逸的终极方案)

如果你频繁遇到 HTTPS 的问题，最稳定、最推荐的长期解决方案是放弃 HTTPS 协议，改用 SSH 协议进行推送。SSH 连接通常更稳定，且不受许多网络代理的干扰。

1.  **生成 SSH 密钥**（如果还没有的话）：
    ```bash
    ssh-keygen -t rsa -b 4096 -C "your_email@example.com"
    # 一路回车即可
    ```

2.  **获取公钥**：
    ```bash
    cat ~/.ssh/id_rsa.pub
    # 复制输出的所有内容，以 ssh-rsa 开头
    ```

3.  **添加到 GitHub**：
    *   登录 GitHub 网站，点击右上角头像 -> Settings -> SSH and GPG keys。
    *   点击 "New SSH key"，将你复制的公钥粘贴进去，标题随便写。

4.  **修改远程仓库地址**：
    *   首先查看你当前的远程仓库名（你的命令里是 `github`）：
        ```bash
        git remote -v
        # 你会看到类似 github  https://github.com/fish68517/Android_sqlite.git (fetch)
        ```
    *   执行命令，将 HTTPS 地址改为 SSH 地址：
        ```bash
        git remote set-url github git@github.com:fish68517/Android_sqlite.git
        ```
        **注意**：格式是 `git@github.com:用户名/仓库名.git`。

5.  **再次推送**：
    现在再执行你的 push 命令，它会通过 SSH 协议进行，大概率会成功。
    ```bash
    git push github -u Android基础中级高级项目 -f
    ```

### 总结和行动建议

我建议你按以下顺序尝试：

1.  **关闭所有 VPN 和网络加速器**，再试一次。
2.  执行 **方案二** 的命令 `git config --global http.postBuffer 524288000`，再试一次。这是最快的修复方法之一。
3.  如果不行，执行 **方案三** 的命令 `git config --global http.sslBackend schannel`，再试一次。
4.  如果还在公司/学校，确认并配置 **方案一** 中的代理。
5.  如果以上都失败了，花点时间配置 **方案四 (SSH)**，这可以从根本上解决这类 HTTPS 连接问题。

### git log 查看提交记录
git log --graph --pretty=format:"%C(yellow)%h%C(reset) -%C(auto)%d%C(reset) %s %C(green)(%ar) %C(bold blue)<%an>%C(reset)"
git config --global alias.lgAll 'log --graph --pretty=format:"%C(yellow)%h%C(reset) -%C(auto)%d%C(reset) %s %C(green)(%ar) %C(bold blue)<%an>%C(reset)"'

绝对可以！这是一个非常棒的进阶想法，通过组合 Git 的能力和 Shell 的能力，我们可以实现你想要的两种功能：**过滤**和**高亮**。

我们将分两部分来实现：

1.  **文本过滤**：这是 `git log` 的内置功能，非常简单。
2.  **文本高亮**：这需要一点点 Shell 技巧，通过管道 (`|`) 将 `git log` 的输出交给另一个命令来处理。

---

### Part 1: 文本过滤 (Git 内置功能)

你的别名 `lgAll` 只是定义了**输出的格式**，你完全可以在使用它的时候，像使用普通的 `git log` 一样，在后面追加**过滤选项**。

这不需要修改你的别名，只需要在使用时加上参数即可。

#### 常用过滤示例：

1.  **过滤提交信息 (Comment)**:
    只想看提交信息中包含 "fix" 或 "修复" 的记录。

    ```bash
    # 使用 --grep 选项
    git lgAll --grep="fix"
    git lgAll --grep="修复"
    ```

2.  **过滤作者 (Author)**:
    只想看某个特定作者的提交。

    ```bash
    # 使用 --author 选项
    git lgAll --author="John Doe"
    ```

3.  **过滤文件**:
    只想看和某个特定文件相关的提交历史。

    ```bash
    git lgAll -- path/to/your/File.java
    ```

4.  **过滤日期**:
    只想看最近一周的提交。

    ```bash
    git lgAll --since="1 week ago"
    ```

**你可以自由组合这些过滤条件**，例如，查找 "John Doe" 在最近一周内提交的包含 "fix" 的记录：
```bash
git lgAll --author="John Doe" --since="1 week ago" --grep="fix"
```

---

### Part 2: 文本高亮 (Git + Shell 管道)

这个更有趣。`git log` 本身不能高亮提交信息中的特定**内容**，但我们可以把它输出的结果通过**管道 `|`** 发送给 `grep` 命令，让 `grep` 来负责高亮。

`grep` 命令不仅能过滤，还能用颜色标记出它匹配到的文本。

#### 核心思路

`git lgAll | grep --color=always "要高亮的词"`

*   `|` (管道符): 将左边命令的输出，作为右边命令的输入。
*   `grep --color=always "..."`: `grep` 会查找并高亮指定的文本。`--color=always` 是为了确保即使在管道中，`grep` 也会强制输出颜色。

#### 问题与解决方案

直接使用 `grep` 会**过滤掉**不包含关键词的行，但这可能不是我们想要的。我们希望**显示所有提交，仅仅高亮**匹配的那些。

解决方法是使用一个 `grep` 的小技巧，让它匹配“你要高亮的词”**或者**“每一行的开头”，这样所有行都会被匹配到，但只有你关心的词会被上色。

#### 创建一个支持高亮的别名 (`hl`, Highlight Log)

我们将创建一个新的别名 `hl`，它会接受一个参数（你要高亮的词），并实现“全部显示+高亮”的效果。

**步骤一：配置 `hl` 别名**

这个别名需要执行 Shell 命令，所以必须以 `!` 开头。

```bash
git config --global alias.hl "!f() { git lgAll | grep -E --color=always --ignore-case \"$1|^\" || true; }; f"
```

**命令解释 (有点复杂，但很强大):**
*   `!f() { ... }; f`: 这是一个在 Git 别名中定义并立即执行 Shell 函数的标准模式，目的是为了能接收参数。
*   `git lgAll | ...`: 执行你原来的 `lgAll` 命令，并将结果通过管道传出。
*   `grep -E --color=always --ignore-case \"$1|^\"`: 这是核心。
    *   `-E`: 使用扩展正则表达式。
    *   `--ignore-case`: 忽略大小写，`fix` 和 `Fix` 都能匹配。
    *   `\"$1|^\"`:
        *   `$1`: 表示你传给别名的**第一个参数** (你要高亮的词)。
        *   `|`: 在正则表达式中表示 "或者"。
        *   `^`: 表示一行的开头。
        *   所以整个模式的意思是：匹配“你给的词”**或者**“一行的开头”。
*   `|| true`: 这是一个安全措施。如果 `grep` 什么都没匹配到（比如日志为空），它会返回一个错误码，`|| true` 可以防止 Git 报错。

**步骤二：如何使用 `hl` 别名**

现在，你可以用这个新别名来高亮你想看的任何关键词了。

**示例:**

1.  高亮所有 "fix" 相关的提交：
    ```bash
    git hl fix
    ```

2.  高亮所有和 "bug" 或 "issue" 相关的提交（使用正则表达式）：
    ```bash
    git hl "bug|issue"
    ```

3.  高亮所有和 "feat" (新功能) 相关的提交：
    ```bash
    git hl feat
    ```

你的终端会显示出完整的 `lgAll` 记录，但所有匹配到你关键词的地方都会被鲜艳的颜色标记出来，让你一目了然！

### 总结

你现在已经解锁了 `git log` 的两大进阶用法：

| 场景 | 命令 | 示例 |
| :--- | :--- | :--- |
| **只想看**包含 "fix" 的提交 | **过滤** (内置功能) | `git lgAll --grep="fix"` |
| **看所有**提交，并**高亮** "fix" | **高亮** (新别名 `hl`) | `git hl fix` |

这两个工具结合起来，能让你在海量的提交历史中快速定位和分析信息，极大地提升效率。

# Git 配置查看指南

本文档介绍了如何在 Git Bash 中查看 Git 的各种配置，包括系统、全局和本地配置。

## 1. 查看所有 Git 配置

此命令会列出所有生效的 Git 配置，包括系统、全局和本地配置。如果同一个配置项在不同级别有定义，则优先级高的会覆盖优先级低的，此命令显示的是最终生效的值。

```bash
git config --list

### 6. 查找文件
git lgFile app\src\main\java\com\patac\launcher\Launcher.java
