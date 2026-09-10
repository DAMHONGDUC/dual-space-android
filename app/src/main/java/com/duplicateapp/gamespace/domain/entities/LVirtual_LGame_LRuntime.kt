package com.duplicateapp.gamespace.features.virtualization.domain

interface virtual_game_runtime {
    fun install_from_device(package_name: String, virtual_user_id: Int): virtual_runtime_result
    fun is_installed(package_name: String, virtual_user_id: Int): Boolean
    fun launch(package_name: String, virtual_user_id: Int): virtual_runtime_result
}

sealed interface virtual_runtime_result {
    data object success : virtual_runtime_result
    data class failure(val message: String?) : virtual_runtime_result
}
