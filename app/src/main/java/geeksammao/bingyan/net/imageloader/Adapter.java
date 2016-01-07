package geeksammao.bingyan.net.imageloader;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import geeksammao.bingyan.net.imageloader.constant.Images;

/**
 * Created by Geeksammao on 1/8/16.
 */
public class Adapter extends RecyclerView.Adapter<MyViewHolder> {
    private Context context;

    public Adapter(Context context){
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.item_layout,parent,false);

        return new MyViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ImageLoader imageLoader = ImageLoader.getInstance(context);
        imageLoader.loadImageToImageView(Images.images[position],holder.imageView);

        Log.e("sam",Integer.toString(holder.imageView.getMeasuredHeight()));
//        Ion.with(context).load(Images.images[position]).intoImageView(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return Images.images.length;
    }
}
