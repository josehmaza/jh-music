package com.jh.caraudio;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FolderTreeAdapter extends BaseAdapter {

    public interface OnTreeNodeClickListener {
        void onNodeClick(TreeNode node, int position);
        void onNodePlayClick(TreeNode node, int position);

        /** Pulsación larga sobre la fila: menú contextual (agregar la carpeta a Mi Mix). */
        void onNodeLongClick(TreeNode node, int position);
    }

    private Context context;
    private List<TreeNode> visibleNodes;
    private OnTreeNodeClickListener listener;
    private Track currentPlayingTrack;
    private MusicThemePalette palette;
    /** Si lo que suena está sonando (true) o en pausa: el ecualizador de su carpeta se mueve o queda quieto. */
    private boolean playbackRunning;
    private String selectedFolder;

    // Destello al tocar una fila (para saber que el toque entró, como en anterior / siguiente)
    private static final int FLASH_MS = 250;
    private TreeNode flashNode;
    /** Carpeta tocada por última vez: su nombre se muestra completo, en hasta 2 renglones. */
    private TreeNode fullTitleNode;
    private final Handler flashHandler = new Handler(Looper.getMainLooper());
    private final Runnable clearFlash = () -> {
        flashNode = null;
        notifyDataSetChanged();
    };

    public FolderTreeAdapter(Context context, List<TreeNode> visibleNodes, OnTreeNodeClickListener listener) {
        this.context = context;
        this.visibleNodes = visibleNodes != null ? visibleNodes : new ArrayList<>();
        this.listener = listener;
    }

    public void setThemePalette(MusicThemePalette p) {
        this.palette = p;
        notifyDataSetChanged();
    }

    public void setVisibleNodes(List<TreeNode> nodes) {
        this.visibleNodes = nodes != null ? nodes : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setPlaybackRunning(boolean running) {
        if (playbackRunning == running) return;
        playbackRunning = running;
        notifyDataSetChanged();
    }

    public void setCurrentPlayingTrack(Track track) {
        this.currentPlayingTrack = track;
        notifyDataSetChanged();
    }

    public void setSelectedFolder(String folder) {
        this.selectedFolder = folder;
        notifyDataSetChanged();
    }

    /** La fila de esta carpeta se enciende en cian un instante. */
    public void flash(TreeNode node) {
        flashNode = node;
        fullTitleNode = node;
        flashHandler.removeCallbacks(clearFlash);
        notifyDataSetChanged();
        flashHandler.postDelayed(clearFlash, FLASH_MS);
    }

    @Override
    public int getCount() {
        return visibleNodes.size();
    }

    @Override
    public TreeNode getItem(int position) {
        if (position >= 0 && position < visibleNodes.size()) {
            return visibleNodes.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /** ¿Es la carpeta de la canción que suena? */
    private boolean isPlayingFolder(TreeNode node) {
        return currentPlayingTrack != null && !node.isUsbRoot() && node.getName() != null
                && node.getName().equalsIgnoreCase(currentPlayingTrack.getFolder());
    }

    private static class ViewHolder {
        LinearLayout layoutTreeItem;
        View viewIndent;
        ImageView ivTreeArrow;
        FrameLayout frameTreeIcon;
        ImageView ivTreeIcon;
        CarMarqueeTextView tvTreeTitle;
        View btnFolderPlay;
        View frameFolderPlayCircle;
        MiniEqualizerView eqFolderPlaying;
        TextView tvTreeDirectCount;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_tree_folder, parent, false);
            holder = new ViewHolder();
            holder.layoutTreeItem = convertView.findViewById(R.id.layoutTreeItem);
            holder.viewIndent = convertView.findViewById(R.id.viewIndent);
            holder.ivTreeArrow = convertView.findViewById(R.id.ivTreeArrow);
            holder.frameTreeIcon = convertView.findViewById(R.id.frameTreeIcon);
            holder.ivTreeIcon = convertView.findViewById(R.id.ivTreeIcon);
            holder.tvTreeTitle = convertView.findViewById(R.id.tvTreeTitle);
            holder.btnFolderPlay = convertView.findViewById(R.id.btnFolderPlay);
            holder.frameFolderPlayCircle = convertView.findViewById(R.id.frameFolderPlayCircle);
            holder.eqFolderPlaying = convertView.findViewById(R.id.eqFolderPlaying);
            holder.tvTreeDirectCount = convertView.findViewById(R.id.tvTreeDirectCount);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TreeNode node = getItem(position);
        if (node != null) {
            float density = context.getResources().getDisplayMetrics().density;

            // Indentación según el nivel de profundidad (poca, para que el nombre tenga lugar en 1024x600)
            int indentPx = (int) ((2 + node.getLevel() * 12) * density);
            ViewGroup.LayoutParams lp = holder.viewIndent.getLayoutParams();
            if (lp != null) {
                lp.width = indentPx;
                holder.viewIndent.setLayoutParams(lp);
            }

            // Flecha de despliegue (> o v)
            if (node.hasChildren()) {
                holder.ivTreeArrow.setVisibility(View.VISIBLE);
                if (node.isExpanded()) {
                    holder.ivTreeArrow.setImageResource(R.drawable.ic_expand_more);
                } else {
                    holder.ivTreeArrow.setImageResource(R.drawable.ic_chevron_right);
                }
            } else {
                // Si no tiene subcarpetas, ocultar flecha pero mantener el espaciado
                holder.ivTreeArrow.setVisibility(View.INVISIBLE);
            }

            // Icono fijo (no se desliza con el nombre): la raíz del pendrive en su cuadro verde de 44dp;
            // las carpetas, solo el dibujo cian de 20dp, del alto del texto
            boolean usbRoot = node.isUsbRoot();
            ViewGroup.LayoutParams frameLp = holder.frameTreeIcon.getLayoutParams();
            frameLp.width = frameLp.height = (int) ((usbRoot ? 44 : 20) * density);
            holder.frameTreeIcon.setLayoutParams(frameLp);
            ViewGroup.LayoutParams iconLp = holder.ivTreeIcon.getLayoutParams();
            iconLp.width = iconLp.height = (int) ((usbRoot ? 26 : 20) * density);
            holder.ivTreeIcon.setLayoutParams(iconLp);
            if (usbRoot) {
                if (palette != null) {
                    holder.frameTreeIcon.setBackground(palette.box(palette.emeraldSoft, palette.emeraldBorder, 10f));
                    holder.ivTreeIcon.setImageResource(R.drawable.ic_usb_trident);
                    holder.ivTreeIcon.setColorFilter(palette.emerald, PorterDuff.Mode.SRC_IN);
                } else {
                    holder.frameTreeIcon.setBackgroundResource(R.drawable.bg_tree_icon_usb);
                    holder.ivTreeIcon.setImageResource(R.drawable.ic_usb_trident);
                    holder.ivTreeIcon.setColorFilter(Color.parseColor("#10b981"), PorterDuff.Mode.SRC_IN);
                }
            } else {
                holder.frameTreeIcon.setBackground(null);
                holder.ivTreeIcon.setImageResource(R.drawable.ic_tree_folder);
                holder.ivTreeIcon.setColorFilter(palette != null ? palette.accent : Color.parseColor("#38bdf8"), PorterDuff.Mode.SRC_IN);
            }
            if (palette != null) {
                holder.ivTreeArrow.setColorFilter(palette.text2, PorterDuff.Mode.SRC_IN);
                holder.tvTreeTitle.setTextColor(palette.text1);
            }
            holder.tvTreeTitle.setText(node.getName());
            // Nombre en 1 renglón que se desliza de derecha a izquierda si no entra; la carpeta
            // tocada por última vez lo muestra completo, en hasta 2 renglones
            if (node == fullTitleNode) {
                holder.tvTreeTitle.setCarMarqueeEnabled(false);
                holder.tvTreeTitle.setSingleLine(false);
                holder.tvTreeTitle.setMaxLines(2);
                holder.tvTreeTitle.setEllipsize(TextUtils.TruncateAt.END);
            } else {
                // Marquesina propia en vez de la nativa (que repinta la pantalla en cada VSYNC)
                holder.tvTreeTitle.setSingleLine(true);
                holder.tvTreeTitle.setCarMarqueeEnabled(true);
            }

            int count = node.getDirectTrackCount();

            // Cantidad de canciones directas (sin las subcarpetas) y botón ▶ (solo si tiene canciones directas)
            if (count > 0) {
                holder.tvTreeDirectCount.setVisibility(View.VISIBLE);
                holder.tvTreeDirectCount.setText(count + (count == 1 ? " canción" : " canciones"));
                holder.btnFolderPlay.setVisibility(View.VISIBLE);
                holder.btnFolderPlay.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onNodePlayClick(node, position);
                    }
                });
            } else {
                holder.tvTreeDirectCount.setVisibility(View.GONE);
                holder.btnFolderPlay.setVisibility(View.GONE);
                holder.btnFolderPlay.setOnClickListener(null);
            }

            boolean isPlayingFolder = isPlayingFolder(node);

            // La carpeta que suena: su botón ▶ muestra un ecualizador (se mueve mientras suena, quieto en pausa)
            holder.frameFolderPlayCircle.setVisibility(isPlayingFolder ? View.GONE : View.VISIBLE);
            holder.eqFolderPlaying.setVisibility(isPlayingFolder ? View.VISIBLE : View.GONE);
            holder.eqFolderPlaying.setAnimating(isPlayingFolder && playbackRunning);
            boolean isSelected = (selectedFolder != null && selectedFolder.equalsIgnoreCase(node.getName()));

            if (palette != null) {
                if (node == flashNode || isPlayingFolder) {
                    holder.layoutTreeItem.setBackground(palette.box(palette.accentSoft, palette.accentBorder, 12f));
                } else if (isSelected) {
                    holder.layoutTreeItem.setBackground(palette.box(palette.cardBg, palette.accentBorder, 12f));
                } else {
                    holder.layoutTreeItem.setBackground(palette.box(palette.cardBg, palette.borderSoft, 12f));
                }
                holder.tvTreeDirectCount.setTextColor(palette.text2);
                if (palette.light) {
                    holder.btnFolderPlay.setBackground(isPlayingFolder
                            ? palette.box(palette.accent, palette.accent, 12f)
                            : palette.box(palette.surface2, palette.border, 12f));
                } else {
                    holder.btnFolderPlay.setBackground(palette.box(palette.accent, palette.accent, 12f));
                }
            } else {
                if (node == flashNode) {
                    holder.layoutTreeItem.setBackgroundResource(R.drawable.bg_tree_node_flash);
                } else if (isSelected && !isPlayingFolder) {
                    holder.layoutTreeItem.setBackgroundResource(R.drawable.bg_tree_node_open);
                } else {
                    holder.layoutTreeItem.setBackgroundResource(R.drawable.bg_tree_node);
                }
            }

            // Pulsación larga (en la fila, la flecha o el ícono): menú contextual de la carpeta. Solo las que
            // tienen canciones directas, que son las que se pueden agregar a Mi Mix
            View.OnLongClickListener longClick = count > 0 ? v -> {
                if (listener != null) listener.onNodeLongClick(node, position);
                return true;
            } : null;
            holder.ivTreeArrow.setOnLongClickListener(longClick);
            holder.frameTreeIcon.setOnLongClickListener(longClick);
            convertView.setOnLongClickListener(longClick);

            // Clic en la flecha de despliegue
            holder.ivTreeArrow.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNodeClick(node, position);
                }
            });

            // Clic en el icono
            holder.frameTreeIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNodeClick(node, position);
                }
            });

            // Clic en la fila (expandir / contraer y seleccionar)
            convertView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNodeClick(node, position);
                }
            });
        }

        return convertView;
    }
}
