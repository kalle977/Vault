package com.gamingvault.app

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog

private val Gold = Color(0xFFEBC77D)
private val Ink = Color(0xFF09080E)
private val Panel = Color(0xEE15121D)
private val Muted = Color(0xFFB6AFC1)
private val games = listOf("World of Warcraft", "Fiesta Online", "AION", "The Elder Scrolls Online", "E-Mail Tresor")
private val art = mapOf("World of Warcraft" to R.drawable.wow, "Fiesta Online" to R.drawable.fiesta, "AION" to R.drawable.aion, "The Elder Scrolls Online" to R.drawable.eso, "E-Mail Tresor" to R.drawable.mail)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.statusBarColor = android.graphics.Color.rgb(9,8,14)
        window.navigationBarColor = android.graphics.Color.rgb(9,8,14)
        window.setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE, android.view.WindowManager.LayoutParams.FLAG_SECURE)
        val store = VaultStore(this)
        setContent { VaultApp(store) }
    }
}

@Composable private fun Frame(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.background(Panel, RoundedCornerShape(18.dp)).border(1.dp, Brush.linearGradient(listOf(Gold.copy(alpha=.8f),Color(0xFF4A2C60),Gold.copy(alpha=.45f))),RoundedCornerShape(18.dp)).padding(12.dp), content = content)
}
@Composable private fun GoldButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick, modifier, colors = ButtonDefaults.buttonColors(containerColor=Color(0xFF33240F),contentColor=Gold), border=BorderStroke(1.dp,Gold), shape=RoundedCornerShape(10.dp), contentPadding=PaddingValues(horizontal=12.dp, vertical=4.dp)) { Text(label, fontSize=12.sp, fontWeight=FontWeight.Bold) }
}
@Composable private fun VaultApp(store: VaultStore) {
    var accounts by remember { mutableStateOf(runCatching { store.load() }.getOrDefault(emptyList())) }
    var selectedGame by remember { mutableStateOf(games.first()) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<VaultAccount?>(null) }
    var adding by remember { mutableStateOf(false) }
    var newGameDialog by remember { mutableStateOf(false) }
    val customGames = remember { mutableStateListOf<String>() }
    var search by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    fun persist(next: List<VaultAccount>) { runCatching { store.save(next) }.onSuccess { accounts=next; message="Gespeichert" }.onFailure { message="Fehler beim Speichern: ${it.message}" } }
    val allGames = (games + customGames + accounts.map { it.game }).distinct()
    val current = accounts.filter { it.game==selectedGame }
    val chosen = current.find { it.id==selectedId } ?: current.firstOrNull()
    MaterialTheme(colorScheme=darkColorScheme(primary=Gold,background=Ink,surface=Panel,onSurface=Color.White)) {
        Box(Modifier.fillMaxSize().background(Ink)) {
            Image(painterResource(R.drawable.dragon),null,Modifier.fillMaxWidth().height(145.dp).align(Alignment.TopCenter),contentScale=ContentScale.Crop,alpha=.55f)
            Row(Modifier.fillMaxSize().padding(14.dp), horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                Frame(Modifier.width(182.dp).fillMaxHeight()) {
                    Text("♛  GAMING",color=Gold,fontSize=18.sp,fontWeight=FontWeight.Bold)
                    Text("      VAULT",color=Gold,fontSize=26.sp,fontWeight=FontWeight.Black)
                    Text("ACCOUNT MANAGER",color=Muted,fontSize=10.sp)
                    Spacer(Modifier.height(22.dp))
                    listOf("⌂   Übersicht","♧   Spiele","✉   E-Mail Konten","☆   Favoriten","♙   Sicherheit").forEach { title ->
                        Box(Modifier.fillMaxWidth().padding(vertical=3.dp).clip(RoundedCornerShape(9.dp)).background(if(title.contains("Übersicht")) Gold.copy(alpha=.15f) else Color.Transparent).clickable { if(title.contains("E-Mail"))selectedGame="E-Mail Tresor" }.padding(10.dp)) { Text(title,color=if(title.contains("Übersicht"))Gold else Color.White,fontSize=12.sp) }
                    }
                    Spacer(Modifier.weight(1f))
                    Text("◆  OFFLINE · VERSCHLÜSSELT",color=Gold,fontSize=9.sp)
                }
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement=Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                        Stat("♧",allGames.size,"Spiele",Modifier.weight(1f)); Stat("▱",accounts.size,"Accounts",Modifier.weight(1f)); Stat("✉",accounts.count { it.email.isNotBlank() },"E-Mail Konten",Modifier.weight(1f))
                    }
                    Row(Modifier.weight(1f), horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                        Frame(Modifier.weight(.95f).fillMaxHeight()) {
                            Row(verticalAlignment=Alignment.CenterVertically) {
                                Text("♧  Meine Spiele",color=Color.White,fontSize=18.sp,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f))
                                GoldButton("+ Spiel", onClick = { newGameDialog=true })
                            }
                            OutlinedTextField(search,{search=it},placeholder={Text("Spiel suchen ...",fontSize=12.sp)},singleLine=true,modifier=Modifier.fillMaxWidth().padding(vertical=6.dp),textStyle= androidx.compose.ui.text.TextStyle(fontSize=12.sp,color=Color.White))
                            LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                                items(allGames.filter { it.contains(search,true) }) { game ->
                                    Box(Modifier.fillMaxWidth().height(73.dp).clip(RoundedCornerShape(12.dp)).border(if(game==selectedGame) 2.dp else 1.dp,if(game==selectedGame)Gold else Color(0xFF56405B),RoundedCornerShape(12.dp)).clickable { selectedGame=game;selectedId=null }) {
                                        art[game]?.let { Image(painterResource(it),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop) }
                                        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xE9000000),Color(0xA6000000),Color.Transparent))))
                                        Column(Modifier.align(Alignment.CenterStart).padding(start=15.dp)) {
                                            Text(game,color=Color.White,fontSize=16.sp,fontWeight=FontWeight.Bold)
                                            Text("${accounts.count { it.game==game }} Accounts${if(game=="Fiesta Online") " · Gamigo" else ""}",color=Gold,fontSize=11.sp)
                                        }
                                    }
                                }
                            }
                        }
                        Frame(Modifier.weight(1.3f).fillMaxHeight()) {
                            Box(Modifier.fillMaxWidth().height(88.dp).clip(RoundedCornerShape(12.dp))) {
                                Image(painterResource(art[selectedGame] ?: R.drawable.castle),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
                                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xEE05050A),Color(0x9905050A),Color.Transparent))))
                                Column(Modifier.align(Alignment.CenterStart).padding(15.dp)) {
                                    Text(selectedGame,color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Bold)
                                    Text("ACCOUNT VERWALTUNG",color=Gold,fontSize=10.sp)
                                }
                            }
                            Spacer(Modifier.height(9.dp))
                            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                                Text("♙  Accounts (${current.size})",color=Gold,fontSize=12.sp,fontWeight=FontWeight.Bold)
                                Text("  •  Zugangsdaten & Notizen",color=Muted,fontSize=11.sp)
                            }
                            Spacer(Modifier.height(9.dp))
                            Row(Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                                Column(Modifier.weight(.75f)) {
                                    LazyColumn(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                                        items(current) { account ->
                                            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).border(1.dp,if(chosen?.id==account.id)Gold else Color(0xFF4D465A),RoundedCornerShape(9.dp)).background(if(chosen?.id==account.id) Color(0xFF3A2916) else Color(0xFF17151F)).clickable { selectedId=account.id }.padding(10.dp)) {
                                                Text("♛  ${account.label.ifBlank { "Account" }}",color=if(chosen?.id==account.id)Gold else Color.White,fontSize=12.sp,fontWeight=FontWeight.Bold,maxLines=1)
                                                Text(account.username,color=Muted,fontSize=10.sp,maxLines=1)
                                            }
                                        }
                                    }
                                    GoldButton("+ Account hinzufügen",{ editing=VaultAccount(game=selectedGame);adding=true },Modifier.fillMaxWidth())
                                }
                                Column(Modifier.weight(1.25f).verticalScroll(rememberScrollState())) {
                                    if(chosen==null) { Text("Noch kein Account vorhanden.",color=Muted,fontSize=12.sp); Text("Erstelle deinen ersten Zugang.",color=Muted,fontSize=11.sp) }
                                    else {
                                        Row(verticalAlignment=Alignment.CenterVertically) {
                                            Text("♙  Account Details",color=Gold,fontWeight=FontWeight.Bold,fontSize=14.sp,modifier=Modifier.weight(1f))
                                            IconButton(onClick={editing=chosen;adding=false},modifier=Modifier.size(30.dp)) { Icon(Icons.Outlined.Edit,"Bearbeiten",tint=Gold) }
                                        }
                                        Detail("Anmeldename",chosen.username)
                                        SecretDetail("Passwort",chosen.password)
                                        Detail("E-Mail",chosen.email)
                                        if(chosen.emailPassword.isNotEmpty()) SecretDetail("E-Mail Passwort",chosen.emailPassword)
                                        if(chosen.gamigoUsername.isNotBlank() || chosen.gamigoPassword.isNotBlank()) {
                                            Text("◆  Gamigo Zugang",color=Gold,fontSize=12.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=8.dp))
                                            Detail("Gamigo Benutzername",chosen.gamigoUsername)
                                            SecretDetail("Gamigo Passwort",chosen.gamigoPassword)
                                        }
                                        Text("▤  Notizen",color=Gold,fontSize=12.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=8.dp))
                                        Text(chosen.notes.ifBlank { "Keine Notizen" },color=Color.White,fontSize=11.sp,modifier=Modifier.fillMaxWidth().padding(vertical=5.dp))
                                        TextButton(onClick={persist(accounts.filterNot { it.id==chosen.id });selectedId=null}) { Text("Account löschen",color=Color(0xFFFF9999),fontSize=11.sp) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if(message.isNotBlank()) { LaunchedEffect(message) { kotlinx.coroutines.delay(2000);message="" }; Text(message,color=Gold,modifier=Modifier.align(Alignment.BottomCenter).background(Ink).padding(8.dp)) }
        }
        if(editing!=null) { AccountEditor(editing!!,onDismiss={editing=null},onSave={ account -> persist(if(adding) accounts+account else accounts.map { if(it.id==account.id) account else it });selectedId=account.id;selectedGame=account.game;editing=null }) }
        if(newGameDialog) { var name by remember { mutableStateOf("") }; AlertDialog(onDismissRequest={newGameDialog=false},title={Text("Spiel hinzufügen")},text={ OutlinedTextField(name,{name=it},label={Text("Spielname")}) },confirmButton={GoldButton("Hinzufügen", onClick = { if(name.isNotBlank()) {customGames.add(name.trim());selectedGame=name.trim()};newGameDialog=false })},dismissButton={TextButton(onClick={newGameDialog=false}){Text("Abbrechen")}}) }
    }
}
@Composable private fun Stat(icon:String,number:Int,label:String,modifier:Modifier=Modifier) { Frame(modifier.height(72.dp)) { Row(verticalAlignment=Alignment.CenterVertically) { Text(icon,color=Gold,fontSize=26.sp); Spacer(Modifier.width(10.dp)); Column { Text("$number",color=Gold,fontSize=25.sp,fontWeight=FontWeight.Bold); Text(label,color=Color.White,fontSize=11.sp) } } } }
@Composable private fun Detail(label:String,value:String) { Column(Modifier.fillMaxWidth().padding(vertical=4.dp)) { Text(label,color=Muted,fontSize=10.sp); Text(value.ifBlank { "—" },color=Color.White,fontSize=12.sp) }; HorizontalDivider(color=Color(0xFF34303C)) }
@Composable private fun SecretDetail(label:String,value:String) { var visible by remember(value) { mutableStateOf(false) }; Column(Modifier.fillMaxWidth().padding(vertical=3.dp)) { Text(label,color=Muted,fontSize=10.sp); Row(verticalAlignment=Alignment.CenterVertically) { Text(if(visible) value else "•".repeat(value.length.coerceIn(8,18)),color=Color.White,fontSize=12.sp,modifier=Modifier.weight(1f)); IconButton(onClick={visible=!visible},modifier=Modifier.size(28.dp)) { Icon(if(visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,"Passwort anzeigen",tint=Gold,modifier=Modifier.size(19.dp)) } } }; HorizontalDivider(color=Color(0xFF34303C)) }
@Composable private fun AccountEditor(initial:VaultAccount,onDismiss:()->Unit,onSave:(VaultAccount)->Unit) {
    var label by remember { mutableStateOf(initial.label) }; var username by remember { mutableStateOf(initial.username) }; var password by remember { mutableStateOf(initial.password) }; var email by remember { mutableStateOf(initial.email) }; var emailPassword by remember { mutableStateOf(initial.emailPassword) }; var gamigoUsername by remember { mutableStateOf(initial.gamigoUsername) }; var gamigoPassword by remember { mutableStateOf(initial.gamigoPassword) }; var notes by remember { mutableStateOf(initial.notes) }
    Dialog(onDismissRequest=onDismiss) { Frame(Modifier.widthIn(max=610.dp).heightIn(max=570.dp)) {
        Text("♛  ${if(initial.label.isBlank()) "Account hinzufügen" else "Account bearbeiten"}",color=Gold,fontSize=20.sp,fontWeight=FontWeight.Bold)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            EditField("Account-Bezeichnung",label){label=it}; EditField("Anmeldename",username){username=it}; EditField("Passwort",password,true){password=it}; EditField("E-Mail-Adresse",email){email=it}; EditField("E-Mail-Passwort",emailPassword,true){emailPassword=it}; EditField("Gamigo Benutzername (optional)",gamigoUsername){gamigoUsername=it}; EditField("Gamigo Passwort (optional)",gamigoPassword,true){gamigoPassword=it}; EditField("Notizen",notes){notes=it}
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End) { TextButton(onClick=onDismiss){Text("Abbrechen")}; GoldButton("Speichern", onClick = { onSave(initial.copy(label=label,username=username,password=password,email=email,emailPassword=emailPassword,gamigoUsername=gamigoUsername,gamigoPassword=gamigoPassword,notes=notes)) }) }
    } }
}
@Composable private fun EditField(label:String,value:String,secret:Boolean=false,onChange:(String)->Unit) { var visible by remember { mutableStateOf(false) }; OutlinedTextField(value,onChange,label={Text(label,fontSize=12.sp)},singleLine=label!="Notizen",visualTransformation=if(secret&&!visible)PasswordVisualTransformation() else VisualTransformation.None,trailingIcon=if(secret) { { IconButton(onClick={visible=!visible}) { Icon(if(visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,"Sichtbarkeit",tint=Gold) } } } else null,modifier=Modifier.fillMaxWidth().padding(vertical=3.dp),textStyle=androidx.compose.ui.text.TextStyle(fontSize=13.sp,color=Color.White)) }
