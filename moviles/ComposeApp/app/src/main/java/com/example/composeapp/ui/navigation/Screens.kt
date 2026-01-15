package com.aristidevs.navigationguide.core.navigation

import android.os.Bundle
import android.os.Parcelable

import kotlinx.serialization.Serializable

@Serializable
object Login

@Serializable
object Home

@Serializable
object User

@Serializable
data class Detail(val name: String)




