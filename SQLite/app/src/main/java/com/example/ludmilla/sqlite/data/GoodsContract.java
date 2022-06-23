package com.example.ludmilla.sqlite.data;

import android.content.ContentResolver;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

/* мы задали final, чтобы этот класс не был типа extended, потому что этот класс будет для констант.
 Для этого внешнего класса мы не будем ничего расширять или внедрять */
public class GoodsContract {

    private GoodsContract() { }
    //Зададим константу для Content Authority, который является именем для провайдера контента
    // Имя основного пакета применяется, поскольку оно будет гарантировать уникальность имени на
    //на мобильном устройстве
    public static final String CONTENT_AUTHORITY = "com.example.ludmilla.sqlite";
    //Свяжем вместе константу Content Authority со схемой "content://" и
    // чтобы в дальнейшем использовать URI применим метод parse(), который вместо строки
    // возвратит сам URI
    //переменную BASE_CONTENT_URI другие приложения смогут использовать
    // чтобы обращаться к провайдеру контента
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    //возможный путь URI для данных с товарами "content://com.example.ludmilla.sqlite/goods"
    public static final String PATH_GOODS = "goods";
    //константа для Content URI

    /* Внутренний класс, который определяет содержимое таблицы */
    public static class GoodsEntry implements BaseColumns {

        /**
         *   MIME тип для провайдера контента списка товаров.
         */
        public static final String CONTENT_LIST_TYPE
                = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GOODS;

        /**
         * MIME тип провайдера контента для конкретного товара.
         */
        public static final String CONTENT_ITEM_TYPE
                = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GOODS;


        //константа для Content URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_GOODS);
        public static final String TABLE_NAME = "goods";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME_TITLE = "name";
        public static final String COLUMN_NAME_CATEGORY = "category";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_PRODUCER = "producer";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_COLOR = "color";
        public static final int CATEGORY_WASHING = 1;
        public static final int CATEGORY_STORING = 2;


        /* эта часть определяет различные категории товаров для выпадающего списка, будет использоваться в EditorActivity */
        public static final int CATEGORY_COVERING = 3;
        public static final int CATEGORY_LOCKS = 4;
        public static final int CATEGORY_LADDERS = 5;
        public static final int CATEGORY_TRUCKS = 6;
        public static final int CATEGORY_THERMOMETERS = 7;
        public static final int CATEGORY_DISHES = 8;
        public static final int CATEGORY_OTHER = 0;
        public static final int COLOR_GREEN = 1;
        public static final int COLOR_BLUE = 2;

        /* эта часть определяет различные цвета для выпадающего списка цветов, так что она также будет использвоаться в EditorActivity */
        public static final int COLOR_RED = 3;
        public static final int COLOR_BLACK = 4;
        public static final int COLOR_WHITE = 5;
        public static final int COLOR_YELLOW = 6;
        public static final int COLOR_PURPLE = 7;
        public static final int COLOR_ORANGE = 8;
        public static final int COLOR_OTHER = 0;


        //метод, который проверяет присутсвует ли для введения в таблицу БД одна из 9 заданных категорий товара
        public static boolean isValidCategory(int category) {
            if (category == CATEGORY_WASHING
                    || category == CATEGORY_STORING
                    || category == CATEGORY_COVERING
                    || category == CATEGORY_LOCKS
                    || category == CATEGORY_LADDERS
                    || category == CATEGORY_TRUCKS
                    || category == CATEGORY_THERMOMETERS
                    || category == CATEGORY_DISHES
                    || category == CATEGORY_OTHER) {

                return true;
            }
            return false;

        }

        public static boolean isValidColor(int color) {
            if (color == COLOR_GREEN
                    || color == COLOR_BLUE
                    || color == COLOR_RED
                    || color == COLOR_BLACK
                    || color == COLOR_WHITE
                    || color == COLOR_YELLOW
                    || color == COLOR_PURPLE
                    || color == COLOR_ORANGE
                    || color == COLOR_OTHER) {

                return true;
            }
            return false;

        }


    }
}





