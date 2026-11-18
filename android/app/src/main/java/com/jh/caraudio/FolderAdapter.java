package com.jh.caraudio;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FolderAdapter extends BaseAdapter {

    public interface OnFolderClickListener {
        void onFolderClick(FolderItem folder, int position);
    }

    /** + : agregar la carpeta entera a Mi Mix. */
    public interface OnFolderAddMixListener {
        void onFolderAddMix(FolderItem folder, int position);
    }

    private Context context;
    private List<FolderItem> folders;
    private OnFolderClickListener folderClickListener;
    private OnFolderAddMixListener addMixListener;
    private MusicThemePalette palette;

    public void setThemePalette(MusicThemePalette palette) {
        this.palette = palette;
        notifyDataSetChanged();
    }

    public FolderAdapter(Context context, List<FolderItem> folders, OnFolderClickListener listener,
                         OnFolderAddMixListener addMixListener) {
        this.context = context;
        this.folders = folders != null ? folders : new ArrayList<>();
        this.folderClickListener = listener;
        this.addMixListener = addMixListener;
    }

    public void setFolders(List<FolderItem> folders) {
        this.folders = folders != null ? folders : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return folders.size();
    }

    @Override
    public FolderItem getItem(int position) {
        if (position >= 0 && position < folders.size()) {
            return folders.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvPath;
        TextView tvUsbBadge;
        TextView tvTrackCount;
        TextView tvChevron;
        ImageButton btnAddMix;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_folder, parent, false);
            holder = new ViewHolder();
            holder.ivIcon = convertView.findViewById(R.id.ivFolderIcon);
            holder.tvName = convertView.findViewById(R.id.tvFolderName);
            holder.tvPath = convertView.findViewById(R.id.tvFolderPath);
            holder.tvUsbBadge = convertView.findViewById(R.id.tvFolderUsbBadge);
            holder.tvTrackCount = convertView.findViewById(R.id.tvFolderTrackCount);
            holder.tvChevron = convertView.findViewById(R.id.tvFolderChevron);
            holder.btnAddMix = convertView.findViewById(R.id.btnFolderAddMix);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FolderItem folder = getItem(position);
        if (folder != null) {
            boolean isLight = palette != null && palette.light;
            holder.tvName.setText(folder.getName());
            holder.tvName.setTextColor(palette != null ? palette.text1 : Color.parseColor("#f1f5f9"));
            holder.tvPath.setTextColor(palette != null ? palette.text2 : Color.parseColor("#94a3b8"));
            holder.tvTrackCount.setTextColor(palette != null ? palette.text2 : Color.parseColor("#94a3b8"));
            if (holder.tvChevron != null) {
                holder.tvChevron.setTextColor(palette != null ? palette.textMuted : Color.parseColor("#64748b"));
            }
            if (holder.ivIcon != null) {
                if (isLight) {
                    holder.ivIcon.setBackground(palette.box(palette.accentSoft, palette.accentBorder, 10f));
                    holder.ivIcon.setColorFilter(palette.accent, PorterDuff.Mode.SRC_IN);
                } else {
                    holder.ivIcon.setBackgroundResource(R.drawable.bg_btn_action);
                    holder.ivIcon.clearColorFilter();
                }
            }

            // Formatear ruta sin prefijo redundante en USB 1 y USB 2
            String path = folder.getPath();
            String badgeText = folder.getUsbSource();
            if (path != null) {
                if (path.startsWith("/storage/udisk1")) {
                    path = path.replace("/storage/udisk1", "");
                    badgeText = "USB 1";
                } else if (path.startsWith("/storage/udisk2")) {
                    path = path.replace("/storage/udisk2", "");
                    badgeText = "USB 2";
                } else if (path.contains("sdcard") || path.contains("emulated")) {
                    badgeText = "INTERNA";
                }
                if (!path.startsWith("/")) path = "/" + path;
                if (path.length() > 1 && path.endsWith("/")) path = path.substring(0, path.length() - 1);
            }
            holder.tvPath.setText(path);

            // Estilos del Badge
            if ("USB 2".equalsIgnoreCase(badgeText) || "udisk2".equalsIgnoreCase(badgeText) || (badgeText != null && badgeText.contains("2"))) {
                int color = isLight ? 0xFF7C3AED : Color.parseColor("#c084fc");
                holder.tvUsbBadge.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_usb_trident, 0, 0, 0);
                holder.tvUsbBadge.setCompoundDrawablePadding((int) (2 * context.getResources().getDisplayMetrics().density));
                holder.tvUsbBadge.setText("2");
                if (palette != null) holder.tvUsbBadge.setBackground(palette.box(isLight ? 0x1A7C3AED : 0x24C084FC, 0, 6f));
                else holder.tvUsbBadge.setBackgroundResource(R.drawable.bg_pill_usb2);
                holder.tvUsbBadge.setTextColor(color);
                Drawable[] d = holder.tvUsbBadge.getCompoundDrawables();
                if (d.length > 0 && d[0] != null) d[0].mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            } else {
                int color = palette != null ? palette.accent : Color.parseColor("#38bdf8");
                holder.tvUsbBadge.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_usb_trident, 0, 0, 0);
                holder.tvUsbBadge.setCompoundDrawablePadding((int) (2 * context.getResources().getDisplayMetrics().density));
                holder.tvUsbBadge.setText("1");
                if (palette != null) holder.tvUsbBadge.setBackground(palette.box(palette.accentSoft, 0, 6f));
                else holder.tvUsbBadge.setBackgroundResource(R.drawable.bg_pill_usb1);
                holder.tvUsbBadge.setTextColor(color);
                Drawable[] d = holder.tvUsbBadge.getCompoundDrawables();
                if (d.length > 0 && d[0] != null) d[0].mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }

            // Solo número limpio sin la palabra "pistas"
            holder.tvTrackCount.setText(String.valueOf(folder.getTrackCount()));

            holder.btnAddMix.setVisibility(folder.getTrackCount() > 0 ? View.VISIBLE : View.GONE);
            if (isLight) {
                holder.btnAddMix.setBackground(palette.box(palette.surface2, palette.border, 10f));
                holder.btnAddMix.setColorFilter(palette.text2, PorterDuff.Mode.SRC_IN);
            } else {
                holder.btnAddMix.setBackgroundResource(R.drawable.bg_folder_addmix);
                holder.btnAddMix.clearColorFilter();
            }
            holder.btnAddMix.setOnClickListener(v -> {
                if (addMixListener != null) {
                    addMixListener.onFolderAddMix(folder, position);
                }
            });

            // Click directo en la fila
            convertView.setOnClickListener(v -> {
                if (folderClickListener != null) {
                    folderClickListener.onFolderClick(folder, position);
                }
            });
        }

        return convertView;
    }
}
