package com.example.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.FacelessCanvasConfig
import com.example.api.GeminiService
import com.example.ui.components.FaceLessCanvasRenderer
import com.example.ui.theme.*
import com.example.viewmodel.StudioViewModel
import kotlinx.coroutines.delay

@Composable
fun StudioScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val prompt by viewModel.prompt.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val activeConfig by viewModel.activeConfig.collectAsState()
    val selectedStyle by viewModel.selectedStyle.collectAsState()
    val isIslamicKidsMode by viewModel.isIslamicKidsMode.collectAsState()
    val userCredits by viewModel.userCredits.collectAsState()
    val isSubscribed by viewModel.isSubscribed.collectAsState()

    // Extensions State
    val isUpscaled by viewModel.isUpscaled.collectAsState()
    val isTransparentBg by viewModel.isTransparentBg.collectAsState()
    val isColoringPageMode by viewModel.isColoringPageMode.collectAsState()
    val isStickerMode by viewModel.isStickerMode.collectAsState()
    val selectedMockup by viewModel.selectedMockup.collectAsState()
    val beforeAfterSplit by viewModel.beforeAfterSplit.collectAsState()

    // Display state for active styling dropdown menu
    var styleDropdownExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp)
    ) {
        // 1. Sleek Header: Credits & Upgrade
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.OfflineBolt, contentDescription = "Credits", tint = TechOrange, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isSubscribed) "PRO UNLIMITED" else "$userCredits CREDITS",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            }

            if (!isSubscribed) {
                Button(
                    onClick = {
                        viewModel.submitSubscription()
                        Toast.makeText(context, "Subscribed to FaceLess PRO! Unlimited credits unlocked.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(28.dp).testTag("pro_upgrade_btn")
                ) {
                    Text("GO PRO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }
            } else {
                Surface(
                    color = TechOrange.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, TechOrange),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "PRO SUBSCRIPTION",
                        color = TechOrange,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // 2. Main Studio Interactive Canvas Display Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(DeepCardNavy)
                .border(2.dp, if (isGenerating) TechOrange else BorderNavy, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isGenerating) {
                AIStudioLoaderAnimation()
            } else if (activeConfig != null) {
                val config = activeConfig!!
                
                Box(modifier = Modifier.fillMaxSize()) {
                    // Check if Transparent background is enabled. If yes, draw a beautiful gray chess grid first!
                    if (isTransparentBg) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cellSize = 24.dp.toPx()
                            val cols = (size.width / cellSize).toInt() + 1
                            val rows = (size.height / cellSize).toInt() + 1
                            for (c in 0..cols) {
                                for (r in 0..rows) {
                                    if ((c + r) % 2 == 0) {
                                        drawRect(
                                            color = Color.White.copy(alpha = 0.15f),
                                            topLeft = Offset(c * cellSize, r * cellSize),
                                            size = Size(cellSize, cellSize)
                                        )
                                    } else {
                                        drawRect(
                                            color = Color.White.copy(alpha = 0.05f),
                                            topLeft = Offset(c * cellSize, r * cellSize),
                                            size = Size(cellSize, cellSize)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Render active procedural illustrations!
                    val finalConfig = if (isStickerMode) config.copy(styleName = "Sticker Style") else config
                    
                    if (selectedMockup != "none") {
                        // Render Mockups directly on screen!
                        MockupContainerLayout(selectedMockup, finalConfig, isColoringPageMode, isUpscaled)
                    } else {
                        // Regular canvas with Before vs After Split screen support
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Render Faceless artwork
                            FaceLessCanvasRenderer(
                                config = finalConfig,
                                modifier = Modifier.fillMaxSize().testTag("active_studio_canvas"),
                                isColoringPageMode = isColoringPageMode,
                                upscaleFactor = if (isUpscaled) 1.25f else 1.0f
                            )

                            // Render Eyes/Mouth overlaid on left side of before/after split ratio
                            if (config.isLivingCreature && !isColoringPageMode) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val screenWidth = size.width
                                    val screenHeight = size.height
                                    val leftBound = screenWidth * beforeAfterSplit
                                    
                                    // Clip drawing to left portion of the slider
                                    clipRect(right = leftBound) {
                                        // Draw eyes and mouth centered on face coordinates
                                        val cx = screenWidth / 2f
                                        val cy = screenHeight / 2f
                                        
                                        // Simple cute closed eyes and small mouth
                                        drawCircle(color = Color(0xFF1E293B), radius = 4f, center = Offset(cx - 16f, cy))
                                        drawCircle(color = Color(0xFF1E293B), radius = 4f, center = Offset(cx + 16f, cy))
                                        
                                        // Rosy cheeks
                                        drawCircle(color = Color(0xFFFF8A80), radius = 6f, center = Offset(cx - 24f, cy + 8f))
                                        drawCircle(color = Color(0xFFFF8A80), radius = 6f, center = Offset(cx + 24f, cy + 8f))

                                        // Smile
                                        drawArc(
                                            color = Color(0xFF1E293B),
                                            startAngle = 0f,
                                            sweepAngle = 180f,
                                            useCenter = false,
                                            topLeft = Offset(cx - 6f, cy + 6f),
                                            size = Size(12f, 8f),
                                            style = Stroke(width = 3f)
                                        )
                                    }

                                    // Draw the Split Slider Line
                                    drawLine(
                                        color = TechOrange,
                                        start = Offset(leftBound, 0f),
                                        end = Offset(leftBound, screenHeight),
                                        strokeWidth = 4f
                                    )

                                    // Draw minor neon anchor bulb
                                    drawCircle(color = TechOrange, radius = 10f, center = Offset(leftBound, screenHeight / 2))
                                    drawCircle(color = Color.White, radius = 5f, center = Offset(leftBound, screenHeight / 2))
                                }
                            }
                        }
                    }
                }
            } else {
                // Empty Illustration state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(TechOrange.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Palette, contentDescription = "Design", tint = TechOrange, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Your Faceless Canvas is empty", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Input a creative prompt below, choose your style and hit generate in real-time.",
                        color = TextGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 3. Before vs After Live Control Ratio Slider (only if illustration is active and has character)
        if (activeConfig != null && activeConfig!!.isLivingCreature && selectedMockup == "none" && !isColoringPageMode) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("← Normal Face (Before)", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Smooth Faceless (After) →", color = TechOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = beforeAfterSplit,
                    onValueChange = { viewModel.setBeforeAfterSplit(it) },
                    colors = SliderDefaults.colors(
                        activeTrackColor = TechOrange.copy(alpha = 0.4f),
                        inactiveTrackColor = ActiveCardNavy,
                        thumbColor = TechOrange
                    ),
                    modifier = Modifier.fillMaxWidth().height(24.dp).testTag("before_after_slider")
                )
            }
        }

        // 4. Lab Controls: Inputs, Suggestions, Style dropdown
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Outlined prompt text area
            OutlinedTextField(
                value = prompt,
                onValueChange = { viewModel.updatePrompt(it) },
                placeholder = { Text("E.g., Gadis berhijab sedang membaca Al-Quran, style kawaii, latar estetik...", color = TextGray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp)
                    .testTag("prompt_text_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = DeepCardNavy,
                    unfocusedContainerColor = DeepCardNavy,
                    focusedBorderColor = TechOrange,
                    unfocusedBorderColor = BorderNavy
                ),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    if (prompt.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updatePrompt("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = TextGray)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // AI Prompt Enhancer & Copy Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Enhancer button
                Button(
                    onClick = { viewModel.enhanceWithAI() },
                    colors = ButtonDefaults.buttonColors(containerColor = ActiveCardNavy),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, TechOrange.copy(alpha = 0.6f)),
                    modifier = Modifier.weight(1f).testTag("ai_enhance_btn"),
                    enabled = prompt.trim().isNotEmpty() && !isGenerating
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = "Enhance", tint = TechOrange, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Prompt Enhancer", fontSize = 11.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                }

                // Copy Prompt
                IconButton(
                    onClick = {
                        if (prompt.isNotEmpty()) {
                            clipboardManager.setText(AnnotatedString(prompt))
                            Toast.makeText(context, "Enhanced prompt copied!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .background(DeepCardNavy, RoundedCornerShape(10.dp))
                        .border(1.dp, BorderNavy, RoundedCornerShape(10.dp))
                        .size(44.dp)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Prompt", tint = TextWhite)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Style Selector and Islamic Kids Mode Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dropdown trigger for AI styles
                Box(modifier = Modifier.weight(1.2f)) {
                    Button(
                        onClick = { styleDropdownExpanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepCardNavy),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BorderNavy),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedStyle, fontSize = 11.sp, color = TextWhite, maxLines = 1)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Styles", tint = TechOrange)
                        }
                    }

                    DropdownMenu(
                        expanded = styleDropdownExpanded,
                        onDismissRequest = { styleDropdownExpanded = false },
                        modifier = Modifier
                            .background(DeepCardNavy)
                            .border(1.dp, BorderNavy)
                    ) {
                        viewModel.availableStyles.forEach { style ->
                            DropdownMenuItem(
                                text = { Text(style, color = TextWhite, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.selectStyle(style)
                                    styleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Toggle for Islamic Kids Illustration Mode!
                Surface(
                    color = if (isIslamicKidsMode) TechOrange.copy(alpha = 0.15f) else DeepCardNavy,
                    border = BorderStroke(1.dp, if (isIslamicKidsMode) TechOrange else BorderNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clickable { viewModel.setIslamicMode(!isIslamicKidsMode) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Star",
                            tint = if (isIslamicKidsMode) TechOrange else TextGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Islamic Mode",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isIslamicKidsMode) TechOrange else TextWhite
                        )
                    }
                }
            }

            // Quick suggestion chips based on active selections of styles/cultures
            Text(
                "Prompt Suggestions",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GeminiService.getPromptSuggestions(selectedStyle, isIslamicKidsMode).forEach { sug ->
                    Card(
                        modifier = Modifier.clickable { viewModel.applySuggestion(sug) },
                        colors = CardDefaults.cardColors(containerColor = ActiveCardNavy.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, BorderNavy)
                    ) {
                        Text(
                            text = sug,
                            fontSize = 10.sp,
                            color = TextWhite,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Generate CTA Button
            Button(
                onClick = { viewModel.generateImage() },
                colors = ButtonDefaults.buttonColors(containerColor = TechOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("core_studio_generate_btn"),
                enabled = !isGenerating
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = "Spark logo", tint = TextWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Faceless Illustration", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // 5. EXTENSIONS LABORATORY (Toggles 6-12)
        if (activeConfig != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                HorizontalDivider(color = BorderNavy, modifier = Modifier.padding(vertical = 12.dp))
                
                Text(
                    "Studio Design Extensions",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonPurple,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Grid mapping out advanced AI additions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Transparent Background Toggle
                    ExtensionCard(
                        title = "BG Remover",
                        icon = Icons.Filled.BlurOn,
                        isActive = isTransparentBg,
                        onClick = { viewModel.toggleBackgroundRemover() },
                        modifier = Modifier.weight(1f).testTag("bg_remover_btn")
                    )

                    // Line curves Coloring toggle
                    ExtensionCard(
                        title = "Coloring Page",
                        icon = Icons.Filled.Brush,
                        isActive = isColoringPageMode,
                        onClick = { viewModel.toggleColoringPage() },
                        modifier = Modifier.weight(1f).testTag("coloring_mode_btn")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Ultra HD Upscaler
                    ExtensionCard(
                        title = "Ultra HD 4K",
                        icon = Icons.Filled.HdrEnhancedSelect,
                        isActive = isUpscaled,
                        onClick = { viewModel.toggleUpscaler() },
                        modifier = Modifier.weight(1f).testTag("upscaler_btn")
                    )

                    // Sticker White Outline Wrap
                    ExtensionCard(
                        title = "Sticker Wrap",
                        icon = Icons.Filled.PhotoFilter,
                        isActive = isStickerMode,
                        onClick = { viewModel.toggleStickerMode() },
                        modifier = Modifier.weight(1f).testTag("sticker_mode_btn")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // AI Product Mockups Showcase options
                Text("AI Product Mockup Generator", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("none" to "No Mock", "tshirt" to "T-Shirt", "mug" to "Mug Cup", "poster" to "Poster Frame").forEach { (type, label) ->
                        val selected = selectedMockup == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) TechOrange.copy(alpha = 0.15f) else ActiveCardNavy)
                                .border(1.dp, if (selected) TechOrange else BorderNavy, RoundedCornerShape(8.dp))
                                .clickable { viewModel.selectMockup(type) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) TechOrange else TextWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Download Simulated High Resolution Actions
                Button(
                    onClick = {
                        Toast.makeText(context, "Downloaded high quality faceless layout in 4K resolution!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ActiveCardNavy),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderNavy),
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("hd_download_btn")
                ) {
                    Icon(Icons.Filled.Download, contentDescription = "Download HD", tint = TechOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download PNG HD (4K Format)", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AIStudioLoaderAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp)
    ) {
        // Glowing spinning loader
        Box(
            modifier = Modifier
                .size(72.dp)
                .drawBehind {
                    drawArc(
                        color = TechOrange,
                        startAngle = angle,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = NeonPurple,
                        startAngle = angle + 180f,
                        sweepAngle = 90f,
                        useCenter = false,
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )
                }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("FaceLess AI is Processing...", color = TextWhite, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Analyzing biological and living structures to instantly reconstruct a premium faceless minimalist output...",
            color = TextGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 240.dp),
            lineHeight = 15.sp
        )
    }
}

@Composable
fun ExtensionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isActive) GlowCyan.copy(alpha = 0.15f) else DeepCardNavy,
        border = BorderStroke(1.dp, if (isActive) GlowCyan else BorderNavy),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .clickable { onClick() }
            .height(54.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isActive) GlowCyan.copy(alpha = 0.2f) else ActiveCardNavy),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isActive) GlowCyan else TextWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(if (isActive) "ACTIVE" else "READY", color = if (isActive) GlowCyan else TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MockupContainerLayout(
    mockupType: String,
    config: FacelessCanvasConfig,
    isColoringPageMode: Boolean,
    isUpscaled: Boolean
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2

        // Base background of mockup canvas
        drawRect(color = Color(0xFFE2E8F0)) // Studio space gray floor mockup

        when (mockupType) {
            "tshirt" -> {
                // Paint beautiful stylized minimalist T-Shirt layout on background
                val shirtPath = Path().apply {
                    moveTo(cx - w * 0.35f, h * 0.1f)
                    lineTo(cx - w * 0.20f, h * 0.12f)
                    lineTo(cx - w * 0.22f, h * 0.28f)
                    lineTo(cx - w * 0.12f, h * 0.28f)
                    lineTo(cx - w * 0.15f, h * 0.90f)
                    lineTo(cx + w * 0.15f, h * 0.90f)
                    lineTo(cx + w * 0.12f, h * 0.28f)
                    lineTo(cx + w * 0.22f, h * 0.28f)
                    lineTo(cx + w * 0.20f, h * 0.12f)
                    lineTo(cx + w * 0.35f, h * 0.1f)
                    close()
                }
                drawPath(path = shirtPath, color = Color.White)
                drawPath(path = shirtPath, color = Color.Black.copy(alpha = 0.1f), style = Stroke(width = 6f))

                // Frame where we overlay the faceless graphic onto the center of chest of the T-Shirt
                clipPath(path = shirtPath) {
                    val scaleSize = Size(w * 0.24f, h * 0.24f)
                    val offsetLeft = Offset(cx - scaleSize.width / 2, h * 0.22f)
                    
                    // Inside chest box drawing
                    drawRoundRect(
                        color = Color(0xFFF1F5F9),
                        topLeft = offsetLeft,
                        size = scaleSize,
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                }
            }
            "mug" -> {
                // Paint custom coffee ceramic mug
                val mugPath = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            left = cx - w * 0.22f,
                            top = h * 0.2f,
                            right = cx + w * 0.22f,
                            bottom = h * 0.8f,
                            cornerRadius = CornerRadius(16.dp.toPx())
                        )
                    )
                }
                drawPath(path = mugPath, color = Color.White)
                
                // Draw handle
                drawArc(
                    color = Color.White,
                    startAngle = -90f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx + w * 0.18f, h * 0.35f),
                    size = Size(48f, h * 0.3f),
                    style = Stroke(width = 18f)
                )
                drawArc(
                    color = Color.Black.copy(alpha = 0.1f),
                    startAngle = -90f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx + w * 0.18f, h * 0.35f),
                    size = Size(48f, h * 0.3f),
                    style = Stroke(width = 2f)
                )
            }
            "poster" -> {
                // Premium minimal wooden picture frame poster mock
                drawRect(
                    color = Color(0xFF78350F), // warm wood bezel
                    topLeft = Offset(w * 0.12f, h * 0.12f),
                    size = Size(w * 0.76f, h * 0.76f)
                )
                drawRect(
                    color = Color.White, // matte white frame border inside
                    topLeft = Offset(w * 0.16f, h * 0.16f),
                    size = Size(w * 0.68f, h * 0.68f)
                )
            }
        }
    }

    // Overlay active watercolor render directly in overlay spot representing placing on mug/shirt/poster
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (mockupType == "tshirt") 120.dp else if (mockupType == "mug") 100.dp else 40.dp)
    ) {
        FaceLessCanvasRenderer(
            config = config,
            modifier = Modifier.fillMaxSize(),
            isColoringPageMode = isColoringPageMode,
            upscaleFactor = if (isUpscaled) 1.2f else 1.0f
        )
    }
}
