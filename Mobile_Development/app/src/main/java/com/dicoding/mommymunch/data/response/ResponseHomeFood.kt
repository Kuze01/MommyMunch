package com.dicoding.mommymunch.data.response

import com.google.gson.annotations.SerializedName

data class ResponseHomeFood(

	@field:SerializedName("ResponseHomeFood")
	val responseHomeFood: List<ResponseHomeFoodItem>
)

data class ResponseHomeFoodItem(

	@field:SerializedName("image")
	val image: String,

	@field:SerializedName("proteins")
	val proteins: String,

	@field:SerializedName("fat")
	val fat: String,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("calories")
	val calories: String,

	@field:SerializedName("carbohydrate")
	val carbohydrate: String
)
