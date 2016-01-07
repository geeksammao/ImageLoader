package geeksammao.bingyan.net.imageloader;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Geeksammao on 1/8/16.
 */
public class MyViewHolder extends RecyclerView.ViewHolder{
    public ImageView imageView;

    public MyViewHolder(View root){
        super(root);

        imageView = (ImageView) root.findViewById(R.id.item_imv);
    }

}
