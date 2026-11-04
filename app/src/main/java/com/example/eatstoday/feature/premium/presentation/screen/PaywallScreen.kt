package com.example.eatstoday.feature.premium.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eatstoday.core.components.*
import com.example.eatstoday.core.utils.AppViewModelProvider
import com.example.eatstoday.feature.profile.presentation.viewmodel.ProfileViewModel

/** Chỉ mô phỏng trạng thái Premium: không tích hợp thanh toán và không trừ tiền. */
@Composable
fun PaywallScreen(
    onClose: () -> Unit,
    onActivated: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    var yearly by rememberSaveable { mutableStateOf(true) }
    ScreenColumn(
        bottomBar = {
            PrimaryButton("Nâng cấp ngay", { viewModel.activatePremium(onActivated) })
            Text(
                "Bản mô phỏng: chưa tích hợp thanh toán, không trừ tiền.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        },
    ) {
        Text("EatsToday Premium", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Nấu ngon hơn, không bị làm phiền",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        EatsCard(Modifier.fillMaxWidth(), contentPadding = 6.dp) {
            listOf("Không quảng cáo", "Công thức đặc biệt từ đầu bếp", "Lưu món không giới hạn").forEach { benefit ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EatsCheckbox(true, {}, benefit)
                    Text(benefit, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PlanCard("Năm · tiết kiệm", "[Giá/năm]", yearly, { yearly = true }, Modifier.weight(1f))
            PlanCard("Tháng", "[Giá/tháng]", !yearly, { yearly = false }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun PlanCard(label: String, price: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = colors.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Text(price, style = MaterialTheme.typography.titleMedium)
        }
    }
}
