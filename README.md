# LinklanLPA Android SDK 对接文档

**SDK 版本：1.0.11**

**包名：`com.linklan.euicc`**

## 目录

- [1. 概述](#1-概述)
- [2. 环境要求](#2-环境要求)
- [3. 集成配置](#3-集成配置)
  - [3.1 权限配置](#31-权限配置)
  - [3.2 依赖配置](#32-依赖配置)
- [4. 核心数据模型](#4-核心数据模型)
  - [4.1 BleDevice](#41-bledevice)
  - [4.2 EuiccDevice](#42-euiccdevice)
  - [4.3 ProfileInfo](#43-profileinfo)
  - [4.4 EuiccResult&lt;T&gt;](#44-euiccresultt)
- [5. API 参考](#6-api-参考)
  - [5.1 初始化](#61-初始化)
  - [5.2 BLE 扫描](#62-ble-扫描)
  - [5.3 设备连接与断开](#63-设备连接与断开)
  - [5.4 设备信息](#64-设备信息)
  - [5.5 设备重置](#65-设备重置)
  - [5.6 Profile 管理](#66-profile-管理)
- [6. 监听器](#7-监听器)
- [7. 错误码说明](#8-错误码说明)
- [8. 完整对接示例](#9-完整对接示例)
- [9. 注意事项](#10-注意事项)

---

## 1. 概述

LinklanLPA 是一款基于 Android BLE（低功耗蓝牙）的 eSIM 设备管理 SDK，提供设备扫描、连接、Profile 下载/启用/禁用/删除、APN 配置等完整能力。



> **核心入口：** `LinklanLPA`（单例 object，全局唯一实例）

---

## 2. 环境要求

| 项目 | 要求 |
|------|------|
| Android 最低版本 | Android 5.0 (API 21) |
| BLE 支持 | 设备需支持 Bluetooth LE |
| 编程语言 | Kotlin（协程） |
| 依赖框架 | AndroidX |
| 权限要求 | 蓝牙相关运行时权限 |

---

## 3. 集成配置

### 3.1 权限配置

在 `AndroidManifest.xml` 中声明以下权限：

```xml
<!-- 蓝牙扫描权限（Android 12+ 需运行时申请） -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<!-- 蓝牙连接权限（Android 12+ 需运行时申请） -->
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<!-- 旧版蓝牙权限（Android 11 及以下） -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<!-- 位置权限（Android 11 及以下扫描需要） -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<!-- 蓝牙硬件特性声明 -->
<uses-feature android:name="android.hardware.bluetooth_le" android:required="true" />
```

> **注意：** Android 12 (API 31) 及以上版本，`BLUETOOTH_SCAN` 和 `BLUETOOTH_CONNECT` 为运行时权限，需在代码中动态申请后再调用 SDK 方法。

### 3.2 依赖配置

SDK 内部使用了 Kotlin 协程，请确保项目已引入协程依赖：

```kotlin
// settings.gradle.kts
repositories {
        maven { url = uri("https://jitpack.io") }
}
```

```kotlin
// build.gradle.kts (app)
dependencies {
    // Kotlin 协程（SDK 依赖）
    implementation("com.github.huangzhangliang:LinklanEuiccAndroid:1.0.11")


    implementation("io.grpc:grpc-okhttp:1.64.0")
    implementation("io.grpc:grpc-protobuf-lite:1.64.0")
    implementation("io.grpc:grpc-stub:1.64.0")
    implementation("com.squareup.okhttp3:okhttp:4.8.0")
    implementation("org.apache.tomcat:annotations-api:6.0.53")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:adapter-rxjava:2.7.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.8.0")


    implementation ("androidx.compose.ui:ui-tooling:1.7.0")
    implementation ("androidx.compose.animation:animation:1.7.0")
    implementation ("androidx.compose.runtime:runtime:1.7.0")
    implementation ("androidx.compose.runtime:runtime-livedata:1.7.0")
    implementation ("androidx.compose.runtime:runtime-rxjava2:1.7.0")
    implementation ("androidx.compose.material3:material3:1.3.2")

    implementation ("androidx.core:core-ktx:1.13.1")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")

    implementation("io.reactivex.rxjava2:rxjava:2.1.12")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.slf4j:slf4j-android:1.7.36")
    implementation("org.bouncycastle:bcprov-jdk15to18:1.68")
}
```

---

## 4. 核心数据模型

### 4.1 BleDevice

**包名：** `com.linklan.euicc`

BLE 扫描发现的设备信息，用于 `connectDevice` 连接入参。

```kotlin
data class BleDevice(
    val address: String,                     // 设备 MAC 地址
    val name: String,                       // 设备名称
    val rssi: Int,                          // 信号强度（dBm）
    val model: String,                      // 设备型号标识
    val scanRecord: ByteArray?,             // 扫描记录原始数据
    val serviceUuids: List<String>?,       // 服务 UUID 列表
    val txPower: Int?,                      // 发射功率（dBm）
    val manufacturerData: SparseArray<ByteArray>?, // 厂商自定义数据
    val lastSeen: Long = System.currentTimeMillis() // 最后发现时间戳
)
```

> 该类的 `equals` / `hashCode` 基于 `address` 字段实现，相同 MAC 地址视为同一设备。

### 4.2 EuiccDevice

**包名：** `com.linklan.euicc`

已连接的 eSIM 设备信息，作为大部分操作的上下文对象。

```kotlin
data class EuiccDevice(
    var eid: String? = null,                // eUICC 标识符 (EID)
    var imei: String? = null,              // 设备 IMEI
    var sn: String? = null,               // 设备序列号
    var name: String? = null,             // 设备名称
    var model: String? = null,            // 设备型号标识
    var deviceName: String? = null,       // 蓝牙广播名称
    var address: String? = null,         // 蓝牙 MAC 地址
    var vendor: String? = null,          // 设备厂商
    var freeVolatileMemory: Int? = null,  // 可用易失性内存（KB）
    var freeNonVolatileMemory: Int? = null // 可用非易失性内存（KB）
)
```

### 4.3 ProfileInfo

**包名：** `com.linklan.euicc`

eSIM Profile（配置文件）信息。

```kotlin
data class ProfileInfo(
    var iccid: String?,                    // ICCID（Profile 唯一标识）
    var profileName: String? = null,       // Profile 名称
    var nickname: String? = null,          // 用户自定义昵称
    var profileClass: Int? = null,         // Profile 类别
    var serviceProviderName: String? = null, // 运营商名称
    var policyRules: Int? = null,          // 策略规则
    var state: Int?,                       // Profile 状态（0 = 禁用, 1 = 启用）
    var mcc: String? = null,               // 移动国家码
    var mnc: String? = null,               // 移动网络码
    var imsi: String? = null              // IMSI
)
```

### 4.4 EuiccResult&lt;T&gt;

**包名：** `com.linklan.euicc`

所有异步操作的统一返回包装类型。

```kotlin
data class EuiccResult<T>(
    var code: Int?,          // 结果码（0 = 成功，1 = 失败/不支持，详见错误码表）
    var data: T? = null,     // 返回数据
    var title: String? = null,  // 结果标题
    var msg: String? = null,    // 结果消息
    var info: String? = null    // 附加信息
)
```

---


## 5. API 参考

### 5.1 初始化

#### `init`

```kotlin
fun init(context: Context, debug: Boolean)
```

**说明：** 初始化 SDK，设置全局 Context 和调试模式开关。必须在调用任何其他 API 之前调用。

| 参数 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| context | Context | 是 | Application 或 Activity Context |
| debug | Boolean | 是 | 是否开启调试日志 |

**示例：**

```kotlin
LinklanLPA.init(context = applicationContext, debug = true)
```

> 建议在 `Application.onCreate()` 中调用。

---

### 5.2 BLE 扫描

#### `startScan`

```kotlin
fun startScan(bleScanCallback: BleScanCallback)
```

**说明：** 开始扫描 BLE 设备，扫描结果通过回调返回。

| 参数 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| bleScanCallback | BleScanCallback | 是 | 扫描结果回调 |

#### `stopScan`

```kotlin
@RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
fun stopScan()
```

**说明：** 停止 BLE 扫描。

**示例：**

```kotlin
// 开始扫描
LinklanLPA.startScan(bleScanCallback = object : BleScanCallback {
    override fun onScanResult(device: BleDevice) {
        // 处理扫描到的设备
        Log.d("Scan", "发现设备: ${device.name} (${device.address}) model=${device.model}")
    }
})

// 停止扫描
LinklanLPA.stopScan()
```

---

### 5.3 设备连接与断开

#### `connectDevice`

```kotlin
@RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN, BLUETOOTH_CONNECT])
suspend fun connectDevice(device: BleDevice?): EuiccResult<EuiccDevice>
```

**说明：** 连接 BLE 设备。根据 `device.model` 自动路由到对应客户端。连接成功后返回 `EuiccDevice` 对象（已填充 name 和 model）。

| 参数 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| device | BleDevice? | 否 | 扫描到的设备对象，`null` 或 model 不匹配时返回 `code = 1` |

**返回：** `EuiccResult<EuiccDevice>`

#### `checkDeviceConnection`

```kotlin
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun checkDeviceConnection(device: EuiccDevice?): Boolean
```

**说明：** 检查设备是否已连接。

| 参数 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| device | EuiccDevice? | 否 | 已连接的设备对象 |

**返回：** `Boolean` — `true` 表示已连接，`false` 表示未连接

#### `disconnectDevice`

```kotlin
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun disconnectDevice(device: EuiccDevice?)
```

**说明：** 断开与设备的蓝牙连接。

| 参数 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| device | EuiccDevice? | 否 | 要断开的设备对象 |

**示例：**

```kotlin
// 连接设备（在协程中调用）
val result = LinklanLPA.connectDevice(device = bleDevice)
if (result.code == 0) {
    val euiccDevice = result.data!!
    Log.d("Connect", "连接成功: ${euiccDevice.name}")
}

// 检查连接状态
val connected = LinklanLPA.checkDeviceConnection(euiccDevice)

// 断开连接
LinklanLPA.disconnectDevice(euiccDevice)
```

---

### 5.4 设备信息

#### `getDeviceInfo`

```kotlin
@RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN, BLUETOOTH_CONNECT])
suspend fun getDeviceInfo(device: EuiccDevice): EuiccResult<EuiccDevice>
```

**说明：** 获取设备的详细信息（EID、IMEI、SN、内存等）。需先连接设备。

| 参数 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| device | EuiccDevice | 是 | 已连接的设备对象，`address` 不能为空 |

**返回：** `EuiccResult<EuiccDevice>` — `data` 中包含完整的设备信息

**示例：**

```kotlin
val result = LinklanLPA.getDeviceInfo(euiccDevice)
if (result.code == 0) {
    val info = result.data!!
    Log.d("DeviceInfo", "EID: ${info.eid}")
    Log.d("DeviceInfo", "IMEI: ${info.imei}")
    Log.d("DeviceInfo", "SN: ${info.sn}")
    Log.d("DeviceInfo", "可用易失内存: ${info.freeVolatileMemory} KB")
}
```

---

### 5.5 设备重置

#### `resetDevice`

```kotlin
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
suspend fun resetDevice(device: EuiccDevice?, duration: Long = 15000L)
```

**说明：** 重置设备



| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|:----:|:------:|------|
| device | EuiccDevice? | 否 | — | 目标设备 |
| duration | Long | 否 | 15000L | 等待时长（毫秒），仅对 bs/ty 生效 |

> 该方法内部使用 `CountDownLatch` 等待，最大超时 30 秒。

---

### 5.6 Profile 管理

#### `getProfileInfoList`

```kotlin
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
suspend fun getProfileInfoList(device: EuiccDevice?): EuiccResult<List<ProfileInfo>>
```

**说明：** 获取设备上所有 Profile 列表。

| 参数 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| device | EuiccDevice? | 否 | 已连接的设备对象 |

**返回：** `EuiccResult<List<ProfileInfo>>`

#### `enableProfile`

```kotlin
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
suspend fun enableProfile(device: EuiccDevice?, iccid: String?): EuiccResult<String>
```

**说明：** 启用指定的 Profile。启用成功后，SDK 会根据型号自动重置设备：

| 参数 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| device | EuiccDevice? | 否 | 已连接的设备对象 |
| iccid | String? | 否 | 目标 Profile 的 ICCID |

**返回：** `EuiccResult<String>`

#### `disableProfile`

```kotlin
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
suspend fun disableProfile(device: EuiccDevice?, iccid: String?): EuiccResult<String>
```

**说明：** 禁用指定的 Profile。禁用后的重置策略与 `enableProfile` 一致。

#### `deleteProfile`

```kotlin
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
suspend fun deleteProfile(device: EuiccDevice?, iccid: String?): EuiccResult<String>
```

**说明：** 删除指定的 Profile。

> 删除操作完成后不会自动重置设备。

#### `downloadProfile`

```kotlin
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
suspend fun downloadProfile(device: EuiccDevice?, activeCode: String?, confirmCode: String?): EuiccResult<String>
```

**返回：** `EuiccResult<String>` — `data` 为下载的 Profile 的 ICCID（ty 型号启用时也使用此 ICCID）

**示例：**

```kotlin
// 获取 Profile 列表
val listResult = LinklanLPA.getProfileInfoList(euiccDevice)
if (listResult.code == 0) {
    listResult.data!!.forEach { profile ->
        Log.d("Profile", "ICCID: ${profile.iccid}, 运营商: ${profile.serviceProviderName}, 状态: ${if (profile.state == 1) "已启用" else "已禁用"}")
    }
}

// 下载 Profile
val downloadResult = LinklanLPA.downloadProfile(
    device = euiccDevice,
    activeCode = "LPA:1$smdpAddress$matchingId",
    confirmCode = null
)
if (downloadResult.code == 0) {
    Log.d("Download", "下载成功, ICCID: ${downloadResult.data}")
}

// 启用 Profile
LinklanLPA.enableProfile(euiccDevice, iccid = "8901234567890123456")

// 删除 Profile
LinklanLPA.deleteProfile(euiccDevice, iccid = "8901234567890123456")
```

---

## 6. 监听器

SDK 提供以下可选监听器，通过 `LinklanLPA` 的公共属性设置：

#### `bleDeviceConnectionListener`

```kotlin
var bleDeviceConnectionListener: BleDeviceConnectionListener?
```

**说明：** 蓝牙设备连接状态变化监听器。

#### `userAuthenticationListener`

```kotlin
var userAuthenticationListener: BleUserAuthenticationListener?
```

**说明：** 蓝牙用户认证监听器。

**示例：**

```kotlin
LinklanLPA.bleDeviceConnectionListener = object : BleDeviceConnectionListener {
    override fun onStatusChange(address: String?, status: String) {
        Log.d("BLE", "地址: ${address} 状态: ${status}")
    }
}
```

> 监听器接口的具体方法签名请参照 SDK 中 `com.linklan.euicc.ble` 包下的 `BleDeviceConnectionListener` 和 `BleUserAuthenticationListener` 接口定义。

---

## 7. 错误码说明

| code | 含义 | 说明 |
|:----:|------|------|
| 0 | 成功 | 操作成功完成，`data` 中包含返回数据 |
| 1 | 失败 | 操作失败，可能原因：设备未连接、model 不匹配、设备不支持该功能 |

> 部分操作可能返回其他非零码，由底层客户端实现决定。建议通过 `msg` 和 `info` 字段获取详细错误信息。

---

## 8. 完整对接示例

以下示例展示从初始化到 Profile 下载启用的完整流程：

```kotlin
import com.linklan.euicc.LinklanLPA
import com.linklan.euicc.ble.BleDevice
import com.linklan.euicc.ble.BleScanCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EuiccManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)

    fun setup() {
        // 1. 初始化 SDK
        LinklanLPA.init(context = context, debug = true)
    }

    fun startScan() {
        // 2. 开始扫描设备
        LinklanLPA.startScan(bleScanCallback = object : BleScanCallback {
            override fun onScanResult(device: BleDevice) {
                // 筛选目标设备（按名称或 model）
                if (device.name.contains("Linklan")) {
                    LinklanLPA.stopScan()
                    connectAndManage(device)
                }
            }
        })
    }

    private fun connectAndManage(bleDevice: BleDevice) {
        scope.launch {
            // 3. 连接设备
            val connectResult = LinklanLPA.connectDevice(bleDevice)
            if (connectResult.code != 0) {
                Log.e("Euicc", "连接失败: ${connectResult.msg}")
                return@launch
            }
            val device = connectResult.data!!

            // 4. 获取设备信息
            val infoResult = LinklanLPA.getDeviceInfo(device)
            if (infoResult.code == 0) {
                val info = infoResult.data!!
                Log.i("Euicc", "EID: ${info.eid}, IMEI: ${info.imei}")
            }

            // 5. 获取 Profile 列表
            val profilesResult = LinklanLPA.getProfileInfoList(device)
            if (profilesResult.code == 0) {
                profilesResult.data!!.forEach { p ->
                    Log.i("Euicc", "Profile: ${p.iccid} - ${p.serviceProviderName} - 状态:${p.state}")
                }
            }

            // 6. 下载新 Profile
            val downloadResult = LinklanLPA.downloadProfile(
                device = device,
                activeCode = "LPA:1\$smdp.example.com\$activationCode123",
                confirmCode = null
            )
            if (downloadResult.code == 0) {
                Log.i("Euicc", "下载成功, ICCID: ${downloadResult.data}")

                // 7. 启用 Profile（ty 型号下载后会自动启用，可跳过）
                if (device.model != "ty") {
                    LinklanLPA.enableProfile(device, downloadResult.data)
                }
            }

            // 8. 完成后断开连接
            LinklanLPA.disconnectDevice(device)
        }
    }
}
```

---

## 9. 注意事项

1. **初始化顺序：** 必须先调用 `LinklanLPA.init()` 完成初始化，再使用其他任何 API。

2. **协程调用：** 带 `suspend` 修饰符的方法（`connectDevice`、`getDeviceInfo`、`getProfileInfoList`、`enableProfile`、`disableProfile`、`deleteProfile`、`downloadProfile`、`resetDevice`、`setApdu`）必须在协程作用域中调用。

3. **运行时权限：** Android 12+ 需运行时动态申请 `BLUETOOTH_SCAN` 和 `BLUETOOTH_CONNECT` 权限，否则调用会抛出 `SecurityException`。

4. **自动重置行为：** `enableProfile`、`disableProfile`、`downloadProfile` 在操作成功后会自动触发设备重置。重置期间设备蓝牙可能短暂断开，需等待重置完成后再进行后续操作。

5. **蓝牙扫描生命周期：** 在 Activity/Fragment 销毁时（`onDestroy`）调用 `stopScan()` 和 `disconnectDevice()` 释放资源，避免蓝牙泄漏。

6. **内存与 Context：** `LinklanLPA` 持有 `Context` 引用，建议传入 `ApplicationContext` 避免 Activity 泄漏。
