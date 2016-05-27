package geeksammao.bingyan.net.imageloader.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import geeksammao.bingyan.net.imageloader.ImageLoader;
import geeksammao.bingyan.net.imageloader.MyApplication;
import geeksammao.bingyan.net.imageloader.R;
import geeksammao.bingyan.net.imageloader.constant.Images;
import geeksammao.bingyan.net.imageloader.util.NativeImageUtil;

/**
 * Created by Geeksammao on 1/8/16.
 */
public class Adapter extends RecyclerView.Adapter<MyViewHolder> {
    private Context context;
    private boolean loading;
    private List<String> pathList;

    public Adapter() {
        this.context = MyApplication.getInstance();
        this.loading = true;
        pathList = NativeImageUtil.getNativeImagePath(context);
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);

        return new MyViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        ImageLoader imageLoader = ImageLoader.getInstance(context);
        imageLoader.loadImageToImageView(Images.images[position], holder.imageView);
    }

    @Override
    public int getItemCount() {
        return Images.images.length;
    }
}
