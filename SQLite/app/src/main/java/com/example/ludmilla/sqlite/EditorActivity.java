package com.example.ludmilla.sqlite;


import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.ludmilla.sqlite.data.GoodsContract.GoodsEntry;

/**
 * Позволяет пользователю добавить новую вещь в бд, заполнить данные о ней или изменить некоторые данные в базе
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {


    private static final int EXISTING_GOOD_LOADER = 0;

    private Uri mCurrentGoodsUri;
    /**
     * EditText поле для того, чтобы ввести имя товара
     */
    private EditText mNameEditText;

    /**
     * EditText поле, чтобы ввести тип товара
     */
    private EditText mTypeEditText;

    /**
     * EditText поле, чтобы указать стоимость товара
     */
    private EditText mPriceEditText;

    /**
     * EditText поле, чтобы указать производителя товара
     */
    private EditText mProducerEditText;

    /**
     * Spinner, служить чтобы выбрать категорию товара из выпадающего списка, например нашу категорию "Посуда"
     */
    private Spinner mCategorySpinner;

    /**
     * Категория товара. Варианты для выбора:
     * 0 для категории "другое",
     * 1 для стирки и уборки,
     * 2 для хранения вещей (ящики, коробки, упаковка),
     * 3 для напольных покрытий (ковриков),
     * 4 для скобяных изделий и замков,
     * 5 для стремянок,
     * 6 для тележек хозяйственных,
     * 7 для термометров бытовых,
     * 8 для посуды. Мы заменим цифры, на названия из перечня GoodsContract, чтобы было удобнее читать
     */

    /**
     * Spinner, этот служит чтобы выбрать цвето товара, также из выпадающего списка. Например, цвет "зелёный"
     */
    private Spinner mColorSpinner;

    /**
     * Цвет товара. Варианты для выбора:
     * 0 для цвета "другое",
     * 1 для зелёного,
     * 2 для синего,
     * 3 для красного
     * 4 для чёрного,
     * 5 для белого,
     * 6 для жёлтого
     * 7 для фиолетового,
     * 8 для оранжевого. Мы заменим цифры, на названия из перечня GoodsContract, чтобы было удобнее читать
     */
    private int mColor = GoodsEntry.COLOR_OTHER;
    private int mCategory = GoodsEntry.CATEGORY_OTHER;
    //переменная для режима редактора, для определения сделаны ли изменения в форме
    private boolean mGoodsHasChanged = false;


    /**
     * OnTouchListener который "слушает" т.е. отслеживает любые косания на View(Вид), подразумевая, редактируется
     * вид, и таким образом изменяется mGoodsHasChanged boolean на true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mGoodsHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_activity);

        Intent intent = getIntent();
        mCurrentGoodsUri = intent.getData();

        // Если интент НЕ содержит URI конетнта товара, тогда узнаём,
        // что создаём новый товар
        if (mCurrentGoodsUri == null) {
            // Это новый товар, поэтому верхний бар содержит название "Добавить товар"
            setTitle(getString(R.string.editor_activity_title_new_good));

            // Аннулируем опции меню, чтобы опция "Удалить" была скрыта ведь товар не был добавлен.
            invalidateOptionsMenu();
        } else {
            // Иначе товар редактируется, и меняется название бара на "Редактировать товар"
            setTitle(getString(R.string.editor_activity_title_edit_good));

            getLoaderManager().initLoader(EXISTING_GOOD_LOADER, null, this);
        }


        // Соотносим все необходимые представления или view, из которых мы будем считывать вводимые пользователем данные.
        mNameEditText = (EditText) findViewById(R.id.edit_good_name);
        mTypeEditText = (EditText) findViewById(R.id.edit_good_type);
        mPriceEditText = (EditText) findViewById(R.id.edit_for_price);
        mProducerEditText = (EditText) findViewById(R.id.edit_for_producer);
        mColorSpinner = (Spinner) findViewById(R.id.spinner_color);
        mCategorySpinner = (Spinner) findViewById(R.id.spinner_category);

        // Устанавливаем OnTouchListener для всех вводимых полей, так что мы можем определить нажимал ли пользователь
        // или изменял их. Это позволит узнать, были ли сделаны несохранённые изменения
        // или нет, когда пользователь пытается выйти из режима редактирования.
        mNameEditText.setOnTouchListener(mTouchListener);
        mTypeEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mProducerEditText.setOnTouchListener(mTouchListener);
        mCategorySpinner.setOnTouchListener(mTouchListener);
        mColorSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();

    }

    /**
     * Настроим выпадающий перечень, который позволит пользователю выбирать какой цвет и категорию товара выбрать.
     */
    private void setupSpinner() {
        // Создадим адаптер для выпадающего списка Spinner. Список опций берётся из строкового массива, который мы заранее задали и поместили в res\values\array.xml
        // расположение выпадающего списка для цветов задали также в файле editor_activity
        ArrayAdapter colorSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_color_options, android.R.layout.simple_spinner_item);

        // Задали стиль выпадающего списка, чтобы он выпадал вниз - это простое представление списка с одним словом в каждой строке
        colorSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Применим адаптер к выпадающему списку
        mColorSpinner.setAdapter(colorSpinnerAdapter);

        // Зададим число типа mSelected на наши константы, в данном случае цвета
        mColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.color_green))) {
                        mColor = GoodsEntry.COLOR_GREEN; // зелёный
                    } else if (selection.equals(getString(R.string.color_blue))) {
                        mColor = GoodsEntry.COLOR_BLUE; //синий
                    } else if (selection.equals(getString(R.string.color_red))) {
                        mColor = GoodsEntry.COLOR_RED; // красный
                    } else if (selection.equals(getString(R.string.color_black))) {
                        mColor = GoodsEntry.COLOR_BLACK; // чёрный
                    } else if (selection.equals(getString(R.string.color_white))) {
                        mColor = GoodsEntry.COLOR_WHITE; // белый
                    } else if (selection.equals(getString(R.string.color_yellow))) {
                        mColor = GoodsEntry.COLOR_YELLOW; // жёлтый
                    } else if (selection.equals(getString(R.string.color_purple))) {
                        mColor = GoodsEntry.COLOR_PURPLE; // фиолетовый
                    } else if (selection.equals(getString(R.string.color_orange))) {
                        mColor = GoodsEntry.COLOR_ORANGE; // оранжевый
                    } else {
                        mColor = GoodsEntry.COLOR_OTHER; // другой
                    }
                }
            }

            // Поскольку AdapterView является абстрактным классом, должна быть определена опция, когда ничего не выбрано т.е. nNothingSelected
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mColor = GoodsEntry.COLOR_OTHER; // Other
            }
        });

        // Создадим адаптер для выпадающего списка Spinner. Список опций берётся из строкового массива, который мы заранее задали и поместили в res\values\array.xml
        // расположение выпадающего списка для категорий также задали в файле editor_activity
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_category_options, android.R.layout.simple_spinner_item);
        // Задали стиль выпадающего списка, чтобы он выпадал вниз - это простое представление списка с одним словом в каждой строке
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        // Применим адаптер к выпадающему списку
        mCategorySpinner.setAdapter(categorySpinnerAdapter);
        // Зададим число типа mSelected на наши константы, в данном случае категории товаров
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.category_goods_washing))) {
                        mCategory = GoodsEntry.CATEGORY_WASHING; // Стирка и уборка
                    } else if (selection.equals(getString(R.string.category_goods_storing))) {
                        mCategory = GoodsEntry.CATEGORY_STORING; // Хранение вещей (ящики, коробки, упаковка)
                    } else if (selection.equals(getString(R.string.category_goods_covering))) {
                        mCategory = GoodsEntry.CATEGORY_COVERING; // Напольные покрытия (коврики)
                    } else if (selection.equals(getString(R.string.category_goods_locks))) {
                        mCategory = GoodsEntry.CATEGORY_LOCKS; // Скобяные изделия и замки
                    } else if (selection.equals(getString(R.string.category_goods_ladders))) {
                        mCategory = GoodsEntry.CATEGORY_LADDERS; // Стремянки
                    } else if (selection.equals(getString(R.string.category_goods_trucks))) {
                        mCategory = GoodsEntry.CATEGORY_TRUCKS; // Тележки хозяйственные
                    } else if (selection.equals(getString(R.string.category_goods_thermometers))) {
                        mCategory = GoodsEntry.CATEGORY_THERMOMETERS; // Термометры бытовые
                    } else if (selection.equals(getString(R.string.category_goods_dishes))) {
                        mCategory = GoodsEntry.CATEGORY_DISHES; // Посуда
                    } else {
                        mCategory = GoodsEntry.CATEGORY_OTHER; // Другое
                    }
                }
            }

            // Поскольку AdapterView является абстрактным классом, должна быть определена опция, когда ничего не выбрано т.е. nNothingSelected
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCategory = GoodsEntry.CATEGORY_OTHER; // Другое
            }
        });

    }

    /**
     * Метод, который будет отвечать за ввод данных с режима редактирования в таблицу и сохранять товар
     */

    private void saveGoods() {

        //Создаёт так называемый объект ContentValues, в которой имена колонок ассоциированы с ключами

        String nameString = mNameEditText.getText().toString().trim();
        String typeString = mTypeEditText.getText().toString().trim();
        String producerString = mProducerEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        if (mCurrentGoodsUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(typeString) &&
                TextUtils.isEmpty(producerString) && mColor == GoodsEntry.COLOR_OTHER && mCategory == GoodsEntry.CATEGORY_OTHER) {
            // Если никакие поля не были изменены, возвращение без создания нового товара в БД.
            // Тогда нет необходимости создавать ContentValues или производить какие-либо операций с провайдером контента.
            return;
        }

        //Соотносим вводимые колонки в таблице с вводимыми значениями EditorActivity
        ContentValues values = new ContentValues();
        values.put(GoodsEntry.COLUMN_NAME_TITLE, nameString);
        values.put(GoodsEntry.COLUMN_NAME_CATEGORY, mCategory);
        values.put(GoodsEntry.COLUMN_NAME_TYPE, typeString);
        values.put(GoodsEntry.COLUMN_NAME_PRODUCER, producerString);
        values.put(GoodsEntry.COLUMN_NAME_COLOR, mColor);

        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {

            price = Integer.parseInt(priceString);

        }

        values.put(GoodsEntry.COLUMN_NAME_PRICE, price);

        // Определяем новый ли это товар или существующий, проверяя, если mCurrentGoodsUri нулевой или нет
        if (mCurrentGoodsUri == null) {
            //Это новый товар, поэтому создается новый товар в провайдере,
            // возвращающий URI контента для нового товара.
            Uri newUri = getContentResolver().insert(GoodsEntry.CONTENT_URI, values);

            //В зависимости от того является ли контент Uri нулевым или нет будет выведено сообщение
            if (newUri == null) {
                //При ошибке сохранения
                Toast.makeText(this, getString(R.string.editor_insert_message_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                //При успешном вводе новой строки
                Toast.makeText(this, getString(R.string.editor_insert_message_success),
                        Toast.LENGTH_SHORT).show();
            }

        } else {

            // в другом случае имеем дело с существующим товаром, так что обновляем товар с URI контента: mCurrentGoodsUri
            // и передаем в новые значения контента ContentValues. Передаём null для отбора "selection" и отбора аргументов "selection args"
            // потому что mCurrentGoodsUri уже определит верную строку в базе данных, которую
            //мы хотим изменить.
            int rowsAffected = getContentResolver().update(mCurrentGoodsUri, values, null, null);

            // Показываем выскакивающее toast сообщение в зависимости того было ли обновление успешным или нет.
            if (rowsAffected == 0) {
                //При ошибке обновления
                Toast.makeText(this, getString(R.string.editor_update_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                //При успешном вводе новой строки
                Toast.makeText(this, getString(R.string.editor_update_success),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Служит для наполнения опций меню из файла res/menu/menu_editor.xml.
        // Добавляет элементы меню к верхней панели приложения
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * Этот метод вызывается после метода invalidateOptionsMenu(), поэтому
     * меню может быть обновлено
     * Некоторые элементы меню могут быть скрыты
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Если это новый товар прячем эелемент"Удалить" .
        if (mCurrentGoodsUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Пользователь нажимает на опцию меню из выпадающего меню
        switch (item.getItemId()) {
            // Отклик на нажатие "Сохранить" опции меню
            case R.id.action_save:
                // Сохраняет товар в базу данных
                saveGoods();
                //выходит из активности
                finish();
                return true;
            // Отклик программы на нажатие опции меню "Удалить"
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Отклик программы на срелку возврата на верхней панели экрана
            case android.R.id.home:
                //Если данные о товаре не были изменены
                // Возвращает в родительской активности (CatalogActivity)
                if (!mGoodsHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Если были не сохранённые изменения, устанавливаем диалоговое окно, чтобы предупредить пользователя
                // создаём click listener чтобы сообщить пользователю о том, что
                // изменения не будут сохранены.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Пользователь нажимает на кнопку"Выйти не сохранив" , переходит к родительской активности (CatalogActivity).
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Показать диалоговое окно, что существуют несохранённые данные
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);

    }

    /**
     * Этот метод вызывается, когда нажимается кнопка назад
     */
    @Override
    public void onBackPressed() {
        // Если данные о товаре не изменились, продолжает выполнение кнопки вернуться назад
        if (!mGoodsHasChanged) {
            super.onBackPressed();
            return;
        }

        // Иначе, диалоговое окно предупредит пользователя
        // создание click listener чтобы передать пользователю подтверждение о том, что изменения будут уничтожены
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Пользователь нажимает кнопку "Выйти не сохранив" и закрывает текущую активность.
                        finish();
                    }
                };

        // показывам диалоговое окно о том, что существуют несохранённые изменения
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                GoodsEntry._ID,
                GoodsEntry.COLUMN_NAME_TITLE,
                GoodsEntry.COLUMN_NAME_TYPE,
                GoodsEntry.COLUMN_NAME_COLOR,
                GoodsEntry.COLUMN_NAME_CATEGORY,
                GoodsEntry.COLUMN_NAME_PRICE,
                GoodsEntry.COLUMN_NAME_PRODUCER};


        return new CursorLoader(this,
                mCurrentGoodsUri,
                projection,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(GoodsEntry.COLUMN_NAME_TITLE);
            int typeColumnIndex = cursor.getColumnIndex(GoodsEntry.COLUMN_NAME_TYPE);
            int colorColumnIndex = cursor.getColumnIndex(GoodsEntry.COLUMN_NAME_COLOR);
            int categoryColumnIndex = cursor.getColumnIndex(GoodsEntry.COLUMN_NAME_CATEGORY);
            int priceColumnIndex = cursor.getColumnIndex(GoodsEntry.COLUMN_NAME_PRICE);
            int producerColumnIndex = cursor.getColumnIndex(GoodsEntry.COLUMN_NAME_PRODUCER);

            String name = cursor.getString(nameColumnIndex);
            String type = cursor.getString(typeColumnIndex);
            int color = cursor.getInt(colorColumnIndex);
            int category = cursor.getInt(categoryColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String producer = cursor.getString(producerColumnIndex);


            mNameEditText.setText(name);
            mTypeEditText.setText(type);
            mProducerEditText.setText(producer);
            mPriceEditText.setText(Integer.toString(price));


            switch (color) {
                case GoodsEntry.COLOR_GREEN:
                    mColorSpinner.setSelection(1);
                    break;
                case GoodsEntry.COLOR_BLUE:
                    mColorSpinner.setSelection(2);
                    break;
                case GoodsEntry.COLOR_RED:
                    mColorSpinner.setSelection(3);
                    break;
                case GoodsEntry.COLOR_BLACK:
                    mColorSpinner.setSelection(4);
                    break;
                case GoodsEntry.COLOR_WHITE:
                    mColorSpinner.setSelection(5);
                    break;
                case GoodsEntry.COLOR_YELLOW:
                    mColorSpinner.setSelection(6);
                    break;
                case GoodsEntry.COLOR_PURPLE:
                    mColorSpinner.setSelection(7);
                    break;
                case GoodsEntry.COLOR_ORANGE:
                    mColorSpinner.setSelection(8);
                    break;
                default:
                    mColorSpinner.setSelection(0);
                    break;
            }


            switch (category) {
                case GoodsEntry.CATEGORY_WASHING:
                    mCategorySpinner.setSelection(1);
                    break;
                case GoodsEntry.CATEGORY_STORING:
                    mCategorySpinner.setSelection(2);
                    break;
                case GoodsEntry.CATEGORY_COVERING:
                    mCategorySpinner.setSelection(3);
                    break;
                case GoodsEntry.CATEGORY_LOCKS:
                    mCategorySpinner.setSelection(4);
                    break;
                case GoodsEntry.CATEGORY_LADDERS:
                    mCategorySpinner.setSelection(5);
                    break;
                case GoodsEntry.CATEGORY_TRUCKS:
                    mCategorySpinner.setSelection(6);
                    break;
                case GoodsEntry.CATEGORY_THERMOMETERS:
                    mCategorySpinner.setSelection(7);
                    break;
                case GoodsEntry.CATEGORY_DISHES:
                    mCategorySpinner.setSelection(8);
                    break;
                default:
                    mCategorySpinner.setSelection(0);
                    break;
            }

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mNameEditText.setText("");
        mTypeEditText.setText("");
        mPriceEditText.setText("");
        mProducerEditText.setText("");

        mColorSpinner.setSelection(0);
        mCategorySpinner.setSelection(0);
    }


    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Создаём AlertDialog то есть диалоговое окно с предупреждением для пользоваля.
        // для создания кнопок с положительным и отрицательным ответом
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Пользователь нажимает на кнопку "Продолжить редактировать" и диалоговое окно исчезнет, вернув в редактирование
                // текущего товара
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Создание и показ предупреждающего диалогового окна
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Создание предупреждающего диалогового с помощью Builder окна AlertDialog.Builder и установление сообщения и объектов "click listener"
        // для кнопок "Удалить" и "Закрыть" в диалоговом окне.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Пользователь нажимает на кнопку "Удалить" для удаления товара
                deleteGood();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Пользователь нажимает на кнопку"Закрыть" чтобы закрыть диалоговое окно.
                // и продолжить редактировать товар
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Создание и пока предупреждающего диалогового окна т.е. AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Реализует удаление товара в базе данных
     */
    private void deleteGood() {
        //Удаляем товар, только если его uri есть и не равно нулю
        if (mCurrentGoodsUri != null) {
            // вызываем ContentResolver, чтобы удалить товар с заданным URI контента.
            // передаём null для отборов, так как mCurrentGoodsUri
            // URI контента уже определяет товар, который нужен для удаления.
            int rowsDeleted = getContentResolver().delete(mCurrentGoodsUri, null, null);
            if (rowsDeleted == 0) {
                // Если ни одна строка не была удалена, появится сообщение toast "Ошибка удаления"
                Toast.makeText(this, getString(R.string.editor_delete_good_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Если удаление успешное выпадет сообщение об этом "Товар удалён"
                Toast.makeText(this, getString(R.string.editor_delete_good_success),
                        Toast.LENGTH_SHORT).show();
            }

        }

        finish();

    }

}