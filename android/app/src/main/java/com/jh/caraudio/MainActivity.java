package com.jh.caraudio;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
        tvCurrentPath = findViewById(R.id.tvCurrentPath);
        tvStorageStatus = findViewById(R.id.tvStorageStatus);
        btnPlayAll = findViewById(R.id.btnPlayAll);
        listViewContent = findViewById(R.id.listViewContent);

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

    // ==========================================
    // SISTEMA DE VOLUMEN RÁPIDO CONDUCTOR
    // Pasos de 5 en 5 y barra vertical sin interferencias
    // ==========================================
    private void setupVolumeControls() {
        updateVolumeUI();

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
        } else {
            listViewContent.setAdapter(trackAdapter);
            tvCurrentPath.setText(tabIndex == 1 ? (currentSelectedFolder != null ? "📁 " + currentSelectedFolder : "🎵 Todas las Pistas") : "⭐ Mis Favoritos");
        }

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

    private boolean trackMatchesTokens(Track track, String[] tokens) {
        String haystack = normalizeText(track.getTitle() + " " + track.getArtist() + " " + track.getAlbum() + " " + track.getFolder() + " " + track.getFilePath());
        String haystackDedup = dedupChars(haystack);
        String[] words = haystack.split("[^a-z0-9]+");

        for (String token : tokens) {
            if (token.isEmpty()) continue;
            if (haystack.contains(token)) continue;
            if (haystackDedup.contains(dedupChars(token))) continue;

            boolean matchedWord = false;
            for (String word : words) {
                if (tokenMatches(token, word)) {
                    matchedWord = true;
                    break;
                }
            }
            if (!matchedWord) return false;
        }
        return true;
    }

    private void performAsyncSearch(final String rawQuery, final int targetVersion) {
        searchExecutor.execute(() -> {
            if (targetVersion != searchVersion) return;

            String norm = normalizeText(rawQuery);
            String[] tokens = norm.split("\\s+");
            List<Track> matched = new ArrayList<>();

            for (Track t : allTracks) {
                if (targetVersion != searchVersion) return; // Cancelar inmediatamente si el usuario teclea de nuevo
                if (trackMatchesTokens(t, tokens)) {
                    matched.add(t);
                }
            }

            if (targetVersion != searchVersion) return;

            runOnUiThread(() -> {
                if (targetVersion != searchVersion) return;
                displayedTracks.clear();
                displayedTracks.addAll(matched);

                if (listViewContent.getAdapter() != trackAdapter) {
                    listViewContent.setAdapter(trackAdapter);
                }

                tvCurrentPath.setText("🔍 Búsqueda: \"" + rawQuery + "\" (" + displayedTracks.size() + " encontradas)");
                tabTracks.setText("🎵 Pistas (" + displayedTracks.size() + ")");
                trackAdapter.setTracks(displayedTracks);
            });
        });
    }

    private void filterSearch(String rawQuery) {
        String norm = normalizeText(rawQuery);
        String[] tokens = norm.split("\\s+");
        boolean hasQuery = !norm.isEmpty() && tokens.length > 0 && !tokens[0].isEmpty();

        // Si hay búsqueda activa: ejecutar de forma asíncrona
        if (hasQuery) {
            searchVersion++;
            performAsyncSearch(rawQuery, searchVersion);
            return;
        }

        // Modo Normal por pestaña
        if (currentTab == 0) {
            displayedFolders.clear();
            displayedFolders.addAll(folderList);
            folderAdapter.setFolders(displayedFolders);
            if (listViewContent.getAdapter() != folderAdapter) {
                listViewContent.setAdapter(folderAdapter);
            }
            tvCurrentPath.setText("📂 Carpetas USB");
        } else if (currentTab == 1) {
            displayedTracks.clear();
            List<Track> source = (currentSelectedFolder != null && !currentPlaylist.isEmpty()) ? currentPlaylist : allTracks;
            displayedTracks.addAll(source);
            trackAdapter.setTracks(displayedTracks);
            if (listViewContent.getAdapter() != trackAdapter) {
                listViewContent.setAdapter(trackAdapter);
            }
            tabTracks.setText("🎵 Pistas (" + displayedTracks.size() + ")");
            tvCurrentPath.setText(currentSelectedFolder != null ? "📁 " + currentSelectedFolder : "🎵 Todas las Pistas");
        } else if (currentTab == 2) {
            displayedTracks.clear();
            for (Track t : allTracks) {
                if (t.isFavorite()) {
                    displayedTracks.add(t);
                }
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

        currentTrackIndex = index;
        Track track = displayedTracks.get(index);

        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setOnCompletionListener(mp -> {
                    if (isRepeat) {
                        playTrack(currentTrackIndex);
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

            // Cargar carátula del archivo si está disponible
            loadAlbumArt(track.getFilePath());

            // Previsualización de canción anterior y siguiente
            updatePreviews();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al reproducir: " + track.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer == null) {
            if (!displayedTracks.isEmpty()) {
                playTrack(0);
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
        if (displayedTracks.isEmpty()) return;
        int nextIndex;
        if (isShuffle) {
            nextIndex = new Random().nextInt(displayedTracks.size());
        } else {
            nextIndex = (currentTrackIndex + 1) % displayedTracks.size();
        }
        playTrack(nextIndex);
    }

    private void playPreviousTrack() {
        if (displayedTracks.isEmpty()) return;
        int prevIndex = (currentTrackIndex - 1 + displayedTracks.size()) % displayedTracks.size();
        playTrack(prevIndex);
    }

    private void updatePreviews() {
        if (displayedTracks.isEmpty()) return;
        int prevIdx = (currentTrackIndex - 1 + displayedTracks.size()) % displayedTracks.size();
        int nextIdx = (currentTrackIndex + 1) % displayedTracks.size();

        tvPrevTrackPreview.setText(displayedTracks.get(prevIdx).getTitle());
        tvNextTrackPreview.setText(displayedTracks.get(nextIdx).getTitle());
    }

    private void loadAlbumArt(String filePath) {
        scanExecutor.execute(() -> {
            Bitmap art = null;
            try {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(filePath);
                byte[] raw = mmr.getEmbeddedPicture();
                if (raw != null) {
                    art = BitmapFactory.decodeByteArray(raw, 0, raw.length);
                }
                mmr.release();
            } catch (Exception ignored) {}

            final Bitmap finalArt = art;
            handler.post(() -> {
                if (finalArt != null) {
                    ivAlbumArt.setImageBitmap(finalArt);
                } else {
                    ivAlbumArt.setImageResource(android.R.drawable.ic_media_play);
                }
            });
        });
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

                                foundTracks.add(new Track(path, title, artist, album, folder, f.getParent(), getExtension(f.getName()), durSec, usbTag));
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

            handler.post(() -> {
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
                        tracks.add(new Track(path, title, artist, folder, folder, file.getParent(), getExtension(file.getName()), 210, usbTag));
                    }
                }
            }
        }
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot > 0) ? fileName.substring(dot + 1).toUpperCase() : "MP3";
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
