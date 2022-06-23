package com.example.ludmilla.sqlite.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.example.ludmilla.sqlite.data.GoodsContract.GoodsEntry;

import android.net.Uri;
import android.util.Log;

public class GoodsProvider extends ContentProvider {

    /**
     * Тег для сообщений от системы
     */
    public static final String LOG_TAG = GoodsProvider.class.getSimpleName();
    /**
     * Код URI matcher для таблицы goods
     */
    private static final int GOODS = 1000;
    /**
     * Код URI matcher для строки таблицы goods
     */
    private static final int GOODS_ID = 1001;
    /**
     * Объект UriMatcher чтобы сопоставить контент URI к соответствующему коду.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(GoodsContract.CONTENT_AUTHORITY, GoodsContract.PATH_GOODS, GOODS);
        sUriMatcher.addURI(GoodsContract.CONTENT_AUTHORITY, GoodsContract.PATH_GOODS + "/#", GOODS_ID);
    }
    private GoodsDbHelper mDbHelper;

    /**
     * Зададим провайдёр и объект helper в базе данных.
     */
    @Override
    public boolean onCreate() {

        mDbHelper = new GoodsDbHelper(getContext());
        return true;
    }

    /**
     * Представляет запрос для заданного URI. Использует параметр "projection"(т.е. проекцию), "selection" (отбор) и другие.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Получаем базу данных для чтения
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // Этот курсор будет содержать результат метода запроса query
        Cursor cursor;

        // Проверка соответствует UriMatcher какому-либо коду из существующих вариантов
        int match = sUriMatcher.match(uri);
        switch (match) {
            case GOODS:
                // Этот случай для таблицы с товарами напрямую, с использованием:
                // "projection", "selection", "selection arguments", и "sort order". Курсор
                // может содержать множество строк в таблице
                cursor = database.query(GoodsEntry.TABLE_NAME, projection,
                        selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case GOODS_ID:
                // для кода GOODS_ID, извлекается первичный ключ из URI.
                //Например, если URI представляет собой:
                // "content://com.example.ludmilla.sqlite/goods/7",
                // процесс отбора ("selection" будет "_id=?"
                // и аргумент отбора т.е "selection argument"
                //  будет строковый массив содержащий ID = 7 в таком случае.
                //
                // Для каждого знака "?" при отборе, необходим элемент в отобранных
                // аргументах, который будет заполнять знак вопроса.
                // Поскольку у нас пока только один такой знак при отборе, будет одна строка
                // в строковом массиве аргументов отбора.
                selection = GoodsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // Здесь мы формируем запрос на строку с _id равным 7 чтобы вернуть
                // курсор, содержащий эту строку в таблице
                cursor = database.query(GoodsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Невозможно запросить неизвестный URI " + uri);
        }
        //Устанавливает уведомления URI для курсора
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;

    }

    /**
     * Добавляет данные в провайдер с заданными значениями контента (ContentValues) .
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        //проверяем есть ли совпадение с заданными вариантами пути uri
        final int match = sUriMatcher.match(uri);
        switch (match) {
            //Здесь только один случай и обращение ко всей таблице, поскольку нет необходимости
            //в случае с отдельной строкой таблицы
            case GOODS:
                //вызываем метод-помощник insertGoods для удобности чтения кода
                return insertGoods(uri, contentValues);
            //Если не будет какого-либо совпадения выпадет сообщение
            default:
                throw new IllegalArgumentException("Ввод не поддерживается для " + uri);
        }


    }

    /**
     * Введём товар в базу данных с заданными значениями контента (content values) .
     * Возвращает новый content URI для конкретной строке в базе данных.
     */
    private Uri insertGoods(Uri uri, ContentValues values) {

        //Начало проверки, что имя ненулевое
        //В зависимости от типа данных применяем getAs..() метод

        String name = values.getAsString(GoodsEntry.COLUMN_NAME_TITLE);
        String type = values.getAsString(GoodsEntry.COLUMN_NAME_TYPE);
        Integer price = values.getAsInteger(GoodsEntry.COLUMN_NAME_PRICE);
        Integer category = values.getAsInteger(GoodsEntry.COLUMN_NAME_CATEGORY);
        Integer color = values.getAsInteger(GoodsEntry.COLUMN_NAME_COLOR);

        //организуем проверку для имени
        //Вообще не всегда поля обязательно могут быть заполнены пользователем, но предполагаем
        //имя обязательно должно быть непустым
        if (name == null) {
            //выпадет ошибку, если имя не заполнено
            throw new IllegalArgumentException("Необходимо ввести имя товара");
        }

        /* Проверка для типа товара */

        if (type == null) {
            //выпадет ошибку, если имя не заполнено
            throw new IllegalArgumentException("Введите тип товара");
        }

        /* Проверка для категории товара */
        if (category == null || !GoodsEntry.isValidCategory(category)) {
            //выпадет ошибку, если имя не заполнено
            throw new IllegalArgumentException("Требуется категория товара");
        }


        /* Проверка для цвета */

        if (category == null || !GoodsEntry.isValidColor(color)) {
            //выпадет ошибку, если имя не заполнено
            throw new IllegalArgumentException("Требуется категория товара");
        }

        /* Проверка для цены товара */
        if (price != null && price < 0) {
            //выпадет ошибку, если имя не заполнено
            throw new IllegalArgumentException("Введите допустимую стоимость товара");
        }


        //  Введём новый товар в таблицу базы данных магазина с заданными  ContentValues
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        //Зная ID в таблице, возвращаем новый URI с ID, которое метод withAppendId() добавляет
        // в самый конец
        long id = database.insert(GoodsEntry.TABLE_NAME, null, values);
        //Проверяем получилось ли ввести новую строку
        if (id == -1) {
            //выводим сообщение в журнале ошибок, в случае ошибки
            Log.e(LOG_TAG, "Не удалось ввести новую строку для " + uri);

            return null;
        }

        //уведомляет о том, что произошли изменения данных для Uri контента товара
        // uri:content://com.example.ludmilla.sqlite/goods
        getContext().getContentResolver().notifyChange(uri, null);
        //при успешном добавлении строки в таблицу, добавление id к URI
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Обновляет данные полученные в результате отбора and отбора аргументов (selectionArgs), c уже новыми значениями "ContentValues".
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GOODS:
                return updateGoods(uri, contentValues, selection, selectionArgs);
            case GOODS_ID:
                // Для кода GOODS_ID, извлекаем id из Uri,
                // тогда мы знаем, что можем обновлять. Отбором будет "_id=?" и selectionArgs
                // будут строковым массивом содержащим сам ID.
                selection = GoodsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateGoods(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);

        }
    }

    private int updateGoods(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        //Начало проверки, что имя ненулевое
        //В зависимости от типа данных применяем getAs..() метод
        //организуем проверку для имени
        //Вообще не всегда поля обязательно могут быть заполнены пользователем, но предполагаем
        //имя обязательно должно быть непустым
        if (values.containsKey(GoodsEntry.COLUMN_NAME_TITLE)) {
            String name = values.getAsString(GoodsEntry.COLUMN_NAME_TITLE);
            if (name == null) {
                //выпадет ошибку, если имя не заполнено
                throw new IllegalArgumentException("Необходимо ввести имя товара");
            }
        }

        /* Проверка для типа товара */
        if (values.containsKey(GoodsEntry.COLUMN_NAME_TYPE)) {
            String type = values.getAsString(GoodsEntry.COLUMN_NAME_TYPE);
            if (type == null) {
                //выпадет ошибку, если имя не заполнено
                throw new IllegalArgumentException("Введите тип товара");
            }
        }

        /* Проверка для категории товара */
        if (values.containsKey(GoodsEntry.COLUMN_NAME_CATEGORY)) {
            Integer category = values.getAsInteger(GoodsEntry.COLUMN_NAME_CATEGORY);
            if (category == null || !GoodsEntry.isValidCategory(category)) {
                //выпадет ошибку, если имя не заполнено
                throw new IllegalArgumentException("Требуется категория товара");
            }
        }


        /* Проверка для цвета */
        if (values.containsKey(GoodsEntry.COLUMN_NAME_COLOR)) {
            Integer color = values.getAsInteger(GoodsEntry.COLUMN_NAME_COLOR);
            if (color == null || !GoodsEntry.isValidColor(color)) {
                //выпадет ошибку, если имя не заполнено
                throw new IllegalArgumentException("Требуется цвет товара");
            }

        }

        if (values.containsKey(GoodsEntry.COLUMN_NAME_PRICE)) {
            Integer price = values.getAsInteger(GoodsEntry.COLUMN_NAME_PRICE);
            /* Проверка для цены товара */
            if (price != null && price < 0) {
                //выпадет ошибку, если имя не заполнено
                throw new IllegalArgumentException("Введите допустимую стоимость товара");
            }
        }

        if (values.size() == 0) {
            return 0;
        }


        //если нет никаких данных для внесения изменений, то пробуем не обновлять БД

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(GoodsEntry.TABLE_NAME, values, selection, selectionArgs);

        // если одна или более строк были изменены, тогда будут уведомлены listeners что данные
        // по этим URI изменились
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Возвращает число изменённых строк
        return rowsUpdated;
    }

    /**
     * Удаляет данные от отбора
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // Получение записываемой базы данных
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GOODS:
                // Удаление всех строк, которые соотвествуют отбору selection и selectionArgs
                rowsDeleted = database.delete(GoodsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case GOODS_ID:
                // Удаление строки по ID в URI
                selection = GoodsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(GoodsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Удаление не поддерживается для " + uri);

        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Возвращает MIME типы данных для контента URI.
     */
    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GOODS:
                return GoodsEntry.CONTENT_LIST_TYPE;
            case GOODS_ID:
                return GoodsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Неизвестный URI " + uri + " с совпадением " + match);

        }
    }
}







