package com.example.medicitaapp.data

import com.google.gson.annotations.SerializedName

data class MedicineApiResponse(
    @SerializedName("expediente") val expediente: String?,
    @SerializedName("producto") val producto: String?,
    @SerializedName("titular") val titular: String?,
    @SerializedName("registrosanitario") val registrosanitario: String?,
    @SerializedName("fechaexpedicion") val fechaexpedicion: String?,
    @SerializedName("fechavencimiento") val fechavencimiento: String?,
    @SerializedName("estadoregistro") val estadoregistro: String?,
    @SerializedName("viaadministracion") val viaadministracion: String?,
    @SerializedName("principioactivo") val principioactivo: String?,
    @SerializedName("atc") val atc: String?,
    @SerializedName("unidad") val unidad: String?,
    @SerializedName("formafarmaceutica") val formafarmaceutica: String?,
    @SerializedName("descripcionatc") val descripcionatc: String?,
    @SerializedName("concentracion") val concentracion: String?  // Nuevo campo
)