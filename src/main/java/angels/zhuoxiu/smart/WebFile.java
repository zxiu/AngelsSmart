package angels.zhuoxiu.smart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;

public class WebFile implements SmartFile {
	private static final int CONNECT_TIMEOUT = 5000;
	private static final int READ_TIMEOUT = 10000;

	private static WebFileCache webFileCache;

	private String url;

	public WebFile(String url) {
		this.url = url;
	}

	public File getFile(Context context) {
		// Don't leak context
		if (webFileCache == null) {
			webFileCache = new WebFileCache(context);
		}

		// Try getting bitmap from cache first
		File file = null;
		if (url != null) {
			file = webFileCache.get(url);
			if (file == null) {
				file = getFileFromUrl(url);
				if (file != null) {
					webFileCache.put(url, file);
				}
			}
		} 
		return file;
	}

	private File getFileFromUrl(String url) {
		File file = new File(WebFileCache.getFilePath(url));
		if (file.exists()){
			file.delete();
		}
		try {
			URLConnection conn = new URL(url).openConnection();
			conn.setConnectTimeout(CONNECT_TIMEOUT);
			conn.setReadTimeout(READ_TIMEOUT);
			final int BUFFER_SIZE = 4096;
			FileOutputStream outputStream = new FileOutputStream(file);
			int bytesRead = -1;
			byte[] buffer = new byte[BUFFER_SIZE];
			InputStream inputStream = conn.getInputStream();
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			} 
			outputStream.close();
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file;
	}

	public static void removeFromCache(String url) {
		if (webFileCache != null) {
			webFileCache.remove(url);
		}
	}
}
