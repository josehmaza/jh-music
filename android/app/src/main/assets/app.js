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
    }
  ];

  const foldersList = [
    { name: "JH PENDRIVE", path: "/storage/udisk1/JH PENDRIVE/", count: 12 },
    { name: "Descargas", path: "/storage/udisk1/Descargas/", count: 218 },
    { name: "2025-07-18", path: "/storage/udisk1/Descargas/2025-07-18/", count: 29 },
    { name: "Música Interna", path: "/mnt/sdcard/media/music/", count: 1 }
  ];

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
    formatBadge.textContent = track.format || "MP3";
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
    } else {
      playIcon.classList.remove('hidden');
      pauseIcon.classList.add('hidden');
      albumContainer.classList.remove('is-playing');
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

  // =================== PESTAÑAS Y RENDERIZADO ===================
  tabButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      tabButtons.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      activeTab = btn.getAttribute('data-tab');
      renderList();
    });
  });

  function renderList() {
    trackListContainer.innerHTML = '';
    const query = searchInput.value.toLowerCase().trim();

    if (activeTab === 'folders') {
      crumbFolder.textContent = "Carpetas USB";
      
      foldersList.forEach(folder => {
        if (query && !folder.name.toLowerCase().includes(query)) return;

        const folderEl = document.createElement('div');
        folderEl.className = 'list-item';
        folderEl.innerHTML = `
          <div class="item-left">
            <div class="item-icon-wrap">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:20px;height:20px;"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
            </div>
            <div class="item-info">
              <span class="item-title">${folder.name}</span>
              <span class="item-subtitle">${folder.path}</span>
            </div>
          </div>
          <div class="item-right">
            <span class="item-duration">${folder.count} pistas</span>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:16px;height:16px;color:var(--text-muted)"><polyline points="9 18 15 12 9 6"/></svg>
          </div>
        `;

        folderEl.addEventListener('click', () => {
          currentFolder = folder.name;
          crumbFolder.textContent = folder.name;
          // Filtrar playlist a esta carpeta
          const folderTracks = sampleTracks.filter(t => t.folder === folder.name);
          if (folderTracks.length > 0) {
            currentPlaylist = folderTracks;
          } else {
            currentPlaylist = [...sampleTracks];
          }
          // Cambiar a la pestaña de pistas
          tabButtons[1].click();
        });

        trackListContainer.appendChild(folderEl);
      });
    } 
    else if (activeTab === 'playlist' || activeTab === 'favorites') {
      let filtered = currentPlaylist;

      if (activeTab === 'favorites') {
        filtered = currentPlaylist.filter(t => t.favorite);
        crumbFolder.textContent = "Mis Favoritos";
      } else {
        crumbFolder.textContent = currentFolder;
      }

      if (query) {
        filtered = filtered.filter(t => 
          t.title.toLowerCase().includes(query) || 
          t.artist.toLowerCase().includes(query)
        );
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
              <span class="item-title">${track.title}</span>
              <span class="item-subtitle">${track.artist} • ${track.album || track.folder}</span>
            </div>
          </div>
          <div class="item-right">
            <span class="item-duration">${track.duration}</span>
            <button class="btn-item-fav ${track.favorite ? 'is-fav' : ''}" title="Marcar Favorito">
              <svg viewBox="0 0 24 24" fill="${track.favorite ? 'currentColor' : 'none'}" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
            </button>
          </div>
        `;

        // Click para reproducir
        itemEl.addEventListener('click', (e) => {
          if (e.target.closest('.btn-item-fav')) return;
          const realIdx = currentPlaylist.findIndex(t => t.id === track.id);
          loadTrack(realIdx, true);
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
  }

  // Búsqueda en vivo
  searchInput.addEventListener('input', () => {
    if (searchInput.value) {
      clearSearch.classList.remove('hidden');
    } else {
      clearSearch.classList.add('hidden');
    }
    renderList();
  });

  clearSearch.addEventListener('click', () => {
    searchInput.value = '';
    clearSearch.classList.add('hidden');
    renderList();
  });

  crumbRoot.addEventListener('click', () => {
    tabButtons[0].click();
  });

  btnPlayAll.addEventListener('click', () => {
    if (currentPlaylist.length > 0) {
      loadTrack(0, true);
    }
  });

  // =================== CARGA DE CANCIONES DESDE PENDRIVE REAL ===================
  fileInput.addEventListener('change', (e) => {
    const files = Array.from(e.target.files).filter(f => f.type.startsWith('audio/') || f.name.endsWith('.mp3') || f.name.endsWith('.flac') || f.name.endsWith('.wav'));
    
    if (files.length === 0) return;

    currentPlaylist = files.map((file, i) => {
      // Intentar extraer título y artista del nombre de archivo
      let cleanName = file.name.replace(/\.[^/.]+$/, "");
      let parts = cleanName.split('-');
      let title = parts.length > 1 ? parts[1].trim() : cleanName;
      let artist = parts.length > 1 ? parts[0].trim() : "Pendrive USB";

      return {
        id: Date.now() + i,
        title: title,
        artist: artist,
        album: "USB Kingston",
        folder: "Mi Pendrive",
        path: "/storage/udisk1/",
        duration: "--:--",
        durationSec: 180,
        format: file.name.split('.').pop().toUpperCase(),
        favorite: false,
        audioUrl: URL.createObjectURL(file)
      };
    });

    currentFolder = "Archivos USB";
    songCountEl.textContent = currentPlaylist.length;
    tabButtons[1].click(); // Ir a lista de pistas
    loadTrack(0, true);
  });

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
        foldersMap[fName] = { name: fName, path: t.path, count: 0 };
      }
      foldersMap[fName].count++;
    });

    foldersList.length = 0;
    foldersList.push(...Object.values(foldersMap));

    songCountEl.textContent = currentPlaylist.length;
    currentFolder = currentPlaylist[0].folder || "USB";
    crumbFolder.textContent = currentFolder;

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
