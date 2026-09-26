package com.duplicateapp.testcompanion.domain

object TestScenario {
    const val targetPackage: String = "com.duplicateapp.gamespace"
    const val timeoutMillis: Long = 45_000L

    val continueLabels: List<String> = listOf("Continue", "Tiếp tục")
    val workspaceMarkers: List<String> = listOf("Add game", "Thêm game", "Add your first game", "Thêm game đầu tiên")
    val settingsLabels: List<String> = listOf("Settings", "Cài đặt")
    val settingsMarkers: List<String> = listOf("Appearance", "Giao diện", "Privacy options", "Tùy chọn quyền riêng tư")
    val closeLabels: List<String> = listOf("Close", "Đóng")
    val aboutLabels: List<String> = listOf("About and help", "Giới thiệu và trợ giúp")
    val aboutMarkers: List<String> = listOf("About Parallel Game Space", "Về Parallel Game Space")
    val gotItLabels: List<String> = listOf("Got it", "Đã hiểu")
}
