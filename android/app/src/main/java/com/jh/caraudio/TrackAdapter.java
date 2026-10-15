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

            if (isPlaying) {
                holder.tvIndex.setText("▶");
                holder.tvIndex.setTextColor(Color.parseColor("#38bdf8"));
                holder.tvTitle.setTextColor(Color.parseColor("#38bdf8"));
                convertView.setBackgroundColor(Color.parseColor("#172030"));
            } else {
                holder.tvIndex.setText(String.format("%02d", position + 1));
                holder.tvIndex.setTextColor(Color.parseColor("#64748b"));
                holder.tvTitle.setTextColor(Color.parseColor("#f1f5f9"));
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }

            holder.tvTitle.setText(track.getTitle());
            // Activar marquesina continua en la canción que está sonando
            holder.tvTitle.setSelected(isPlaying);

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

            // Click directo en la fila de la canción -> Reproducir de inmediato
            convertView.setOnClickListener(v -> {
                if (trackClickListener != null) {
                    trackClickListener.onTrackClick(track, position);
                }
            });
        }

        return convertView;
    }
}
