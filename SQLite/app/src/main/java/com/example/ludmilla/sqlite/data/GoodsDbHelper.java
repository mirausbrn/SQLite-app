package com.example.ludmilla.sqlite.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.ludmilla.sqlite.data.GoodsContract.GoodsEntry;

/**
 * DatabaseHelper для приложения SQLite. Необходим для создания базы данных и её управления
 */
public class GoodsDbHelper extends SQLiteOpenHelper {


    public static final String LOG_TAG = GoodsDbHelper.class.getSimpleName();

    // Здесь мы обозначили название нашей базы данных store.db

    private static final String DATABASE_NAME = "store.db";

    // При изменении схемы БД, изменяется версия базы данных

    private static final int DATABASE_VERSION = 1;

    // Создаём новый экземпляр GoodsDBHelper
    public GoodsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //для создания базы данных в первый раз, задаём схему БД
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + GoodsEntry.TABLE_NAME + " ("
                + GoodsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + GoodsEntry.COLUMN_NAME_TITLE + " TEXT NOT NULL, "
                + GoodsEntry.COLUMN_NAME_CATEGORY + " INTEGER NOT NULL, "
                + GoodsEntry.COLUMN_NAME_TYPE + " TEXT NOT NULL, "
                + GoodsEntry.COLUMN_NAME_PRODUCER + " TEXT, "
                + GoodsEntry.COLUMN_NAME_COLOR + " INTEGER NOT NULL, "
                + GoodsEntry.COLUMN_NAME_PRICE + " INTEGER DEFAULT 0);");


    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Если база данных в версии 1, здесь оставляем без изменений.
    }
}










