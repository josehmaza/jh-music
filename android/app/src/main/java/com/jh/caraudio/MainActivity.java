package com.jh.caraudio;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
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
import java.util.LinkedHashMap;
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
    private ImageButton btnNavHome;
    private ImageButton btnVolMute;
    private TextView tvVolPercent;
    private Button btnVolUp, btnVolDown;
    private VerticalVolumeBar seekVolume;
    private int currentVolPercent = 75;

    // Reproductor Central
    private TextView tvPlaybackStatus;
    private ImageButton btnToggleExplorer;
    private View layoutExplorerToggleZone;
    private View layoutAlbumArtCard;
    private ImageView ivAlbumArt;
    private SoundWaveBarsView soundWaveBars;
    private TextView tvFormatBadge;
    private TextView tvTrackTitle, tvTrackArtist, tvTrackArtistDot, tvTrackAlbum;
    private View layoutPrevCard, layoutNextCard;
    private TextView tvPrevTrackPreview, tvNextTrackPreview;

    // Gestos globales del conductor para mostrar / ocultar explorador
    private float globalTouchStartX = 0f;
    private float globalTouchStartY = 0f;
    private long globalTouchStartTime = 0;
    private boolean globalTouchOnSeekbar = false;
    private boolean globalTouchInToggleZone = false;
    private boolean globalGestureHandled = false;
    private SeekBar seekTrackProgress;
    private TextView tvTimeCurrent, tvTimeTotal;
    private ImageButton btnShuffle, btnPrev, btnPlayPause, btnNext, btnRepeat;

    // Explorador Derecho
    private LinearLayout layoutExplorerPanel;
    private Button tabFolders, tabTracks, tabFavorites;
    private EditText etSearch;
    private ImageButton btnSearchClear;
    private View pbSearchLoading;
    private TextView tvCurrentPath, tvStorageStatus;
    private Button btnPlayAll;
    private ListView listViewContent;
    private ImageButton btnCollapseAllTree;
    private View scrollSearchFilters;

    // FastScroller lateral ergonómico y burbuja flotante A-Z para 200+ canciones
    private LinearLayout layoutFastScrollerTrack;
    private View viewFastScrollThumb;
    private LinearLayout layoutFastScrollBubble;
    private TextView tvFastScrollBubbleLetter;
    private TextView tvFastScrollBubbleTrackNumber;
    private LinearLayout layoutSearchLoading;
    private final Handler fastScrollHandler = new Handler(Looper.getMainLooper());
    private Runnable hideBubbleRunnable;

    // Adaptadores
    private TrackAdapter trackAdapter;
    private FolderAdapter folderAdapter;
    private FolderTreeAdapter folderTreeAdapter;

    // Listas de datos
    private List<Track> allTracks = new ArrayList<>();
    private List<Track> currentPlaylist = new ArrayList<>();
    private List<Track> displayedTracks = new ArrayList<>();
    private List<FolderItem> folderList = new ArrayList<>();
    private List<FolderItem> displayedFolders = new ArrayList<>();
    private List<TreeNode> rootTreeNodes = new ArrayList<>();
    private List<TreeNode> visibleTreeNodes = new ArrayList<>();

    // Estado del Reproductor
    private int currentTrackIndex = -1;
    private boolean isPlaying = false;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private boolean isMuted = false;
    private int currentTab = 0; // 0: Carpetas, 1: Pistas, 2: Favoritos
    private String currentSelectedFolder = null;
    private String currentSelectedFolderUsbSource = null;

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
        setupFastScroller();
        buildTreeFromTracks(allTracks);
        updateVisibleTreeNodes();
        setupClock();
        setupVolumeControls();
        setupPlayerControls();
        setupExplorerControls();
        setupProgressUpdater();

        checkAndRequestStoragePermissions();

        // Ocultar teclado por defecto y remover foco del buscador al abrir la app
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (etSearch != null) etSearch.clearFocus();
    }

    private void initViews() {
        // Barra Superior
        tvHeaderPath = findViewById(R.id.tvHeaderPath);
        if (tvHeaderPath != null) tvHeaderPath.setText("Q hubo jefe 😉");
        TextView tvAppVersion = findViewById(R.id.tvAppVersion);
        if (tvAppVersion != null) {
            try {
                String verName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                tvAppVersion.setText("v" + verName);
            } catch (Exception e) {
                tvAppVersion.setText("v1.7.0");
            }
        }
        tvClock = findViewById(R.id.tvClock);
        btnRescanUsb = findViewById(R.id.btnRescanUsb);

        // Franja de volumen (54dp)
        btnNavHome = findViewById(R.id.btnNavHome);
        btnVolMute = findViewById(R.id.btnVolMute);
        tvVolPercent = findViewById(R.id.tvVolPercent);
        btnVolUp = findViewById(R.id.btnVolUp);
        btnVolDown = findViewById(R.id.btnVolDown);
        seekVolume = findViewById(R.id.seekVolume);

        // Reproductor central
        tvPlaybackStatus = findViewById(R.id.tvPlaybackStatus);
        btnToggleExplorer = findViewById(R.id.btnToggleExplorer);
        layoutExplorerToggleZone = findViewById(R.id.layoutExplorerToggleZone);
        layoutAlbumArtCard = findViewById(R.id.layoutAlbumArtCard);
        ivAlbumArt = findViewById(R.id.ivAlbumArt);
        soundWaveBars = findViewById(R.id.soundWaveBars);
        tvFormatBadge = findViewById(R.id.tvFormatBadge);
        tvTrackTitle = findViewById(R.id.tvTrackTitle);
        tvTrackArtist = findViewById(R.id.tvTrackArtist);
        tvTrackArtistDot = findViewById(R.id.tvTrackArtistDot);
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
        layoutSearchLoading = findViewById(R.id.layoutSearchLoading);
        tvCurrentPath = findViewById(R.id.tvCurrentPath);
        if (tvCurrentPath != null) {
            tvCurrentPath.setVisibility(View.GONE);
        }
        tvStorageStatus = findViewById(R.id.tvStorageStatus);
        btnPlayAll = findViewById(R.id.btnPlayAll);
        listViewContent = findViewById(R.id.listViewContent);
        if (listViewContent != null) {
            listViewContent.setVerticalScrollBarEnabled(false);
            listViewContent.setHorizontalScrollBarEnabled(false);
            listViewContent.setFastScrollEnabled(false);
            listViewContent.setFastScrollAlwaysVisible(false);
            listViewContent.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        btnCollapseAllTree = findViewById(R.id.btnCollapseAllTree);
        if (btnCollapseAllTree != null) {
            btnCollapseAllTree.setOnClickListener(v -> collapseAllTreeNodes());
        }

        // Filtros de búsqueda (Esta Carpeta, TODO, USB 1, USB 2)
        scrollSearchFilters = findViewById(R.id.scrollSearchFilters);
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
                    openFolder(folder.getName(), folder.getUsbSource());
                }
        );

        folderTreeAdapter = new FolderTreeAdapter(this, visibleTreeNodes, new FolderTreeAdapter.OnTreeNodeClickListener() {
            @Override
            public void onNodeClick(TreeNode node, int position) {
                hideKeyboard();
                if (node.hasChildren()) {
                    node.setExpanded(!node.isExpanded());
                    updateVisibleTreeNodes();
                }
                selectFolderFromNode(node);
            }

            @Override
            public void onNodePlayClick(TreeNode node, int position) {
                hideKeyboard();
                playFolderFromNode(node);
            }
        });

        listViewContent.setAdapter(folderTreeAdapter);
    }

    // ==========================================
    // FASTSCROLLER ERGONÓMICO Y BURBUJA FLOTANTE A-Z PARA CONDUCCIÓN
    // ==========================================
    private void setupFastScroller() {
        layoutFastScrollerTrack = findViewById(R.id.layoutFastScrollerTrack);
        viewFastScrollThumb = findViewById(R.id.viewFastScrollThumb);
        layoutFastScrollBubble = findViewById(R.id.layoutFastScrollBubble);
        tvFastScrollBubbleLetter = findViewById(R.id.tvFastScrollBubbleLetter);
        tvFastScrollBubbleTrackNumber = findViewById(R.id.tvFastScrollBubbleTrackNumber);

        hideBubbleRunnable = () -> {
            if (layoutFastScrollBubble != null) {
                layoutFastScrollBubble.animate()
                        .alpha(0f)
                        .setDuration(220)
                        .withEndAction(() -> {
                            if (layoutFastScrollBubble != null) {
                                layoutFastScrollBubble.setVisibility(View.GONE);
                            }
                        })
                        .start();
            }
        };

        if (listViewContent != null) {
            listViewContent.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {}

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (layoutFastScrollerTrack != null && layoutFastScrollerTrack.getVisibility() == View.VISIBLE && totalItemCount > 0) {
                        int trackHeight = layoutFastScrollerTrack.getHeight();
                        int thumbHeight = (viewFastScrollThumb != null && viewFastScrollThumb.getHeight() > 0) ? viewFastScrollThumb.getHeight() : 46;
                        float maxTranslation = Math.max(0, trackHeight - thumbHeight);
                        float ratio = (float) firstVisibleItem / (float) Math.max(1, totalItemCount - visibleItemCount);
                        ratio = Math.max(0f, Math.min(1f, ratio));
                        if (viewFastScrollThumb != null) {
                            viewFastScrollThumb.setTranslationY(ratio * maxTranslation);
                        }
                    }
                }
            });
        }

        if (layoutFastScrollerTrack != null) {
            layoutFastScrollerTrack.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_MOVE: {
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                            fastScrollHandler.removeCallbacks(hideBubbleRunnable);

                            int trackHeight = layoutFastScrollerTrack.getHeight();
                            if (trackHeight <= 0) return true;

                            float y = event.getY();
                            y = Math.max(0, Math.min(y, trackHeight));
                            float ratio = y / (float) trackHeight;

                            int thumbHeight = (viewFastScrollThumb != null && viewFastScrollThumb.getHeight() > 0) ? viewFastScrollThumb.getHeight() : 46;
                            float maxTranslation = Math.max(0, trackHeight - thumbHeight);
                            if (viewFastScrollThumb != null) {
                                viewFastScrollThumb.setTranslationY(ratio * maxTranslation);
                            }

                            int itemCount = (listViewContent != null && listViewContent.getAdapter() != null)
                                    ? listViewContent.getAdapter().getCount()
                                    : 0;

                            if (itemCount > 0) {
                                int targetIndex = Math.min((int) (ratio * itemCount), itemCount - 1);
                                listViewContent.setSelectionFromTop(targetIndex, 0);
                                updateFastScrollBubble(targetIndex, itemCount);
                            }
                            return true;
                        }

                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL: {
                            fastScrollHandler.removeCallbacks(hideBubbleRunnable);
                            fastScrollHandler.postDelayed(hideBubbleRunnable, 450);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }

    private void updateFastScrollBubble(int position, int totalCount) {
        if (layoutFastScrollBubble == null) return;

        String letter = "#";
        if (listViewContent != null && listViewContent.getAdapter() == trackAdapter && position >= 0 && position < displayedTracks.size()) {
            Track t = displayedTracks.get(position);
            if (t != null && t.getTitle() != null && !t.getTitle().trim().isEmpty()) {
                char ch = Character.toUpperCase(t.getTitle().trim().charAt(0));
                if (ch >= 'A' && ch <= 'Z') {
                    letter = String.valueOf(ch);
                }
            }
        } else if (listViewContent != null && listViewContent.getAdapter() == folderTreeAdapter && position >= 0 && position < visibleTreeNodes.size()) {
            TreeNode node = visibleTreeNodes.get(position);
            if (node != null && node.getName() != null && !node.getName().trim().isEmpty()) {
                char ch = Character.toUpperCase(node.getName().trim().charAt(0));
                if (ch >= 'A' && ch <= 'Z') {
                    letter = String.valueOf(ch);
                }
            }
        }

        if (tvFastScrollBubbleLetter != null) {
            tvFastScrollBubbleLetter.setText(letter);
        }
        if (tvFastScrollBubbleTrackNumber != null) {
            tvFastScrollBubbleTrackNumber.setText((position + 1) + " / " + totalCount);
        }

        layoutFastScrollBubble.animate().cancel();
        layoutFastScrollBubble.setAlpha(1f);
        layoutFastScrollBubble.setVisibility(View.VISIBLE);
    }

    private void updateFastScrollerVisibility() {
        if (layoutFastScrollerTrack == null) return;
        int count = (listViewContent != null && listViewContent.getAdapter() != null)
                ? listViewContent.getAdapter().getCount()
                : 0;
        if (count > 12) {
            layoutFastScrollerTrack.setVisibility(View.VISIBLE);
        } else {
            layoutFastScrollerTrack.setVisibility(View.GONE);
        }
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
        if (etSearch != null) {
            etSearch.clearFocus();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                globalTouchStartX = ev.getRawX();
                globalTouchStartY = ev.getRawY();
                globalTouchStartTime = System.currentTimeMillis();
                globalGestureHandled = false;
                globalTouchOnSeekbar = false;

                // Ocultar teclado táctil si se toca fuera de EditText
                View v = getCurrentFocus();
                if (v instanceof EditText) {
                    Rect outRect = new Rect();
                    v.getGlobalVisibleRect(outRect);
                    if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                        hideKeyboard();
                    }
                }

                // Proteger deslizadores (seekbar y volumen) de gestos de panel
                if (seekTrackProgress != null) {
                    Rect seekRect = new Rect();
                    if (seekTrackProgress.getGlobalVisibleRect(seekRect)) {
                        seekRect.inset(0, -25);
                        if (seekRect.contains((int) globalTouchStartX, (int) globalTouchStartY)) {
                            globalTouchOnSeekbar = true;
                        }
                    }
                }
                if (seekVolume != null) {
                    Rect volRect = new Rect();
                    if (seekVolume.getGlobalVisibleRect(volRect)) {
                        volRect.inset(-20, -20);
                        if (volRect.contains((int) globalTouchStartX, (int) globalTouchStartY)) {
                            globalTouchOnSeekbar = true;
                        }
                    }
                }

                // Restringir gesto de arrastrar/swipe ÚNICAMENTE a la zona roja del botón y franja libre
                globalTouchInToggleZone = false;
                if (layoutExplorerToggleZone != null) {
                    Rect toggleRect = new Rect();
                    if (layoutExplorerToggleZone.getGlobalVisibleRect(toggleRect)) {
                        toggleRect.inset(-25, -25);
                        if (toggleRect.contains((int) globalTouchStartX, (int) globalTouchStartY)) {
                            globalTouchInToggleZone = true;
                        }
                    }
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (!globalGestureHandled && globalTouchInToggleZone && !globalTouchOnSeekbar && layoutExplorerPanel != null) {
                    float dx = ev.getRawX() - globalTouchStartX;
                    float dy = ev.getRawY() - globalTouchStartY;

                    // Si el explorador está OCULTO: Jalar hacia la derecha muestra el explorador
                    if (layoutExplorerPanel.getVisibility() != View.VISIBLE) {
                        if (dx > 35 && Math.abs(dx) > Math.abs(dy) * 1.1f) {
                            globalGestureHandled = true;
                            layoutExplorerPanel.setVisibility(View.VISIBLE);
                            return true;
                        }
                    }
                    // Si el explorador está VISIBLE: Deslizar hacia la izquierda cierra el explorador
                    else {
                        if (dx < -35 && Math.abs(dx) > Math.abs(dy) * 1.1f) {
                            globalGestureHandled = true;
                            hideKeyboard();
                            layoutExplorerPanel.setVisibility(View.GONE);
                            return true;
                        }
                    }
                }
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (globalGestureHandled) {
                    return true;
                }
                if (globalTouchInToggleZone && !globalTouchOnSeekbar && layoutExplorerPanel != null) {
                    float dx = ev.getRawX() - globalTouchStartX;
                    float dy = ev.getRawY() - globalTouchStartY;

                    if (layoutExplorerPanel.getVisibility() != View.VISIBLE) {
                        if (dx > 35 && Math.abs(dx) > Math.abs(dy) * 1.1f) {
                            globalGestureHandled = true;
                            layoutExplorerPanel.setVisibility(View.VISIBLE);
                            return true;
                        }
                    } else {
                        if (dx < -35 && Math.abs(dx) > Math.abs(dy) * 1.1f) {
                            globalGestureHandled = true;
                            hideKeyboard();
                            layoutExplorerPanel.setVisibility(View.GONE);
                            return true;
                        }
                    }
                }
                break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        hideKeyboard();

        // 1. Si hay texto en el buscador, borrar búsqueda y restaurar vista en la carpeta que está sonando
        if (etSearch != null && etSearch.getText().length() > 0) {
            etSearch.setText("");
            if (btnSearchClear != null) btnSearchClear.setVisibility(View.GONE);
            if (pbSearchLoading != null) pbSearchLoading.setVisibility(View.GONE);
            if (currentSelectedFolder != null && !currentPlaylist.isEmpty()) {
                currentSearchFilter = FILTER_FOLDER;
                updateFilterChipsVisibility();
                updateFilterChipsUI();
            }
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

    /**
     * Mismo comportamiento que pulsar el botón físico HOME del auto:
     * Minimiza la app al launcher principal de la radio del auto instantáneamente.
     */
    private void goHome() {
        hideKeyboard();
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(homeIntent);
        } catch (Exception e) {
            moveTaskToBack(true);
        }
    }

    // ==========================================
    // SISTEMA DE VOLUMEN RÁPIDO CONDUCTOR
    // Pasos de 5 en 5 y barra vertical sin interferencias
    // ==========================================
    private void setupVolumeControls() {
        updateVolumeUI();

        // Botón HOME en pantalla (Mismo comportamiento que el botón físico HOME del auto)
        if (btnNavHome != null) {
            btnNavHome.setOnClickListener(v -> goHome());
        }

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
        // Toque en el cuadro grande de carátula: reproduce / pausa de emergencia con 1 toque
        if (layoutAlbumArtCard != null) {
            layoutAlbumArtCard.setOnClickListener(v -> togglePlayPause());
        }
        btnNext.setOnClickListener(v -> playNextTrack());
        btnPrev.setOnClickListener(v -> playPreviousTrack());

        // Tarjetas flotantes anterior y siguiente
        if (layoutPrevCard != null) layoutPrevCard.setOnClickListener(v -> playPreviousTrack());
        if (layoutNextCard != null) layoutNextCard.setOnClickListener(v -> playNextTrack());

        // Botón y Zona Táctil Ergonómica para alternar o deslizar el explorador de música
        setupExplorerToggleGestures();

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
    // GESTOS ERGONÓMICOS DEL EXPLORADOR PARA EL CONDUCTOR
    // Botón de lupa (click conmuta) y zona libre roja lateral (jalar a la izq oculta, a la der muestra; toques ignorados)
    // ==========================================
    private void setupExplorerToggleGestures() {
        if (btnToggleExplorer != null) {
            btnToggleExplorer.setClickable(false);
            btnToggleExplorer.setFocusable(false);
        }

        if (layoutExplorerToggleZone != null) {
            // NUNCA asignar setOnClickListener a layoutExplorerToggleZone para evitar alternar con un simple toque accidental
            layoutExplorerToggleZone.setOnTouchListener(new View.OnTouchListener() {
                private float downX, downY;
                private boolean gestureHandled = false;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            downX = event.getRawX();
                            downY = event.getRawY();
                            gestureHandled = false;
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            if (!gestureHandled && layoutExplorerPanel != null) {
                                float dx = event.getRawX() - downX;
                                float dy = event.getRawY() - downY;
                                if (Math.abs(dx) > 35 && Math.abs(dx) > Math.abs(dy) * 1.1f) {
                                    // Jalar hacia la IZQUIERDA (<-) oculta el explorador
                                    if (dx < -35 && layoutExplorerPanel.getVisibility() == View.VISIBLE) {
                                        gestureHandled = true;
                                        hideKeyboard();
                                        layoutExplorerPanel.setVisibility(View.GONE);
                                        return true;
                                    }
                                    // Jalar hacia la DERECHA (->) muestra el explorador
                                    else if (dx > 35 && layoutExplorerPanel.getVisibility() != View.VISIBLE) {
                                        gestureHandled = true;
                                        layoutExplorerPanel.setVisibility(View.VISIBLE);
                                        return true;
                                    }
                                }
                            }
                            break;

                        case MotionEvent.ACTION_UP:
                            if (!gestureHandled && layoutExplorerPanel != null) {
                                float totalDx = event.getRawX() - downX;
                                float totalDy = event.getRawY() - downY;
                                if (Math.abs(totalDx) > 35 && Math.abs(totalDx) > Math.abs(totalDy) * 1.1f) {
                                    if (totalDx < -35 && layoutExplorerPanel.getVisibility() == View.VISIBLE) {
                                        hideKeyboard();
                                        layoutExplorerPanel.setVisibility(View.GONE);
                                    } else if (totalDx > 35 && layoutExplorerPanel.getVisibility() != View.VISIBLE) {
                                        layoutExplorerPanel.setVisibility(View.VISIBLE);
                                    }
                                }
                                // Si fue un simple toque (tap, Math.abs(totalDx) <= 35), NO se hace nada.
                            }
                            return true;

                        case MotionEvent.ACTION_CANCEL:
                            gestureHandled = false;
                            break;
                    }
                    return true;
                }
            });
        }
    }

    private void toggleExplorerVisibility() {
        hideKeyboard();
        if (layoutExplorerPanel != null) {
            if (layoutExplorerPanel.getVisibility() == View.VISIBLE) {
                layoutExplorerPanel.setVisibility(View.GONE);
            } else {
                layoutExplorerPanel.setVisibility(View.VISIBLE);
            }
        }
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
                    if (pbSearchLoading != null) pbSearchLoading.setVisibility(View.GONE);
                    if (layoutSearchLoading != null) layoutSearchLoading.setVisibility(View.GONE);
                    if (listViewContent != null) {
                        listViewContent.setAlpha(1.0f);
                        listViewContent.setEnabled(true);
                    }
                    // Preservar el filtro que el usuario tiene activo (USB 1, USB 2, TODO, Carpeta)
                    filterSearch("");
                    return;
                }

                // Mostrar de inmediato que está buscando: lista atenuada/borrosa y overlay central con spinner
                if (pbSearchLoading != null) pbSearchLoading.setVisibility(View.VISIBLE);
                if (layoutSearchLoading != null) layoutSearchLoading.setVisibility(View.VISIBLE);
                if (listViewContent != null) {
                    listViewContent.setAlpha(0.25f);
                    listViewContent.setEnabled(false);
                }
                if (layoutFastScrollerTrack != null) {
                    layoutFastScrollerTrack.setVisibility(View.GONE);
                }
                tvCurrentPath.setText("⏳ Buscando: \"" + q + "\"...");

                // Esperar a que el usuario termine de teclear (250ms)
                searchRunnable = () -> performAsyncSearch(q, thisVersion);
                searchDebounceHandler.postDelayed(searchRunnable, 250);
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
            if (layoutSearchLoading != null) layoutSearchLoading.setVisibility(View.GONE);
            if (listViewContent != null) {
                listViewContent.setAlpha(1.0f);
                listViewContent.setEnabled(true);
            }
            hideKeyboard();
            // Al borrar la búsqueda, restaurar y mostrar las canciones de la carpeta actual seleccionada
            updateFilterChipsVisibility();
            updateFilterChipsUI();
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
        if (etSearch != null && etSearch.getText().length() > 0) {
            filterSearch(etSearch.getText().toString());
        }
    }

    private void updateFilterChipsUI() {
        if (btnFilterFolder != null) {
            boolean sel = (currentSearchFilter == FILTER_FOLDER);
            btnFilterFolder.setBackgroundResource(sel ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
            int color = sel ? Color.parseColor("#0d0f14") : Color.parseColor("#f1f5f9");
            btnFilterFolder.setTextColor(color);
            btnFilterFolder.setTypeface(null, Typeface.BOLD);
            if (currentSelectedFolder != null) {
                String usbNum = (currentSelectedFolderUsbSource != null && currentSelectedFolderUsbSource.contains("2")) ? "2" : (currentSelectedFolderUsbSource != null && currentSelectedFolderUsbSource.contains("1") ? "1" : "");
                String label = currentSelectedFolder.length() > 16 ? currentSelectedFolder.substring(0, 14) + "…" : currentSelectedFolder;
                btnFilterFolder.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                btnFilterFolder.setCompoundDrawablePadding(0);
                btnFilterFolder.setText(buildFolderFilterText(label, usbNum, color));
            }
        }

        if (btnFilterAll != null) {
            boolean sel = (currentSearchFilter == FILTER_ALL);
            btnFilterAll.setBackgroundResource(sel ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
            btnFilterAll.setTextColor(sel ? Color.parseColor("#0d0f14") : Color.parseColor("#f1f5f9"));
            btnFilterAll.setTypeface(null, Typeface.BOLD);
        }

        if (btnFilterUsb1 != null) {
            boolean sel = (currentSearchFilter == FILTER_USB1);
            btnFilterUsb1.setBackgroundResource(sel ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
            int color = sel ? Color.parseColor("#0d0f14") : Color.parseColor("#f1f5f9");
            btnFilterUsb1.setTextColor(color);
            btnFilterUsb1.setTypeface(null, Typeface.BOLD);
            Drawable[] drawables = btnFilterUsb1.getCompoundDrawables();
            if (drawables.length > 0 && drawables[0] != null) {
                drawables[0].mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }
        }

        if (btnFilterUsb2 != null) {
            boolean sel = (currentSearchFilter == FILTER_USB2);
            btnFilterUsb2.setBackgroundResource(sel ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
            int color = sel ? Color.parseColor("#0d0f14") : Color.parseColor("#f1f5f9");
            btnFilterUsb2.setTextColor(color);
            btnFilterUsb2.setTypeface(null, Typeface.BOLD);
            Drawable[] drawables = btnFilterUsb2.getCompoundDrawables();
            if (drawables.length > 0 && drawables[0] != null) {
                drawables[0].mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }
        }
    }

    private CharSequence buildFolderFilterText(String label, String usbNum, int textColor) {
        if (usbNum == null || usbNum.isEmpty()) {
            return "📁 " + label;
        }
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append("📁  ");
        int start = ssb.length();
        ssb.append(" ");
        try {
            Drawable d = getDrawable(R.drawable.ic_usb_trident);
            if (d != null) {
                d = d.mutate();
                d.setColorFilter(textColor, PorterDuff.Mode.SRC_IN);
                int size = (int) (14 * getResources().getDisplayMetrics().density);
                d.setBounds(0, 0, size, size);
                ssb.setSpan(new CenteredImageSpan(d), start, start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } catch (Exception ignored) {}
        ssb.append(" ").append(usbNum).append(" | ").append(label);
        return ssb;
    }

    private static class CenteredImageSpan extends ImageSpan {
        public CenteredImageSpan(Drawable drawable) {
            super(drawable);
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            Drawable b = getDrawable();
            canvas.save();
            int transY = top + (bottom - top) / 2 - b.getBounds().height() / 2;
            canvas.translate(x, transY);
            b.draw(canvas);
            canvas.restore();
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            Drawable d = getDrawable();
            Rect rect = d.getBounds();
            if (fm != null) {
                Paint.FontMetricsInt pfm = paint.getFontMetricsInt();
                fm.ascent = pfm.ascent;
                fm.descent = pfm.descent;
                fm.top = pfm.top;
                fm.bottom = pfm.bottom;
            }
            return rect.right;
        }
    }

    private void updateFilterChipsVisibility() {
        // La barra de filtros solo tiene utilidad en la pestaña Pistas (currentTab == 1).
        // En Carpetas USB (0) el árbol organiza los pendrives, y en Favoritos (2) se muestra la lista VIP completa.
        if (scrollSearchFilters != null) {
            scrollSearchFilters.setVisibility(currentTab == 1 ? View.VISIBLE : View.GONE);
        }
        if (currentTab != 1) {
            return;
        }

        if (btnFilterFolder != null) {
            if (currentTab == 1 && currentSelectedFolder != null) {
                btnFilterFolder.setVisibility(View.VISIBLE);
                boolean sel = (currentSearchFilter == FILTER_FOLDER);
                int color = sel ? Color.parseColor("#0d0f14") : Color.parseColor("#f1f5f9");
                String usbNum = (currentSelectedFolderUsbSource != null && currentSelectedFolderUsbSource.contains("2")) ? "2" : (currentSelectedFolderUsbSource != null && currentSelectedFolderUsbSource.contains("1") ? "1" : "");
                String label = currentSelectedFolder.length() > 16 ? currentSelectedFolder.substring(0, 14) + "…" : currentSelectedFolder;
                btnFilterFolder.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                btnFilterFolder.setCompoundDrawablePadding(0);
                btnFilterFolder.setText(buildFolderFilterText(label, usbNum, color));
            } else {
                btnFilterFolder.setVisibility(View.GONE);
            }
        }

        if (btnFilterAll != null) {
            btnFilterAll.setVisibility(View.VISIBLE);
        }

        boolean multipleUsb = (hasUsb1 && hasUsb2);

        // Si solo hay un pendrive conectado, TODO ya hace referencia a ese único USB,
        // por lo que los chips individuales USB 1 y USB 2 solo se muestran si ambos están presentes
        if (btnFilterUsb1 != null) {
            btnFilterUsb1.setVisibility(multipleUsb ? View.VISIBLE : View.GONE);
        }

        if (btnFilterUsb2 != null) {
            btnFilterUsb2.setVisibility(multipleUsb ? View.VISIBLE : View.GONE);
        }

        if (!multipleUsb && (currentSearchFilter == FILTER_USB1 || currentSearchFilter == FILTER_USB2)) {
            currentSearchFilter = FILTER_ALL;
        }

        updateFilterChipsUI();
    }

    private void selectTab(int tabIndex) {
        hideKeyboard();
        if (searchRunnable != null) {
            searchDebounceHandler.removeCallbacks(searchRunnable);
        }
        searchVersion++;
        if (etSearch != null && etSearch.getText().length() > 0) {
            etSearch.setText("");
        }
        if (btnSearchClear != null) {
            btnSearchClear.setVisibility(View.GONE);
        }
        if (pbSearchLoading != null) {
            pbSearchLoading.setVisibility(View.GONE);
        }
        if (layoutSearchLoading != null) {
            layoutSearchLoading.setVisibility(View.GONE);
        }
        if (listViewContent != null) {
            listViewContent.setAlpha(1.0f);
            listViewContent.setEnabled(true);
        }

        currentTab = tabIndex;
        tabFolders.setBackgroundResource(tabIndex == 0 ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
        tabFolders.setTextColor(tabIndex == 0 ? Color.parseColor("#38bdf8") : Color.parseColor("#cbd5e1"));
        tabFolders.setTypeface(null, Typeface.BOLD);

        tabTracks.setBackgroundResource(tabIndex == 1 ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
        tabTracks.setTextColor(tabIndex == 1 ? Color.parseColor("#38bdf8") : Color.parseColor("#cbd5e1"));
        tabTracks.setTypeface(null, Typeface.BOLD);

        tabFavorites.setBackgroundResource(tabIndex == 2 ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
        tabFavorites.setTextColor(tabIndex == 2 ? Color.parseColor("#38bdf8") : Color.parseColor("#cbd5e1"));
        tabFavorites.setTypeface(null, Typeface.BOLD);

        if (tabIndex == 0) {
            if (etSearch.getText().toString().trim().isEmpty()) {
                listViewContent.setAdapter(folderTreeAdapter);
                if (btnCollapseAllTree != null) btnCollapseAllTree.setVisibility(View.VISIBLE);
                updateVisibleTreeNodes();
            } else {
                listViewContent.setAdapter(folderAdapter);
                if (btnCollapseAllTree != null) btnCollapseAllTree.setVisibility(View.GONE);
            }
            if (currentSearchFilter == FILTER_FOLDER) {
                currentSearchFilter = FILTER_ALL;
            }
        } else if (tabIndex == 1) {
            if (btnCollapseAllTree != null) btnCollapseAllTree.setVisibility(View.GONE);
            listViewContent.setAdapter(trackAdapter);
            String usbTag = (currentSelectedFolderUsbSource != null && !currentSelectedFolderUsbSource.isEmpty()) ? "[" + currentSelectedFolderUsbSource + "] " : "";
            tvCurrentPath.setText(currentSelectedFolder != null ? "📁 " + usbTag + currentSelectedFolder : "🎵 Todas las Pistas");
            if (currentSelectedFolder != null) {
                currentSearchFilter = FILTER_FOLDER;
            } else if (currentSearchFilter == FILTER_FOLDER) {
                currentSearchFilter = FILTER_ALL;
            }
        } else {
            if (btnCollapseAllTree != null) btnCollapseAllTree.setVisibility(View.GONE);
            listViewContent.setAdapter(trackAdapter);
            tvCurrentPath.setText("⭐ Mis Favoritos");
            if (currentSearchFilter == FILTER_FOLDER) {
                currentSearchFilter = FILTER_ALL;
            }
        }

        updateFilterChipsVisibility();
        filterByTab();
    }

    private void openFolder(String folderName, String usbSource) {
        currentSelectedFolder = folderName;
        currentSelectedFolderUsbSource = usbSource;
        if (currentSelectedFolderUsbSource == null || currentSelectedFolderUsbSource.isEmpty()) {
            for (Track t : allTracks) {
                if (t.getFolder() != null && t.getFolder().equalsIgnoreCase(folderName)) {
                    currentSelectedFolderUsbSource = t.getUsbSource();
                    break;
                }
            }
        }
        currentPlaylist.clear();
        for (Track t : allTracks) {
            if (t.getFolder() != null && t.getFolder().equalsIgnoreCase(folderName)) {
                if (isTrackFromUsb(t, currentSelectedFolderUsbSource)) {
                    currentPlaylist.add(t);
                }
            }
        }

        currentSearchFilter = FILTER_FOLDER;
        selectTab(1);
        String usbTag = (currentSelectedFolderUsbSource != null && !currentSelectedFolderUsbSource.isEmpty()) ? "[" + currentSelectedFolderUsbSource + "] " : "";
        tvCurrentPath.setText("📁 " + usbTag + folderName);
        tvHeaderPath.setText("Q hubo jefe 😉");
    }

    private void openFolder(String folderName) {
        openFolder(folderName, null);
    }

    // ========================================================
    // ÁRBOL JERÁRQUICO DE CARPETAS USB (Estilo Web Maqueta Hernan)
    // ========================================================
    private void buildTreeFromTracks(List<Track> tracks) {
        rootTreeNodes.clear();

        if (tracks == null || tracks.isEmpty()) {
            buildDefaultTreeDemo();
            return;
        }

        Map<String, TreeNode> rootUsbMap = new LinkedHashMap<>();

        for (Track track : tracks) {
            String path = track.getFilePath() != null ? track.getFilePath() : track.getPath();
            String usb = track.getUsbSource();
            if (usb == null || usb.isEmpty()) {
                usb = (path != null && path.toLowerCase().contains("udisk2")) ? "USB 2" : "USB 1";
            }
            boolean isUsb2 = "USB 2".equalsIgnoreCase(usb) || (path != null && (path.toLowerCase().contains("udisk2") || path.toLowerCase().contains("usb1")));
            String usbId = isUsb2 ? "usb2" : "usb1";
            String usbName = isUsb2 ? "USB 2 (udisk2)" : "USB 1 (udisk1)";
            String usbPath = isUsb2 ? "/storage/udisk2/" : "/storage/udisk1/";

            TreeNode rootNode = rootUsbMap.get(usbId);
            if (rootNode == null) {
                rootNode = new TreeNode(usbId, usbName, usbPath, isUsb2 ? "USB 2" : "USB 1", true, 0);
                rootUsbMap.put(usbId, rootNode);
            }
            rootNode.incrementTotalCount();

            // Extraer ruta relativa de directorios (sin el nombre del archivo mp3)
            String relDir = "";
            if (path != null) {
                String lower = path.toLowerCase();
                String[] usbPatterns = {"udisk2", "udisk1", "usb1", "usb0", "udisk3", "usb2"};
                for (String pat : usbPatterns) {
                    int idx = lower.indexOf(pat);
                    if (idx != -1) {
                        int slashAfter = path.indexOf('/', idx);
                        if (slashAfter != -1 && slashAfter < path.length() - 1) {
                            relDir = path.substring(slashAfter + 1);
                        }
                        break;
                    }
                }
                if (relDir.isEmpty() && path.startsWith(usbPath)) {
                    relDir = path.substring(usbPath.length());
                }
            }

            int lastSlash = relDir.lastIndexOf('/');
            if (lastSlash != -1) {
                relDir = relDir.substring(0, lastSlash);
            } else {
                relDir = "";
            }

            if (!relDir.trim().isEmpty()) {
                String[] parts = relDir.split("/+");
                TreeNode currentNode = rootNode;
                String accumPath = usbPath;

                for (int i = 0; i < parts.length; i++) {
                    String p = parts[i].trim();
                    if (p.isEmpty()) continue;
                    accumPath += p + "/";

                    TreeNode child = currentNode.findChildByName(p);
                    if (child == null) {
                        String childId = usbId + "_" + p.replaceAll("[^a-zA-Z0-9]", "_") + "_" + i;
                        child = new TreeNode(childId, p, accumPath, currentNode.getUsbSource(), false, 0);
                        currentNode.addChild(child);
                    }
                    child.incrementTotalCount();
                    if (i == parts.length - 1) {
                        child.incrementDirectCount();
                    }
                    currentNode = child;
                }
            } else {
                rootNode.incrementDirectCount();
            }
        }

        if (hasUsb1 && !rootUsbMap.containsKey("usb1")) {
            rootUsbMap.put("usb1", new TreeNode("usb1", "USB 1 (udisk1)", "/storage/udisk1/", "USB 1", true, 0));
        }
        if (hasUsb2 && !rootUsbMap.containsKey("usb2")) {
            rootUsbMap.put("usb2", new TreeNode("usb2", "USB 2 (udisk2)", "/storage/udisk2/", "USB 2", true, 0));
        }

        for (TreeNode root : rootUsbMap.values()) {
            sortNodeChildren(root);
            rootTreeNodes.add(root);
        }
    }

    private void sortNodeChildren(TreeNode node) {
        if (node == null || !node.hasChildren()) return;
        Collections.sort(node.getChildren(), (c1, c2) -> {
            String n1 = c1.getName() != null ? c1.getName() : "";
            String n2 = c2.getName() != null ? c2.getName() : "";
            return n1.compareToIgnoreCase(n2);
        });
        for (TreeNode child : node.getChildren()) {
            sortNodeChildren(child);
        }
    }

    private void buildDefaultTreeDemo() {
        rootTreeNodes.clear();

        // USB 1 (udisk1)
        TreeNode usb1 = new TreeNode("usb1", "USB 1 (udisk1)", "/storage/udisk1/", "USB 1", true, 0);
        usb1.setTotalTrackCount(247);

        TreeNode descargas = new TreeNode("descargas", "Descargas", "/storage/udisk1/Descargas/", "USB 1", false, 2);
        descargas.setTotalTrackCount(218);

        TreeNode sub1 = new TreeNode("subcarpeta1", "Subcarpeta 1", "/storage/udisk1/Descargas/Subcarpeta 1/", "USB 1", false, 0);
        sub1.setTotalTrackCount(8);

        TreeNode subsub1_1 = new TreeNode("subsub1_1", "Sub subcarpeta 1,1", "/storage/udisk1/Descargas/Subcarpeta 1/Sub subcarpeta 1,1/", "USB 1", false, 3);
        subsub1_1.setTotalTrackCount(5);

        TreeNode subsubDeep = new TreeNode("subsub1_1_1_deep", "SUbcarpet de prueba jh nombre largo de carpetaa 1,1.1", "/storage/udisk1/Descargas/Subcarpeta 1/Sub subcarpeta 1,1/SUbcarpet de prueba jh nombre largo de carpetaa 1,1.1/", "USB 1", false, 2);
        subsubDeep.setTotalTrackCount(2);
        subsub1_1.addChild(subsubDeep);

        TreeNode subsub2 = new TreeNode("subsub2", "Sub subcarpeta 2", "/storage/udisk1/Descargas/Subcarpeta 1/Sub subcarpeta 2/", "USB 1", false, 3);
        subsub2.setTotalTrackCount(3);

        sub1.addChild(subsub1_1);
        sub1.addChild(subsub2);

        TreeNode descargasFecha = new TreeNode("descargas_fecha", "2025-07-18", "/storage/udisk1/Descargas/2025-07-18/", "USB 1", false, 29);
        descargasFecha.setTotalTrackCount(29);

        descargas.addChild(sub1);
        descargas.addChild(descargasFecha);

        TreeNode jhPendrive = new TreeNode("jh_pendrive", "JH PENDRIVE", "/storage/udisk1/JH PENDRIVE/", "USB 1", false, 12);
        jhPendrive.setTotalTrackCount(12);

        usb1.addChild(descargas);
        usb1.addChild(jhPendrive);

        // USB 2 (udisk2)
        TreeNode usb2 = new TreeNode("usb2", "USB 2 (udisk2)", "/storage/udisk2/", "USB 2", true, 0);
        usb2.setTotalTrackCount(84);

        TreeNode musicaViaje = new TreeNode("musica_viaje", "Música Viaje", "/storage/udisk2/Música Viaje/", "USB 2", false, 84);
        musicaViaje.setTotalTrackCount(84);
        usb2.addChild(musicaViaje);

        rootTreeNodes.add(usb1);
        rootTreeNodes.add(usb2);
    }

    private void updateVisibleTreeNodes() {
        visibleTreeNodes.clear();

        List<TreeNode> rootsToDisplay = new ArrayList<>();
        if (currentSearchFilter == FILTER_USB1) {
            for (TreeNode r : rootTreeNodes) {
                if ("USB 1".equalsIgnoreCase(r.getUsbSource()) || "usb1".equalsIgnoreCase(r.getId())) {
                    rootsToDisplay.add(r);
                }
            }
        } else if (currentSearchFilter == FILTER_USB2) {
            for (TreeNode r : rootTreeNodes) {
                if ("USB 2".equalsIgnoreCase(r.getUsbSource()) || "usb2".equalsIgnoreCase(r.getId())) {
                    rootsToDisplay.add(r);
                }
            }
        } else {
            rootsToDisplay.addAll(rootTreeNodes);
        }

        for (TreeNode root : rootsToDisplay) {
            addVisibleNodeRecursive(root, 0, visibleTreeNodes);
        }

        if (folderTreeAdapter != null) {
            folderTreeAdapter.setVisibleNodes(visibleTreeNodes);
            Track currentTrack = getCurrentTrack();
            folderTreeAdapter.setCurrentPlayingTrack(currentTrack);
            folderTreeAdapter.setSelectedFolder(currentSelectedFolder);
        }
    }

    private void addVisibleNodeRecursive(TreeNode node, int level, List<TreeNode> output) {
        node.setLevel(level);
        output.add(node);
        if (node.isExpanded() && node.hasChildren()) {
            for (TreeNode child : node.getChildren()) {
                addVisibleNodeRecursive(child, level + 1, output);
            }
        }
    }

    private void collapseAllTreeNodes() {
        for (TreeNode root : rootTreeNodes) {
            setNodeExpandedRecursive(root, false);
        }
        updateVisibleTreeNodes();
    }

    private void setNodeExpandedRecursive(TreeNode node, boolean expanded) {
        if (node == null) return;
        node.setExpanded(expanded);
        if (node.hasChildren()) {
            for (TreeNode child : node.getChildren()) {
                setNodeExpandedRecursive(child, expanded);
            }
        }
    }

    private void selectFolderFromNode(TreeNode node) {
        if (node == null) return;
        currentSelectedFolder = node.getName();
        currentSelectedFolderUsbSource = node.getUsbSource();
        String usbTag = (currentSelectedFolderUsbSource != null && !currentSelectedFolderUsbSource.isEmpty()) ? "[" + currentSelectedFolderUsbSource + "] " : "";
        tvCurrentPath.setText("📁 " + usbTag + node.getName());

        List<Track> folderTracks = getTracksForNode(node);
        if (!folderTracks.isEmpty()) {
            currentPlaylist.clear();
            currentPlaylist.addAll(folderTracks);
            tabTracks.setText("🎵 Pistas (" + currentPlaylist.size() + ")");
        }
        if (folderTreeAdapter != null) {
            folderTreeAdapter.setSelectedFolder(node.getName());
        }
    }

    private void playFolderFromNode(TreeNode node) {
        if (node == null) return;
        openFolderFromNode(node, true);
    }

    private void openFolderFromNode(TreeNode node, boolean autoPlay) {
        if (node == null) return;
        currentSelectedFolder = node.getName();
        currentSelectedFolderUsbSource = node.getUsbSource();

        List<Track> folderTracks = getTracksForNode(node);
        if (!folderTracks.isEmpty()) {
            currentPlaylist.clear();
            currentPlaylist.addAll(folderTracks);
        }

        currentSearchFilter = FILTER_FOLDER;
        selectTab(1);
        String usbTag = (currentSelectedFolderUsbSource != null && !currentSelectedFolderUsbSource.isEmpty()) ? "[" + currentSelectedFolderUsbSource + "] " : "";
        tvCurrentPath.setText("📁 " + usbTag + node.getName() + " (" + currentPlaylist.size() + ")");

        if (autoPlay && !displayedTracks.isEmpty()) {
            playTrack(0);
        }
    }

    private boolean isTrackFromUsb(Track t, String usbSource) {
        if (t == null || usbSource == null || usbSource.isEmpty()) return true;
        boolean wantUsb2 = usbSource.contains("2") || usbSource.toLowerCase().contains("udisk2");
        boolean wantUsb1 = usbSource.contains("1") || usbSource.toLowerCase().contains("udisk1");

        String tUsb = t.getUsbSource();
        String tPath = t.getFilePath() != null ? t.getFilePath().toLowerCase() : (t.getPath() != null ? t.getPath().toLowerCase() : "");
        boolean isTrackUsb2 = "USB 2".equalsIgnoreCase(tUsb) || tPath.contains("udisk2") || tPath.contains("usb1");

        if (wantUsb2) return isTrackUsb2;
        if (wantUsb1) return !isTrackUsb2;
        return true;
    }

    private List<Track> getTracksForNode(TreeNode node) {
        List<Track> list = new ArrayList<>();
        if (node == null) return list;
        String nodePath = node.getPath();
        String nodeName = node.getName();
        String nodeUsb = node.getUsbSource();

        for (Track t : allTracks) {
            // 1. Debe coincidir el pendrive USB correspondiente al nodo
            if (!isTrackFromUsb(t, nodeUsb)) {
                continue;
            }

            boolean match = false;
            if (nodePath != null && !nodePath.isEmpty() && t.getFilePath() != null && t.getFilePath().startsWith(nodePath)) {
                match = true;
            } else if (nodeName != null && nodeName.equalsIgnoreCase(t.getFolder())) {
                match = true;
            }
            if (match) {
                list.add(t);
            }
        }
        return list;
    }

    private void revealPlayingFolderInTree(Track track) {
        if (track == null || rootTreeNodes.isEmpty()) return;
        String folder = track.getFolder();
        String path = track.getFilePath();

        for (TreeNode root : rootTreeNodes) {
            if (isTrackFromUsb(track, root.getUsbSource()) && expandPathToNode(root, folder, path)) {
                root.setExpanded(true);
            }
        }
        updateVisibleTreeNodes();
    }

    private boolean expandPathToNode(TreeNode current, String targetFolder, String targetPath) {
        if (current == null) return false;
        boolean matchesThis = (!current.isUsbRoot() && (
                (targetFolder != null && targetFolder.equalsIgnoreCase(current.getName())) ||
                (targetPath != null && current.getPath() != null && targetPath.startsWith(current.getPath()))
        ));

        boolean childMatched = false;
        if (current.hasChildren()) {
            for (TreeNode child : current.getChildren()) {
                if (expandPathToNode(child, targetFolder, targetPath)) {
                    childMatched = true;
                }
            }
        }

        if (childMatched || matchesThis) {
            current.setExpanded(true);
            return true;
        }
        return false;
    }

    private Track getCurrentTrack() {
        if (!currentPlaylist.isEmpty() && currentTrackIndex >= 0 && currentTrackIndex < currentPlaylist.size()) {
            return currentPlaylist.get(currentTrackIndex);
        }
        if (!allTracks.isEmpty() && currentTrackIndex >= 0 && currentTrackIndex < allTracks.size()) {
            return allTracks.get(currentTrackIndex);
        }
        return null;
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
            if (layoutSearchLoading != null) layoutSearchLoading.setVisibility(View.VISIBLE);
            if (listViewContent != null) {
                listViewContent.setAlpha(0.25f);
                listViewContent.setEnabled(false);
            }
            if (layoutFastScrollerTrack != null) {
                layoutFastScrollerTrack.setVisibility(View.GONE);
            }
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
                    if (layoutSearchLoading != null) layoutSearchLoading.setVisibility(View.GONE);
                    if (listViewContent != null) {
                        listViewContent.setAlpha(1.0f);
                        listViewContent.setEnabled(true);
                    }
                    if (btnCollapseAllTree != null) btnCollapseAllTree.setVisibility(View.GONE);
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
                if (filter == FILTER_FOLDER && folder != null) {
                    if (!currentPlaylist.isEmpty()) {
                        for (Track t : currentPlaylist) {
                            if (isTrackFromUsb(t, currentSelectedFolderUsbSource)) {
                                sourceTracks.add(t);
                            }
                        }
                    } else {
                        for (Track t : allTracks) {
                            if (t.getFolder() != null && t.getFolder().equalsIgnoreCase(folder) && isTrackFromUsb(t, currentSelectedFolderUsbSource)) {
                                sourceTracks.add(t);
                            }
                        }
                    }
                } else if (filter == FILTER_USB1) {
                    for (Track t : allTracks) {
                        if (isTrackFromUsb(t, "USB 1")) {
                            sourceTracks.add(t);
                        }
                    }
                } else if (filter == FILTER_USB2) {
                    for (Track t : allTracks) {
                        if (isTrackFromUsb(t, "USB 2")) {
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
                    if (layoutSearchLoading != null) layoutSearchLoading.setVisibility(View.GONE);
                    if (listViewContent != null) {
                        listViewContent.setAlpha(1.0f);
                        listViewContent.setEnabled(true);
                    }
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
                    updateFastScrollerVisibility();
                });

            } else {
                // PESTAÑA FAVORITOS: Buscar en todas las canciones favoritas
                List<Track> matchedFavs = new ArrayList<>();
                for (Track t : allTracks) {
                    if (targetVersion != searchVersion) return;
                    if (!t.isFavorite()) continue;
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
                    if (layoutSearchLoading != null) layoutSearchLoading.setVisibility(View.GONE);
                    if (listViewContent != null) {
                        listViewContent.setAlpha(1.0f);
                        listViewContent.setEnabled(true);
                    }
                    displayedTracks.clear();
                    displayedTracks.addAll(matchedFavs);
                    if (listViewContent.getAdapter() != trackAdapter) {
                        listViewContent.setAdapter(trackAdapter);
                    }
                    tvCurrentPath.setText("⭐ Favoritos: \"" + rawQuery + "\" (" + displayedTracks.size() + ")");
                    trackAdapter.setTracks(displayedTracks);
                    updateFastScrollerVisibility();
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
        if (layoutSearchLoading != null) layoutSearchLoading.setVisibility(View.GONE);
        if (listViewContent != null) {
            listViewContent.setAlpha(1.0f);
            listViewContent.setEnabled(true);
        }

        // Modo Normal por pestaña y filtro seleccionado
        if (currentTab == 0) {
            if (btnCollapseAllTree != null) btnCollapseAllTree.setVisibility(View.VISIBLE);
            if (currentSearchFilter == FILTER_USB1) {
                tvCurrentPath.setText("Carpetas USB 1 (Árbol)");
            } else if (currentSearchFilter == FILTER_USB2) {
                tvCurrentPath.setText("Carpetas USB 2 (Árbol)");
            } else {
                tvCurrentPath.setText("Carpetas USB (Árbol de Pendrives)");
            }

            updateVisibleTreeNodes();
            if (listViewContent.getAdapter() != folderTreeAdapter) {
                listViewContent.setAdapter(folderTreeAdapter);
            }

        } else if (currentTab == 1) {
            if (btnCollapseAllTree != null) btnCollapseAllTree.setVisibility(View.GONE);
            displayedTracks.clear();

            if (currentSearchFilter == FILTER_USB1) {
                for (Track t : allTracks) {
                    if (isTrackFromUsb(t, "USB 1")) {
                        displayedTracks.add(t);
                    }
                }
                tvCurrentPath.setText("🎵 Pistas USB 1 (" + displayedTracks.size() + ")");
            } else if (currentSearchFilter == FILTER_USB2) {
                for (Track t : allTracks) {
                    if (isTrackFromUsb(t, "USB 2")) {
                        displayedTracks.add(t);
                    }
                }
                tvCurrentPath.setText("🎵 Pistas USB 2 (" + displayedTracks.size() + ")");
            } else if (currentSearchFilter == FILTER_ALL) {
                displayedTracks.addAll(allTracks);
                tvCurrentPath.setText("🎵 Todas las pistas (" + displayedTracks.size() + ")");
            } else {
                // FILTER_FOLDER: mostrar canciones de la carpeta actual seleccionada
                if (currentPlaylist != null && !currentPlaylist.isEmpty()) {
                    displayedTracks.addAll(currentPlaylist);
                } else if (currentSelectedFolder != null) {
                    for (Track t : allTracks) {
                        if (t.getFolder() != null && t.getFolder().equalsIgnoreCase(currentSelectedFolder)) {
                            if (isTrackFromUsb(t, currentSelectedFolderUsbSource)) {
                                displayedTracks.add(t);
                            }
                        }
                    }
                } else if (!allTracks.isEmpty()) {
                    currentSelectedFolder = allTracks.get(0).getFolder();
                    currentSelectedFolderUsbSource = allTracks.get(0).getUsbSource();
                    for (Track t : allTracks) {
                        if (t.getFolder() != null && t.getFolder().equalsIgnoreCase(currentSelectedFolder)) {
                            if (isTrackFromUsb(t, currentSelectedFolderUsbSource)) {
                                displayedTracks.add(t);
                            }
                        }
                    }
                }
                String usbTag = (currentSelectedFolderUsbSource != null && !currentSelectedFolderUsbSource.isEmpty()) ? "[" + currentSelectedFolderUsbSource + "] " : "";
                tvCurrentPath.setText(currentSelectedFolder != null ? "📁 " + usbTag + currentSelectedFolder + " (" + displayedTracks.size() + ")" : "🎵 Pistas (" + displayedTracks.size() + ")");
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
            if (btnCollapseAllTree != null) btnCollapseAllTree.setVisibility(View.GONE);
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
        updateFastScrollerVisibility();
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
            currentSelectedFolderUsbSource = track.getUsbSource();
            if (currentSelectedFolderUsbSource == null || currentSelectedFolderUsbSource.isEmpty()) {
                String p = track.getFilePath() != null ? track.getFilePath().toLowerCase() : "";
                if (p.contains("udisk2") || p.contains("usb2")) {
                    currentSelectedFolderUsbSource = "USB 2";
                } else if (p.contains("udisk1") || p.contains("usb1") || p.contains("usb0")) {
                    currentSelectedFolderUsbSource = "USB 1";
                }
            }
            updateFilterChipsVisibility();
            currentPlaylist.clear();
            for (Track t : allTracks) {
                if (t.getFolder() != null && t.getFolder().equalsIgnoreCase(currentSelectedFolder)) {
                    if (isTrackFromUsb(t, currentSelectedFolderUsbSource)) {
                        currentPlaylist.add(t);
                    }
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
            updateNowPlayingInfo(track);

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

            updateNowPlayingInfo(track);

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
            String embeddedArtist = null;
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
                embeddedArtist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                if (embeddedArtist == null || embeddedArtist.trim().isEmpty()) {
                    embeddedArtist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
                }
                mmr.release();
            } catch (Exception ignored) {}

            final Bitmap finalArt = art;
            final String finalFormat = exactFormat;
            final String finalArtist = (embeddedArtist != null && !embeddedArtist.trim().isEmpty()) ? embeddedArtist.trim() : null;
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
                if (finalArtist != null) {
                    if (track.getArtist() == null || track.getArtist().trim().isEmpty() || "Pendrive".equalsIgnoreCase(track.getArtist())) {
                        track.setArtist(finalArtist);
                        List<Track> queue = getQueue();
                        if (currentTrackIndex >= 0 && currentTrackIndex < queue.size()) {
                            if (filePath.equals(queue.get(currentTrackIndex).getFilePath())) {
                                updateNowPlayingInfo(track);
                            }
                        }
                    }
                }
            });
        });
    }

    private void updateNowPlayingInfo(Track track) {
        if (track == null) return;
        tvTrackTitle.setText(track.getTitle());
        String art = track.getArtist();
        if (art != null && !art.trim().isEmpty() && !"Pendrive".equalsIgnoreCase(art.trim())) {
            tvTrackArtist.setVisibility(View.VISIBLE);
            tvTrackArtist.setText(art);
            if (tvTrackArtistDot != null) tvTrackArtistDot.setVisibility(View.VISIBLE);
        } else {
            tvTrackArtist.setVisibility(View.GONE);
            if (tvTrackArtistDot != null) tvTrackArtistDot.setVisibility(View.GONE);
        }
        tvTrackAlbum.setText(track.getAlbum());
        tvFormatBadge.setText(track.getFormat());
        tvHeaderPath.setText("Q hubo jefe 😉");

        if (folderTreeAdapter != null) {
            folderTreeAdapter.setCurrentPlayingTrack(track);
            revealPlayingFolderInTree(track);
        }
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

            // Montajes de Pendrives USB en plataformas de auto (AC8227L, Allwinner, Rockchip, MTK, etc.)
            String[] targetUsbRoots = {
                    "/storage/udisk1",
                    "/storage/udisk2",
                    "/storage/udisk3",
                    "/storage/usb0",
                    "/storage/usb1",
                    "/mnt/media_rw/udisk1",
                    "/mnt/media_rw/udisk2",
                    "/mnt/udisk1",
                    "/mnt/udisk2"
            };

            for (String rootPath : targetUsbRoots) {
                File dir = new File(rootPath);
                if (dir.exists() && dir.isDirectory()) {
                    String usbTag = (rootPath.contains("udisk2") || rootPath.contains("usb1")) ? "USB 2" : "USB 1";
                    scanDirectory(dir, foundTracks, seenPaths, usbTag, 0);
                }
            }

            // Escaneo complementario mediante MediaStore (SOLO pistas ubicadas en USB 1 o USB 2)
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
                        // Filtro estricto: solo incluir canciones que provengan de pendrives USB
                        if (path != null && (path.contains("udisk") || path.contains("usb")) && !seenPaths.contains(path)) {
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
                                String usbTag = (path.contains("udisk2") || path.contains("usb1")) ? "USB 2" : "USB 1";

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

                // Ordenar pistas alfabéticamente por título (A-Z)
                Collections.sort(foundTracks, (t1, t2) -> {
                    String s1 = t1.getTitle() != null ? t1.getTitle().trim() : "";
                    String s2 = t2.getTitle() != null ? t2.getTitle().trim() : "";
                    return s1.compareToIgnoreCase(s2);
                });

                // Ordenar carpetas alfabéticamente por nombre (A-Z)
                Collections.sort(generatedFolders, (f1, f2) -> {
                    String n1 = f1.getName() != null ? f1.getName().trim() : "";
                    String n2 = f2.getName() != null ? f2.getName().trim() : "";
                    return n1.compareToIgnoreCase(n2);
                });

                allTracks.clear();
                allTracks.addAll(foundTracks);

                folderList.clear();
                folderList.addAll(generatedFolders);

                buildTreeFromTracks(allTracks);
                updateVisibleTreeNodes();

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
                        String artist = "";
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
    protected void onResume() {
        super.onResume();
        setImmersiveMode();
        if (etSearch != null) {
            etSearch.clearFocus();
        }
        hideKeyboard();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideKeyboard();
        if (etSearch != null) {
            etSearch.clearFocus();
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
