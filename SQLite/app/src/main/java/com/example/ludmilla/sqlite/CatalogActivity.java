package com.example.ludmilla.sqlite;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.ludmilla.sqlite.data.GoodsContract.GoodsEntry;

 /**
 * Отображает списки вещей, которые были введены и хранятся в базу данных нашего приложения
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
 //Задаём константу для загрузчика и присваиваем её ID=0(может быть любым)
     private static final int GOODS_LOADER = 0;
     GoodsCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalog_activity);

        // Устанавливает кнопку, для запуска нашей активности EditorActivity, то есть активности редактирования
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        //Находим ListView, который будет наполняться данными
        ListView goodsListView = (ListView) findViewById(R.id.listView);
        //Находим и устанавливаем пустой вид (emptyView) в ListView, так что он отображается только
        //когда товаров в таблице 0 и ещё ничего не добавлено
        View emptyView = findViewById(R.id.empty_view);
        goodsListView.setEmptyView(emptyView);
        mCursorAdapter = new GoodsCursorAdapter(this,null);
        goodsListView.setAdapter(mCursorAdapter);

        goodsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri currentGoodsUri = ContentUris.withAppendedId(GoodsEntry.CONTENT_URI, id);
                intent.setData(currentGoodsUri);
                startActivity(intent);
            }
        });

        // задаём загрузчик
        getLoaderManager().initLoader(GOODS_LOADER, null, this);
    }
    /**
     * Метод для помощника Helper, с помощью которого мы напрямую вводим в базу данных строку. Используется исключительно для отладки и изучения
     */
    private void insertGoods() {

        //Создаёт так называемый объект ContentValues, в которой имена колонок ассоциированы с ключами
        ContentValues values = new ContentValues();
        //вводим значения для каждой колонки в новой строке
        values.put(GoodsEntry.COLUMN_NAME_TITLE, "Новый товар");
        values.put(GoodsEntry.COLUMN_NAME_CATEGORY, GoodsEntry.CATEGORY_OTHER);
        values.put(GoodsEntry.COLUMN_NAME_TYPE, "вещь");
        values.put(GoodsEntry.COLUMN_NAME_PRODUCER, "Новый производитель");
        values.put(GoodsEntry.COLUMN_NAME_COLOR, GoodsEntry.COLOR_OTHER);
        values.put(GoodsEntry.COLUMN_NAME_PRICE, 100);
        // Введение новой строки, возвращает первичный ключ новой строки
        // Первый параметр это имя нового товара.
        // Второй параметр - категория товара, которую мы задали в GoodsContract
        // В том случае, если было указано NOT NULL и значения не будут указаны, будет
        // невозможно ввести в строку пустое значение
        // Третий параметр это тип товара
        // Четрвёртый - производитель товара
        // Пятый - цвет товара
        // Шестой - стоимость товара
        // Вводим новую строку для товара в провайдер с использованием ContentResolver
        // Получаем новый Uri, который в дальнейшем повзолит нам обращаться к "Новому товару"
        Uri newUri = getContentResolver().insert(GoodsEntry.CONTENT_URI, values);
    }

    private void deleteAllGoods(){
        //С помощью getContentResolver() удаляем ту часть которая отвечает за URI всей таблицы
        int rowsDeleted = getContentResolver().delete(GoodsEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " Строк удалено из базы данных");

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Служит для наполнения опций меню из файла res/menu/menu_catalog.xml.
        // Добавляет элементы меню к верхней панели приложения
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }


     @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Пользователь нажимает на опцию меню из выпадающего меню верхней панели
        switch (item.getItemId()) {
            // Отклик программы на нажатие опции "Ввести псевдоданные"
            case R.id.action_insert_dummy_data:
                insertGoods();
                return true;
            // Отклик на нажатие "Удалить все товары"
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



     @Override
     public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Укажем проекцию, которая определяет какие колонки из таблицы нам нужны для вывода пользователю
         //При этом ID будет необходимо для адаптера курсора, но отображать мы его не будем
         String[] projection = {
            GoodsEntry._ID,
            GoodsEntry.COLUMN_NAME_TITLE,
            GoodsEntry.COLUMN_NAME_TYPE,
            GoodsEntry.COLUMN_NAME_PRODUCER,
            GoodsEntry.COLUMN_NAME_PRICE };

         return new CursorLoader( this, //контекст родительской активности
                 GoodsEntry.CONTENT_URI, // URI провайдера контента для запроса
                 projection, //Колонки, которые будут включены в результирующий курсор
                 null, //без отбора
                 null, // без отбора аргументов
                 null); // Сортировка по умолчанию


     }

     @Override
     public void onLoadFinished( Loader<Cursor> loader, Cursor data) {

    // Обновление адаптера курсора GoodsCursorAdapter с новым курсором, содержащим обновленнные данные о товарах
         mCursorAdapter.swapCursor(data);


     }

         @Override
     //Создание нового курсора
     public void onLoaderReset( Loader<Cursor> loader) {
         mCursorAdapter.swapCursor(null);
     }


     private void showDeleteConfirmationDialog() {
         // Создание предупреждающего диалогового с помощью Builder окна AlertDialog.Builder и установление сообщения и объектов "click listener"
         // для кнопок "Удалить" и "Закрыть" в диалоговом окне.
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.delete_all_dialog_msg);
         builder.setPositiveButton(R.string.action_delete_all_entries, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 // Пользователь нажимает на кнопку "Удалить" для удаления товара
                 deleteAllGoods();
             }
         });
         builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 //Пользователь нажимает на кнопку"Закрыть" чтобы закрыть диалоговое окно.
                 if (dialog != null) {
                     dialog.dismiss();
                 }
             }
         });

         // Создание и пока предупреждающего диалогового окна т.е. AlertDialog
         AlertDialog alertDialog = builder.create();
         alertDialog.show();
     }
 }
