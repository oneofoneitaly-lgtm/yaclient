'use strict';
const $ = (s, r = document) => r.querySelector(s);
const esc = (s) => String(s == null ? '' : s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const safeImg = (u) => (/^(https:\/\/|\.\.\/assets\/[\w\/.-]+$|data:image\/png;base64,[A-Za-z0-9+\/=]+$)/i.test(u || '') ? u : '');

const I18N = {
  en: { launchpad: 'Launchpad', mods: 'Modules', servers: 'Servers', cosmetics: 'Cosmetics', settings: 'Settings', launch: 'LAUNCH GAME', launching: 'LAUNCHING...',
    welcome: 'Minecraft 1.8.9 PvP client', news: 'News', noAcc: 'Not signed in', signIn: 'Sign in', switchAcc: 'Switch account', advert: 'Ad space',
    play: 'Play', noServers: 'No servers yet. Add them in launcher/content.json', modsHint: 'Enable or disable modules. Changes apply the next time you launch.',
    store: 'Open store', storeHint: 'Cosmetics are bought on our website and equipped in game (Right Shift > Cosmetics).', notConf: 'The store is not configured yet.', offline: 'Store unreachable.',
    language: 'Language', ram: 'Memory (MB)', java: 'Java 8 path', detect: 'Detect', minimize: 'Minimize when game starts', folder: 'Open game folder', logout: 'Sign out', console: 'Console',
    microsoft: 'Microsoft login', offlineL: 'Offline name', offlineBtn: 'Continue offline', cancel: 'Close', name_short: 'Name must be at least 3 characters.', ms_failed: 'Microsoft login failed.',
    need_login: 'Sign in first.', session_expired: 'Your Microsoft session expired. Sign in again (Account > Sign in).', launch_failed: 'Launch failed.', launch_error: 'Error', crashed: 'The game closed unexpectedly. Check the console.', closed: 'Game closed.', ingame: 'In game',
    updating: 'Updating client...', loader: 'Preparing loader...', starting: 'Starting game...', download: 'Downloading game files...', wings: 'Wings', cape: 'Capes', pet: 'Pets', bandana: 'Bandanas', all: 'All', price: 'Price', online: 'Online', javaHint: 'Optional: the launcher downloads Java by itself' },
  it: { launchpad: 'Launchpad', mods: 'Moduli', servers: 'Server', cosmetics: 'Cosmetici', settings: 'Impostazioni', launch: 'AVVIA GIOCO', launching: 'AVVIO...',
    welcome: 'Client PvP per Minecraft 1.8.9', news: 'Novità', noAcc: 'Non connesso', signIn: 'Accedi', switchAcc: 'Cambia account', advert: 'Spazio pubblicitario',
    play: 'Gioca', noServers: 'Nessun server. Aggiungili in launcher/content.json', modsHint: 'Attiva o disattiva i moduli. Le modifiche valgono al prossimo avvio.',
    store: 'Apri lo store', storeHint: 'I cosmetici si comprano sul nostro sito e si equipaggiano in gioco (Shift destro > Cosmetici).', notConf: 'Lo store non è ancora configurato.', offline: 'Store non raggiungibile.',
    language: 'Lingua', ram: 'Memoria (MB)', java: 'Percorso Java 8', detect: 'Rileva', minimize: 'Riduci a icona all\'avvio', folder: 'Apri cartella di gioco', logout: 'Esci', console: 'Console',
    microsoft: 'Accesso Microsoft', offlineL: 'Nome offline', offlineBtn: 'Continua offline', cancel: 'Chiudi', name_short: 'Il nome deve avere almeno 3 caratteri.', ms_failed: 'Accesso Microsoft non riuscito.',
    need_login: 'Accedi prima.', session_expired: 'La sessione Microsoft è scaduta. Accedi di nuovo (Account > Accedi).', launch_failed: 'Avvio non riuscito.', launch_error: 'Errore', crashed: 'Il gioco si è chiuso all\'improvviso. Controlla la console.', closed: 'Gioco chiuso.', ingame: 'In gioco',
    updating: 'Aggiorno il client...', loader: 'Preparo il loader...', starting: 'Avvio il gioco...', download: 'Scarico i file di gioco...', wings: 'Ali', cape: 'Mantelli', pet: 'Animaletti', bandana: 'Bandane', all: 'Tutti', price: 'Prezzo', online: 'Online', javaHint: 'Facoltativo: il launcher scarica Java da solo' },
  es: { launchpad: 'Launchpad', mods: 'Módulos', servers: 'Servidores', cosmetics: 'Cosméticos', settings: 'Ajustes', launch: 'INICIAR JUEGO', launching: 'INICIANDO...',
    welcome: 'Cliente PvP de Minecraft 1.8.9', news: 'Noticias', noAcc: 'Sin sesión', signIn: 'Iniciar sesión', switchAcc: 'Cambiar cuenta', advert: 'Espacio publicitario',
    play: 'Jugar', noServers: 'Aún no hay servidores. Añádelos en launcher/content.json', modsHint: 'Activa o desactiva módulos. Los cambios se aplican al iniciar.',
    store: 'Abrir tienda', storeHint: 'Los cosméticos se compran en nuestra web y se equipan en el juego (Shift der. > Cosméticos).', notConf: 'La tienda aún no está configurada.', offline: 'Tienda no disponible.',
    language: 'Idioma', ram: 'Memoria (MB)', java: 'Ruta de Java 8', detect: 'Detectar', minimize: 'Minimizar al iniciar', folder: 'Abrir carpeta del juego', logout: 'Cerrar sesión', console: 'Consola',
    microsoft: 'Cuenta Microsoft', offlineL: 'Nombre sin conexión', offlineBtn: 'Continuar sin conexión', cancel: 'Cerrar', name_short: 'El nombre necesita 3 caracteres como mínimo.', ms_failed: 'Error al iniciar sesión con Microsoft.',
    need_login: 'Inicia sesión primero.', session_expired: 'La sesión de Microsoft caducó. Inicia sesión de nuevo.', launch_failed: 'Error al iniciar.', launch_error: 'Error', crashed: 'El juego se cerró inesperadamente. Revisa la consola.', closed: 'Juego cerrado.', ingame: 'En juego',
    updating: 'Actualizando cliente...', loader: 'Preparando loader...', starting: 'Iniciando juego...', download: 'Descargando archivos...', wings: 'Alas', cape: 'Capas', pet: 'Mascotas', bandana: 'Bandanas', all: 'Todo', price: 'Precio', online: 'En línea', javaHint: 'Opcional: el launcher descarga Java solo' },
  de: { launchpad: 'Launchpad', mods: 'Module', servers: 'Server', cosmetics: 'Kosmetik', settings: 'Einstellungen', launch: 'SPIEL STARTEN', launching: 'STARTE...',
    welcome: 'PvP-Client für Minecraft 1.8.9', news: 'Neuigkeiten', noAcc: 'Nicht angemeldet', signIn: 'Anmelden', switchAcc: 'Konto wechseln', advert: 'Werbefläche',
    play: 'Spielen', noServers: 'Noch keine Server. Trage sie in launcher/content.json ein', modsHint: 'Module ein-/ausschalten. Gilt ab dem nächsten Start.',
    store: 'Shop öffnen', storeHint: 'Kosmetik wird auf unserer Website gekauft und im Spiel ausgerüstet (Rechts-Shift > Kosmetik).', notConf: 'Der Shop ist noch nicht eingerichtet.', offline: 'Shop nicht erreichbar.',
    language: 'Sprache', ram: 'Speicher (MB)', java: 'Java-8-Pfad', detect: 'Suchen', minimize: 'Beim Start minimieren', folder: 'Spielordner öffnen', logout: 'Abmelden', console: 'Konsole',
    microsoft: 'Microsoft-Login', offlineL: 'Offline-Name', offlineBtn: 'Offline fortfahren', cancel: 'Schließen', name_short: 'Name braucht mindestens 3 Zeichen.', ms_failed: 'Microsoft-Login fehlgeschlagen.',
    need_login: 'Bitte zuerst anmelden.', launch_failed: 'Start fehlgeschlagen.', launch_error: 'Fehler', crashed: 'Das Spiel wurde unerwartet beendet. Siehe Konsole.', closed: 'Spiel beendet.', ingame: 'Im Spiel',
    updating: 'Client wird aktualisiert...', loader: 'Loader wird vorbereitet...', starting: 'Spiel startet...', download: 'Lade Spieldateien...', wings: 'Flügel', cape: 'Umhänge', pet: 'Haustiere', bandana: 'Bandanas', all: 'Alle', price: 'Preis', online: 'Online', javaHint: 'Optional: der Launcher lädt Java selbst herunter' }
};
Object.assign(I18N.en, {"bundles": "Bundles", "get_it": "View on the website", "owned_free": "Free",  back: 'Welcome back', versions: 'Versions', latest: 'Latest', releases: 'Releases', snapshots: 'Snapshots', old: 'Old', search: 'Search version...', modsFor: 'Modules for Minecraft', avail: 'available on this version',
  needs: 'Only on', vanilla: 'Vanilla', ya: 'YA CLIENT', soon: 'Soon', vanillaNote: 'This version starts without YA CLIENT mods. The mods below work on', java_missing: 'Java {n} is required for this version. Install it (Temurin {n}) and try again.', latestRelease: 'Latest release', change: 'Change version', noRes: 'No versions found.', offlineList: 'Offline: showing a short list of versions.' });
Object.assign(I18N.it, {"bundles": "Bundle", "get_it": "Vedi sul sito", "owned_free": "Gratis",  back: 'Bentornato', versions: 'Versioni', latest: 'Ultima', releases: 'Release', snapshots: 'Snapshot', old: 'Vecchie', search: 'Cerca versione...', modsFor: 'Mod per Minecraft', avail: 'disponibili in questa versione',
  needs: 'Solo su', vanilla: 'Vanilla', ya: 'YA CLIENT', soon: 'Presto', vanillaNote: 'Questa versione parte senza le mod di YA CLIENT. Le mod qui sotto funzionano su', java_missing: 'Per questa versione serve Java {n}. Installalo (Temurin {n}) e riprova.', latestRelease: 'Ultima release', change: 'Cambia versione', noRes: 'Nessuna versione trovata.', offlineList: 'Offline: mostro un elenco ridotto di versioni.' });
Object.assign(I18N.es, { back: 'Bienvenido de nuevo', versions: 'Versiones', latest: 'Última', releases: 'Releases', snapshots: 'Snapshots', old: 'Antiguas', search: 'Buscar versión...', modsFor: 'Mods para Minecraft', avail: 'disponibles en esta versión',
  needs: 'Solo en', vanilla: 'Vanilla', ya: 'YA CLIENT', soon: 'Pronto', vanillaNote: 'Esta versión inicia sin los mods de YA CLIENT. Los mods de abajo funcionan en', java_missing: 'Esta versión necesita Java {n}. Instálalo (Temurin {n}) e inténtalo de nuevo.', latestRelease: 'Última release', change: 'Cambiar versión', noRes: 'No hay versiones.', offlineList: 'Sin conexión: lista reducida de versiones.' });
Object.assign(I18N.de, { session_expired: 'Die Microsoft-Sitzung ist abgelaufen. Bitte neu anmelden.', back: 'Willkommen zurück', versions: 'Versionen', latest: 'Neueste', releases: 'Releases', snapshots: 'Snapshots', old: 'Alt', search: 'Version suchen...', modsFor: 'Mods für Minecraft', avail: 'in dieser Version verfügbar',
  needs: 'Nur in', vanilla: 'Vanilla', ya: 'YA CLIENT', soon: 'Bald', vanillaNote: 'Diese Version startet ohne YA-CLIENT-Mods. Die Mods unten funktionieren in', java_missing: 'Für diese Version wird Java {n} benötigt. Installiere es (Temurin {n}) und versuche es erneut.', latestRelease: 'Neueste Release', change: 'Version ändern', noRes: 'Keine Versionen gefunden.', offlineList: 'Offline: gekürzte Versionsliste.' });
Object.assign(I18N.en, {"plus_nav": "YA Plus", "chip_on": "YA Plus active", "chip_off": "Get YA Plus", "online_s": "Online", "preview": "Preview", "p_title": "YA Plus", "p_promo_t": "Upgrade to YA Plus", "p_promo_d": "Animated cape, 50% off the shop, random rewards and more.", "p_buy": "Get YA Plus", "p_active_t": "YA Plus is active", "p_active_d": "Thanks for supporting YA CLIENT. Your perks are active.", "p_rerun": "Run setup again", "p_how": "Plus is added to your account from our store or Discord.", "perk_theme": "50% off the shop", "perk_cape": "Animated cape movement", "perk_more": "Random cosmetic + monthly bundle", "perk_setup": "Golden logo, Donator tag, priority support", "wp_eyebrow": "YA CLIENT PLUS", "wp_title_a": "Welcome to", "wp_title_b": "Plus", "wp_sub": "Your YA Plus subscription is active on this launcher. Here is what you just unlocked.", "cont": "Continue", "skip": "Skip for now", "back_b": "Back", "finish": "Start playing", "st_title": "Set up your client", "st_sub": "Pick how YA CLIENT should start. You can change everything later in Settings and Modules.", "lbl_preset": "MOD PRESET", "pre_pvp": "PvP", "pre_pvp_d": "Combat mods and utilities on, extra info HUD off", "pre_all": "Everything", "pre_all_d": "Every available mod enabled", "pre_min": "Minimal", "pre_min_d": "Only performance and basic HUD", "lbl_perf": "PERFORMANCE", "ram_l": "Game memory", "ram_d": "Recommended for PvP: 3 GB", "done_t": "You are all set", "done_s": "Your launcher is red and your Plus perks are active in game. Join a server and see your cape move."});
Object.assign(I18N.it, {"plus_nav": "YA Plus", "chip_on": "YA Plus attivo", "chip_off": "Ottieni YA Plus", "online_s": "Online", "preview": "Anteprima", "p_title": "YA Plus", "p_promo_t": "Passa a YA Plus", "p_promo_d": "Mantello animato, 50% di sconto sullo store, premi casuali e altro.", "p_buy": "Ottieni YA Plus", "p_active_t": "YA Plus è attivo", "p_active_d": "Grazie per supportare YA CLIENT. I tuoi vantaggi sono attivi.", "p_rerun": "Rifai la configurazione", "p_how": "Plus viene aggiunto al tuo account dal nostro store o da Discord.", "perk_theme": "50% di sconto sullo store", "perk_cape": "Mantello animato", "perk_more": "Cosmetico casuale + bundle mensile", "perk_setup": "Logo dorato, tag Donator, supporto prioritario", "wp_eyebrow": "YA CLIENT PLUS", "wp_title_a": "Benvenuto in", "wp_title_b": "Plus", "wp_sub": "Il tuo abbonamento YA Plus è attivo su questo launcher. Ecco cosa hai appena sbloccato.", "cont": "Continua", "skip": "Salta per ora", "back_b": "Indietro", "finish": "Inizia a giocare", "st_title": "Configura il tuo client", "st_sub": "Scegli come deve partire YA CLIENT. Potrai cambiare tutto dopo in Impostazioni e Moduli.", "lbl_preset": "PRESET MOD", "pre_pvp": "PvP", "pre_pvp_d": "Mod di combattimento e utilità attive, HUD info spento", "pre_all": "Tutto", "pre_all_d": "Tutte le mod disponibili attive", "pre_min": "Minimo", "pre_min_d": "Solo prestazioni e HUD base", "lbl_perf": "PRESTAZIONI", "ram_l": "Memoria di gioco", "ram_d": "Consigliato per PvP: 3 GB", "done_t": "Tutto pronto", "done_s": "Il launcher è rosso e i tuoi vantaggi Plus sono attivi in gioco. Entra in un server e guarda il mantello muoversi."});
Object.assign(I18N.es, {"plus_nav": "YA Plus", "chip_on": "YA Plus activo", "chip_off": "Consigue YA Plus", "online_s": "En línea", "preview": "Vista previa", "p_title": "YA Plus", "p_promo_t": "Pásate a YA Plus", "p_promo_d": "Capa animada, 50% de descuento en la tienda, premios aleatorios y más.", "p_buy": "Consigue YA Plus", "p_active_t": "YA Plus está activo", "p_active_d": "Gracias por apoyar YA CLIENT. Tus ventajas están activas.", "p_rerun": "Repetir configuración", "p_how": "Plus se añade a tu cuenta desde nuestra tienda o Discord.", "perk_theme": "50% de descuento en la tienda", "perk_cape": "Capa animada", "perk_more": "Cosmético aleatorio + bundle mensual", "perk_setup": "Logo dorado, etiqueta Donator, soporte prioritario", "wp_eyebrow": "YA CLIENT PLUS", "wp_title_a": "Bienvenido a", "wp_title_b": "Plus", "wp_sub": "Tu suscripción YA Plus está activa en este launcher. Esto es lo que acabas de desbloquear.", "cont": "Continuar", "skip": "Omitir por ahora", "back_b": "Atrás", "finish": "Empezar a jugar", "st_title": "Configura tu cliente", "st_sub": "Elige cómo debe iniciar YA CLIENT. Podrás cambiarlo luego en Ajustes y Módulos.", "lbl_preset": "PRESET DE MODS", "pre_pvp": "PvP", "pre_pvp_d": "Mods de combate y utilidades activos, HUD de info apagado", "pre_all": "Todo", "pre_all_d": "Todos los mods disponibles activos", "pre_min": "Mínimo", "pre_min_d": "Solo rendimiento y HUD básico", "lbl_perf": "RENDIMIENTO", "ram_l": "Memoria del juego", "ram_d": "Recomendado para PvP: 3 GB", "done_t": "Todo listo", "done_s": "Tu launcher es rojo y tus ventajas Plus están activas en el juego."});
Object.assign(I18N.de, {"plus_nav": "YA Plus", "chip_on": "YA Plus aktiv", "chip_off": "YA Plus holen", "online_s": "Online", "preview": "Vorschau", "p_title": "YA Plus", "p_promo_t": "Auf YA Plus upgraden", "p_promo_d": "Animierter Umhang, 50% Rabatt im Shop, Zufallsbelohnungen und mehr.", "p_buy": "YA Plus holen", "p_active_t": "YA Plus ist aktiv", "p_active_d": "Danke für die Unterstützung von YA CLIENT. Deine Vorteile sind aktiv.", "p_rerun": "Einrichtung wiederholen", "p_how": "Plus wird deinem Konto über unseren Shop oder Discord hinzugefügt.", "perk_theme": "50% Rabatt im Shop", "perk_cape": "Animierter Umhang", "perk_more": "Zufälliges Cosmetic + Monats-Bundle", "perk_setup": "Goldenes Logo, Donator-Tag, Prioritäts-Support", "wp_eyebrow": "YA CLIENT PLUS", "wp_title_a": "Willkommen bei", "wp_title_b": "Plus", "wp_sub": "Dein YA-Plus-Abo ist in diesem Launcher aktiv. Das hast du gerade freigeschaltet.", "cont": "Weiter", "skip": "Vorerst überspringen", "back_b": "Zurück", "finish": "Los geht's", "st_title": "Richte deinen Client ein", "st_sub": "Wähle, wie YA CLIENT starten soll. Du kannst alles später ändern.", "lbl_preset": "MOD-PRESET", "pre_pvp": "PvP", "pre_pvp_d": "Kampf-Mods und Hilfsmittel an, Info-HUD aus", "pre_all": "Alles", "pre_all_d": "Alle verfügbaren Mods aktiv", "pre_min": "Minimal", "pre_min_d": "Nur Leistung und Basis-HUD", "lbl_perf": "LEISTUNG", "ram_l": "Spielspeicher", "ram_d": "Empfohlen für PvP: 3 GB", "done_t": "Alles bereit", "done_s": "Dein Launcher ist rot und deine Plus-Vorteile sind im Spiel aktiv."});
Object.assign(I18N.en, {"wp_eyebrow2": "PLUS UNLOCKED", "wp_h": "You're in.", "f1t": "50% off the shop", "f1d": "Every cosmetic and bundle costs half.", "f2t": "Animated cape", "f2d": "Your cape moves with cloth motion in game.", "f3t": "Random rewards", "f3d": "1 cosmetic forever and 1 bundle for as long as Plus lasts."});
Object.assign(I18N.it, {"wp_eyebrow2": "PLUS SBLOCCATO", "wp_h": "Sei dentro.", "f1t": "50% di sconto", "f1d": "Ogni cosmetico e bundle costa la metà.", "f2t": "Mantello animato", "f2d": "Il tuo mantello si muove come un vero tessuto in gioco.", "f3t": "Premi casuali", "f3d": "1 cosmetico per sempre e 1 bundle finché dura Plus."});
Object.assign(I18N.es, {"wp_eyebrow2": "PLUS DESBLOQUEADO", "wp_h": "Ya estás dentro.", "f1t": "50% de descuento", "f1d": "Cada cosmético y bundle cuesta la mitad.", "f2t": "Capa animada", "f2d": "Tu capa se mueve como una tela real en el juego.", "f3t": "Premios aleatorios", "f3d": "1 cosmético para siempre y 1 bundle mientras dure Plus."});
Object.assign(I18N.de, {"wp_eyebrow2": "PLUS FREIGESCHALTET", "wp_h": "Du bist dabei.", "f1t": "50% Rabatt", "f1d": "Jedes Cosmetic und Bundle kostet die Hälfte.", "f2t": "Animierter Umhang", "f2d": "Dein Umhang bewegt sich im Spiel wie echter Stoff.", "f3t": "Zufallsbelohnungen", "f3d": "1 Cosmetic für immer und 1 Bundle, solange Plus läuft."});
Object.assign(I18N.en, { java_dl: 'Downloading Java (first launch only)...' });
Object.assign(I18N.it, { java_dl: 'Scarico Java (solo al primo avvio)...' });
Object.assign(I18N.es, { java_dl: 'Descargando Java (solo la primera vez)...' });
Object.assign(I18N.de, { java_dl: 'Lade Java (nur beim ersten Start)...' });
Object.assign(I18N.en, {"friends_nav": "Friends", "fr_title": "Friends", "fr_sub": "Add friends by their Minecraft name and see who is online.", "fr_ph": "Minecraft name", "fr_add": "Add friend", "fr_req": "Requests", "fr_on": "Online", "fr_off": "Offline", "fr_sent": "Sent", "fr_accept": "Accept", "fr_decline": "Decline", "fr_remove": "Remove", "fr_cancel": "Cancel", "fr_wants": "Wants to be your friend", "fr_pending": "Request sent", "fr_game": "Playing now", "fr_launcher": "In the launcher", "fr_offl": "Offline", "fr_empty": "No friends yet. Add someone with their Minecraft name above.", "fr_premium": "Friends need a Microsoft account. Sign in with Microsoft to use them.", "fr_login": "Could not sign in to the friends service. Try signing in again.", "fr_offline": "Friends are not available right now.", "fr_loading": "Loading...", "fr_sending": "Sending...", "fr_r_sent": "Request sent", "fr_r_accepted": "You are now friends", "fr_r_not_found": "Player not found. They must have used YA CLIENT.", "fr_r_self": "You can't add yourself.", "fr_r_already": "Already friends or request pending.", "fr_r_offline": "Could not reach the server.", "fr_n_online": "online"});
Object.assign(I18N.it, {"friends_nav": "Amici", "fr_title": "Amici", "fr_sub": "Aggiungi amici col loro nome Minecraft e guarda chi è online.", "fr_ph": "Nome Minecraft", "fr_add": "Aggiungi amico", "fr_req": "Richieste", "fr_on": "Online", "fr_off": "Offline", "fr_sent": "Inviate", "fr_accept": "Accetta", "fr_decline": "Rifiuta", "fr_remove": "Rimuovi", "fr_cancel": "Annulla", "fr_wants": "Vuole essere tuo amico", "fr_pending": "Richiesta inviata", "fr_game": "In gioco", "fr_launcher": "Nel launcher", "fr_offl": "Offline", "fr_empty": "Ancora nessun amico. Aggiungi qualcuno col suo nome Minecraft qui sopra.", "fr_premium": "Gli amici richiedono un account Microsoft. Accedi con Microsoft per usarli.", "fr_login": "Impossibile accedere al servizio amici. Prova ad accedere di nuovo.", "fr_offline": "Gli amici non sono disponibili al momento.", "fr_loading": "Caricamento...", "fr_sending": "Invio...", "fr_r_sent": "Richiesta inviata", "fr_r_accepted": "Ora siete amici", "fr_r_not_found": "Giocatore non trovato. Deve aver usato YA CLIENT.", "fr_r_self": "Non puoi aggiungere te stesso.", "fr_r_already": "Siete già amici o c'è una richiesta in sospeso.", "fr_r_offline": "Impossibile contattare il server.", "fr_n_online": "online"});
Object.assign(I18N.es, {"friends_nav": "Amigos", "fr_title": "Amigos", "fr_sub": "Añade amigos por su nombre de Minecraft y mira quién está en línea.", "fr_ph": "Nombre de Minecraft", "fr_add": "Añadir amigo", "fr_req": "Solicitudes", "fr_on": "En línea", "fr_off": "Desconectados", "fr_sent": "Enviadas", "fr_accept": "Aceptar", "fr_decline": "Rechazar", "fr_remove": "Eliminar", "fr_cancel": "Cancelar", "fr_wants": "Quiere ser tu amigo", "fr_pending": "Solicitud enviada", "fr_game": "Jugando", "fr_launcher": "En el launcher", "fr_offl": "Desconectado", "fr_empty": "Aún no tienes amigos. Añade a alguien con su nombre de Minecraft arriba.", "fr_premium": "Los amigos necesitan una cuenta Microsoft. Inicia sesión con Microsoft.", "fr_login": "No se pudo iniciar sesión en el servicio de amigos. Inténtalo de nuevo.", "fr_offline": "Los amigos no están disponibles ahora.", "fr_loading": "Cargando...", "fr_sending": "Enviando...", "fr_r_sent": "Solicitud enviada", "fr_r_accepted": "Ya sois amigos", "fr_r_not_found": "Jugador no encontrado. Debe haber usado YA CLIENT.", "fr_r_self": "No puedes añadirte a ti mismo.", "fr_r_already": "Ya sois amigos o hay una solicitud pendiente.", "fr_r_offline": "No se pudo contactar con el servidor.", "fr_n_online": "en línea"});
Object.assign(I18N.de, {"friends_nav": "Freunde", "fr_title": "Freunde", "fr_sub": "Füge Freunde per Minecraft-Namen hinzu und sieh, wer online ist.", "fr_ph": "Minecraft-Name", "fr_add": "Freund hinzufügen", "fr_req": "Anfragen", "fr_on": "Online", "fr_off": "Offline", "fr_sent": "Gesendet", "fr_accept": "Annehmen", "fr_decline": "Ablehnen", "fr_remove": "Entfernen", "fr_cancel": "Abbrechen", "fr_wants": "Möchte dein Freund sein", "fr_pending": "Anfrage gesendet", "fr_game": "Spielt gerade", "fr_launcher": "Im Launcher", "fr_offl": "Offline", "fr_empty": "Noch keine Freunde. Füge oben jemanden per Minecraft-Namen hinzu.", "fr_premium": "Freunde brauchen ein Microsoft-Konto. Melde dich mit Microsoft an.", "fr_login": "Anmeldung beim Freunde-Dienst fehlgeschlagen. Melde dich erneut an.", "fr_offline": "Freunde sind gerade nicht verfügbar.", "fr_loading": "Laden...", "fr_sending": "Sende...", "fr_r_sent": "Anfrage gesendet", "fr_r_accepted": "Ihr seid jetzt Freunde", "fr_r_not_found": "Spieler nicht gefunden. Er muss YA CLIENT benutzt haben.", "fr_r_self": "Du kannst dich nicht selbst hinzufügen.", "fr_r_already": "Bereits befreundet oder Anfrage offen.", "fr_r_offline": "Server nicht erreichbar.", "fr_n_online": "online"});
Object.assign(I18N.en, {"fr_r_not_configured": "The friends server is not set up yet. It will work once the YA CLIENT server is online.", "fr_r_login": "Could not sign in to the friends service. Sign in with Microsoft again.", "fr_r_premium": "Friends need a Microsoft account."});
Object.assign(I18N.it, {"fr_r_not_configured": "Il server degli amici non è ancora attivo. Funzionerà appena il server di YA CLIENT sarà online.", "fr_r_login": "Impossibile accedere al servizio amici. Accedi di nuovo con Microsoft.", "fr_r_premium": "Gli amici richiedono un account Microsoft."});
Object.assign(I18N.es, {"fr_r_not_configured": "El servidor de amigos aún no está activo. Funcionará cuando el servidor de YA CLIENT esté en línea.", "fr_r_login": "No se pudo iniciar sesión en el servicio de amigos. Inicia sesión con Microsoft otra vez.", "fr_r_premium": "Los amigos necesitan una cuenta Microsoft."});
Object.assign(I18N.de, {"fr_r_not_configured": "Der Freunde-Server ist noch nicht aktiv. Er funktioniert, sobald der YA-CLIENT-Server online ist.", "fr_r_login": "Anmeldung beim Freunde-Dienst fehlgeschlagen. Melde dich erneut mit Microsoft an.", "fr_r_premium": "Freunde brauchen ein Microsoft-Konto."});
const LANGS = { en: 'English', it: 'Italiano', es: 'Español', de: 'Deutsch' };
const SLOT = { ya_cape: 'cape', ya_wings: 'wings', ya_bandana: 'bandana', ya_pet: 'pet', mono_cape: 'cape', mono_wings: 'wings', mono_bandana: 'bandana', mono_pet: 'pet', qlux_cape: 'cape', qlux_wings: 'wings', qlux_bandana: 'bandana', qlux_pet: 'pet', sakura_cape: 'cape', sakura_wings: 'wings', sakura_bandana: 'bandana', sakura_pet: 'pet', shinobi_cape: 'cape', shinobi_wings: 'wings', shinobi_bandana: 'bandana', shinobi_pet: 'pet', kitsune_cape: 'cape', kitsune_wings: 'wings', kitsune_bandana: 'bandana', kitsune_pet: 'pet', santa_cape: 'cape', santa_wings: 'wings', santa_bandana: 'bandana', santa_pet: 'pet', frost_cape: 'cape', frost_wings: 'wings', frost_bandana: 'bandana', frost_pet: 'pet', clown_cape: 'cape', clown_wings: 'wings', clown_bandana: 'bandana', clown_pet: 'pet', luck_cape: 'cape', luck_wings: 'wings', luck_bandana: 'bandana', luck_pet: 'pet', sky_cape: 'cape', ocean_cape: 'cape', sunset_cape: 'cape', forest_cape: 'cape', space_cape: 'cape', dragon_wings: 'wings', butterfly_wings: 'wings', azure_wings: 'wings', venom_wings: 'wings', dragonfly_wings: 'wings', ninja_bandana: 'bandana', crown_bandana: 'bandana', halo_bandana: 'bandana', cat_bandana: 'bandana', rainbow_bandana: 'bandana', drake_pet: 'pet', bunny_pet: 'pet', penguin_pet: 'pet', panda_pet: 'pet', axolotl_pet: 'pet', vampire_wings: 'wings', vamp_crimson_wings: 'wings', vamp_scarlet_wings: 'wings', vamp_orange_wings: 'wings', vamp_amber_wings: 'wings', vamp_gold_wings: 'wings', vamp_lime_wings: 'wings', vamp_green_wings: 'wings', vamp_emerald_wings: 'wings', vamp_teal_wings: 'wings', vamp_cyan_wings: 'wings', vamp_sky_wings: 'wings', vamp_blue_wings: 'wings', vamp_indigo_wings: 'wings', vamp_violet_wings: 'wings', vamp_purple_wings: 'wings', vamp_magenta_wings: 'wings', vamp_pink_wings: 'wings', vamp_rose_wings: 'wings', vamp_white_wings: 'wings', vamp_silver_wings: 'wings', vampire_cape: 'cape', vamp_crimson_cape: 'cape', vamp_scarlet_cape: 'cape', vamp_orange_cape: 'cape', vamp_amber_cape: 'cape', vamp_gold_cape: 'cape', vamp_lime_cape: 'cape', vamp_green_cape: 'cape', vamp_emerald_cape: 'cape', vamp_teal_cape: 'cape', vamp_cyan_cape: 'cape', vamp_sky_cape: 'cape', vamp_blue_cape: 'cape', vamp_indigo_cape: 'cape', vamp_violet_cape: 'cape', vamp_purple_cape: 'cape', vamp_magenta_cape: 'cape', vamp_pink_cape: 'cape', vamp_rose_cape: 'cape', vamp_white_cape: 'cape', vamp_silver_cape: 'cape', storm_cape: 'cape', puppy_pet: 'pet' };
const NAMES = { ya_cape: 'YA Cape', ya_wings: 'Ember Wings', ya_bandana: 'YA Bandana', ya_pet: 'Ember (pet)', mono_cape: 'Infinity Cape', mono_wings: 'Shadow Dragon Wings', mono_bandana: 'Monochrome Bandana', mono_pet: 'Yin Yang (pet)' , qlux_cape: 'Qlux Cape', qlux_wings: 'Qlux Angel Wings', qlux_bandana: 'Qlux Band', qlux_pet: 'Lumi (pet)', sakura_cape: 'Sakura Cape', sakura_wings: 'Petal Wings', sakura_bandana: 'Blossom Band', sakura_pet: 'Mochi (pet)', shinobi_cape: 'Moonlit Cloak', shinobi_wings: 'Blade Wings', shinobi_bandana: 'Shinobi Headband', shinobi_pet: 'Kage (pet)', kitsune_cape: 'Kitsune Cape', kitsune_wings: 'Flame Wings', kitsune_bandana: 'Shrine Ribbon', kitsune_pet: 'Inari (pet)', santa_cape: 'Santa Cape', santa_wings: 'Candy Wings', santa_bandana: 'Santa Band', santa_pet: 'Rudy (pet)', frost_cape: 'Snowflake Cape', frost_wings: 'Ice Wings', frost_bandana: 'Frost Band', frost_pet: 'Frosty (pet)', clown_cape: 'Clown Cape', clown_wings: 'Balloon Wings', clown_bandana: 'Clown Band', clown_pet: 'Bozo (pet)', luck_cape: 'Clover Cape', luck_wings: 'Clover Wings', luck_bandana: 'Lucky Band', luck_pet: 'Lucky (pet)', sky_cape: 'Sky Cape', ocean_cape: 'Ocean Cape', sunset_cape: 'Sunset Cape', forest_cape: 'Forest Cape', space_cape: 'Space Cape', dragon_wings: 'Dragon Wings', butterfly_wings: 'Butterfly Wings', azure_wings: 'Azure Wings', venom_wings: 'Valkyrie Wings', dragonfly_wings: 'Dragonfly Wings', ninja_bandana: 'Ninja Headband', crown_bandana: 'Golden Crown', halo_bandana: 'Angel Halo', cat_bandana: 'Cat Ears', rainbow_bandana: 'Rainbow Band', drake_pet: 'Drake (pet)', bunny_pet: 'Bunny (pet)', penguin_pet: 'Penguin (pet)', panda_pet: 'Panda (pet)', axolotl_pet: 'Axolotl (pet)', vampire_wings: "Vampire Wings (Black)", vamp_crimson_wings: "Vampire Wings (Crimson)", vamp_scarlet_wings: "Vampire Wings (Scarlet)", vamp_orange_wings: "Vampire Wings (Orange)", vamp_amber_wings: "Vampire Wings (Amber)", vamp_gold_wings: "Vampire Wings (Gold)", vamp_lime_wings: "Vampire Wings (Lime)", vamp_green_wings: "Vampire Wings (Green)", vamp_emerald_wings: "Vampire Wings (Emerald)", vamp_teal_wings: "Vampire Wings (Teal)", vamp_cyan_wings: "Vampire Wings (Cyan)", vamp_sky_wings: "Vampire Wings (Sky)", vamp_blue_wings: "Vampire Wings (Blue)", vamp_indigo_wings: "Vampire Wings (Indigo)", vamp_violet_wings: "Vampire Wings (Violet)", vamp_purple_wings: "Vampire Wings (Purple)", vamp_magenta_wings: "Vampire Wings (Magenta)", vamp_pink_wings: "Vampire Wings (Pink)", vamp_rose_wings: "Vampire Wings (Rose)", vamp_white_wings: "Vampire Wings (White)", vamp_silver_wings: "Vampire Wings (Silver)", vampire_cape: "Geometric Bat Cape", vamp_crimson_cape: "Geometric Heart Cape", vamp_scarlet_cape: "Geometric Flame Cape", vamp_orange_cape: "Geometric Sun Cape", vamp_amber_cape: "Geometric Bolt Cape", vamp_gold_cape: "Geometric Crown Cape", vamp_lime_cape: "Geometric Clover Cape", vamp_green_cape: "Geometric Pine Cape", vamp_emerald_cape: "Geometric Gem Cape", vamp_teal_cape: "Geometric Waves Cape", vamp_cyan_cape: "Geometric Snowflake Cape", vamp_sky_cape: "Geometric Cloud Cape", vamp_blue_cape: "Geometric Compass Cape", vamp_indigo_cape: "Geometric Moon Cape", vamp_violet_cape: "Geometric Eye Cape", vamp_purple_cape: "Geometric Hexagram Cape", vamp_magenta_cape: "Geometric Butterfly Cape", vamp_pink_cape: "Geometric Flower Cape", vamp_rose_cape: "Geometric Paw Cape", vamp_white_cape: "Geometric Angel Cape", vamp_silver_cape: "Geometric Sword Cape", storm_cape: "Storm Cape", puppy_pet: "Puppy (pet)" };
const ICON = {
  home: '<path d="M3 11l9-8 9 8"/><path d="M5 10v10h14V10"/>',
  versions: '<path d="M12 3l9 5-9 5-9-5z"/><path d="M3 13l9 5 9-5"/><path d="M3 17.5l9 5 9-5"/>',
  mods: '<rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/>',
  servers: '<rect x="3" y="4" width="18" height="7" rx="2"/><rect x="3" y="13" width="18" height="7" rx="2"/><path d="M7 7.5h.01M7 16.5h.01"/>',
  cosmetics: '<path d="M8 3L3.5 5.5 2 10l3.5 1.5L7 10v11h10V10l1.5 1.5L22 10l-1.5-4.5L16 3"/><path d="M8 3c.5 2 2 3.5 4 3.5S15.5 5 16 3"/>',
  console: '<rect x="3" y="4" width="18" height="16" rx="2.5"/><path d="M7 9l3 3-3 3M12 16h5"/>',
  settings: '<circle cx="12" cy="12" r="3"/><path d="M12 2v3M12 19v3M2 12h3M19 12h3M4.9 4.9l2.1 2.1M17 17l2.1 2.1M4.9 19.1L7 17M17 7l2.1-2.1"/>',
  lock: '<rect x="5" y="11" width="14" height="10" rx="2.5"/><path d="M8 11V8a4 4 0 0 1 8 0v3"/>',
  plus: '<path d="M12 3l2.7 5.6 6.1.9-4.4 4.3 1 6.1L12 17l-5.4 2.9 1-6.1L3.2 9.5l6.1-.9z"/>',
  check: '<path d="M5 12.5l4.5 4.5L19 7.5"/>',
  chev: '<path d="M6 9l6 6 6-6"/>',
  friends: '<circle cx="9" cy="8" r="3.6"/><path d="M2.5 20c0-4 3-6 6.5-6s6.5 2 6.5 6"/><path d="M16 4.6a3.6 3.6 0 0 1 1.4 6.4M18 14.2c2.4.6 3.8 2.5 3.8 5.6"/>',
  user: '<circle cx="12" cy="8" r="4"/><path d="M4 21c1-4 4-6 8-6s7 2 8 6"/>'
};
ICON.modules = ICON.mods;
ICON.search = '<circle cx="11" cy="11" r="6.5"/><path d="M16 16l4.5 4.5"/>';
ICON.clock = '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>';
ICON.play = '<path d="M7 4.5v15l12-7.5z"/>';
ICON.trash = '<path d="M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13"/>';
ICON.folder = '<path d="M3 6.5A2.5 2.5 0 0 1 5.5 4H10l2 2.5h6.5A2.5 2.5 0 0 1 21 9v8.5a2.5 2.5 0 0 1-2.5 2.5h-13A2.5 2.5 0 0 1 3 17.5z"/>';
const ic = (n) => `<svg viewBox="0 0 24 24">${ICON[n]}</svg>`;

let S = { lang: 'en', account: null, ram: 2048, javaPath: 'java', minimizeOnLaunch: false };
let C = {}; let MODSTATE = {}; let MODS = []; let VERS = { versions: [], latest: {}, online: true }; let vtab = 'release'; let vq = ''; let page = 'home'; let busy = false; let cosTab = 'all';
let srvQ = ''; let srvTab = 'all'; let srvAdd = false; let srvDir = false; let srvSel = '';
let mq = ''; let mcat = 'all'; let profSel = ''; let PING = {};
let logBuf = []; let status = { text: '', pct: 0, show: false };
const t = (k) => (I18N[S.lang] && I18N[S.lang][k]) || I18N.en[k] || k;

Object.assign(I18N.es, { bundles: 'Packs', get_it: 'Ver en la web', owned_free: 'Gratis' });
Object.assign(I18N.de, { bundles: 'Bundles', get_it: 'Auf der Website ansehen', owned_free: 'Gratis' });
Object.assign(I18N.en, {"home_sub": "Everything you need to play: your modules, servers and friends in one place.", "version": "Version", "profile": "Profile", "prof_cur": "Current setup", "st_play": "Time played", "st_last": "Last session", "st_mods": "Active modules", "quick": "Quick join", "all": "All", "noServers2": "No servers yet. Add one in Servers.", "offline_s": "Offline", "now": "now", "g_night": "Good night", "g_morning": "Good morning", "g_afternoon": "Good afternoon", "g_evening": "Good evening", "srv_sub": "Add your favourite servers, check the ping and join with one click.", "srv_name": "Name (optional)", "srv_add": "Add server", "srv_bad": "Invalid address. Use host or host:port.", "srv_added": "Server added", "srv_removed": "Server removed", "mod_search": "Search modules...", "mod_on": "Enabled", "mod_none": "Nothing found.", "mod_na": "Not available for this version", "cat_pvp": "PvP", "cat_info": "Info", "cat_utility": "Utility", "pre_pvp": "PvP", "pre_all": "Everything", "pre_min": "Minimal", "prof_save": "Save profile", "prof_name": "Profile name", "prof_saved": "Profile saved", "prof_deleted": "Profile deleted", "prof_applied": "Profile applied", "prof_del": "Delete", "pal_ph": "Search pages, modules, servers, actions...", "pal_pages": "Pages", "pal_act": "Actions", "open_store": "Open store"});
Object.assign(I18N.it, {"home_sub": "Tutto quello che ti serve per giocare: moduli, server e amici in un posto solo.", "version": "Versione", "profile": "Profilo", "prof_cur": "Setup attuale", "st_play": "Tempo di gioco", "st_last": "Ultima sessione", "st_mods": "Moduli attivi", "quick": "Accesso rapido", "all": "Tutti", "noServers2": "Nessun server. Aggiungine uno in Server.", "offline_s": "Offline", "now": "ora", "g_night": "Buonanotte", "g_morning": "Buongiorno", "g_afternoon": "Buon pomeriggio", "g_evening": "Buonasera", "srv_sub": "Aggiungi i tuoi server preferiti, controlla il ping ed entra con un click.", "srv_name": "Nome (facoltativo)", "srv_add": "Aggiungi server", "srv_bad": "Indirizzo non valido. Usa host o host:porta.", "srv_added": "Server aggiunto", "srv_removed": "Server rimosso", "mod_search": "Cerca moduli...", "mod_on": "Attivi", "mod_none": "Nessun risultato.", "mod_na": "Non disponibile per questa versione", "cat_pvp": "PvP", "cat_info": "Info", "cat_utility": "Utilità", "pre_pvp": "PvP", "pre_all": "Tutto", "pre_min": "Minimo", "prof_save": "Salva profilo", "prof_name": "Nome profilo", "prof_saved": "Profilo salvato", "prof_deleted": "Profilo eliminato", "prof_applied": "Profilo applicato", "prof_del": "Elimina", "pal_ph": "Cerca pagine, moduli, server, azioni...", "pal_pages": "Pagine", "pal_act": "Azioni", "open_store": "Apri lo store"});
Object.assign(I18N.es, {"home_sub": "Todo lo que necesitas para jugar: módulos, servidores y amigos en un solo lugar.", "version": "Versión", "profile": "Perfil", "prof_cur": "Configuración actual", "st_play": "Tiempo jugado", "st_last": "Última sesión", "st_mods": "Módulos activos", "quick": "Acceso rápido", "all": "Todos", "noServers2": "Aún no hay servidores. Añade uno en Servidores.", "offline_s": "Sin conexión", "now": "ahora", "g_night": "Buenas noches", "g_morning": "Buenos días", "g_afternoon": "Buenas tardes", "g_evening": "Buenas noches", "srv_sub": "Añade tus servidores favoritos, mira el ping y entra con un clic.", "srv_name": "Nombre (opcional)", "srv_add": "Añadir servidor", "srv_bad": "Dirección no válida. Usa host o host:puerto.", "srv_added": "Servidor añadido", "srv_removed": "Servidor eliminado", "mod_search": "Buscar módulos...", "mod_on": "Activos", "mod_none": "Sin resultados.", "mod_na": "No disponible para esta versión", "cat_pvp": "PvP", "cat_info": "Info", "cat_utility": "Utilidad", "pre_pvp": "PvP", "pre_all": "Todo", "pre_min": "Mínimo", "prof_save": "Guardar perfil", "prof_name": "Nombre del perfil", "prof_saved": "Perfil guardado", "prof_deleted": "Perfil eliminado", "prof_applied": "Perfil aplicado", "prof_del": "Eliminar", "pal_ph": "Buscar páginas, módulos, servidores, acciones...", "pal_pages": "Páginas", "pal_act": "Acciones", "open_store": "Abrir tienda"});
Object.assign(I18N.de, {"home_sub": "Alles zum Spielen an einem Ort: Module, Server und Freunde.", "version": "Version", "profile": "Profil", "prof_cur": "Aktuelles Setup", "st_play": "Spielzeit", "st_last": "Letzte Sitzung", "st_mods": "Aktive Module", "quick": "Schnellzugriff", "all": "Alle", "noServers2": "Noch keine Server. Füge einen unter Server hinzu.", "offline_s": "Offline", "now": "jetzt", "g_night": "Gute Nacht", "g_morning": "Guten Morgen", "g_afternoon": "Guten Tag", "g_evening": "Guten Abend", "srv_sub": "Füge deine Lieblingsserver hinzu, prüfe den Ping und tritt mit einem Klick bei.", "srv_name": "Name (optional)", "srv_add": "Server hinzufügen", "srv_bad": "Ungültige Adresse. Nutze Host oder Host:Port.", "srv_added": "Server hinzugefügt", "srv_removed": "Server entfernt", "mod_search": "Module suchen...", "mod_on": "Aktiv", "mod_none": "Nichts gefunden.", "mod_na": "Für diese Version nicht verfügbar", "cat_pvp": "PvP", "cat_info": "Info", "cat_utility": "Werkzeug", "pre_pvp": "PvP", "pre_all": "Alles", "pre_min": "Minimal", "prof_save": "Profil speichern", "prof_name": "Profilname", "prof_saved": "Profil gespeichert", "prof_deleted": "Profil gelöscht", "prof_applied": "Profil angewendet", "prof_del": "Löschen", "pal_ph": "Seiten, Module, Server, Aktionen suchen...", "pal_pages": "Seiten", "pal_act": "Aktionen", "open_store": "Shop öffnen"});
Object.assign(I18N.en, {"sv_search": "Search servers", "sv_direct": "Direct connect", "sv_connect": "Connect", "sv_recent": "Recent", "sv_none": "No saved servers", "sv_none_r": "No recent servers", "sv_none_d": "Add a server, or use direct connect.", "sv_choose": "Choose a server", "sv_saved": "saved"});
Object.assign(I18N.it, {"sv_search": "Cerca server", "sv_direct": "Connessione diretta", "sv_connect": "Connetti", "sv_recent": "Recenti", "sv_none": "Nessun server salvato", "sv_none_r": "Nessun server recente", "sv_none_d": "Aggiungi un server o usa la connessione diretta.", "sv_choose": "Scegli un server", "sv_saved": "salvati"});
Object.assign(I18N.es, {"sv_search": "Buscar servidores", "sv_direct": "Conexión directa", "sv_connect": "Conectar", "sv_recent": "Recientes", "sv_none": "Sin servidores guardados", "sv_none_r": "Sin servidores recientes", "sv_none_d": "Añade un servidor o usa la conexión directa.", "sv_choose": "Elige un servidor", "sv_saved": "guardados"});
Object.assign(I18N.de, {"sv_search": "Server suchen", "sv_direct": "Direktverbindung", "sv_connect": "Verbinden", "sv_recent": "Zuletzt", "sv_none": "Keine gespeicherten Server", "sv_none_r": "Keine letzten Server", "sv_none_d": "Füge einen Server hinzu oder nutze die Direktverbindung.", "sv_choose": "Server wählen", "sv_saved": "gespeichert"});
Object.assign(I18N.en, {"as_name": "Server name", "as_addr": "Server address", "as_save": "Save", "as_hint": "Type an address to see a live preview."});
Object.assign(I18N.it, {"as_name": "Nome del server", "as_addr": "Indirizzo del server", "as_save": "Salva", "as_hint": "Scrivi un indirizzo per vedere l'anteprima."});
Object.assign(I18N.es, {"as_name": "Nombre del servidor", "as_addr": "Dirección del servidor", "as_save": "Guardar", "as_hint": "Escribe una dirección para ver la vista previa."});
Object.assign(I18N.de, {"as_name": "Servername", "as_addr": "Serveradresse", "as_save": "Speichern", "as_hint": "Gib eine Adresse ein, um die Vorschau zu sehen."});
Object.assign(I18N.en, { acc_short: 'Account' }); Object.assign(I18N.it, { acc_short: 'Account' }); Object.assign(I18N.es, { acc_short: 'Cuenta' }); Object.assign(I18N.de, { acc_short: 'Konto' });
const PAGES = ['home', 'modules', 'versions', 'servers', 'friends', 'cosmetics', 'plus', 'console', 'settings'];
const PKEY = { modules: 'mods', home: 'launchpad', versions: 'versions', servers: 'servers', friends: 'friends_nav', cosmetics: 'cosmetics', console: 'console', settings: 'settings', plus: 'plus_nav' };

function renderNav() {
  $('#nav').innerHTML = PAGES.map((p) => `<button class="nav ${p === page ? 'on' : ''} ${p === 'plus' ? 'star' : ''}" data-p="${p}">${ic(p)}<em>${esc(t(PKEY[p]))}</em>${p === 'friends' && FR.data && FR.data.incoming.length ? `<b class="bdg">${FR.data.incoming.length}</b>` : ''}</button>`).join('');
  $('#accIcon').innerHTML = ic('user');
  $('#accLbl').textContent = S.account ? t('acc_short') : t('signIn');
  $('#accPill').innerHTML = S.account ? `<img class="face" data-ini="${esc(S.account.name[0].toUpperCase())}" src="https://minotar.net/helm/${encodeURIComponent(S.account.name)}/48.png" alt="">${esc(S.account.name)}` : esc(t('signIn'));
  $('#ver').textContent = (isYa(S.version) ? 'YA CLIENT ' : '') + (S.version || '');
  document.body.classList.toggle('plus', plusOn());
}
const plusOn = () => !!(S.plus || S.plusPreview);
function go(p) { page = p; renderNav(); render(); }

function adHtml(a) {
  if (a && safeImg(a.image)) return `<div class="card ad ${a.url ? 'link' : ''}" data-url="${esc(a.url || '')}"><img src="${esc(safeImg(a.image))}" alt=""></div>`;
  return `<div class="card ad">${esc(t('advert'))}</div>`;
}
function newsHtml(list) {
  if (!list || !list.length) return '';
  return `<h2>${esc(t('news'))}</h2><div class="news">` + list.map((n, i) => {
    const im = safeImg(n.image);
    return `<div class="card nc ${n.url || n.sections ? 'link' : ''}" data-url="${esc(n.url || '')}" ${n.sections ? `data-ci="${i}"` : ''}><div class="im" ${im ? `style="background-image:url('${esc(im)}')"` : ''}></div>
      <div class="tx"><b>${esc(n.title)}</b><p>${esc(n.text)}</p><small>${esc(n.date || '')}</small></div></div>`;
  }).join('') + '</div>';
}

function showChangelog(i) {
  const n = (C.news || [])[i];
  if (!n || !n.sections) return;
  closeChangelog();
  const im = safeImg(n.image);
  const secs = n.sections.map((sec) => `<div class="cls ${esc(sec.k)}"><h4><span class="clchip ${esc(sec.k)}">${esc(sec.t)}</span></h4><div class="clgrid">` +
    sec.list.map((it) => `<div class="clit"><b>${esc(it.b)}</b><span>${esc(it.s)}</span></div>`).join('') + '</div></div>').join('');
  const d = document.createElement('div');
  d.className = 'clov'; d.id = 'clov';
  d.innerHTML = `<div class="clbox"><button class="clx" data-clx="1" aria-label="Close">&times;</button>
    ${im ? `<div class="clban" style="background-image:url('${esc(im)}')"></div>` : ''}
    <div class="clhead"><h2>Update ${esc(n.ver || '')}</h2><span class="clchip date">${esc(n.date || '')}</span></div>
    <p class="cllead">${esc((n.title || '').replace(/^Update [\d.]+ - /, ''))}</p>${secs}</div>`;
  d.addEventListener('click', (e) => { if (e.target === d) closeChangelog(); });
  document.body.appendChild(d);
}
function closeChangelog() { const o = document.getElementById('clov'); if (o) o.remove(); }
document.addEventListener('keydown', (e) => { if (e.key === 'Escape') closeChangelog(); });

function plusCard() {
  if (plusOn()) return `<div class="card pcard"><span class="tag">PLUS</span><b>${esc(t('p_active_t'))}</b><p>${esc(t('p_active_d'))}</p><button class="btn sm" data-p="plus">${esc(t('plus_nav'))}</button></div>`;
  return `<div class="card pcard"><span class="tag">PLUS</span><b>${esc(t('p_promo_t'))}</b><p>${esc(t('p_promo_d'))}</p><button class="btn sm hi" data-p="plus">${esc(t('p_buy'))}</button></div>`;
}
const BGS = 4;
const bgCss = (n) => (n > 0 ? `background:linear-gradient(180deg,rgba(0,0,0,.25),rgba(0,0,0,.6)),url('bg/bg${n}.jpg') center/cover no-repeat` : '');
const BG_ICON = '<svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="16" rx="3"/><circle cx="9" cy="10" r="1.8"/><path d="M3.5 17l5-4.5 4 3.5 3-2.5 5 4"/></svg>';
const fmtMs = (ms) => { const m = Math.floor((ms || 0) / 60000); return m >= 60 ? Math.floor(m / 60) + 'h ' + (m % 60) + 'm' : m + 'm'; };
const fmtAgo = (ts) => { if (!ts) return '-'; const d = Date.now() - ts; const m = Math.floor(d / 60000); if (m < 1) return t('now'); if (m < 60) return m + 'm'; const h = Math.floor(m / 60); if (h < 48) return h + 'h'; return Math.floor(h / 24) + 'd'; };
const modOn = (m) => (MODSTATE[m.id] !== undefined ? !!MODSTATE[m.id] : !!m.def);
const activeMods = () => MODS.filter((m) => modOk(m, S.version) && modOn(m)).length;
const allServers = () => [...(C.servers || []).map((x) => ({ ...x, _b: 1 })), ...(S.customServers || []).map((x, i) => ({ ...x, _c: i }))];
const srvKey = (x) => x.host + ':' + (x.port || 25565);
const findSrv = (k) => allServers().concat(S.recentServers || []).find((y) => srvKey(y) === k);
function pingAll() {
  allServers().concat(S.recentServers || []).forEach((x) => {
    const k = srvKey(x); if (PING[k] !== undefined) return; PING[k] = null;
    window.ya.ping(x.host, x.port || 25565).then((v) => { PING[k] = v; paintPing(); }).catch(() => { PING[k] = -1; paintPing(); });
  });
}
function msBadge(x) { const v = PING[srvKey(x)]; return v == null ? `<span class="ms wait">...</span>` : v < 0 ? `<span class="ms bad">${esc(t('offline_s'))}</span>` : `<span class="ms">${v} ms</span>`; }
function paintPing() { document.querySelectorAll('[data-pk]').forEach((el) => { const x = findSrv(el.dataset.pk); if (x) el.innerHTML = msBadge(x); }); }
function viewHome() {
  const ads = C.ads || [];
  const nm = S.account ? S.account.name : '';
  const tag = isYa(S.version) ? 'YA CLIENT' : t('vanilla');
  const av = nm ? `<div class="mc3d" id="mc3d"></div>` : `<img class="logo" src="../assets/logo.png" alt="">`;
  const profs = S.profiles || [];
  const vers = [...new Set([S.version, ...(VERS.versions || []).filter((v) => v.type === 'release').slice(0, 14).map((v) => v.id)])].filter(Boolean);
  const srv = allServers().slice(0, 4);
  const hr = new Date().getHours();
  const greet = hr < 6 ? t('g_night') : hr < 13 ? t('g_morning') : hr < 19 ? t('g_afternoon') : t('g_evening');
  const online = FR.data ? FR.data.friends.filter((f) => f.online) : [];
  return `<div class="hm"><div class="hxc">
    <div class="hx2" id="hero"><div class="bgi" id="bgi" style="${(S.bg == null ? 1 : S.bg) > 0 ? `background-image:url('bg/bg${S.bg == null ? 1 : S.bg}.jpg')` : ''}"></div>
      <div class="hl"><small>${esc(tag)}</small><h1>${esc(greet)}${nm ? ', ' + esc(nm) : ''}</h1><p>${esc(t('home_sub'))}</p></div>
      <div class="pl">${av}</div>
      <div class="ctl"><div class="row2">
        <button class="playbig" id="play" ${busy ? 'disabled' : ''}><b>${esc(busy ? t('launching') : t('launch'))}</b><span>${esc(tag)} ${esc(S.version)}</span></button>
        <div class="chipsel"><label>${esc(t('version'))}</label><select id="verSel">${vers.map((v) => `<option ${v === S.version ? 'selected' : ''}>${esc(v)}</option>`).join('')}</select></div>
        <button class="sqb" id="bgBtn" aria-label="Background" title="Background">${BG_ICON}</button></div>
        <div class="bar" id="bar" style="display:${status.show ? 'block' : 'none'}"><i id="barI" style="width:${status.pct}%"></i></div><div class="stat" id="stat">${esc(status.text)}</div></div></div>
    <div class="stts">
      <div class="card stt"><span class="ico">${ic('clock')}</span><div><b>${esc(fmtMs(S.playMs))}</b><small>${esc(t('st_play'))}</small></div></div>
      <div class="card stt"><span class="ico">${ic('play')}</span><div><b>${esc(fmtAgo(S.lastPlayed))}</b><small>${esc(t('st_last'))}</small></div></div>
      <div class="card stt lnk" data-p="modules"><span class="ico">${ic('mods')}</span><div><b>${activeMods()}</b><small>${esc(t('st_mods'))}</small></div></div></div>
    ${C.news && C.news.length ? newsHtml(C.news).replace('class="news"', 'class="nrow"') : ''}</div>
    <div class="side">
      <div class="card qs"><h3>${esc(t('quick'))}<a data-p="servers">${esc(t('all'))}</a></h3>${srv.length ? srv.map((x) => `<div class="qrow" data-qs="${esc(srvKey(x))}"><div class="ic2" ${safeImg(x.image) ? `style="background-image:url('${esc(safeImg(x.image))}')"` : ''}>${safeImg(x.image) ? '' : esc((x.name || '?')[0].toUpperCase())}</div><div><b>${esc(x.name)}</b><small>${esc(x.host)}</small></div><span data-pk="${esc(srvKey(x))}">${msBadge(x)}</span></div>`).join('') : `<div class="qe">${esc(t('noServers2'))}</div>`}</div>
      ${online.length ? `<div class="card qs"><h3>${esc(t('fr_title'))}<a data-p="friends">${esc(t('all'))}</a></h3>${online.slice(0, 5).map((f) => `<div class="qrow"><div class="ic2" style="background-image:url('https://minotar.net/helm/${encodeURIComponent(f.name)}/48.png')"></div><div><b>${esc(f.name)}</b><small>${esc(t('fr_n_online'))}</small></div></div>`).join('')}</div>` : ''}
      ${plusCard()}${adHtml(ads[0])}</div></div>`;
}
function viewPlus() {
  const perks = ['perk_theme', 'perk_cape', 'perk_more', 'perk_setup'].map((k) => `<span class="perk">${ic('check')}${esc(t(k))}</span>`).join('');
  const on = plusOn();
  return `<h1>${esc(t('p_title'))}</h1><p class="sub">${esc(on ? t('p_active_d') : t('p_promo_d'))}</p>
    <div class="card hero3" style="height:300px;justify-content:center;padding:0"><div style="position:relative;z-index:2;text-align:center;padding:0 24px">
      <span class="tag">${on ? 'ACTIVE' : 'PLUS'}</span><h1 style="font-size:40px;letter-spacing:-1px;margin:6px 0">${esc(on ? t('p_active_t') : t('p_promo_t'))}</h1>
      <div class="perks" style="justify-content:center">${perks}</div>
      ${on ? `<button class="btn hi" id="rerun">${esc(t('p_rerun'))}</button>` : `<button class="btn hi" id="buyPlus">${esc(t('p_buy'))}</button><p class="note" style="margin:12px 0 0">${esc(t('p_how'))}</p>`}
    </div></div>`;
}
function cmpV(a, b) {
  const pa = String(a).split('.').map((n) => parseInt(n, 10) || 0), pb = String(b).split('.').map((n) => parseInt(n, 10) || 0);
  for (let i = 0; i < 3; i++) { const d = (pa[i] || 0) - (pb[i] || 0); if (d) return d; }
  return 0;
}
function specOk(spec, v) {
  spec = String(spec);
  if (spec === '*') return true;
  if (spec.indexOf('>=') === 0) return /^\d+\.\d+/.test(v) && cmpV(v, spec.slice(2)) >= 0;
  if (/\.x$/.test(spec)) return v === spec.slice(0, -2) || v.indexOf(spec.slice(0, -1)) === 0;
  return v === spec;
}
const modOk = (m, v) => !m.soon && (m.versions || []).some((sp) => specOk(sp, v));
const isYa = (v) => v === (S.ya || '1.8.9') || (S.yaFabric || []).includes(v);

let ART = {}; let vline = null;
const lineOf = (id) => { const m = /^(\d+\.\d+)/.exec(id); return m ? m[1] : id; };
const SHOWN = ['26', '1.21', '1.16', '1.8', '1.7'];
const groupOf = (id) => { const m = /^(\d+)\.(\d+)/.exec(id); if (!m) return ''; return +m[1] >= 26 ? '26' : m[1] + '.' + m[2]; };
function vLines() {
  const map = new Map();
  VERS.versions.filter((v) => v.type === 'release').forEach((v) => { const l = groupOf(v.id); if (SHOWN.indexOf(l) < 0) return; if (!map.has(l)) map.set(l, []); map.get(l).push(v); });
  return SHOWN.filter((k) => map.has(k) && map.get(k).some((v) => isYa(v.id))).map((k) => ({ key: k, title: k === '26' ? 'Minecraft 26.x' : 'Minecraft ' + k, vs: map.get(k) }));
}
const HUES = [212, 150, 28, 280, 350, 190, 60, 320];
function artStyle(l, i) {
  const a = ART[l.key] || ART[lineOf(l.vs[0].id)]; const h = HUES[(i * 3 + 1) % HUES.length];
  const g = `linear-gradient(135deg,hsl(${h} 40% 22%),hsl(${(h + 40) % 360} 45% 10%))`;
  return `background:url('art/${esc(l.key)}.jpg') center/cover,${a ? `url('${esc(a.img)}') center/cover,` : ''}${g}`;
}
function curLine() {
  const ls = vLines();
  return ls.find((l) => l.key === vline) || ls.find((l) => l.vs.some((v) => v.id === S.version)) || ls[0];
}
function loaderBtns() {
  const ok = fabricSup(S.version) && !isYa18(S.version);
  const cur = S.loader || 'fabric';
  if (isYa18(S.version)) return `<div class="note">${esc(t('vanillaNote').replace(/.*/, 'YA CLIENT (Weave)'))}</div>`;
  const opts = [['vanilla', 'Vanilla'], ['fabric', 'Fabric'], ['sodium', 'Fabric + Sodium']];
  return opts.map((o) => `<button class="ldb ${cur === o[0] ? 'on' : ''} ${ok || o[0] === 'vanilla' ? '' : 'off'}" data-ld="${o[0]}" ${ok || o[0] === 'vanilla' ? '' : 'disabled'}>${o[1]}</button>`).join('');
}
const fabricSup = (v) => /^\d+\.\d+(\.\d+)?$/.test(v) && (() => { const p = v.split('.').map(Number); return p[0] > 1 || p[1] > 16 || (p[1] === 16 && (p[2] || 0) >= 0); })() && !/^1\.16(\.[0-4])?$/.test(v);
const isYa18 = (v) => v === (S.ya || '1.8.9');
function vPanel() {
  const l = curLine(); if (!l) return '';
  const i = Math.max(0, vLines().findIndex((x) => x.key === l.key)); const a = ART[l.key] || ART[lineOf(l.vs[0].id)];
  const g = modsGrid();
  const opts = l.vs.map((v) => `<option value="${esc(v.id)}" ${v.id === S.version ? 'selected' : ''}>${isYa(v.id) ? '★ ' : ''}${esc(v.id)}${isYa(v.id) ? '  —  YA CLIENT' : ''}</option>`).join('');
  const yaList = l.vs.filter((v) => isYa(v.id)).map((v) => v.id);
  const yaHint = yaList.length ? `<div class="note">★ YA CLIENT: ${esc(yaList.join(', '))}</div>` : '';
  return `<div class="vart"><u style="${artStyle(l, i)}"></u><i></i></div>
    <div class="vbody"><h2>${esc(l.title)}</h2>
    <p class="vdesc">${esc(a && a.text ? a.text : '')}</p>
    <label class="vlab">${esc(t('version') || 'Version')}</label>
    <select id="vsel" class="vsel">${opts}</select>${yaHint}
    <label class="vlab">Loaders</label><div class="vld" id="vld">${loaderBtns()}</div>
    <button class="launch2 vgo" id="play2" ${busy ? 'disabled' : ''}><b>${esc(busy ? t('launching') : t('launch'))}</b><span>${esc(S.version)}</span></button></div>`;
}
function vCards() {
  return vLines().map((l, i) => {
    const on = curLine().key === l.key;
    return `<button class="vcard ${on ? 'on' : ''}" data-vl="${esc(l.key)}" ><u style="${artStyle(l, i)}"></u><i></i><b>${esc(l.title)}</b></button>`;
  }).join('');
}
function modsGrid() {
  const v = S.version;
  const ok = MODS.filter((m) => modOk(m, v)).length;
  return { ok, html: '' };
}
function viewVersions() {
  const ls0 = vLines();
  if (ls0.length && !ls0.some((l) => l.vs.some((v) => v.id === S.version))) { S.version = ls0[0].vs[0].id; window.ya.saveSettings({ version: S.version }); }
  return `<div class="vp2"><div class="vgrid" id="vgrid">${vCards()}</div><div class="card vpanel" id="vpanel">${vPanel()}</div></div>`;
}
function svRows() {
  const q = srvQ.trim().toLowerCase();
  const base = srvTab === 'recent' ? (S.recentServers || []) : allServers();
  const list = base.filter((x) => !q || (x.name + ' ' + x.host).toLowerCase().includes(q));
  if (!list.length) return `<div class="svempty"><b>${esc(t(srvTab === 'recent' ? 'sv_none_r' : 'sv_none'))}</b><small>${esc(t('sv_none_d'))}</small>${srvTab === 'recent' ? '' : `<button class="btn hi" id="svAddT2">+ ${esc(t('srv_add'))}</button>`}</div>`;
  return list.map((x) => {
    const im = safeImg(x.image);
    return `<div class="srow ${srvKey(x) === srvSel ? 'sel' : ''}" data-ss="${esc(srvKey(x))}"><div class="im" ${im ? `style="background-image:url('${esc(im)}')"` : ''}>${im ? '' : esc((x.name || '?')[0].toUpperCase())}</div>
      <div class="tx"><b>${esc(x.name)}</b><small>${esc(x.description || srvKey(x))}</small></div><span data-pk="${esc(srvKey(x))}">${msBadge(x)}</span>
      <button class="btn sm hi" data-sp="${esc(srvKey(x))}" ${busy ? 'disabled' : ''}>${esc(t('play'))}</button>
      ${x._c != null ? `<button class="btn sm ghostb" data-sdel="${x._c}" aria-label="Remove">${ic('trash').replace('<svg', '<svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8"')}</button>` : ''}</div>`;
  }).join('');
}
function svSide() {
  const x = srvSel ? findSrv(srvSel) : null; const im = x && safeImg(x.image);
  return `<div class="svsel"><div class="im" ${im ? `style="background-image:url('${esc(im)}')"` : ''}>${x ? (im ? '' : esc((x.name || '?')[0].toUpperCase())) : ic('servers')}</div>
    <div><b>${esc(x ? x.name : t('sv_none'))}</b><small>${esc(x ? srvKey(x) : t('sv_none_d'))}</small>${x ? `<span data-pk="${esc(srvKey(x))}">${msBadge(x)}</span>` : ''}</div></div>
    <button class="btn hi big" id="svPlay" ${x && !busy ? '' : 'disabled'}>${esc(x ? t('play') + ' ' + x.name : t('sv_choose'))}</button>
    <div class="svcount">${(S.customServers || []).length + (C.servers || []).length} ${esc(t('sv_saved'))}</div>`;
}

// ---------- add-server dialog with live preview ----------
function openAddServer() {
  const m = $('#modal'); m.hidden = false;
  m.innerHTML = `<div class="dlg addm"><div class="amh"><h3>${esc(t('srv_add'))}</h3><button class="wb" id="asX" aria-label="Close">&#10005;</button></div>
    <div class="amg"><div class="amf"><label>${esc(t('as_name'))}</label><input type="text" id="asName" placeholder="Minecraft Server" maxlength="32" autocomplete="off">
      <label>${esc(t('as_addr'))}</label><input type="text" id="asIp" placeholder="play.example.net" maxlength="80" autocomplete="off" spellcheck="false"><div class="err" id="asErr"></div></div>
      <div class="amp" id="asPrev"></div></div>
    <div class="amb"><button class="btn" id="asCancel">${esc(t('cancel'))}</button><button class="btn hi" id="asSave">${esc(t('as_save'))}</button></div></div>`;
  let st = null, tm = 0, seq = 0;
  const parse = () => /^([A-Za-z0-9._-]{1,253})(?::(\d{1,5}))?$/.exec(($('#asIp').value || '').trim());
  const draw = () => {
    const nm = ($('#asName').value || '').trim(); const p = parse();
    const badge = st ? (st.online ? 'ONLINE' : 'OFFLINE') : '';
    const lines = st && st.online ? (st.motd || '').split('\n').map((l) => `<div>${esc(l.trim())}</div>`).join('') : '';
    $('#asPrev').innerHTML = `<div class="amc"><div class="amb2">${badge ? `<span class="bdg2 ${st.online ? 'on' : ''}">${badge}</span>` : ''}</div>
      <div class="amic" ${st && st.favicon ? `style="background-image:url('${st.favicon}')"` : ''}>${st && st.favicon ? '' : ic('servers')}</div>
      <b>${esc(nm || (p ? p[1] : 'Minecraft Server'))}</b>
      <small>${st === 'wait' ? '...' : st && st.online ? `${st.players} online · ${st.ping} ms · ${esc(st.version)}` : st ? esc(t('offline_s')) : esc(t('as_hint'))}</small><div class="amm">${lines}</div></div>`;
  };
  const query = () => {
    const p = parse(); clearTimeout(tm); st = null; draw(); if (!p) return;
    st = 'wait'; draw(); const my = ++seq;
    tm = setTimeout(async () => { const r = await window.ya.serverStatus(p[1], +p[2] || 25565); if (my !== seq) return; st = r; draw(); }, 450);
  };
  $('#asIp').oninput = query; $('#asName').oninput = draw; draw(); $('#asIp').focus();
  const close = () => { m.hidden = true; m.innerHTML = ''; };
  $('#asX').onclick = close; $('#asCancel').onclick = close;
  $('#asSave').onclick = () => {
    const p = parse(); if (!p) { $('#asErr').textContent = t('srv_bad'); return; }
    const list = S.customServers || []; if (list.length >= 30) return;
    const sv = { name: ($('#asName').value || '').trim() || p[1], host: p[1], port: +p[2] || 25565 };
    if (st && st.online && st.favicon) sv.image = st.favicon;
    list.push(sv); S.customServers = list; window.ya.saveSettings({ customServers: list });
    close(); toast(t('srv_added')); if (page === 'servers') render();
  };
}
function viewServers() {
  const form = srvDir ? `<div class="card sadd"><input type="text" id="dcIp" placeholder="play.example.net:25565" maxlength="80" autocomplete="off" spellcheck="false"><button class="btn hi" id="svGo">${esc(t('sv_connect'))}</button></div>` : '';
  return `<div class="sv2"><div class="svmain">
    <div class="card svbar"><h1>${esc(t('servers'))}</h1><div class="svsearch">${ic('search').replace('<svg', '<svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8"')}<input type="text" id="srvQ" placeholder="${esc(t('sv_search'))}" value="${esc(srvQ)}" autocomplete="off"></div>
      <button class="btn hi" id="svAddT">+ ${esc(t('srv_add'))}</button><button class="btn" id="svDirect">${esc(t('sv_direct'))}</button></div>
    ${form}<div class="svtabs"><button class="${srvTab === 'all' ? 'on' : ''}" data-svt="all">${esc(t('all'))}</button><button class="${srvTab === 'recent' ? 'on' : ''}" data-svt="recent">${esc(t('sv_recent'))}</button></div>
    <div class="card svlist" id="svlist">${svRows()}</div></div>
    <aside class="svside card" id="svside">${svSide()}</aside></div>`;
}
const MCATS = ['all', 'pvp', 'info', 'utility', 'on'];
function modCards() {
  const q = mq.trim().toLowerCase();
  const list = MODS.filter((m) => (mcat === 'all' || (mcat === 'on' ? modOn(m) : m.category === mcat)) && (!q || (m.name + ' ' + m.desc + ' ' + m.id).toLowerCase().includes(q)));
  const ok = (m) => modOk(m, S.version);
  list.sort((a, b) => (ok(b) - ok(a)) || a.name.localeCompare(b.name));
  return list.length ? list.map((m) => {
    const on = modOn(m), can = ok(m);
    const svg = window.MODICON && window.MODICON[m.id] ? `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">${window.MODICON[m.id]}</svg>` : ic('mods');
    return `<div class="card mm ${on && can ? 'on' : ''} ${can ? '' : 'off'}" ${can ? `data-mod="${esc(m.id)}"` : ''} title="${can ? '' : esc(t('mod_na'))}"><div class="mi">${svg}</div>
      <div class="mt"><b>${esc(m.name)}</b><small>${esc(m.desc || '')}</small><span class="cat">${esc(t('cat_' + m.category))}</span></div><div class="sw ${on && can ? 'on' : ''}"></div></div>`;
  }).join('') : `<div class="qe">${esc(t('mod_none'))}</div>`;
}
function profChips() {
  const own = S.profiles || [];
  return Object.keys(PRESETS).map((k) => `<button class="prof" data-pre="${k}">${esc(t('pre_' + k))}</button>`).join('') +
    own.map((p, i) => `<button class="prof ${String(i) === String(profSel) ? 'on' : ''}" data-prof="${i}">${esc(p.name)}<i>${p.mods.length}</i><span class="x" data-pdel="${i}" title="${esc(t('prof_del'))}">&times;</span></button>`).join('') +
    `<button class="prof add" id="profNew">+ ${esc(t('prof_save'))}</button>`;
}
function viewModules() {
  const ok = MODS.filter((m) => modOk(m, S.version)).length;
  return `<h1>${esc(t('mods'))}</h1><p class="sub">${esc(t('modsHint'))}</p>
    <div class="mhead"><div class="grow"><input type="text" id="mqIn" placeholder="${esc(t('mod_search'))}" value="${esc(mq)}" autocomplete="off"></div>
      ${MCATS.map((c) => `<button class="tab ${c === mcat ? 'on' : ''}" data-mc="${c}">${esc(t(c === 'all' ? 'all' : c === 'on' ? 'mod_on' : 'cat_' + c))}</button>`).join('')}
      <span class="mcount" id="mcount">${activeMods()} / ${ok}</span></div>
    <div class="mgrid" id="mgrid">${modCards()}</div>`;
}
let FR = { data: null, err: '', msg: '', ok: true };
function frRows(list, kind) {
  return list.map((f) => {
    const on = kind === 'friend' && f.online;
    const sub = kind === 'req' ? t('fr_wants') : kind === 'sent' ? t('fr_pending') : on ? (f.where === 'game' ? t('fr_game') : t('fr_launcher')) : t('fr_offl');
    const acts = kind === 'req'
      ? `<button class="btn sm hi" data-fa="accept" data-fu="${esc(f.uuid)}">${esc(t('fr_accept'))}</button><button class="btn sm" data-fa="decline" data-fu="${esc(f.uuid)}">${esc(t('fr_decline'))}</button>`
      : kind === 'sent' ? `<button class="btn sm" data-fa="decline" data-fu="${esc(f.uuid)}">${esc(t('fr_cancel'))}</button>`
      : `<button class="btn sm ghostb" data-fa="remove" data-fu="${esc(f.uuid)}">${esc(t('fr_remove'))}</button>`;
    return `<div class="card fr ${kind === 'friend' && !f.online ? 'off' : ''}"><div class="fav"><span>${esc(f.name[0].toUpperCase())}</span><img alt="" src="https://mc-heads.net/avatar/${encodeURIComponent(f.name)}/64">${on ? '<i class="dot"></i>' : ''}</div>
      <div class="ftx"><b>${esc(f.name)}</b><small class="${on ? 'live' : ''}">${esc(sub)}</small></div><div class="fact">${acts}</div></div>`;
  }).join('');
}
function frList() {
  if (FR.err) return `<div class="card sv ph">${esc(t(FR.err === 'not_configured' ? 'fr_r_not_configured' : 'fr_' + FR.err))}</div>`;
  if (!FR.data) return `<div class="card sv ph">${esc(t('fr_loading'))}</div>`;
  const d = FR.data; let h = '';
  const sec = (key, n, body) => body ? `<div class="frh">${esc(t(key))} <em>${n}</em></div>${body}` : '';
  h += sec('fr_req', d.incoming.length, frRows(d.incoming, 'req'));
  const on = d.friends.filter((f) => f.online), off = d.friends.filter((f) => !f.online);
  h += sec('fr_on', on.length, frRows(on, 'friend'));
  h += sec('fr_off', off.length, frRows(off, 'friend'));
  h += sec('fr_sent', d.outgoing.length, frRows(d.outgoing, 'sent'));
  return h || `<div class="card sv ph">${esc(t('fr_empty'))}</div>`;
}
function frSub() {
  const n = FR.data ? FR.data.friends.filter((f) => f.online).length : 0;
  return esc(t('fr_sub')) + (FR.data ? ` <span class="frn"><i class="dot s"></i>${n} ${esc(t('fr_n_online'))}</span>` : '');
}
function viewFriends() {
  return `<h1>${esc(t('fr_title'))}</h1><p class="sub" id="frSub">${frSub()}</p>
    <div class="card fradd"><div class="frin">${ic('friends')}<input id="frName" placeholder="${esc(t('fr_ph'))}" maxlength="16" autocomplete="off" spellcheck="false"></div><button class="btn hi" id="frAdd">${esc(t('fr_add'))}</button></div>
    <div class="frmsg ${FR.ok ? 'ok' : 'bad'}" id="frMsg">${esc(FR.msg)}</div><div class="frl" id="frList">${frList()}</div>`;
}
async function loadFriends() {
  if (!S.account) { FR.data = null; FR.err = 'premium'; return; }
  const r = await window.ya.friends();
  if (r.error) { FR.err = ['not_configured', 'login', 'premium'].includes(r.error) ? r.error : 'offline'; if (!FR.data) FR.data = null; }
  else { FR.err = ''; FR.data = r; }
  renderNav();
  if (page === 'friends') { const l = $('#frList'); if (l) l.innerHTML = frList(); const s = $('#frSub'); if (s) s.innerHTML = frSub(); }
}
setInterval(() => { if (S.account && S.account.type === 'microsoft') loadFriends(); }, 15000);
async function friendAct(a, v) {
  const r = await window.ya.friendAct(a, v);
  if (a === 'add') {
    FR.ok = !r.error; FR.msg = r.error ? (I18N.en['fr_r_' + r.error] ? t('fr_r_' + r.error) : t('fr_r_offline')) : r.status === 'accepted' ? t('fr_r_accepted') : t('fr_r_sent');
    const m = $('#frMsg'); if (m) { m.className = 'frmsg ' + (FR.ok ? 'ok' : 'bad'); m.textContent = FR.msg; }
    if (FR.ok) { const i = $('#frName'); if (i) i.value = ''; }
  }
  await loadFriends();
}
function viewCosmetics() {
  const prem = S.account && S.account.type !== 'microsoft' ? `<p class="note" style="margin-bottom:12px">${esc(t('cosPrem'))}</p>` : '';
  return `<h1>${esc(t('cosmetics'))}</h1><p class="sub">${esc(t('storeHint'))}</p>${prem}
    <div class="playrow" style="margin-bottom:18px"><button class="btn" id="openStore">${esc(t('store'))}</button></div>
    <div class="tabs" id="ctabs"></div><div id="cmsg" class="note"></div><div class="cos" id="cgrid"></div>`;
}
const BUNDLES = [
  ['beta', 'Beta Tester Bundle', '#e10600', 0], ['mono', 'Monochrome Bundle', '#e4e4e8', 10.99], ['qlux', 'Qlux Bundle', '#9fd8ff', 8.99],
  ['sakura', 'Sakura Bundle', '#ff86b6', 10.99], ['shinobi', 'Shinobi Bundle', '#8c6cff', 10.99], ['kitsune', 'Kitsune Bundle', '#ff9b3d', 10.99],
  ['santa', 'Santa Bundle', '#ff4040', 10.99], ['frost', 'Frost Bundle', '#6ec6ff', 10.99], ['clown', 'Clown Bundle', '#ffd21f', 10.99], ['luck', 'Luck Bundle', '#3ddc6a', 10.99]];
const SETCOL = { ya: '#e10600', mono: '#e4e4e8', qlux: '#9fd8ff', sakura: '#ff86b6', shinobi: '#8c6cff', kitsune: '#ff9b3d', santa: '#ff4040', frost: '#6ec6ff', clown: '#ffd21f', luck: '#3ddc6a', azure: '#c9cdd8', axolotl: '#c9cdd8', butterfly: '#c9cdd8', cat: '#c9cdd8', crown: '#c9cdd8', dragon: '#c9cdd8', dragonfly: '#c9cdd8', drake: '#c9cdd8', halo: '#c9cdd8', sky: '#c9cdd8', bunny: '#c9cdd8', sunset: '#c9cdd8', forest: '#c9cdd8', ninja: '#c9cdd8', ocean: '#c9cdd8', panda: '#c9cdd8', venom: '#c9cdd8', rainbow: '#c9cdd8', space: '#c9cdd8', penguin: '#c9cdd8', vamp: '#c9cdd8', vampire: '#c9cdd8', storm: '#c9cdd8', puppy: '#c9cdd8' };
function siteBase() { return String(S._store || C.storeUrl || '').replace(/\/+$/, '').replace(/\/store$/, ''); }
function openCos(v) {
  const b = siteBase(); if (!b) return;
  const [kind, id] = v.split(':');
  window.ya.openExternal(b + (kind === 'b' ? '/bundle?b=' : '/item?i=') + encodeURIComponent(id));
}
async function fillCosmetics() {
  const r = await window.ya.catalog();
  const msg = $('#cmsg'); if (!msg) return;
  if (r.error) { msg.textContent = r.error === 'offline' ? t('offline') : t('notConf'); }
  const prices = (r.data) || {};
  const slots = ['all', 'cape', 'wings', 'bandana', 'pet', 'bundles'];
  $('#ctabs').innerHTML = slots.map((s) => `<button class="tab ${s === cosTab ? 'on' : ''}" data-ct="${s}">${esc(t(s))}</button>`).join('');
  const money = (n) => (n ? '\u20ac' + n : t('owned_free'));
  const card = (img, col, name, price, v) =>
    `<div class="card ci" data-co="${esc(v)}" style="--c:${col}"><div class="pv"><img src="${img}" alt="" loading="lazy"></div><b>${esc(name)}</b><small>${esc(price)}</small></div>`;
  if (cosTab === 'bundles') {
    $('#cgrid').innerHTML = BUNDLES.map((b) => card('../assets/cos/b_' + b[0] + '.jpg', b[2], b[1], money(b[3]), 'b:' + b[0])).join('');
  } else {
    $('#cgrid').innerHTML = Object.keys(SLOT).filter((id) => cosTab === 'all' || SLOT[id] === cosTab).map((id) =>
      card('../assets/cos/' + id + '.jpg', SETCOL[id.split('_')[0]], NAMES[id], prices[id] ? t('price') + ': ' + prices[id] : money(id.startsWith('ya_') ? 0 : 3.49), 'i:' + id)).join('');
  }
  S._store = r.store || '';
}
let stab = 'game'; let sq = '';
const ST_TABS = [['game', 'Game'], ['general', 'General'], ['account', 'Account']];
function sRadio(name, cur, opts) {
  return opts.map((o) => `<button class="rad ${String(cur) === String(o[0]) ? 'on' : ''}" data-rad="${name}" data-val="${o[0]}"><i></i><b>${esc(o[1])}</b>${o[3] ? '<em class="rec">Recommended</em>' : ''}<small>${esc(o[2])}</small></button>`).join('');
}
function sSec(title, desc, body) { return `<section class="ssec"><h3>${esc(title)}</h3>${desc ? `<p>${esc(desc)}</p>` : ''}${body}</section>`; }
function settingsBody() {
  const q = sq.trim().toLowerCase(); const parts = [];
  const W = S.width || 1280, H = S.height || 720;
  const game = [
    sSec('Allocated Memory', 'How much memory should we allocate to the game instance', `<div class="ramv"><b id="ramv">${S.ram} MB</b></div><input type="range" id="sRam" min="1024" max="16384" step="256" value="${S.ram}"><div class="rr"><span>1 GB</span><span>16 GB</span></div>`),
    sSec('Game Resolution', 'Set the resolution of the game instance', `<div class="res"><span>W</span><input type="number" id="sW" value="${W}"><span>&times;</span><span>H</span><input type="number" id="sH" value="${H}"></div>
      <label class="chk"><button class="sw ${S.fullscreen ? 'on' : ''}" id="sFull"></button> Launch in fullscreen mode</label>`),
    sSec('Java', t('javaHint'), `<div style="display:flex;gap:8px"><input type="text" id="sJava" value="${esc(S.javaPath || 'java')}"><button class="btn sm" id="sDet">${esc(t('detect'))}</button></div>`),
    sSec('Launcher visibility on Game Launch', 'Select which option the Launcher should take after the game has launched', sRadio('min', !!S.minimizeOnLaunch, [[false, 'Keep Open', 'Launcher will remain open in background', true], [true, 'Hide', 'Launcher will hide when the game instance spawns']])),
  ];
  const general = [
    sSec(t('language'), 'Select the language of the launcher', `<select id="sLang" class="vsel">${Object.keys(LANGS).map((k) => `<option value="${k}" ${k === S.lang ? 'selected' : ''}>${LANGS[k]}</option>`).join('')}</select>`),
    sSec(t('folder'), 'Open the folder where game files are stored', '<button class="btn sm" id="sFolder">Open &#8594;</button>'),
  ];
  const account = [S.account ? sSec(S.account.name, S.account.type === 'microsoft' ? 'Microsoft account' : 'Offline account', `<button class="btn sm" id="sOut">${esc(t('logout'))}</button>`) : sSec('Account', 'Not signed in', '')];
  const sets = { game, general, account };
  if (q) Object.values(sets).forEach((l) => l.forEach((h) => { if (h.toLowerCase().replace(/<[^>]+>/g, ' ').indexOf(q) >= 0) parts.push(h); }));
  else (sets[stab] || game).forEach((h) => parts.push(h));
  return parts.join('') || '<p class="sub">No results</p>';
}
function viewSettings() {
  return `<div class="smodal"><div class="snav"><input type="text" id="sSearch" placeholder="Search settings..." value="${esc(sq)}">
    ${ST_TABS.map((x) => `<button class="snb ${stab === x[0] && !sq ? 'on' : ''}" data-st="${x[0]}">${x[1]}</button>`).join('')}</div>
    <div class="sright"><h2>${esc(sq ? 'Search' : ST_TABS.find((x) => x[0] === stab)[1] + ' Settings')}</h2><div class="sbody" id="sbody">${settingsBody()}</div></div></div>`;
}
function viewConsole() {
  return `<h1>${esc(t('console'))}</h1><p class="sub"></p><div class="con conp" id="con">${esc(logBuf.join('\n'))}</div>`;
}
function render() {
  const v = $('#view');
  v.innerHTML = { home: viewHome, modules: viewModules, plus: viewPlus, versions: viewVersions, servers: viewServers, friends: viewFriends, cosmetics: viewCosmetics, console: viewConsole, settings: viewSettings }[page]();
  if (page === 'cosmetics') fillCosmetics();
  if (page === 'friends') loadFriends();
  if (page === 'home' || page === 'servers') pingAll();
  if (page === 'home' && S.account) mountPlayer($('#mc3d'), S.account.name, 5.8);
}


// ---------- 3D player (CSS 3D, no WebGL): builds the Minecraft model from the account's skin ----------
const SKIN_URLS = (n) => ['https://mc-heads.net/skin/' + encodeURIComponent(n), 'https://minotar.net/skin/' + encodeURIComponent(n)];
const skinCache = {};
function loadSkin(name) {
  if (skinCache[name]) return skinCache[name];
  const tryAt = (i, resolve) => {
    const urls = SKIN_URLS(name);
    if (i >= urls.length) return resolve(null);
    const im = new Image();
    im.onload = () => resolve({ url: urls[i], h: im.naturalHeight >= 64 ? 64 : 32 });
    im.onerror = () => tryAt(i + 1, resolve);
    im.src = urls[i];
  };
  skinCache[name] = new Promise((res) => tryAt(0, res));
  return skinCache[name];
}
const SHADE = ['', 's-b', 's-s', 's-s', 's-t', 's-d'];
function mkBox(K, sk, w, h, d, u, v, pad) {
  const el = document.createElement('div'); el.className = 'part';
  const W = w + pad * 2, H = h + pad * 2, D = d + pad * 2;
  const faces = [
    [u + d, v + d, w, h, 'translateZ(' + (D * K / 2) + 'px)', W, H],
    [u + 2 * d + w, v + d, w, h, 'rotateY(180deg) translateZ(' + (D * K / 2) + 'px)', W, H],
    [u, v + d, d, h, 'rotateY(-90deg) translateZ(' + (W * K / 2) + 'px)', D, H],
    [u + d + w, v + d, d, h, 'rotateY(90deg) translateZ(' + (W * K / 2) + 'px)', D, H],
    [u + d, v, w, d, 'rotateX(90deg) translateZ(' + (H * K / 2) + 'px)', W, D],
    [u + d + w, v, w, d, 'rotateX(-90deg) translateZ(' + (H * K / 2) + 'px)', W, D]
  ];
  faces.forEach((f) => {
    const sx = f[0], sy = f[1], fw = f[2], fh = f[3], pw = f[5], ph = f[6];
    const e = document.createElement('div'); e.className = 'f ' + SHADE[faces.indexOf(f)];
    e.style.cssText = 'width:' + pw * K + 'px;height:' + ph * K + 'px;margin:' + (-ph * K / 2) + 'px 0 0 ' + (-pw * K / 2) + 'px;transform:' + f[4] +
      ';background-image:url("' + sk.url + '");background-size:' + (64 * K * pw / fw) + 'px ' + (sk.h * K * ph / fh) + 'px;background-position:' + (-sx * K * pw / fw) + 'px ' + (-sy * K * ph / fh) + 'px';
    el.appendChild(e);
  });
  return el;
}
async function mountPlayer(host, name, K) {
  if (!host) return;
  K = K || 5.2;
  const sk = await loadSkin(name);
  if (!sk || !host.isConnected) { if (host.isConnected) host.innerHTML = '<img class="logo" src="../assets/logo.png" alt="">'; return; }
  const modern = sk.h >= 64;
  host.innerHTML = '<div class="stage"></div>';
  const stage = host.firstChild;
  const put = (parent, x, y, kids) => { const g = document.createElement('div'); g.className = 'limb'; g.style.transform = 'translate3d(' + x * K + 'px,' + y * K + 'px,0)'; kids.forEach((c) => g.appendChild(c)); parent.appendChild(g); return g; };
  const part = (w, h, d, u, v, ov, ou, dy) => {   // a body part plus its overlay layer, shifted so the pivot is at the top
    const g = document.createElement('div'); g.className = 'part'; g.style.transform = 'translateY(' + (dy * K) + 'px)';
    g.appendChild(mkBox(K, sk, w, h, d, u, v, 0));
    if (ov) g.appendChild(mkBox(K, sk, w, h, d, ou[0], ou[1], 0.45));
    return g;
  };
  put(stage, 0, -10 + 0, [(() => { const g = part(8, 8, 8, 0, 0, true, [32, 0], 0); return g; })()]);                    // head
  put(stage, 0, 0, [part(8, 12, 4, 16, 16, modern, [16, 32], 0)]);                                                         // body
  const arm = (x, u, v, ou) => put(stage, x, -6, [part(4, 12, 4, u, v, modern, ou, 6)]);
  const leg = (x, u, v, ou) => put(stage, x, 6, [part(4, 12, 4, u, v, modern, ou, 6)]);
  const rArm = arm(-6, 40, 16, [40, 32]);
  const lArm = arm(6, modern ? 32 : 40, modern ? 48 : 16, [48, 48]);
  const rLeg = leg(-2, 0, 16, [0, 32]);
  const lLeg = leg(2, modern ? 16 : 0, modern ? 48 : 16, [0, 48]);
  let yaw = 25, drag = false, lx = 0, t0 = performance.now();
  host.onpointerdown = (e) => { drag = true; lx = e.clientX; host.setPointerCapture(e.pointerId); host.classList.add('grab'); };
  host.onpointermove = (e) => { if (drag) { yaw += (e.clientX - lx) * 0.8; lx = e.clientX; } };
  host.onpointerup = host.onpointercancel = () => { drag = false; host.classList.remove('grab'); };
  (function frame(now) {
    if (!host.isConnected) return;
    const t = (now - t0) / 1000;
    if (!drag) yaw += 0.35;
    stage.style.transform = 'rotateX(-12deg) rotateY(' + yaw + 'deg)';
    const sw = Math.sin(t * 1.6) * 7;
    rArm.style.transform = 'translate3d(' + (-6 * K) + 'px,' + (-6 * K) + 'px,0) rotateX(' + sw + 'deg)';
    lArm.style.transform = 'translate3d(' + (6 * K) + 'px,' + (-6 * K) + 'px,0) rotateX(' + (-sw) + 'deg)';
    rLeg.style.transform = 'translate3d(' + (-2 * K) + 'px,' + (6 * K) + 'px,0) rotateX(' + (-sw * 0.6) + 'deg)';
    lLeg.style.transform = 'translate3d(' + (2 * K) + 'px,' + (6 * K) + 'px,0) rotateX(' + (sw * 0.6) + 'deg)';
    requestAnimationFrame(frame);
  })(performance.now());
}

// ---------- YA Plus: detection, welcome + setup ----------
async function refreshPlus() {
  try {
    const r = await window.ya.plus();
    const was = plusOn();
    S.plus = !!(r && r.plus);
    if (was !== plusOn()) { renderNav(); render(); }
    if (plusOn() && !S.plusSetupDone) showOnboarding();
  } catch (_) { }
}
let obStep = 0, obPreset = 'pvp', obRam = 3072;
const PRESETS = {
  pvp: (m) => m.category === 'pvp' || m.category === 'utility',
  all: () => true,
  min: (m) => ['fps_boost', 'bg_fps', 'fps', 'cps', 'keystrokes', 'coords'].indexOf(m.id) >= 0
};
async function applyPreset() {
  const pick = PRESETS[obPreset] || PRESETS.pvp;
  for (const m of MODS) {
    if (m.soon || !(m.versions || []).some((sp) => specOk(sp, S.ya || '1.8.9'))) continue;
    const on = !!pick(m);
    MODSTATE[m.id] = on; await window.ya.modsSet(m.id, on);
  }
}
function showOnboarding(startAt) {
  obStep = startAt || 0; obPreset = 'pvp'; obRam = Math.max(S.ram || 2048, 3072);
  $('#ob').hidden = false; drawOb();
}
function drawOb() {
  const o = $('#ob');
  const dots = `<div class="dots">${[0, 1, 2].map((i) => `<i class="${i === obStep ? 'on' : ''}"></i>`).join('')}</div>`;
  const perks = ['perk_theme', 'perk_cape', 'perk_more', 'perk_setup'].map((k) => `<span class="perk">${ic('check')}${esc(t(k))}</span>`).join('');
  if (obStep === 0) {
    const row = (ico, k) => `<div class="frow"><span class="fic">${ic(ico)}</span><div><b>${esc(t(k + 't'))}</b><small>${esc(t(k + 'd'))}</small></div></div>`;
    o.innerHTML = `<div class="split"><div class="vis"><div class="mc3d big" id="obPlayer"></div><span class="plusbadge">PLUS</span></div>
      <div class="txt"><div class="eyebrow">${esc(t('wp_eyebrow2'))}</div><h1>${esc(t('wp_h'))}</h1><p>${esc(t('wp_sub'))}</p>
        <div class="frows">${row('plus', 'f1')}${row('cosmetics', 'f2')}${row('check', 'f3')}</div>
        <div class="acts"><button class="go" data-ob="next">${esc(t('cont'))}</button><button class="ghost" data-ob="skip">${esc(t('skip'))}</button></div>${dots}</div></div>`;
    if (S.account) mountPlayer($('#obPlayer'), S.account.name, 6.2);
  } else if (obStep === 1) {
    const op = (k) => `<button class="opt ${obPreset === k ? 'on' : ''}" data-preset="${k}"><b>${esc(t('pre_' + k))}</b><small>${esc(t('pre_' + k + '_d'))}</small></button>`;
    o.innerHTML = `<div class="in"><div class="eyebrow">&#9733; ${esc(t('wp_eyebrow'))}</div><h1 style="font-size:40px">${esc(t('st_title'))}</h1><p>${esc(t('st_sub'))}</p>
      <div class="lbl">${esc(t('lbl_preset'))}</div><div class="opts">${op('pvp')}${op('all')}${op('min')}</div>
      <div class="lbl">${esc(t('lbl_perf'))}</div>
      <div class="line"><div><b>${esc(t('ram_l'))}</b><small id="obRamV">${obRam} MB · ${esc(t('ram_d'))}</small></div><input type="range" id="obRam" min="1024" max="8192" step="256" value="${obRam}"></div>
      <div class="line"><div><b>${esc(t('minimize'))}</b></div><button class="sw ${S.minimizeOnLaunch ? 'on' : ''}" data-ob="min"></button></div>
      <button class="go" data-ob="next">${esc(t('cont'))}</button><button class="ghost" data-ob="back">${esc(t('back_b'))}</button>${dots}</div>`;
  } else {
    o.innerHTML = `<div class="in"><div class="eyebrow">&#9733; ${esc(t('wp_eyebrow'))}</div><h1 style="font-size:46px">${esc(t('done_t'))}</h1><p>${esc(t('done_s'))}</p>
      <button class="go" data-ob="finish">${esc(t('finish'))}</button>${dots}</div>`;
  }
}
async function finishOb() {
  S.ram = obRam; S.plusSetupDone = true;
  await window.ya.saveSettings({ ram: S.ram, plusSetupDone: true, minimizeOnLaunch: S.minimizeOnLaunch });
  await applyPreset();
  $('#ob').hidden = true; go('home');
}

function showLogin() {
  const m = $('#modal'); m.hidden = false;
  m.innerHTML = `<div class="dlg"><div class="mark">YA</div><button class="btn" id="lMs">${esc(t('microsoft'))}</button>
    <label style="color:var(--mute);font-size:12px">${esc(t('offlineL'))}</label><input type="text" id="lName" maxlength="16" style="width:100%">
    <button class="btn" id="lOff">${esc(t('offlineBtn'))}</button><p class="note" style="margin:0">${esc(t('offHint'))}</p><div class="err" id="lErr"></div><button class="btn" id="lClose">${esc(t('cancel'))}</button></div>`;
  const done = (r) => {
    if (r && r.error) { $('#lErr').textContent = t(r.error); return; }
    S.account = r; m.hidden = true; renderNav(); render(); refreshPlus();
  };
  $('#lClose').onclick = () => (m.hidden = true);
  $('#lMs').onclick = async () => { $('#lErr').textContent = '...'; done(await window.ya.loginMicrosoft()); };
  $('#lOff').onclick = async () => done(await window.ya.loginOffline($('#lName').value));
}

function pushRecent(sv) {
  const f = allServers().find((y) => srvKey(y) === srvKey(sv)) || sv;
  const rec = { name: f.name || sv.host, host: sv.host, port: sv.port || 25565, image: f.image };
  S.recentServers = [rec, ...(S.recentServers || []).filter((y) => srvKey(y) !== srvKey(rec))].slice(0, 8);
  window.ya.saveSettings({ recentServers: S.recentServers });
}
async function launch(server) {
  if (busy) return;
  if (server && S.account) pushRecent(server);
  if (!S.account) { showLogin(); return; }
  busy = true; status = { text: '', pct: 2, show: true }; go('home');
  const r = await window.ya.play(server ? { server } : {});
  if (r && r.error) {
    busy = false; status = { text: r.error === 'java_missing' ? t('java_missing').replace(/\{n\}/g, r.detail) : t(r.error) + (r.detail ? ': ' + r.detail : ''), pct: 0, show: false };
    if (r.error === 'need_login') showLogin();
    if (page === 'home') render();
  }
}

// ---------- toasts ----------
function toast(msg, kind) {
  const w = $('#toasts'); if (!w) return;
  const d = document.createElement('div'); d.className = 'toast ' + (kind || ''); d.innerHTML = '<i></i><span></span>'; d.lastChild.textContent = msg; w.appendChild(d);
  setTimeout(() => { d.classList.add('out'); setTimeout(() => d.remove(), 320); }, 2600);
}
// ---------- mod profiles ----------
async function applyProfile(pick) {
  for (const m of MODS) {
    if (!modOk(m, S.version)) continue;
    const on = !!pick(m); if (modOn(m) === on) continue;
    MODSTATE[m.id] = on; await window.ya.modsSet(m.id, on);
  }
}
async function applyProf(p) {
  await applyProfile((m) => p.mods.includes(m.id));
  if (p.ram) { S.ram = p.ram; window.ya.saveSettings({ ram: p.ram }); }
}
function saveProfPrompt() {
  const el = $('#profs'); if (!el || el.querySelector('#pnIn')) return;
  const add = $('#profNew'); const inp = document.createElement('input');
  inp.type = 'text'; inp.id = 'pnIn'; inp.placeholder = t('prof_name'); inp.maxLength = 24; inp.style.cssText = 'height:36px;width:170px';
  add.replaceWith(inp); inp.focus();
  const done = () => {
    const name = inp.value.trim().slice(0, 24); if (!name) return render();
    const list = (S.profiles || []).filter((p) => p.name !== name); if (list.length >= 12) list.shift();
    list.push({ name, mods: MODS.filter((m) => modOk(m, S.version) && modOn(m)).map((m) => m.id), ram: S.ram }); S.profiles = list;
    window.ya.saveSettings({ profiles: list }); profSel = String(list.length - 1); toast(t('prof_saved')); render();
  };
  inp.addEventListener('keydown', (ev) => { if (ev.key === 'Enter') done(); if (ev.key === 'Escape') render(); });
  inp.addEventListener('blur', () => setTimeout(() => { if (document.body.contains(inp)) done(); }, 120));
}
// ---------- command palette (Ctrl+K) ----------
let palQ = '', palSel = 0, palItems = [];
function palList() {
  const q = palQ.trim().toLowerCase(); const out = [];
  const add = (g, label, ico, hint, fn) => { if (!q || label.toLowerCase().includes(q)) out.push({ g, label, ico, hint, fn }); };
  PAGES.forEach((p) => add(t('pal_pages'), t(PKEY[p]), p, '', () => go(p)));
  add(t('pal_act'), t('launch'), 'play', S.version, () => launch());
  add(t('pal_act'), t('folder'), 'folder', '', () => window.ya.openFolder());
  add(t('pal_act'), t('open_store'), 'cosmetics', '', () => { const u = S._store || C.storeUrl; if (u) window.ya.openExternal(u); });
  allServers().forEach((x) => add(t('servers'), x.name, 'servers', srvKey(x), () => launch({ host: x.host, port: x.port || 25565 })));
  if (q) MODS.filter((m) => modOk(m, S.version)).forEach((m) => add(t('mods'), m.name, 'mods', modOn(m) ? 'ON' : 'OFF', async () => { const on = !modOn(m); MODSTATE[m.id] = on; await window.ya.modsSet(m.id, on); toast(m.name + ': ' + (on ? 'ON' : 'OFF')); if (page === 'modules') render(); }));
  return out.slice(0, 40);
}
function drawPal() {
  palItems = palList(); let last = '';
  $('#palList').innerHTML = palItems.map((it, i) => { const g = it.g !== last ? `<div class="pgrp">${esc(it.g)}</div>` : ''; last = it.g; return `${g}<button class="pit ${i === palSel ? 'sel' : ''}" data-pit="${i}">${ICON[it.ico] ? ic(it.ico) : ic('mods')}<b>${esc(it.label)}</b><small>${esc(it.hint)}</small></button>`; }).join('') || `<div class="qe">${esc(t('mod_none'))}</div>`;
}
function openPal() {
  const p = $('#pal'); palQ = ''; palSel = 0; p.hidden = false;
  p.innerHTML = `<div class="palbox"><input type="text" id="palIn" placeholder="${esc(t('pal_ph'))}" autocomplete="off" spellcheck="false"><div class="pall" id="palList"></div></div>`;
  drawPal(); $('#palIn').focus();
}
function closePal() { const p = $('#pal'); if (p) { p.hidden = true; p.innerHTML = ''; } }
function runPal(i) { const it = palItems[i]; closePal(); if (it) it.fn(); }
document.addEventListener('click', (e) => {
  const pit = e.target.closest('[data-pit]');
  if (pit) { e.stopPropagation(); return runPal(+pit.dataset.pit); }
  if (e.target.id === 'pal') closePal();
}, true);
document.addEventListener('keydown', (e) => {
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') { e.preventDefault(); return $('#pal').hidden ? openPal() : closePal(); }
  if ($('#pal') && !$('#pal').hidden) {
    if (e.key === 'Escape') return closePal();
    if (e.key === 'ArrowDown') { e.preventDefault(); palSel = Math.min(palItems.length - 1, palSel + 1); drawPal(); const s2 = $('.pit.sel'); if (s2) s2.scrollIntoView({ block: 'nearest' }); }
    if (e.key === 'ArrowUp') { e.preventDefault(); palSel = Math.max(0, palSel - 1); drawPal(); const s2 = $('.pit.sel'); if (s2) s2.scrollIntoView({ block: 'nearest' }); }
    if (e.key === 'Enter') { e.preventDefault(); runPal(palSel); }
  }
});

let brandClicks = 0, brandT = 0;
document.addEventListener('click', (e) => {
  if (!e.target.closest('#brand')) return;
  const now = Date.now(); brandClicks = now - brandT < 900 ? brandClicks + 1 : 1; brandT = now;
  if (brandClicks >= 5) {   // hidden: preview the Plus look without a subscription (look only, perks stay server-side)
    brandClicks = 0; S.plusPreview = !S.plusPreview; window.ya.saveSettings({ plusPreview: S.plusPreview });
    renderNav(); render(); if (S.plusPreview && !S.plusSetupDone) showOnboarding();
  }
});
document.addEventListener('click', async (e) => {
  const q = e.target.closest('[data-qs],[data-pit],[data-mc],[data-pre],[data-prof],[data-pdel],[data-sp],[data-sdel],#saAdd,#profNew,#openPal');
  if (!q) return;
  if (q.id === 'openPal') return openPal();
  if (q.dataset.qs) { const x = allServers().find((y) => srvKey(y) === q.dataset.qs); if (x) launch({ host: x.host, port: x.port || 25565 }); return; }
  if (q.dataset.sp) { e.stopPropagation(); const x = findSrv(q.dataset.sp); if (x) launch({ host: x.host, port: x.port || 25565 }); return; }
  if (q.dataset.sdel != null) { S.customServers = (S.customServers || []).filter((_, i) => i !== +q.dataset.sdel); window.ya.saveSettings({ customServers: S.customServers }); toast(t('srv_removed')); return render(); }
  if (q.id === 'saAdd') {
    const name = ($('#saName').value || '').trim(); let ip = ($('#saIp').value || '').trim();
    const m = /^([A-Za-z0-9._-]{1,253})(?::(\d{1,5}))?$/.exec(ip);
    if (!m) { toast(t('srv_bad'), 'bad'); return; }
    const list = S.customServers || []; if (list.length >= 30) return;
    list.push({ name: name || m[1], host: m[1], port: +m[2] || 25565 }); S.customServers = list;
    window.ya.saveSettings({ customServers: list }); toast(t('srv_added')); srvAdd = false; return render();
  }
  if (q.dataset.mc) { mcat = q.dataset.mc; return render(); }
  if (q.dataset.pre) { await applyProfile((m) => PRESETS[q.dataset.pre](m)); profSel = ''; toast(t('pre_' + q.dataset.pre)); return render(); }
  if (q.dataset.pdel != null) { e.stopPropagation(); const i = +q.dataset.pdel; S.profiles = (S.profiles || []).filter((_, k) => k !== i); profSel = ''; window.ya.saveSettings({ profiles: S.profiles }); toast(t('prof_deleted')); return render(); }
  if (q.dataset.prof != null) {
    const p = (S.profiles || [])[+q.dataset.prof]; if (p) { await applyProf(p); profSel = q.dataset.prof; toast(t('prof_applied') + ': ' + p.name); render(); } return;
  }
  if (q.id === 'profNew') return saveProfPrompt();
}, true);
document.addEventListener('click', (e) => {
  const q = e.target.closest('[data-ss],[data-svt],#svAddT,#svAddT2,#svDirect,#svGo,#svPlay');
  if (!q || e.target.closest('[data-sp],[data-sdel]')) return;
  if (q.dataset.ss) { srvSel = q.dataset.ss; document.querySelectorAll('.srow').forEach((r) => r.classList.toggle('sel', r.dataset.ss === srvSel)); $('#svside').innerHTML = svSide(); return; }
  if (q.dataset.svt) { srvTab = q.dataset.svt; return render(); }
  if (q.id === 'svAddT' || q.id === 'svAddT2') return openAddServer();
  if (q.id === 'svDirect') { srvDir = !srvDir; srvAdd = false; return render(); }
  if (q.id === 'svGo') {
    const m = /^([A-Za-z0-9._-]{1,253})(?::(\d{1,5}))?$/.exec(($('#dcIp').value || '').trim());
    if (!m) { toast(t('srv_bad'), 'bad'); return; }
    srvDir = false; return launch({ host: m[1], port: +m[2] || 25565 });
  }
  if (q.id === 'svPlay') { const x = findSrv(srvSel); if (x) launch({ host: x.host, port: x.port || 25565 }); return; }
}, true);
document.addEventListener('click', async (e) => {
  if (e.target.closest('[data-pdel]')) return;
  const el = e.target.closest('button, .nc, .ad, .ci, [data-p], [data-mod]');
  if (!el) return;
  if (el.dataset.w) return window.ya.win(el.dataset.w);
  if (el.dataset.ob) {
    const a = el.dataset.ob;
    if (a === 'next') { obStep++; return drawOb(); }
    if (a === 'back') { obStep--; return drawOb(); }
    if (a === 'skip') { S.plusSetupDone = true; window.ya.saveSettings({ plusSetupDone: true }); $('#ob').hidden = true; return; }
    if (a === 'min') { S.minimizeOnLaunch = !S.minimizeOnLaunch; el.classList.toggle('on', S.minimizeOnLaunch); return; }
    if (a === 'finish') return finishOb();
  }
  if (el.dataset.clx) return closeChangelog();
  if (el.dataset.ci) return showChangelog(+el.dataset.ci);
  if (el.dataset.preset) { obPreset = el.dataset.preset; return drawOb(); }
  if (el.id === 'plusChip') return go('plus');
  if (el.id === 'frAdd') { const v = ($('#frName').value || '').trim(); if (v.length < 3) return; FR.ok = true; FR.msg = t('fr_sending'); $('#frMsg').textContent = FR.msg; $('#frMsg').className = 'frmsg ok'; return friendAct('add', v); }
  if (el.dataset.fa) return friendAct(el.dataset.fa, el.dataset.fu);
  if (el.id === 'accPill') return showLogin();
  if (el.id === 'rerun') return showOnboarding(0);
  if (el.id === 'buyPlus') { const u = C.storeUrl; if (u) window.ya.openExternal(u); return; }
  if (el.dataset.p) return go(el.dataset.p);
  if (el.id === 'btnAcc' || el.id === 'accBtn') return showLogin();
  if (el.id === 'bgBtn') { S.bg = (((S.bg == null ? 1 : S.bg)) + 1) % (BGS + 1); window.ya.saveSettings({ bg: S.bg }); const h = $('#bgi'); if (h) h.style.backgroundImage = S.bg > 0 ? `url('bg/bg${S.bg}.jpg')` : ''; return; }
  if (el.id === 'play') return launch();
  if (el.dataset.srv != null) { const s = (C.servers || [])[+el.dataset.srv]; if (s) launch({ host: s.host, port: s.port || 25565 }); return; }
  if (el.dataset.url) return window.ya.openExternal(el.dataset.url);
  if (el.dataset.st) { stab = el.dataset.st; sq = ''; return render(); }
  if (el.dataset.rad) { if (el.dataset.rad === 'min') { S.minimizeOnLaunch = el.dataset.val === 'true'; window.ya.saveSettings({ minimizeOnLaunch: S.minimizeOnLaunch }); } $('#sbody').innerHTML = settingsBody(); return; }
  if (el.id === 'sFull') { S.fullscreen = !S.fullscreen; el.classList.toggle('on', S.fullscreen); return window.ya.saveSettings({ fullscreen: S.fullscreen }); }
  if (el.dataset.vl) { vline = el.dataset.vl; const l = curLine(); if (l && !l.vs.some((v) => v.id === S.version)) { S.version = l.vs[0].id; window.ya.saveSettings({ version: S.version }); renderNav(); } $('#vgrid').innerHTML = vCards(); $('#vpanel').innerHTML = vPanel(); return; }
  if (el.dataset.ld) { S.loader = el.dataset.ld; window.ya.saveSettings({ loader: S.loader }); $('#vld').innerHTML = loaderBtns(); return; }
  if (el.id === 'play2') return launch();
  if (el.dataset.mod) {
    const id = el.dataset.mod; const m = MODS.find((x) => x.id === id); const on = !modOn(m); MODSTATE[id] = on;
    el.classList.toggle('on', on); const sw = el.querySelector('.sw'); if (sw) sw.classList.toggle('on', on); const mc = $('#mcount'); if (mc) mc.textContent = activeMods() + ' / ' + MODS.filter((x) => modOk(x, S.version)).length;
    if (mcat === 'on') { $('#mgrid').innerHTML = modCards(); }
    return window.ya.modsSet(id, on);
  }
  if (el.dataset.co) return openCos(el.dataset.co);
  if (el.dataset.ct) { cosTab = el.dataset.ct; return fillCosmetics(); }
  if (el.id === 'openStore') { const u = S._store || C.storeUrl; if (u) window.ya.openExternal(u); return; }
  if (el.id === 'sMin') { S.minimizeOnLaunch = !S.minimizeOnLaunch; el.classList.toggle('on', S.minimizeOnLaunch); return window.ya.saveSettings({ minimizeOnLaunch: S.minimizeOnLaunch }); }
  if (el.id === 'sFolder') return window.ya.openFolder();
  if (el.id === 'sDet') { const p = await window.ya.detectJava(); if (p) { $('#sJava').value = p; S.javaPath = p; window.ya.saveSettings({ javaPath: p }); } return; }
  if (el.id === 'sOut') { await window.ya.logout(); S.account = null; S.plus = false; window.ya.saveSettings({ plus: false }); renderNav(); render(); }
});
document.addEventListener('change', async (e) => {
  if (e.target.id === 'verSel') { S.version = e.target.value; window.ya.saveSettings({ version: S.version }); renderNav(); render(); }
  if (e.target.id === 'vsel') { S.version = e.target.value; window.ya.saveSettings({ version: S.version }); renderNav(); $('#vgrid').innerHTML = vCards(); $('#vpanel').innerHTML = vPanel(); }
  if (e.target.id === 'sW' || e.target.id === 'sH') { S.width = +$('#sW').value || 1280; S.height = +$('#sH').value || 720; window.ya.saveSettings({ width: S.width, height: S.height }); }
  if (e.target.id === 'sLang') { S.lang = e.target.value; await window.ya.setLang(S.lang); renderNav(); render(); }
  if (e.target.id === 'sJava') { S.javaPath = e.target.value || 'java'; window.ya.saveSettings({ javaPath: S.javaPath }); }
  if (e.target.id === 'sRam') window.ya.saveSettings({ ram: S.ram });
});
document.addEventListener('keydown', (e) => { if (e.key === 'Enter' && e.target.id === 'frName') $('#frAdd').click(); });
document.addEventListener('input', (e) => {
  if (e.target.id === 'srvQ') { srvQ = e.target.value; $('#svlist').innerHTML = svRows(); pingAll(); }
  if (e.target.id === 'mqIn') { mq = e.target.value; $('#mgrid').innerHTML = modCards(); }
  if (e.target.id === 'palIn') { palQ = e.target.value; palSel = 0; drawPal(); }
  if (e.target.id === 'sSearch') { sq = e.target.value; $('#sbody').innerHTML = settingsBody(); }
  if (e.target.id === 'obRam') { obRam = +e.target.value; $('#obRamV').textContent = obRam + ' MB · ' + t('ram_d'); }
  if (e.target.id === 'sRam') { S.ram = +e.target.value; $('#ramv').textContent = S.ram + ' MB'; }
});

window.ya.on('status', (d) => {
  const txt = t(d.k);
  status = { text: d.done ? t(d.k) : txt, pct: d.pct || 0, show: !d.done };
  if (d.k === 'ingame' && !S._pStart) { S._pStart = Date.now(); toast(t('ingame')); }
  if (d.done) {
    busy = false;
    if (S._pStart) { S.playMs = (S.playMs || 0) + (Date.now() - S._pStart); S._pStart = 0; S.lastPlayed = Date.now(); window.ya.saveSettings({ playMs: S.playMs, lastPlayed: S.lastPlayed }); }
    if (d.k === 'crashed') toast(t('crashed'), 'bad');
  }
  if (page === 'home') {
    const bar = $('#bar'); if (bar) { bar.style.display = status.show ? 'block' : 'none'; $('#barI').style.width = status.pct + '%'; $('#stat').textContent = status.text; }
    const p = $('#play'); if (p) { p.disabled = busy; const b = p.querySelector('b'); if (b) b.textContent = busy ? t('launching') : t('launch'); }
    if (d.done) render();
  }
});
window.ya.on('log', (m) => {
  logBuf.push(String(m)); if (logBuf.length > 400) logBuf.shift();
  const c = $('#con'); if (c) { c.textContent = logBuf.join('\n'); c.scrollTop = c.scrollHeight; }
});

Object.assign(I18N.en, { offHint: 'Offline / TLauncher accounts can play, but cosmetics only work with a Microsoft (premium) account.', cosPrem: 'You are signed in offline. Cosmetics only work with a Microsoft (premium) account.' });
Object.assign(I18N.it, { offHint: 'Gli account offline / TLauncher possono giocare, ma i cosmetici funzionano solo con un account Microsoft (premium).', cosPrem: 'Sei collegato offline. I cosmetici funzionano solo con un account Microsoft (premium).' });
Object.assign(I18N.es, { offHint: 'Las cuentas sin conexión / TLauncher pueden jugar, pero los cosméticos solo funcionan con una cuenta Microsoft (premium).', cosPrem: 'Has iniciado sesión sin conexión. Los cosméticos solo funcionan con una cuenta Microsoft (premium).' });
Object.assign(I18N.de, { offHint: 'Offline-/TLauncher-Konten können spielen, Kosmetik funktioniert aber nur mit einem Microsoft-Konto (Premium).', cosPrem: 'Du bist offline angemeldet. Kosmetik funktioniert nur mit einem Microsoft-Konto (Premium).' });

document.addEventListener('error', (e) => {   // inline onerror is blocked by the CSP, so listen here
  const im = e.target; if (im && im.classList && im.classList.contains('face')) { const i = document.createElement('i'); i.textContent = im.dataset.ini || '?'; im.replaceWith(i); }
}, true);

Object.assign(I18N.en, {"wz_wel": "Welcome to YA CLIENT", "wz_wel_s": "Let's get you set up. It only takes a minute.", "wz_lang": "Choose your preferred language", "wz_dir": "Where should YA CLIENT live?", "wz_dir_s": "Your game files, mods and settings are stored here. You can open this folder any time from Settings.", "wz_dir_l": "Your folder", "wz_terms": "Terms & Privacy", "wz_terms_s": "Please read and accept these before continuing.", "wz_accept": "Accept", "wz_decline": "Decline", "wz_look": "Appearance", "wz_look_s": "Pick a look you like. You can change it any time.", "wz_graph": "Graphite", "wz_mid": "Midnight", "wz_anim": "Animations", "wz_anim_d": "Whether all launcher animations and transitions should be enabled.", "wz_min": "Minimize on launch", "wz_min_d": "Keep the launcher out of the way while the game runs.", "wz_set": "Your setup", "wz_set_s": "Choose how much of the client you want on and how much memory to give it.", "wz_acc": "Sign in", "wz_acc_s": "Use a Microsoft (premium) account for cosmetics and friends, or play offline.", "wz_acc_b": "Sign in", "wz_acc_ok": "Signed in as", "wz_done": "You're all set", "wz_done_s": "Everything is ready. Have fun and good luck in your matches!", "wz_back": "Back", "wz_next": "Next", "wz_finish": "Start playing", "wz_skip": "Skip", "wz_loc": "localized", "wz_t1": "YA CLIENT is a free Minecraft client. It is not affiliated with Mojang or Microsoft.", "wz_t2": "The launcher downloads game files from Mojang and stores them on your computer.", "wz_t3": "We only keep what is needed for cosmetics, friends and YA Plus: your Minecraft name and UUID. We never see your Microsoft password.", "wz_t4": "You can sign out at any time from Settings."});
Object.assign(I18N.it, {"wz_wel": "Benvenuto in YA CLIENT", "wz_wel_s": "Prepariamo tutto. Ci vuole solo un minuto.", "wz_lang": "Scegli la tua lingua", "wz_dir": "Dove deve stare YA CLIENT?", "wz_dir_s": "File di gioco, mod e impostazioni sono salvati qui. Puoi aprire questa cartella quando vuoi dalle Impostazioni.", "wz_dir_l": "La tua cartella", "wz_terms": "Termini e Privacy", "wz_terms_s": "Leggi e accetta prima di continuare.", "wz_accept": "Accetta", "wz_decline": "Rifiuta", "wz_look": "Aspetto", "wz_look_s": "Scegli lo stile che preferisci. Puoi cambiarlo quando vuoi.", "wz_graph": "Grafite", "wz_mid": "Mezzanotte", "wz_anim": "Animazioni", "wz_anim_d": "Attiva tutte le animazioni e transizioni del launcher.", "wz_min": "Riduci all'avvio", "wz_min_d": "Tiene il launcher fuori dai piedi mentre giochi.", "wz_set": "Il tuo setup", "wz_set_s": "Scegli quante funzioni vuoi attive e quanta memoria dare al gioco.", "wz_acc": "Accedi", "wz_acc_s": "Usa un account Microsoft (premium) per cosmetici e amici, oppure gioca offline.", "wz_acc_b": "Accedi", "wz_acc_ok": "Accesso come", "wz_done": "Tutto pronto", "wz_done_s": "È tutto a posto. Buon divertimento e buona fortuna nei match!", "wz_back": "Indietro", "wz_next": "Avanti", "wz_finish": "Inizia a giocare", "wz_skip": "Salta", "wz_loc": "tradotto", "wz_t1": "YA CLIENT è un client Minecraft gratuito. Non è affiliato a Mojang o Microsoft.", "wz_t2": "Il launcher scarica i file di gioco da Mojang e li salva sul tuo computer.", "wz_t3": "Conserviamo solo ciò che serve per cosmetici, amici e YA Plus: nome Minecraft e UUID. Non vediamo mai la tua password Microsoft.", "wz_t4": "Puoi uscire dall'account in qualsiasi momento dalle Impostazioni."});
Object.assign(I18N.es, {"wz_wel": "Bienvenido a YA CLIENT", "wz_wel_s": "Vamos a prepararlo todo. Solo toma un minuto.", "wz_lang": "Elige tu idioma", "wz_dir": "¿Dónde debe vivir YA CLIENT?", "wz_dir_s": "Tus archivos de juego, mods y ajustes se guardan aquí. Puedes abrir esta carpeta cuando quieras desde Ajustes.", "wz_dir_l": "Tu carpeta", "wz_terms": "Términos y Privacidad", "wz_terms_s": "Léelos y acéptalos antes de continuar.", "wz_accept": "Aceptar", "wz_decline": "Rechazar", "wz_look": "Apariencia", "wz_look_s": "Elige el estilo que prefieras. Puedes cambiarlo cuando quieras.", "wz_graph": "Grafito", "wz_mid": "Medianoche", "wz_anim": "Animaciones", "wz_anim_d": "Activa todas las animaciones y transiciones del launcher.", "wz_min": "Minimizar al iniciar", "wz_min_d": "Mantiene el launcher fuera del camino mientras juegas.", "wz_set": "Tu configuración", "wz_set_s": "Elige cuántas funciones quieres activas y cuánta memoria dar al juego.", "wz_acc": "Iniciar sesión", "wz_acc_s": "Usa una cuenta Microsoft (premium) para cosméticos y amigos, o juega sin conexión.", "wz_acc_b": "Iniciar sesión", "wz_acc_ok": "Sesión iniciada como", "wz_done": "Todo listo", "wz_done_s": "Todo está listo. ¡Diviértete y suerte en tus partidas!", "wz_back": "Atrás", "wz_next": "Siguiente", "wz_finish": "Empezar a jugar", "wz_skip": "Omitir", "wz_loc": "traducido", "wz_t1": "YA CLIENT es un cliente de Minecraft gratuito. No está afiliado a Mojang ni a Microsoft.", "wz_t2": "El launcher descarga los archivos del juego de Mojang y los guarda en tu equipo.", "wz_t3": "Solo guardamos lo necesario para cosméticos, amigos y YA Plus: tu nombre de Minecraft y UUID. Nunca vemos tu contraseña de Microsoft.", "wz_t4": "Puedes cerrar sesión cuando quieras desde Ajustes."});
Object.assign(I18N.de, {"wz_wel": "Willkommen bei YA CLIENT", "wz_wel_s": "Wir richten alles ein. Das dauert nur eine Minute.", "wz_lang": "Wähle deine Sprache", "wz_dir": "Wo soll YA CLIENT liegen?", "wz_dir_s": "Spieldateien, Mods und Einstellungen werden hier gespeichert. Den Ordner kannst du jederzeit in den Einstellungen öffnen.", "wz_dir_l": "Dein Ordner", "wz_terms": "Bedingungen & Datenschutz", "wz_terms_s": "Bitte lesen und akzeptieren, bevor es weitergeht.", "wz_accept": "Akzeptieren", "wz_decline": "Ablehnen", "wz_look": "Aussehen", "wz_look_s": "Wähle einen Stil, der dir gefällt. Du kannst ihn jederzeit ändern.", "wz_graph": "Graphit", "wz_mid": "Mitternacht", "wz_anim": "Animationen", "wz_anim_d": "Alle Animationen und Übergänge des Launchers aktivieren.", "wz_min": "Beim Start minimieren", "wz_min_d": "Hält den Launcher aus dem Weg, während du spielst.", "wz_set": "Dein Setup", "wz_set_s": "Wähle, wie viele Funktionen aktiv sein sollen und wie viel Speicher das Spiel bekommt.", "wz_acc": "Anmelden", "wz_acc_s": "Nutze ein Microsoft-Konto (Premium) für Kosmetik und Freunde oder spiele offline.", "wz_acc_b": "Anmelden", "wz_acc_ok": "Angemeldet als", "wz_done": "Alles bereit", "wz_done_s": "Alles ist eingerichtet. Viel Spaß und viel Glück in deinen Matches!", "wz_back": "Zurück", "wz_next": "Weiter", "wz_finish": "Jetzt spielen", "wz_skip": "Überspringen", "wz_loc": "übersetzt", "wz_t1": "YA CLIENT ist ein kostenloser Minecraft-Client. Er steht in keiner Verbindung zu Mojang oder Microsoft.", "wz_t2": "Der Launcher lädt Spieldateien von Mojang herunter und speichert sie auf deinem Computer.", "wz_t3": "Wir speichern nur, was für Kosmetik, Freunde und YA Plus nötig ist: Minecraft-Name und UUID. Dein Microsoft-Passwort sehen wir nie.", "wz_t4": "Du kannst dich jederzeit in den Einstellungen abmelden."});

// ---------- first-run setup wizard ----------
let wzStep = 0;
const WZ = ['wel', 'lang', 'dir', 'terms', 'look', 'set', 'acc', 'done'];
const WZ_ART = {
  wel: '<path d="M12 2.5l2.4 6.1 6.1 2.4-6.1 2.4L12 19.5l-2.4-6.1L3.5 11l6.1-2.4z"/>',
  lang: '<circle cx="12" cy="12" r="9"/><path d="M3 12h18M12 3c2.8 3 2.8 15 0 18M12 3c-2.8 3-2.8 15 0 18"/>',
  dir: '<path d="M3 6.5A2.5 2.5 0 0 1 5.5 4H10l2 2.5h6.5A2.5 2.5 0 0 1 21 9v8.5a2.5 2.5 0 0 1-2.5 2.5h-13A2.5 2.5 0 0 1 3 17.5z"/><path d="M12 10v5m-2.2-2.2L12 15l2.2-2.2"/>',
  terms: '<path d="M7 3h7l4 4v14H7z"/><path d="M14 3v4h4M10 12h5M10 16h5"/>',
  look: '<path d="M12 3a9 9 0 1 0 0 18c1.5 0 2-1 1.5-2-.6-1.2.2-2.5 1.6-2.5H17a4 4 0 0 0 4-4C21 6.6 17 3 12 3z"/><circle cx="8" cy="11" r="1"/><circle cx="12" cy="7.5" r="1"/><circle cx="16" cy="11" r="1"/>',
  set: '<path d="M4 7h10M18 7h2M4 17h2M10 17h10"/><circle cx="16" cy="7" r="2"/><circle cx="8" cy="17" r="2"/>',
  acc: '<circle cx="12" cy="8" r="4"/><path d="M4 21c1-4 4-6 8-6s7 2 8 6"/>',
  done: '<circle cx="12" cy="12" r="9"/><path d="M7.5 12.5l3 3 6-6.5"/>'
};
function wzApplyLook() {
  document.documentElement.dataset.theme = S.theme || 'graphite';
  document.body.classList.toggle('noanim', S.anim === false);
}
function wzBody(k) {
  if (k === 'wel') return `<h1>${esc(t('wz_wel'))}</h1><p class="wzs">${esc(t('wz_wel_s'))}</p>`;
  if (k === 'lang') return `<h1>YA CLIENT</h1><p class="wzs">${esc(t('wz_wel_s'))}</p><h3>${esc(t('wz_lang'))}</h3><div class="wzl">${Object.keys(LANGS).map((c) => `<button class="wzo ${c === S.lang ? 'on' : ''}" data-wl="${c}"><b>${esc(LANGS[c])}</b><small>${I18N[c] && c !== 'en' ? '' : '100%'} ${esc(t('wz_loc'))}</small></button>`).join('')}</div>`;
  if (k === 'dir') return `<h1>${esc(t('wz_dir'))}</h1><p class="wzs">${esc(t('wz_dir_s'))}</p><div class="wzo on static"><b>${esc(t('wz_dir_l'))}</b><small>${esc((S.dir || '%APPDATA%\\.yaclient') + '\\game')}</small><i>${ic('check')}</i></div>`;
  if (k === 'terms') return `<h1>${esc(t('wz_terms'))}</h1><p class="wzs">${esc(t('wz_terms_s'))}</p><div class="wzdoc">${['wz_t1', 'wz_t2', 'wz_t3', 'wz_t4'].map((x) => `<p>${esc(t(x))}</p>`).join('')}</div>`;
  if (k === 'look') return `<h1>${esc(t('wz_look'))}</h1><p class="wzs">${esc(t('wz_look_s'))}</p>
    <div class="wzth">${[['graphite', 'wz_graph'], ['midnight', 'wz_mid']].map(([id, l]) => `<button class="wzt ${(S.theme || 'graphite') === id ? 'on' : ''}" data-th="${id}"><span class="pv ${id}"><i></i><i></i></span><b>${esc(t(l))}</b></button>`).join('')}</div>
    ${wzSw('anim', 'wz_anim', 'wz_anim_d', S.anim !== false)}${wzSw('min', 'wz_min', 'wz_min_d', !!S.minimizeOnLaunch)}`;
  if (k === 'set') return `<h1>${esc(t('wz_set'))}</h1><p class="wzs">${esc(t('wz_set_s'))}</p>
    <div class="wzth three">${['pvp', 'all', 'min'].map((id) => `<button class="wzt ${obPreset === id ? 'on' : ''}" data-wp="${id}"><b>${esc(t('pre_' + id))}</b></button>`).join('')}</div>
    <div class="wzram"><div><b>RAM</b><span id="wzRamV">${S.ram} MB</span></div><input type="range" id="wzRam" min="1024" max="8192" step="512" value="${S.ram || 2048}"></div>`;
  if (k === 'acc') return `<h1>${esc(t('wz_acc'))}</h1><p class="wzs">${esc(t('wz_acc_s'))}</p>${S.account ? `<div class="wzo on static"><b>${esc(t('wz_acc_ok'))} ${esc(S.account.name)}</b><i>${ic('check')}</i></div>` : `<button class="btn hi" id="wzLogin">${esc(t('wz_acc_b'))}</button>`}`;
  return `<h1>${esc(t('wz_done'))}</h1><p class="wzs">${esc(t('wz_done_s'))}</p>`;
}
const wzSw = (id, a, b, on) => `<div class="wzrow"><div><b>${esc(t(a))}</b><small>${esc(t(b))}</small></div><button class="sw ${on ? 'on' : ''}" data-ws="${id}" aria-label="${esc(t(a))}"></button></div>`;
function showSetup(step) {
  wzStep = step || 0; const w = $('#wz'); w.hidden = false; drawSetup(true);
}
function drawSetup(first) {
  const w = $('#wz'); const k = WZ[wzStep]; const center = k === 'wel' || k === 'done';
  const last = wzStep === WZ.length - 1;
  const next = k === 'terms' ? `<button class="btn" data-wa="decline">${esc(t('wz_decline'))} ×</button><button class="btn hi" data-wa="next">${esc(t('wz_accept'))} ✓</button>`
    : `<button class="btn hi" data-wa="${last ? 'finish' : 'next'}">${esc(t(last ? 'wz_finish' : 'wz_next'))} →</button>`;
  w.innerHTML = `<div class="wzp"><i style="width:${Math.round((wzStep + 1) / WZ.length * 100)}%"></i></div>
    <header class="wzh"><img src="../assets/logo.png" alt=""><b>YA CLIENT</b><div class="top-r"><button class="wb" data-w="min" aria-label="Minimize">&#8211;</button><button class="wb x" data-w="close" aria-label="Close">&#10005;</button></div></header>
    <div class="wzm ${center ? 'c' : ''}" key="${k}"><div class="wza"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round">${WZ_ART[k]}</svg></div><div class="wzc">${wzBody(k)}</div></div>
    <footer class="wzf"><span>YA CLIENT · yaprojects.it</span><div>${wzStep > 0 ? `<button class="btn" data-wa="back">${esc(t('wz_back'))}</button>` : ''}${next}</div></footer>`;
}
function endSetup() {
  S.frDone = true; window.ya.saveSettings({ frDone: true, theme: S.theme || 'graphite', anim: S.anim !== false, minimizeOnLaunch: !!S.minimizeOnLaunch, ram: S.ram });
  const w = $('#wz'); w.hidden = true; w.innerHTML = ''; renderNav(); render();
}
document.addEventListener('click', async (e) => {
  const el = e.target.closest('#wz [data-wa],#wz [data-wl],#wz [data-th],#wz [data-ws],#wz [data-wp],#wz #wzLogin');
  if (!el) return;
  e.stopPropagation();
  if (el.dataset.wl) { S.lang = el.dataset.wl; await window.ya.setLang(S.lang); window.ya.saveSettings({ lang: S.lang }); document.documentElement.lang = S.lang; return drawSetup(); }
  if (el.dataset.th) { S.theme = el.dataset.th; wzApplyLook(); return drawSetup(); }
  if (el.dataset.ws === 'anim') { S.anim = S.anim === false; wzApplyLook(); return drawSetup(); }
  if (el.dataset.ws === 'min') { S.minimizeOnLaunch = !S.minimizeOnLaunch; return drawSetup(); }
  if (el.dataset.wp) { obPreset = el.dataset.wp; await applyProfile((m) => PRESETS[obPreset](m)); return drawSetup(); }
  if (el.id === 'wzLogin') { showLogin(); return; }
  const a = el.dataset.wa;
  if (a === 'next') { wzStep++; return drawSetup(); }
  if (a === 'back') { wzStep--; return drawSetup(); }
  if (a === 'decline') return window.ya.win('close');
  if (a === 'finish') return endSetup();
}, true);
document.addEventListener('input', (e) => { if (e.target.id === 'wzRam') { S.ram = +e.target.value; $('#wzRamV').textContent = S.ram + ' MB'; } });

(async function init() {
  S = { ...S, ...(await window.ya.getState()) };
  if (!I18N[S.lang]) S.lang = 'en';
  try { C = await window.ya.content(); } catch (_) { C = {}; }
  try { MODSTATE = await window.ya.modsGet(); } catch (_) { MODSTATE = {}; }
  try { MODS = (await window.ya.mods()).mods || []; } catch (_) { MODS = []; }
  try { VERS = await window.ya.versions(); } catch (_) { }
  window.ya.art().then((a) => { ART = a || {}; if (page === 'versions') render(); }).catch(() => {});
  $('#palLbl').textContent = t('pal_ph').split(',')[0].replace('...', '') + '...';
  renderNav(); render();
  wzApplyLook();
  if (!S.frDone && !S.account) showSetup(); else if (!S.account) showLogin();
  refreshPlus();
  window.addEventListener('focus', () => { if (Date.now() - (S._pt || 0) > 60000) { S._pt = Date.now(); refreshPlus(); } });
})();
