(function(){
var Y=window.YA||{};
var P=[["/","Home"],["/features","Features"],["/changelogs","Changelogs"],["/servers","Servers"],["/faq","FAQ"],["/store","Store"]];
var path=location.pathname.replace(/\.html$/,"").replace(/\/$/,"")||"/";
if(path==="/index")path="/";
if(path==="/item"||path==="/bundle")path="/store";
var h=document.createElement("header");h.className="pxh";
h.innerHTML='<div class="pxw pxn"><a class="pxb" href="/"><img src="/px/logo-white.png" alt="">YA CLIENT</a><button class="pxburger" aria-label="Menu">=</button><div class="pxl">'+
 P.map(function(p){return '<a class="pl'+(path===p[0]?' on':'')+(p[0]==="/plus"?' gold':'')+'" href="'+p[0]+'">'+p[1]+'</a>'}).join('')+'<a class="pxbtn dark" href="/download">Download</a></div></div>';
document.body.prepend(h);
h.querySelector(".pxburger").onclick=function(){h.querySelector(".pxl").classList.toggle("open")};
var f=document.createElement("footer");f.className="pxf";
f.innerHTML='<div class="pxw pxfr"><div>'+P.map(function(p){return '<a href="'+p[0]+'">'+p[1]+'</a>'}).join('')+'<a href="'+(Y.discord||'#')+'" target="_blank" rel="noopener">Discord</a><small>Powered by YA Projects &middot; YA CLIENT is not affiliated with Mojang Studios or Microsoft.</small></div><img src="/px/logo-white.png" width="56" height="56" alt=""></div>';
document.body.appendChild(f);
})();
