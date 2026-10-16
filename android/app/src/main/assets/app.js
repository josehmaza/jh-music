/**
 * JH Car Audio - Hyundai Tucson ix35
 * Lógica del reproductor de música para auto (simulación web interactiva)
 */

function initApp() {

  // =================== ESTADO INICIAL Y DATOS ===================
  // Canciones de muestra basadas exactamente en las capturas de Hernan
  const sampleTracks = [
    {
      id: 1,
      title: "Me Emborracharé (Extended Club DJ Remix Official Edition)",
      artist: "Grupo Extra Ft. Romeo Santos & Bachata All Stars",
      album: "Blanco Deluxe",
      folder: "JH PENDRIVE",
      path: "/storage/udisk1/JH PENDRIVE/",
      duration: "04:32",
      durationSec: 272,
      format: "MP3 320K",
      favorite: true,
      audioUrl: null // Se genera sintetizador de audio de prueba si no hay archivo
    },
    {
      id: 2,
      title: "Guaracadita (Ultra Bass Zapateo Remix 2024 - Especial Tucson ix35)",
      artist: "DJ Fuego & DJ Master Latino",
      album: "Guaracha Electro 2024",
      folder: "Descargas",
      path: "/storage/udisk1/Descargas/",
      duration: "03:15",
      durationSec: 195,
      format: "MP3 320K",
      favorite: true,
      audioUrl: null
    },
    {
      id: 3,
      title: "Guaro RMX INTRO DJ (MegaMix Fin de Semana en Vivo Party Edition)",
      artist: "Varios Artistas & Invitados Especiales",
      album: "Remixes Fin de Semana",
      folder: "Descargas",
      path: "/storage/udisk1/Descargas/",
      duration: "03:45",
      durationSec: 225,
      format: "MP3 320K",
      favorite: false,
      audioUrl: null
    },
    {
      id: 4,
      title: "Mosaico Nacional (Senderito de Amor / La Bocina / Chola Cuencana / Vasija de Barro)",
      artist: "Héctor Jaramillo & Los Embajadores del Recuerdo",
      album: "Clásicos del Ayer",
      folder: "2025-07-18",
      path: "/storage/udisk1/Descargas/2025-07-18/",
      duration: "05:12",
      durationSec: 312,
      format: "MP3 256K",
      favorite: false,
      audioUrl: null
    },
    {
      id: 5,
      title: "Veneno de Amor (Versión Especial Remasterizada 2024)",
      artist: "Hipátia Balseca",
      album: "Grandes Éxitos",
      folder: "JH PENDRIVE",
      path: "/storage/udisk1/JH PENDRIVE/",
      duration: "03:32",
      durationSec: 212,
      format: "MP3 320K",
      favorite: true,
      audioUrl: null
    },
    {
      id: 6,
      title: "La Bicicleta (Tropical Party MegaMix Fiesta Latina Concierto en Vivo)",
      artist: "Carlos Vives, Shakira & Maluma",
      album: "Hits Latinos 2024",
      folder: "JH PENDRIVE",
      path: "/storage/udisk1/JH PENDRIVE/",
      duration: "04:15",
      durationSec: 255,
      format: "MP3 320K",
      favorite: false,
      audioUrl: null
    },
    {
      id: 7,
      title: "Vivir Mi Vida (En Vivo Estadio Latinoamericano Tour)",
      artist: "Marc Anthony",
      album: "Salsa Gigantes 2024",
      folder: "Música Viaje",
      path: "/storage/udisk2/Música Viaje/",
      duration: "04:10",
      durationSec: 250,
      format: "MP3 320K",
      favorite: true,
      audioUrl: null
    },
    {
      id: 8,
      title: "Cumbia del Sol (Edición Radio Estéreo)",
      artist: "Los Ángeles de la Cumbia",
      album: "Cumbias Inmortales",
      folder: "Música Interna",
      path: "/mnt/sdcard/media/music/",
      duration: "03:40",
      durationSec: 220,
      format: "MP3 320K",
      favorite: false,
      audioUrl: null
    },
    {
      id: 9,
      title: "Parranda Vallenata (Fiesta en mi Pueblo)",
      artist: "Silvestre Dangond",
      album: "Vallenatos de Oro",
      folder: "Sub subcarpeta 1,1",
      path: "/storage/udisk1/Descargas/Subcarpeta 1/Sub subcarpeta 1,1/",
      duration: "03:50",
      durationSec: 230,
      format: "MP3 320K",
      favorite: true,
      audioUrl: null
    },
    {
      id: 10,
      title: "La Rebelión (No Le Pegue a la Negra)",
      artist: "Joe Arroyo & La Verdad",
      album: "Salsa Colombiana Inmortal",
      folder: "Sub subcarpeta 1,1",
      path: "/storage/udisk1/Descargas/Subcarpeta 1/Sub subcarpeta 1,1/",
      duration: "04:12",
      durationSec: 252,
      format: "MP3 320K",
      favorite: false,
      audioUrl: null
    },
    {
      id: 11,
      title: "Colegiala (Cumbia Tropical Ecuatoriana)",
      artist: "Rodolfo Aicardi y su Típica",
      album: "Cumbias Bailables",
      folder: "Sub subcarpeta 1,1",
      path: "/storage/udisk1/Descargas/Subcarpeta 1/Sub subcarpeta 1,1/",
      duration: "03:28",
      durationSec: 208,
      format: "MP3 320K",
      favorite: true,
      audioUrl: null
    },
    {
      id: 12,
      title: "El Aguacate (Pasillo Tradicional Ecuatoriano)",
      artist: "Julio Jaramillo",
      album: "Alma Llanera y Pasillos",
      folder: "Sub subcarpeta 2",
      path: "/storage/udisk1/Descargas/Subcarpeta 1/Sub subcarpeta 2/",
      duration: "03:05",
      durationSec: 185,
      format: "MP3 320K",
      favorite: true,
      audioUrl: null
    },
    {
      id: 13,
      title: "Guayaquil de mis Amores (Vals Ecuatoriano Clásico)",
      artist: "Olimpo Cárdenas",
      album: "Joyas del Pasillo",
      folder: "Sub subcarpeta 2",
      path: "/storage/udisk1/Descargas/Subcarpeta 1/Sub subcarpeta 2/",
      duration: "02:58",
      durationSec: 178,
      format: "MP3 320K",
      favorite: false,
      audioUrl: null
    },
    {
      id: 14,
      title: "Sendas Distintas (Acoustic Guitar Pasillo)",
      artist: "Carlota Jaramillo & Dúo Benítez Valencia",
      album: "Nostalgias de los Andes",
      folder: "Sub subcarpeta 2",
      path: "/storage/udisk1/Descargas/Subcarpeta 1/Sub subcarpeta 2/",
      duration: "03:22",
      durationSec: 202,
      format: "MP3 320K",
      favorite: false,
      audioUrl: null
    },
    {
      id: 15,
      title: "Mix Chicha Ecuatoriana (En Vivo desde Loja)",
      artist: "Don Medardo y sus Players",
      album: "Cumbia y Chicha 2024",
      folder: "SUbcarpet de prueba jh nombre largo de carpetaa 1,1.1",
      path: "/storage/udisk1/Descargas/Subcarpeta 1/Sub subcarpeta 1,1/SUbcarpet de prueba jh nombre largo de carpetaa 1,1.1/",
      duration: "04:35",
      durationSec: 275,
      format: "MP3 320K",
      favorite: true,
      audioUrl: null
    },
    {
      id: 16,
      title: "San Juanito Alegre (Fiesta en Cayambe)",
      artist: "Los Corazas",
      album: "Folklore Andino",
      folder: "SUbcarpet de prueba jh nombre largo de carpetaa 1,1.1",
      path: "/storage/udisk1/Descargas/Subcarpeta 1/Sub subcarpeta 1,1/SUbcarpet de prueba jh nombre largo de carpetaa 1,1.1/",
      duration: "03:10",
      durationSec: 190,
      format: "MP3 320K",
      favorite: false,
      audioUrl: null
    }
  ];

  // Helper de ordenamiento alfabético natural para títulos de pistas (A-Z)
  function sortTracksAlphabetically(trackList) {
    if (!trackList || !Array.isArray(trackList)) return trackList;
    return trackList.sort((a, b) => {
      const t1 = (a.title || a.name || '').trim();
      const t2 = (b.title || b.name || '').trim();
      return t1.localeCompare(t2, 'es', { numeric: true, sensitivity: 'base' });
    });
  }

  // Ordenar canciones de muestra alfabéticamente por título
  sortTracksAlphabetically(sampleTracks);

  const foldersList = [
    { name: "JH PENDRIVE", path: "/storage/udisk1/JH PENDRIVE/", count: 12, usbSource: "USB 1" },
    { name: "Descargas", path: "/storage/udisk1/Descargas/", count: 2, usbSource: "USB 1" },
    { name: "Subcarpeta 1", path: "/storage/udisk1/Descargas/Subcarpeta 1/", count: 0, usbSource: "USB 1" },
    { name: "Sub subcarpeta 1,1", path: "/storage/udisk1/Descargas/Subcarpeta 1/Sub subcarpeta 1,1/", count: 3, usbSource: "USB 1" },
    { name: "SUbcarpet de prueba jh nombre largo de carpetaa 1,1.1", path: "/storage/udisk1/Descargas/Subcarpeta 1/Sub subcarpeta 1,1/SUbcarpet de prueba jh nombre largo de carpetaa 1,1.1/", count: 2, usbSource: "USB 1" },
    { name: "Sub subcarpeta 2", path: "/storage/udisk1/Descargas/Subcarpeta 1/Sub subcarpeta 2/", count: 3, usbSource: "USB 1" },
    { name: "2025-07-18", path: "/storage/udisk1/Descargas/2025-07-18/", count: 29, usbSource: "USB 1" },
    { name: "Música Viaje", path: "/storage/udisk2/Música Viaje/", count: 84, usbSource: "USB 2" },
    { name: "Música Interna", path: "/mnt/sdcard/media/music/", count: 5, usbSource: "INTERNA" }
  ];

  // Estructura de Árbol Jerárquico de Carpetas USB (Maqueta Interactiva de Hernan)
  const defaultTreeFolders = [
    {
      id: "usb1",
      name: "USB 1 (udisk1)",
      isUsbRoot: true,
      usbSource: "USB 1",
      path: "/storage/udisk1/",
      count: 247,
      directCount: 0,
      children: [
        {
          id: "descargas",
          name: "Descargas",
          usbSource: "USB 1",
          path: "/storage/udisk1/Descargas/",
          count: 218,
          directCount: 2,
          children: [
            {
              id: "subcarpeta1",
              name: "Subcarpeta 1",
              usbSource: "USB 1",
              path: "/storage/udisk1/Descargas/Subcarpeta 1/",
              count: 8,
              directCount: 0,
              children: [
                {
                  id: "subsub1_1",
                  name: "Sub subcarpeta 1,1",
                  usbSource: "USB 1",
                  path: "/storage/udisk1/Descargas/Subcarpeta 1/Sub subcarpeta 1,1/",
                  count: 5,
                  directCount: 3,
                  children: [
                    {
                      id: "subsub1_1_1_deep",
                      name: "SUbcarpet de prueba jh nombre largo de carpetaa 1,1.1",
                      usbSource: "USB 1",
                      path: "/storage/udisk1/Descargas/Subcarpeta 1/Sub subcarpeta 1,1/SUbcarpet de prueba jh nombre largo de carpetaa 1,1.1/",
                      count: 2,
                      directCount: 2,
                      children: []
                    }
                  ]
                },
                {
                  id: "subsub2",
                  name: "Sub subcarpeta 2",
                  usbSource: "USB 1",
                  path: "/storage/udisk1/Descargas/Subcarpeta 1/Sub subcarpeta 2/",
                  count: 3,
                  directCount: 3,
                  children: []
                }
              ]
            },
            {
              id: "descargas_fecha",
              name: "2025-07-18",
              usbSource: "USB 1",
              path: "/storage/udisk1/Descargas/2025-07-18/",
              count: 29,
              directCount: 29,
              children: []
            }
          ]
        },
        {
          id: "jh_pendrive",
          name: "JH PENDRIVE",
          usbSource: "USB 1",
          path: "/storage/udisk1/JH PENDRIVE/",
          count: 12,
          directCount: 12,
          children: []
        }
      ]
    },
    {
      id: "usb2",
      name: "USB 2 (udisk2)",
      isUsbRoot: true,
      usbSource: "USB 2",
      path: "/storage/udisk2/",
      count: 84,
      directCount: 0,
      children: [
        {
          id: "musica_viaje",
          name: "Música Viaje",
          usbSource: "USB 2",
          path: "/storage/udisk2/Música Viaje/",
          count: 84,
          directCount: 84,
          children: []
        }
      ]
    }
  ];

  let currentTreeData = JSON.parse(JSON.stringify(defaultTreeFolders));
  // Nodos colapsados por defecto para que el usuario los vaya abriendo según su gusto
  let expandedTreeNodes = new Set();

  let masterTracks = [...sampleTracks];
  let currentPlaylist = [...sampleTracks];
  let currentTrackIndex = 0;
  let isPlaying = false;
  let isShuffle = false;
  let isRepeat = false;
  let currentVolume = 75;
  let previousVolume = 75;
  let isMuted = false;
  let activeTab = "folders"; // "folders", "playlist", "favorites"
  let currentFolder = "Descargas";
  let currentFolderUsbSource = "USB 1";

  // Audio HTML5 y Sintetizador Web Audio
  const audio = document.getElementById('audioPlayer');
  let audioContext = null;
  let synthOscillator = null;
  let synthGain = null;
  let simInterval = null;
  let simCurrentSec = 0;

  // =================== ELEMENTOS DEL DOM ===================
  const frameWrapper = document.getElementById('frameWrapper');
  const clockDisplay = document.getElementById('clockDisplay');
  const driverModeBadge = document.getElementById('driverModeBadge');
  const playbackStatusText = document.getElementById('playbackStatusText');
  const trackTitle = document.getElementById('trackTitle');
  const trackArtist = document.getElementById('trackArtist');
  const trackAlbum = document.getElementById('trackAlbum');
  const formatBadge = document.getElementById('formatBadge');
  const albumContainer = document.getElementById('albumContainer');
  const albumArt = document.getElementById('albumArt');
  const albumPlaceholder = document.getElementById('albumPlaceholder');
  const currentPathLabel = document.getElementById('currentPathLabel');
  
  // Progreso
  const progressBarContainer = document.getElementById('progressBarContainer');
  const progressCurrent = document.getElementById('progressCurrent');
  const currentTimeEl = document.getElementById('currentTime');
  const totalDurationEl = document.getElementById('totalDuration');

  // Botones de Reproducción
  const btnPlayPause = document.getElementById('btnPlayPause');
  const playIcon = document.getElementById('playIcon');
  const pauseIcon = document.getElementById('pauseIcon');
  const btnPrev = document.getElementById('btnPrev');
  const btnNext = document.getElementById('btnNext');
  const btnShuffle = document.getElementById('btnShuffle');
  const btnRepeat = document.getElementById('btnRepeat');

  // Modo Reproductor Expandido / Buscador de Archivos
  const dashboardGrid = document.querySelector('.dashboard-grid');
  const btnToggleExpand = document.getElementById('btnToggleExpand');
  const btnAutoMix = document.getElementById('btnAutoMix');

  // =================== AUTOMIX (simulado en la maqueta) ===================
  // Misma regla que la app nativa: mezcla de 5 s, 2 s en mixes de 10 min o más,
  // sin mezcla en canciones cortas (menos de 3 veces la mezcla) ni con Repetir.
  const AUTOMIX_SEC = 5;
  const AUTOMIX_LONG_TRACK_SEC = 600;
  const AUTOMIX_LONG_TRACK_MIX_SEC = 2;
  let isAutoMix = false;
  let isMixing = false;
  let mixEndTimer = null;
  try {
    isAutoMix = localStorage.getItem('jhmusic_automix') === '1';
  } catch (e) {}

  function updateAutoMixButton() {
    if (!btnAutoMix) return;
    btnAutoMix.classList.toggle('is-active', isAutoMix);
    btnAutoMix.classList.toggle('is-mixing', isMixing);
    btnAutoMix.title = isAutoMix ? "Automix activado: toca para desactivar" : "Automix: mezcla automática entre canciones";
  }

  function autoMixSecondsFor(current, next) {
    if (!current || !next) return 0;
    let mix = AUTOMIX_SEC;
    if (current.durationSec >= AUTOMIX_LONG_TRACK_SEC) mix = Math.min(mix, AUTOMIX_LONG_TRACK_MIX_SEC);
    if (current.durationSec < mix * 3 || next.durationSec < mix * 3) return 0;
    return mix;
  }

  /** Siguiente para mezclar: en aleatorio se sortea ahora y nunca es la misma canción. */
  function pickAutoMixNextIndex() {
    const n = currentPlaylist.length;
    if (!isShuffle) return (currentTrackIndex + 1) % n;
    let index = Math.floor(Math.random() * (n - 1));
    if (index >= currentTrackIndex) index++;
    return index;
  }

  function startAutoMix(nextIndex, mixSec) {
    // Como en la app: la canción nueva aparece al empezar la mezcla
    loadTrack(nextIndex, true);
    isMixing = true;
    clearTimeout(mixEndTimer);
    mixEndTimer = setTimeout(() => {
      isMixing = false;
      updateAutoMixButton();
      updatePlayStateUI();
    }, mixSec * 1000);
    updateAutoMixButton();
    updatePlayStateUI();
  }

  function cancelAutoMix() {
    if (!isMixing) return;
    clearTimeout(mixEndTimer);
    isMixing = false;
    updateAutoMixButton();
  }

  if (btnAutoMix) {
    updateAutoMixButton();
    btnAutoMix.addEventListener('click', () => {
      isAutoMix = !isAutoMix;
      if (!isAutoMix) cancelAutoMix();
      try {
        localStorage.setItem('jhmusic_automix', isAutoMix ? '1' : '0');
      } catch (e) {}
      updateAutoMixButton();
      updatePlayStateUI();
    });
  }

  // Previsualización de Canción Anterior y Posterior
  const adjPrevCard = document.getElementById('adjPrevCard');
  const prevTrackTitle = document.getElementById('prevTrackTitle');
  const adjNextCard = document.getElementById('adjNextCard');
  const nextTrackTitle = document.getElementById('nextTrackTitle');

  // Controles de Volumen (Solución a botones apagados de noche)
  const volPercent = document.getElementById('volPercent');
  const volRange = document.getElementById('volRange');
  const btnVolDown = document.getElementById('btnVolDown');
  const btnVolUp = document.getElementById('btnVolUp');
  const btnMute = document.getElementById('btnMute');
  const iconUnmuted = document.getElementById('iconUnmuted');
  const iconMuted = document.getElementById('iconMuted');

  // Explorador y Pestañas
  const tabButtons = document.querySelectorAll('.tab-btn');
  const trackListContainer = document.getElementById('trackListContainer');
  const songCountEl = document.getElementById('songCount');
  const searchInput = document.getElementById('searchInput');
  const clearSearch = document.getElementById('clearSearch');
  const searchSpinner = document.getElementById('searchSpinner');
  const searchOverlayLoading = document.getElementById('searchOverlayLoading');
  const crumbRoot = document.getElementById('crumbRoot');
  const crumbFolder = document.getElementById('crumbFolder');
  const fileInput = document.getElementById('fileInput');
  const btnToggleFullscreen = document.getElementById('btnToggleFullscreen');
  const treeActions = document.getElementById('treeActions');
  const btnCollapseAllTree = document.getElementById('btnCollapseAllTree');

  // =================== RELOJ DE AUTO ===================
  function updateClock() {
    const now = new Date();
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    clockDisplay.textContent = `${hours}:${minutes}`;
  }
  setInterval(updateClock, 1000);
  updateClock();

  // =================== CARGA Y REPRODUCCIÓN ===================
  function loadTrack(index, autoPlay = true) {
    if (index < 0) index = currentPlaylist.length - 1;
    if (index >= currentPlaylist.length) index = 0;
    currentTrackIndex = index;
    const track = currentPlaylist[currentTrackIndex];
    if (!track) return;

    // Actualizar UI
    trackTitle.textContent = track.title;
    trackArtist.textContent = track.artist;
    trackAlbum.textContent = track.album || track.folder;
    formatBadge.textContent = (track.format && track.format.includes('K')) ? track.format : `${track.format || 'MP3'} 320K`;
    totalDurationEl.textContent = track.duration;
    currentTimeEl.textContent = "00:00";
    progressCurrent.style.width = "0%";
    simCurrentSec = 0;

    // Carátula
    if (track.coverUrl) {
      albumArt.src = track.coverUrl;
      albumArt.classList.remove('hidden');
      albumPlaceholder.classList.add('hidden');
    } else {
      albumArt.classList.add('hidden');
      albumPlaceholder.classList.remove('hidden');
    }

    // Mensaje de saludo en cabecera
    currentPathLabel.textContent = "Q hubo jefe 😉";

    // Fuente de Audio
    if (window.AndroidBridge && window.AndroidBridge.playNativeAudio && track.filePath) {
      stopSimulatedAudio();
      audio.pause();
      if (autoPlay) playAudio();
    } else if (track.audioUrl) {
      stopSimulatedAudio();
      audio.src = track.audioUrl;
      audio.volume = isMuted ? 0 : (currentVolume / 100);
      if (autoPlay) playAudio();
    } else {
      // Audio simulado para pruebas
      audio.pause();
      if (autoPlay) startSimulatedAudio();
    }

    // Actualizar Previsualización de canción anterior y siguiente
    if (currentPlaylist.length > 0) {
      const prevIdx = (currentTrackIndex - 1 + currentPlaylist.length) % currentPlaylist.length;
      const nextIdx = (currentTrackIndex + 1) % currentPlaylist.length;
      const prevTrackObj = currentPlaylist[prevIdx];
      const nextTrackObj = currentPlaylist[nextIdx];

      if (prevTrackObj && prevTrackTitle) {
        prevTrackTitle.textContent = `${prevTrackObj.title} • ${prevTrackObj.artist}`;
        if (adjPrevCard) adjPrevCard.setAttribute('title', `Anterior: ${prevTrackObj.title} - ${prevTrackObj.artist}`);
      }
      if (nextTrackObj && nextTrackTitle) {
        nextTrackTitle.textContent = `${nextTrackObj.title} • ${nextTrackObj.artist}`;
        if (adjNextCard) adjNextCard.setAttribute('title', `Siguiente: ${nextTrackObj.title} - ${nextTrackObj.artist}`);
      }
    }

    // Auto-scroll / Marquesina para títulos largos
    refreshAllMarquees();

    renderList();
  }

  // =================== AUTO-SCROLL / MARQUESINA PARA TÍTULOS LARGOS ===================
  function applyMarquee(element, container) {
    if (!element || !container) return;

    // Resetear para recalcular limpio
    element.classList.remove('is-scrolling');
    container.classList.remove('has-overflow');
    element.style.removeProperty('--scroll-dist');
    element.style.removeProperty('--scroll-dur');

    setTimeout(() => {
      const elWidth = element.scrollWidth;
      const containerWidth = container.clientWidth;
      const overflow = elWidth - containerWidth;

      if (overflow > 4) {
        container.classList.add('has-overflow');
        // Velocidad suave automotriz (pausa al inicio, desplazamiento gradual y pausa al final)
        const duration = Math.max(6, Math.min(16, (overflow / 25) + 4));
        element.style.setProperty('--scroll-dist', `-${overflow + 10}px`);
        element.style.setProperty('--scroll-dur', `${duration}s`);
        element.classList.add('is-scrolling');
      } else {
        container.classList.remove('has-overflow');
      }
    }, 80);
  }

  function refreshAllMarquees() {
    applyMarquee(trackTitle, trackTitle ? trackTitle.parentElement : null);
    applyMarquee(prevTrackTitle, prevTrackTitle ? prevTrackTitle.parentElement : null);
    applyMarquee(nextTrackTitle, nextTrackTitle ? nextTrackTitle.parentElement : null);
    
    // Auto-scroll / marquesina en chip de carpeta activa si su nombre es largo
    const chipText = document.getElementById('chipFolderText');
    const chipWrap = chipFilterFolder ? chipFilterFolder.querySelector('.chip-folder-text-wrap') : null;
    if (chipText && chipWrap) {
      applyMarquee(chipText, chipWrap);
    }

    // Auto-scroll / marquesina también para la canción que está sonando en la lista de pistas
    if (trackListContainer) {
      const activeItemTitle = trackListContainer.querySelector('.list-item.is-playing .item-title');
      if (activeItemTitle) {
        applyMarquee(activeItemTitle, activeItemTitle.parentElement);
      }
    }
  }

  let nativeTrackerInterval = null;

  function startNativeAudioTracker() {
    clearInterval(nativeTrackerInterval);
    nativeTrackerInterval = setInterval(() => {
      if (!isPlaying) return;
      if (window.AndroidBridge && window.AndroidBridge.getNativeAudioPosition) {
        const posMs = window.AndroidBridge.getNativeAudioPosition();
        const durMs = window.AndroidBridge.getNativeAudioDuration();
        if (durMs > 0) {
          const percent = (posMs / durMs) * 100;
          progressCurrent.style.width = `${percent}%`;
          currentTimeEl.textContent = formatTime(Math.floor(posMs / 1000));
          totalDurationEl.textContent = formatTime(Math.floor(durMs / 1000));
        }
      }
    }, 400);
  }

  function playAudio() {
    const track = currentPlaylist[currentTrackIndex];
    if (!track) return;

    // Si estamos en la pantalla del auto, usar el MediaPlayer nativo de Android
    if (window.AndroidBridge && window.AndroidBridge.playNativeAudio && track.filePath) {
      window.AndroidBridge.playNativeAudio(track.filePath);
      isPlaying = true;
      updatePlayStateUI();
      startNativeAudioTracker();
      return;
    }

    if (track.audioUrl) {
      audio.play().then(() => {
        isPlaying = true;
        updatePlayStateUI();
      }).catch(err => {
        console.warn("Audio autoplay bloqueado por el navegador:", err);
        startSimulatedAudio();
      });
    } else {
      startSimulatedAudio();
    }
  }

  function pauseAudio() {
    cancelAutoMix(); // pausa en medio de una mezcla: queda solo la canción nueva
    isPlaying = false;
    clearInterval(nativeTrackerInterval);
    if (window.AndroidBridge && window.AndroidBridge.pauseNativeAudio) {
      window.AndroidBridge.pauseNativeAudio();
    }
    audio.pause();
    stopSimulatedAudio();
    updatePlayStateUI();
  }

  function togglePlayPause() {
    if (isPlaying) {
      pauseAudio();
    } else {
      playAudio();
    }
  }

  function updatePlayStateUI() {
    if (isPlaying) {
      playIcon.classList.add('hidden');
      pauseIcon.classList.remove('hidden');
      albumContainer.classList.add('is-playing');
      if (driverModeBadge) {
        driverModeBadge.classList.add('is-playing');
        driverModeBadge.classList.remove('is-paused');
      }
      if (playbackStatusText) {
        playbackStatusText.textContent = isMixing ? "MEZCLANDO ⇄"
          : (isAutoMix ? "REPRODUCIENDO · AUTOMIX" : "REPRODUCIENDO");
      }
    } else {
      playIcon.classList.remove('hidden');
      pauseIcon.classList.add('hidden');
      albumContainer.classList.remove('is-playing');
      if (driverModeBadge) {
        driverModeBadge.classList.remove('is-playing');
        driverModeBadge.classList.add('is-paused');
      }
      if (playbackStatusText) {
        playbackStatusText.textContent = "EN PAUSA";
      }
    }
    renderList();
  }

  // Generador de audio de prueba interactivo (Web Audio API)
  function startSimulatedAudio() {
    isPlaying = true;
    updatePlayStateUI();
    clearInterval(simInterval);

    // Sonido sutil para saber que está sonando algo real en los altavoces
    try {
      if (!audioContext) {
        audioContext = new (window.AudioContext || window.webkitAudioContext)();
      }
      if (audioContext.state === 'suspended') {
        audioContext.resume();
      }
    } catch(e) {}

    simInterval = setInterval(() => {
      if (!isPlaying) return;
      simCurrentSec++;
      const track = currentPlaylist[currentTrackIndex];
      const maxSec = track ? track.durationSec : 240;

      // Automix: al llegar a los últimos segundos, pasar a la siguiente mezclando
      if (isAutoMix && !isMixing && !isRepeat && currentPlaylist.length > 1) {
        const next = currentPlaylist[(currentTrackIndex + 1) % currentPlaylist.length];
        const mixSec = autoMixSecondsFor(track, next);
        if (mixSec > 0 && maxSec - simCurrentSec <= mixSec) {
          startAutoMix(pickAutoMixNextIndex(), mixSec);
          return;
        }
      }

      if (simCurrentSec >= maxSec) {
        simCurrentSec = 0;
        if (isRepeat) {
          loadTrack(currentTrackIndex, true);
        } else {
          nextTrack();
        }
        return;
      }

      const percent = (simCurrentSec / maxSec) * 100;
      progressCurrent.style.width = `${percent}%`;
      currentTimeEl.textContent = formatTime(simCurrentSec);
    }, 1000);
  }

  function stopSimulatedAudio() {
    clearInterval(simInterval);
  }

  function nextTrack() {
    cancelAutoMix();
    if (isShuffle) {
      const rand = Math.floor(Math.random() * currentPlaylist.length);
      loadTrack(rand, true);
    } else {
      loadTrack(currentTrackIndex + 1, true);
    }
  }

  function prevTrack() {
    cancelAutoMix();
    if (simCurrentSec > 3) {
      // Si ya avanzó más de 3 segundos, reiniciar la pista
      simCurrentSec = 0;
      currentTimeEl.textContent = "00:00";
      progressCurrent.style.width = "0%";
      if (audio.src) audio.currentTime = 0;
    } else {
      loadTrack(currentTrackIndex - 1, true);
    }
  }

  function formatTime(seconds) {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
  }

  // Eventos del Audio real si se cargó un archivo
  audio.addEventListener('timeupdate', () => {
    if (!audio.duration) return;
    const percent = (audio.currentTime / audio.duration) * 100;
    progressCurrent.style.width = `${percent}%`;
    currentTimeEl.textContent = formatTime(audio.currentTime);
  });

  audio.addEventListener('ended', () => {
    if (isRepeat) {
      audio.currentTime = 0;
      audio.play();
    } else {
      nextTrack();
    }
  });

  // Salto en la barra de progreso
  progressBarContainer.addEventListener('click', (e) => {
    const rect = progressBarContainer.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const ratio = Math.max(0, Math.min(1, clickX / rect.width));
    const track = currentPlaylist[currentTrackIndex];

    if (window.AndroidBridge && window.AndroidBridge.seekNativeAudio && track && track.filePath) {
      const durMs = window.AndroidBridge.getNativeAudioDuration();
      if (durMs > 0) {
        window.AndroidBridge.seekNativeAudio(Math.floor(ratio * durMs));
        progressCurrent.style.width = `${ratio * 100}%`;
        currentTimeEl.textContent = formatTime(Math.floor((ratio * durMs) / 1000));
      }
    } else if (audio.src && audio.duration) {
      audio.currentTime = ratio * audio.duration;
    } else if (track) {
      simCurrentSec = Math.floor(ratio * track.durationSec);
      progressCurrent.style.width = `${ratio * 100}%`;
      currentTimeEl.textContent = formatTime(simCurrentSec);
    }
  });

  // =================== SISTEMA DE VOLUMEN RÁPIDO (BORDE DEL CONDUCTOR) ===================
  const volMeterWrap = document.getElementById('volMeterWrap');
  const volMeterFill = document.getElementById('volMeterFill');
  const volMeterThumb = document.getElementById('volMeterThumb');

  function setVolume(val) {
    currentVolume = Math.max(0, Math.min(100, Math.round(val)));
    volPercent.textContent = `${currentVolume}%`;
    if (volMeterFill) volMeterFill.style.height = `${currentVolume}%`;
    if (volMeterThumb) volMeterThumb.style.bottom = `${currentVolume}%`;
    
    if (currentVolume === 0) {
      isMuted = true;
    } else {
      isMuted = false;
    }
    updateMuteUI();

    audio.volume = isMuted ? 0 : (currentVolume / 100);

    // Integración directa con el sistema de audio de la pantalla del auto
    if (window.AndroidBridge && window.AndroidBridge.setSystemVolume) {
      try { window.AndroidBridge.setSystemVolume(currentVolume); } catch(e) {}
    }
  }

  function toggleMute() {
    if (isMuted) {
      isMuted = false;
      setVolume(previousVolume > 0 ? previousVolume : 50);
    } else {
      previousVolume = currentVolume;
      isMuted = true;
      audio.volume = 0;
      volPercent.textContent = "0%";
      if (volMeterFill) volMeterFill.style.height = "0%";
      if (volMeterThumb) volMeterThumb.style.bottom = "0%";
    }
    updateMuteUI();

    // Integración mute nativo en auto
    if (window.AndroidBridge && window.AndroidBridge.muteSystemVolume) {
      try { window.AndroidBridge.muteSystemVolume(isMuted); } catch(e) {}
    }
  }

  function updateMuteUI() {
    if (isMuted) {
      btnMute.classList.add('is-muted');
      iconUnmuted.classList.add('hidden');
      iconMuted.classList.remove('hidden');
    } else {
      btnMute.classList.remove('is-muted');
      iconUnmuted.classList.remove('hidden');
      iconMuted.classList.add('hidden');
    }
  }

  // Interacción táctil y de arrastre en la barra vertical
  if (volMeterWrap) {
    let isDraggingVol = false;

    function updateVolFromPointer(e) {
      const rect = volMeterWrap.getBoundingClientRect();
      const clientY = e.clientY;
      const offsetY = clientY - rect.top;
      const ratio = 1 - (offsetY / rect.height);
      const val = Math.max(0, Math.min(100, Math.round(ratio * 100)));
      setVolume(val);
    }

    volMeterWrap.addEventListener('pointerdown', (e) => {
      isDraggingVol = true;
      volMeterWrap.setPointerCapture(e.pointerId);
      updateVolFromPointer(e);
    });

    volMeterWrap.addEventListener('pointermove', (e) => {
      if (isDraggingVol) updateVolFromPointer(e);
    });

    volMeterWrap.addEventListener('pointerup', (e) => {
      isDraggingVol = false;
      try { volMeterWrap.releasePointerCapture(e.pointerId); } catch(err) {}
    });

    volMeterWrap.addEventListener('pointercancel', () => {
      isDraggingVol = false;
    });
  }

  btnVolDown.addEventListener('click', () => setVolume(currentVolume - 5));
  btnVolUp.addEventListener('click', () => setVolume(currentVolume + 5));
  btnMute.addEventListener('click', toggleMute);

  // =================== BOTONES DE NAVEGACIÓN Y CONTROL ===================
  btnPlayPause.addEventListener('click', togglePlayPause);

  // Click en el cuadro grande de la carátula: Pausa / Reproduce rápido (fácil toque en conducción)
  if (albumContainer) {
    albumContainer.style.cursor = 'pointer';
    albumContainer.setAttribute('title', 'Tocar para Pausar / Reproducir');
    albumContainer.addEventListener('click', togglePlayPause);
  }

  btnNext.addEventListener('click', nextTrack);
  btnPrev.addEventListener('click', prevTrack);

  // Clicks directos en las tarjetas de canción anterior y siguiente
  if (adjPrevCard) {
    adjPrevCard.addEventListener('click', prevTrack);
  }
  if (adjNextCard) {
    adjNextCard.addEventListener('click', nextTrack);
  }

  // Alternador de Buscador de Música y Archivos / Modo Reproductor Completo
  let isPlayerExpanded = false;
  // El botón ya no alterna con click directo; se maneja exclusivamente por gesto de arrastre táctil
  if (btnToggleExpand) {
    btnToggleExpand.style.cursor = "grab";
  }

  // =================== ZONA EXCLUSIVA DE GESTOS TÁCTILES / ARRASTRE ===================
  // El área de touch y arrastrar para alternar el explorador se restringe ÚNICAMENTE
  // a la zona delimitada por el botón de explorador y la franja vertical a su lado (área roja)
  let isDragGestureActive = false;
  let touchStartX = 0;
  let touchStartY = 0;

  function isInsideExplorerDragZone(clientX, clientY) {
    if (!btnToggleExpand || !playerPanel) return false;
    const btnRect = btnToggleExpand.getBoundingClientRect();
    const playerRect = playerPanel.getBoundingClientRect();
    const albumContainer = document.getElementById('albumContainer');
    const albumRect = albumContainer ? albumContainer.getBoundingClientRect() : null;

    // Área roja delimitada por Hernan:
    // - Horizontal: Desde el borde izquierdo del panel del reproductor hasta pasar el botón (+20px)
    // - Vertical: Desde la parte superior del botón (-10px) hasta el fondo de la carátula (+10px)
    const minX = Math.max(0, Math.min(playerRect.left, btnRect.left - 15));
    const maxX = btnRect.right + 22;
    const minY = Math.max(0, btnRect.top - 10);
    const maxY = albumRect ? (albumRect.bottom + 10) : (btnRect.bottom + 200);

    return clientX >= minX && clientX <= maxX && clientY >= minY && clientY <= maxY;
  }

  function handleSwipeAction(dx, dy) {
    if (Math.abs(dx) < 35 || Math.abs(dx) < Math.abs(dy) * 1.1) return;

    // Con el explorador en la izquierda:
    // Si el explorador está ABIERTO: arrastrar hacia la izquierda (<-) lo CIERRA (expande reproductor)
    if (!isPlayerExpanded && dx < -35) {
      if (dashboardGrid && btnToggleExpand) {
        isPlayerExpanded = true;
        dashboardGrid.classList.add('is-expanded');
        btnToggleExpand.classList.remove('is-active');
        btnToggleExpand.title = "Abrir buscador de archivos y pistas";
        setTimeout(refreshAllMarquees, 320);
      }
    }
    // Si el explorador está CERRADO: arrastrar hacia la derecha (->) lo ABRE
    else if (isPlayerExpanded && dx > 35) {
      if (dashboardGrid && btnToggleExpand) {
        isPlayerExpanded = false;
        dashboardGrid.classList.remove('is-expanded');
        btnToggleExpand.classList.add('is-active');
        btnToggleExpand.title = "Ocultar buscador (reproductor a pantalla completa)";
        setTimeout(refreshAllMarquees, 320);
      }
    }
  }

  // Toques en pantalla táctil (solo inician si el dedo cayó en el área roja)
  window.addEventListener('touchstart', (e) => {
    if (e.touches && e.touches.length === 1) {
      const x = e.touches[0].clientX;
      const y = e.touches[0].clientY;
      if (isInsideExplorerDragZone(x, y)) {
        isDragGestureActive = true;
        touchStartX = x;
        touchStartY = y;
      } else {
        isDragGestureActive = false;
      }
    }
  }, { passive: true });

  window.addEventListener('touchend', (e) => {
    if (isDragGestureActive && e.changedTouches && e.changedTouches.length === 1) {
      const dx = e.changedTouches[0].clientX - touchStartX;
      const dy = e.changedTouches[0].clientY - touchStartY;
      isDragGestureActive = false;
      handleSwipeAction(dx, dy);
    }
  }, { passive: true });

  // Arrastre con ratón en PC (solo en el área roja)
  let isMouseDraggingZone = false;
  let mouseStartX = 0;
  let mouseStartY = 0;

  window.addEventListener('mousedown', (e) => {
    if (e.button === 0 && isInsideExplorerDragZone(e.clientX, e.clientY)) {
      isMouseDraggingZone = true;
      mouseStartX = e.clientX;
      mouseStartY = e.clientY;
    }
  });

  window.addEventListener('mouseup', (e) => {
    if (isMouseDraggingZone) {
      const dx = e.clientX - mouseStartX;
      const dy = e.clientY - mouseStartY;
      isMouseDraggingZone = false;
      handleSwipeAction(dx, dy);
    }
  });

  window.addEventListener('resize', () => {
    refreshAllMarquees();
  });

  btnShuffle.addEventListener('click', () => {
    isShuffle = !isShuffle;
    btnShuffle.classList.toggle('active', isShuffle);
  });

  btnRepeat.addEventListener('click', () => {
    isRepeat = !isRepeat;
    btnRepeat.classList.toggle('active', isRepeat);
  });

  // =================== UTILIDADES DE BÚSQUEDA Y ALMACENAMIENTO ===================
  function normalizeText(text) {
    if (!text) return '';
    return text.toString()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .trim();
  }

  function dedupChars(s) {
    return s.replace(/([a-z])\1+/g, '$1');
  }

  function levenshtein(a, b) {
    if (a === b) return 0;
    if (!a.length) return b.length;
    if (!b.length) return a.length;
    const d = [];
    for (let i = 0; i <= a.length; i++) d[i] = [i];
    for (let j = 0; j <= b.length; j++) d[0][j] = j;
    for (let i = 1; i <= a.length; i++) {
      for (let j = 1; j <= b.length; j++) {
        const cost = a[i - 1] === b[j - 1] ? 0 : 1;
        d[i][j] = Math.min(
          d[i - 1][j] + 1,
          d[i][j - 1] + 1,
          d[i - 1][j - 1] + cost
        );
      }
    }
    return d[a.length][b.length];
  }

  function tokenMatchesWord(token, word) {
    if (!token || !word) return false;
    if (word.includes(token)) return true;
    
    // Deduplicación de letras consecutivas (ej. "guarracha" -> "guaracha")
    const tDedup = dedupChars(token);
    const wDedup = dedupChars(word);
    if (wDedup.includes(tDedup)) return true;
    if (tDedup.includes(wDedup) && wDedup.length >= 4) return true;

    // Distancia de edición para errores de tipeo al conducir
    const maxDist = token.length >= 6 ? 2 : (token.length >= 4 ? 1 : 0);
    if (maxDist > 0 && Math.abs(token.length - word.length) <= maxDist) {
      if (levenshtein(token, word) <= maxDist) return true;
    }
    // Prefijo
    if (word.startsWith(token) || (token.length >= 4 && word.startsWith(token.slice(0, -1)))) return true;

    return false;
  }

  function trackMatchesTokens(track, tokens) {
    const haystack = normalizeText(`${track.title} ${track.artist} ${track.album || ''} ${track.folder || ''} ${track.path || ''}`);
    const haystackDedup = dedupChars(haystack);
    const haystackWords = haystack.split(/[^a-z0-9]+/);

    return tokens.every(token => {
      // 1. Subcadena directa
      if (haystack.includes(token)) return true;
      // 2. Subcadena con letras duplicadas colapsadas
      const tokenDedup = dedupChars(token);
      if (haystackDedup.includes(tokenDedup)) return true;
      // 3. Comparación difusa palabra por palabra
      return haystackWords.some(word => tokenMatchesWord(token, word));
    });
  }

  // =================== PUNTUACIÓN DE RELEVANCIA ===================
  function getTrackRelevance(track, rawQuery, normQuery, tokens) {
    let score = 0;
    const titleNorm = normalizeText(track.title || '');
    const artistNorm = normalizeText(track.artist || '');
    const albumNorm = normalizeText(track.album || '');
    const folderNorm = normalizeText(track.folder || '');
    const titleWords = titleNorm.split(/[^a-z0-9]+/).filter(Boolean);
    const artistWords = artistNorm.split(/[^a-z0-9]+/).filter(Boolean);

    // 1. Coincidencia directa completa al inicio del título
    if (titleNorm === normQuery) {
      score += 25000;
    } else if (titleNorm.startsWith(normQuery + ' ') || titleNorm.startsWith(normQuery + '-')) {
      score += 18000;
    } else if (titleNorm.startsWith(normQuery)) {
      score += 12000;
    } else if (titleNorm.includes(normQuery)) {
      score += 6000;
    }

    // 2. Coincidencia token por token
    tokens.forEach(token => {
      // Título
      if (titleWords.includes(token)) {
        score += 8000;
      } else if (titleWords.some(w => w.startsWith(token))) {
        score += 4000;
      } else if (titleNorm.includes(token)) {
        score += 2000;
      } else if (titleWords.some(w => tokenMatchesWord(token, w))) {
        score += 300;
      }

      // Artista
      if (artistWords.includes(token)) {
        score += 3000;
      } else if (artistWords.some(w => w.startsWith(token))) {
        score += 1500;
      } else if (artistNorm.includes(token)) {
        score += 800;
      }

      // Carpeta / Álbum
      if (folderNorm.includes(token) || albumNorm.includes(token)) {
        score += 400;
      }
    });

    // Desempate fino: títulos más concisos y directos ganan prioridad
    score -= Math.min(titleNorm.length, 60);
    return score;
  }

  function getFolderRelevance(folder, rawQuery, normQuery, tokens) {
    let score = 0;
    const nameNorm = normalizeText(folder.name || '');
    const words = nameNorm.split(/[^a-z0-9]+/).filter(Boolean);

    if (nameNorm === normQuery) {
      score += 20000;
    } else if (nameNorm.startsWith(normQuery + ' ') || nameNorm.startsWith(normQuery)) {
      score += 12000;
    } else if (nameNorm.includes(normQuery)) {
      score += 5000;
    }

    tokens.forEach(token => {
      if (words.includes(token)) score += 6000;
      else if (words.some(w => w.startsWith(token))) score += 3000;
      else if (nameNorm.includes(token)) score += 1000;
    });

    score -= Math.min(nameNorm.length, 50);
    return score;
  }

  // Icono oficial vectorial de USB (tridente universal)
  const USB_TRIDENT_SVG = `<svg class="usb-icon" viewBox="0 0 24 24"><path d="M15 7v4h1v2h-3V5h2l-3-4-3 4h2v8H8v-2.07c.7-.37 1.2-1.08 1.2-1.93 0-1.2-.99-2.18-2.2-2.18-1.21 0-2.2.98-2.2 2.18 0 .85.5 1.56 1.2 1.93V13c0 1.11.89 2 2 2h3v3.05c-.71.37-1.2 1.1-1.2 1.95a2.2 2.2 0 0 0 4.4 0c0-.85-.49-1.58-1.2-1.95V15h3c1.11 0 2-.89 2-2v-2h1V7h-4z"/></svg>`;

  function isSameUsb(track, targetUsbSource) {
    if (!targetUsbSource || !track) return true;
    const want2 = targetUsbSource.includes('2') || targetUsbSource.toLowerCase().includes('udisk2');
    const want1 = targetUsbSource.includes('1') || targetUsbSource.toLowerCase().includes('udisk1');
    const tUsb = track.usbSource || '';
    const tPath = (track.path || '').toLowerCase();
    const isTrack2 = tUsb === 'USB 2' || tPath.includes('udisk2') || tPath.includes('usb1');
    if (want2) return isTrack2;
    if (want1) return !isTrack2;
    return true;
  }

  // Generador de etiquetas para USB 1, USB 2 y Memoria Interna
  function getStorageBadgeHtml(path) {
    if (!path) return '';
    const p = path.toLowerCase();
    if (p.includes('udisk1') || p.includes('usb1')) {
      return `<span class="badge-storage usb1">${USB_TRIDENT_SVG} 1</span>`;
    } else if (p.includes('udisk2') || p.includes('usb2')) {
      return `<span class="badge-storage usb2">${USB_TRIDENT_SVG} 2</span>`;
    } else if (p.includes('sdcard') || p.includes('internal') || p.includes('emulated')) {
      return `<span class="badge-storage internal">INTERNA</span>`;
    }
    return '';
  }

  // Formateador de ruta limpia: en USB1 y USB2 omite '/storage/udiskX' para evitar redundancia
  function formatDisplayPath(path) {
    if (!path) return '';
    let p = path;
    if (p.startsWith('/storage/udisk1')) {
      p = p.replace('/storage/udisk1', '');
    } else if (p.startsWith('/storage/udisk2')) {
      p = p.replace('/storage/udisk2', '');
    }
    // Asegurar que comience con /
    if (!p.startsWith('/')) p = '/' + p;
    // Quitar barra final para estética limpia
    if (p.length > 1 && p.endsWith('/')) p = p.slice(0, -1);
    return p;
  }

  function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
  }

  // =================== FILTROS DE BÚSQUEDA Y USBS ===================
  let currentSearchFilter = 'folder'; // 'folder' | 'all' | 'usb1' | 'usb2'
  const searchFiltersBar = document.getElementById('searchFiltersBar');
  const filterChips = document.querySelectorAll('.filter-chip');
  const chipFilterFolder = document.getElementById('chipFilterFolder');
  const chipFilterAll = document.getElementById('chipFilterAll');
  const chipFilterUsb1 = document.getElementById('chipFilterUsb1');
  const chipFilterUsb2 = document.getElementById('chipFilterUsb2');

  function updateFolderChipContent() {
    if (!chipFilterFolder) return;

    let usbBadgeEl = document.getElementById('chipFolderUsbBadge');
    let textEl = document.getElementById('chipFolderText');

    if (!usbBadgeEl || !textEl) {
      chipFilterFolder.innerHTML = `
        <span class="chip-folder-icon">📁</span>
        <span class="chip-folder-usb-badge hidden" id="chipFolderUsbBadge"></span>
        <span class="chip-folder-text-wrap">
          <span class="chip-folder-text" id="chipFolderText"></span>
        </span>
      `;
      usbBadgeEl = document.getElementById('chipFolderUsbBadge');
      textEl = document.getElementById('chipFolderText');
    }

    const folderIconEl = chipFilterFolder.querySelector('.chip-folder-icon');

    let usbNum = '';
    if (currentFolderUsbSource) {
      const src = currentFolderUsbSource.toLowerCase();
      if (src.includes('1') || src.includes('udisk1')) usbNum = '1';
      else if (src.includes('2') || src.includes('udisk2')) usbNum = '2';
    }

    if (folderIconEl) folderIconEl.classList.remove('hidden');

    if (usbNum) {
      usbBadgeEl.innerHTML = `${USB_TRIDENT_SVG}<span>${usbNum}</span><span class="chip-folder-sep">|</span>`;
      usbBadgeEl.classList.remove('hidden');
    } else {
      usbBadgeEl.innerHTML = '';
      usbBadgeEl.classList.add('hidden');
    }

    textEl.textContent = currentFolder || '';
    const fullTitle = (usbNum ? `[USB ${usbNum}] ` : '') + (currentFolder || '');
    chipFilterFolder.setAttribute('title', fullTitle);
    chipFilterFolder.classList.remove('hidden');

    const wrapEl = chipFilterFolder.querySelector('.chip-folder-text-wrap');
    if (wrapEl && textEl) {
      setTimeout(() => applyMarquee(textEl, wrapEl), 80);
    }
  }

  function updateFilterChipsUI() {
    // La barra de filtros solo tiene utilidad en la pestaña Pistas (activeTab === 'playlist').
    // En Carpetas USB el árbol organiza los pendrives, y en Favoritos se muestra la lista VIP completa.
    if (searchFiltersBar) {
      searchFiltersBar.classList.toggle('hidden', activeTab !== 'playlist');
    }

    if (activeTab === 'playlist' && currentFolder) {
      updateFolderChipContent();
    } else {
      if (chipFilterFolder) {
        chipFilterFolder.classList.add('hidden');
        if (currentSearchFilter === 'folder') {
          currentSearchFilter = 'all';
        }
      }
    }

    const allPool = masterTracks.length > 0 ? masterTracks : sampleTracks;
    const hasUsb1 = allPool.some(t => t.usbSource === 'USB 1' || (t.path && (t.path.includes('udisk1') || t.path.includes('usb0'))));
    const hasUsb2 = allPool.some(t => t.usbSource === 'USB 2' || (t.path && (t.path.includes('udisk2') || t.path.includes('usb1'))));
    const multipleUsb = hasUsb1 && hasUsb2;

    // Si solo hay un pendrive conectado, TODO ya hace referencia a ese único USB,
    // por lo que los chips USB 1 y USB 2 solo se muestran si ambos están presentes
    if (chipFilterUsb1) chipFilterUsb1.classList.toggle('hidden', !multipleUsb);
    if (chipFilterUsb2) chipFilterUsb2.classList.toggle('hidden', !multipleUsb);

    if (!multipleUsb && (currentSearchFilter === 'usb1' || currentSearchFilter === 'usb2')) {
      currentSearchFilter = 'all';
    }

    filterChips.forEach(chip => {
      const f = chip.getAttribute('data-filter');
      if (f === currentSearchFilter) {
        chip.classList.add('active');
      } else {
        chip.classList.remove('active');
      }
    });
  }

  filterChips.forEach(chip => {
    chip.addEventListener('click', () => {
      searchInput.blur();
      currentSearchFilter = chip.getAttribute('data-filter');
      updateFilterChipsUI();
      if (searchInput.value.trim().length > 0) {
        renderList();
      }
    });
  });

  // Ocultar teclado táctil al tocar fuera del cuadro de búsqueda
  document.addEventListener('pointerdown', (e) => {
    if (!e.target.closest('.search-box')) {
      searchInput.blur();
    }
  });

  // Botón HOME Táctil Conductor (Misma función que el botón físico HOME del auto)
  const btnNavHome = document.getElementById('btnNavHome') || document.getElementById('btnNavBack');
  if (btnNavHome) {
    btnNavHome.addEventListener('click', () => {
      searchInput.blur();
      if (searchInput.value.trim().length > 0) {
        searchInput.value = '';
        clearSearch.classList.add('hidden');
      }
      if (tabButtons[0]) {
        tabButtons[0].click();
      }
    });
  }

  // =================== PESTAÑAS Y RENDERIZADO ===================
  // Función para revelar y desplegar automáticamente la carpeta de la canción que está sonando
  function revealPlayingFolderInTree() {
    const currentTrack = currentPlaylist[currentTrackIndex];
    if (!currentTrack) return;

    const targetFolder = currentTrack.folder || currentFolder;
    const targetPath = currentTrack.path || '';
    const targetUsb = currentTrack.usbSource || (targetPath.toLowerCase().includes('udisk2') ? 'USB 2' : 'USB 1');

    function findNodeChain(nodes, chain = []) {
      for (const node of nodes) {
        const nextChain = [...chain, node.id];
        // Buscar primero recursivamente en los hijos para priorizar el nodo exacto más profundo
        if (node.children && node.children.length > 0) {
          const found = findNodeChain(node.children, nextChain);
          if (found) return found;
        }

        const isExactMatch = (node.name === targetFolder) ||
          (targetPath && node.path && !node.isUsbRoot && (targetPath.replace(/\/+$/, '') === node.path.replace(/\/+$/, '')));

        if (isExactMatch) {
          return nextChain;
        }
      }
      return null;
    }

    const chain = findNodeChain(currentTreeData);
    if (chain && chain.length > 0) {
      chain.forEach(nodeId => expandedTreeNodes.add(nodeId));
    } else {
      if (targetUsb === 'USB 2') {
        expandedTreeNodes.add('usb2');
      } else {
        expandedTreeNodes.add('usb1');
      }
    }

    // Si la canción es de USB 2 y el filtro estaba en USB 1, poner en TODO para que aparezca
    if (targetUsb === 'USB 2' && currentSearchFilter === 'usb1') {
      currentSearchFilter = 'all';
    } else if (targetUsb === 'USB 1' && currentSearchFilter === 'usb2') {
      currentSearchFilter = 'all';
    }
  }

  tabButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      searchInput.blur();
      // Limpiar búsqueda y loaders al cambiar de pestaña
      if (searchTimer) clearTimeout(searchTimer);
      searchInput.value = '';
      clearSearch.classList.add('hidden');
      if (searchSpinner) searchSpinner.classList.add('hidden');
      if (searchOverlayLoading) searchOverlayLoading.classList.add('hidden');
      trackListContainer.classList.remove('is-searching');

      tabButtons.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      activeTab = btn.getAttribute('data-tab');
      if (activeTab === 'folders') {
        searchInput.placeholder = "Buscar entre carpetas...";
        currentSearchFilter = 'all';
        revealPlayingFolderInTree();
      } else if (activeTab === 'playlist') {
        searchInput.placeholder = "Buscar canción, artista o álbum...";
        if (currentFolder) {
          currentSearchFilter = 'folder';
        } else {
          currentSearchFilter = 'all';
        }
      } else if (activeTab === 'favorites') {
        searchInput.placeholder = "Buscar en favoritos...";
        if (currentSearchFilter === 'folder') {
          currentSearchFilter = 'all';
        }
      }
      renderList();
    });
  });

  function renderList() {
    trackListContainer.innerHTML = '';
    updateFilterChipsUI();

    const rawQuery = searchInput.value;
    const normQuery = normalizeText(rawQuery);
    const tokens = normQuery.split(/\s+/).filter(t => t.length > 0);

    // Mostrar botones de Colapsar/Descolapsar únicamente en la pestaña de Carpetas USB sin búsqueda activa
    if (treeActions) {
      treeActions.classList.toggle('hidden', activeTab !== 'folders' || tokens.length > 0);
    }

    // BÚSQUEDA ACTIVA SEGÚN LA PESTAÑA SELECCIONADA
    if (tokens.length > 0) {
      if (activeTab === 'folders') {
        // PESTAÑA CARPETAS: Buscar ÚNICAMENTE en Carpetas
        let fPool = foldersList;
        if (currentSearchFilter === 'usb1') {
          fPool = fPool.filter(f => f.path.includes('udisk1') || f.path.includes('usb0') || f.badge === 'USB 1');
        } else if (currentSearchFilter === 'usb2') {
          fPool = fPool.filter(f => f.path.includes('udisk2') || f.path.includes('usb1') || f.badge === 'USB 2');
        }

        const matchedFolders = fPool.filter(f => {
          const text = normalizeText(f.name + " " + f.path);
          const dedup = dedupChars(text);
          return tokens.every(token => text.includes(token) || dedup.includes(dedupChars(token)));
        });

        matchedFolders.sort((a, b) => getFolderRelevance(b, rawQuery, normQuery, tokens) - getFolderRelevance(a, rawQuery, normQuery, tokens));

        const filterLabel = currentSearchFilter === 'usb1' ? ' [USB 1]' : (currentSearchFilter === 'usb2' ? ' [USB 2]' : '');
        crumbFolder.innerHTML = `📂 Carpetas${filterLabel}: "<strong>${escapeHtml(rawQuery)}</strong>" (${matchedFolders.length})`;

        if (matchedFolders.length === 0) {
          trackListContainer.innerHTML = `
            <div class="empty-search-box">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="empty-icon"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
              <div class="empty-title">Sin carpetas para "<strong>${escapeHtml(rawQuery)}</strong>"</div>
              <div class="empty-desc">No se encontraron carpetas coincidentes en este origen.</div>
              <button class="btn-clear-search-action" id="btnClearSearchAction">Borrar búsqueda y ver todo</button>
            </div>
          `;
          const btnReset = document.getElementById('btnClearSearchAction');
          if (btnReset) {
            btnReset.addEventListener('click', () => {
              searchInput.value = '';
              clearSearch.classList.add('hidden');
              searchInput.blur();
              renderList();
            });
          }
          return;
        }

        matchedFolders.forEach(folder => {
          const folderEl = document.createElement('div');
          folderEl.className = 'list-item';
          const hasFolderMusic = (folder.count || 0) > 0;
          const playBtnHtml = hasFolderMusic ? `
            <button class="btn-folder-play" title="Reproducir inmediatamente esta carpeta">
              <span class="track-count-num">${folder.count}</span>
              <div class="play-circle">
                <svg viewBox="0 0 24 24"><path d="M8 5v14l11-7z"/></svg>
              </div>
            </button>
          ` : '';

          folderEl.innerHTML = `
            <div class="item-left">
              <div class="item-icon-wrap">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:20px;height:20px;"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
              </div>
              <div class="item-info">
                <span class="item-title">${folder.name}</span>
                <div class="item-subtitle">
                  ${getStorageBadgeHtml(folder.path)}
                  <span class="path-text">${formatDisplayPath(folder.path)}</span>
                </div>
              </div>
            </div>
            ${playBtnHtml}
          `;

          const playBtn = folderEl.querySelector('.btn-folder-play');
          if (playBtn) {
            playBtn.addEventListener('click', (e) => {
              e.stopPropagation();
              playFolderTree(folder);
            });
          }

          folderEl.addEventListener('click', () => {
            searchInput.blur();
            currentFolder = folder.name;
            currentFolderUsbSource = folder.usbSource || folder.badge || (folder.path && folder.path.toLowerCase().includes('udisk2') ? 'USB 2' : 'USB 1');
            crumbFolder.textContent = `${currentFolderUsbSource ? '[' + currentFolderUsbSource + '] ' : ''}${folder.name}`;
            currentSearchFilter = 'folder';
            
            const allPool = masterTracks.length > 0 ? masterTracks : sampleTracks;
            const folderTracks = allPool.filter(t => t.folder === folder.name);
            currentPlaylist = folderTracks.length > 0 ? folderTracks : [...allPool];
            tabButtons[1].click();
          });

          trackListContainer.appendChild(folderEl);
        });
        return;
      }

      // PESTAÑAS PISTAS O FAVORITOS: Buscar en pistas
      const allPool = masterTracks.length > 0 ? masterTracks : sampleTracks;
      let searchPool = allPool;

      if (activeTab === 'favorites') {
        searchPool = allPool.filter(t => t.favorite);
      } else {
        if (currentSearchFilter === 'folder' && currentFolder) {
          searchPool = allPool.filter(t => t.folder === currentFolder && isSameUsb(t, currentFolderUsbSource));
        }
      }

      if (currentSearchFilter === 'usb1') {
        searchPool = searchPool.filter(t => t.usbSource === 'USB 1' || (t.path && (t.path.includes('udisk1') || t.path.includes('usb0'))));
      } else if (currentSearchFilter === 'usb2') {
        searchPool = searchPool.filter(t => t.usbSource === 'USB 2' || (t.path && (t.path.includes('udisk2') || t.path.includes('usb1'))));
      }

      const filterLabel = (activeTab === 'favorites') ? ' [Favoritos]' :
          (currentSearchFilter === 'folder' ? ` [${currentFolder}]` : (currentSearchFilter === 'usb1' ? ' [USB 1]' : (currentSearchFilter === 'usb2' ? ' [USB 2]' : ' [TODO]')));
      crumbFolder.innerHTML = `🔍 Búsqueda${filterLabel}: "<strong>${escapeHtml(rawQuery)}</strong>"`;
      
      const searchResults = searchPool.filter(t => trackMatchesTokens(t, tokens));
      searchResults.sort((a, b) => getTrackRelevance(b, rawQuery, normQuery, tokens) - getTrackRelevance(a, rawQuery, normQuery, tokens));

      songCountEl.textContent = searchResults.length;

      if (searchResults.length === 0) {
        trackListContainer.innerHTML = `
          <div class="empty-search-box">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="empty-icon"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            <div class="empty-title">Sin resultados en ${filterLabel} para "<strong>${escapeHtml(rawQuery)}</strong>"</div>
            <div class="empty-desc">No se encontraron canciones en este origen. Cambia el filtro o prueba otro término.</div>
            <button class="btn-clear-search-action" id="btnClearSearchAction">Borrar término de búsqueda</button>
          </div>
        `;
        const btnReset = document.getElementById('btnClearSearchAction');
        if (btnReset) {
          btnReset.addEventListener('click', () => {
            searchInput.value = '';
            clearSearch.classList.add('hidden');
            searchInput.blur();
            renderList();
          });
        }
        return;
      }

      searchResults.forEach((track) => {
        const isCurrent = (currentPlaylist[currentTrackIndex]?.id === track.id);
        const itemEl = document.createElement('div');
        itemEl.className = `list-item ${isCurrent ? 'is-playing' : ''}`;
        
        itemEl.innerHTML = `
          <div class="item-left">
            <div class="item-icon-wrap">
              ${isCurrent && isPlaying ? `
                <div class="sound-wave-bars" style="height:14px; gap:2px;">
                  <span style="height:12px;background:#0c0e12"></span>
                  <span style="height:8px;background:#0c0e12"></span>
                  <span style="height:14px;background:#0c0e12"></span>
                </div>
              ` : `
                <svg viewBox="0 0 24 24" fill="currentColor" style="width:16px;height:16px;"><path d="M8 5v14l11-7z"/></svg>
              `}
            </div>
            <div class="item-info">
              <div class="item-title-wrap">
                <span class="item-title">${track.title}</span>
              </div>
              <div class="item-subtitle">
                ${getStorageBadgeHtml(track.path)}
                <span class="path-text">${track.artist} • ${track.album || track.folder}</span>
              </div>
            </div>
          </div>
          <div class="item-right">
            <span class="item-duration">${track.duration}</span>
            <button class="btn-item-fav ${track.favorite ? 'is-fav' : ''}" title="Marcar Favorito">
              <svg viewBox="0 0 24 24" fill="${track.favorite ? 'currentColor' : 'none'}" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
            </button>
          </div>
        `;

        // 1. Click en el botón de play al principio: SOLO AQUÍ SE REPRODUCE
        const playBtn = itemEl.querySelector('.item-icon-wrap');
        playBtn.setAttribute('title', 'Reproducir');
        playBtn.addEventListener('click', (e) => {
          e.stopPropagation();
          searchInput.blur();
          
          // 1. Setear la carpeta actual a la carpeta de la canción seleccionada
          currentFolder = track.folder;
          currentFolderUsbSource = track.usbSource || (track.path && track.path.toLowerCase().includes('udisk2') ? 'USB 2' : 'USB 1');

          // 2. Cargar en currentPlaylist todas las canciones de esa carpeta
          const folderTracks = allPool.filter(t => t.folder === track.folder && (track.usbSource ? t.usbSource === track.usbSource : true));
          currentPlaylist = folderTracks.length > 0 ? folderTracks : [...allPool];

          // 3. Índice de la pista en esa carpeta
          const realFolderIdx = currentPlaylist.findIndex(t => t.id === track.id);

          // 4. Actualizar el chip de carpeta para que muestre el nuevo nombre con marquesina si es largo
          if (chipFilterFolder) {
            updateFolderChipContent();
          }

          // 5. Reproducir la canción (actualiza reproductor izquierdo, metadata y previews)
          loadTrack(realFolderIdx >= 0 ? realFolderIdx : 0, true);

          // 6. La lista de búsqueda NO se oculta, sigue mostrando los resultados
          // Marcamos visualmente esta canción como la que está sonando
          trackListContainer.querySelectorAll('.list-item').forEach(el => el.classList.remove('is-playing'));
          itemEl.classList.add('is-playing');
          const iconWrap = itemEl.querySelector('.item-icon-wrap');
          if (iconWrap) {
            iconWrap.innerHTML = `
              <div class="sound-wave-bars" style="height:14px; gap:2px;">
                <span style="height:12px;background:#0c0e12"></span>
                <span style="height:8px;background:#0c0e12"></span>
                <span style="height:14px;background:#0c0e12"></span>
              </div>
            `;
          }
        });

        // 2. Click en el texto:
        // 1 toque: Mostrar/ocultar nombre completo (espera 280ms para confirmar)
        // 2 toques: Cancela la apertura y reproduce la canción directamente
        const infoEl = itemEl.querySelector('.item-info');
        infoEl.setAttribute('title', '1 toque: ver nombre completo • 2 toques: reproducir');
        let clickCount = 0;
        let clickTimer = null;
        infoEl.addEventListener('click', (e) => {
          e.stopPropagation();
          searchInput.blur();
          clickCount++;

          if (clickCount === 1) {
            clickTimer = setTimeout(() => {
              // 1 toque confirmado: EXPANDIR / COLAPSAR
              clickCount = 0;
              clickTimer = null;
              const wasExpanded = itemEl.classList.contains('is-expanded');
              trackListContainer.querySelectorAll('.list-item.is-expanded').forEach(el => {
                el.classList.remove('is-expanded');
              });
              if (!wasExpanded) {
                itemEl.classList.add('is-expanded');
              }
            }, 280);
          } else if (clickCount >= 2) {
            // 2 toques: CANCELAR la apertura y REPRODUCIR DIRECTO
            if (clickTimer) {
              clearTimeout(clickTimer);
              clickTimer = null;
            }
            clickCount = 0;
            playBtn.click();
          }
        });

        // Click en favorito
        const favBtn = itemEl.querySelector('.btn-item-fav');
        favBtn.addEventListener('click', (e) => {
          e.stopPropagation();
          track.favorite = !track.favorite;
          favBtn.classList.toggle('is-fav', track.favorite);
          favBtn.querySelector('svg').setAttribute('fill', track.favorite ? 'currentColor' : 'none');
        });

        trackListContainer.appendChild(itemEl);
      });
      return;
    }

    // MODO NORMAL (SIN BÚSQUEDA) - ÁRBOL JERÁRQUICO DE CARPETAS USB
    if (activeTab === 'folders') {
      let filteredRoots = currentTreeData;
      if (currentSearchFilter === 'usb1') {
        filteredRoots = currentTreeData.filter(n => n.usbSource === 'USB 1' || n.id === 'usb1');
        crumbFolder.textContent = "Carpetas USB 1 (Árbol)";
      } else if (currentSearchFilter === 'usb2') {
        filteredRoots = currentTreeData.filter(n => n.usbSource === 'USB 2' || n.id === 'usb2');
        crumbFolder.textContent = "Carpetas USB 2 (Árbol)";
      } else {
        crumbFolder.textContent = "Carpetas USB (Árbol de Pendrives)";
      }

      function renderTreeNode(node, level = 0) {
        const hasChildren = node.children && node.children.length > 0;
        const isExpanded = expandedTreeNodes.has(node.id);
        const currentTrack = currentPlaylist[currentTrackIndex];
        const isPlayingFolder = currentTrack && (
          (node.name === currentTrack.folder) ||
          (currentTrack.path && node.path && !node.isUsbRoot && (currentTrack.path.replace(/\/+$/, '') === node.path.replace(/\/+$/, '')))
        );
        const isActive = isPlayingFolder || (currentFolder === node.name);
        const indentPx = 10 + (level * 20);

        // Si la carpeta tiene 0 músicas directas (ej: es solo contenedor de subcarpetas o raíz),
        // se lista normalmente pero NO debe salir el botón de play.
        // Solo las carpetas con canciones directas (> 0) muestran el botón de play con su número.
        const directTracksCount = (typeof node.directCount === 'number')
          ? node.directCount
          : (hasChildren ? 0 : (node.count || 0));
        const hasDirectMusic = directTracksCount > 0;

        const nodeEl = document.createElement('div');
        nodeEl.className = `tree-node-item ${isPlayingFolder ? 'is-playing-folder' : (isActive ? 'is-active-folder' : '')}`;
        nodeEl.style.paddingLeft = `${indentPx}px`;

        const isUsbRoot = !!node.isUsbRoot;

        let playBtnHtml = '';
        if (hasDirectMusic) {
          playBtnHtml = `
            <button class="btn-folder-play" title="Reproducir música de ${escapeHtml(node.name)}">
              <span class="track-count-num">${directTracksCount}</span>
              <div class="play-circle">
                <svg viewBox="0 0 24 24"><path d="M8 5v14l11-7z"/></svg>
              </div>
            </button>
          `;
        }

        // Límite prudente para evitar scrolls kilométricos si hay carpetas con nombres excesivos
        const MAX_FOLDER_CHARS = 26;
        const rawFolderName = node.name || '';
        const displayFolderName = rawFolderName.length > MAX_FOLDER_CHARS 
          ? rawFolderName.slice(0, MAX_FOLDER_CHARS) + '…' 
          : rawFolderName;

        nodeEl.innerHTML = `
          <div class="tree-node-left">
            <div class="tree-toggle-arrow ${isExpanded ? 'is-expanded' : ''} ${!hasChildren ? 'no-children' : ''}">
              ${hasChildren ? `
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.6" style="width:16px;height:16px;">
                  <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
              ` : `
                <span style="display:inline-block;width:6px;height:6px;border-radius:50%;background:rgba(148,163,184,0.35);"></span>
              `}
            </div>
            <div class="tree-node-icon ${isUsbRoot ? 'is-usb-root' : ''}">
              ${isUsbRoot ? `
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:20px;height:20px;">
                  <rect x="6" y="2" width="12" height="20" rx="2"></rect>
                  <line x1="9" y1="6" x2="9" y2="8"></line>
                  <line x1="15" y1="6" x2="15" y2="8"></line>
                  <line x1="9" y1="18" x2="15" y2="18"></line>
                </svg>
              ` : `
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:20px;height:20px;">
                  <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
                </svg>
              `}
            </div>
            <div class="tree-node-info" title="${escapeHtml(rawFolderName)}">
              <span class="tree-node-title">${escapeHtml(displayFolderName)}</span>
            </div>
          </div>
          ${playBtnHtml}
        `;

        // Click en botón Play [ N ▶ ] (Solo si la carpeta tiene canciones directas)
        const playBtn = nodeEl.querySelector('.btn-folder-play');
        if (playBtn) {
          playBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            playFolderTree(node);
          });
        }

        // Click en la fila: expandir / contraer y preparar pistas
        nodeEl.addEventListener('click', () => {
          searchInput.blur();
          if (hasChildren) {
            if (expandedTreeNodes.has(node.id)) {
              expandedTreeNodes.delete(node.id);
            } else {
              expandedTreeNodes.add(node.id);
            }
          }
          currentFolder = node.name;
          currentFolderUsbSource = node.usbSource || (node.path && node.path.includes('udisk2') ? 'USB 2' : 'USB 1');
          const usbTag = currentFolderUsbSource ? `[${currentFolderUsbSource}] ` : '';
          crumbFolder.textContent = `${usbTag}${node.name}`;
          
          // Actualizar playlist para esta carpeta
          const allPool = masterTracks.length > 0 ? masterTracks : sampleTracks;
          const folderTracks = allPool.filter(t => {
            if (!isSameUsb(t, currentFolderUsbSource)) return false;
            if (t.folder === node.name) return true;
            if (t.path && node.path && t.path.startsWith(node.path)) return true;
            return false;
          });
          if (folderTracks.length > 0) {
            sortTracksAlphabetically(folderTracks);
            currentPlaylist = folderTracks;
            songCountEl.textContent = currentPlaylist.length;
          }

          renderList();
        });

        trackListContainer.appendChild(nodeEl);

        // Si el nodo está desplegado y tiene subcarpetas, renderizarlas con su nivel de sangría
        if (hasChildren && isExpanded) {
          node.children.forEach(child => {
            renderTreeNode(child, level + 1);
          });
        }
      }

      filteredRoots.forEach(rootNode => {
        renderTreeNode(rootNode, 0);
      });

      // Auto-scroll a la carpeta que está sonando actualmente
      setTimeout(() => {
        const playingFolderEl = trackListContainer.querySelector('.tree-node-item.is-playing-folder');
        if (playingFolderEl) {
          playingFolderEl.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }
      }, 70);
    } 
    else if (activeTab === 'playlist' || activeTab === 'favorites') {
      let filtered = currentPlaylist;
      const allPool = masterTracks.length > 0 ? masterTracks : sampleTracks;

      if (activeTab === 'favorites') {
        let favPool = allPool.filter(t => t.favorite);
        sortTracksAlphabetically(favPool);
        filtered = favPool;
        crumbFolder.textContent = `Mis Favoritos (${filtered.length})`;
      } else {
        if (currentSearchFilter === 'usb1') {
          filtered = allPool.filter(t => t.usbSource === 'USB 1' || (t.path && (t.path.includes('udisk1') || t.path.includes('usb0'))));
          crumbFolder.textContent = `Pistas USB 1 (${filtered.length})`;
        } else if (currentSearchFilter === 'usb2') {
          filtered = allPool.filter(t => t.usbSource === 'USB 2' || (t.path && (t.path.includes('udisk2') || t.path.includes('usb1'))));
          crumbFolder.textContent = `Pistas USB 2 (${filtered.length})`;
        } else if (currentSearchFilter === 'all') {
          filtered = [...allPool];
          crumbFolder.textContent = `Todas las pistas (${filtered.length})`;
        } else {
          filtered = currentPlaylist;
          const usbTag = currentFolderUsbSource ? `[${currentFolderUsbSource}] ` : '';
          crumbFolder.textContent = `${usbTag}${currentFolder} (${filtered.length})`;
        }
        sortTracksAlphabetically(filtered);
      }

      songCountEl.textContent = filtered.length;

      filtered.forEach((track, idx) => {
        const isCurrent = (currentPlaylist[currentTrackIndex]?.id === track.id);
        const itemEl = document.createElement('div');
        itemEl.className = `list-item ${isCurrent ? 'is-playing' : ''}`;
        
        itemEl.innerHTML = `
          <div class="item-left">
            <div class="item-icon-wrap">
              ${isCurrent && isPlaying ? `
                <div class="sound-wave-bars" style="height:14px; gap:2px;">
                  <span style="height:12px;background:#0c0e12"></span>
                  <span style="height:8px;background:#0c0e12"></span>
                  <span style="height:14px;background:#0c0e12"></span>
                </div>
              ` : `
                <svg viewBox="0 0 24 24" fill="currentColor" style="width:16px;height:16px;"><path d="M8 5v14l11-7z"/></svg>
              `}
            </div>
            <div class="item-info">
              <div class="item-title-wrap">
                <span class="item-title">${track.title}</span>
              </div>
              <div class="item-subtitle">
                ${getStorageBadgeHtml(track.path)}
                <span class="path-text">${track.artist} • ${track.album || track.folder}</span>
              </div>
            </div>
          </div>
          <div class="item-right">
            <span class="item-duration">${track.duration}</span>
            <button class="btn-item-fav ${track.favorite ? 'is-fav' : ''}" title="Marcar Favorito">
              <svg viewBox="0 0 24 24" fill="${track.favorite ? 'currentColor' : 'none'}" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
            </button>
          </div>
        `;

        // 1. Click en el botón de play al principio: SOLO AQUÍ SE REPRODUCE
        const playBtn = itemEl.querySelector('.item-icon-wrap');
        playBtn.setAttribute('title', 'Reproducir');
        playBtn.addEventListener('click', (e) => {
          e.stopPropagation();
          searchInput.blur();
          const realIdx = currentPlaylist.findIndex(t => t.id === track.id);
          loadTrack(realIdx >= 0 ? realIdx : idx, true);
        });

        // 2. Click en el texto:
        // 1 toque: Mostrar/ocultar nombre completo
        // 2. Click en el texto:
        // 1 toque: Mostrar/ocultar nombre completo (espera 280ms para confirmar)
        // 2 toques: Cancela la apertura y reproduce la canción directamente
        const infoEl = itemEl.querySelector('.item-info');
        infoEl.setAttribute('title', '1 toque: ver nombre completo • 2 toques: reproducir');
        let clickCount = 0;
        let clickTimer = null;
        infoEl.addEventListener('click', (e) => {
          e.stopPropagation();
          searchInput.blur();
          clickCount++;

          if (clickCount === 1) {
            clickTimer = setTimeout(() => {
              // 1 toque confirmado: EXPANDIR / COLAPSAR
              clickCount = 0;
              clickTimer = null;
              const wasExpanded = itemEl.classList.contains('is-expanded');
              trackListContainer.querySelectorAll('.list-item.is-expanded').forEach(el => {
                el.classList.remove('is-expanded');
              });
              if (!wasExpanded) {
                itemEl.classList.add('is-expanded');
              }
            }, 280);
          } else if (clickCount >= 2) {
            // 2 toques: CANCELAR la apertura y REPRODUCIR DIRECTO
            if (clickTimer) {
              clearTimeout(clickTimer);
              clickTimer = null;
            }
            clickCount = 0;
            playBtn.click();
          }
        });

        // Click en favorito
        const favBtn = itemEl.querySelector('.btn-item-fav');
        favBtn.addEventListener('click', (e) => {
          e.stopPropagation();
          track.favorite = !track.favorite;
          favBtn.classList.toggle('is-fav', track.favorite);
          favBtn.querySelector('svg').setAttribute('fill', track.favorite ? 'currentColor' : 'none');
          if (activeTab === 'favorites') renderList();
        });

        trackListContainer.appendChild(itemEl);
      });
    }

    // Activar marquesina en la canción que está sonando en la lista
    setTimeout(refreshAllMarquees, 70);
    setTimeout(updateWebFastScroller, 80);
  }

  // =================== FASTSCROLLER TÁCTIL ERGONÓMICO Y BURBUJA A-Z ===================
  const webFastScroller = document.getElementById('webFastScroller');
  const webFastThumb = document.getElementById('webFastThumb');
  const webFastBubble = document.getElementById('webFastBubble');
  const webBubbleLetter = document.getElementById('webBubbleLetter');
  const webBubbleCount = document.getElementById('webBubbleCount');
  let webHideBubbleTimer = null;

  function updateWebFastScroller() {
    if (!webFastScroller || !trackListContainer) return;
    const items = trackListContainer.querySelectorAll('.list-item');
    if (items.length > 8) {
      webFastScroller.style.display = 'flex';
      syncWebThumbPosition();
    } else {
      webFastScroller.style.display = 'none';
      if (webFastBubble) webFastBubble.classList.remove('is-active');
    }
  }

  function syncWebThumbPosition() {
    if (!webFastScroller || !webFastThumb || !trackListContainer) return;
    const maxScroll = trackListContainer.scrollHeight - trackListContainer.clientHeight;
    if (maxScroll <= 0) {
      webFastThumb.style.transform = 'translateY(0)';
      return;
    }
    const ratio = Math.max(0, Math.min(1, trackListContainer.scrollTop / maxScroll));
    const scrollerHeight = webFastScroller.clientHeight;
    const thumbHeight = webFastThumb.clientHeight || 44;
    const maxTravel = Math.max(0, scrollerHeight - thumbHeight);
    webFastThumb.style.transform = `translateY(${ratio * maxTravel}px)`;
  }

  if (trackListContainer) {
    trackListContainer.addEventListener('scroll', () => {
      syncWebThumbPosition();
    }, { passive: true });
  }

  function handleFastScroll(clientY) {
    if (!webFastScroller || !trackListContainer) return;
    const rect = webFastScroller.getBoundingClientRect();
    const scrollerHeight = rect.height;
    if (scrollerHeight <= 0) return;

    let offsetY = clientY - rect.top;
    offsetY = Math.max(0, Math.min(offsetY, scrollerHeight));
    const ratio = offsetY / scrollerHeight;

    const maxScroll = trackListContainer.scrollHeight - trackListContainer.clientHeight;
    trackListContainer.scrollTop = ratio * maxScroll;

    const thumbHeight = webFastThumb ? (webFastThumb.clientHeight || 44) : 44;
    const maxTravel = Math.max(0, scrollerHeight - thumbHeight);
    if (webFastThumb) {
      webFastThumb.style.transform = `translateY(${ratio * maxTravel}px)`;
    }

    const items = trackListContainer.querySelectorAll('.list-item');
    if (items.length > 0) {
      const targetIdx = Math.min(Math.floor(ratio * items.length), items.length - 1);
      const targetItem = items[targetIdx];
      const titleEl = targetItem ? targetItem.querySelector('.item-title') : null;
      let letter = '#';
      if (titleEl && titleEl.textContent) {
        const firstChar = titleEl.textContent.trim().charAt(0).toUpperCase();
        if (firstChar >= 'A' && firstChar <= 'Z') letter = firstChar;
      }

      if (webBubbleLetter) webBubbleLetter.textContent = letter;
      if (webBubbleCount) webBubbleCount.textContent = `${targetIdx + 1} / ${items.length}`;

      clearTimeout(webHideBubbleTimer);
      if (webFastBubble) webFastBubble.classList.add('is-active');
    }
  }

  if (webFastScroller) {
    let isDraggingScroller = false;

    webFastScroller.addEventListener('mousedown', (e) => {
      isDraggingScroller = true;
      handleFastScroll(e.clientY);
    });

    window.addEventListener('mousemove', (e) => {
      if (isDraggingScroller) {
        handleFastScroll(e.clientY);
      }
    });

    window.addEventListener('mouseup', () => {
      if (isDraggingScroller) {
        isDraggingScroller = false;
        clearTimeout(webHideBubbleTimer);
        webHideBubbleTimer = setTimeout(() => {
          if (webFastBubble) webFastBubble.classList.remove('is-active');
        }, 450);
      }
    });

    webFastScroller.addEventListener('touchstart', (e) => {
      if (e.touches && e.touches.length === 1) {
        isDraggingScroller = true;
        handleFastScroll(e.touches[0].clientY);
      }
    }, { passive: true });

    window.addEventListener('touchmove', (e) => {
      if (isDraggingScroller && e.touches && e.touches.length === 1) {
        handleFastScroll(e.touches[0].clientY);
      }
    }, { passive: true });

    window.addEventListener('touchend', () => {
      if (isDraggingScroller) {
        isDraggingScroller = false;
        clearTimeout(webHideBubbleTimer);
        webHideBubbleTimer = setTimeout(() => {
          if (webFastBubble) webFastBubble.classList.remove('is-active');
        }, 450);
      }
    }, { passive: true });
  }

  // =================== GESTIÓN Y REPRODUCCIÓN DEL ÁRBOL JERÁRQUICO ===================
  function playFolderTree(node) {
    const allPool = masterTracks.length > 0 ? masterTracks : sampleTracks;
    const nodeUsb = node.usbSource || (node.path && node.path.includes('udisk2') ? 'USB 2' : 'USB 1');
    let folderTracks = allPool.filter(t => {
      if (!isSameUsb(t, nodeUsb)) return false;
      if (t.folder === node.name) return true;
      if (t.path && node.path) {
        const normTrack = t.path.replace(/\/+$/, '');
        const normNode = node.path.replace(/\/+$/, '');
        if (normTrack === normNode) return true;
      }
      return false;
    });

    if (folderTracks.length === 0) {
      folderTracks = allPool.filter(t => t.path && node.path && t.path.startsWith(node.path));
    }

    sortTracksAlphabetically(folderTracks);

    currentPlaylist = folderTracks.length > 0 ? folderTracks : [...allPool];
    currentFolder = node.name;
    currentFolderUsbSource = node.usbSource || (node.path && node.path.includes('udisk2') ? 'USB 2' : 'USB 1');
    const usbTag = currentFolderUsbSource ? `[${currentFolderUsbSource}] ` : '';
    crumbFolder.textContent = `${usbTag}${node.name}`;
    songCountEl.textContent = currentPlaylist.length;

    // Abrir lista de pistas mostrando las canciones de la carpeta seleccionada (sin autoreproducir a pedido de Hernan)
    tabButtons[1].click();
  }

  function getAllTreeIds(nodes) {
    let ids = [];
    nodes.forEach(n => {
      ids.push(n.id);
      if (n.children && n.children.length > 0) {
        ids = ids.concat(getAllTreeIds(n.children));
      }
    });
    return ids;
  }

  // Botón "Colapsar Todo" (Solo icono, flechas hacia adentro)
  if (btnCollapseAllTree) {
    btnCollapseAllTree.addEventListener('click', (e) => {
      e.stopPropagation();
      expandedTreeNodes.clear();
      renderList();
    });
  }

  // Búsqueda en vivo inteligente
  let searchTimer = null;
  searchInput.addEventListener('input', () => {
    const val = searchInput.value.trim();
    if (val.length > 0) {
      clearSearch.classList.remove('hidden');
      if (searchSpinner) searchSpinner.classList.remove('hidden');
      crumbFolder.innerHTML = `⏳ Buscando: "<strong>${escapeHtml(val)}</strong>"...`;

      // EFECTO VISUAL INMEDIATO: Listado actual borroso y overlay flotante de carga
      trackListContainer.classList.add('is-searching');
      if (searchOverlayLoading) searchOverlayLoading.classList.remove('hidden');
      if (webFastScroller) webFastScroller.style.display = 'none';
    } else {
      clearSearch.classList.add('hidden');
      if (searchSpinner) searchSpinner.classList.add('hidden');
      trackListContainer.classList.remove('is-searching');
      if (searchOverlayLoading) searchOverlayLoading.classList.add('hidden');
      // Preservar el filtro que el usuario tiene activo (USB 1, USB 2, TODO, Carpeta)
      updateFilterChipsUI();
      renderList();
      return;
    }

    if (searchTimer) clearTimeout(searchTimer);
    searchTimer = setTimeout(() => {
      renderList();
      trackListContainer.classList.remove('is-searching');
      if (searchOverlayLoading) searchOverlayLoading.classList.add('hidden');
      if (searchSpinner) searchSpinner.classList.add('hidden');
    }, 180);
  });

  // Botón X para borrar búsqueda y ocultar teclado
  clearSearch.addEventListener('click', () => {
    if (searchTimer) clearTimeout(searchTimer);
    searchInput.value = '';
    clearSearch.classList.add('hidden');
    if (searchSpinner) searchSpinner.classList.add('hidden');
    trackListContainer.classList.remove('is-searching');
    if (searchOverlayLoading) searchOverlayLoading.classList.add('hidden');
    searchInput.blur();
    // Preservar el filtro que el usuario tiene activo (USB 1, USB 2, TODO, Carpeta)
    updateFilterChipsUI();
    renderList();
  });

  // Ocultar teclado virtual al presionar Enter en el buscador
  searchInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      searchInput.blur();
    }
  });

  crumbRoot.addEventListener('click', () => {
    tabButtons[0].click();
  });

  // =================== BOTÓN RE-ESCANEAR USB (NATIVO / AUTO) ===================
  const btnRescanUsb = document.getElementById('btnRescanUsb');
  const rescanLabel = document.getElementById('rescanLabel');

  if (btnRescanUsb) {
    btnRescanUsb.addEventListener('click', () => {
      btnRescanUsb.classList.add('is-spinning');
      if (rescanLabel) rescanLabel.textContent = "Escaneando...";

      // En Android nativo, re-escanear los dos pendrives udisk1 y udisk2
      if (window.AndroidBridge && window.AndroidBridge.rescanUsbDrives) {
        window.AndroidBridge.rescanUsbDrives();
      }

      setTimeout(() => {
        btnRescanUsb.classList.remove('is-spinning');
        if (rescanLabel) rescanLabel.textContent = "USB Actualizado";
        setTimeout(() => {
          if (rescanLabel) rescanLabel.textContent = "Re-escanear USB";
        }, 1600);
        renderList();
      }, 900);
    });
  }

  if (fileInput) {
    fileInput.addEventListener('change', (e) => {
      const files = Array.from(e.target.files).filter(f => f.type.startsWith('audio/') || f.name.endsWith('.mp3') || f.name.endsWith('.flac') || f.name.endsWith('.wav'));
      if (files.length === 0) return;
      currentPlaylist = files.map((file, i) => {
        let cleanName = file.name.replace(/\.[^/.]+$/, "");
        let parts = cleanName.split('-');
        return {
          id: Date.now() + i,
          title: parts.length > 1 ? parts[1].trim() : cleanName,
          artist: parts.length > 1 ? parts[0].trim() : "Pendrive USB",
          album: "USB Kingston",
          folder: "Mi Pendrive",
          path: "/storage/udisk1/",
          duration: "--:--",
          durationSec: 180,
          format: `${file.name.split('.').pop().toUpperCase()} 320K`,
          favorite: false,
          audioUrl: URL.createObjectURL(file)
        };
      });
      masterTracks = [...currentPlaylist];
      currentFolder = "Archivos USB";
      songCountEl.textContent = currentPlaylist.length;
      tabButtons[1].click();
      loadTrack(0, true);
    });
  }

  // =================== PANTALLA COMPLETA AUTO ===================
  if (btnToggleFullscreen && frameWrapper) {
    btnToggleFullscreen.addEventListener('click', () => {
      frameWrapper.classList.toggle('is-fullscreen');
    });
  }

  // =================== CONTROLES POR TECLADO ===================
  // Simulación de los botones del volante del auto
  window.addEventListener('keydown', (e) => {
    if (document.activeElement === searchInput) return;

    if (e.code === 'Space') {
      e.preventDefault();
      togglePlayPause();
    } else if (e.code === 'ArrowRight') {
      nextTrack();
    } else if (e.code === 'ArrowLeft') {
      prevTrack();
    } else if (e.code === 'ArrowUp') {
      e.preventDefault();
      setVolume(currentVolume + 5);
    } else if (e.code === 'ArrowDown') {
      e.preventDefault();
      setVolume(currentVolume - 5);
    } else if (e.key === 'm' || e.key === 'M') {
      toggleMute();
    }
  });

  // Constructor de árbol dinámico jerárquico a partir de canciones reales leídas por Android
  function buildTreeFromTracks(tracks) {
    if (!tracks || tracks.length === 0) return JSON.parse(JSON.stringify(defaultTreeFolders));

    const rootUsbMap = {};
    tracks.forEach(track => {
      const usb = track.usbSource || (track.path && track.path.toLowerCase().includes('udisk2') ? 'USB 2' : 'USB 1');
      const usbId = usb === 'USB 2' ? 'usb2' : 'usb1';
      const usbName = `${usb} (${usb === 'USB 2' ? 'udisk2' : 'udisk1'})`;
      const usbPath = usb === 'USB 2' ? '/storage/udisk2/' : '/storage/udisk1/';

      if (!rootUsbMap[usbId]) {
        rootUsbMap[usbId] = {
          id: usbId,
          name: usbName,
          isUsbRoot: true,
          usbSource: usb,
          path: usbPath,
          count: 0,
          directCount: 0,
          folders: {}
        };
      }
      rootUsbMap[usbId].count++;

      let relPath = track.path || '';
      if (relPath.startsWith(usbPath)) {
        relPath = relPath.slice(usbPath.length);
      }
      const parts = relPath.split('/').filter(p => p.trim().length > 0);
      if (parts.length > 0) {
        let curLvl = rootUsbMap[usbId].folders;
        let accumPath = usbPath;
        parts.forEach((p, idx) => {
          accumPath += p + '/';
          if (!curLvl[p]) {
            curLvl[p] = {
              id: `${usbId}_${p.replace(/[^a-zA-Z0-9]/g, '_')}_${idx}`,
              name: p,
              usbSource: usb,
              path: accumPath,
              count: 0,
              directCount: 0,
              childrenMap: {}
            };
          }
          curLvl[p].count++;
          if (idx === parts.length - 1) {
            curLvl[p].directCount++;
          }
          curLvl = curLvl[p].childrenMap;
        });
      } else {
        rootUsbMap[usbId].directCount++;
      }
    });

    function mapChildren(obj) {
      return Object.values(obj).map(item => ({
        id: item.id,
        name: item.name,
        usbSource: item.usbSource,
        path: item.path,
        count: item.count,
        directCount: item.directCount || 0,
        children: mapChildren(item.childrenMap)
      }));
    }

    return Object.values(rootUsbMap).map(u => ({
      id: u.id,
      name: u.name,
      isUsbRoot: true,
      usbSource: u.usbSource,
      path: u.path,
      count: u.count,
      directCount: u.directCount || 0,
      children: mapChildren(u.folders)
    }));
  }

  // Callback global invocado desde Android cuando se terminan de escanear los pendrives
  window.onUsbMusicLoaded = function(tracks) {
    if (!tracks || tracks.length === 0) {
      console.log("No se encontraron canciones en los pendrives.");
      return;
    }

    sortTracksAlphabetically(tracks);
    masterTracks = [...tracks];
    currentPlaylist = tracks;

    // Extraer todas las carpetas únicas de ambos pendrives
    const foldersMap = {};
    tracks.forEach(t => {
      const fName = t.folder || "Memoria USB";
      if (!foldersMap[fName]) {
        foldersMap[fName] = { 
          name: fName, 
          path: t.path, 
          count: 0,
          usbSource: t.usbSource || (t.path && t.path.toLowerCase().includes('udisk2') ? 'USB 2' : 'USB 1')
        };
      }
      foldersMap[fName].count++;
    });

    foldersList.length = 0;
    foldersList.push(...Object.values(foldersMap));

    // Reconstruir árbol jerárquico dinámico (colapsado por defecto)
    currentTreeData = buildTreeFromTracks(tracks);
    expandedTreeNodes = new Set();

    songCountEl.textContent = currentPlaylist.length;
    currentFolder = currentPlaylist[0].folder || "USB";
    currentFolderUsbSource = currentPlaylist[0].usbSource || (currentPlaylist[0].path && currentPlaylist[0].path.toLowerCase().includes('udisk2') ? 'USB 2' : 'USB 1');
    const usbTag = currentFolderUsbSource ? `[${currentFolderUsbSource}] ` : '';
    crumbFolder.textContent = `${usbTag}${currentFolder}`;



    // Cargar la primera canción real del pendrive
    loadTrack(0, false);
    renderList();
  };

  // Fin de canción en reproductor nativo de Android
  window.onNativeAudioEnded = function() {
    if (isRepeat) {
      loadTrack(currentTrackIndex, true);
    } else {
      nextTrack();
    }
  };

  // Botón manual para re-escanear pendrives
  const btnUsbLoad = document.getElementById('btnUsbLoad');
  const usbLoadLabel = document.getElementById('usbLoadLabel');
  if (btnUsbLoad) {
    btnUsbLoad.addEventListener('click', (e) => {
      if (window.AndroidBridge && window.AndroidBridge.scanUsbMusicAsync) {
        e.preventDefault();
        window.AndroidBridge.scanUsbMusicAsync();
        if (usbLoadLabel) usbLoadLabel.textContent = "Buscando en USBs...";
        setTimeout(() => {
          if (usbLoadLabel) usbLoadLabel.textContent = "Cargar desde Pendrive";
        }, 3000);
      }
    });
  }

  // Sincronizar volumen nativo y escanear pendrive si está en la pantalla del auto
  if (window.AndroidBridge) {
    try {
      if (window.AndroidBridge.getSystemVolume) {
        const sysVol = window.AndroidBridge.getSystemVolume();
        if (sysVol >= 0 && sysVol <= 100) setVolume(sysVol);
      }
      if (window.AndroidBridge.scanUsbMusicAsync) {
        window.AndroidBridge.scanUsbMusicAsync();
      }
    } catch(e) {
      console.warn("AndroidBridge inicialización:", e);
    }
  }

  // Inicialización
  songCountEl.textContent = currentPlaylist.length;
  if (searchInput) {
    searchInput.placeholder = (activeTab === 'folders') ? "Buscar entre carpetas..." : "Buscar canción, artista o álbum...";
  }
  loadTrack(0, false);
  renderList();
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initApp);
} else {
  initApp();
}
