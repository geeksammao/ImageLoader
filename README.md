# ImageLoader
## A light image loader


#### Feature

You can load image from web/file/assets with different scheme:

1. http://
2. file://
3.content:// 
4. assets://

You can get a `ImageLoader` by `ImageLoader.getInstance(context)` ,and load image to `ImageView` using `loader.loadImageToImageView(String uri,ImageView imageView)` or simply load image and use a callback to deal with the bitmap by using `loader.loadImage(String uri,ImageLoadCallback callback)`.

Also,you can use `imageLoader.setPlaceHolder(int res)` to set custom placeholder before loading.

#### Caution

You must call the load method from the UI thread,and the load work is dealed with asynchronously by the loader.

### PS

This is a very first version,so the function,expansion and code implemention are quite poor.Decide to use `builder pattern` to implement loader,replace `thread` with `future` and implement custom `thread pool` later.

