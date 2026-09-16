# 泥浆生活 (MudLife) — 跨智能体协同开发规范与工程知识库

> 本文件是本项目（MudLife）的核心知识基线与智能体行为准则。
> 无论你是 **Codex (GPT)** 还是 **Antigravity (Gemini)**，在接手本项目的任何需求、代码审查或方案制定前，必须完整遵循本规范。

---

## 一、 协作角色定位与分工模式

本项目采用 **“GPT 架构/审查/攻坚 + Gemini 实施/调试/落地”** 的串行协同模式，严禁双模型同时并发修改同一文件：

```
[用户提出需求/Bug]
       │
       ▼
┌─────────────────────────────────────────────────────────────┐
│ 🧠 Codex (GPT) 职责：大脑与审核者                            │
│ 1. 深度分析业务逻辑、逆向比对抓包/官方反编译代码             │
│ 2. 制定高可靠实施方案（明确改动文件、类、函数、逻辑分支）     │
│ 3. 审查与核实 Gemini 的代码变更与设计合理性                 │
└──────────────────────────────┬──────────────────────────────┘
                               │ 输出清晰实施方案
                               ▼
┌─────────────────────────────────────────────────────────────┐
│ 🛠️ Antigravity (Gemini) 职责：工兵与落地者                   │
│ 1. 严格承接并落地 GPT 确认的实施方案                         │
│ 2. 本地自动化编译、Gradle 依赖构建、代码编写                 │
│ 3. 调度真机 ADB、无线自连、推送覆装与实机交互验证            │
└─────────────────────────────────────────────────────────────┘
```

---

## 二、 核心业务底层与技术“暗坑”（方案制定必读）

GPT 在制定方案、检查逻辑时，**绝不可违背以下底层硬事实**：

### 1. 设备通信绝不走手机 BLE
* **事实**：热水器自带 4G 模组。手机 App **完全通过云端 HTTP API (`v3-api.china-qzxy.cn`)** 控制热水器，云端通过 4G 转发下发开阀/关阀指令。
* **BLE 的唯一作用**：近场扫码与广播发现，仅用于获取设备的 MAC 地址以换取 `snCode`。绝不要尝试用手机 BLE GATT 读写特征值来控制水阀！
* **实时推送**：开阀成功、扣费更新走 MQTT 服务器（`tcp://47.107.37.60:1883`），不走蓝牙。

### 2. 密码加密算法非标
* **算法**：`MD5(明文密码) -> 取末尾 10 字符 -> 全部转为大写`。
* 严禁修改为标准 32 位 MD5，否则将导致所有用户登录凭证失效。

### 3. 订单状态判定逻辑
* 接口 `POST /order/tcpDevice/query/rateOrder/using`：
  * 当用户有正在进行的洗澡/用水订单时，后端接口会返回 **`errorCode = 307`**。
  * **307 在本项目中代表“有进行中订单”的正常业务状态**，`data.orderNo` 即订单号，绝对不能作为网络请求失败拦截吞掉！

### 4. 吹风机使用码机制
* 吹风机采用 8 位物理键盘码。
* 换码流程包含：生成候选码（每日限次）-> 提交应用候选码 -> 同步状态开关。

---

## 三、 工程结构与文件地图

* **项目物理根目录**：`C:\Users\炎小黄\.gemini\antigravity\scratch\MudLife`
* **包名**：`cn.mudlife.app`
* **技术栈**：Kotlin + Jetpack Compose + Material 3 + Liquid Glassmorphism

```
app/src/main/
├── AndroidManifest.xml                    # 清单文件、权限与 Activity 声明
└── java/cn/mudlife/app/
    ├── MainActivity.kt                    # 单 Activity 架构、主导航与全局主题弹窗
    ├── QrScanActivity.kt                  # 扫码界面 (CameraX + MLKit Barcode)
    │
    ├── api/                               # 网络层
    │   ├── QzxyService.kt                 # Retrofit 接口定义（核心 API 集合）
    │   ├── NetworkModule.kt               # OkHttp 客户端、Token/Sign 拦截器
    │   └── SafeApi.kt                     # 防 R8 混淆泛型擦除的手动安全解析扩展
    │
    ├── data/
    │   └── AuthRepository.kt              # 用户凭证存储 (EncryptedSharedPreferences)
    │
    ├── model/                             # 数据模型
    │   ├── LoginModels.kt                 # 登录、Token、基础 BaseResponse
    │   ├── DeviceModels.kt                # 设备详情、热水器/吹风机模型
    │   ├── ActiveOrder.kt                 # 活跃订单状态模型
    │   └── MqttModels.kt                  # MQTT 消费推送报文
    │
    └── ui/                                # 界面与表现层
        ├── theme/                         # Liquid Glassmorphism 拟态玻璃主题
        ├── LoginScreen.kt & LoginViewModel.kt
        ├── MainScreen.kt & MainViewModel.kt    # 核心用水主控台、开/关阀状态机
        ├── WalletScreen.kt & WalletViewModel.kt
        └── DryerCodeDialog.kt             # 吹风机使用码弹窗
```

---

## 四、 本地环境与工具链锚点（严禁臆造路径）

Gemini 执行构建或 GPT 编写执行指令时，必须使用以下固化路径：

| 组件/工具 | 本地固定绝对路径 / 命令 | 作用 |
|---|---|---|
| **ADB 锚点** | `C:\工具\platform-tools\adb.exe` | Android 调试桥，严禁全盘检索 |
| **无线自连脚本** | `python "C:\工具\platform-tools\adb_auto_connect.py"` | 自动捕获局域网 Android 动态端口建连 |
| **Android SDK** | `C:\Android\Sdk` | SDK 根目录 |
| **永久专属签名** | `C:\Users\炎小黄\.gemini\antigravity\scratch\MudLife\mudlife.jks`<br>别名：`mudlife`，密码：`123456` | 永久统一证书，严禁改动，确保真机可无缝覆盖更新 |
| **一键编译覆装** | 项目根目录下 `deploy.bat` | 自动增量编译 Release APK + ADB 连接 + 覆盖安装 |
| **手工构建命令** | `.\gradlew.bat assembleRelease` | 编译标准 Release 产物 |

---

## 五、 GPT 方案制定规范（交付标准）

当 GPT 负责“制定方案 / 解决难题”时，方案应遵循以下结构，以便 Gemini 直接无歧义执行：

1. **Bug/需求根因分析**：说明现象背后的本质逻辑（附带抓包或反编译比对证据）。
2. **影响文件列表**：明确需要修改的绝对/相对路径。
3. **精准代码变更（Diff / 关键逻辑块）**：给出改动前后的逻辑与关键代码，不含糊推测。
4. **边界与副作用防范**：评估是否会破坏持久化缓存、签名机制或现有 MQTT 推送。
5. **验证闭环步骤**：说明 Gemini 在实机上应观察什么现象以确认问题解决。
