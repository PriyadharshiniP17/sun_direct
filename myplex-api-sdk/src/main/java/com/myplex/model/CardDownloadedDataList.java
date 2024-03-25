package com.myplex.model;

import java.io.Serializable;
import java.util.HashMap;

public class CardDownloadedDataList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7402056199262714763L;

	public HashMap<String,CardDownloadData> mDownloadedList = new HashMap<String,CardDownloadData>();

	@Override
	public String toString() {
		if (mDownloadedList == null) {
			return "null list";

		}
		String objString = "\n size- " + mDownloadedList.size();
		int i = 1;
		for (String key :
				mDownloadedList.keySet()) {
			objString = objString + "\n "+ i + ". " + mDownloadedList.get(key);
			i++;
		}
		return objString;
	}
}
