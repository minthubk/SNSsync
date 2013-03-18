/**
 * 
 */
package com.syw.SNSsync.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

/**
 * @author onerain
 * 双缓存的异步图片加载
 */
public class AsyncBitmapLoader {
	//图片目录
	private static String imgDir = "/mnt/sdcard/SNSsync/img/";
	//内存图片软引用缓冲
	private HashMap<String, SoftReference<Bitmap>> imageCache = null;

	public AsyncBitmapLoader() {
		imageCache = new HashMap<String, SoftReference<Bitmap>>();
	}

	public Bitmap loadBitmap(final ImageView imageView, final String imageURL,
			final ImageCallBack imageCallBack) {
		// 在内存缓存中，则返回Bitmap对象
		if (imageCache.containsKey(imageURL)) {
			SoftReference<Bitmap> reference = imageCache.get(imageURL);
			Bitmap bitmap = reference.get();
			if (bitmap != null) {
				return bitmap;
			}
		} else {
			//否则在本地缓存查找
			String bitmapName = imageURL.substring(imageURL.lastIndexOf("/") + 1);
			File cacheDir = new File(imgDir);
			if (!cacheDir.exists()) {
				cacheDir.mkdirs();
			}
			File[] cacheFiles = cacheDir.listFiles();
			if (cacheFiles != null) {
				int i = 0;
				for (; i < cacheFiles.length; i++) {
					if (bitmapName.equals(cacheFiles[i].getName())) {
						break;
					}
				}
				//如果在本地缓存找到图片，则返回它
				if (i < cacheFiles.length) {
					return BitmapFactory.decodeFile(imgDir + bitmapName);
				}
			}
		}

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				imageCallBack.imageLoaded(imageView, (Bitmap) msg.obj);
			}
		};

		// 如果不在内存缓存中，也不在本地（被jvm回收掉），则开启线程下载图片
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				InputStream bitmapIs = HTTPUtil.getInputStreamFromUrl(imageURL);

				Bitmap bitmap = BitmapFactory.decodeStream(bitmapIs);
				imageCache.put(imageURL, new SoftReference<Bitmap>(bitmap));
				Message msg = handler.obtainMessage(0, bitmap);
				handler.sendMessage(msg);

				File dir = new File(imgDir);
				if (!dir.exists()) {
					dir.mkdirs();
				}

				File bitmapFile = new File(imgDir + imageURL.substring(imageURL.lastIndexOf("/") + 1));
				if (!bitmapFile.exists()) {
					try {
						bitmapFile.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(bitmapFile);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
					fos.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

		return null;
	}

	/**
	 * @author onerain
	 * 图片加载完成后的回调接口
	 */
	public interface ImageCallBack {
		public void imageLoaded(ImageView imageView, Bitmap bitmap);
	}
}