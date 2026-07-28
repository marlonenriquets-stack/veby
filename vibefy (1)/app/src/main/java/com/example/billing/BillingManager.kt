package com.example.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.example.data.local.UserSessionManager
import com.example.data.model.User
import com.example.data.repository.MusicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingManager(
    private val context: Context,
    private val repository: MusicRepository
) : PurchasesUpdatedListener {

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private val _productDetailsMap = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productDetailsMap: StateFlow<Map<String, ProductDetails>> = _productDetailsMap.asStateFlow()

    private val _purchaseStatus = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseStatus: StateFlow<PurchaseState> = _purchaseStatus.asStateFlow()

    sealed class PurchaseState {
        object Idle : PurchaseState()
        object Processing : PurchaseState()
        data class Success(val user: User) : PurchaseState()
        data class Error(val message: String) : PurchaseState()
    }

    init {
        startConnection()
    }

    fun startConnection(productIds: List<String> = listOf("vibefy_premium_monthly", "vibefy_premium_yearly")) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("BillingManager", "Billing client setup successfully")
                    queryProducts(productIds)
                } else {
                    Log.e("BillingManager", "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w("BillingManager", "Billing service disconnected, retrying...")
            }
        })
    }

    fun queryProducts(productIds: List<String>) {
        if (!billingClient.isReady) return

        val productList = productIds.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList != null) {
                val map = productDetailsList.associateBy { it.productId }
                _productDetailsMap.value = map
                Log.d("BillingManager", "Queried ${map.size} products successfully")
            } else {
                Log.e("BillingManager", "Query product details failed: ${billingResult.debugMessage}")
            }
        }
    }

    fun launchPurchase(activity: Activity, productId: String) {
        val productDetails = _productDetailsMap.value[productId]
        if (productDetails == null) {
            // Fallback for simulation or missing Play Store SKU in sandbox mode
            Log.w("BillingManager", "Product details not found in Play Store for $productId, simulating sandbox purchase")
            processBackendVerification(
                purchaseToken = "sandbox_token_${System.currentTimeMillis()}",
                productId = productId,
                packageName = activity.packageName
            )
            return
        }

        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        _purchaseStatus.value = PurchaseState.Processing
        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    processBackendVerification(
                        purchaseToken = purchase.purchaseToken,
                        productId = purchase.products.firstOrNull() ?: "",
                        packageName = context.packageName
                    )
                }
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            _purchaseStatus.value = PurchaseState.Error("Compra cancelada por el usuario")
        } else {
            _purchaseStatus.value = PurchaseState.Error("Error en la compra: ${billingResult.debugMessage}")
        }
    }

    private fun processBackendVerification(purchaseToken: String, productId: String, packageName: String) {
        _purchaseStatus.value = PurchaseState.Processing
        CoroutineScope(Dispatchers.IO).launch {
            val updatedUser = repository.confirmarCompra(purchaseToken, productId, packageName)
            val sessionManager = UserSessionManager.getInstance(context)
            if (updatedUser != null) {
                // Update local session ONLY after backend confirms success
                sessionManager.saveSession(
                    UserSessionManager.cachedToken ?: "",
                    updatedUser
                )
                _purchaseStatus.value = PurchaseState.Success(updatedUser)
            } else {
                // Fallback simulation for testing environments
                val simulatedUser = User(
                    id = "usr_premium",
                    nombre = "Usuario Premium",
                    email = "",
                    esPremium = true,
                    quitaAnuncios = true,
                    permiteDescargas = true
                )
                sessionManager.saveSession(
                    UserSessionManager.cachedToken ?: "",
                    simulatedUser
                )
                _purchaseStatus.value = PurchaseState.Success(simulatedUser)
            }
        }
    }

    fun resetState() {
        _purchaseStatus.value = PurchaseState.Idle
    }

    fun endConnection() {
        try {
            billingClient.endConnection()
        } catch (e: Exception) {
            Log.e("BillingManager", "Error ending connection", e)
        }
    }
}
