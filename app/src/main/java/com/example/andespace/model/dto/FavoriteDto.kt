package com.example.andespace.model.dto

import com.google.gson.annotations.SerializedName

data class AddFavoriteRequest(
    @SerializedName("room_id") val roomId: String,
    @SerializedName("room_number") val name: String? = null,
    @SerializedName("building_name") val building: String? = null,
    @SerializedName("building_code") val buildingCode: String? = null,
    @SerializedName("capacity") val capacity: Int? = null,
    @SerializedName("utilities") val utilities: List<String>? = emptyList()
)

data class GetFavoritesResponse(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("items") val items: List<FavoriteItemDto> = emptyList()
)

data class FavoriteItemDto(
    @SerializedName(value = "room_id", alternate = ["id"]) val roomId: String? = null,
    @SerializedName(value = "room_number", alternate = ["name"]) val name: String? = null,
    @SerializedName(value = "building_name", alternate = ["building"]) val building: String? = null,
    @SerializedName("building_code") val buildingCode: String? = null,
    @SerializedName("capacity") val capacity: Int? = null,
    @SerializedName("utilities") val utilities: List<String> = emptyList()
) {
    fun toRoomDto(): RoomDto? {
        val id = roomId ?: return null
        return RoomDto(
            id = id,
            name = name,
            building = building,
            buildingCode = buildingCode,
            capacity = capacity,
            utilities = utilities
        )
    }
}
