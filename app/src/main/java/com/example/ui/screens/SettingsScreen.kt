package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.StudioViewModel

@Composable
fun SettingsScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isSubscribed by viewModel.isSubscribed.collectAsState()
    val userCredits by viewModel.userCredits.collectAsState()

    var activeLanguage by remember { mutableStateOf("Bahasa Indonesia") }
    var analyticsToggle by remember { mutableStateOf(true) }
    var cloudBackupToggle by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // 1. Sleek Account Profile Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = DeepCardNavy),
            border = BorderStroke(1.dp, BorderNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(TechOrange.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, contentDescription = "User avatar", tint = TechOrange, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Developer Raihan", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = if (isSubscribed) "PRO Unlimited Plan Member" else "Free Account Tier",
                        color = if (isSubscribed) TechOrange else TextGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. Credits Allocation Hub
        Text("AI Generation Credits Balance", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite, modifier = Modifier.padding(bottom = 8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = DeepCardNavy),
            border = BorderStroke(1.dp, BorderNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Available Credits", color = TextGray, fontSize = 11.sp)
                        Text(
                            text = if (isSubscribed) "∞ Unlimited" else "$userCredits Credits",
                            color = TextWhite,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                    }

                    if (!isSubscribed) {
                        Button(
                            onClick = {
                                viewModel.submitSubscription()
                                Toast.makeText(context, "Subscription mock successfully processed!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TechOrange),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Upgrade to Pro", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextWhite)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Each default generation consumes 10 credits. Upscaling and Mocking calls consume 15 credits. Pro subscription accounts receive unlimited real-time generation slots.",
                    color = TextGray,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }

        // 3. Subscription Pricing Cards (M3 design system)
        Text("Creative Subscription Plans", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite, modifier = Modifier.padding(bottom = 8.dp))
        PricingPlanCard(
            title = "FaceLess Standard",
            price = "Free forever",
            desc = "150 daily free generation credits, standard AI styles access, 1080p outputs.",
            isActive = !isSubscribed
        )
        Spacer(modifier = Modifier.height(8.dp))
        PricingPlanCard(
            title = "FaceLess Unlimited PRO",
            price = "Rp 49.000 / month",
            desc = "Infinite generation credits, Ultra HD 4K, transparent backgrounds, printable sticker wraps, T-Shirt/Mug mockups, priority Gemini 3.5 Flash queries.",
            isActive = isSubscribed
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Studio settings (Language, Backup, Cache)
        Text("System Preferences", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite, modifier = Modifier.padding(bottom = 8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = DeepCardNavy),
            border = BorderStroke(1.dp, BorderNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                // Language Preference Dropdown Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Language, contentDescription = "Lang", tint = TechOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Applet Language", color = TextWhite, fontSize = 12.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("English", "Bahasa Indonesia", "العربية").forEach { lang ->
                            val active = activeLanguage == lang
                            Box(
                                modifier = Modifier
                                    .background(if (active) TechOrange.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (active) TechOrange else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable {
                                        activeLanguage = lang
                                        Toast.makeText(context, "Language switched to $lang", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = lang,
                                    color = if (active) TechOrange else TextGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = BorderNavy)

                // Advanced diagnostics / Gemini details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.SettingsSuggest, contentDescription = "API model", tint = TechOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Underlying AI Engine", color = TextWhite, fontSize = 12.sp)
                    }

                    Text("Gemini 3.5 Flash API", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = BorderNavy)

                // Cache diagnostics row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.SdCard, contentDescription = "Storage", tint = TechOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Clear Database & Caches", color = TextWhite, fontSize = 12.sp)
                    }

                    TextButton(
                        onClick = {
                            viewModel.clearAllHistory()
                            Toast.makeText(context, "Room Database cache cleared!", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("RESET CACHES", color = Color.Red.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PricingPlanCard(
    title: String,
    price: String,
    desc: String,
    isActive: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (isActive) TechOrange.copy(alpha = 0.05f) else DeepCardNavy),
        border = BorderStroke(1.dp, if (isActive) TechOrange else BorderNavy),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                if (isActive) {
                    Surface(
                        color = TechOrange,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "ACTIVE PLAN",
                            color = TextWhite,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(price, color = TechOrange, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(desc, color = TextGray, fontSize = 10.sp, lineHeight = 13.sp)
        }
    }
}
