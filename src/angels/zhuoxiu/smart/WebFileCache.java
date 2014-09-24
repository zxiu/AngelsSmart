package angels.zhuoxiu.smart;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.webkit.URLUtil;

import com.zhuoxiu.angelslibrary.net.Conn;
import com.zhuoxiu.angelslibrary.util.AndroidUtils;

public class WebFileCache {
	static final String tag = WebFileCache.class.getSimpleName();
	static final String DISK_CACHE_PATH = "/web_image_cache/";

	ConcurrentHashMap<String, SoftReference<File>> memoryCache;
	static String diskCachePath;
	boolean diskCacheEnable, diskCacheInPublic;

	public WebFileCache(Context context) {
		// Set up in-memory cache store
		memoryCache = new ConcurrentHashMap<String, SoftReference<File>>();

		// Set up disk cache store
		if (AndroidUtils.hasSDCard()) {
			diskCachePath = context.getExternalCacheDir() + DISK_CACHE_PATH;
			diskCacheInPublic = true;
		} else {
			diskCachePath = context.getCacheDir() + DISK_CACHE_PATH;
			diskCacheInPublic = false;
		}

		File outFile = new File(diskCachePath);
		if (!outFile.exists()) {
			outFile.mkdirs();
		}
		diskCacheEnable = outFile.exists();
	}

	public File get(final String url) {
		File file = null;

		// Check for image in memory
		file = getFileFromMemory(url);

		// Check for image on disk cache
		if (file == null) {
			file = getFileFromDisk(url);

			// Try get file form Internet
			if (file == null) {
				file = getFileFromWeb(url);
			}

			// Write bitmap back into memory cache
			if (file != null) {
				putFileToMemory(url, file);
			}
		}

		return file;
	}

	public void put(String url, File file) {
		putFileToMemory(url, file);
		// cacheFileToDisk(url, file);
	}

	public void remove(String url) {
		if (url == null) {
			return;
		}

		// Remove from memory cache
		memoryCache.remove(getCacheKey(url));

		// Remove from file cache
		File f = new File(diskCachePath, getCacheKey(url));
		if (f.exists() && f.isFile()) {
			f.delete();
		}
	}

	public void clear() {
		// Remove everything from memory cache
		memoryCache.clear();

		// Remove everything from file cache
		File cachedFileDir = new File(diskCachePath);
		if (cachedFileDir.exists() && cachedFileDir.isDirectory()) {
			File[] cachedFiles = cachedFileDir.listFiles();
			for (File f : cachedFiles) {
				if (f.exists() && f.isFile()) {
					f.delete();
				}
			}
		}
	}

	private void putFileToMemory(final String url, final File file) {
		memoryCache.put(getCacheKey(url), new SoftReference<File>(file));
	}

	private File getFileFromMemory(String url) {
		File file = null;
		SoftReference<File> softRef = memoryCache.get(getCacheKey(url));
		if (softRef != null) {
			file = softRef.get();
		}
		return file;
	}

	private File getFileFromDisk(String url) {
		if (diskCacheEnable) {
			File file = new File(getFilePath(url));
			if (file.exists()) {
				return file;
			}
		}
		return null;
	}

	private File getFileFromWeb(String url) {
		File file = new File(WebFileCache.getFilePath(url));
		if (file.exists()) {
			file.delete();
		}
		try {
			file = Conn.download(url, getFilePath(url), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file;
	}

	public static String getFilePath(String url) {
		return diskCachePath + getCacheKey(url) + "_" + URLUtil.guessFileName(url, null, null);
	}

	public static String getCacheKey(String url) {
		if (url == null) {
			throw new RuntimeException("Null url passed in");
		} else {
			return url.replaceAll("[.:/,%?&=]", "_").replaceAll("[_]+", "_");
		}
	}

	public boolean isDiskCacheEnable() {
		return diskCacheEnable;
	}

	public boolean isDiskCacheInPublic() {
		return diskCacheEnable && diskCacheInPublic;
	}
}
