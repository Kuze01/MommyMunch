package com.dicoding.mommymunch.data.response

import com.google.gson.annotations.SerializedName

data class ResponseAnalyze(

	@field:SerializedName("nutrition_info")
	val nutritionInfo: Map<String, Any>,

	@field:SerializedName("predicted_class")
	val predictedClass: String,

	@field:SerializedName("content")
	val content: String
)

