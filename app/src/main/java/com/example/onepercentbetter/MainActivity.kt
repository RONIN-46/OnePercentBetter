package com.example.onepercentbetter

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// --- CYBER THEME COLORS ---
val PitchBlack = Color(0xFF000000)
val ElectricBlue = Color(0xFF00E5FF)
val DarkNavy = Color(0xFF0A121E)
val SoftBlue = Color(0xFF80D8FF)

// --- DATASTORE FOR PERSISTENCE ---
val Context.dataStore by preferencesDataStore(name = "user_progress")
class ProgressManager(private val context: Context) {
    private val STREAK_KEY = intPreferencesKey("current_streak")
    val streakFlow: Flow<Int> = context.dataStore.data.map { it[STREAK_KEY] ?: 0 }
    suspend fun saveStreak(streak: Int) = context.dataStore.edit { it[STREAK_KEY] = streak }
}

// --- CHALLENGE DATABASE ---
data class Challenge(val skill: String, val task: String)
val challengeDatabase = listOf(
    Challenge("Communication", "Compliment a stranger's work"),
    Challenge("Coding", "Solve a logic puzzle"),
    Challenge("Fitness", "Hold a plank for 60 seconds"),
    Challenge("Productivity", "Write down your top 3 goals"),
    Challenge("Mindset", "List 3 things you are grateful for"),
    Challenge("Focus", "25 mins of deep work (No Phone)"),
    Challenge("Confidence", "Make eye contact during a full conversation")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = ElectricBlue,
                    surface = DarkNavy,
                    background = PitchBlack
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = PitchBlack) {
                    OnePercentApp()
                }
            }
        }
    }
}

@Composable
fun OnePercentApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val manager = remember { ProgressManager(context) }
    val savedStreak by manager.streakFlow.collectAsState(initial = 0)

    var showCalendar by remember { mutableStateOf(false) }
    var isDone by remember { mutableStateOf(false) }
    val currentTask by remember { mutableStateOf(challengeDatabase.random()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PitchBlack, Color(0xFF001520))))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("1% BETTER", fontWeight = FontWeight.Black, color = ElectricBlue)
            IconButton(onClick = { showCalendar = !showCalendar }, modifier = Modifier.border(1.dp, ElectricBlue, CircleShape)) {
                Text(if (showCalendar) "✕" else "📊", color = ElectricBlue)
            }
        }

        if (showCalendar) {
            CalendarView(savedStreak)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(60.dp))
                Box(contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(160.dp).background(ElectricBlue.copy(alpha = 0.1f), CircleShape).border(2.dp, ElectricBlue, CircleShape))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚡", fontSize = 40.sp)
                        Text("$savedStreak", fontSize = 46.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("STREAK", fontSize = 12.sp, color = ElectricBlue)
                    }
                }
                Spacer(modifier = Modifier.height(50.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().border(0.5.dp, SoftBlue.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkNavy)
                ) {
                    Column(modifier = Modifier.padding(32.dp)) {
                        Text(currentTask.skill.uppercase(), color = SoftBlue, style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(currentTask.task, fontSize = 22.sp, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    onClick = {
                        isDone = true
                        scope.launch { manager.saveStreak(savedStreak + 1) }
                        Toast.makeText(context, "Progress Saved!", Toast.LENGTH_SHORT).show()
                    },
                    enabled = !isDone,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDone) Color.DarkGray else ElectricBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (isDone) "COMPLETED" else "MARK AS DONE", fontWeight = FontWeight.Bold, color = if(isDone) Color.White else PitchBlack)
                }
            }
        }
    }
}

@Composable
fun CalendarView(streak: Int) {
    Column(modifier = Modifier.padding(top = 20.dp)) {
        Text("Activity Log", style = MaterialTheme.typography.titleLarge, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(300.dp)
        ) {
            items(35) { index ->
                val isActive = index < streak
                Box(
                    modifier = Modifier.aspectRatio(1f).background(if (isActive) ElectricBlue else Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isActive) Text("✓", color = PitchBlack, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}