/* "Get free": type your Minecraft name and the item is added to your account on the spot. */
(function(){
function el(h){var d=document.createElement('div');d.innerHTML=h;return d.firstChild}
function open(item,title){
  var saved='';try{saved=localStorage.getItem('ya_skin_name')||''}catch(e){}
  var o=el('<div class="cl-o"><div class="cl-m"><button class="cl-x" aria-label="Close">x</button><h3>Get '+title+'</h3><p>Type your Minecraft name and it is added to your account right now. It is free.</p><form><input type="text" maxlength="16" spellcheck="false" placeholder="Your Minecraft name" autocomplete="off"><button class="btn pri" type="submit">Get free</button></form><div class="cl-msg"></div></div></div>');
  document.body.appendChild(o);
  var inp=o.querySelector('input'),msg=o.querySelector('.cl-msg'),btn=o.querySelector('form .btn');inp.value=saved;inp.focus();
  function close(){o.remove()}
  o.querySelector('.cl-x').onclick=close;o.addEventListener('click',function(e){if(e.target===o)close()});
  o.querySelector('form').addEventListener('submit',function(e){
    e.preventDefault();var n=inp.value.trim();
    if(!/^[A-Za-z0-9_]{3,16}$/.test(n)){msg.className='cl-msg err';msg.textContent='Enter a valid Minecraft name.';return}
    btn.disabled=true;msg.className='cl-msg';msg.textContent='Adding it to '+n+'...';
    fetch('/v1/claim',{method:'POST',headers:{'content-type':'application/x-www-form-urlencoded'},body:'username='+encodeURIComponent(n)+'&item='+item}).then(function(r){return r.json().then(function(j){return{ok:r.ok,j:j}})}).then(function(x){
      btn.disabled=false;
      if(x.ok){try{localStorage.setItem('ya_skin_name',x.j.name)}catch(e){}msg.className='cl-msg ok';msg.innerHTML='Done! The '+title+' is now on <b>'+x.j.name+'</b>. Open YA CLIENT and join a world: you will wear it.<br><a class="btn sm" href="/download" style="margin-top:12px">Download YA CLIENT</a>'}
      else{msg.className='cl-msg err';msg.textContent=x.j.error==='unknown username'?'We could not find that Minecraft name. Check the spelling and try again in a minute.':(x.j.error||'Something went wrong, try again.')}
    }).catch(function(){btn.disabled=false;msg.className='cl-msg err';msg.textContent='Could not reach the server, try again.'});
  });
}
document.addEventListener('click',function(e){var t=e.target.closest('[data-claim]');if(!t)return;e.preventDefault();e.stopPropagation();open(t.dataset.claim,t.dataset.title||'YA Cape')},true);
})();
