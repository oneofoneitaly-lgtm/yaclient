(function(){
var bar=document.getElementById('plusbar2');if(!bar)return;
bar.className='plusbar';
bar.innerHTML='<div><b>YA Plus members: -50% on the whole shop</b><span>Enter your Minecraft name to check your YA Plus discount.</span></div>'+
 '<form class="pbf" autocomplete="off"><input type="text" maxlength="16" placeholder="Your Minecraft name" spellcheck="false" aria-label="Minecraft name"><button class="btn" type="submit">Check</button></form><a class="btn" href="/plus">See YA Plus →</a>';
var inp=bar.querySelector('input'),key='ya_skin_name';
try{inp.value=localStorage.getItem(key)||''}catch(e){}
function check(name){
 name=(name||'').trim();
 if(!/^[A-Za-z0-9_]{3,16}$/.test(name)){window.YAplusOn=false;window.YAplusName='';document.dispatchEvent(new CustomEvent('yaplus',{detail:{on:false}}));return}
 try{localStorage.setItem(key,name)}catch(e){}
 fetch('/v1/plus?name='+encodeURIComponent(name),{cache:'no-store'}).then(function(r){return r.json()}).then(function(j){
  var on=!!j&&j.plus==='1';window.YAplusOn=on;window.YAplusName=on?name:'';
  document.dispatchEvent(new CustomEvent('yaplus',{detail:{on:on,name:name,known:j&&j.known==='1'}}));
  var s=bar.querySelector('div span');if(s&&!on)s.textContent='No YA Plus found for '+name+'.';
 }).catch(function(){});
}
bar.querySelector('form').addEventListener('submit',function(e){e.preventDefault();check(inp.value)});
if(inp.value)check(inp.value);
})();
