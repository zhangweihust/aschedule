package com.archermind.schedule.Utils;

import java.util.ArrayList;
import java.util.Map;

import com.amtcloud.mobile.android.business.AmtAlbumObj;
import com.amtcloud.mobile.android.business.AmtAlbumObj.AlbumItem;
import com.amtcloud.mobile.android.utils.Consts;

public class AlbumInfoUtil {

	private static int albumNum;

	private static AlbumItem[] albumItems;

	public static AlbumItem[] getAlbumInfos(AmtAlbumObj amtobj, Object data) {

		Map<String, ArrayList<Map<String, Object>>> rMap = (Map<String, ArrayList<Map<String, Object>>>) (data);
		ArrayList<Map<String, Object>> list = null;
		if (rMap.containsKey(Consts.ALBUMMAP_LIST)) {
			list = (ArrayList<Map<String, Object>>) (rMap
					.get(Consts.ALBUMMAP_LIST));
			int size = (list != null) ? list.size() : 0;
			albumNum = size;
			if (size > 0) {
				albumItems = new AlbumItem[size];
				Map<String, Object> map = null;
				for (int i = 0; i < size; i++) {
					map = list.get(i);
					int albumid = -1;
					String albumname = "";
					String albumuser = "";
					int parentid = -1;
					int albumcover = 0;
					String albumurl = "";
					String createtime = "";
					Object temp = "";
					Object objint = null;
					if (map != null) {
						if (map.containsKey(Consts.PS_ALBUMID)) {
							objint = map.get(Consts.PS_ALBUMID);
							if (objint != null && objint instanceof Integer)

							{
								albumid = (Integer) objint;
							}
						}
						if (map.containsKey(Consts.PS_ALBUMNAME)) {
							temp = map.get(Consts.PS_ALBUMNAME);
							if (temp != null && temp instanceof String) {
								albumname = (String) temp;
							}
						}
						if (map.containsKey(Consts.PS_ALBUMUSER)) {
							temp = map.get(Consts.PS_ALBUMUSER);
							if (temp != null && temp instanceof String) {
								albumuser = (String) temp;
							}
						}
						if (map.containsKey(Consts.PS_PARENTID)) {
							objint = map.get(Consts.PS_PARENTID);
							if (objint != null && objint instanceof Integer) {
								parentid = ((Integer) objint).intValue();
							}
						}
						if (map.containsKey(Consts.PS_ALBUMCOVER)) {
							objint = map.get(Consts.PS_ALBUMCOVER);
							if (objint != null && objint instanceof Integer) {
								int value = ((Integer) objint).intValue();
								albumcover = value;
							}
						}

						if (map.containsKey(Consts.PS_ALBUMURL)) {
							temp = map.get(Consts.PS_ALBUMURL);
							if (temp != null && temp instanceof String) {
								albumurl = (String) temp;
							}
						}

						if (map.containsKey(Consts.PS_CREATETIME)) {
							temp = map.get(Consts.PS_CREATETIME);
							if (temp != null && temp instanceof String) {
								createtime = (String) temp;
							}
						}
					}

					albumItems[i] = amtobj.constructAlbumItem(albumid,
							albumname, albumuser, parentid, albumcover,
							albumurl, createtime);

				}

				return albumItems;
			}
		}
		return null;
	}

	// 根据相册的名字查找相册的ID
	public static int getAlbumIdByName(AmtAlbumObj amtobj, Object data,
			String album) {
		int id = 0;

		Map<String, ArrayList<Map<String, Object>>> rMap = (Map<String, ArrayList<Map<String, Object>>>) (data);
		ArrayList<Map<String, Object>> list = null;
		if (rMap.containsKey(Consts.ALBUMMAP_LIST)) {
			list = (ArrayList<Map<String, Object>>) (rMap
					.get(Consts.ALBUMMAP_LIST));
			int size = (list != null) ? list.size() : 0;
			albumNum = size;
			if (size > 0) {
				albumItems = new AlbumItem[size];
				Map<String, Object> map = null;
				for (int i = 0; i < size; i++) {
					map = list.get(i);

					Object temp = "";
					Object objint = null;
					if (map != null) {

						if (map.containsKey(Consts.PS_ALBUMNAME)) {
							temp = map.get(Consts.PS_ALBUMNAME);
							if (temp != null && temp instanceof String) {
								String albumname = (String) temp;

								if (albumname.equals(album)) {

									if (map.containsKey(Consts.PS_ALBUMID)) {
										objint = map.get(Consts.PS_ALBUMID);
										if (objint != null
												&& objint instanceof Integer)

										{
											id = (Integer) objint;
										}
									}
								}

							}
						}

					}

				}
			}
		}

		return id;
	}

}
