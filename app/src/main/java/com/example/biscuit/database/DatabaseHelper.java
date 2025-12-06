package com.example.biscuit.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BiscuitDB";
    private static final int DATABASE_VERSION = 2;

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
        // Create the user table
        db.execSQL("CREATE TABLE " + USER_TABLE + "(" +
                ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                EMAIL_COLUMN + " TEXT UNIQUE," +
                PASSWORD_COLUMN + " TEXT," +
                FIRST_NAME_COLUMN + " TEXT," +
                LAST_NAME_COLUMN + " TEXT)" );

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
        contentValues.put(FIRST_NAME_COLUMN, firstName);
        contentValues.put(LAST_NAME_COLUMN, lastName);

        long result = db.insert(USER_TABLE, null, contentValues);
        Log.d("DB", "Insert result = " + result);
        return result != -1;
    }

    // Method to check login credentials
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

    // Method to get user's full name
    public String getUserName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String fullName = "User"; // Default

        Cursor cursor = db.rawQuery("SELECT " + FIRST_NAME_COLUMN + ", " + LAST_NAME_COLUMN +
                " FROM " + USER_TABLE + " WHERE " + EMAIL_COLUMN + "=?", new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            String first = cursor.getString(0);
            String last = cursor.getString(1);

            if (first != null && !first.isEmpty()) {
                fullName = first;
                if (last != null && !last.isEmpty()) {
                    fullName += " " + last;
                }
            }
            cursor.close();
        }
        return fullName;
    }

    // Method to get user's first name
    public String getFirstName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String firstName = "";
        Cursor cursor = db.rawQuery("SELECT " + FIRST_NAME_COLUMN + " FROM " + USER_TABLE + " WHERE " + EMAIL_COLUMN + "=?", new String[]{email});
        if (cursor != null && cursor.moveToFirst()) {
            firstName = cursor.getString(0);
            cursor.close();
        }
        return firstName;
    }

    // Method to get user's last name
    public String getLastName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String lastName = "";
        Cursor cursor = db.rawQuery("SELECT " + LAST_NAME_COLUMN + " FROM " + USER_TABLE + " WHERE " + EMAIL_COLUMN + "=?", new String[]{email});
        if (cursor != null && cursor.moveToFirst()) {
            lastName = cursor.getString(0);
            cursor.close();
        }
        return lastName;
    }

    public boolean update(String oldEmail, String newEmail, String newPassword, String newFirstName, String newLastName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(EMAIL_COLUMN, newEmail);
        cv.put(PASSWORD_COLUMN, newPassword);
        cv.put(FIRST_NAME_COLUMN, newFirstName);
        cv.put(LAST_NAME_COLUMN, newLastName);

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

