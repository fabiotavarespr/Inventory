package br.com.fabiotavares.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ProductContract {
    public static final String CONTENT_AUTHORITY = "br.com.fabiotavares.inventory";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PRODUCT = "product";

    private ProductContract() {
    }

    public static final class ProductEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCT);
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        public static final String TABELA_PRODUTO = "produto";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUTO_NOME = "produto_nome";
        public static final String COLUMN_PRODUTO_PRECO = "produto_preco";
        public static final String COLUMN_PRODUTO_QUANTIDADE = "produto_quantidade";
        public static final String COLUMN_PRODUTO_FORNECEDOR_NOME = "fornecedor_nome";
        public static final String COLUMN_PRODUTO_FORNECEDOR_TELEFONE = "fornecedor_telefone";
    }
}
