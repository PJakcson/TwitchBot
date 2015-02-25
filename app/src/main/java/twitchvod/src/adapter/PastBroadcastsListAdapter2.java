package twitchvod.src.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import twitchvod.src.R;
import twitchvod.src.data.TwitchNetworkTasks;
import twitchvod.src.data.primitives.TwitchVideo;
import twitchvod.src.ui_fragments.ChannelDetailFragment;

public class PastBroadcastsListAdapter2 extends BaseAdapter {
    private final static int IS_HEADER = 0;
    private final static int IS_HIGHLIGHT_HEADER = 1;
    private final static int IS_HIGHLIGHT = 2;
    private final static int IS_BROADCAST_HEADER = 3;
    private final static int IS_BROADCAST = 4;

    private String mHighlightHeader = "Highlights", mBroadcastHeader = "Broadcasts";

    private Activity mActivity;
    private ChannelDetailFragment mFragment;
    private ArrayList<TwitchVideo> mHighlights;
    private ArrayList<TwitchVideo> mBroadcasts;
    private LayoutInflater mInflater;
    private int mWidth;
    private RelativeLayout.LayoutParams mRelativeLayout;

    public PastBroadcastsListAdapter2(ChannelDetailFragment c) {
        mActivity = c.getActivity();
        mFragment = c;
        mHighlights = new ArrayList<>();
        mBroadcasts = new ArrayList<>();
        mInflater = LayoutInflater.from(c.getActivity());
    }

    public void updateHighlights(ArrayList <TwitchVideo> c) {
        if (c == null) return;
        if (mHighlights == null) mHighlights = new ArrayList<>();
        mHighlights.addAll(c);
        notifyDataSetChanged();
    }

    public void updateBroadcasts(ArrayList <TwitchVideo> c) {
        if (c == null) return;
        if (mHighlights == null) mBroadcasts = new ArrayList<>();
        mBroadcasts.addAll(c);
        notifyDataSetChanged();
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.broadcast_row_layout, parent, false);
            holder = new ViewHolder();
            holder.firstLine = (TextView) convertView.findViewById(R.id.firstLine);
            holder.secondLine = (TextView) convertView.findViewById(R.id.secondLine);
            holder.secondLineViewers = (TextView) convertView.findViewById(R.id.secondLineViewers);
            holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (parent.getMeasuredWidth() != mWidth && holder.imageView.getDrawable() != null) {
            mWidth = parent.getMeasuredWidth();
            float scale = 1.0f * holder.imageView.getDrawable().getIntrinsicHeight()/holder.imageView.getDrawable().getIntrinsicWidth();
            int imageWidth = Math.round(mWidth);
            int imageHeight = Math.round(imageWidth * scale);
            mRelativeLayout = new RelativeLayout.LayoutParams(imageWidth, imageHeight);
        }

        int j = getGroup(position+1);
        int i = mHighlights.isEmpty() ? 0 : 1;
        if (j == IS_HIGHLIGHT_HEADER) {
            View highlights = mInflater.inflate(R.layout.channel_video_footer, null);
            ((TextView)highlights.findViewById(R.id.textView)).setText(mHighlightHeader);
            if (mHighlights.isEmpty()) highlights.setVisibility(View.INVISIBLE);
            return highlights;
        } else if (j == IS_HIGHLIGHT) {
            int index = getChildPosition(position, j)+1;
            holder.firstLine.setText(mHighlights.get(index).mTitle);
            holder.secondLine.setText(mHighlights.get(index).timeAgo());
            holder.secondLineViewers.setText(mHighlights.get(index).mViews);

            if (mHighlights.get(index).mPreview == null)
                loadImage(0, index, mHighlights.get(index).mPreviewLink, holder.imageView);
            else
                holder.imageView.setImageBitmap(mHighlights.get(index).mPreview);

        } else if (j == IS_BROADCAST_HEADER) {
            View broadcasts = mInflater.inflate(R.layout.channel_video_footer, null);
            ((TextView)broadcasts.findViewById(R.id.textView)).setText(mBroadcastHeader);
            if (mBroadcasts.isEmpty()) broadcasts.setVisibility(View.INVISIBLE);
            return broadcasts;

        } else if (j == IS_BROADCAST) {
            int broadPos = getChildPosition(position, j)+1;
            holder.firstLine.setText(mBroadcasts.get(broadPos).mTitle);
            holder.secondLine.setText(mBroadcasts.get(broadPos).timeAgo());
            holder.secondLineViewers.setText(mBroadcasts.get(broadPos).mViews);
            if (mBroadcasts.get(broadPos).mPreview == null)
                loadImage(1, broadPos, mBroadcasts.get(broadPos).mPreviewLink, holder.imageView);
            else
                holder.imageView.setImageBitmap(mBroadcasts.get(broadPos).mPreview);
        }

        return convertView;
    }

    public int getCount() {
        int count = 0;
        if (!mHighlights.isEmpty()) count += mHighlights.size() + 1;
        if (!mBroadcasts.isEmpty()) count += mBroadcasts.size() + 1;
        return count;
    }

    public TwitchVideo getHighlight(int position) {
        return mHighlights.get(position);
    }

    public TwitchVideo getBroadcast(int position) {
        return mBroadcasts.get(position);
    }

    public TwitchVideo getItem(int position) {
        if (position < mHighlights.size() && position != 0) return mHighlights.get(position - 1);
        if (position > mHighlights.size() && position != 0) return mBroadcasts.get(position - mHighlights.size());
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public int getGroup(int position) {
        if (position == 0) return IS_HEADER;
        if (!mHighlights.isEmpty() && position == 1) return IS_HIGHLIGHT_HEADER;
        if (!mHighlights.isEmpty() && position <= mHighlights.size() + 1) return IS_HIGHLIGHT;
        if (!mHighlights.isEmpty() && position == mHighlights.size() + 2) return IS_BROADCAST_HEADER;
        if (!mHighlights.isEmpty() && position <= mHighlights.size() + mBroadcasts.size() + 2) return IS_BROADCAST;
        if (mHighlights.isEmpty() && position == 1) return IS_BROADCAST_HEADER;
        if (mHighlights.isEmpty() && position <= mBroadcasts.size()+2) return IS_BROADCAST;
        return -1;
    }

    public int getChildPosition(int position, int group) {
        if (group == IS_HEADER) return 0;
        if (!mHighlights.isEmpty() && group == IS_HIGHLIGHT_HEADER) return 0;
        if (!mHighlights.isEmpty() && group == IS_HIGHLIGHT) return position - 2;
        if (!mHighlights.isEmpty() && group == IS_BROADCAST_HEADER) return 0;
        if (!mHighlights.isEmpty() && group == IS_BROADCAST) return position - mHighlights.size() - 3;
        if (mHighlights.isEmpty() && group == IS_BROADCAST_HEADER) return 0;
        if (mHighlights.isEmpty() && group == IS_BROADCAST) return position - 2;
        return -1;
    }

    private void loadImage(final int group, final int child, final String url, final ImageView imageView) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                final Bitmap bitmap = TwitchNetworkTasks.downloadBitmap(url);
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                        if (group == 0) mHighlights.get(child).mPreview = bitmap;
                        if (group == 1) mBroadcasts.get(child).mPreview = bitmap;
                    }
                });
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    public void setHighlightHeader(String h) {
        mHighlightHeader = h;
    }

    public void setBroadcastHeader(String h) {
        mBroadcastHeader = h;
    }

    public ArrayList<TwitchVideo> getHighlights() {
        return mHighlights;
    }

    public ArrayList<TwitchVideo> getBroadcasts() {
        return mBroadcasts;
    }

    public void clearAllData() {
        mHighlights.clear();
        mBroadcasts.clear();
    }

    public void clearHighlightData() {
        mHighlights.clear();
        notifyDataSetChanged();
    }

    public void clearBroadcastData() {
        mBroadcasts.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder {
        public ImageView imageView;
        public TextView secondLine;
        public TextView firstLine;
        public TextView secondLineViewers;
    }
}