package cn.mudlife.app.model

import androidx.compose.ui.graphics.Color

data class DeviceInfo(
    val deviceId: Int,
    val deviceName: String,
    val snCode: String,
    val macAddress: String,
    val withholdMoney: Double,
    val onlineStatusId: Int,
    val projectId: Int? = null,
    val bigTypeId: Int? = null,
    val smallTypeId: Int? = null,
    val bigTypeName: String? = null,
    val smallTypeName: String? = null
) {
    val displayName: String get() = formatDeviceName(deviceName)
    val locationOnly: String get() = displayName

    val isDrinkingWater: Boolean get() =
        bigTypeId == 5 || deviceName.contains("饮水") || deviceName.contains("冷水") || (deviceName.contains("热水") && !deviceName.startsWith("热水器") && !deviceName.startsWith("热水表"))

    val typeEmoji: String get() = if (isDrinkingWater) "🚰" else "🚿"

    val typeName: String get() = if (isDrinkingWater) "直饮水" else "出水设备"

    val actionText: String get() = if (isDrinkingWater) "开始饮水" else "开始出水"

    val stopText: String get() = if (isDrinkingWater) "结束饮水" else "结束出水"

    val startingText: String get() = if (isDrinkingWater) "正在开启饮水机..." else "正在开启设备..."

    val stoppingText: String get() = if (isDrinkingWater) "正在关闭饮水机..." else "正在关闭设备..."

    val typeColor: Color get() = Color(0xFF0284C7)

    val statusText: String get() = "正在出水中"

    companion object {
        fun formatDeviceName(name: String): String {
            return name
                .replace(Regex("^(直饮)?(开水|冷水|热水|温水)[- ]*"), "")
                .replace(Regex("^直饮[冷热]?水[- ]*"), "")
                .replace(Regex("^热水[器表][- ]*"), "")
                .replace("-", " ")
                .trim()
        }
    }
}

data class RecentDevice(
    val name: String,
    val mac: String,
    val snCode: String,
    val emoji: String = "🚰",
    val waterType: String = "冷水",
    val projectId: String? = null
) {
    val isHot: Boolean get() = waterType == "热水" || waterType == "开水" || name.contains("热") || name.contains("开水") || snCode.contains(",M,")
    val cleanName: String get() = DeviceInfo.formatDeviceName(name).ifEmpty { if (isHot) "热水直饮机" else "冷水直饮机" }
}
