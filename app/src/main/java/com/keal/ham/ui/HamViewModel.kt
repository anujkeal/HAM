package com.keal.ham.ui
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.keal.ham.data.CloneEntity
import com.keal.ham.data.CloneRepository
import com.keal.ham.domain.InstalledApp
import com.keal.ham.engine.BlackBoxEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
data class HamState(val clones:List<CloneEntity> = emptyList(),val apps:List<InstalledApp> = emptyList(),val query:String="",val system:Boolean=false,val recentSort:Boolean=false,val selected:InstalledApp?=null,val page:String="home",val busy:Boolean=false,val message:String?=null,val error:String?=null)
class HamViewModel(app:Application):AndroidViewModel(app) {
 private val repo=CloneRepository(app,BlackBoxEngine())
 private val _state=MutableStateFlow(HamState())
 val state=_state.asStateFlow()
 init { viewModelScope.launch { repo.clones.collectLatest { _state.value=_state.value.copy(clones=it) } } }
 fun home(){_state.value=_state.value.copy(page="home",selected=null)}
 fun settings(){_state.value=_state.value.copy(page="settings")}
 fun picker(){_state.value=_state.value.copy(page="picker",query="",selected=null);refresh()}
 fun query(v:String){_state.value=_state.value.copy(query=v)}
 fun system(v:Boolean){_state.value=_state.value.copy(system=v);refresh()}
 fun recentSort(v:Boolean){_state.value=_state.value.copy(recentSort=v)}
 fun select(a:InstalledApp){_state.value=_state.value.copy(selected=a,page="confirm")}
 private fun refresh(){viewModelScope.launch{_state.value=_state.value.copy(busy=true);runCatching{repo.installed(_state.value.system)}.onSuccess{_state.value=_state.value.copy(apps=it,busy=false)}.onFailure{_state.value=_state.value.copy(error=it.message,busy=false)}}}
 fun create(name:String){val a=_state.value.selected?:return;viewModelScope.launch{_state.value=_state.value.copy(busy=true);val r=repo.create(a,name.ifBlank{"${a.label} 2"});_state.value=_state.value.copy(busy=false,page=if(r.isSuccess)"home" else "confirm",selected=if(r.isSuccess)null else a,message=if(r.isSuccess)"Clone created." else null,error=r.exceptionOrNull()?.message)}}
 fun launch(c:CloneEntity){viewModelScope.launch{val r=repo.launch(c);_state.value=_state.value.copy(message=if(r.isSuccess)"Launching ${c.cloneName}" else null,error=r.exceptionOrNull()?.message)}}
 fun rename(c:CloneEntity,n:String){viewModelScope.launch{repo.rename(c,n)}}
 fun clear(c:CloneEntity){viewModelScope.launch{repo.clear(c).onSuccess{_state.value=_state.value.copy(message="Clone data cleared.")}.onFailure{_state.value=_state.value.copy(error=it.message)}}}
 fun stop(c:CloneEntity){viewModelScope.launch{repo.stop(c)}}
 fun delete(c:CloneEntity){viewModelScope.launch{repo.delete(c).onSuccess{_state.value=_state.value.copy(message="Clone deleted.")}.onFailure{_state.value=_state.value.copy(error=it.message)}}}
 fun dismiss(){_state.value=_state.value.copy(message=null,error=null)}
}
