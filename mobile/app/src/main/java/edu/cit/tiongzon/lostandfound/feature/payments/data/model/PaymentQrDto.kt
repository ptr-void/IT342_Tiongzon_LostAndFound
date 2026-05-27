package edu.cit.tiongzon.lostandfound.feature.payments.data.model

data class PaymentQrDto(
    val qrImageUrl: String = "",
    val label: String = "",
    val amount: Long = 0,
    val currency: String = "PHP",
    val instructions: String = ""
)
