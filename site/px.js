(function(){
const Y=window.YA||{};
const PAGES=[["index.html","Home"],["features.html","Features"],["changelogs.html","Changelogs"],["servers.html","Servers"],["install.html","Download"],["faq.html","FAQ"],["store.html","Store"]];
const here=(location.pathname.split("/").pop()||"index.html").replace(/\/$/,"")||"index.html";
const EXE="https://github.com/"+Y.repo+"/releases/download/latest/YA-CLIENT.exe";

/* ---- pixel icons (8x8 bitmaps) ---- */
const IC={
 sword:["......##",".....###","....###.","#..###..","##.##...",".###....",".##.....","#..#...."],
 hud:["#######.","#.....#.","#.###.#.","#.....#.","#.##..#.","#.....#.","#######.","........"],
 eye:["........","..####..",".#....#.","#..##..#","#..##..#",".#....#.","..####..","........"],
 bolt:["...###..","..###...",".###....","#######.","..###...","..##....",".##.....",".#......"],
 aim:["...##...","...##...","...##...","###..###","###..###","...##...","...##...","...##..."],
 tag:["..####..",".######.","########","#..##..#","#..##..#","########",".######.","..####.."],
 chat:["########","#......#","#.####.#","#......#","#.##...#","########","..##....",".#......"],
 pin:["..####..",".######.","##....##","##....##",".######.","..####..","...##...","...##..."],
 disc:["########","#.#..#.#","#......#","#.####.#","#.#..#.#","#......#","#.####.#","########"],
 clock:["..####..",".#....#.","#..#...#","#..#...#","#..###.#","#......#",".#....#.","..####.."],
 dl:["...##...","...##...","...##...","#######.",".#####..","..###...","...#....","########"],
 star:["...##...","...##...","########",".######.","..####..",".##..##.","##....##","#......#"]
};
function icon(n){const b=IC[n]||IC.star;let r="";b.forEach((row,y)=>[...row].forEach((c,x)=>{if(c=="#")r+=`<rect x="${x}" y="${y}" width="1" height="1"/>`}));
 return `<svg class="ico" viewBox="0 0 8 8" fill="currentColor" shape-rendering="crispEdges">${r}</svg>`}
window.pxIcon=icon;

/* ---- header / footer ---- */
const hd=document.createElement("header");
hd.innerHTML=`<div class="wrap nav"><a class="brand" href="index.html"><img src="/px/logo-white.png" alt="">YA CLIENT</a>
<button class="burger" aria-label="Menu">=</button>
<div class="links">${PAGES.map(p=>`<a class="l${here==p[0]?" on":""}" href="${p[0]}">${p[1]}</a>`).join("")}<a class="btn dark" href="install.html">Download</a></div></div>`;
document.body.prepend(hd);
hd.querySelector(".burger").onclick=()=>hd.querySelector(".links").classList.toggle("open");
const ft=document.createElement("footer");
ft.innerHTML=`<div class="wrap fr"><div><a href="features.html">Features</a><a href="changelogs.html">Changelogs</a><a href="servers.html">Servers</a><a href="install.html">Download</a><a href="faq.html">FAQ</a><a href="${Y.discord}">Discord</a>
<small>Powered by YA Projects &middot; YA CLIENT is not affiliated with Mojang Studios or Microsoft. Minecraft is a trademark of Mojang Studios.</small></div><img src="/px/logo-white.png" width="56" height="56" alt=""></div>`;
document.body.appendChild(ft);
document.querySelectorAll("[data-dl]").forEach(a=>a.href=EXE);
document.querySelectorAll("[data-icon]").forEach(e=>e.insertAdjacentHTML("afterbegin",icon(e.dataset.icon)));

/* ---- data: GitHub first (you edit it there), local copy as backup ---- */
window.loadData=async function(name){
  const raw="https://raw.githubusercontent.com/"+Y.repo+"/HEAD/data/"+name+".json?t="+Math.floor(Date.now()/60000);
  try{const r=await fetch(raw);if(r.ok)return await r.json()}catch(e){}
  try{const r=await fetch("/data/"+name+".json");if(r.ok)return await r.json()}catch(e){}
  return [];
};
window.newsImg=function(x){if(!x)return"";return /^https?:/.test(x)?x:"https://raw.githubusercontent.com/"+Y.repo+"/HEAD/data/images/"+x};
window.esc=s=>String(s==null?"":s).replace(/[&<>"]/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;"}[c]));

/* ---- reveal on scroll ---- */
const io=new IntersectionObserver(es=>es.forEach(e=>{if(e.isIntersecting){e.target.classList.add("in");io.unobserve(e.target)}}),{threshold:.08});
window.reveal=()=>document.querySelectorAll(".reveal:not(.in)").forEach(e=>io.observe(e));
setTimeout(reveal,50);

/* ---- pixel night scene for the hero ---- */
const cv=document.getElementById("scene");
if(cv){
  const S=4;let w,h,stars=[],clouds=[];let t=0;const ctx=cv.getContext("2d");
  function rnd(i){const x=Math.sin(i*127.1)*43758.5453;return x-Math.floor(x)}
  function size(){const p=cv.parentElement;w=Math.ceil(p.clientWidth/S);h=Math.ceil(p.clientHeight/S);cv.width=w;cv.height=h;
    stars=[];for(let i=0;i<70;i++)stars.push([Math.floor(rnd(i)*w),Math.floor(rnd(i+99)*h*.55),i]);
    clouds=[];for(let i=0;i<5;i++)clouds.push({x:rnd(i+5)*w,y:8+rnd(i+20)*h*.3,l:14+Math.floor(rnd(i+40)*16),s:.04+rnd(i+60)*.06})}
  const hill=(x,a,f,o,base)=>Math.floor(base+Math.sin(x*f+o)*a+Math.sin(x*f*2.3+o*1.7)*a*.45);
  function tree(x,y,c){ctx.fillStyle=c;for(let r=0;r<7;r++){const half=Math.floor(r/2)+1;ctx.fillRect(x-half,y-14+r*2,half*2+1,2)}ctx.fillRect(x,y-1,1,2)}
  function draw(){
    t++;
    // sky bands
    const bands=["#050505","#080808","#0b0b0b","#0f0f0f","#131313","#171717"];
    for(let i=0;i<bands.length;i++){ctx.fillStyle=bands[i];ctx.fillRect(0,Math.floor(i*h*.6/bands.length),w,Math.ceil(h*.6/bands.length)+1)}
    ctx.fillStyle="#1b1b1b";ctx.fillRect(0,Math.floor(h*.6),w,h);
    // stars
    stars.forEach(s=>{const tw=(Math.floor(t/10)+s[2])%7;ctx.fillStyle=tw==0?"#444":tw<3?"#bbb":"#fff";ctx.fillRect(s[0],s[1],1,1)});
    // moon
    const mx=Math.floor(w*.82),my=14;ctx.fillStyle="#e9e9e9";ctx.fillRect(mx,my,12,12);ctx.fillStyle="#bfbfbf";ctx.fillRect(mx+3,my+3,3,3);ctx.fillRect(mx+8,my+7,2,2);
    // clouds
    clouds.forEach(c=>{c.x+=c.s;if(c.x>w+30)c.x=-30;ctx.fillStyle="#262626";const x=Math.floor(c.x),y=Math.floor(c.y);ctx.fillRect(x,y,c.l,3);ctx.fillRect(x+3,y-3,c.l-8,3);ctx.fillRect(x+6,y+3,c.l-10,2)});
    // hills
    const L=[["#161616",9,.045,1,.66],["#1e1e1e",7,.06,3,.74],["#292929",5,.08,6,.83]];
    L.forEach((l,i)=>{for(let x=0;x<w;x++){const y=hill(x,l[1],l[2],l[3],h*l[4]);ctx.fillStyle=l[0];ctx.fillRect(x,y,1,h-y);if(i==2&&x%2==0){ctx.fillStyle="#3a3a3a";ctx.fillRect(x,y,1,1)}}});
    for(let i=0;i<Math.floor(w/9);i++){const x=Math.floor(rnd(i+300)*w),y=hill(x,5,.08,6,h*.83);if(rnd(i+7)>.35)tree(x,y+2,"#0d0d0d")}
    // ground
    ctx.fillStyle="#111";ctx.fillRect(0,h-10,w,10);ctx.fillStyle="#3b3b3b";for(let x=0;x<w;x+=2)ctx.fillRect(x,h-10,1,1);
  }
  size();draw();setInterval(draw,90);addEventListener("resize",()=>{size();draw()});
}
})();
