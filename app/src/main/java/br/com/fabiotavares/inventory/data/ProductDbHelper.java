package br.com.fabiotavares.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ProductDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "inventario.db";

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(createTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(dropTable());
        db.execSQL(createTable());
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(dropTable());
        db.execSQL(createTable());
    }

    private String createTable() {
        String createTable = "CREATE TABLE " + ProductContract.ProductEntry.TABELA_PRODUTO + "(" +
                ProductContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ProductContract.ProductEntry.COLUMN_PRODUTO_NOME + " TEXT NOT NULL ," +
                ProductContract.ProductEntry.COLUMN_PRODUTO_PRECO + " DOUBLE NOT NULL DEFAULT 0," +
                ProductContract.ProductEntry.COLUMN_PRODUTO_QUANTIDADE + " INTEGER NOT NULL DEFAULT 0," +
                ProductContract.ProductEntry.COLUMN_PRODUTO_IMAGEM + " TEXT NOT NULL, " +
                ProductContract.ProductEntry.COLUMN_PRODUTO_FORNECEDOR_NOME + " TEXT NOT NULL, " +
                ProductContract.ProductEntry.COLUMN_PRODUTO_FORNECEDOR_TELEFONE + " TEXT NOT NULL );";
        return createTable;
    }

    private String dropTable() {
        String dropTable = "DROP TABLE " + ProductContract.ProductEntry.TABELA_PRODUTO + ";";
        return dropTable;
    }
}
