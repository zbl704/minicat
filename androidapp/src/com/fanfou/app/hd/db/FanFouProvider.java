package com.fanfou.app.hd.db;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.fanfou.app.hd.R;
import com.fanfou.app.hd.App;
import com.fanfou.app.hd.api.User;
import com.fanfou.app.hd.db.Contents.BasicColumns;
import com.fanfou.app.hd.db.Contents.DirectMessageInfo;
import com.fanfou.app.hd.db.Contents.DraftInfo;
import com.fanfou.app.hd.db.Contents.StatusInfo;
import com.fanfou.app.hd.db.Contents.UserInfo;
import com.fanfou.app.hd.service.Constants;
import com.fanfou.app.hd.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.05.24
 * @version 1.5 2011.05.25
 * @version 1.6 2011.06.04
 * @version 1.7 2011.07.14
 * @version 1.8 2011.07.22
 * @version 1.9 2011.10.09
 * @version 2.0 2011.10.25
 * @version 2.5 2011.10.26
 * @version 3.0 2011.10.28
 * @version 3.1 2011.10.30
 * @version 3.2 2011.11.07
 * @version 3.5 2011.11.10
 * @version 3.6 2011.11.11
 * @version 3.7 2011.11.15
 * @version 3.8 2011.11.21
 * @version 3.9 2011.11.23
 * @version 4.0 2011.12.19
 * @version 4.1 2012.02.15
 * 
 */
public class FanFouProvider extends ContentProvider {

	private static final String TAG = FanFouProvider.class.getSimpleName();

	private void log(String message) {
		Log.d(TAG, message);
	}

	private SQLiteHelper dbHelper;

	public static final String ORDERBY_DATE = BasicColumns.CREATED_AT;
	public static final String ORDERBY_DATE_DESC = BasicColumns.CREATED_AT
			+ " DESC";

	public static final String ORDERBY_STATUSES_COUNT = UserInfo.STATUSES_COUNT;
	public static final String ORDERBY_STATUSES_COUNT_DESC = UserInfo.STATUSES_COUNT
			+ " DESC";

	public static final int USERS_ALL = 1;// 查询全部用户信息，可附加条件参数
	public static final int USER_SEARCH = 2; // 搜索用户，未实现
	public static final int USER_ID = 3; // 根据ID查询单个用户
	public static final int USER_TYPE = 4;

	public static final int USER_SEARCH_SUGGEST = 7;
	public static final int USER_REFRESH_SHORTCUT = 8;

	public static final int STATUSES_ALL = 21;
	public static final int STATUS_SEARCH_LOCAL = 22;
	public static final int STATUS_ID = 24;
	public static final int STATUS_SEARCH = 26;
	public static final int STATUS_TYPE = 27;
	public static final int STATUS_ACTION_CLEAN = 28;
	public static final int STATUS_ACTION_COUNT = 29;

	public static final int MESSAGES_ALL = 41;
	public static final int MESSAGE_ITEM = 42;
	public static final int MESSAGE_ID = 43;
	public static final int MESSAGE_LIST = 44;// 对话列表，每个人最新的一条，收件箱为准
	public static final int MESSAGE_USER = 45;// 每个人的私信对话列表

	public static final int DRAFT_ALL = 61;
	public static final int DRAFT_ID = 62;

	public static final int ACTION_CLEAN_ALL = 110;
	public static final int ACTION_CLEAN_STATUS = 112;
	public static final int ACTION_CLEAN_MESSAGE = 113;
	public static final int ACTION_CLEAN_USER = 114;

	public static final int ACTION_COUNT_STATUS = 121;
	public static final int ACTION_COUNT_MESSAGE = 122;
	public static final int ACTION_COUNT_USER = 123;

	private static final UriMatcher sUriMatcher;
	// private static HashMap<String, String> sUserProjectionMap;
	// private static HashMap<String, String> sStatusProjectionMap;
	// private static HashMap<String, String> sMessageProjectionMap;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		sUriMatcher.addURI(Contents.AUTHORITY, UserInfo.URI_PATH, USERS_ALL);
		sUriMatcher.addURI(Contents.AUTHORITY, UserInfo.URI_PATH + "/search/*",
				USER_SEARCH);
		sUriMatcher.addURI(Contents.AUTHORITY, UserInfo.URI_PATH + "/id/*",
				USER_ID);
		sUriMatcher.addURI(Contents.AUTHORITY, UserInfo.URI_PATH + "/type/#",
				USER_TYPE);

		// add for user info search in search box
		sUriMatcher.addURI(Contents.AUTHORITY,
				SearchManager.SUGGEST_URI_PATH_QUERY, USER_SEARCH_SUGGEST);
		sUriMatcher.addURI(Contents.AUTHORITY,
				SearchManager.SUGGEST_URI_PATH_QUERY + "/*",
				USER_SEARCH_SUGGEST);
		sUriMatcher.addURI(Contents.AUTHORITY,
				SearchManager.SUGGEST_URI_PATH_SHORTCUT, USER_REFRESH_SHORTCUT);
		sUriMatcher.addURI(Contents.AUTHORITY,
				SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*",
				USER_REFRESH_SHORTCUT);

		sUriMatcher.addURI(Contents.AUTHORITY, StatusInfo.URI_PATH,
				STATUSES_ALL);
		sUriMatcher.addURI(Contents.AUTHORITY,
				StatusInfo.URI_PATH + "/local/*", STATUS_SEARCH_LOCAL);
		sUriMatcher.addURI(Contents.AUTHORITY, StatusInfo.URI_PATH + "/id/*",
				STATUS_ID);

		sUriMatcher.addURI(Contents.AUTHORITY, StatusInfo.URI_PATH
				+ "/search/*", STATUS_SEARCH);

		sUriMatcher.addURI(Contents.AUTHORITY, StatusInfo.URI_PATH + "/type/#",
				STATUS_TYPE);
		sUriMatcher.addURI(Contents.AUTHORITY, StatusInfo.URI_PATH
				+ "/action/count/#", STATUS_ACTION_COUNT);
		sUriMatcher.addURI(Contents.AUTHORITY, StatusInfo.URI_PATH
				+ "/action/clean", STATUS_ACTION_CLEAN);

		sUriMatcher.addURI(Contents.AUTHORITY, DirectMessageInfo.URI_PATH,
				MESSAGES_ALL);
		sUriMatcher.addURI(Contents.AUTHORITY, DirectMessageInfo.URI_PATH
				+ "/item/*", MESSAGE_ITEM);
		sUriMatcher.addURI(Contents.AUTHORITY, DirectMessageInfo.URI_PATH
				+ "/id/#", MESSAGE_ID);

		sUriMatcher.addURI(Contents.AUTHORITY, DirectMessageInfo.URI_PATH
				+ "/list", MESSAGE_LIST);
		sUriMatcher.addURI(Contents.AUTHORITY, DirectMessageInfo.URI_PATH
				+ "/user/*", MESSAGE_USER);

		sUriMatcher.addURI(Contents.AUTHORITY, DraftInfo.URI_PATH, DRAFT_ALL);
		sUriMatcher.addURI(Contents.AUTHORITY, DraftInfo.URI_PATH + "/#",
				DRAFT_ID);

	}

	@Override
	public boolean onCreate() {
		dbHelper = new SQLiteHelper(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case USERS_ALL:
		case USER_TYPE:
			return UserInfo.CONTENT_TYPE;
		case USER_ID:
			return UserInfo.CONTENT_ITEM_TYPE;
		case STATUSES_ALL:
		case STATUS_SEARCH_LOCAL:
		case STATUS_SEARCH:
			return StatusInfo.CONTENT_TYPE;
		case STATUS_ID:
		case STATUS_ACTION_COUNT:
		case STATUS_ACTION_CLEAN:
			return StatusInfo.CONTENT_ITEM_TYPE;
		case MESSAGES_ALL:
		case MESSAGE_LIST:
		case MESSAGE_USER:
			return DirectMessageInfo.CONTENT_TYPE;
		case MESSAGE_ITEM:
		case MESSAGE_ID:
			return DirectMessageInfo.CONTENT_ITEM_TYPE;
		case DRAFT_ALL:
			return DraftInfo.CONTENT_TYPE;
		case DRAFT_ID:
			return DraftInfo.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("getType() Unknown URI " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] columns, String where,
			String[] whereArgs, String orderBy) {
		if (App.DEBUG) {
			log("query() uri = " + uri + " where = (" + where
					+ ") whereArgs = " + StringHelper.toString(whereArgs)
					+ " orderBy = " + orderBy);
		}
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String order = orderBy;
		String selection = where;
		String[] selectionArgs = whereArgs;
		switch (sUriMatcher.match(uri)) {
		case USERS_ALL:
			qb.setTables(UserInfo.TABLE_NAME);
			break;
		case USER_TYPE:
			qb.setTables(UserInfo.TABLE_NAME);
			qb.appendWhere(BasicColumns.TYPE + "=");
			qb.appendWhere(uri.getPathSegments().get(2));
			break;
		case USER_ID:
			qb.setTables(UserInfo.TABLE_NAME);
			qb.appendWhere(BasicColumns.ID + "=");
			qb.appendWhere("'" + uri.getPathSegments().get(2) + "'");
			break;
		case STATUSES_ALL:
		case STATUS_SEARCH:
			qb.setTables(StatusInfo.TABLE_NAME);
//			if (order == null) {
//				order = ORDERBY_DATE_DESC;
//			}
			break;
		case STATUS_ID:
			qb.setTables(StatusInfo.TABLE_NAME);
			qb.appendWhere(BasicColumns.ID + "=");
			qb.appendWhere("'" + uri.getPathSegments().get(2) + "'");
			break;
		case STATUS_ACTION_COUNT:
			return countStatus(uri);
		case MESSAGES_ALL:
			qb.setTables(DirectMessageInfo.TABLE_NAME);
			if (order == null) {
				order = ORDERBY_DATE_DESC;
			}
			break;
		case MESSAGE_LIST:
			// String sql =
			// "select * from message m1 where type= 21 and created_at =
			// (select max(created_at) from message m2 where type= 21 and
			// m1.sender_id = m2.sender_id group by (sender_id))
			// order by created_at desc;";
			String typeStr = DirectMessageInfo.TYPE + "= "
					+ Constants.TYPE_DIRECT_MESSAGES_INBOX;
			String orderStr = " order by " + DirectMessageInfo.CREATED_AT + " desc";
			String subQuery = "(select max(" + DirectMessageInfo.CREATED_AT
					+ ") from " + DirectMessageInfo.TABLE_NAME + " m2 where "
					+ typeStr + " and m1.sender_id = m2.sender_id group by ("
					+ DirectMessageInfo.SENDER_ID + "))";
			String querySql = "select * from " + DirectMessageInfo.TABLE_NAME
					+ " m1 where " + typeStr + " and "
					+ DirectMessageInfo.CREATED_AT + " = " + subQuery + orderStr
					+ " ;";
			Cursor cursor = db.rawQuery(querySql, null);
			if (App.DEBUG) {
				log("query() uri MESSAGE_LIST " + uri + " cursor=" + cursor);
			}
			if (cursor == null) {
				if (App.DEBUG) {
					log("query() uri MESSAGE_LIST " + uri + " failed.");
				}
			} else {
				cursor.setNotificationUri(getContext().getContentResolver(),
						uri);
			}
			return cursor;
			// break;
		case MESSAGE_USER:
			break;
		case MESSAGE_ITEM:
			qb.setTables(DirectMessageInfo.TABLE_NAME);
			qb.appendWhere(BasicColumns.ID + "=");
			qb.appendWhere("'" + uri.getPathSegments().get(2) + "'");
			break;
		case MESSAGE_ID:
			qb.setTables(DirectMessageInfo.TABLE_NAME);
			qb.appendWhere(BaseColumns._ID + "=");
			qb.appendWhere(uri.getPathSegments().get(2));
			break;
		case DRAFT_ALL:
			qb.setTables(DraftInfo.TABLE_NAME);
			if (order == null) {
				order = ORDERBY_DATE_DESC;
			}
			break;
		case DRAFT_ID:
			throw new UnsupportedOperationException("unsupported operation: "
					+ uri);
			// break;
		default:
			throw new IllegalArgumentException("query() Unknown URI " + uri);
		}

		Cursor c = qb.query(db, columns, selection, selectionArgs, null, null,
				order);

		if (c == null) {
			if (App.DEBUG) {
				log("query() uri " + uri + " failed.");
			}
		} else {
			c.setNotificationUri(getContext().getContentResolver(), uri);
		}

		return c;
	}

	/**
	 * 计算某种类型的消息数量
	 * 
	 * @param uri
	 * @return
	 */
	private Cursor countStatus(Uri uri) {
		int type = Integer.parseInt(uri.getPathSegments().get(3));
		String sql = "SELECT COUNT(" + BasicColumns.ID + ") FROM "
				+ StatusInfo.TABLE_NAME;
		if (type == Constants.TYPE_NONE) {
			sql += " ;";
		} else {
			sql += " WHERE " + BasicColumns.TYPE + "=" + type + ";";
		}
		// log("countStatus() status count, uri=" + uri + " ");
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor result = db.rawQuery(sql, null);
		return result;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (values == null || values.size() == 0) {
			throw new IllegalArgumentException("插入数据不能为空.");
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String table;
		Uri contentUri;
		switch (sUriMatcher.match(uri)) {
		case USERS_ALL:
			table = UserInfo.TABLE_NAME;
			contentUri = UserInfo.CONTENT_URI;
			break;
		case STATUSES_ALL:
			table = StatusInfo.TABLE_NAME;
			contentUri = StatusInfo.CONTENT_URI;
			break;
		// case PUBLIC:
		// table = StatusInfo.PUBLIC_TABLE_NAME;
		// contentUri = StatusInfo.PUBLIC_URI;
		// break;
		case MESSAGES_ALL:
			table = DirectMessageInfo.TABLE_NAME;
			contentUri = DirectMessageInfo.CONTENT_URI;
			break;
		case DRAFT_ALL:
			table = DraftInfo.TABLE_NAME;
			contentUri = DraftInfo.CONTENT_URI;
			break;
		case USER_TYPE:
		case USER_ID:
		case STATUS_ID:
		case MESSAGE_ITEM:
		case DRAFT_ID:
			throw new UnsupportedOperationException("Cannot insert URI: " + uri);
		default:
			throw new IllegalArgumentException("insert() Unknown URI " + uri);
		}

		long rowId = db.insert(table, null, values);
		if (App.DEBUG) {
			log("insert() uri=" + uri.toString() + " id="
					+ values.getAsString(BasicColumns.ID) + " rowId=" + rowId);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return uri;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		final int match = sUriMatcher.match(uri);
		int result = 0;
		switch (match) {
		case STATUSES_ALL:
			result = bulkInsertStatuses(values);
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		case USERS_ALL:
			result = bulkInsertUsers(values);
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		case MESSAGES_ALL:
			result = bulkInsertData(DirectMessageInfo.TABLE_NAME, values);
			break;
		case DRAFT_ALL:
			result = bulkInsertData(DraftInfo.TABLE_NAME, values);
			break;
		default:
			if (App.DEBUG) {
				throw new UnsupportedOperationException("unsupported uri: "
						+ uri);
			}
			break;
		}
		return result;
	}

	private int bulkInsertData(String table, ContentValues[] values) {
		int numInserted = 0;
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			for (ContentValues value : values) {
				long result = db.insert(table, null, value);
				if (result > -1) {
					numInserted++;
				}
			}
			if (App.DEBUG) {
				log("bulkInsertData insert rows=" + numInserted);
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			if (App.DEBUG) {
				e.printStackTrace();
			}
		} finally {
			db.endTransaction();
		}
		return numInserted;

	}

	private int bulkInsertUsers(ContentValues[] values) {
		int numInserted = 0;
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		InsertHelper ih = new InsertHelper(db, UserInfo.TABLE_NAME);

		int id = ih.getColumnIndex(BasicColumns.ID);
		int rawId=ih.getColumnIndex(BasicColumns.RAWID);
		int ownerId = ih.getColumnIndex(BasicColumns.OWNER_ID);

		int screenName = ih.getColumnIndex(UserInfo.SCREEN_NAME);
		int location = ih.getColumnIndex(UserInfo.LOCATION);
		int gender = ih.getColumnIndex(UserInfo.GENDER);
		int birthday = ih.getColumnIndex(UserInfo.BIRTHDAY);

		int description = ih.getColumnIndex(UserInfo.DESCRIPTION);
		int profileImageUrl = ih.getColumnIndex(UserInfo.PROFILE_IMAGE_URL);
		int url = ih.getColumnIndex(UserInfo.URL);
		int protect = ih.getColumnIndex(UserInfo.PROTECTED);

		int followersCount = ih.getColumnIndex(UserInfo.FOLLOWERS_COUNT);
		int friendsCount = ih.getColumnIndex(UserInfo.FRIENDS_COUNT);
		int favoritesCount = ih.getColumnIndex(UserInfo.FAVORITES_COUNT);
		int statusesCount = ih.getColumnIndex(UserInfo.STATUSES_COUNT);

		int following = ih.getColumnIndex(UserInfo.FOLLOWING);
		int createdAt = ih.getColumnIndex(BasicColumns.CREATED_AT);

		int type = ih.getColumnIndex(BasicColumns.TYPE);

		try {
			db.beginTransaction();
			for (ContentValues value : values) {
				ih.prepareForInsert();

				ih.bind(id, value.getAsString(BasicColumns.ID));
				ih.bind(rawId, value.getAsLong(BasicColumns.RAWID));
				ih.bind(ownerId, value.getAsString(BasicColumns.OWNER_ID));

				ih.bind(screenName, value.getAsString(UserInfo.SCREEN_NAME));
				ih.bind(location, value.getAsString(UserInfo.LOCATION));
				ih.bind(gender, value.getAsString(UserInfo.GENDER));
				ih.bind(birthday, value.getAsString(UserInfo.BIRTHDAY));

				ih.bind(description, value.getAsString(UserInfo.DESCRIPTION));
				ih.bind(profileImageUrl,
						value.getAsString(UserInfo.PROFILE_IMAGE_URL));
				ih.bind(url, value.getAsString(UserInfo.URL));
				ih.bind(protect, value.getAsBoolean(UserInfo.PROTECTED));

				ih.bind(followersCount,
						value.getAsInteger(UserInfo.FOLLOWERS_COUNT));
				ih.bind(friendsCount,
						value.getAsInteger(UserInfo.FRIENDS_COUNT));
				ih.bind(favoritesCount,
						value.getAsInteger(UserInfo.FAVORITES_COUNT));
				ih.bind(statusesCount,
						value.getAsInteger(UserInfo.STATUSES_COUNT));

				ih.bind(following, value.getAsBoolean(UserInfo.FOLLOWING));
				ih.bind(createdAt, value.getAsLong(BasicColumns.CREATED_AT));
				ih.bind(type, value.getAsInteger(BasicColumns.TYPE));

				long result = ih.execute();
				if (result > -1) {
					numInserted++;
				}
			}
			if (App.DEBUG) {
				log("bulkInsertUsers insert rows=" + numInserted);
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			if (App.DEBUG) {
				e.printStackTrace();
			}
		} finally {
			ih.close();
			db.endTransaction();
		}
		return numInserted;
	}

	private int bulkInsertStatuses(ContentValues[] values) {
		int numInserted = 0;
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		InsertHelper ih = new InsertHelper(db, StatusInfo.TABLE_NAME);

		int id = ih.getColumnIndex(BasicColumns.ID);
		int rawId=ih.getColumnIndex(BasicColumns.RAWID);
		int ownerId = ih.getColumnIndex(BasicColumns.OWNER_ID);
		int createdAt = ih.getColumnIndex(BasicColumns.CREATED_AT);

		int text = ih.getColumnIndex(StatusInfo.TEXT);
		int simpleText = ih.getColumnIndex(StatusInfo.SIMPLE_TEXT);
		int source = ih.getColumnIndex(StatusInfo.SOURCE);

		int inReplyToStatusId = ih
				.getColumnIndex(StatusInfo.IN_REPLY_TO_STATUS_ID);
		int inReplyToUserId = ih.getColumnIndex(StatusInfo.IN_REPLY_TO_USER_ID);
		int inReplyToScreenName = ih
				.getColumnIndex(StatusInfo.IN_REPLY_TO_SCREEN_NAME);

		int photoImageUrl = ih.getColumnIndex(StatusInfo.PHOTO_IMAGE_URL);
		int photoThumbUrl = ih.getColumnIndex(StatusInfo.PHOTO_THUMB_URL);
		int photoLargeUrl = ih.getColumnIndex(StatusInfo.PHOTO_LARGE_URL);

		int userId = ih.getColumnIndex(StatusInfo.USER_ID);
		int userScreenName = ih.getColumnIndex(StatusInfo.USER_SCREEN_NAME);
		int userProfileImageUrl = ih
				.getColumnIndex(StatusInfo.USER_PROFILE_IMAGE_URL);

		int truncated = ih.getColumnIndex(StatusInfo.TRUNCATED);
		int favorited = ih.getColumnIndex(StatusInfo.FAVORITED);
		int isSelf = ih.getColumnIndex(StatusInfo.IS_SELF);

		int isRead = ih.getColumnIndex(StatusInfo.IS_READ);
		int isThread = ih.getColumnIndex(StatusInfo.IS_THREAD);
		int hasPhoto = ih.getColumnIndex(StatusInfo.HAS_PHOTO);
		int special = ih.getColumnIndex(StatusInfo.SPECIAL);

		int type = ih.getColumnIndex(BasicColumns.TYPE);

		try {
			db.beginTransaction();
			for (ContentValues value : values) {
				ih.prepareForInsert();

				ih.bind(id, value.getAsString(BasicColumns.ID));
				ih.bind(rawId, value.getAsLong(BasicColumns.RAWID));
				ih.bind(ownerId, value.getAsString(BasicColumns.OWNER_ID));
				ih.bind(createdAt, value.getAsLong(BasicColumns.CREATED_AT));

				ih.bind(text, value.getAsString(StatusInfo.TEXT));
				ih.bind(simpleText, value.getAsString(StatusInfo.SIMPLE_TEXT));
				ih.bind(source, value.getAsString(StatusInfo.SOURCE));

				ih.bind(inReplyToStatusId,
						value.getAsString(StatusInfo.IN_REPLY_TO_STATUS_ID));
				ih.bind(inReplyToUserId,
						value.getAsString(StatusInfo.IN_REPLY_TO_USER_ID));
				ih.bind(inReplyToScreenName,
						value.getAsString(StatusInfo.IN_REPLY_TO_SCREEN_NAME));

				ih.bind(photoImageUrl,
						value.getAsString(StatusInfo.PHOTO_IMAGE_URL));
				ih.bind(photoThumbUrl,
						value.getAsString(StatusInfo.PHOTO_THUMB_URL));
				ih.bind(photoLargeUrl,
						value.getAsString(StatusInfo.PHOTO_LARGE_URL));

				ih.bind(userId, value.getAsString(StatusInfo.USER_ID));
				ih.bind(userScreenName,
						value.getAsString(StatusInfo.USER_SCREEN_NAME));
				ih.bind(userProfileImageUrl,
						value.getAsString(StatusInfo.USER_PROFILE_IMAGE_URL));

				ih.bind(truncated, value.getAsBoolean(StatusInfo.TRUNCATED));
				ih.bind(favorited, value.getAsBoolean(StatusInfo.FAVORITED));
				ih.bind(isSelf, value.getAsBoolean(StatusInfo.IS_SELF));

				ih.bind(isRead, value.getAsBoolean(StatusInfo.IS_READ));
				ih.bind(isThread, value.getAsBoolean(StatusInfo.IS_THREAD));
				ih.bind(hasPhoto, value.getAsBoolean(StatusInfo.HAS_PHOTO));
				ih.bind(special, value.getAsBoolean(StatusInfo.SPECIAL));

				ih.bind(type, value.getAsInteger(BasicColumns.TYPE));

				long result = ih.execute();
				if (result > -1) {
					numInserted++;
				}

			}
			if (App.DEBUG) {
				log("bulkInsertStatuses insert rows=" + numInserted);
			}
			db.setTransactionSuccessful();
			// numInserted = values.length;
		} catch (Exception e) {
			if (App.DEBUG) {
				e.printStackTrace();
			}
		} finally {
			ih.close();
			db.endTransaction();
		}
		return numInserted;

	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		String id;
		String _id;
		switch (sUriMatcher.match(uri)) {
		case USERS_ALL:
			count = db.delete(UserInfo.TABLE_NAME, where, whereArgs);
			break;
		case USER_TYPE:
			count = db.delete(UserInfo.TABLE_NAME, BasicColumns.TYPE + "=?",
					new String[] { uri.getPathSegments().get(2) });
		case USER_ID:
			id = uri.getPathSegments().get(2);
			count = db.delete(UserInfo.TABLE_NAME, BasicColumns.ID + "=?",
					new String[] { id });
			break;
		case STATUSES_ALL:
			count = db.delete(StatusInfo.TABLE_NAME, where, whereArgs);
			break;
		case STATUS_ID:
			id = uri.getPathSegments().get(2);
			count = db.delete(StatusInfo.TABLE_NAME, BasicColumns.ID + "=?",
					new String[] { id });
			break;
		case STATUS_ACTION_CLEAN:
			// count = cleanDatabase(uri, where, whereArgs);
			count = cleanAll();
			break;
		case MESSAGES_ALL:
			count = db.delete(DirectMessageInfo.TABLE_NAME, where, whereArgs);
			break;
		case MESSAGE_ITEM:
			id = uri.getPathSegments().get(2);
			count = db.delete(DirectMessageInfo.TABLE_NAME, BasicColumns.ID
					+ "=?", new String[] { id });
			break;
		case MESSAGE_ID:
			_id = uri.getPathSegments().get(2);
			count = db.delete(DirectMessageInfo.TABLE_NAME, BaseColumns._ID
					+ "=?", new String[] { _id });
			break;
		case DRAFT_ALL:
			count = db.delete(DraftInfo.TABLE_NAME, where, whereArgs);
			break;
		case DRAFT_ID:
			_id = uri.getPathSegments().get(1);
			count = db.delete(DraftInfo.TABLE_NAME, BaseColumns._ID + "=?",
					new String[] { _id });
			break;
		default:
			throw new IllegalArgumentException("delete() Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	private int cleanAll() {
		final String count = getContext().getString(R.string.config_store_max);
		int result = 0;
		result += cleanMessages(count);
		result += cleanHome(count);
		result += cleanMentions(count);
		result += cleanPublicTimeline();
		result += cleanOthersTimeline();
		result += cleanUserTimeline();
		result += cleanFavorites();

		result += cleanUsers();
		return result;
	}

	/**
	 * 压缩数据库，删除旧消息
	 * 
	 * @param uri
	 * @param db
	 * @return
	 */
	private int cleanDatabase(Uri uri, String where, String[] whereArgs) {
		final String count = getContext().getString(R.string.config_store_max);
		int type = Integer.parseInt(uri.getPathSegments().get(3));
		int result = -1;
		switch (type) {
		case Constants.TYPE_STATUSES_HOME_TIMELINE:
			result = cleanHome(count);
			break;
		case Constants.TYPE_STATUSES_MENTIONS:
			result = cleanMentions(count);
			break;
		case Constants.TYPE_STATUSES_PUBLIC_TIMELINE:
			result = cleanPublicTimeline();
			break;
		case Constants.TYPE_STATUSES_USER_TIMELINE:
			if (whereArgs != null && whereArgs.length == 1
					&& !StringHelper.isEmpty(whereArgs[0])) {
				result = cleanTimeline(whereArgs[0], count);
			}
			break;
		case Constants.TYPE_FAVORITES_LIST:
			if (whereArgs != null && whereArgs.length == 1
					&& !StringHelper.isEmpty(whereArgs[0])) {
				result = cleanFavorites(whereArgs[0]);
			}
			break;
		default:
			break;
		}

		return result;
	}

	private int cleanUserTimeline() {
		String where = BasicColumns.TYPE + "=?";
		String[] whereArgs = new String[] { String
				.valueOf(Constants.TYPE_STATUSES_USER_TIMELINE), };
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(StatusInfo.TABLE_NAME, where, whereArgs);
	}

	private int cleanFavorites() {
		String where = BasicColumns.TYPE + "=?";
		String[] whereArgs = new String[] { String
				.valueOf(Constants.TYPE_FAVORITES_LIST), };
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(StatusInfo.TABLE_NAME, where, whereArgs);
	}

	private int cleanPublicTimeline() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		// String where = " " + StatusInfo.CREATED_AT + " < " + " (SELECT "
		// + StatusInfo.CREATED_AT + " FROM " + StatusInfo.TABLE_NAME;
		//
		// where += " WHERE " + StatusInfo.TYPE + " = " + Status.TYPE_PUBLIC;
		//
		// where += " ORDER BY " + StatusInfo.CREATED_AT +
		// " DESC LIMIT 1 OFFSET "
		// + Commons.DATA_NORMAL_MAX + ")";
		//
		// where += " AND " + StatusInfo.TYPE + " = " + Status.TYPE_PUBLIC +
		// " ";
		//
		// // log("cleanPublic where: [" + where + "]");
		//
		//
		// return db.delete(StatusInfo.TABLE_NAME, where, null);
		String where = BasicColumns.TYPE + "=?";
		String[] whereArgs = new String[] { String
				.valueOf(Constants.TYPE_STATUSES_PUBLIC_TIMELINE) };
		return db.delete(StatusInfo.TABLE_NAME, where, whereArgs);
	}

	private int cleanOthersTimeline() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String where = BasicColumns.TYPE + "=?";
		String[] whereArgs = new String[] { String.valueOf(Constants.TYPE_NONE) };
		return db.delete(StatusInfo.TABLE_NAME, where, whereArgs);
	}

	private int cleanTimeline(String userId, String count) {

		String where = " " + BasicColumns.CREATED_AT + " < " + " (SELECT "
				+ BasicColumns.CREATED_AT + " FROM " + StatusInfo.TABLE_NAME;

		where += " WHERE " + BasicColumns.TYPE + " = "
				+ Constants.TYPE_STATUSES_USER_TIMELINE;
		where += " AND " + StatusInfo.USER_ID + " = " + " '" + userId + "' ";

		where += " ORDER BY " + BasicColumns.CREATED_AT
				+ " DESC LIMIT 1 OFFSET " + count + " )";

		where += " AND " + BasicColumns.TYPE + " = "
				+ Constants.TYPE_STATUSES_USER_TIMELINE + " ";
		where += " AND " + StatusInfo.USER_ID + " = " + " '" + userId + "' ";

		// log("cleanTimeline where: [" + where + "]");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(StatusInfo.TABLE_NAME, where, null);

	}

	private int cleanFavorites(String ownerId) {

		String where = " " + BasicColumns.CREATED_AT + " < " + " (SELECT "
				+ BasicColumns.CREATED_AT + " FROM " + StatusInfo.TABLE_NAME;

		where += " WHERE " + BasicColumns.TYPE + " = "
				+ Constants.TYPE_FAVORITES_LIST;
		where += " AND " + BasicColumns.OWNER_ID + " = " + " '" + ownerId
				+ "' ";

		where += " ORDER BY " + BasicColumns.CREATED_AT
				+ " DESC LIMIT 1 OFFSET 20 )";

		where += " AND " + BasicColumns.TYPE + " = "
				+ Constants.TYPE_FAVORITES_LIST + " ";
		where += " AND " + BasicColumns.OWNER_ID + " = " + " '" + ownerId
				+ "' ";

		// log("cleanFavorites where: [" + where + "]");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(StatusInfo.TABLE_NAME, where, null);
	}

	private int cleanHome(String count) {
		String where = " " + BasicColumns.CREATED_AT + " < " + " (SELECT "
				+ BasicColumns.CREATED_AT + " FROM " + StatusInfo.TABLE_NAME;

		where += " WHERE " + BasicColumns.TYPE + " = "
				+ Constants.TYPE_STATUSES_HOME_TIMELINE;

		where += " ORDER BY " + BasicColumns.CREATED_AT
				+ " DESC LIMIT 1 OFFSET " + count + " )";
		where += " AND " + BasicColumns.TYPE + " = "
				+ Constants.TYPE_STATUSES_HOME_TIMELINE + " ";

		// log("cleanHome where: [" + where + "]");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(StatusInfo.TABLE_NAME, where, null);

	}

	private int cleanMentions(String count) {

		String where = " " + BasicColumns.CREATED_AT + " < " + " (SELECT "
				+ BasicColumns.CREATED_AT + " FROM " + StatusInfo.TABLE_NAME;

		where += " WHERE " + BasicColumns.TYPE + " = "
				+ Constants.TYPE_STATUSES_MENTIONS;

		where += " ORDER BY " + BasicColumns.CREATED_AT
				+ " DESC LIMIT 1 OFFSET " + count + " )";

		where += " AND " + BasicColumns.TYPE + " = "
				+ Constants.TYPE_STATUSES_MENTIONS + " ";

		// log("cleanMentions where: [" + where + "]");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(StatusInfo.TABLE_NAME, where, null);

	}

	private int cleanMessages(String count) {
		String where = " " + BasicColumns.CREATED_AT + " < " + " (SELECT "
				+ BasicColumns.CREATED_AT + " FROM "
				+ DirectMessageInfo.TABLE_NAME;

		where += " ORDER BY " + BasicColumns.CREATED_AT
				+ " DESC LIMIT 1 OFFSET " + count + " )";

		// log("cleanMentions where: [" + where + "]");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(DirectMessageInfo.TABLE_NAME, where, null);
	}

	private int cleanUsers() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String where = BasicColumns.OWNER_ID + " !=? ";
		String[] whereArgs = new String[] { String.valueOf(App.getUserId()) };
		return db.delete(UserInfo.TABLE_NAME, where, whereArgs);
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		// log("update() uri = " + uri + " where= (" + where + ") whereArgs = "
		// + StringHelper.toString(whereArgs));
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		String id;
		String _id;
		switch (sUriMatcher.match(uri)) {
		case USER_ID:
			id = uri.getPathSegments().get(2);
			count = db.update(UserInfo.TABLE_NAME, values, BasicColumns.ID
					+ "=?", new String[] { id });
			break;
		case STATUS_ID:
			id = uri.getPathSegments().get(2);
			count = db.update(StatusInfo.TABLE_NAME, values, BasicColumns.ID
					+ "=?", new String[] { id });
			// count = db.update(StatusInfo.TABLE_NAME, values,
			// StatusInfo.ID
			// + "="
			// + statusId
			// + (!TextUtils.isEmpty(where) ? " AND (" + where
			// + ')' : ""), whereArgs);
			break;
		case MESSAGE_ITEM:
			id = uri.getPathSegments().get(2);
			count = db.update(DirectMessageInfo.TABLE_NAME, values,
					BasicColumns.ID + "=?", new String[] { id });
			// count = db.update(
			// DirectMessageInfo.TABLE_NAME,
			// values,
			// DirectMessageInfo.ID
			// + "="
			// + messageId
			// + (!TextUtils.isEmpty(where) ? " AND (" + where
			// + ')' : ""), whereArgs);
			break;
		case MESSAGE_ID:
			_id = uri.getPathSegments().get(2);
			id = _id;
			count = db.update(DirectMessageInfo.TABLE_NAME, values,
					BaseColumns._ID + "=?", new String[] { _id });
			break;
		case USERS_ALL:
			id = "";
			count = db.update(UserInfo.TABLE_NAME, values, where, whereArgs);
			break;
		case USER_TYPE:
			id = "";
			count = db.update(UserInfo.TABLE_NAME, values, BasicColumns.TYPE
					+ "=?", new String[] { uri.getPathSegments().get(2) });
			break;
		case STATUSES_ALL:
			id = "";
			count = db.update(StatusInfo.TABLE_NAME, values, where, whereArgs);
			break;
		// case PUBLIC:
		// id = "";
		// count = db.update(StatusInfo.PUBLIC_TABLE_NAME, values, where,
		// whereArgs);
		// break;
		case MESSAGES_ALL:
			id = "";
			count = db.update(DirectMessageInfo.TABLE_NAME, values, where,
					whereArgs);
			break;
		case DRAFT_ALL:
		case DRAFT_ID:
			throw new UnsupportedOperationException(
					"unsupported update action: " + uri);
		default:
			throw new IllegalArgumentException("update() Unknown URI " + uri);
		}
		if (App.DEBUG) {
			if (count > 0) {
				log("update() result uri=" + uri + " id=" + id + " count="
						+ count);
			}
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	// 更新用户信息
	public static boolean updateUserInfo(Context context, User u) {
		if (u == null || u.isNull()) {
			return false;
		}
		Uri uri = Uri.parse(UserInfo.CONTENT_URI + "/id/" + u.id);
		int result = context.getContentResolver().update(uri,
				u.toSimpleContentValues(), null, null);
		if (App.DEBUG) {
			if (result > 0) {
				Log.d(TAG, "updateUserInfo id=" + u.id + " updated rows: "
						+ result + " uri=" + uri);
			}
		}
		return result > 0;
	}

	// 插入用户信息
	public static void insertUserInfo(Context context, User u) {
		if (u == null || u.isNull()) {
			return;
		}
		Uri uri = context.getContentResolver().insert(UserInfo.CONTENT_URI,
				u.toContentValues());
		if (App.DEBUG) {
			Log.d(TAG, "insertUserInfo uri=" + uri);
		}
	}

	public static int updateStatusProfileImageUrl(Context context, User u) {
		if (u == null || u.isNull()) {
			return -1;
		}
		ContentValues values = new ContentValues();
		values.put(StatusInfo.USER_PROFILE_IMAGE_URL, u.profileImageUrl);
		String where = StatusInfo.USER_ID + " =? ";
		String[] whereArgs = new String[] { u.id };
		int result = context.getContentResolver().update(
				StatusInfo.CONTENT_URI, values, where, whereArgs);
		if (App.DEBUG) {
			Log.d(TAG, "updateStatusProfileImageUrl id=" + u.id
					+ " updated rows: " + result);
		}
		return result;

	}

	public static Uri buildUriWithUserType(int type) {
		Uri uri = Uri.parse(UserInfo.CONTENT_URI + "/type/" + type);
		if (App.DEBUG) {
			Log.d(TAG, "buildUriWithUserType uri=" + uri);
		}
		return uri;
	}

	public static Uri buildUriWithUserId(String userId) {
		Uri uri = Uri.parse(UserInfo.CONTENT_URI + "/id/" + userId);
		if (App.DEBUG) {
			Log.d(TAG, "buildUriWithUserId uri=" + uri);
		}
		return uri;
	}

	public static Uri buildUriWithStatusId(String statusId) {
		Uri uri = Uri.parse(StatusInfo.CONTENT_URI + "/id/" + statusId);
		if (App.DEBUG) {
			Log.d(TAG, "buildUriWithStatusId uri=" + uri);
		}
		return uri;
	}

	public static Uri buildUriWithDirectMessageId(String msgId) {
		Uri uri = Uri.parse(DirectMessageInfo.CONTENT_URI + "/id/" + msgId);
		if (App.DEBUG) {
			Log.d(TAG, "buildUriWithDirectMessageId uri=" + uri);
		}
		return uri;
	}

}