/**
 * JH Car Audio - Hyundai Tucson ix35
 * Lógica del reproductor de música para auto (simulación web interactiva)
 */

document.addEventListener('DOMContentLoaded', () => {

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
    }
  ];

  const foldersList = [
    { name: "JH PENDRIVE", path: "/storage/udisk1/JH PENDRIVE/", count: 12, usbSource: "USB 1" },
    { name: "Descargas", path: "/storage/udisk1/Descargas/", count: 218, usbSource: "USB 1" },
    { name: "2025-07-18", path: "/storage/udisk1/Descargas/2025-07-18/", count: 29, usbSource: "USB 1" },
    { name: "Música Viaje", path: "/storage/udisk2/Música Viaje/", count: 84, usbSource: "USB 2" },
    { name: "Música Interna", path: "/mnt/sdcard/media/music/", count: 5, usbSource: "INTERNA" }
  ];

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
  let currentFolder = "JH PENDRIVE";
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
  const crumbRoot = document.getElementById('crumbRoot');
  const crumbFolder = document.getElementById('crumbFolder');
  const btnPlayAll = document.getElementById('btnPlayAll');
  const fileInput = document.getElementById('fileInput');
  const btnToggleFullscreen = document.getElementById('btnToggleFullscreen');

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

    // Ruta
    currentPathLabel.textContent = `${track.path || '/storage/udisk1/'}${track.folder}/`;

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
        playbackStatusText.textContent = "REPRODUCIENDO";
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
    if (isShuffle) {
      const rand = Math.floor(Math.random() * currentPlaylist.length);
      loadTrack(rand, true);
    } else {
      loadTrack(currentTrackIndex + 1, true);
    }
  }

  function prevTrack() {
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
  if (btnToggleExpand && dashboardGrid) {
    btnToggleExpand.addEventListener('click', () => {
      isPlayerExpanded = !isPlayerExpanded;
      dashboardGrid.classList.toggle('is-expanded', isPlayerExpanded);
      btnToggleExpand.classList.toggle('is-active', !isPlayerExpanded);
      if (isPlayerExpanded) {
        btnToggleExpand.title = "Abrir buscador de archivos y pistas";
      } else {
        btnToggleExpand.title = "Ocultar buscador (reproductor a pantalla completa)";
      }
      setTimeout(refreshAllMarquees, 320);
    });
  }

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

  // Generador de etiquetas para USB 1, USB 2 y Memoria Interna
  function getStorageBadgeHtml(path) {
    if (!path) return '';
    const p = path.toLowerCase();
    if (p.includes('udisk1') || p.includes('usb1')) {
      return `<span class="badge-storage usb1">USB 1</span>`;
    } else if (p.includes('udisk2') || p.includes('usb2')) {
      return `<span class="badge-storage usb2">USB 2</span>`;
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
  const filterChips = document.querySelectorAll('.filter-chip');
  const chipFilterFolder = document.getElementById('chipFilterFolder');
  const chipFilterAll = document.getElementById('chipFilterAll');
  const chipFilterUsb1 = document.getElementById('chipFilterUsb1');
  const chipFilterUsb2 = document.getElementById('chipFilterUsb2');

  function updateFilterChipsUI() {
    if (activeTab === 'playlist' && currentFolder) {
      if (chipFilterFolder) {
        chipFilterFolder.classList.remove('hidden');
        const usbTag = currentFolderUsbSource ? `[${currentFolderUsbSource}] ` : '';
        const name = currentFolder.length > 13 ? currentFolder.slice(0, 11) + '…' : currentFolder;
        chipFilterFolder.textContent = `📁 ${usbTag}${name}`;
      }
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

    if (chipFilterUsb1) chipFilterUsb1.classList.toggle('hidden', !hasUsb1);
    if (chipFilterUsb2) chipFilterUsb2.classList.toggle('hidden', !hasUsb2);

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
      renderList();
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
  tabButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      searchInput.blur();
      tabButtons.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      activeTab = btn.getAttribute('data-tab');
      if (activeTab === 'playlist' && currentFolder) {
        currentSearchFilter = 'folder';
      } else if (currentSearchFilter === 'folder') {
        currentSearchFilter = 'all';
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
            <div class="item-right">
              <span class="item-duration">${folder.count}</span>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:16px;height:16px;color:var(--text-muted)"><polyline points="9 18 15 12 9 6"/></svg>
            </div>
          `;

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
          searchPool = currentPlaylist.length > 0 ? currentPlaylist : allPool.filter(t => t.folder === currentFolder);
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
            <button class="btn-clear-search-action" id="btnClearSearchAction">Borrar búsqueda y ver todo</button>
          </div>
        `;
        const btnReset = document.getElementById('btnClearSearchAction');
        if (btnReset) {
          btnReset.addEventListener('click', () => {
            searchInput.value = '';
            clearSearch.classList.add('hidden');
            searchInput.blur();
            currentSearchFilter = 'all';
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

          // 4. Actualizar el chip de carpeta para que muestre el nuevo nombre con USB
          if (chipFilterFolder) {
            const usbTag = currentFolderUsbSource ? `[${currentFolderUsbSource}] ` : '';
            const name = currentFolder.length > 13 ? currentFolder.slice(0, 11) + '…' : currentFolder;
            chipFilterFolder.textContent = `📁 ${usbTag}${name}`;
            chipFilterFolder.classList.remove('hidden');
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
        // 1 toque: Mostrar/ocultar nombre completo
        // 2 toques: Reproducir la canción de inmediato
        const infoEl = itemEl.querySelector('.item-info');
        infoEl.setAttribute('title', '1 toque: ver nombre completo • 2 toques: reproducir');
        let lastInfoClickTime = 0;
        infoEl.addEventListener('click', (e) => {
          e.stopPropagation();
          searchInput.blur();
          const now = Date.now();
          if (now - lastInfoClickTime < 450) {
            // Doble toque: REPRODUCIR LA CANCIÓN
            lastInfoClickTime = 0;
            playBtn.click();
            return;
          }
          lastInfoClickTime = now;
          const wasExpanded = itemEl.classList.contains('is-expanded');
          // Cerrar cualquier otra pista que esté abierta (solo 1 abierta a la vez)
          trackListContainer.querySelectorAll('.list-item.is-expanded').forEach(el => {
            el.classList.remove('is-expanded');
          });
          if (!wasExpanded) {
            itemEl.classList.add('is-expanded');
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

    // MODO NORMAL (SIN BÚSQUEDA)
    if (activeTab === 'folders') {
      let fList = foldersList;
      if (currentSearchFilter === 'usb1') {
        fList = foldersList.filter(f => f.path.includes('udisk1') || f.path.includes('usb0') || f.badge === 'USB 1');
        crumbFolder.textContent = `Carpetas USB 1 (${fList.length})`;
      } else if (currentSearchFilter === 'usb2') {
        fList = foldersList.filter(f => f.path.includes('udisk2') || f.path.includes('usb1') || f.badge === 'USB 2');
        crumbFolder.textContent = `Carpetas USB 2 (${fList.length})`;
      } else {
        crumbFolder.textContent = `Carpetas USB (${fList.length})`;
      }
      
      fList.forEach(folder => {
        const folderEl = document.createElement('div');
        folderEl.className = 'list-item';
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
          <div class="item-right">
            <span class="item-duration">${folder.count}</span>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:16px;height:16px;color:var(--text-muted)"><polyline points="9 18 15 12 9 6"/></svg>
          </div>
        `;

        folderEl.addEventListener('click', () => {
          searchInput.blur();
          currentFolder = folder.name;
          currentFolderUsbSource = folder.usbSource || folder.badge || (folder.path && folder.path.toLowerCase().includes('udisk2') ? 'USB 2' : 'USB 1');
          crumbFolder.textContent = `${currentFolderUsbSource ? '[' + currentFolderUsbSource + '] ' : ''}${folder.name}`;
          currentSearchFilter = 'folder';
          
          const allPool = masterTracks.length > 0 ? masterTracks : sampleTracks;
          const folderTracks = allPool.filter(t => t.folder === folder.name);
          if (folderTracks.length > 0) {
            currentPlaylist = folderTracks;
          } else {
            currentPlaylist = [...allPool];
          }
          // Cambiar a la pestaña de pistas
          tabButtons[1].click();
        });

        trackListContainer.appendChild(folderEl);
      });
    } 
    else if (activeTab === 'playlist' || activeTab === 'favorites') {
      let filtered = currentPlaylist;
      const allPool = masterTracks.length > 0 ? masterTracks : sampleTracks;

      if (activeTab === 'favorites') {
        let favPool = allPool.filter(t => t.favorite);
        if (currentSearchFilter === 'usb1') {
          favPool = favPool.filter(t => t.usbSource === 'USB 1' || (t.path && (t.path.includes('udisk1') || t.path.includes('usb0'))));
        } else if (currentSearchFilter === 'usb2') {
          favPool = favPool.filter(t => t.usbSource === 'USB 2' || (t.path && (t.path.includes('udisk2') || t.path.includes('usb1'))));
        }
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
          filtered = allPool;
          crumbFolder.textContent = `Todas las Pistas (${filtered.length})`;
        } else {
          filtered = currentPlaylist;
          const usbTag = currentFolderUsbSource ? `[${currentFolderUsbSource}] ` : '';
        crumbFolder.textContent = `${usbTag}${currentFolder} (${filtered.length})`;
        }
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
        // 2 toques: Reproducir la canción de inmediato
        const infoEl = itemEl.querySelector('.item-info');
        infoEl.setAttribute('title', '1 toque: ver nombre completo • 2 toques: reproducir');
        let lastInfoClickTime = 0;
        infoEl.addEventListener('click', (e) => {
          e.stopPropagation();
          searchInput.blur();
          const now = Date.now();
          if (now - lastInfoClickTime < 450) {
            // Doble toque: REPRODUCIR LA CANCIÓN
            lastInfoClickTime = 0;
            playBtn.click();
            return;
          }
          lastInfoClickTime = now;
          const wasExpanded = itemEl.classList.contains('is-expanded');
          // Cerrar cualquier otra pista que esté abierta (solo 1 abierta a la vez)
          trackListContainer.querySelectorAll('.list-item.is-expanded').forEach(el => {
            el.classList.remove('is-expanded');
          });
          if (!wasExpanded) {
            itemEl.classList.add('is-expanded');
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
  }

  // Búsqueda en vivo inteligente
  let searchTimer = null;
  searchInput.addEventListener('input', () => {
    const val = searchInput.value.trim();
    if (val.length > 0) {
      clearSearch.classList.remove('hidden');
      if (searchSpinner) searchSpinner.classList.remove('hidden');
      crumbFolder.innerHTML = `⏳ Buscando: "<strong>${escapeHtml(val)}</strong>"...`;
    } else {
      clearSearch.classList.add('hidden');
      if (searchSpinner) searchSpinner.classList.add('hidden');
      if (currentFolder) {
        currentSearchFilter = 'folder';
      }
      updateFilterChipsUI();
      renderList();
      return;
    }

    if (searchTimer) clearTimeout(searchTimer);
    searchTimer = setTimeout(() => {
      renderList();
      if (searchSpinner) searchSpinner.classList.add('hidden');
    }, 120);
  });

  // Botón X para borrar búsqueda y ocultar teclado
  clearSearch.addEventListener('click', () => {
    if (searchTimer) clearTimeout(searchTimer);
    searchInput.value = '';
    clearSearch.classList.add('hidden');
    if (searchSpinner) searchSpinner.classList.add('hidden');
    searchInput.blur();
    // Al limpiar la búsqueda, mostrar las canciones de la carpeta que fue seteada al darle play
    if (currentFolder) {
      currentSearchFilter = 'folder';
    } else {
      currentSearchFilter = 'all';
    }
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

  btnPlayAll.addEventListener('click', () => {
    if (currentPlaylist.length > 0) {
      loadTrack(0, true);
    }
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
  btnToggleFullscreen.addEventListener('click', () => {
    frameWrapper.classList.toggle('is-fullscreen');
  });

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

  // Callback global invocado desde Android cuando se terminan de escanear los pendrives
  window.onUsbMusicLoaded = function(tracks) {
    if (!tracks || tracks.length === 0) {
      console.log("No se encontraron canciones en los pendrives.");
      return;
    }

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

    songCountEl.textContent = currentPlaylist.length;
    currentFolder = currentPlaylist[0].folder || "USB";
    currentFolderUsbSource = currentPlaylist[0].usbSource || (currentPlaylist[0].path && currentPlaylist[0].path.toLowerCase().includes('udisk2') ? 'USB 2' : 'USB 1');
    const usbTag = currentFolderUsbSource ? `[${currentFolderUsbSource}] ` : '';
    crumbFolder.textContent = `${usbTag}${currentFolder}`;

    // Actualizar texto del almacenamiento en el footer
    const storageInfo = document.querySelector('.storage-info span');
    if (storageInfo) {
      storageInfo.textContent = `Pendrives USB • ${tracks.length} canciones encontradas`;
    }

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
  loadTrack(0, false);
  renderList();
});
