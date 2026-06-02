package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.StudioViewModel

@Composable
fun LandingScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val prompt by viewModel.prompt.collectAsState()
    val scrollState = rememberScrollState()

    // Interactive animated infinite lights
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val ambientGlowVal by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .verticalScroll(scrollState)
    ) {
        // Hero Section & Glass Title Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            // Neon radial back shade
            Box(
                modifier = Modifier
                    .size(width = 280.dp, height = 280.dp)
                    .align(Alignment.TopCenter)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(NeonPurple.copy(alpha = 0.2f * ambientGlowVal), Color.Transparent),
                                center = Offset(size.width / 2, size.height / 2),
                                radius = size.width / 2
                            )
                        )
                    }
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sleek badge
                Surface(
                    color = DeepCardNavy,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, TechOrange.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = "AI Powered",
                            tint = TechOrange,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NEXT GENERATION GENERATOR",
                            color = TextWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Header
                Text(
                    text = buildAnnotatedString {
                        append("The Premier ")
                        withStyle(style = SpanStyle(color = TechOrange)) {
                            append("FaceLess\n")
                        }
                        append("AI Art Studio")
                    },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "Automated vector design studio that instantly sanitizes facial details of living figures into high-quality, cute, and premium faceless illustrations.",
                    fontSize = 13.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                        .widthIn(max = 480.dp),
                    lineHeight = 18.sp
                )

                // Call To Action (CTA) Action
                Button(
                    onClick = { viewModel.navigateTo("studio") },
                    colors = ButtonDefaults.buttonColors(containerColor = TechOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(50.dp)
                        .testTag("launch_studio_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Launch AI Workspace", fontWeight = FontWeight.Bold, color = TextWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Enter", tint = TextWhite)
                    }
                }
            }
        }

        // Feature overview slider row (Islamic kids mode, 3D clay, cartoon stickers)
        Text(
            text = "AI Visual Styling Modes",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 12.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            val slides = listOf(
                SlideItem(Icons.Filled.Woman, "Islamic Aesthetic", "Cute hijab characters rendered on canvas in pure soft aesthetic pastel colors, perfect for parenting or children books.", IslamicIllustration),
                SlideItem(Icons.Filled.Animation, "3D Cute Render", "Soft tactile plastic/clay styled faceless models with adorable accessories and realistic shadows.", ThreeDCute),
                SlideItem(Icons.Filled.WorkspacePremium, "Flat Premium", "Sophisticated professional characters with sleek workspace gadgets, optimal for tech landing pages.", FlatPremium),
                SlideItem(Icons.Filled.Layers, "Sticker Die-Cut", "Generates thick high-contrast white borders around your character silhouettes, optimized for prints.", StickerStyle)
            )
            items(slides) { slide ->
                SlideCard(slide) {
                    viewModel.selectStyle(slide.styleValue)
                    viewModel.navigateTo("studio")
                }
            }
        }

        // Live Demo prompt generator interactive card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = DeepCardNavy),
            border = BorderStroke(1.dp, BorderNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Live Sandbox Demo",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TechOrange,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = "Type any prompt. If AI detetcts any humans or animals, they'll instantly render faceless!",
                    fontSize = 11.sp,
                    color = TextGray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Quick selector tags
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val promptIdeas = listOf(
                        "Gadis hijab membaca buku",
                        "Cute faceless orange cat",
                        "Coffee house cozy barist",
                        "Minimalist tech programmer"
                    )
                    promptIdeas.forEach { idea ->
                        SuggestionChip(
                            onClick = { viewModel.updatePrompt(idea) },
                            label = { Text(idea, fontSize = 10.sp, color = TextWhite) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = ActiveCardNavy
                            ),
                            border = BorderStroke(1.dp, BorderNavy)
                        )
                    }
                }

                OutlinedTextField(
                    value = prompt,
                    onValueChange = { viewModel.updatePrompt(it) },
                    placeholder = { Text("E.g., Gadis berhijab sedang menulis...", color = TextGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = ActiveCardNavy,
                        unfocusedContainerColor = ActiveCardNavy,
                        focusedBorderColor = TechOrange,
                        unfocusedBorderColor = BorderNavy
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.generateImage()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("sandbox_gen_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = "Spark", tint = TextWhite, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Auto Faceless Generate", fontWeight = FontWeight.SemiBold, color = TextWhite)
                }
            }
        }

        // Modern Testimonials Showcase Section
        FAQSection()

        // Modern Startup Footer
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepCardNavy)
                .padding(vertical = 24.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "FaceLess AI Studio",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Indonesian Creative AI Startup Engine.\nNo Faces, Extreme Freedom. Aesthetic Always.",
                    color = TextGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "© 2026 FaceLess AI Inc. Built for Premium Android Streaming.",
                    color = TextGray.copy(alpha = 0.6f),
                    fontSize = 9.sp
                )
            }
        }
    }
}

data class SlideItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val desc: String,
    val styleValue: String
)

// Aesthetic constant style matchers
const val IslamicIllustration = "Islamic Illustration"
const val ThreeDCute = "3D Cute"
const val FlatPremium = "Flat Vector Premium"
const val StickerStyle = "Sticker Style"

@Composable
fun SlideCard(
    slide: SlideItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(140.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DeepCardNavy),
        border = BorderStroke(1.dp, BorderNavy),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(TechOrange.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(slide.icon, contentDescription = slide.title, tint = TechOrange, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(slide.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Text(slide.desc, color = TextGray, fontSize = 10.sp, lineHeight = 14.sp)

            Text(
                "APPLY STYLE →",
                color = TechOrange,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun FAQSection() {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "Frequently Asked Questions",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val faqs = listOf(
            "Mengapa semua makhluk hidup dibuat faceless?" to "Filosofi kami adalah memajukan seni minimalis yang modern, eye-safe, cute, sekaligus menjaga privacy dan prinsip kecantikan ekspresi non-facial.",
            "Apakah benda mati juga akan dihilangkan wajahnya?" to "Tidak. Benda mati seperti gunung, mobil, danau, bunga, dan rumah akan dirender biasa dengan detail penuh tanpa modifikasi.",
            "Bagaimana cara mengaktifkan Mode Islami Kid?" to "Cukup pilih gaya 'Islamic Illustration', AI kami otomatis menyesuaikan hijab berbusana muslim, palet warna pastel, dan latar ramah anak."
        )

        faqs.forEach { (q, a) ->
            var expanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { expanded = !expanded },
                colors = CardDefaults.cardColors(containerColor = DeepCardNavy),
                border = BorderStroke(1.dp, if (expanded) TechOrange.copy(alpha = 0.5f) else BorderNavy)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = q,
                            color = if (expanded) TechOrange else TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.9f)
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = TextGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    AnimatedVisibility(visible = expanded) {
                        Text(
                            text = a,
                            color = TextGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
