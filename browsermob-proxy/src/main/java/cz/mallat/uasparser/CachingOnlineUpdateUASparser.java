/*
 * 
 */
package cz.mallat.uasparser;

import java.io.*;
import java.net.URL;
import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * Adds a cache to the OnlineUpdateUAParser.
 * 
 * @author oli
 */
public class CachingOnlineUpdateUASparser extends OnlineUpdateUASparser {

	/** The Constant CACHE_FILENAME. */
	private static final String CACHE_FILENAME = "userAgentString.txt";

	/** The Constant PROPERTIES_FILENAME. */
	private static final String PROPERTIES_FILENAME = "userAgentString.properties";

	/** The prop. */
	private Properties prop;

	/** The cache dir. */
	private String cacheDir;

	/**
	 * The cache files are put into the java tmp directory.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public CachingOnlineUpdateUASparser() throws IOException {
		this(null);
	}

	/**
	 * The cache files are put into the cacheDir.
	 * 
	 * @param cacheDir
	 *            the cache dir
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public CachingOnlineUpdateUASparser(String cacheDir) throws IOException {
		this.cacheDir = cacheDir;
		this.prop = new Properties();

		if (cacheDir != null && !(new File(cacheDir).canWrite())) {
			throw new RuntimeException("Can't write to cacheDir: " + cacheDir);
		}

		File propFile = getPropertiesFile();
		if (propFile.exists()) {
			FileInputStream fis = new FileInputStream(getPropertiesFile());
			try {
				prop.load(fis);
				lastUpdateCheck = Long.parseLong(prop
						.getProperty("lastUpdateCheck"));
				currentVersion = prop.getProperty("currentVersion");
			} finally {
				fis.close();
			}

			try {
				loadDataFromFile(getCacheFile());
			} catch (IOException e) {
				e.printStackTrace();
				// reset the status variables, so we'll load the data file again
				lastUpdateCheck = 0;
				currentVersion = "";
			}
		}
	}

	/**
	 * This implementation uses a local properties file to keep the lastUpdate
	 * time and the local data file version.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	protected synchronized void checkDataMaps() throws IOException {
		if (lastUpdateCheck == 0
				|| lastUpdateCheck < System.currentTimeMillis()
						- updateInterval) {
			String versionOnServer = getVersionFromServer();
			if (currentVersion == null
					|| versionOnServer.compareTo(currentVersion) > 0) {
				loadDataFromInternetAndSave();
				loadDataFromFile(getCacheFile());
				currentVersion = versionOnServer;
				prop.setProperty("currentVersion", currentVersion);
			}
			lastUpdateCheck = System.currentTimeMillis();
			prop.setProperty("lastUpdateCheck", Long.toString(lastUpdateCheck));
			saveProperties(prop);
		}
	}

	/**
	 * Gets the cache file.
	 * 
	 * @return the cache file
	 */
	private File getCacheFile() {
		return new File(cacheDir == null ? System.getProperty("java.io.tmpdir")
				: cacheDir, CACHE_FILENAME);
	}

	/**
	 * Gets the properties file.
	 * 
	 * @return the properties file
	 */
	private File getPropertiesFile() {
		return new File(cacheDir == null ? System.getProperty("java.io.tmpdir")
				: cacheDir, PROPERTIES_FILENAME);
	}

	/**
	 * loads the data file from the server and saves it to the local file
	 * system.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void loadDataFromInternetAndSave() throws IOException {
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			URL url = new URL(DATA_RETRIVE_URL);
			is = url.openStream();
			fos = new FileOutputStream(getCacheFile());
			byte[] buff = new byte[1024 * 8];
			int len = 0;
			while ((len = is.read(buff)) != -1) {
				fos.write(buff, 0, len);
			}
		} finally {
			if (is != null) {
				is.close();
			}
			if (fos != null) {
				fos.close();
			}
		}

	}

	/**
	 * Saves the properties file to the local filesystem.
	 * 
	 * @param prop
	 *            the prop
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void saveProperties(Properties prop) throws FileNotFoundException,
			IOException {
		FileOutputStream fos = new FileOutputStream(getPropertiesFile());
		try {
			prop.store(fos, null);
		} finally {
			fos.close();
		}
	}

}
