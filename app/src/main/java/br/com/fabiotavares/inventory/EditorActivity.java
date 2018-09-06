package br.com.fabiotavares.inventory;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

import br.com.fabiotavares.inventory.data.ProductContract;
import br.com.fabiotavares.inventory.util.ImageHelper;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    private static final int PRODUCT_LOADER = 0;
    private EditText nameProductEditText;
    private EditText priceProductEditText;
    private EditText quantityProductEditText;
    private EditText nameSupplierEditText;
    private EditText phoneSupplierEditText;
    private Uri mCurrentProductUri;
    private boolean mProductHasChanged = false;
    private byte[] mImageByteArray;
    private Button mImageButton;
    private ImageView mProductImageView;
    private static final int IMAGE_PICKER_CODE = 7;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        mCurrentProductUri = getIntent().getData();
        ImageView orderImageView = findViewById(R.id.image_view_order);
        final ImageView upImageView = findViewById(R.id.image_view_up);
        final ImageView downImageView = findViewById(R.id.image_view_down);

        mProductImageView = (ImageView) findViewById(R.id.product_image);
        mProductImageView.setVisibility(View.GONE);

        if (mCurrentProductUri == null) {
            setTitle(R.string.editor_activity_title_new_product);
            orderImageView.setVisibility(View.GONE);
            upImageView.setVisibility(View.GONE);
            downImageView.setVisibility(View.GONE);
            invalidateOptionsMenu();

        } else {
            setTitle(R.string.editor_activity_title_edit_product);
            orderImageView.setVisibility(View.VISIBLE);
            upImageView.setVisibility(View.VISIBLE);
            downImageView.setVisibility(View.VISIBLE);
            getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        }
        nameProductEditText = findViewById(R.id.edit_text_name);
        priceProductEditText = findViewById(R.id.edit_text_price);
        quantityProductEditText = findViewById(R.id.edit_text_quantity);
        nameSupplierEditText = findViewById(R.id.edit_text_name_supplie);
        phoneSupplierEditText = findViewById(R.id.edit_text_phone);

        // Setup image upload button
        mImageButton = (Button) findViewById(R.id.image_upload_button);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create image picker intent
                // Several helpful answers on here: http://stackoverflow.com/questions/5309190/android-pick-images-from-gallery
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                Intent chooserIntent = Intent.createChooser(intent, getString(R.string.select_image));
                startActivityForResult(chooserIntent, IMAGE_PICKER_CODE);
            }
        });

        nameProductEditText.setOnTouchListener(mTouchListener);
        priceProductEditText.setOnTouchListener(mTouchListener);
        quantityProductEditText.setOnTouchListener(mTouchListener);
        nameSupplierEditText.setOnTouchListener(mTouchListener);
        phoneSupplierEditText.setOnTouchListener(mTouchListener);


        orderImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall();
            }
        });
        upImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantityString = quantityProductEditText.getText().toString().trim();
                int quantity = Integer.parseInt(quantityString);
                adjustProductQuantity(mCurrentProductUri, quantity, upImageView);
            }
        });
        downImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantityString = quantityProductEditText.getText().toString().trim();
                int quantity = Integer.parseInt(quantityString);
                adjustProductQuantity(mCurrentProductUri, quantity, downImageView);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (validation()) {
                    saveProduct();
                    finish();
                }
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUTO_NOME,
                ProductContract.ProductEntry.COLUMN_PRODUTO_PRECO,
                ProductContract.ProductEntry.COLUMN_PRODUTO_QUANTIDADE,
                ProductContract.ProductEntry.COLUMN_PRODUTO_IMAGEM,
                ProductContract.ProductEntry.COLUMN_PRODUTO_FORNECEDOR_NOME,
                ProductContract.ProductEntry.COLUMN_PRODUTO_FORNECEDOR_TELEFONE};

        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int nameProductColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUTO_NOME);
            int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUTO_PRECO);
            int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUTO_QUANTIDADE);
            int nameSupplierColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUTO_FORNECEDOR_NOME);
            int phoneSupplierColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUTO_FORNECEDOR_TELEFONE);

            String nameProduct = cursor.getString(nameProductColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String nameSupplier = cursor.getString(nameSupplierColumnIndex);
            String phoneSupplier = cursor.getString(phoneSupplierColumnIndex);

            nameProductEditText.setText(nameProduct);
            priceProductEditText.setText(Double.toString(price));
            quantityProductEditText.setText(Integer.toString(quantity));
            nameSupplierEditText.setText(nameSupplier);
            phoneSupplierEditText.setText(phoneSupplier);

            mImageByteArray = cursor.getBlob(cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUTO_IMAGEM));
            Bitmap productImage = ImageHelper.convertBlobToBitmap(mImageByteArray);

            if (productImage != null) {
                mProductImageView.setImageBitmap(productImage);
                mProductImageView.setVisibility(View.VISIBLE);
            } else {
                mProductImageView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameProductEditText.setText("");
        priceProductEditText.setText(String.valueOf(0));
        quantityProductEditText.setText(String.valueOf(0));
        nameSupplierEditText.setText("");
        phoneSupplierEditText.setText("");

    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICKER_CODE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            // Otherwise get image out of data
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                mProductImageView.setImageBitmap(selectedImage);
                // Temp code to store image
                mImageByteArray = ImageHelper.convertBitmapToBlob(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(EditorActivity.this, getString(R.string.something_wrong), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == IMAGE_PICKER_CODE) {
            Toast.makeText(EditorActivity.this, getString(R.string.no_picked_image), Toast.LENGTH_LONG).show();
        }
    }


    private void saveProduct() {
        String nameString = nameProductEditText.getText().toString().trim();
        String priceString = priceProductEditText.getText().toString().trim();
        String quantityString = quantityProductEditText.getText().toString().trim();
        String nameSupplierString = nameSupplierEditText.getText().toString().trim();
        String phoneSupplierString = phoneSupplierEditText.getText().toString().trim();


        if (mCurrentProductUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(nameSupplierString) && TextUtils.isEmpty(phoneSupplierString)) {
            return;
        }

        ContentValues values = new ContentValues();

        values.put(ProductContract.ProductEntry.COLUMN_PRODUTO_NOME, nameString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUTO_FORNECEDOR_NOME, nameSupplierString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUTO_FORNECEDOR_TELEFONE, phoneSupplierString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUTO_IMAGEM, mImageByteArray);
        double price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }
        values.put(ProductContract.ProductEntry.COLUMN_PRODUTO_PRECO, price);

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(ProductContract.ProductEntry.COLUMN_PRODUTO_QUANTIDADE, quantity);

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean validation() {
        String nameString = nameProductEditText.getText().toString().trim();
        String priceString = priceProductEditText.getText().toString().trim();
        String quantityString = quantityProductEditText.getText().toString().trim();
        String nameSupplierString = nameSupplierEditText.getText().toString().trim();
        String phoneSupplierString = phoneSupplierEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.name_product_empty), Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.price_empty), Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, getString(R.string.quantity_empty), Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(nameSupplierString)) {
            Toast.makeText(this, getString(R.string.name_supplier_empty), Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(phoneSupplierString)) {
            Toast.makeText(this, getString(R.string.phone_supplier_invalid), Toast.LENGTH_SHORT).show();
            return false;
        } else if (mImageByteArray == null) {
            Toast.makeText(this, getString(R.string.image_hint), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private void makePhoneCall() {
        String phoneNumber = phoneSupplierEditText.getText().toString().trim();

        if (ContextCompat.checkSelfPermission(EditorActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditorActivity.this, new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);
        } else {
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber)));
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void adjustProductQuantity(Uri productUri, int currentQuantityInStock, View v) {
        if (v.getId() == R.id.image_view_up) {
            int newQuantityValue = (currentQuantityInStock >= 1) ? currentQuantityInStock + 1 : 0;

            ContentValues contentValues = new ContentValues();
            contentValues.put(ProductContract.ProductEntry.COLUMN_PRODUTO_QUANTIDADE, newQuantityValue);
            int numRowsUpdated = getContentResolver().update(productUri, contentValues, null, null);
            if (numRowsUpdated > 0) {
                Toast.makeText(getApplicationContext(), R.string.up_quantity, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.no_product_in_stock, Toast.LENGTH_SHORT).show();

            }
        } else if (v.getId() == R.id.image_view_down) {
            int newQuantityValue = (currentQuantityInStock >= 1) ? currentQuantityInStock - 1 : 0;

            if (currentQuantityInStock == 0) {
                Toast.makeText(getApplicationContext(), R.string.toast_out_of_stock_msg, Toast.LENGTH_SHORT).show();
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(ProductContract.ProductEntry.COLUMN_PRODUTO_QUANTIDADE, newQuantityValue);
            int numRowsUpdated = getContentResolver().update(productUri, contentValues, null, null);
            if (numRowsUpdated > 0) {
                Toast.makeText(getApplicationContext(), R.string.down_quantity, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.no_product_in_stock, Toast.LENGTH_SHORT).show();

            }
        }

    }
}
