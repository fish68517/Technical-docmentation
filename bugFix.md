### 1. 如何查看CUDA 版本
您好，这是查看 CUDA 版本最快、最常用的方法：

请打开您的命令行终端（CMD 或 PowerShell），然后输入以下命令并按回车：

```bash
nvidia-smi
```

运行后，您会在输出的**右上角**看到 CUDA 版本，如下图所示：

```
+-----------------------------------------------------------------------------+
| NVIDIA-SMI 515.65.01    Driver Version: 515.65.01    CUDA Version: 11.7     |
|-------------------------------+----------------------+----------------------+
| GPU  Name        Persistence-M| Bus-Id        Disp.A | Volatile Uncorr. ECC |
...
```

在这个例子中，CUDA 版本就是 **11.7**。

**请注意：**

  * `nvidia-smi` 显示的是您的 NVIDIA 显卡**驱动**所支持的**最高** CUDA 版本。
  * 您实际在 Conda 环境中安装的 `pytorch` 所使用的 CUDA 版本可能**低于**这个版本（例如，您的驱动支持 12.2，但 `pytorch` 使用的是 11.8），这是完全正常的。

对于我们上一步的 `pip install` 命令，您可以使用 `nvidia-smi` 看到的版本（例如 11.7, 11.8, 12.1...）对应的 `cuXXX` 标签（例如 `cu117`, `cu118`, `cu121`）。我们使用的 `cu118` 是一个非常通用的版本，很有可能与您的驱动程序兼容。

### 2. 如何升级CUDA版本
感谢您提供这个关键信息。

这是一个**核心冲突**，我们必须先解决它。

### 问题分析

1.  **项目要求**：`requirements.txt` 文件指定需要 `torch==2.0.1`。
2.  **PyTorch 的限制**：PyTorch `2.0.1` 版本的预编译包仅支持 CUDA **11.7** 或 **11.8**。
3.  **您的环境**：您的 `nvidia-smi` 显示 `CUDA Version: 11.2`。这意味着您的 NVIDIA 显卡驱动程序**太旧了**，它最高只能支持到 CUDA 11.2，**无法运行**为 CUDA 11.7 或 11.8 编译的程序。

这就是为什么您在尝试安装 `torchvision`（它依赖 `torch`）时会失败。

### 解决方案：更新您的显卡驱动

最正确、最稳定的解决方案是**更新您的 NVIDIA 显卡驱动程序**。

1.  请访问 NVIDIA 官方网站（或使用您电脑上的 "GeForce Experience" 软件）。
2.  下载并安装**最新**的 Game Ready 或 Studio 驱动程序。
3.  安装完成后，**重启您的电脑**。
4.  重启后，再次运行 `nvidia-smi`，您会看到右上角的 `CUDA Version` 已经更新到一个更高的版本（例如 `12.x`）。

**一旦您的驱动更新完毕**，您的显卡就可以向后兼容 PyTorch 11.8 了。请**按顺序**执行以下全新的安装步骤：

-----

### 驱动更新后的完整安装步骤

#### 第 1 步：安装 `torch` 和 `torchvision` (CUDA 11.8 版本)

(请确保您已激活 `fvhuman` conda 环境)
这个命令会从 PyTorch 官方源下载，专门针对 `torch==2.0.1` 和 CUDA 11.8。

```bash
pip install torch==2.0.1 torchvision==0.15.2 --index-url https://download.pytorch.org/whl/cu118 --trusted-host download.pytorch.org
```

#### 第 2 步：安装本地 `clip` 包（不查找依赖）

由于 `torchvision` (clip 的依赖项) 已经在第 1 步装好了，我们现在使用 `--no-deps` 标志来安装您已下载的 `clip.zip`：

```bash
pip install --no-deps CLIP-d50d76daa670286dd6cacf3bcd80b5e4823fc8e1.zip
```

#### 第 3 步：修改 `requirements.txt`

打开 `requirements.txt` 文件，在下面这**三行**前面加上 `#` 号，将它们**全部注释掉**（因为我们已经手动装完了）：

```txt
# clip @ https://github.com/openai/CLIP/archive/d50d76daa670286dd6cacf3bcd80b5e4823fc8e1.zip#...
...
# torch==2.0.1
...
# torchvision
...
```

#### 第 4 步：安装所有剩余的包

现在，我们来安装 `requirements.txt` 中所有剩余的包。我们将继续使用 `--trusted-host` 方案来解决您之前遇到的 SSL 连接问题：

```bash
pip install -r requirements.txt -i https://mirrors.aliyun.com/pypi/simple/ --trusted-host mirrors.aliyun.com
```

（如果您想用清华源，也可以替换为： `pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple --trusted-host pypi.tuna.tsinghua.edu.cn`）

### 3. 我更新 cuda 的版本后，还是报下面的错误
Could not fetch URL https://download.pytorch.org/whl/cu118/torch/: There was a problem confirming the ssl certificate: HTTPSConnectionPool(host='download.pytorch.org', port=443): Max retries exceeded with url: /whl/cu118/torch/ (Caused by SSLError(SSLZeroReturnError(6, 'TLS/SSL connection has been closed (EOF) (_ssl.c:1149)'))) - skipping
ERROR: Could not find a version that satisfies the requirement torch==2.0.1 (from versions: none)
Could not fetch URL https://download.pytorch.org/whl/cu118/pip/: There was a problem confirming the ssl certificate: HTTPSConnectionPool(host='download.pytorch.org', port=443): Max retries exceeded with url: /whl/cu118/pip/ (Caused by SSLError(SSLZeroReturnError(6, 'TLS/SSL connection has been closed (EOF) (_ssl.c:1149)'))) - skipping
ERROR: No matching distribution found for torch==2.0.1

哎，您好。

我非常理解您的沮丧，感谢您在更新驱动后再次尝试。

这个错误日志是**最关键的证据**。请您仔细看：

1.  您尝试从 `download.pytorch.org` (PyTorch官方服务器) 下载。
2.  它失败了，报了 `SSLError(SSLZeroReturnError... 'TLS/SSL connection has been closed (EOF)')`。

### 核心问题分析

这**100% 证明了问题与 CUDA 版本、PyTorch 版本、镜像源（清华、阿里）都无关**。

您电脑的网络环境（**很可能是公司防火墙、网络代理或杀毒软件**）正在**主动拦截和关闭** `pip` 程序发起的所有 `HTTPS (SSL)` 安全连接。

无论 `pip` 尝试连接哪个服务器（`github.com`, `pypi.tuna.tsinghua.edu.cn`, `mirrors.aliyun.com`, 还是 `download.pytorch.org`），它都会被您的本地网络环境强行切断。

### 最终解决方案（“完全手动”安装）

既然 `pip` 无法从任何地方下载文件，我们必须使用**唯一还能正常联网的程序——您的浏览器**——来手动下载所有东西，然后让 `pip` 从您的本地硬盘安装它们。

请您**严格按照**这最后一套步骤操作：

-----

#### 第 1 步：用浏览器手动下载 PyTorch 轮子 (.whl) 文件

请**打开您的浏览器**，复制并粘贴以下两个链接，**手动下载**这两个文件。

(这对应 `torch==2.0.1` 和 `torchvision==0.15.2`，以及 `python=3.8` 和 CUDA 11.8。您的新驱动可以完美兼容它们。)

1.  **Torch:**
    `https://download.pytorch.org/whl/cu118/torch-2.0.1%2Bcu118-cp38-cp38-win_amd64.whl`
2.  **TorchVision:**
    `https://download.pytorch.org/whl/cu118/torchvision-0.15.2%2Bcu118-cp38-cp38-win_amd64.whl`

-----

#### 第 2 步：安装本地的 `torch`, `torchvision` 和 `clip`

1.  将您刚刚下载的两个 `.whl` 文件，以及您**之前就已经下载好**的 `CLIP-d50d76...zip` 文件，**全部移动**到您的 `D:\Acode\...\FVHuman...` 项目根目录中。

2.  打开您的命令行（确保已激活 `(fvhuman)` 环境），`cd` 到该项目目录。

3.  **分三步**运行以下命令。我们使用 `--no-deps` 标志来**强制 `pip` 不要尝试联网**（否则它又会去下载依赖项，然后再次失败）：

    ```bash
    # 1. 安装 torch (不联网)
    pip install --no-deps torch-2.0.1+cu118-cp38-cp38-win_amd64.whl

    # 2. 安装 torchvision (不联网)
    pip install --no-deps torchvision-0.15.2+cu118-cp38-cp38-win_amd64.whl

    # 3. 安装 clip (不联网)
    pip install --no-deps CLIP-d50d76daa670286dd6cacf3bcd80b5e4823fc8e1.zip
    ```

-----

#### 第 3 步：修改 `requirements.txt`

现在，这三个最麻烦的包已经装好了。请打开 `requirements.txt` 文件，在下面这**三行**前面加上 `#` 号，将它们**全部注释掉**：

```txt
# clip @ https://github.com/openai/CLIP/archive/d50d76...
...
# torch==2.0.1
...
# torchvision
...
```

-----

#### 第 4 步：安装所有剩余的依赖

最后，我们来安装 `requirements.txt` 中剩余的**所有其他小包**（例如 `numpy`, `ftfy`, `accelerate` 等）。

我们将最后一次尝试使用 `--trusted-host` 方案来绕过 SSL 检查：

```bash
pip install -r requirements.txt -i https://mirrors.aliyun.com/pypi/simple/ --trusted-host mirrors.aliyun.com
```

**如果（万一）第 4 步还是失败了**，那说明您的网络环境彻底锁死了 `pip`。唯一的办法就是：
您需要**手动**打开 `requirements.txt`，**挨个**去 `https://mirrors.aliyun.com/pypi/simple/` 上用浏览器下载它们（比如 `accelerate-0.21.0-py3-none-any.whl`, `av-11.0.0-cp38-cp38-win_amd64.whl`, `ftfy-6.2.0-py3-none-any.whl`...），然后把它们全放在一个文件夹里，运行 `pip install --no-deps *.whl` 来安装。

### 5. pip install --no-deps torch-2.0.1+cu118-cp38-cp38-win_amd64.whl
Looking in indexes: https://pypi.tuna.tsinghua.edu.cn/simple
既然是在本地安装，为什么还是会 Looking https://pypi.tuna.tsinghua.edu.cn/simple

您好。这是一个非常好的问题，也是一个好消息！

您运行的命令 `pip install --no-deps ...` **已经成功了** (日志显示 `Successfully installed torch-2.0.1+cu118`)。

您之所以还看到 `Looking in indexes` 和最后的 `SSLError`，是因为：

1.  **全局配置：** 您的 `pip` 被**全局配置**为默认使用清华镜像 (`pypi.tuna.tsinghua.edu.cn`)。
2.  **`pip` 的习惯：** 无论您是否需要联网，`pip` 在开始任何安装任务时，**习惯性地**都会先报告 "我正在查看这个索引..." (`Looking in indexes: ...`)。
3.  **`--no-deps` 的作用：** 您添加的 `--no-deps` 标志非常关键。它成功地阻止了 `pip` **为 `torch` 下载依赖项**，因此 `pip` 只是安装了您本地的 `.whl` 文件，**安装成功了**。
4.  **最后的 SSL 错误：** 在 `pip` 成功安装完 `torch` 之后，它尝试执行一项“内务”任务（比如检查一下 `pip` 本身是否有新版本，即日志中的 `/simple/pip/`）。当它尝试这个**无关紧要**的网络连接时，它再次被您的防火墙/网络环境拦截，所以报了 `SSLError`。

**总之：请忽略这个 `SSLError` 警告。您的 `torch` 已经成功安装。**

-----

### 下一步（请立即执行）

您现在应该**立刻继续执行**我们“完全手动”方案的后续步骤。

(请确保您还在 `(fvhuman)` 环境中，并且 `torchvision...whl` 和 `clip...zip` 都在当前目录)

1.  **安装 `torchvision` (不联网)：**

    ```bash
    pip install --no-deps torchvision-0.15.2+cu118-cp38-cp38-win_amd64.whl
    ```

2.  **安装 `clip` (不联网)：**

    ```bash
    pip install --no-deps CLIP-d50d76daa670286dd6cacf3bcd80b5e4823fc8e1.zip
    ```

3.  **修改 `requirements.txt`：**
    （确保 `torch==2.0.1`, `torchvision`, 和 `clip @ https...` 这三行 **都已经被 `#` 注释掉了**）

4.  **安装所有剩余的包：**
    （最后一步，安装所有其他小包，我们将再次尝试使用“受信任的”镜像）

    ```bash
    pip install -r requirements.txt -i https://mirrors.aliyun.com/pypi/simple/ --trusted-host mirrors.aliyun.com
    ```
	
### 6.配置清华和阿里云镜像
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple --trusted-host pypi.tuna.tsinghua.edu.cn
pip install -r requirements.txt -i https://mirrors.aliyun.com/pypi/simple/ --trusted-host mirrors.aliyun.com

### 7. 

