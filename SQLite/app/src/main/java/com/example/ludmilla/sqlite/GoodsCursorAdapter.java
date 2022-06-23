package com.example.ludmilla.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.ludmilla.sqlite.data.GoodsContract.GoodsEntry;

    /**
     * GoodsCursorAdapter - это адаптер для списка ( в нашем случае списка товаров) или для grid view
     * который использует курсор с данными о товаре как собственный источник данных. Этот адаптер
     * знает как создавать элементы списка товаров т.е. list item для каждой строки из данных о товарах
     * содержащихся в курсоре.
     */
    public class GoodsCursorAdapter extends CursorAdapter {

        /**
         * Формирует новый cursor adapter .
         *
         * @param context контекст
         * @param c       Курсор от которого получаем данные.
         */
        public GoodsCursorAdapter(Context context, Cursor c) {
            super(context, c, 0 /* flags */);
        }

        /**
         * Создаёт вид со списком пустых элементов. И к видам никакие данные ещё не привязаны
         *
         * @param context контекст приложения
         * @param cursor  Курсор от которого получаем данные. Курсор уже
         *                перемещён на нужную позицию.
         * @param parent  Родитель к которому привязан новый вид
         * чтобы вернуть новый созданный вид со списком элементов.
         */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        }

        /**
         * Этот метод связывает данные о товарах (в конкретной строке указанной курсором)предложенному
         * расположению для элемента из списка т.е. тому, который задали в list_item.xml
         * Например, имя конкретного товара может устанавливаться в TextView созданного для имени товара
         * в list_item.xml.
         *
         * @param view    Existing view, returned earlier by newView() method
         * @param context app context
         * @param cursor  The cursor from which to get the data. The cursor is already moved to the
         *                correct row.
         */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Находит поля чтобы заполнить наложенный шаблон
            TextView tvName = (TextView) view.findViewById(R.id.name);
            TextView tvTitle = (TextView) view.findViewById(R.id.title);
            TextView tvProducer = (TextView) view.findViewById(R.id.producer);
            TextView tvPrice = (TextView) view.findViewById(R.id.price);

            // Извлекаем свойства из курсора
            String name = cursor.getString(cursor.getColumnIndexOrThrow(GoodsEntry.COLUMN_NAME_TITLE));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(GoodsEntry.COLUMN_NAME_TYPE));
            String producer = cursor.getString(cursor.getColumnIndexOrThrow(GoodsEntry.COLUMN_NAME_PRODUCER));
            int price = cursor.getInt(cursor.getColumnIndexOrThrow(GoodsEntry.COLUMN_NAME_PRICE));

            // Заполняет поля с извлечёнными свойствами
            tvName.setText(name);
            tvTitle.setText(title);
            tvProducer.setText(producer);
            tvPrice.setText(String.valueOf(price));

        }
    }

