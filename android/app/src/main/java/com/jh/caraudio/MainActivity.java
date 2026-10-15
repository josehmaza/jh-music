package com.jh.caraudio;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private static final int PERM_REQUEST_CODE = 2024;

    // Barra Superior
    private TextView tvHeaderPath, tvClock;
    private Button btnRescanUsb;

    // Franja de Volumen Conductor
    private ImageButton btnNavBack;
    private ImageButton btnVolMute;
    private TextView tvVolPercent;
    private Button btnVolUp, btnVolDown;
    private VerticalVolumeBar seekVolume;
    private int currentVolPercent = 75;

    // Reproductor Central
    private TextView tvPlaybackStatus;
    private ImageButton btnToggleExplorer;
    private ImageView ivAlbumArt;
    private SoundWaveBarsView soundWaveBars;
    private TextView tvFormatBadge;
    private TextView tvTrackTitle, tvTrackArtist, tvTrackAlbum;
    private View layoutPrevCard, layoutNextCard;
    private TextView tvPrevTrackPreview, tvNextTrackPreview;
    private SeekBar seekTrackProgress;
    private TextView tvTimeCurrent, tvTimeTotal;
    private ImageButton btnShuffle, btnPrev, btnPlayPause, btnNext, btnRepeat;

    // Explorador Derecho
    private LinearLayout layoutExplorerPanel;
    private Button tabFolders, tabTracks, tabFavorites;
    private EditText etSearch;
    private ImageButton btnSearchClear;
    private ProgressBar pbSearchLoading;
    private TextView tvCurrentPath, tvStorageStatus;
    private Button btnPlayAll;
    private ListView listViewContent;

    // Adaptadores
    private TrackAdapter trackAdapter;
    private FolderAdapter folderAdapter;

    // Listas de datos
    private List<Track> allTracks = new ArrayList<>();
    private List<Track> currentPlaylist = new ArrayList<>();
    private List<Track> displayedTracks = new ArrayList<>();
    private List<FolderItem> folderList = new ArrayList<>();
    private List<FolderItem> displayedFolders = new ArrayList<>();

    // Estado del Reproductor
    private int currentTrackIndex = -1;
    private boolean isPlaying = false;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private boolean isMuted = false;
    private int currentTab = 0; // 0: Carpetas, 1: Pistas, 2: Favoritos
    private String currentSelectedFolder = null;

    // Filtros de Búsqueda y USBs
    public static final int FILTER_FOLDER = 0; // Esta Carpeta
    public static final int FILTER_ALL = 1;    // TODO
    public static final int FILTER_USB1 = 2;   // USB 1
    public static final int FILTER_USB2 = 3;   // USB 2

    private int currentSearchFilter = FILTER_FOLDER;
    private boolean hasUsb1 = false;
    private boolean hasUsb2 = false;

    private TextView btnFilterFolder;
    private TextView btnFilterAll;
    private TextView btnFilterUsb1;
    private TextView btnFilterUsb2;

    // Búsqueda Asíncrona con Debounce
    private final Handler searchDebounceHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable = null;
    private final ExecutorService searchExecutor = Executors.newSingleThreadExecutor();
    private volatile int searchVersion = 0;

    // Servicios del Sistema
    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable progressRunnable;
    private Runnable clockRunnable;
    private ExecutorService scanExecutor;
    private boolean isUserSeeking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler(Looper.getMainLooper());
        scanExecutor = Executors.newSingleThreadExecutor();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Modo pantalla completa inmersiva para pantalla de auto
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setImmersiveMode();

        setContentView(R.layout.activity_main);

        initViews();
        setupClock();
        setupVolumeControls();
        setupPlayerControls();
        setupExplorerControls();
        setupProgressUpdater();

        checkAndRequestStoragePermissions();
    }

    private void initViews() {
        // Barra Superior
        tvHeaderPath = findViewById(R.id.tvHeaderPath);
        tvClock = findViewById(R.id.tvClock);
        btnRescanUsb = findViewById(R.id.btnRescanUsb);

        // Franja de volumen (54dp)
        btnNavBack = findViewById(R.id.btnNavBack);
        btnVolMute = findViewById(R.id.btnVolMute);
        tvVolPercent = findViewById(R.id.tvVolPercent);
        btnVolUp = findViewById(R.id.btnVolUp);
        btnVolDown = findViewById(R.id.btnVolDown);
        seekVolume = findViewById(R.id.seekVolume);

        // Reproductor central
        tvPlaybackStatus = findViewById(R.id.tvPlaybackStatus);
        btnToggleExplorer = findViewById(R.id.btnToggleExplorer);
        ivAlbumArt = findViewById(R.id.ivAlbumArt);
        soundWaveBars = findViewById(R.id.soundWaveBars);
        tvFormatBadge = findViewById(R.id.tvFormatBadge);
        tvTrackTitle = findViewById(R.id.tvTrackTitle);
        tvTrackArtist = findViewById(R.id.tvTrackArtist);
        tvTrackAlbum = findViewById(R.id.tvTrackAlbum);
        layoutPrevCard = findViewById(R.id.layoutPrevCard);
        layoutNextCard = findViewById(R.id.layoutNextCard);
        tvPrevTrackPreview = findViewById(R.id.tvPrevTrackPreview);
        tvNextTrackPreview = findViewById(R.id.tvNextTrackPreview);
        seekTrackProgress = findViewById(R.id.seekTrackProgress);
        tvTimeCurrent = findViewById(R.id.tvTimeCurrent);
        tvTimeTotal = findViewById(R.id.tvTimeTotal);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnPrev = findViewById(R.id.btnPrev);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnRepeat = findViewById(R.id.btnRepeat);

        // Explorador derecho
        layoutExplorerPanel = findViewById(R.id.layoutExplorerPanel);
        tabFolders = findViewById(R.id.tabFolders);
        tabTracks = findViewById(R.id.tabTracks);
        tabFavorites = findViewById(R.id.tabFavorites);
        etSearch = findViewById(R.id.etSearch);
        btnSearchClear = findViewById(R.id.btnSearchClear);
        pbSearchLoading = findViewById(R.id.pbSearchLoading);
        tvCurrentPath = findViewById(R.id.tvCurrentPath);
        tvStorageStatus = findViewById(R.id.tvStorageStatus);
        btnPlayAll = findViewById(R.id.btnPlayAll);
        listViewContent = findViewById(R.id.listViewContent);

        // Filtros de búsqueda (Esta Carpeta, TODO, USB 1, USB 2)
        btnFilterFolder = findViewById(R.id.btnFilterFolder);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterUsb1 = findViewById(R.id.btnFilterUsb1);
        btnFilterUsb2 = findViewById(R.id.btnFilterUsb2);
        checkUsbPresence();

        // Adaptadores con listeners directos en la fila (evita robo de focus)
        trackAdapter = new TrackAdapter(this, displayedTracks,
                (track, position) -> {
                    hideKeyboard();
                    playTrack(position);
                },
                (track, position) -> {
                    track.setFavorite(!track.isFavorite());
                }
        );

        folderAdapter = new FolderAdapter(this, displayedFolders,
                (folder, position) -> {
                    hideKeyboard();
                    openFolder(folder.getName());
                }
        );

        listViewContent.setAdapter(folderAdapter);
    }

    private void setupClock() {
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                if (tvClock != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    tvClock.setText(sdf.format(new Date()));
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(clockRunnable);
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view == null) view = etSearch;
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            view.clearFocus();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    hideKeyboard();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        hideKeyboard();

        // 1. Si hay texto en el buscador, borrar búsqueda y restaurar vista
        if (etSearch != null && etSearch.getText().length() > 0) {
            etSearch.setText("");
            if (btnSearchClear != null) btnSearchClear.setVisibility(View.GONE);
            filterSearch("");
            return;
        }

        // 2. Si estamos dentro de una carpeta en Pistas (tab 1), volver a la lista de Carpetas (tab 0)
        if (currentTab == 1 && currentSelectedFolder != null) {
            selectTab(0);
            return;
        }

        // 3. Si estamos en Favoritos (tab 2), volver a Carpetas (tab 0)
        if (currentTab == 2) {
            selectTab(0);
            return;
        }

        // 4. Si ya estamos en la raíz (tab 0), realizar la acción física del sistema (salir / minimizar al launcher del auto)
        super.onBackPressed();
    }

    // ==========================================
    // SISTEMA DE VOLUMEN RÁPIDO CONDUCTOR
    // Pasos de 5 en 5 y barra vertical sin interferencias
    // ==========================================
    private void setupVolumeControls() {
        updateVolumeUI();

        // Botón físico Atrás en pantalla para visión nocturna
        btnNavBack.setOnClickListener(v -> onBackPressed());

        btnVolUp.setOnClickListener(v -> adjustVolumeStep(true));
        btnVolDown.setOnClickListener(v -> adjustVolumeStep(false));
        btnVolMute.setOnClickListener(v -> toggleMute());

        seekVolume.setOnVolumeChangeListener((percent, fromUser) -> {
            if (fromUser && audioManager != null) {
                currentVolPercent = percent;
                int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                int target = Math.round((percent / 100f) * max);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0);
                tvVolPercent.setText(percent + "%");
            }
        });
    }

    private void updateVolumeUI() {
        if (audioManager == null) return;
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int percent = max > 0 ? Math.round(((float) current / max) * 100) : 0;
        currentVolPercent = percent;

        tvVolPercent.setText(percent + "%");
        seekVolume.setProgress(percent);
    }

    private void adjustVolumeStep(boolean up) {
        if (audioManager == null) return;
        // Incremento / decremento de 5 en 5 exacto a pedido de Hernan
        int nextPercent = up ? Math.min(100, currentVolPercent + 5) : Math.max(0, currentVolPercent - 5);
        currentVolPercent = nextPercent;

        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int target = Math.round((currentVolPercent / 100f) * max);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0);

        seekVolume.setProgress(currentVolPercent);
        tvVolPercent.setText(currentVolPercent + "%");
    }

    private void toggleMute() {
        if (audioManager == null) return;
        isMuted = !isMuted;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    isMuted ? AudioManager.ADJUST_MUTE : AudioManager.ADJUST_UNMUTE, 0);
        } else {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, isMuted);
        }
        btnVolMute.setSelected(isMuted);
        tvVolPercent.setText(isMuted ? "MUTE" : (currentVolPercent + "%"));
    }

    // ==========================================
    // CONTROLES DE REPRODUCCIÓN (MEDIA PLAYER)
    // ==========================================
    private void setupPlayerControls() {
        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnNext.setOnClickListener(v -> playNextTrack());
        btnPrev.setOnClickListener(v -> playPreviousTrack());

        // Tarjetas flotantes anterior y siguiente
        if (layoutPrevCard != null) layoutPrevCard.setOnClickListener(v -> playPreviousTrack());
        if (layoutNextCard != null) layoutNextCard.setOnClickListener(v -> playNextTrack());

        // Botón alternar explorador de música (mostrar u ocultar panel derecho)
        if (btnToggleExplorer != null) {
            btnToggleExplorer.setOnClickListener(v -> {
                hideKeyboard();
                if (layoutExplorerPanel.getVisibility() == View.VISIBLE) {
                    layoutExplorerPanel.setVisibility(View.GONE);
                } else {
                    layoutExplorerPanel.setVisibility(View.VISIBLE);
                }
            });
        }

        btnShuffle.setOnClickListener(v -> {
            isShuffle = !isShuffle;
            btnShuffle.setSelected(isShuffle);
            Toast.makeText(this, isShuffle ? "Aleatorio activado" : "Modo secuencial", Toast.LENGTH_SHORT).show();
        });

        btnRepeat.setOnClickListener(v -> {
            isRepeat = !isRepeat;
            btnRepeat.setSelected(isRepeat);
            Toast.makeText(this, isRepeat ? "Repetir canción actual" : "Repetir carpeta", Toast.LENGTH_SHORT).show();
        });

        seekTrackProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    int duration = mediaPlayer.getDuration();
                    if (duration > 0) {
                        int currentMs = (int) ((progress / 100f) * duration);
                        tvTimeCurrent.setText(formatMs(currentMs));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
                if (mediaPlayer != null) {
                    int duration = mediaPlayer.getDuration();
                    if (duration > 0) {
                        int targetMs = (int) ((seekBar.getProgress() / 100f) * duration);
                        mediaPlayer.seekTo(targetMs);
                    }
                }
            }
        });
    }

    private void updatePlaybackStateUI() {
        if (isPlaying) {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            tvPlaybackStatus.setText("● REPRODUCIENDO");
            tvPlaybackStatus.setTextColor(Color.parseColor("#38bdf8"));
            if (soundWaveBars != null) soundWaveBars.setPlaying(true);
        } else {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            tvPlaybackStatus.setText("● EN PAUSA");
            tvPlaybackStatus.setTextColor(Color.parseColor("#f59e0b"));
            if (soundWaveBars != null) soundWaveBars.setPlaying(false);
        }
        // Activar marquesina en el título
        tvTrackTitle.setSelected(isPlaying);
    }

    // ==========================================
    // EXPLORADOR Y BÚSQUEDA INTELIGENTE
    // ==========================================
    private void setupExplorerControls() {
        tabFolders.setOnClickListener(v -> {
            hideKeyboard();
            selectTab(0);
        });
        tabTracks.setOnClickListener(v -> {
            hideKeyboard();
            selectTab(1);
        });
        tabFavorites.setOnClickListener(v -> {
            hideKeyboard();
            selectTab(2);
        });

        // Listeners de Chips de Filtro (Esta Carpeta, TODO, USB 1, USB 2)
        if (btnFilterFolder != null) {
            btnFilterFolder.setOnClickListener(v -> {
                hideKeyboard();
                setSearchFilter(FILTER_FOLDER);
            });
        }
        if (btnFilterAll != null) {
            btnFilterAll.setOnClickListener(v -> {
                hideKeyboard();
                setSearchFilter(FILTER_ALL);
            });
        }
        if (btnFilterUsb1 != null) {
            btnFilterUsb1.setOnClickListener(v -> {
                hideKeyboard();
                setSearchFilter(FILTER_USB1);
            });
        }
        if (btnFilterUsb2 != null) {
            btnFilterUsb2.setOnClickListener(v -> {
                hideKeyboard();
                setSearchFilter(FILTER_USB2);
            });
        }

        btnRescanUsb.setOnClickListener(v -> {
            hideKeyboard();
            Toast.makeText(this, "Escaneando pendrives USB...", Toast.LENGTH_SHORT).show();
            startUsbScan();
        });

        btnPlayAll.setOnClickListener(v -> {
            hideKeyboard();
            if (!displayedTracks.isEmpty()) {
                playTrack(0);
            }
        });

        // Búsqueda inteligente optimizada: debounce de 350ms para no trabar el teclado
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String q = s.toString();
                btnSearchClear.setVisibility(q.trim().length() > 0 ? View.VISIBLE : View.GONE);

                // Cancelar búsqueda anterior encolada
                if (searchRunnable != null) {
                    searchDebounceHandler.removeCallbacks(searchRunnable);
                }
                searchVersion++;
                final int thisVersion = searchVersion;

                if (q.trim().isEmpty()) {
                    filterSearch("");
                    return;
                }

                // Esperar a que el usuario termine de teclear (350ms)
                searchRunnable = () -> performAsyncSearch(q, thisVersion);
                searchDebounceHandler.postDelayed(searchRunnable, 350);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSearchClear.setOnClickListener(v -> {
            if (searchRunnable != null) {
                searchDebounceHandler.removeCallbacks(searchRunnable);
            }
            searchVersion++;
            etSearch.setText("");
            btnSearchClear.setVisibility(View.GONE);
            if (pbSearchLoading != null) pbSearchLoading.setVisibility(View.GONE);
            hideKeyboard();
            filterSearch("");
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                if (searchRunnable != null) {
                    searchDebounceHandler.removeCallbacks(searchRunnable);
                }
                searchVersion++;
                performAsyncSearch(etSearch.getText().toString(), searchVersion);
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    private void checkUsbPresence() {
        String[] u1Paths = {"/storage/udisk1", "/mnt/media_rw/udisk1", "/mnt/udisk1", "/storage/usb0"};
        String[] u2Paths = {"/storage/udisk2", "/mnt/media_rw/udisk2", "/mnt/udisk2", "/storage/usb1"};
        for (String p : u1Paths) {
            if (new File(p).exists()) { hasUsb1 = true; break; }
        }
        for (String p : u2Paths) {
            if (new File(p).exists()) { hasUsb2 = true; break; }
        }
        for (Track t : allTracks) {
            if ("USB 1".equalsIgnoreCase(t.getUsbSource()) || (t.getFilePath() != null && (t.getFilePath().contains("udisk1") || t.getFilePath().contains("usb0")))) {
                hasUsb1 = true;
            }
            if ("USB 2".equalsIgnoreCase(t.getUsbSource()) || (t.getFilePath() != null && (t.getFilePath().contains("udisk2") || t.getFilePath().contains("usb1")))) {
                hasUsb2 = true;
            }
        }
        updateFilterChipsVisibility();
    }

    private void setSearchFilter(int filter) {
        currentSearchFilter = filter;
        updateFilterChipsUI();
        filterSearch(etSearch.getText().toString());
    }

    private void updateFilterChipsUI() {
        if (btnFilterFolder != null) {
            boolean sel = (currentSearchFilter == FILTER_FOLDER);
            btnFilterFolder.setBackgroundResource(sel ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
            btnFilterFolder.setTextColor(sel ? Color.parseColor("#0d0f14") : Color.parseColor("#94a3b8"));
            btnFilterFolder.setTypeface(null, sel ? Typeface.BOLD : Typeface.NORMAL);
        }

        if (btnFilterAll != null) {
            boolean sel = (currentSearchFilter == FILTER_ALL);
            btnFilterAll.setBackgroundResource(sel ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
            btnFilterAll.setTextColor(sel ? Color.parseColor("#0d0f14") : Color.parseColor("#94a3b8"));
            btnFilterAll.setTypeface(null, sel ? Typeface.BOLD : Typeface.NORMAL);
        }

        if (btnFilterUsb1 != null) {
            boolean sel = (currentSearchFilter == FILTER_USB1);
            btnFilterUsb1.setBackgroundResource(sel ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
            btnFilterUsb1.setTextColor(sel ? Color.parseColor("#0d0f14") : Color.parseColor("#94a3b8"));
            btnFilterUsb1.setTypeface(null, sel ? Typeface.BOLD : Typeface.NORMAL);
        }

        if (btnFilterUsb2 != null) {
            boolean sel = (currentSearchFilter == FILTER_USB2);
            btnFilterUsb2.setBackgroundResource(sel ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
            btnFilterUsb2.setTextColor(sel ? Color.parseColor("#0d0f14") : Color.parseColor("#94a3b8"));
            btnFilterUsb2.setTypeface(null, sel ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void updateFilterChipsVisibility() {
        if (btnFilterFolder != null) {
            if (currentTab == 1 && currentSelectedFolder != null) {
                btnFilterFolder.setVisibility(View.VISIBLE);
                String label = currentSelectedFolder.length() > 14 ? currentSelectedFolder.substring(0, 12) + "…" : currentSelectedFolder;
                btnFilterFolder.setText("📁 " + label);
            } else {
                btnFilterFolder.setVisibility(View.GONE);
            }
        }

        if (btnFilterAll != null) {
            btnFilterAll.setVisibility(View.VISIBLE);
        }

        if (btnFilterUsb1 != null) {
            btnFilterUsb1.setVisibility(hasUsb1 ? View.VISIBLE : View.GONE);
        }

        if (btnFilterUsb2 != null) {
            btnFilterUsb2.setVisibility(hasUsb2 ? View.VISIBLE : View.GONE);
        }

        updateFilterChipsUI();
    }

    private void selectTab(int tabIndex) {
        currentTab = tabIndex;
        tabFolders.setBackgroundResource(tabIndex == 0 ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
        tabFolders.setTextColor(tabIndex == 0 ? Color.parseColor("#38bdf8") : Color.parseColor("#94a3b8"));

        tabTracks.setBackgroundResource(tabIndex == 1 ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
        tabTracks.setTextColor(tabIndex == 1 ? Color.parseColor("#38bdf8") : Color.parseColor("#94a3b8"));

        tabFavorites.setBackgroundResource(tabIndex == 2 ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
        tabFavorites.setTextColor(tabIndex == 2 ? Color.parseColor("#38bdf8") : Color.parseColor("#94a3b8"));

        if (tabIndex == 0) {
            listViewContent.setAdapter(folderAdapter);
            tvCurrentPath.setText("📂 Carpetas USB");
            if (currentSearchFilter == FILTER_FOLDER) {
                currentSearchFilter = FILTER_ALL;
            }
        } else if (tabIndex == 1) {
            listViewContent.setAdapter(trackAdapter);
            tvCurrentPath.setText(currentSelectedFolder != null ? "📁 " + currentSelectedFolder : "🎵 Todas las Pistas");
            if (currentSelectedFolder != null) {
                currentSearchFilter = FILTER_FOLDER;
            } else if (currentSearchFilter == FILTER_FOLDER) {
                currentSearchFilter = FILTER_ALL;
            }
        } else {
            listViewContent.setAdapter(trackAdapter);
            tvCurrentPath.setText("⭐ Mis Favoritos");
            if (currentSearchFilter == FILTER_FOLDER) {
                currentSearchFilter = FILTER_ALL;
            }
        }

        updateFilterChipsVisibility();
        filterByTab();
    }

    private void openFolder(String folderName) {
        currentSelectedFolder = folderName;
        currentPlaylist.clear();
        for (Track t : allTracks) {
            if (t.getFolder().equalsIgnoreCase(folderName)) {
                currentPlaylist.add(t);
            }
        }

        currentSearchFilter = FILTER_FOLDER;
        selectTab(1);
        tvCurrentPath.setText("📁 " + folderName);
        tvHeaderPath.setText("/" + folderName);
    }

    private void filterByTab() {
        filterSearch(etSearch.getText().toString());
    }

    // ==========================================
    // ALGORITMO DE BÚSQUEDA INTELIGENTE
    // Normalización de tildes + deduplicación de letras + fuzzy match
    // ==========================================
    private String normalizeText(String text) {
        if (text == null) return "";
        String n = Normalizer.normalize(text, Normalizer.Form.NFD);
        return n.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase().trim();
    }

    private String dedupChars(String s) {
        if (s == null) return "";
        return s.replaceAll("([a-z])\\1+", "$1");
    }

    private int levenshtein(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++) costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    private boolean tokenMatches(String token, String word) {
        if (token == null || word == null) return false;
        if (word.contains(token)) return true;

        String tDedup = dedupChars(token);
        String wDedup = dedupChars(word);
        if (wDedup.contains(tDedup)) return true;
        if (tDedup.contains(wDedup) && wDedup.length() >= 4) return true;

        int maxDist = token.length() >= 6 ? 2 : (token.length() >= 4 ? 1 : 0);
        if (maxDist > 0 && Math.abs(token.length() - word.length()) <= maxDist) {
            if (levenshtein(token, word) <= maxDist) return true;
        }
        if (word.startsWith(token) || (token.length() >= 4 && word.startsWith(token.substring(0, token.length() - 1)))) {
            return true;
        }
        return false;
    }

    private boolean fastTrackMatches(Track track, String[] tokens) {
        String haystack = normalizeText(track.getTitle() + " " + track.getArtist() + " " + track.getAlbum() + " " + track.getFolder() + " " + track.getFilePath());
        String haystackDedup = dedupChars(haystack);

        for (String token : tokens) {
            if (token.isEmpty()) continue;
            // Búsqueda ultra rápida en subcadenas (<0.01ms por pista)
            if (haystack.contains(token)) continue;
            if (haystackDedup.contains(dedupChars(token))) continue;
            return false;
        }
        return true;
    }

    private int calculateTrackRelevance(Track track, String normQuery, String[] tokens) {
        int score = 0;
        String titleNorm = normalizeText(track.getTitle());
        String artistNorm = normalizeText(track.getArtist());
        String albumNorm = normalizeText(track.getAlbum());
        String folderNorm = normalizeText(track.getFolder());

        String[] titleWords = titleNorm.split("[^a-z0-9]+");
        String[] artistWords = artistNorm.split("[^a-z0-9]+");

        if (titleNorm.equals(normQuery)) {
            score += 25000;
        } else if (titleNorm.startsWith(normQuery + " ") || titleNorm.startsWith(normQuery + "-")) {
            score += 18000;
        } else if (titleNorm.startsWith(normQuery)) {
            score += 12000;
        } else if (titleNorm.contains(normQuery)) {
            score += 6000;
        }

        for (String token : tokens) {
            if (token.isEmpty()) continue;
            boolean wordExact = false;
            boolean wordPrefix = false;
            for (String w : titleWords) {
                if (w.equals(token)) {
                    wordExact = true;
                    break;
                }
                if (w.startsWith(token)) {
                    wordPrefix = true;
                }
            }

            if (wordExact) {
                score += 8000;
            } else if (wordPrefix) {
                score += 4000;
            } else if (titleNorm.contains(token)) {
                score += 2000;
            } else {
                score += 300;
            }

            boolean artExact = false;
            for (String w : artistWords) {
                if (w.equals(token)) {
                    artExact = true;
                    break;
                }
            }
            if (artExact) score += 3000;
            else if (artistNorm.contains(token)) score += 1000;

            if (folderNorm.contains(token) || albumNorm.contains(token)) {
                score += 400;
            }
        }

        score -= Math.min(titleNorm.length(), 60);
        return score;
    }

    private int calculateFolderRelevance(FolderItem folder, String normQuery, String[] tokens) {
        int score = 0;
        String nameNorm = normalizeText(folder.getName());
        String[] words = nameNorm.split("[^a-z0-9]+");

        if (nameNorm.equals(normQuery)) {
            score += 20000;
        } else if (nameNorm.startsWith(normQuery + " ") || nameNorm.startsWith(normQuery)) {
            score += 12000;
        } else if (nameNorm.contains(normQuery)) {
            score += 5000;
        }

        for (String token : tokens) {
            if (token.isEmpty()) continue;
            boolean exact = false;
            for (String w : words) {
                if (w.equals(token)) {
                    exact = true;
                    break;
                }
            }
            if (exact) score += 6000;
            else if (nameNorm.contains(token)) score += 2000;
        }

        score -= Math.min(nameNorm.length(), 50);
        return score;
    }

    private void performAsyncSearch(final String rawQuery, final int targetVersion) {
        final int tab = currentTab;
        final int filter = currentSearchFilter;
        final String folder = currentSelectedFolder;
        runOnUiThread(() -> {
            if (pbSearchLoading != null) pbSearchLoading.setVisibility(View.VISIBLE);
        });

        searchExecutor.execute(() -> {
            if (targetVersion != searchVersion) return;

            String norm = normalizeText(rawQuery);
            String[] tokens = norm.split("\\s+");

            if (tab == 0) {
                // PESTAÑA CARPETAS: Buscar con filtro USB 1 / USB 2 / TODO
                List<FolderItem> matchedFolders = new ArrayList<>();
                for (FolderItem f : folderList) {
                    if (targetVersion != searchVersion) return;
                    if (filter == FILTER_USB1) {
                        if (!"USB 1".equalsIgnoreCase(f.getUsbSource()) && (f.getPath() == null || (!f.getPath().contains("udisk1") && !f.getPath().contains("usb0")))) {
                            continue;
                        }
                    } else if (filter == FILTER_USB2) {
                        if (!"USB 2".equalsIgnoreCase(f.getUsbSource()) && (f.getPath() == null || (!f.getPath().contains("udisk2") && !f.getPath().contains("usb1")))) {
                            continue;
                        }
                    }

                    String fText = normalizeText(f.getName() + " " + f.getPath());
                    String fDedup = dedupChars(fText);
                    boolean match = true;
                    for (String token : tokens) {
                        if (token.isEmpty()) continue;
                        if (!fText.contains(token) && !fDedup.contains(dedupChars(token))) {
                            match = false;
                            break;
                        }
                    }
                    if (match) matchedFolders.add(f);
                }

                Collections.sort(matchedFolders, (f1, f2) -> Integer.compare(
                        calculateFolderRelevance(f2, norm, tokens),
                        calculateFolderRelevance(f1, norm, tokens)
                ));

                if (targetVersion != searchVersion) return;

                runOnUiThread(() -> {
                    if (targetVersion != searchVersion) return;
                    if (pbSearchLoading != null) pbSearchLoading.setVisibility(View.GONE);
                    displayedFolders.clear();
                    displayedFolders.addAll(matchedFolders);
                    if (listViewContent.getAdapter() != folderAdapter) {
                        listViewContent.setAdapter(folderAdapter);
                    }
                    String filterLabel = filter == FILTER_USB1 ? " [USB 1]" : (filter == FILTER_USB2 ? " [USB 2]" : "");
                    tvCurrentPath.setText("📂 Carpetas" + filterLabel + ": \"" + rawQuery + "\" (" + displayedFolders.size() + ")");
                    folderAdapter.setFolders(displayedFolders);
                });

            } else if (tab == 1) {
                // PESTAÑA PISTAS: Buscar con filtro (Esta Carpeta, TODO, USB 1, USB 2)
                List<Track> sourceTracks = new ArrayList<>();
                if (filter == FILTER_FOLDER && folder != null && !currentPlaylist.isEmpty()) {
                    sourceTracks.addAll(currentPlaylist);
                } else if (filter == FILTER_FOLDER && folder != null) {
                    for (Track t : allTracks) {
                        if (t.getFolder().equalsIgnoreCase(folder)) {
                            sourceTracks.add(t);
                        }
                    }
                } else if (filter == FILTER_USB1) {
                    for (Track t : allTracks) {
                        if ("USB 1".equalsIgnoreCase(t.getUsbSource()) || (t.getFilePath() != null && (t.getFilePath().contains("udisk1") || t.getFilePath().contains("usb0")))) {
                            sourceTracks.add(t);
                        }
                    }
                } else if (filter == FILTER_USB2) {
                    for (Track t : allTracks) {
                        if ("USB 2".equalsIgnoreCase(t.getUsbSource()) || (t.getFilePath() != null && (t.getFilePath().contains("udisk2") || t.getFilePath().contains("usb1")))) {
                            sourceTracks.add(t);
                        }
                    }
                } else {
                    sourceTracks.addAll(allTracks);
                }

                List<Track> matchedTracks = new ArrayList<>();
                for (Track t : sourceTracks) {
                    if (targetVersion != searchVersion) return;
                    if (fastTrackMatches(t, tokens)) {
                        matchedTracks.add(t);
                    }
                }

                Collections.sort(matchedTracks, (t1, t2) -> Integer.compare(
                        calculateTrackRelevance(t2, norm, tokens),
                        calculateTrackRelevance(t1, norm, tokens)
                ));

                if (targetVersion != searchVersion) return;

                runOnUiThread(() -> {
                    if (targetVersion != searchVersion) return;
                    if (pbSearchLoading != null) pbSearchLoading.setVisibility(View.GONE);
                    displayedTracks.clear();
                    displayedTracks.addAll(matchedTracks);
                    if (listViewContent.getAdapter() != trackAdapter) {
                        listViewContent.setAdapter(trackAdapter);
                    }
                    String filterLabel = filter == FILTER_FOLDER ? " [" + (folder != null ? folder : "Carpeta") + "]" :
                            (filter == FILTER_USB1 ? " [USB 1]" : (filter == FILTER_USB2 ? " [USB 2]" : " [Todo]"));
                    tvCurrentPath.setText("🔍 Pistas" + filterLabel + ": \"" + rawQuery + "\" (" + displayedTracks.size() + ")");
                    tabTracks.setText("🎵 Pistas (" + displayedTracks.size() + ")");
                    trackAdapter.setTracks(displayedTracks);
                });

            } else {
                // PESTAÑA FAVORITOS: Buscar en favoritos con filtro
                List<Track> matchedFavs = new ArrayList<>();
                for (Track t : allTracks) {
                    if (targetVersion != searchVersion) return;
                    if (!t.isFavorite()) continue;
                    if (filter == FILTER_USB1) {
                        if (!"USB 1".equalsIgnoreCase(t.getUsbSource()) && (t.getFilePath() == null || (!t.getFilePath().contains("udisk1") && !t.getFilePath().contains("usb0")))) {
                            continue;
                        }
                    } else if (filter == FILTER_USB2) {
                        if (!"USB 2".equalsIgnoreCase(t.getUsbSource()) && (t.getFilePath() == null || (!t.getFilePath().contains("udisk2") && !t.getFilePath().contains("usb1")))) {
                            continue;
                        }
                    }
                    if (fastTrackMatches(t, tokens)) {
                        matchedFavs.add(t);
                    }
                }

                Collections.sort(matchedFavs, (t1, t2) -> Integer.compare(
                        calculateTrackRelevance(t2, norm, tokens),
                        calculateTrackRelevance(t1, norm, tokens)
                ));

                if (targetVersion != searchVersion) return;

                runOnUiThread(() -> {
                    if (targetVersion != searchVersion) return;
                    if (pbSearchLoading != null) pbSearchLoading.setVisibility(View.GONE);
                    displayedTracks.clear();
                    displayedTracks.addAll(matchedFavs);
                    if (listViewContent.getAdapter() != trackAdapter) {
                        listViewContent.setAdapter(trackAdapter);
                    }
                    String filterLabel = filter == FILTER_USB1 ? " [USB 1]" : (filter == FILTER_USB2 ? " [USB 2]" : "");
                    tvCurrentPath.setText("⭐ Favoritos" + filterLabel + ": \"" + rawQuery + "\" (" + displayedTracks.size() + ")");
                    trackAdapter.setTracks(displayedTracks);
                });
            }
        });
    }

    private void filterSearch(String rawQuery) {
        String norm = normalizeText(rawQuery);
        String[] tokens = norm.split("\\s+");
        boolean hasQuery = !norm.isEmpty() && tokens.length > 0 && !tokens[0].isEmpty();

        // Si hay búsqueda activa: ejecutar de forma asíncrona según la pestaña y filtro
        if (hasQuery) {
            searchVersion++;
            performAsyncSearch(rawQuery, searchVersion);
            return;
        }

        if (pbSearchLoading != null) pbSearchLoading.setVisibility(View.GONE);

        // Modo Normal por pestaña y filtro seleccionado
        if (currentTab == 0) {
            displayedFolders.clear();
            if (currentSearchFilter == FILTER_USB1) {
                for (FolderItem f : folderList) {
                    if ("USB 1".equalsIgnoreCase(f.getUsbSource()) || (f.getPath() != null && (f.getPath().contains("udisk1") || f.getPath().contains("usb0")))) {
                        displayedFolders.add(f);
                    }
                }
                tvCurrentPath.setText("📂 Carpetas USB 1 (" + displayedFolders.size() + ")");
            } else if (currentSearchFilter == FILTER_USB2) {
                for (FolderItem f : folderList) {
                    if ("USB 2".equalsIgnoreCase(f.getUsbSource()) || (f.getPath() != null && (f.getPath().contains("udisk2") || f.getPath().contains("usb1")))) {
                        displayedFolders.add(f);
                    }
                }
                tvCurrentPath.setText("📂 Carpetas USB 2 (" + displayedFolders.size() + ")");
            } else {
                displayedFolders.addAll(folderList);
                tvCurrentPath.setText("📂 Carpetas USB (" + displayedFolders.size() + ")");
            }

            folderAdapter.setFolders(displayedFolders);
            if (listViewContent.getAdapter() != folderAdapter) {
                listViewContent.setAdapter(folderAdapter);
            }

        } else if (currentTab == 1) {
            displayedTracks.clear();
            if (currentSearchFilter == FILTER_FOLDER && currentSelectedFolder != null && !currentPlaylist.isEmpty()) {
                displayedTracks.addAll(currentPlaylist);
                tvCurrentPath.setText("📁 " + currentSelectedFolder + " (" + displayedTracks.size() + ")");
            } else if (currentSearchFilter == FILTER_USB1) {
                for (Track t : allTracks) {
                    if ("USB 1".equalsIgnoreCase(t.getUsbSource()) || (t.getFilePath() != null && (t.getFilePath().contains("udisk1") || t.getFilePath().contains("usb0")))) {
                        displayedTracks.add(t);
                    }
                }
                tvCurrentPath.setText("💾 Pistas USB 1 (" + displayedTracks.size() + ")");
            } else if (currentSearchFilter == FILTER_USB2) {
                for (Track t : allTracks) {
                    if ("USB 2".equalsIgnoreCase(t.getUsbSource()) || (t.getFilePath() != null && (t.getFilePath().contains("udisk2") || t.getFilePath().contains("usb1")))) {
                        displayedTracks.add(t);
                    }
                }
                tvCurrentPath.setText("💾 Pistas USB 2 (" + displayedTracks.size() + ")");
            } else {
                List<Track> source = (currentSelectedFolder != null && !currentPlaylist.isEmpty() && currentSearchFilter == FILTER_FOLDER) ? currentPlaylist : allTracks;
                displayedTracks.addAll(source);
                tvCurrentPath.setText(currentSelectedFolder != null && currentSearchFilter == FILTER_FOLDER ? "📁 " + currentSelectedFolder + " (" + displayedTracks.size() + ")" : "🎵 Todas las Pistas (" + displayedTracks.size() + ")");
            }

            trackAdapter.setTracks(displayedTracks);
            if (listViewContent.getAdapter() != trackAdapter) {
                listViewContent.setAdapter(trackAdapter);
            }
            tabTracks.setText("🎵 Pistas (" + displayedTracks.size() + ")");
            if (currentTrackIndex >= 0 && currentTrackIndex < displayedTracks.size()) {
                trackAdapter.setCurrentPlayingIndex(currentTrackIndex);
            }

        } else if (currentTab == 2) {
            displayedTracks.clear();
            for (Track t : allTracks) {
                if (!t.isFavorite()) continue;
                if (currentSearchFilter == FILTER_USB1) {
                    if (!"USB 1".equalsIgnoreCase(t.getUsbSource()) && (t.getFilePath() == null || (!t.getFilePath().contains("udisk1") && !t.getFilePath().contains("usb0")))) {
                        continue;
                    }
                } else if (currentSearchFilter == FILTER_USB2) {
                    if (!"USB 2".equalsIgnoreCase(t.getUsbSource()) && (t.getFilePath() == null || (!t.getFilePath().contains("udisk2") && !t.getFilePath().contains("usb1")))) {
                        continue;
                    }
                }
                displayedTracks.add(t);
            }
            trackAdapter.setTracks(displayedTracks);
            if (listViewContent.getAdapter() != trackAdapter) {
                listViewContent.setAdapter(trackAdapter);
            }
            tvCurrentPath.setText("⭐ Mis Favoritos (" + displayedTracks.size() + ")");
        }
    }

    // ==========================================
    // MOTOR DE AUDIO NATIVO Y REPRODUCCIÓN
    // ==========================================
    private void playTrack(int index) {
        if (displayedTracks.isEmpty() || index < 0 || index >= displayedTracks.size()) return;

        Track track = displayedTracks.get(index);

        // Si la canción pertenece a una carpeta, configurar esa carpeta como la activa y llenar la lista
        if (track.getFolder() != null && !track.getFolder().isEmpty()) {
            currentSelectedFolder = track.getFolder();
            currentPlaylist.clear();
            for (Track t : allTracks) {
                if (t.getFolder().equalsIgnoreCase(currentSelectedFolder)) {
                    currentPlaylist.add(t);
                }
            }
            int folderIdx = -1;
            for (int i = 0; i < currentPlaylist.size(); i++) {
                if (currentPlaylist.get(i).getFilePath().equals(track.getFilePath())) {
                    folderIdx = i;
                    break;
                }
            }
            currentTrackIndex = folderIdx >= 0 ? folderIdx : 0;
        } else {
            currentTrackIndex = index;
        }

        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setOnCompletionListener(mp -> {
                    if (isRepeat) {
                        playTrackFromQueue(getQueue(), currentTrackIndex);
                    } else {
                        playNextTrack();
                    }
                });
            } else {
                mediaPlayer.reset();
            }

            mediaPlayer.setDataSource(track.getFilePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;

            trackAdapter.setCurrentPlayingIndex(index);
            updatePlaybackStateUI();

            // Actualizar información en pantalla con tipografía grande y clara
            tvTrackTitle.setText(track.getTitle());
            tvTrackArtist.setText(track.getArtist());
            tvTrackAlbum.setText(track.getAlbum());
            tvFormatBadge.setText(track.getFormat());
            tvHeaderPath.setText(track.getFilePath());

            int durationMs = mediaPlayer.getDuration();
            tvTimeTotal.setText(formatMs(durationMs));
            tvTimeCurrent.setText("00:00");
            seekTrackProgress.setProgress(0);

            // Cargar carátula del archivo y bitrate exacto
            loadAlbumArt(track);

            // Previsualización de canción anterior y siguiente de la carpeta activa
            updatePreviews();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al reproducir: " + track.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    private List<Track> getQueue() {
        return !currentPlaylist.isEmpty() ? currentPlaylist : displayedTracks;
    }

    private void playTrackFromQueue(List<Track> queue, int index) {
        if (queue.isEmpty() || index < 0 || index >= queue.size()) return;
        currentTrackIndex = index;
        Track track = queue.get(index);

        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setOnCompletionListener(mp -> {
                    if (isRepeat) {
                        playTrackFromQueue(getQueue(), currentTrackIndex);
                    } else {
                        playNextTrack();
                    }
                });
            } else {
                mediaPlayer.reset();
            }

            mediaPlayer.setDataSource(track.getFilePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;

            // Si la canción está visible en la lista mostrada, actualizar índice
            int dispIdx = -1;
            for (int i = 0; i < displayedTracks.size(); i++) {
                if (displayedTracks.get(i).getFilePath().equals(track.getFilePath())) {
                    dispIdx = i;
                    break;
                }
            }
            trackAdapter.setCurrentPlayingIndex(dispIdx);
            updatePlaybackStateUI();

            tvTrackTitle.setText(track.getTitle());
            tvTrackArtist.setText(track.getArtist());
            tvTrackAlbum.setText(track.getAlbum());
            tvFormatBadge.setText(track.getFormat());
            tvHeaderPath.setText(track.getFilePath());

            int durationMs = mediaPlayer.getDuration();
            tvTimeTotal.setText(formatMs(durationMs));
            tvTimeCurrent.setText("00:00");
            seekTrackProgress.setProgress(0);

            loadAlbumArt(track);
            updatePreviews();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer == null) {
            List<Track> queue = getQueue();
            if (!queue.isEmpty()) {
                playTrackFromQueue(queue, 0);
            }
            return;
        }

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        } else {
            mediaPlayer.start();
            isPlaying = true;
        }
        updatePlaybackStateUI();
    }

    private void playNextTrack() {
        List<Track> queue = getQueue();
        if (queue.isEmpty()) return;
        int nextIndex;
        if (isShuffle) {
            nextIndex = new Random().nextInt(queue.size());
        } else {
            nextIndex = (currentTrackIndex + 1) % queue.size();
        }
        playTrackFromQueue(queue, nextIndex);
    }

    private void playPreviousTrack() {
        List<Track> queue = getQueue();
        if (queue.isEmpty()) return;
        int prevIndex = (currentTrackIndex - 1 + queue.size()) % queue.size();
        playTrackFromQueue(queue, prevIndex);
    }

    private void updatePreviews() {
        List<Track> queue = getQueue();
        if (queue.isEmpty()) return;
        int prevIdx = (currentTrackIndex - 1 + queue.size()) % queue.size();
        int nextIdx = (currentTrackIndex + 1) % queue.size();

        tvPrevTrackPreview.setText(queue.get(prevIdx).getTitle());
        tvNextTrackPreview.setText(queue.get(nextIdx).getTitle());
    }

    private void loadAlbumArt(Track track) {
        if (track == null) return;
        final String filePath = track.getFilePath();
        scanExecutor.execute(() -> {
            Bitmap art = null;
            String exactFormat = null;
            try {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(filePath);
                byte[] raw = mmr.getEmbeddedPicture();
                if (raw != null) {
                    art = BitmapFactory.decodeByteArray(raw, 0, raw.length);
                }
                String bitrateStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
                if (bitrateStr != null && !bitrateStr.isEmpty()) {
                    try {
                        int bitrate = Integer.parseInt(bitrateStr);
                        if (bitrate > 0) {
                            int kbps = Math.round(bitrate / 1000f);
                            if (kbps > 0) {
                                String ext = getExtension(filePath);
                                exactFormat = ext + " " + kbps + "K";
                            }
                        }
                    } catch (Exception ignored) {}
                }
                mmr.release();
            } catch (Exception ignored) {}

            final Bitmap finalArt = art;
            final String finalFormat = exactFormat;
            handler.post(() -> {
                if (finalArt != null) {
                    ivAlbumArt.setVisibility(View.VISIBLE);
                    ivAlbumArt.setImageBitmap(finalArt);
                } else {
                    ivAlbumArt.setVisibility(View.GONE);
                }
                if (finalFormat != null) {
                    track.setFormat(finalFormat);
                    List<Track> queue = getQueue();
                    if (currentTrackIndex >= 0 && currentTrackIndex < queue.size()) {
                        if (filePath.equals(queue.get(currentTrackIndex).getFilePath())) {
                            tvFormatBadge.setText(finalFormat);
                        }
                    }
                }
            });
        });
    }

    private void loadAlbumArt(String filePath) {
        if (filePath != null) {
            Track t = new Track(filePath, "", "", "", "", "", "MP3 320K", 0, "");
            loadAlbumArt(t);
        }
    }

    private void setupProgressUpdater() {
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlaying && !isUserSeeking) {
                    try {
                        int current = mediaPlayer.getCurrentPosition();
                        int total = mediaPlayer.getDuration();
                        if (total > 0) {
                            int progress = (int) (((float) current / total) * 100);
                            seekTrackProgress.setProgress(progress);
                            tvTimeCurrent.setText(formatMs(current));
                        }
                    } catch (Exception ignored) {}
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.post(progressRunnable);
    }

    private String formatMs(int ms) {
        int totalSec = ms / 1000;
        int m = totalSec / 60;
        int s = totalSec % 60;
        return String.format("%02d:%02d", m, s);
    }

    // ==========================================
    // ESCANEO TOTAL Y NATIVO DE PENDRIVES USB
    // ==========================================
    private void checkAndRequestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, PERM_REQUEST_CODE);
                return;
            }
        }
        startUsbScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_REQUEST_CODE) {
            startUsbScan();
        }
    }

    private void startUsbScan() {
        tvStorageStatus.setText("Escaneando USB 1 y USB 2...");

        scanExecutor.execute(() -> {
            List<Track> foundTracks = new ArrayList<>();
            Set<String> seenPaths = new HashSet<>();

            // Montajes de Pendrives en plataforma de auto AC8227L y genéricos
            String[] targetUsbRoots = {
                    "/storage/udisk1",
                    "/storage/udisk2",
                    "/storage/udisk3",
                    "/storage/usb0",
                    "/storage/usb1",
                    "/mnt/media_rw/udisk1",
                    "/mnt/media_rw/udisk2",
                    "/mnt/udisk1",
                    "/mnt/udisk2",
                    "/mnt/sdcard/media/music",
                    "/sdcard/Music",
                    "/sdcard/Download",
                    Environment.getExternalStorageDirectory().getAbsolutePath()
            };

            for (String rootPath : targetUsbRoots) {
                File dir = new File(rootPath);
                if (dir.exists() && dir.isDirectory()) {
                    String usbTag = rootPath.contains("udisk1") || rootPath.contains("usb0") ? "USB 1" :
                            (rootPath.contains("udisk2") || rootPath.contains("usb1") ? "USB 2" : "INTERNA");
                    scanDirectory(dir, foundTracks, seenPaths, usbTag, 0);
                }
            }

            // Escaneo complementario mediante MediaStore
            try {
                Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DATA
                };

                Cursor cursor = getContentResolver().query(musicUri, projection,
                        MediaStore.Audio.Media.IS_MUSIC + " != 0", null, null);

                if (cursor != null) {
                    int titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                    int artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                    int albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                    int durCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                    int dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                    while (cursor.moveToNext()) {
                        String path = cursor.getString(dataCol);
                        if (path != null && !seenPaths.contains(path)) {
                            File f = new File(path);
                            if (f.exists()) {
                                seenPaths.add(path);
                                String title = cursor.getString(titleCol);
                                String artist = cursor.getString(artistCol);
                                String album = cursor.getString(albumCol);
                                int durSec = (int) (cursor.getLong(durCol) / 1000);

                                if (title == null || title.isEmpty() || title.equalsIgnoreCase("<unknown>")) {
                                    title = f.getName().replaceFirst("[.][^.]+$", "");
                                }
                                if (artist == null || artist.equalsIgnoreCase("<unknown>")) {
                                    artist = "Artista";
                                }
                                String folder = f.getParentFile() != null ? f.getParentFile().getName() : "USB";
                                String usbTag = path.contains("udisk2") ? "USB 2" :
                                        (path.contains("udisk1") ? "USB 1" : "INTERNA");

                                String format = buildFormatBadge(f.getName(), f.length(), durSec);
                                foundTracks.add(new Track(path, title, artist, album, folder, f.getParent(), format, durSec, usbTag));
                            }
                        }
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Agrupar carpetas
            Map<String, FolderItem> folderMap = new HashMap<>();
            for (Track t : foundTracks) {
                String fName = t.getFolder();
                if (!folderMap.containsKey(fName)) {
                    folderMap.put(fName, new FolderItem(fName, t.getPath(), 1, t.getUsbSource()));
                } else {
                    folderMap.get(fName).incrementCount();
                }
            }

            List<FolderItem> generatedFolders = new ArrayList<>(folderMap.values());

            boolean fUsb1 = false;
            boolean fUsb2 = false;
            for (Track t : foundTracks) {
                if ("USB 1".equalsIgnoreCase(t.getUsbSource()) || (t.getFilePath() != null && (t.getFilePath().contains("udisk1") || t.getFilePath().contains("usb0")))) {
                    fUsb1 = true;
                }
                if ("USB 2".equalsIgnoreCase(t.getUsbSource()) || (t.getFilePath() != null && (t.getFilePath().contains("udisk2") || t.getFilePath().contains("usb1")))) {
                    fUsb2 = true;
                }
                if (fUsb1 && fUsb2) break;
            }
            if (!fUsb1) {
                String[] u1Paths = {"/storage/udisk1", "/mnt/media_rw/udisk1", "/mnt/udisk1", "/storage/usb0"};
                for (String p : u1Paths) { if (new File(p).exists()) { fUsb1 = true; break; } }
            }
            if (!fUsb2) {
                String[] u2Paths = {"/storage/udisk2", "/mnt/media_rw/udisk2", "/mnt/udisk2", "/storage/usb1"};
                for (String p : u2Paths) { if (new File(p).exists()) { fUsb2 = true; break; } }
            }
            final boolean u1 = fUsb1;
            final boolean u2 = fUsb2;

            handler.post(() -> {
                hasUsb1 = u1;
                hasUsb2 = u2;
                updateFilterChipsVisibility();

                allTracks.clear();
                allTracks.addAll(foundTracks);

                folderList.clear();
                folderList.addAll(generatedFolders);

                tvStorageStatus.setText(allTracks.isEmpty() ? "No se encontraron canciones" : "Pendrives listos (" + folderList.size() + " carpetas, " + allTracks.size() + " canciones)");
                tabTracks.setText("🎵 Pistas (" + allTracks.size() + ")");

                filterByTab();

                if (!allTracks.isEmpty() && currentTrackIndex == -1) {
                    // Cargar primera canción en vista previa
                    Track first = allTracks.get(0);
                    tvTrackTitle.setText(first.getTitle());
                    tvTrackArtist.setText(first.getArtist());
                    tvTrackAlbum.setText(first.getAlbum());
                    tvFormatBadge.setText(first.getFormat());
                    tvTimeTotal.setText(first.getFormattedDuration());
                    loadAlbumArt(first);
                    updatePreviews();
                }
            });
        });
    }

    private void scanDirectory(File dir, List<Track> tracks, Set<String> seen, String usbTag, int depth) {
        if (dir == null || !dir.exists() || !dir.isDirectory() || depth > 6) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                String name = file.getName();
                if (!name.startsWith(".") && !name.equalsIgnoreCase("Android") && !name.equalsIgnoreCase("lost.dir") && !name.equalsIgnoreCase("System Volume Information")) {
                    scanDirectory(file, tracks, seen, usbTag, depth + 1);
                }
            } else {
                String path = file.getAbsolutePath();
                if (!seen.contains(path)) {
                    String lower = file.getName().toLowerCase();
                    if (lower.endsWith(".mp3") || lower.endsWith(".flac") || lower.endsWith(".wav") || lower.endsWith(".m4a") || lower.endsWith(".ogg") || lower.endsWith(".aac")) {
                        seen.add(path);

                        String cleanName = file.getName().replaceFirst("[.][^.]+$", "");
                        String title = cleanName;
                        String artist = "Pendrive";
                        if (cleanName.contains("-")) {
                            String[] parts = cleanName.split("-", 2);
                            artist = parts[0].trim();
                            title = parts[1].trim();
                        }

                        String folder = file.getParentFile() != null ? file.getParentFile().getName() : "USB";
                        String format = buildFormatBadge(file.getName(), file.length(), 210);
                        tracks.add(new Track(path, title, artist, folder, folder, file.getParent(), format, 210, usbTag));
                    }
                }
            }
        }
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot > 0) ? fileName.substring(dot + 1).toUpperCase() : "MP3";
    }

    private String buildFormatBadge(String fileName, long fileSize, int durSec) {
        String ext = getExtension(fileName);
        int kbps = 320; // Calidad estándar de referencia

        if (durSec > 0 && fileSize > 0) {
            int calculated = (int) ((fileSize * 8L) / (durSec * 1000L));
            if (calculated >= 32 && calculated <= 3000) {
                if (ext.equalsIgnoreCase("MP3")) {
                    if (calculated >= 285) kbps = 320;
                    else if (calculated >= 225) kbps = 256;
                    else if (calculated >= 175) kbps = 192;
                    else if (calculated >= 145) kbps = 160;
                    else if (calculated >= 115) kbps = 128;
                    else if (calculated >= 80) kbps = 96;
                    else kbps = calculated;
                } else if (ext.equalsIgnoreCase("FLAC") || ext.equalsIgnoreCase("WAV")) {
                    kbps = (calculated > 500) ? calculated : 1411;
                } else {
                    kbps = calculated;
                }
            }
        } else {
            if (ext.equalsIgnoreCase("FLAC") || ext.equalsIgnoreCase("WAV")) {
                kbps = 1411;
            } else if (ext.equalsIgnoreCase("M4A") || ext.equalsIgnoreCase("AAC")) {
                kbps = 256;
            }
        }
        return ext + " " + kbps + "K";
    }

    // ==========================================
    // MANDOS DEL VOLANTE DEL AUTO Y HARDWARE
    // ==========================================
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                togglePlayPause();
                return true;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                playNextTrack();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                playPreviousTrack();
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                adjustVolumeStep(true);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                adjustVolumeStep(false);
                return true;
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                toggleMute();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setImmersiveMode();
            updateVolumeUI();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressRunnable != null) {
            handler.removeCallbacks(progressRunnable);
        }
        if (clockRunnable != null) {
            handler.removeCallbacks(clockRunnable);
        }
        if (searchRunnable != null) {
            searchDebounceHandler.removeCallbacks(searchRunnable);
        }
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }
        if (scanExecutor != null) {
            scanExecutor.shutdown();
        }
        if (searchExecutor != null) {
            searchExecutor.shutdown();
        }
    }
}
