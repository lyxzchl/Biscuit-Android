package com.example.biscuit.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BiscuitDB";
    private static final int DATABASE_VERSION = 1;

    private static final String USER_TABLE = "_user";
    private static final String ID_COLUMN = "id";
    private static final String EMAIL_COLUMN = "email";
    private static final String PASSWORD_COLUMN = "password";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the user table
        db.execSQL("CREATE TABLE " + USER_TABLE + "(" + 
                ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
                EMAIL_COLUMN + " TEXT UNIQUE," + 
                PASSWORD_COLUMN + " TEXT)" );
        
        // Add a default user for testing
        ContentValues contentValues = new ContentValues();
        contentValues.put(EMAIL_COLUMN, "user");
        contentValues.put(PASSWORD_COLUMN, "password");
        db.insert(USER_TABLE, null, contentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        onCreate(db);
    }

    // Method to register a new user
    public boolean register(String email, String password){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(EMAIL_COLUMN, email);
        contentValues.put(PASSWORD_COLUMN, password);

        long result = db.insert(USER_TABLE, null, contentValues);
        return result != -1;
    }

    // Method to check login credentials
    public boolean login(String email, String password){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + USER_TABLE + " WHERE " +
                        EMAIL_COLUMN + "=? AND " + PASSWORD_COLUMN + "=?",
                new String[]{email, password});
        boolean result = cursor.getCount() > 0;
        cursor.close();

        return result;
    }
}
