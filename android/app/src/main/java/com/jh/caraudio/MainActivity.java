package com.jh.caraudio;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.Gravity;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
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
    private ImageButton btnAutoMix;
    private AutoMixer autoMixer;
    private AudioAnalyzer audioAnalyzer;
    private View layoutExplorerToggleZone;
    private View layoutAlbumArtCard;
    private ImageView ivAlbumArt;
    private NowPlayingPillView nowPlayingPill; // cápsula PAUSED / PLAYING / MIXING ⇄ con ecualizador
    private LaserRingView laserRing;           // borde láser giratorio de la carátula
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

    // Explorador y Reproductor
    private LinearLayout layoutExplorerPanel;
    private View layoutPlayerPanel;
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

    // Detección de 1 toque (cambio de pista) vs 2 toques (adelantar/retroceder 10s)
    private static final int SEEK_SKIP_MS = 10000;
    private static final int DOUBLE_TAP_TIMEOUT_MS = 280;
    private final Handler navTapHandler = new Handler(Looper.getMainLooper());
    private Runnable nextTapRunnable = null;
    private Runnable prevTapRunnable = null;
    private int nextTapHits = 0;
    private int prevTapHits = 0;
    private final Runnable resetNextTapHitsRunnable = () -> nextTapHits = 0;
    private final Runnable resetPrevTapHitsRunnable = () -> prevTapHits = 0;

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
    private int currentTrackIndex = -1; // posición dentro de playQueue
    private final List<Track> playQueue = new ArrayList<>();
    private boolean isPlaying = false;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private boolean isMuted = false;
    private int currentTab = 0; // 0: Carpetas, 1: Pistas, 2: Favoritos
    // Búsqueda de Pistas guardada al cambiar de pestaña (se descarta al abrir otra carpeta con [N ▶])
    private String savedTracksQuery = "";
    private int savedTracksFilter = FILTER_FOLDER;
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

    private static final String[] USB1_MOUNT_PATHS = {"/storage/udisk1", "/mnt/media_rw/udisk1", "/mnt/udisk1", "/storage/usb0"};
    private static final String[] USB2_MOUNT_PATHS = {"/storage/udisk2", "/mnt/media_rw/udisk2", "/mnt/udisk2", "/storage/usb1"};

    // Re-escaneo automático al conectar/quitar un pendrive (con espera para agrupar los eventos seguidos)
    private final Runnable usbRescanRunnable = this::startUsbScan;
    private final BroadcastReceiver usbMountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handler.removeCallbacks(usbRescanRunnable);
            handler.postDelayed(usbRescanRunnable, 1500);
        }
    };

    private TextView btnFilterFolder;
    private TextView btnFilterAll;
    private TextView btnFilterUsb1;
    private TextView btnFilterUsb2;

    // Búsqueda Asíncrona con Debounce
    private final Handler searchDebounceHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable = null;
    private final ExecutorService searchExecutor = Executors.newSingleThreadExecutor();
    // Lectura de duraciones en segundo plano (hilo propio para no retrasar carátulas ni escaneos)
    private final ExecutorService durationExecutor = Executors.newSingleThreadExecutor();
    private final ArrayDeque<Track> pendingDurations = new ArrayDeque<>(); // protegido por sí mismo
    private final Set<Track> visibleDurationTracks = new HashSet<>();      // protegido por pendingDurations
    private volatile int durationGeneration = 0;
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
        audioAnalyzer = new AudioAnalyzer();
        autoMixer = new AutoMixer(handler, autoMixHost, audioAnalyzer);
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
        // Automix recuerda si quedó activado
        autoMixer.setEnabled(getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(PREF_AUTOMIX, false));
        updateAutoMixButton();
        setupExplorerControls();
        setupProgressUpdater();

        checkAndRequestStoragePermissions();

        // Detectar cuando se conecta o se quita un pendrive para actualizar Carpetas, Pistas y filtros
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        usbFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        usbFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        usbFilter.addDataScheme("file");
        registerReceiver(usbMountReceiver, usbFilter);

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
        btnAutoMix = findViewById(R.id.btnAutoMix);
        layoutExplorerToggleZone = findViewById(R.id.layoutExplorerToggleZone);
        layoutAlbumArtCard = findViewById(R.id.layoutAlbumArtCard);
        ivAlbumArt = findViewById(R.id.ivAlbumArt);
        nowPlayingPill = findViewById(R.id.nowPlayingPill);
        laserRing = findViewById(R.id.laserRing);
        // La carátula se recorta con las esquinas redondeadas del interior (como overflow:hidden en la web)
        View albumInner = findViewById(R.id.layoutAlbumArtInner);
        if (albumInner != null) albumInner.setClipToOutline(true);
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

        // Explorador y reproductor
        layoutExplorerPanel = findViewById(R.id.layoutExplorerPanel);
        layoutPlayerPanel = findViewById(R.id.layoutPlayerPanel);
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
                    saveFavorite(track);
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
                // Un toque solo explora (expande/contrae); la carpeta activa de Pistas se elige con [N ▶]
                if (node.hasChildren()) {
                    node.setExpanded(!node.isExpanded());
                    updateVisibleTreeNodes();
                }
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
                            setExplorerVisible(true);
                            return true;
                        }
                    }
                    // Si el explorador está VISIBLE: Deslizar hacia la izquierda cierra el explorador
                    else {
                        if (dx < -35 && Math.abs(dx) > Math.abs(dy) * 1.1f) {
                            globalGestureHandled = true;
                            hideKeyboard();
                            setExplorerVisible(false);
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
                            setExplorerVisible(true);
                            return true;
                        }
                    } else {
                        if (dx < -35 && Math.abs(dx) > Math.abs(dy) * 1.1f) {
                            globalGestureHandled = true;
                            hideKeyboard();
                            setExplorerVisible(false);
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
                if (isMuted) setMuted(false);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // El mute pudo quedar activo al salir con HOME o venir del sistema del radio: leer el estado real
            isMuted = audioManager.isStreamMute(AudioManager.STREAM_MUSIC);
        }
        btnVolMute.setSelected(isMuted);
        if (isMuted) {
            // En mute getStreamVolume devuelve 0: se conserva el último porcentaje real para restaurarlo al desmutear
            tvVolPercent.setText("MUTE");
            return;
        }

        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int percent = max > 0 ? Math.round(((float) current / max) * 100) : 0;
        currentVolPercent = percent;

        tvVolPercent.setText(percent + "%");
        seekVolume.setProgress(percent);
    }

    private void adjustVolumeStep(boolean up) {
        if (audioManager == null) return;
        if (isMuted) setMuted(false);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isMuted = audioManager.isStreamMute(AudioManager.STREAM_MUSIC);
        }
        setMuted(!isMuted);
    }

    private void setMuted(boolean mute) {
        if (audioManager == null) return;
        isMuted = mute;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    mute ? AudioManager.ADJUST_MUTE : AudioManager.ADJUST_UNMUTE, 0);
        } else {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, mute);
        }
        // Al desmutear se lee el volumen real restaurado por el sistema (no el valor guardado en la app)
        updateVolumeUI();
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
        btnNext.setOnClickListener(v -> handleNextButtonClick());
        btnPrev.setOnClickListener(v -> handlePrevButtonClick());

        // Tarjetas flotantes anterior y siguiente
        if (layoutPrevCard != null) layoutPrevCard.setOnClickListener(v -> handlePrevButtonClick());
        if (layoutNextCard != null) layoutNextCard.setOnClickListener(v -> handleNextButtonClick());

        // Botón y Zona Táctil Ergonómica para alternar o deslizar el explorador de música
        setupExplorerToggleGestures();

        btnShuffle.setOnClickListener(v -> {
            isShuffle = !isShuffle;
            btnShuffle.setSelected(isShuffle);
            autoMixer.invalidateNext();
            Toast.makeText(this, isShuffle ? "Aleatorio activado" : "Modo secuencial", Toast.LENGTH_SHORT).show();
        });

        btnRepeat.setOnClickListener(v -> {
            isRepeat = !isRepeat;
            btnRepeat.setSelected(isRepeat);
            autoMixer.invalidateNext();
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
                    autoMixer.finishMixNow(); // adelantar en medio de una mezcla la cancela
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
            tvPlaybackStatus.setText(autoMixer.isEnabled() ? "● REPRODUCIENDO · AUTOMIX" : "● REPRODUCIENDO");
            tvPlaybackStatus.setTextColor(Color.parseColor("#38bdf8"));
            if (nowPlayingPill != null) nowPlayingPill.setPlaying(true);
            if (laserRing != null) laserRing.setPlaying(true);
        } else {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            tvPlaybackStatus.setText("● EN PAUSA");
            tvPlaybackStatus.setTextColor(Color.parseColor("#f59e0b"));
            if (nowPlayingPill != null) nowPlayingPill.setPlaying(false);
            if (laserRing != null) laserRing.setPlaying(false);
        }
        // Activar marquesina en el título
        tvTrackTitle.setSelected(isPlaying);
    }

    // ==========================================
    // GESTOS ERGONÓMICOS DEL EXPLORADOR PARA EL CONDUCTOR
    // Botón de lupa (click conmuta) y zona libre roja lateral (jalar a la izq oculta, a la der muestra; toques ignorados)
    // ==========================================
    private void setupExplorerToggleGestures() {
        // Lupa y Automix están dentro de la zona de gestos: no son clicables por sí mismos para que
        // el gesto de jalar funcione aunque empiece sobre ellos; el toque se detecta en la zona.
        if (btnToggleExplorer != null) {
            btnToggleExplorer.setClickable(false);
            btnToggleExplorer.setFocusable(false);
        }
        if (btnAutoMix != null) {
            btnAutoMix.setClickable(false);
            btnAutoMix.setFocusable(false);
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
                            if (btnToggleExplorer != null && isTouchOnView(btnToggleExplorer, downX, downY)) {
                                btnToggleExplorer.setPressed(true);
                            } else if (btnAutoMix != null && isTouchOnView(btnAutoMix, downX, downY)) {
                                btnAutoMix.setPressed(true);
                            }
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
                                        setExplorerVisible(false);
                                        return true;
                                    }
                                    // Jalar hacia la DERECHA (->) muestra el explorador
                                    else if (dx > 35 && layoutExplorerPanel.getVisibility() != View.VISIBLE) {
                                        gestureHandled = true;
                                        setExplorerVisible(true);
                                        return true;
                                    }
                                }
                            }
                            break;

                        case MotionEvent.ACTION_UP:
                            if (btnToggleExplorer != null) btnToggleExplorer.setPressed(false);
                            if (btnAutoMix != null) btnAutoMix.setPressed(false);
                            if (!gestureHandled && layoutExplorerPanel != null) {
                                float totalDx = event.getRawX() - downX;
                                float totalDy = event.getRawY() - downY;
                                if (Math.abs(totalDx) > 35 && Math.abs(totalDx) > Math.abs(totalDy) * 1.1f) {
                                    if (totalDx < -35 && layoutExplorerPanel.getVisibility() == View.VISIBLE) {
                                        hideKeyboard();
                                        setExplorerVisible(false);
                                    } else if (totalDx > 35 && layoutExplorerPanel.getVisibility() != View.VISIBLE) {
                                        setExplorerVisible(true);
                                    }
                                } else if (Math.abs(totalDx) <= 35 && Math.abs(totalDy) <= 35
                                        && btnToggleExplorer != null && isTouchOnView(btnToggleExplorer, downX, downY)) {
                                    // Toque simple sobre la lupa: alterna el explorador
                                    toggleExplorerVisibility();
                                } else if (Math.abs(totalDx) <= 35 && Math.abs(totalDy) <= 35
                                        && btnAutoMix != null && isTouchOnView(btnAutoMix, downX, downY)) {
                                    // Toque simple sobre Automix: activa / desactiva la mezcla entre canciones
                                    toggleAutoMix();
                                }
                                // Un toque simple en el resto de la zona libre NO hace nada (evita toques accidentales).
                            }
                            return true;

                        case MotionEvent.ACTION_CANCEL:
                            if (btnToggleExplorer != null) btnToggleExplorer.setPressed(false);
                            if (btnAutoMix != null) btnAutoMix.setPressed(false);
                            gestureHandled = false;
                            break;
                    }
                    return true;
                }
            });
        }
    }

    private boolean isTouchOnView(View view, float rawX, float rawY) {
        Rect rect = new Rect();
        if (!view.getGlobalVisibleRect(rect)) return false;
        rect.inset(-10, -10);
        return rect.contains((int) rawX, (int) rawY);
    }

    private void toggleExplorerVisibility() {
        hideKeyboard();
        if (layoutExplorerPanel != null) {
            setExplorerVisible(layoutExplorerPanel.getVisibility() != View.VISIBLE);
        }
    }

    /** Muestra u oculta el explorador; igual que la web, la lupa queda resaltada mientras está oculto. */
    private void setExplorerVisible(boolean visible) {
        if (layoutExplorerPanel == null) return;
        layoutExplorerPanel.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (btnToggleExplorer != null) btnToggleExplorer.setSelected(!visible);
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
            // Solo visible en Pistas y Favoritos: reproduce la lista que se está mostrando
            if (!displayedTracks.isEmpty()) {
                playTrack(0);
            } else if (!currentPlaylist.isEmpty()) {
                displayedTracks.clear();
                displayedTracks.addAll(currentPlaylist);
                showDisplayedTracks();
                playTrack(0);
            }
        });

        // Búsqueda inteligente optimizada: debounce adaptativo (500ms con 1-2 letras, 250ms después)
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
                    // Se conserva el chip activo; con el campo vacío Pistas muestra la carpeta activa
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

                // Esperar a que el usuario termine de teclear: con 1-2 letras casi todo coincide y
                // seguramente seguirá escribiendo, así que se espera más (500ms); si no, 250ms
                int debounceMs = q.trim().length() <= 2 ? 500 : 250;
                searchRunnable = () -> performAsyncSearch(q, thisVersion);
                searchDebounceHandler.postDelayed(searchRunnable, debounceMs);
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
            // Al borrar la búsqueda se conserva el chip activo y se muestran las canciones de la carpeta activa
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

    /**
     * En el radio la carpeta de montaje (/storage/udisk2...) sigue existiendo vacía aunque se quite
     * el pendrive: solo se considera conectado si está montado y tiene contenido.
     */
    private static boolean isUsbMounted(String[] mountPaths) {
        for (String p : mountPaths) {
            File[] entries = new File(p).listFiles();
            if (entries != null && entries.length > 0) return true;
        }
        return false;
    }

    private void checkUsbPresence() {
        hasUsb1 = isUsbMounted(USB1_MOUNT_PATHS);
        hasUsb2 = isUsbMounted(USB2_MOUNT_PATHS);
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
        // Recordar la búsqueda de Pistas (texto + chip) para recuperarla al volver a la pestaña
        if (currentTab == 1 && etSearch != null) {
            savedTracksQuery = etSearch.getText().toString();
            savedTracksFilter = currentSearchFilter;
        }
        boolean restoreTracksSearch = tabIndex == 1 && !savedTracksQuery.trim().isEmpty();

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
        if (btnPlayAll != null) {
            // En Carpetas USB cada carpeta tiene su propio botón [N ▶]
            btnPlayAll.setVisibility(tabIndex == 0 ? View.GONE : View.VISIBLE);
        }
        if (etSearch != null) {
            // El texto de ayuda indica qué busca cada pestaña
            etSearch.setHint(tabIndex == 0 ? "Buscar carpeta..." :
                    (tabIndex == 1 ? "Buscar canción o artista..." : "Buscar en favoritos..."));
        }
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
            if (restoreTracksSearch) {
                // Hay una búsqueda pendiente: conservar el chip con el que se estaba buscando
                currentSearchFilter = savedTracksFilter;
            } else if (currentSelectedFolder != null) {
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
        if (restoreTracksSearch) {
            etSearch.setText(savedTracksQuery);
            etSearch.setSelection(savedTracksQuery.length());
            // Relanzar la búsqueda de inmediato, sin esperar el debounce del TextWatcher
            if (searchRunnable != null) {
                searchDebounceHandler.removeCallbacks(searchRunnable);
            }
            searchVersion++;
            performAsyncSearch(savedTracksQuery, searchVersion);
        } else {
            filterByTab();
        }
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
        savedTracksQuery = ""; // Se eligió otra carpeta: la búsqueda anterior de Pistas ya no aplica
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
                        child.addDirectTrack(track);
                    }
                    currentNode = child;
                }
            } else {
                rootNode.addDirectTrack(track);
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

    /** El número de la pestaña Pistas es siempre el de la carpeta activa, nunca el de una búsqueda. */
    private void updateTracksTabCount() {
        tabTracks.setText("🎵 Pistas (" + currentPlaylist.size() + ")");
    }

    private void playFolderFromNode(TreeNode node) {
        if (node == null) return;
        // Al pulsar el botón de la carpeta en Carpetas USB: abrirla en la pestaña Pistas sin autoreproducir
        openFolderFromNode(node, false);
    }

    private void openFolderFromNode(TreeNode node, boolean autoPlay) {
        if (node == null) return;
        currentSelectedFolder = node.getName();
        currentSelectedFolderUsbSource = node.getUsbSource();

        List<Track> folderTracks = getTracksForNode(node);
        if (!folderTracks.isEmpty()) {
            currentPlaylist.clear();
            currentPlaylist.addAll(folderTracks);
            updateTracksTabCount();
        }
        if (folderTreeAdapter != null) {
            folderTreeAdapter.setSelectedFolder(node.getName());
        }

        currentSearchFilter = FILTER_FOLDER;
        savedTracksQuery = ""; // Se eligió otra carpeta: la búsqueda anterior de Pistas ya no aplica
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

    /**
     * Canciones que están directamente en la carpeta del nodo: las mismas que cuenta el botón [N ▶].
     * Si la carpeta solo tiene subcarpetas (o es la raíz del USB), devuelve todas las de sus subcarpetas.
     */
    private List<Track> getTracksForNode(TreeNode node) {
        List<Track> list = new ArrayList<>();
        if (node == null) return list;

        if (!node.getDirectTracks().isEmpty()) {
            list.addAll(node.getDirectTracks());
            return list;
        }

        Set<Track> subtreeTracks = new HashSet<>();
        collectSubtreeTracks(node, subtreeTracks);
        // Recorrer allTracks para conservar el orden alfabético global
        for (Track t : allTracks) {
            if (subtreeTracks.contains(t)) list.add(t);
        }
        return list;
    }

    private void collectSubtreeTracks(TreeNode node, Set<Track> out) {
        out.addAll(node.getDirectTracks());
        for (TreeNode child : node.getChildren()) {
            collectSubtreeTracks(child, out);
        }
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
        List<Track> queue = getQueue();
        if (currentTrackIndex >= 0 && currentTrackIndex < queue.size()) {
            return queue.get(currentTrackIndex);
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
        return SearchText.normalize(text);
    }

    private String dedupChars(String s) {
        return SearchText.dedup(s);
    }

    /** Resultado de búsqueda con su puntaje ya calculado (una vez por elemento, no en cada comparación del sort). */
    private static final class Scored<T> {
        final T item;
        final int score;

        Scored(T item, int score) {
            this.item = item;
            this.score = score;
        }
    }

    private static <T> List<T> sortByScore(List<Scored<T>> scored) {
        Collections.sort(scored, (a, b) -> Integer.compare(b.score, a.score));
        List<T> sorted = new ArrayList<>(scored.size());
        for (Scored<T> s : scored) sorted.add(s.item);
        return sorted;
    }

    private static String[] dedupTokens(String[] tokens) {
        String[] out = new String[tokens.length];
        for (int i = 0; i < tokens.length; i++) out[i] = SearchText.dedup(tokens[i]);
        return out;
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

    private boolean fastTrackMatches(Track track, String[] tokens, String[] tokensDedup) {
        Track.SearchKey key = track.getSearchKey();

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].isEmpty()) continue;
            // Búsqueda ultra rápida en subcadenas sobre texto ya normalizado
            if (key.haystack.contains(tokens[i])) continue;
            if (key.haystackDedup.contains(tokensDedup[i])) continue;
            return false;
        }
        return true;
    }

    private int calculateTrackRelevance(Track track, String normQuery, String[] tokens) {
        int score = 0;
        Track.SearchKey key = track.getSearchKey();
        String titleNorm = key.title;
        String artistNorm = key.artist;
        String albumNorm = key.album;
        String folderNorm = key.folder;

        String[] titleWords = key.titleWords;
        String[] artistWords = key.artistWords;

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
        FolderItem.SearchKey key = folder.getSearchKey();
        String nameNorm = key.name;
        String[] words = key.words;

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
            String[] tokens = SearchText.tokens(norm);
            String[] tokensDedup = dedupTokens(tokens);

            if (tab == 0) {
                // PESTAÑA CARPETAS: Buscar con filtro USB 1 / USB 2 / TODO
                List<Scored<FolderItem>> scoredFolders = new ArrayList<>();
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

                    FolderItem.SearchKey key = f.getSearchKey();
                    boolean match = true;
                    for (int i = 0; i < tokens.length; i++) {
                        if (tokens[i].isEmpty()) continue;
                        if (!key.haystack.contains(tokens[i]) && !key.haystackDedup.contains(tokensDedup[i])) {
                            match = false;
                            break;
                        }
                    }
                    if (match) scoredFolders.add(new Scored<>(f, calculateFolderRelevance(f, norm, tokens)));
                }

                List<FolderItem> matchedFolders = sortByScore(scoredFolders);

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

                List<Scored<Track>> scoredTracks = new ArrayList<>();
                for (Track t : sourceTracks) {
                    if (targetVersion != searchVersion) return;
                    if (fastTrackMatches(t, tokens, tokensDedup)) {
                        scoredTracks.add(new Scored<>(t, calculateTrackRelevance(t, norm, tokens)));
                    }
                }

                List<Track> matchedTracks = sortByScore(scoredTracks);

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
                    // La cantidad de resultados va en la ruta; el número de la pestaña sigue siendo el de la carpeta
                    tvCurrentPath.setText("🔍 Pistas" + filterLabel + ": \"" + rawQuery + "\" (" + displayedTracks.size() + ")");
                    showDisplayedTracks();
                    updateFastScrollerVisibility();
                });

            } else {
                // PESTAÑA FAVORITOS: Buscar en todas las canciones favoritas
                List<Scored<Track>> scoredFavs = new ArrayList<>();
                for (Track t : allTracks) {
                    if (targetVersion != searchVersion) return;
                    if (!t.isFavorite()) continue;
                    if (fastTrackMatches(t, tokens, tokensDedup)) {
                        scoredFavs.add(new Scored<>(t, calculateTrackRelevance(t, norm, tokens)));
                    }
                }

                List<Track> matchedFavs = sortByScore(scoredFavs);

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
                    showDisplayedTracks();
                    updateFastScrollerVisibility();
                });
            }
        });
    }

    private void filterSearch(String rawQuery) {
        String norm = normalizeText(rawQuery);
        String[] tokens = SearchText.tokens(norm);
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

            // Sin texto en el buscador, Pistas muestra siempre la carpeta activa (la elegida con [N ▶]
            // o la de la última canción reproducida), sea cual sea el chip. Los chips solo definen dónde buscar.
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
            }
            // Si aún no se eligió ninguna carpeta, la lista queda vacía (no se elige una automáticamente)
            String usbTag = (currentSelectedFolderUsbSource != null && !currentSelectedFolderUsbSource.isEmpty()) ? "[" + currentSelectedFolderUsbSource + "] " : "";
            tvCurrentPath.setText(currentSelectedFolder != null ? "📁 " + usbTag + currentSelectedFolder + " (" + displayedTracks.size() + ")" : "🔍 Elige una carpeta o escribe para buscar");

            showDisplayedTracks();
            if (listViewContent.getAdapter() != trackAdapter) {
                listViewContent.setAdapter(trackAdapter);
            }
            updateTracksTabCount();
            trackAdapter.setCurrentPlayingTrack(getCurrentTrack());

        } else if (currentTab == 2) {
            if (btnCollapseAllTree != null) btnCollapseAllTree.setVisibility(View.GONE);
            displayedTracks.clear();
            for (Track t : allTracks) {
                if (t.isFavorite()) {
                    displayedTracks.add(t);
                }
            }
            showDisplayedTracks();
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

        int indexInPlaylist = currentPlaylist.indexOf(track);
        if (currentTab == 2) {
            // Desde Favoritos: la cola es la lista de favoritos mostrada; la carpeta activa de Pistas no cambia
            playQueue.clear();
            playQueue.addAll(displayedTracks);
            currentTrackIndex = index;
        } else if (indexInPlaylist >= 0) {
            // La canción ya es de la carpeta activa: conservar esa lista tal cual (la del botón [N ▶])
            setPlayQueueFromFolder();
            currentTrackIndex = indexInPlaylist;
        } else if (track.getFolder() != null && !track.getFolder().isEmpty()) {
            // Canción de otra carpeta (p. ej. desde una búsqueda): su carpeta pasa a ser la activa
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
            setPlayQueueFromFolder();
            currentTrackIndex = folderIdx >= 0 ? folderIdx : 0;
            updateTracksTabCount();
            if (folderTreeAdapter != null) {
                folderTreeAdapter.setSelectedFolder(currentSelectedFolder);
            }
        } else {
            playQueue.clear();
            playQueue.addAll(displayedTracks);
            currentTrackIndex = index;
        }

        if (!startPlayback(track)) {
            Toast.makeText(this, "Error al reproducir: " + track.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    // ==========================================
    // INICIO DE REPRODUCCIÓN (compartido por toque, Siguiente/Anterior y Automix)
    // ==========================================
    private MediaPlayer createPlayer() {
        MediaPlayer mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnCompletionListener(this::onPlayerCompleted);
        return mp;
    }

    private void onPlayerCompleted(MediaPlayer mp) {
        if (autoMixer.isOutgoing(mp)) {
            // La canción que salía en la mezcla terminó antes que el fundido
            autoMixer.onOutgoingCompleted();
            return;
        }
        if (mp != mediaPlayer) return;
        if (isRepeat) {
            playTrackFromQueue(getQueue(), currentTrackIndex);
            return;
        }
        // Automix: si la canción terminó antes de lo previsto, la siguiente ya precargada entra al instante
        if (autoMixer.startPreparedNextNow()) return;
        playNextTrack();
    }

    private boolean startPlayback(Track track) {
        return startPlayback(track, false);
    }

    /**
     * Reproduce una canción desde cero (inicio manual). Devuelve false si el archivo no se pudo abrir.
     * quickFade: con Automix activo y música sonando, cambia con un fundido corto en vez de un corte seco.
     */
    private boolean startPlayback(Track track, boolean quickFade) {
        boolean fade = quickFade && autoMixer.isEnabled() && mediaPlayer != null && isPlaying;
        autoMixer.onManualPlayback();
        if (fade) {
            MediaPlayer old = mediaPlayer;
            MediaPlayer incoming = createPlayer();
            try {
                incoming.setDataSource(track.getFilePath());
                incoming.prepare();
            } catch (Exception e) {
                e.printStackTrace();
                releasePlayerQuietly(incoming);
                return false;
            }
            mediaPlayer = incoming;
            if (autoMixer.fadeManual(old, incoming)) {
                isPlaying = true;
                onTrackStarted(track);
                return true;
            }
            // No pudo arrancar con fundido: volver al reproductor anterior y seguir por el camino normal
            mediaPlayer = old;
            releasePlayerQuietly(incoming);
        }
        try {
            if (mediaPlayer == null) {
                mediaPlayer = createPlayer();
            } else {
                mediaPlayer.reset();
            }
            mediaPlayer.setDataSource(track.getFilePath());
            mediaPlayer.prepare();
            mediaPlayer.setVolume(1f, 1f);
            mediaPlayer.start();
            isPlaying = true;
            onTrackStarted(track);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void releasePlayerQuietly(MediaPlayer mp) {
        try {
            mp.release();
        } catch (Exception ignored) {}
    }

    /** Actualiza la pantalla para la canción que acaba de empezar (a mano o por mezcla). */
    private void onTrackStarted(Track track) {
        onTrackStarted(track, false);
    }

    /**
     * smoothUi: mezcla automática. Igual que la web: los datos cambian al instante (sin vacíos ni
     * parpadeos) y solo la carátula cambia con fundido cruzado.
     */
    private void onTrackStarted(Track track, boolean smoothUi) {
        // Automix analiza en segundo plano dónde se apaga esta canción
        autoMixer.onTrackStarted(track);

        int durationMs = mediaPlayer.getDuration();
        if (durationMs > 0) track.setDurationSec(durationMs / 1000);

        albumArtFadeForPath = smoothUi ? track.getFilePath() : null;
        showTrackOnScreen(track);
    }

    /** Pone en pantalla los datos de la canción que suena. */
    private void showTrackOnScreen(Track track) {
        // Marcar la canción en la lista mostrada (si está en ella)
        trackAdapter.setCurrentPlayingTrack(track);
        updatePlaybackStateUI();

        // Actualizar información en pantalla con tipografía grande y clara
        updateNowPlayingInfo(track);

        int durationMs = 0;
        try {
            durationMs = mediaPlayer != null ? mediaPlayer.getDuration() : 0;
        } catch (Exception ignored) {}
        tvTimeTotal.setText(formatMs(durationMs));
        tvTimeCurrent.setText("00:00");
        seekTrackProgress.setProgress(0);

        // Cargar carátula del archivo y bitrate exacto
        loadAlbumArt(track);

        // Previsualización de canción anterior y siguiente de la carpeta activa
        updatePreviews();
    }

    // Carátula real (detrás del disco y las barras): en la mezcla automática cambia con fundido cruzado
    private static final int ALBUM_ART_FADE_MS = 700;
    private String albumArtFadeForPath;   // canción cuya carátula debe entrar con fundido

    private int albumArtFadeToken = 0;    // cada cambio de carátula invalida el fundido anterior

    /**
     * Con carátula: se ve la imagen completa y el disco (dibujo de "sin carátula") se oculta.
     * Sin carátula: se ve el disco. La cápsula de estado queda siempre abajo.
     */
    private void setCoverMode(boolean hasCover) {
        // Igual que la web: con carátula se oculta el disco; la cápsula con el ecualizador queda siempre abajo
        View vinyl = findViewById(R.id.ivVinylDisc);
        if (vinyl != null) vinyl.setVisibility(hasCover ? View.GONE : View.VISIBLE);
    }

    private void showAlbumArt(Bitmap art, boolean smooth) {
        albumArtFadeToken++;
        Drawable previous = ivAlbumArt.getVisibility() == View.VISIBLE ? ivAlbumArt.getDrawable() : null;
        setCoverMode(art != null);
        if (!smooth) {
            ivAlbumArt.setAlpha(1f);
            if (art != null) {
                ivAlbumArt.setVisibility(View.VISIBLE);
                ivAlbumArt.setImageBitmap(art);
            } else {
                ivAlbumArt.setVisibility(View.GONE);
            }
            return;
        }
        if (art != null) {
            final Drawable next = new BitmapDrawable(getResources(), art);
            if (previous != null) {
                // De una carátula a otra: se disuelve una en la otra
                final TransitionDrawable transition = new TransitionDrawable(new Drawable[]{previous, next});
                transition.setCrossFadeEnabled(true);
                ivAlbumArt.setAlpha(1f);
                ivAlbumArt.setImageDrawable(transition);
                transition.startTransition(ALBUM_ART_FADE_MS);
                // Al terminar, dejar solo la nueva (evita encadenar transiciones canción tras canción)
                handler.postDelayed(() -> {
                    if (ivAlbumArt.getDrawable() == transition) ivAlbumArt.setImageDrawable(next);
                }, ALBUM_ART_FADE_MS + 50);
            } else {
                // Sin carátula antes: aparece sobre el fondo del cuadro
                ivAlbumArt.setImageDrawable(next);
                ivAlbumArt.setVisibility(View.VISIBLE);
                fadeViewAlpha(ivAlbumArt, 0f, 1f, null);
            }
        } else if (previous != null) {
            // La nueva no trae carátula: la anterior se desvanece
            fadeViewAlpha(ivAlbumArt, 1f, 0f, () -> {
                ivAlbumArt.setVisibility(View.GONE);
                ivAlbumArt.setAlpha(1f);
            });
        }
    }

    /** Fundido de opacidad con Handler (no Animator), para radios con animaciones desactivadas. */
    private void fadeViewAlpha(final View view, final float from, final float to, final Runnable onEnd) {
        final long start = SystemClock.uptimeMillis();
        final int token = albumArtFadeToken;
        view.setAlpha(from);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (token != albumArtFadeToken) return; // otra carátula ya tomó el control
                float t = Math.min(1f, (SystemClock.uptimeMillis() - start) / (float) ALBUM_ART_FADE_MS);
                view.setAlpha(from + (to - from) * t);
                if (t < 1f) {
                    handler.postDelayed(this, 30);
                } else if (onEnd != null) {
                    onEnd.run();
                }
            }
        });
    }

    // ==========================================
    // AUTOMIX: mezcla automática entre canciones
    // ==========================================
    private static final String PREF_AUTOMIX = "automix_enabled";

    private final AutoMixer.Host autoMixHost = new AutoMixer.Host() {
        @Override
        public MediaPlayer getCurrentPlayer() {
            return mediaPlayer;
        }

        @Override
        public boolean isPlaybackActive() {
            return mediaPlayer != null && isPlaying && !isUserSeeking;
        }

        @Override
        public AutoMixer.NextTrack pickNextTrack() {
            if (isRepeat) return null;
            List<Track> queue = getQueue();
            if (queue.size() < 2 || currentTrackIndex < 0 || currentTrackIndex >= queue.size()) return null;
            int index;
            if (isShuffle) {
                // Aleatorio: se sortea ahora (no al terminar) y nunca la misma canción
                index = new Random().nextInt(queue.size() - 1);
                if (index >= currentTrackIndex) index++;
            } else {
                index = (currentTrackIndex + 1) % queue.size();
            }
            return new AutoMixer.NextTrack(index, queue.get(index));
        }

        @Override
        public boolean isStillNext(AutoMixer.NextTrack next) {
            if (next == null || isRepeat) return false;
            List<Track> queue = getQueue();
            return next.index < queue.size() && queue.get(next.index) == next.track;
        }

        @Override
        public MediaPlayer createPlayer() {
            return MainActivity.this.createPlayer();
        }

        @Override
        public void onMixStarted(MediaPlayer incoming, AutoMixer.NextTrack next, boolean automatic) {
            mediaPlayer = incoming;
            currentTrackIndex = next.index;
            isPlaying = true;
            onTrackStarted(next.track, automatic);
        }

        @Override
        public void onMixVisualChanged(boolean mixing) {
            // Igual que la web: segundo haz láser magenta + cápsula MIXING ⇄ mientras dura la mezcla
            if (laserRing != null) laserRing.setMixing(mixing);
            if (nowPlayingPill != null) nowPlayingPill.setMixing(mixing);
        }
    };

    private void toggleAutoMix() {
        boolean on = !autoMixer.isEnabled();
        autoMixer.setEnabled(on);
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putBoolean(PREF_AUTOMIX, on).apply();
        updateAutoMixButton();
        updatePlaybackStateUI();
        Toast.makeText(this, on
                ? "Automix activado: las canciones se mezclan " + (AutoMixer.DEFAULT_MIX_MS / 1000) + " s"
                : "Automix desactivado", Toast.LENGTH_SHORT).show();
    }

    private void updateAutoMixButton() {
        if (btnAutoMix == null) return;
        boolean on = autoMixer.isEnabled();
        btnAutoMix.setSelected(on);
        btnAutoMix.setColorFilter(Color.parseColor(on ? "#38bdf8" : "#64748b"), PorterDuff.Mode.SRC_IN);
    }

    /**
     * Cola de lo que suena (anterior/siguiente). Es una copia: abrir otra carpeta en Pistas o
     * cambiar de pestaña no altera la cola hasta que se reproduzca algo de la nueva lista.
     */
    private List<Track> getQueue() {
        if (!playQueue.isEmpty()) return playQueue;
        return !currentPlaylist.isEmpty() ? currentPlaylist : displayedTracks;
    }

    private void setPlayQueueFromFolder() {
        playQueue.clear();
        playQueue.addAll(currentPlaylist);
    }

    /**
     * Tras re-escanear (p. ej. al quitar un pendrive): pasar la carpeta activa y la cola a las pistas
     * nuevas, quitar las que ya no existen y detener la reproducción si su pendrive se quitó.
     */
    private void reconcileListsAfterRescan(Map<String, Track> tracksByPath) {
        Track playing = getCurrentTrack();
        // La siguiente precargada puede ser del pendrive que se quitó: se vuelve a elegir
        autoMixer.invalidateNext();

        remapTracks(currentPlaylist, tracksByPath);
        if (currentPlaylist.isEmpty()) {
            currentSelectedFolder = null;
            currentSelectedFolderUsbSource = null;
        }
        remapTracks(playQueue, tracksByPath);

        Track stillPlaying = playing != null ? tracksByPath.get(playing.getFilePath()) : null;
        if (stillPlaying != null) {
            currentTrackIndex = getQueue().indexOf(stillPlaying);
        } else if (playing != null) {
            // La canción que sonaba estaba en el pendrive que se quitó
            autoMixer.onManualPlayback();
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.release();
                } catch (Exception ignored) {}
                mediaPlayer = null;
            }
            isPlaying = false;
            playQueue.clear();
            currentTrackIndex = -1;
            trackAdapter.setCurrentPlayingTrack(null);
            updatePlaybackStateUI();
        }
    }

    // ==========================================
    // FAVORITOS PERSISTENTES
    // ==========================================
    private static final String PREFS_NAME = "jh_music";
    private static final String PREF_FAVORITES = "favorite_paths";

    /** Clave del favorito: ruta relativa al pendrive, para que se conserve aunque el USB cambie de puerto. */
    private static String favoriteKey(Track t) {
        return SearchText.relativeUsbPath(t.getFilePath());
    }

    private Set<String> loadFavoriteKeys() {
        return new HashSet<>(getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getStringSet(PREF_FAVORITES, new HashSet<>()));
    }

    private void saveFavorite(Track t) {
        Set<String> keys = loadFavoriteKeys();
        if (t.isFavorite()) {
            keys.add(favoriteKey(t));
        } else {
            keys.remove(favoriteKey(t));
        }
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putStringSet(PREF_FAVORITES, keys).apply();
    }

    // ==========================================
    // DURACIÓN REAL DE LAS PISTAS EN SEGUNDO PLANO
    // Las que MediaStore no conoce se leen del archivo, primero las de la lista visible
    // ==========================================
    private void showDisplayedTracks() {
        trackAdapter.setTracks(displayedTracks);
        prioritizeDurations(displayedTracks);
    }

    private void startDurationFill(List<Track> tracks) {
        final int generation = ++durationGeneration;
        synchronized (pendingDurations) {
            pendingDurations.clear();
            visibleDurationTracks.clear();
            for (Track t : tracks) {
                if (!t.hasDuration()) pendingDurations.add(t);
            }
            if (pendingDurations.isEmpty()) return;
        }
        durationExecutor.execute(() -> fillDurations(generation));
    }

    /** Pasa al frente de la cola las pistas de la lista que se está mostrando. */
    private void prioritizeDurations(List<Track> visible) {
        synchronized (pendingDurations) {
            visibleDurationTracks.clear();
            for (int i = visible.size() - 1; i >= 0; i--) {
                Track t = visible.get(i);
                if (t.hasDuration()) continue;
                visibleDurationTracks.add(t);
                if (pendingDurations.remove(t)) pendingDurations.addFirst(t);
            }
        }
    }

    private void fillDurations(int generation) {
        long lastRefresh = System.currentTimeMillis();
        boolean visibleChanged = false;
        while (generation == durationGeneration) {
            Track t;
            boolean isVisible;
            synchronized (pendingDurations) {
                t = pendingDurations.pollFirst();
                isVisible = t != null && visibleDurationTracks.contains(t);
            }
            if (t == null) break;
            if (t.hasDuration()) continue;
            readDurationFromFile(t);
            visibleChanged |= isVisible;

            long now = System.currentTimeMillis();
            if (visibleChanged && now - lastRefresh > 500) {
                lastRefresh = now;
                visibleChanged = false;
                // handler.post publica en el hilo de UI los valores escritos aquí
                handler.post(() -> trackAdapter.notifyDataSetChanged());
            }
        }
        if (visibleChanged && generation == durationGeneration) {
            handler.post(() -> trackAdapter.notifyDataSetChanged());
        }
    }

    private void readDurationFromFile(Track t) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(t.getFilePath());
            String durMs = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durMs != null) {
                int sec = (int) (Long.parseLong(durMs) / 1000);
                if (sec > 0) t.setDurationSec(sec);
            }
            String bitrate = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
            if (bitrate != null) {
                int kbps = Math.round(Integer.parseInt(bitrate) / 1000f);
                if (kbps > 0) t.setFormat(getExtension(t.getFilePath()) + " " + kbps + "K");
            }
        } catch (Exception ignored) {
            // Archivo dañado o pendrive retirado: queda como "--:--"
        } finally {
            try {
                mmr.release();
            } catch (Exception ignored) {}
        }
    }

    private static void remapTracks(List<Track> list, Map<String, Track> tracksByPath) {
        List<Track> remapped = new ArrayList<>(list.size());
        for (Track t : list) {
            Track fresh = tracksByPath.get(t.getFilePath());
            if (fresh != null) remapped.add(fresh);
        }
        list.clear();
        list.addAll(remapped);
    }

    private void playTrackFromQueue(List<Track> queue, int index) {
        playTrackFromQueue(queue, index, false);
    }

    private void playTrackFromQueue(List<Track> queue, int index, boolean quickFade) {
        if (queue.isEmpty() || index < 0 || index >= queue.size()) return;
        currentTrackIndex = index;
        startPlayback(queue.get(index), quickFade);
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
            // Pausa en medio de una mezcla: quedarse solo con la canción nueva
            autoMixer.finishMixNow();
            mediaPlayer.pause();
            isPlaying = false;
        } else {
            mediaPlayer.start();
            isPlaying = true;
        }
        updatePlaybackStateUI();
    }

    private void playNextTrack() {
        // Automix: si la siguiente ya está precargada, usarla directo (instantáneo con pendrive lento)
        if (autoMixer.skipToPreparedNext()) return;
        List<Track> queue = getQueue();
        if (queue.isEmpty()) return;
        int nextIndex;
        if (isShuffle) {
            nextIndex = new Random().nextInt(queue.size());
        } else {
            nextIndex = (currentTrackIndex + 1) % queue.size();
        }
        playTrackFromQueue(queue, nextIndex, true);
    }

    private void playPreviousTrack() {
        List<Track> queue = getQueue();
        if (queue.isEmpty()) return;
        int prevIndex = (currentTrackIndex - 1 + queue.size()) % queue.size();
        playTrackFromQueue(queue, prevIndex, true);
    }

    /** 1 toque = siguiente canción; 2 toques (doble touch) = adelanta 10 segundos */
    private void handleNextButtonClick() {
        if (nextTapRunnable != null) {
            navTapHandler.removeCallbacks(nextTapRunnable);
            nextTapRunnable = null;
        }
        nextTapHits++;
        if (nextTapHits == 1) {
            nextTapRunnable = () -> {
                nextTapHits = 0;
                nextTapRunnable = null;
                playNextTrack();
            };
            navTapHandler.postDelayed(nextTapRunnable, DOUBLE_TAP_TIMEOUT_MS);
        } else {
            // Doble toque (o toques adicionales seguidos): avanza 10s
            seekRelative(SEEK_SKIP_MS, nextTapHits - 1);
            navTapHandler.removeCallbacks(resetNextTapHitsRunnable);
            navTapHandler.postDelayed(resetNextTapHitsRunnable, 450);
        }
    }

    /** 1 toque = canción anterior; 2 toques (doble touch) = retrocede 10 segundos */
    private void handlePrevButtonClick() {
        if (prevTapRunnable != null) {
            navTapHandler.removeCallbacks(prevTapRunnable);
            prevTapRunnable = null;
        }
        prevTapHits++;
        if (prevTapHits == 1) {
            prevTapRunnable = () -> {
                prevTapHits = 0;
                prevTapRunnable = null;
                playPreviousTrack();
            };
            navTapHandler.postDelayed(prevTapRunnable, DOUBLE_TAP_TIMEOUT_MS);
        } else {
            // Doble toque (o toques adicionales seguidos): retrocede 10s
            seekRelative(-SEEK_SKIP_MS, prevTapHits - 1);
            navTapHandler.removeCallbacks(resetPrevTapHitsRunnable);
            navTapHandler.postDelayed(resetPrevTapHitsRunnable, 450);
        }
    }

    // Aviso +10s / -10s igual que la web (#seekFeedbackToast): recuadro oscuro con borde cian,
    // centrado en el reproductor al 45% de su alto; aparece con un leve agrandado y se apaga a los 0,8 s.
    // Animado con Handler (no con Animator) por los radios con animaciones desactivadas.
    private static final int SEEK_FEEDBACK_SHOW_MS = 800;
    private static final int SEEK_FEEDBACK_FADE_MS = 250;
    private TextView seekFeedbackView;
    private long seekFeedbackStartMs;
    private boolean seekFeedbackHiding;

    private final Runnable seekFeedbackRunnable = new Runnable() {
        @Override
        public void run() {
            long elapsed = SystemClock.uptimeMillis() - seekFeedbackStartMs;
            float t;
            if (!seekFeedbackHiding) {
                t = Math.min(1f, elapsed / (float) SEEK_FEEDBACK_FADE_MS);
                seekFeedbackView.setAlpha(t);
                float scale = 1f + 0.05f * t;               // crece hasta 1,05
                seekFeedbackView.setScaleX(scale);
                seekFeedbackView.setScaleY(scale);
                if (elapsed >= SEEK_FEEDBACK_SHOW_MS) {
                    seekFeedbackHiding = true;
                    seekFeedbackStartMs = SystemClock.uptimeMillis();
                }
            } else {
                t = Math.min(1f, elapsed / (float) SEEK_FEEDBACK_FADE_MS);
                seekFeedbackView.setAlpha(1f - t);
                float scale = 1.05f - 0.1f * t;              // se achica hasta 0,95
                seekFeedbackView.setScaleX(scale);
                seekFeedbackView.setScaleY(scale);
                if (t >= 1f) {
                    seekFeedbackView.setVisibility(View.GONE);
                    return;
                }
            }
            handler.postDelayed(this, 30);
        }
    };

    private void showSeekFeedback(String text) {
        FrameLayout root = findViewById(android.R.id.content);
        if (root == null) return;
        float density = getResources().getDisplayMetrics().density;
        if (seekFeedbackView == null) {
            seekFeedbackView = new TextView(this);
            seekFeedbackView.setTextColor(Color.rgb(0, 210, 255));
            seekFeedbackView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            seekFeedbackView.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
            seekFeedbackView.setPadding(Math.round(22 * density), Math.round(10 * density),
                    Math.round(22 * density), Math.round(10 * density));
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(14 * density);
            bg.setColor(Color.argb(235, 12, 16, 24));
            bg.setStroke(Math.max(1, Math.round(density)), Color.argb(115, 0, 210, 255));
            seekFeedbackView.setBackground(bg);
            seekFeedbackView.setClickable(false);
            seekFeedbackView.setVisibility(View.GONE);
            root.addView(seekFeedbackView, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        }
        seekFeedbackView.setText(text);
        seekFeedbackView.setVisibility(View.VISIBLE);
        seekFeedbackView.bringToFront();

        // Centrar en el reproductor (como en la web: left 50%, top 45%)
        seekFeedbackView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        View anchor = layoutPlayerPanel != null && layoutPlayerPanel.getWidth() > 0 ? layoutPlayerPanel : root;
        int[] anchorLoc = new int[2];
        int[] rootLoc = new int[2];
        anchor.getLocationInWindow(anchorLoc);
        root.getLocationInWindow(rootLoc);
        float centerX = anchorLoc[0] - rootLoc[0] + anchor.getWidth() / 2f;
        float centerY = anchorLoc[1] - rootLoc[1] + anchor.getHeight() * 0.45f;
        seekFeedbackView.setX(centerX - seekFeedbackView.getMeasuredWidth() / 2f);
        seekFeedbackView.setY(centerY - seekFeedbackView.getMeasuredHeight() / 2f);

        // Reiniciar la animación (toques seguidos actualizan el texto: +10s, +20s...)
        handler.removeCallbacks(seekFeedbackRunnable);
        seekFeedbackHiding = false;
        seekFeedbackStartMs = SystemClock.uptimeMillis() - (seekFeedbackView.getAlpha() > 0.5f ? SEEK_FEEDBACK_FADE_MS : 0);
        handler.post(seekFeedbackRunnable);
    }

    /** Adelanta o retrocede deltaMs milisegundos con feedback visual */
    private void seekRelative(int deltaMs, int consecutiveHits) {
        if (mediaPlayer == null) return;
        try {
            autoMixer.finishMixNow();
            int current = mediaPlayer.getCurrentPosition();
            int duration = mediaPlayer.getDuration();
            int target = current + deltaMs;
            if (target < 0) target = 0;
            if (duration > 0 && target > duration) target = Math.max(0, duration - 1000);
            mediaPlayer.seekTo(target);

            // Reflejar cambio inmediato en barra de progreso y reloj
            if (duration > 0) {
                int progress = (int) (((float) target / duration) * 100);
                seekTrackProgress.setProgress(progress);
                tvTimeCurrent.setText(formatMs(target));
            }

            // Aviso breve centrado en el reproductor (solo texto: +10s / -10s)
            int totalSec = (deltaMs > 0 ? 10 : -10) * Math.max(1, consecutiveHits);
            showSeekFeedback(totalSec > 0 ? ("+" + totalSec + "s") : (totalSec + "s"));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                // En una mezcla automática la carátula cambia con fundido cruzado
                boolean smooth = filePath != null && filePath.equals(albumArtFadeForPath);
                if (smooth) albumArtFadeForPath = null;
                showAlbumArt(finalArt, smooth);
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
            Map<String, Integer> mediaStoreDurations = new HashMap<>();
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
                        // Pista ya encontrada recorriendo carpetas: solo aprovechar su duración real
                        if (path != null && seenPaths.contains(path)) {
                            long durMs = cursor.getLong(durCol);
                            if (durMs > 0) mediaStoreDurations.put(path, (int) (durMs / 1000));
                            continue;
                        }
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

            // Duración real (y bitrate estimado con ella) para las pistas que MediaStore ya conoce
            for (Track t : foundTracks) {
                Integer durSec = t.hasDuration() ? null : mediaStoreDurations.get(t.getFilePath());
                if (durSec != null) {
                    t.setDurationSec(durSec);
                    File f = new File(t.getFilePath());
                    t.setFormat(buildFormatBadge(f.getName(), f.length(), durSec));
                }
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

            // Precalcular aquí (hilo de escaneo) el texto normalizado del buscador, para que cada búsqueda solo compare
            for (Track t : foundTracks) t.getSearchKey();
            for (FolderItem f : generatedFolders) f.getSearchKey();

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
            if (!fUsb1) fUsb1 = isUsbMounted(USB1_MOUNT_PATHS);
            if (!fUsb2) fUsb2 = isUsbMounted(USB2_MOUNT_PATHS);
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

                // Marcar los favoritos guardados en el radio
                Set<String> favoriteKeys = loadFavoriteKeys();
                Map<String, Track> tracksByPath = new HashMap<>();
                for (Track t : foundTracks) {
                    if (favoriteKeys.contains(favoriteKey(t))) t.setFavorite(true);
                    tracksByPath.put(t.getFilePath(), t);
                }

                allTracks.clear();
                allTracks.addAll(foundTracks);

                folderList.clear();
                folderList.addAll(generatedFolders);

                reconcileListsAfterRescan(tracksByPath);
                startDurationFill(allTracks);

                buildTreeFromTracks(allTracks);
                updateVisibleTreeNodes();

                tvStorageStatus.setText(allTracks.isEmpty() ? "No se encontraron canciones" : "Pendrives listos (" + folderList.size() + " carpetas, " + allTracks.size() + " canciones)");
                // Pistas solo cuenta la carpeta elegida (0 hasta que se elija una)
                updateTracksTabCount();

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
                        // Duración 0 = desconocida: se toma de MediaStore o se lee del archivo en segundo plano
                        String format = buildFormatBadge(file.getName(), file.length(), 0);
                        tracks.add(new Track(path, title, artist, folder, folder, file.getParent(), format, 0, usbTag));
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
        handler.removeCallbacks(usbRescanRunnable);
        handler.removeCallbacks(seekFeedbackRunnable);
        try {
            unregisterReceiver(usbMountReceiver);
        } catch (Exception ignored) {}
        durationGeneration++;
        durationExecutor.shutdownNow();
        autoMixer.release();
        audioAnalyzer.shutdown();
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
