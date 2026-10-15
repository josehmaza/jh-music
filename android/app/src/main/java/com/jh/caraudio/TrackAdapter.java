package com.jh.caraudio;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TrackAdapter extends BaseAdapter {

    public interface OnTrackClickListener {
        void onTrackClick(Track track, int position);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Track track, int position);
    }

    private Context context;
    private List<Track> tracks;
    private int currentPlayingIndex = -1;
    private int selectedMarqueeIndex = -1;
    private long lastClickTime = 0;
    private int lastClickPosition = -1;
    private static final long DOUBLE_CLICK_TIME_DELTA = 450; // ms para detectar doble toque táctil
    private OnTrackClickListener trackClickListener;
    private OnFavoriteClickListener favoriteClickListener;

    public TrackAdapter(Context context, List<Track> tracks, OnTrackClickListener trackListener, OnFavoriteClickListener favListener) {
        this.context = context;
        this.tracks = tracks != null ? tracks : new ArrayList<>();
        this.trackClickListener = trackListener;
        this.favoriteClickListener = favListener;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks != null ? tracks : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setCurrentPlayingIndex(int index) {
        this.currentPlayingIndex = index;
        this.selectedMarqueeIndex = index;
        notifyDataSetChanged();
    }

    public void setSelectedMarqueeIndex(int index) {
        this.selectedMarqueeIndex = index;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return tracks.size();
    }

    @Override
    public Track getItem(int position) {
        if (position >= 0 && position < tracks.size()) {
            return tracks.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        TextView tvIndex;
        TextView tvTitle;
        TextView tvArtist;
        TextView tvUsbBadge;
        TextView tvDuration;
        ImageButton btnFav;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_track, parent, false);
            holder = new ViewHolder();
            holder.tvIndex = convertView.findViewById(R.id.tvItemIndex);
            holder.tvTitle = convertView.findViewById(R.id.tvItemTitle);
            holder.tvArtist = convertView.findViewById(R.id.tvItemArtist);
            holder.tvUsbBadge = convertView.findViewById(R.id.tvItemUsbBadge);
            holder.tvDuration = convertView.findViewById(R.id.tvItemDuration);
            holder.btnFav = convertView.findViewById(R.id.btnItemFav);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Track track = getItem(position);
        if (track != null) {
            boolean isPlaying = (position == currentPlayingIndex);
            boolean isSelectedForMarquee = (position == selectedMarqueeIndex) || isPlaying;

            if (isPlaying) {
                holder.tvIndex.setText("▶");
                holder.tvIndex.setTextColor(Color.parseColor("#38bdf8"));
                holder.tvTitle.setTextColor(Color.parseColor("#38bdf8"));
                convertView.setBackgroundColor(Color.parseColor("#172030"));
            } else {
                holder.tvIndex.setText(String.format("%02d", position + 1));
                holder.tvIndex.setTextColor(Color.parseColor("#94a3b8"));
                holder.tvTitle.setTextColor(isSelectedForMarquee ? Color.parseColor("#38bdf8") : Color.parseColor("#f1f5f9"));
                convertView.setBackgroundColor(isSelectedForMarquee ? Color.parseColor("#131a26") : Color.TRANSPARENT);
            }

            holder.tvTitle.setText(track.getTitle());
            if (position == selectedMarqueeIndex) {
                // Modo expandido: texto completo sin recortar
                holder.tvTitle.setSingleLine(false);
                holder.tvTitle.setMaxLines(4);
                holder.tvTitle.setEllipsize(null);
                holder.tvTitle.setSelected(false);
                holder.tvTitle.setTextColor(Color.parseColor("#38bdf8"));
                convertView.setBackgroundColor(Color.parseColor("#1c2536"));
            } else {
                // Modo normal de 1 línea con marquesina si está reproduciéndose
                holder.tvTitle.setSingleLine(true);
                holder.tvTitle.setMaxLines(1);
                holder.tvTitle.setEllipsize(android.text.TextUtils.TruncateAt.MARQUEE);
                holder.tvTitle.setSelected(isPlaying);
                holder.tvTitle.setTextColor(isPlaying ? Color.parseColor("#38bdf8") : Color.parseColor("#f1f5f9"));
                convertView.setBackgroundColor(isPlaying ? Color.parseColor("#172030") : Color.TRANSPARENT);
            }

            holder.tvArtist.setText(track.getArtist() + " • " + track.getAlbum());

            // Badge de almacenamiento USB 1 / USB 2 / INTERNA
            String source = track.getUsbSource();
            String path = track.getFilePath();
            if (source == null && path != null) {
                if (path.contains("udisk1")) source = "USB 1";
                else if (path.contains("udisk2")) source = "USB 2";
                else source = "INTERNA";
            }
            if ("USB 2".equalsIgnoreCase(source) || (source != null && source.contains("2"))) {
                holder.tvUsbBadge.setText("USB 2");
                holder.tvUsbBadge.setBackgroundResource(R.drawable.bg_pill_usb2);
                holder.tvUsbBadge.setTextColor(Color.parseColor("#c084fc"));
            } else if ("INTERNA".equalsIgnoreCase(source) || (path != null && (path.contains("sdcard") || path.contains("emulated")))) {
                holder.tvUsbBadge.setText("INTERNA");
                holder.tvUsbBadge.setBackgroundResource(R.drawable.bg_pill_internal);
                holder.tvUsbBadge.setTextColor(Color.parseColor("#fbbf24"));
            } else {
                holder.tvUsbBadge.setText("USB 1");
                holder.tvUsbBadge.setBackgroundResource(R.drawable.bg_pill_usb1);
                holder.tvUsbBadge.setTextColor(Color.parseColor("#38bdf8"));
            }

            holder.tvDuration.setText(track.getFormattedDuration());

            if (track.isFavorite()) {
                holder.btnFav.setImageResource(android.R.drawable.star_on);
            } else {
                holder.btnFav.setImageResource(android.R.drawable.star_off);
            }

            holder.btnFav.setOnClickListener(v -> {
                if (favoriteClickListener != null) {
                    favoriteClickListener.onFavoriteClick(track, position);
                    notifyDataSetChanged();
                }
            });

            // 1. Click en el icono de la izquierda (tvIndex): REPRODUCIR DE INMEDIATO (1 toque)
            holder.tvIndex.setOnClickListener(v -> {
                if (trackClickListener != null) {
                    trackClickListener.onTrackClick(track, position);
                }
            });

            // 2. Click en la fila o en el texto:
            // 1 toque: Muestra el nombre completo (expandir / contraer)
            // 2 toques: Reproduce la canción
            View.OnClickListener rowClickListener = v -> {
                long clickTime = System.currentTimeMillis();
                if (position == lastClickPosition && (clickTime - lastClickTime) < DOUBLE_CLICK_TIME_DELTA) {
                    // Doble toque detectado: REPRODUCIR LA CANCIÓN
                    lastClickTime = 0;
                    lastClickPosition = -1;
                    if (trackClickListener != null) {
                        trackClickListener.onTrackClick(track, position);
                    }
                } else {
                    // Primer toque (o toques espaciados): Expandir/Colapsar para leer el nombre completo
                    lastClickTime = clickTime;
                    lastClickPosition = position;
                    if (selectedMarqueeIndex == position) {
                        selectedMarqueeIndex = -1; // Toggle para contraer
                    } else {
                        selectedMarqueeIndex = position; // Expandir para leer todo
                    }
                    notifyDataSetChanged();
                }
            };

            convertView.setOnClickListener(rowClickListener);
            holder.tvTitle.setOnClickListener(rowClickListener);
        }

        return convertView;
    }
}
