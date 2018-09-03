package br.com.fabiotavares.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProductDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "inventario.db";

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String DATABASE_CREATE = "CREATE TABLE " + ProductContract.ProductEntry.TABELA_PRODUTO + "(" +
                ProductContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ProductContract.ProductEntry.COLUMN_PRODUTO_NOME + " TEXT NOT NULL ," +
                ProductContract.ProductEntry.COLUMN_PRODUTO_PRECO + " DOUBLE NOT NULL DEFAULT 0," +
                ProductContract.ProductEntry.COLUMN_PRODUTO_QUANTIDADE + " INTEGER NOT NULL DEFAULT 0," +
                ProductContract.ProductEntry.COLUMN_PRODUTO_FORNECEDOR_NOME + " TEXT NOT NULL, " +
                ProductContract.ProductEntry.COLUMN_PRODUTO_FORNECEDOR_TELEFONE + " TEXT NOT NULL );";
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
