package com.example.biscuit.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BiscuitDB";
    private static final int DATABASE_VERSION = 1;

    private static final String USER_TABLE = "_user";
    private static final String ID_COLUMN = "id";
    private static final String EMAIL_COLUMN = "email";
    private static final String PASSWORD_COLUMN = "password";
    private static final String ENABLED_COLUMN = "enabled";
    
    private static final String TOKEN_TABLE = "token";
    private static final String TOKEN_COLUMN = "token";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + USER_TABLE + "(" + ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT," + EMAIL_COLUMN + "TEXT UNIQUE," + PASSWORD_COLUMN + " TEXT," + ENABLED_COLUMN + " BOOLEAN)" );
        register("user","password");

        db.execSQL("CREATE TABLE " + TOKEN_TABLE + "(" + ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT," + TOKEN_COLUMN + "TEXT," + EMAIL_COLUMN + " INTEGER)" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        onCreate(db);
    }

    public boolean register(String email, String password){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(EMAIL_COLUMN, email);
        contentValues.put(PASSWORD_COLUMN, password);
        contentValues.put(ENABLED_COLUMN, false);

        long result = db.insert(USER_TABLE, null, contentValues);
        Log.d("DB", "Insert result = " + result);
        return result != -1;
    }

    public boolean login(String email, String password){
        SQLiteDatabase db = this.getReadableDatabase();
        //rawQuery returns data, execSQL does not.
        // a cursor is a pointer that allows you to read rows returned from a SQL query. (a Row type)
        Cursor cursor = db.rawQuery("SELECT * FROM " + USER_TABLE + " WHERE " +
                        EMAIL_COLUMN + "=? AND " + PASSWORD_COLUMN + "=?",
                new String[]{email, password});
        boolean result = cursor.getCount() > 0;
        cursor.close();

        return result;
    }

    public boolean update(String oldEmail,String newEmail, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("email", newEmail);
        cv.put("password", newPassword);

        int result = db.update(USER_TABLE, cv, EMAIL_COLUMN + " = ?", new String[]{oldEmail});

        return result > 0;  // if at least 1 row updated → true
    }

    public boolean saveCode(String code, String email){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TOKEN_COLUMN, code);
        contentValues.put(EMAIL_COLUMN, email);

        long result = db.insert(TOKEN_TABLE, null, contentValues);
        Log.d("DB", "Insert token = " +code + "result = " + result);
        return result != -1;
    }

    public boolean isTokenValid(String code, String email){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TOKEN_TABLE + " WHERE " +
                        TOKEN_COLUMN + "=? AND " + EMAIL_COLUMN + "=?",
                new String[]{code, email});
        boolean result = cursor.getCount() > 0;
        cursor.close();

        return result;
    }

    public boolean validateEmail(String email){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("enabled", true);
        int result = db.update(USER_TABLE, cv, EMAIL_COLUMN + " = ?", new String[]{email});
        return result > 0;
    }

}