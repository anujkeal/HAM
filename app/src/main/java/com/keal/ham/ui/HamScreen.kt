package com.keal.ham.ui
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.keal.ham.data.CloneEntity
import com.keal.ham.domain.InstalledApp

@OptIn(ExperimentalFoundationApi::class,ExperimentalMaterial3Api::class)
@Composable fun HamScreen(vm:HamViewModel){
 val s by vm.state.collectAsState()
 BackHandler(enabled=s.page!="home"){vm.home()}
 var actions by remember{mutableStateOf<CloneEntity?>(null)}
 var deleteCandidate by remember{mutableStateOf<CloneEntity?>(null)}
 var rename by remember{mutableStateOf("")}
 var cloneName by remember(s.selected){mutableStateOf(s.selected?.let{"${it.label} 2"}?:"")}
 Scaffold(topBar={TopAppBar(title={Column{Text("HAM",fontWeight=FontWeight.Black);Text("Hold A Minute",style=MaterialTheme.typography.labelSmall)}},navigationIcon={if(s.page!="home")IconButton(onClick=vm::home){Icon(Icons.Default.ArrowBack,null)}},actions={IconButton(onClick=vm::settings){Icon(Icons.Default.Settings,null)}})},floatingActionButton={if(s.page=="home")FloatingActionButton(onClick=vm::picker){Icon(Icons.Default.Add,"Clone app")}}){pad->
 when(s.page){"picker"->Picker(s,vm,Modifier.padding(pad));"confirm"->Confirm(s,cloneName,{cloneName=it},vm,Modifier.padding(pad));"settings"->Settings(s,Modifier.padding(pad),vm);else->Home(s,vm,Modifier.padding(pad)){actions=it;rename=it.cloneName}}
 }
 actions?.let{c->AlertDialog(onDismissRequest={actions=null},title={Text(c.cloneName)},text={Column{OutlinedTextField(rename,{rename=it},label={Text("Clone name")});TextButton(onClick={vm.rename(c,rename);actions=null}){Text("Rename")};TextButton(onClick={vm.clear(c);actions=null}){Text("Clear data")};TextButton(onClick={vm.stop(c);actions=null}){Text("Force stop")};TextButton(onClick={deleteCandidate=c;actions=null}){Text("Delete clone",color=MaterialTheme.colorScheme.error)}}},confirmButton={TextButton(onClick={actions=null}){Text("Close")}})}
 deleteCandidate?.let{c->AlertDialog(onDismissRequest={deleteCandidate=null},title={Text("Delete clone?")},text={Text("This removes ${c.cloneName} and its virtual app data.")},confirmButton={TextButton(onClick={vm.delete(c);deleteCandidate=null}){Text("Delete",color=MaterialTheme.colorScheme.error)}},dismissButton={TextButton(onClick={deleteCandidate=null}){Text("Cancel")}})}
 if(s.error!=null||s.message!=null)AlertDialog(onDismissRequest=vm::dismiss,title={Text(if(s.error!=null)"Error" else "HAM")},text={Text(s.error?:s.message.orEmpty())},confirmButton={TextButton(onClick=vm::dismiss){Text("OK")}})
}
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable private fun Home(s:HamState,vm:HamViewModel,m:Modifier=Modifier,onLong:(CloneEntity)->Unit){
 val context = LocalContext.current
 Column(m.fillMaxSize().padding(20.dp)){Text("Clone an app, then use it right here.",color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(24.dp));Row(verticalAlignment=Alignment.CenterVertically){Text("Your clones",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Spacer(Modifier.weight(1f));Text("${s.clones.size} apps",color=MaterialTheme.colorScheme.onSurfaceVariant)};Spacer(Modifier.height(12.dp))
 if(s.clones.isEmpty())Card{Column(Modifier.fillMaxWidth().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("No clones yet",style=MaterialTheme.typography.titleMedium);Text("Choose an installed app to create your first independent space.",color=MaterialTheme.colorScheme.onSurfaceVariant);Button(onClick=vm::picker){Icon(Icons.Default.Add,null);Text("Clone app")}}}
 else LazyVerticalGrid(GridCells.Fixed(3),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){items(s.clones,key={it.id}){c->Column(Modifier.combinedClickable(onClick={vm.launch(c)},onLongClick={onLong(c)}).padding(6.dp),horizontalAlignment=Alignment.CenterHorizontally){Surface(Modifier.size(66.dp),shape=MaterialTheme.shapes.large,color=MaterialTheme.colorScheme.surfaceVariant){Box(contentAlignment=Alignment.Center){val icon=remember(c.packageName){runCatching{context.packageManager.getApplicationIcon(c.packageName).toBitmap(96,96).asImageBitmap()}.getOrNull()};if(icon!=null)androidx.compose.foundation.Image(icon,null,Modifier.fillMaxSize()) else Text(c.appName.take(1),style=MaterialTheme.typography.headlineMedium)}};Text(c.cloneName,maxLines=1,style=MaterialTheme.typography.labelLarge);Row(verticalAlignment=Alignment.CenterVertically){Surface(color=androidx.compose.ui.graphics.Color(0xFFCDF45E),shape=MaterialTheme.shapes.small){Text("CLONE",Modifier.padding(horizontal=5.dp,vertical=2.dp),color=androidx.compose.ui.graphics.Color(0xFF273000),style=MaterialTheme.typography.labelSmall)};Text(" ${c.status}",style=MaterialTheme.typography.labelSmall)}}};item{OutlinedCard(onClick=vm::picker,modifier=Modifier.height(120.dp)){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.Add,null);Text("Clone app")}}}}}
 }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable private fun Picker(s:HamState,vm:HamViewModel,m:Modifier=Modifier){
 val list=remember(s.apps,s.query,s.clones,s.recentSort){
  val filtered=s.apps.filter{it.label.contains(s.query,true)||it.packageName.contains(s.query,true)}
  if(!s.recentSort) filtered.sortedBy{it.label.lowercase()} else {
   val recent=s.clones.sortedByDescending{it.lastLaunchedAt?:it.createdAt}.map{it.packageName}.distinct()
   filtered.sortedBy{val index=recent.indexOf(it.packageName);if(index<0) Int.MAX_VALUE else index}
  }
 }
 Column(m.fillMaxSize().padding(horizontal=18.dp)){Spacer(Modifier.height(10.dp));Text("Choose an app",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);OutlinedTextField(s.query,vm::query,Modifier.fillMaxWidth(),label={Text("Search apps")},singleLine=true);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){TextButton(onClick={vm.recentSort(false)}){Text("A–Z",fontWeight=if(!s.recentSort)FontWeight.Bold else FontWeight.Normal)};TextButton(onClick={vm.recentSort(true)}){Text("Recently cloned",fontWeight=if(s.recentSort)FontWeight.Bold else FontWeight.Normal)}};Row(verticalAlignment=Alignment.CenterVertically){Text("Show system apps",Modifier.weight(1f));Switch(s.system,vm::system)};if(s.busy)LinearProgressIndicator(Modifier.fillMaxWidth());LazyColumn{items(list.size){i->val a=list[i];Row(Modifier.fillMaxWidth().combinedClickable(onClick={vm.select(a)}),verticalAlignment=Alignment.CenterVertically){androidx.compose.foundation.Image(a.icon.toBitmap(96,96).asImageBitmap(),null,Modifier.size(48.dp));Column(Modifier.padding(12.dp)){Text(a.label,fontWeight=FontWeight.SemiBold);Text(a.packageName,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}};HorizontalDivider()}}}
}
@Composable private fun Confirm(s:HamState,name:String,onName:(String)->Unit,vm:HamViewModel,m:Modifier=Modifier){
 val a=s.selected?:return
 val context=LocalContext.current
 val risky=remember(a.packageName){runCatching{val json=org.json.JSONObject(context.assets.open("risk_apps.json").bufferedReader().use{it.readText()});val packages=json.getJSONArray("packages");(0 until packages.length()).any{packages.getString(it)==a.packageName}}.getOrDefault(false)}
 Column(m.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally){Spacer(Modifier.height(25.dp));androidx.compose.foundation.Image(a.icon.toBitmap(128,128).asImageBitmap(),null,Modifier.size(90.dp));Text(a.label,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(a.packageName,style=MaterialTheme.typography.bodySmall);Spacer(Modifier.height(22.dp));OutlinedTextField(name,onName,Modifier.fillMaxWidth(),label={Text("Clone name")},singleLine=true);Row(Modifier.fillMaxWidth().padding(vertical=8.dp)){Text("Version",Modifier.weight(1f));Text(a.version)};Column(Modifier.fillMaxWidth().padding(vertical=8.dp)){Row(Modifier.fillMaxWidth().padding(vertical=6.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("Base APK",Modifier.weight(1f));Text(String.format("%.1f MB",a.apkBytes/1048576.0))};if(a.splitApkBytes>0){Row(Modifier.fillMaxWidth().padding(vertical=6.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("Split APKs",Modifier.weight(1f));Text(String.format("%.1f MB",a.splitApkBytes/1048576.0))}};HorizontalDivider(Modifier.padding(vertical=4.dp));Row(Modifier.fillMaxWidth().padding(vertical=6.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("Total APK files",Modifier.weight(1f),fontWeight=FontWeight.SemiBold);Text(String.format("%.1f MB",(a.apkBytes+a.splitApkBytes)/1048576.0),fontWeight=FontWeight.SemiBold)};Text("Installed APK file sizes, not the app-store download size or app data.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};if(risky)Text("This app may not work in HAM or may restrict your account.",color=MaterialTheme.colorScheme.error);Spacer(Modifier.height(24.dp));Column(Modifier.fillMaxWidth()){Button(onClick={vm.create(name)},enabled=!s.busy,modifier=Modifier.fillMaxWidth()){if(s.busy)CircularProgressIndicator(Modifier.size(18.dp))else Text("Clone")};if(s.busy){Spacer(Modifier.height(12.dp));LinearProgressIndicator(Modifier.fillMaxWidth());Text(s.progressMessage?: "Working…",modifier=Modifier.padding(top=8.dp),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);Text("Large apps can take several minutes to install the first time. Keep HAM open.",modifier=Modifier.padding(top=4.dp),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun Settings(s:HamState,m:Modifier,vm:HamViewModel){Column(m.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp)){Text("Settings",style=MaterialTheme.typography.headlineSmall);Spacer(Modifier.height(18.dp));Text("Compatibility",style=MaterialTheme.typography.titleMedium);if(s.clones.isEmpty())Text("Apps you try will appear here with their launch status.") else s.clones.forEach{c->ListItem(headlineContent={Text(c.appName)},supportingContent={Text(c.packageName)},trailingContent={Text(c.status,color=if(c.status=="Works")MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)})};Spacer(Modifier.height(20.dp));Text("Disclaimer",style=MaterialTheme.typography.titleMedium);Text("Clones run the real app in a virtual space. Some apps may not work or may restrict accounts. Use at your own risk.")}}
