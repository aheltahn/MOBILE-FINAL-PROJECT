package com.example.finalprojectmu.fishiohouse.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fishiohouse.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_PRODUCTS = "products";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_IMAGE_URL = "imageUrl";
    private static final String COLUMN_TYPE = "type";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PRICE + " REAL, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_IMAGE_URL + " TEXT, " +
                COLUMN_TYPE + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }

    // Thêm hoặc cập nhật sản phẩm
    public void insertOrUpdateProduct(ProductEntity product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, product.getId());
        values.put(COLUMN_NAME, product.getName());
        values.put(COLUMN_PRICE, product.getPrice());
        values.put(COLUMN_DESCRIPTION, product.getDescription());
        values.put(COLUMN_IMAGE_URL, product.getImageUrl());
        values.put(COLUMN_TYPE, product.getType());

        // Sử dụng insertWithOnConflict để thay thế nếu ID đã tồn tại
        db.insertWithOnConflict(TABLE_PRODUCTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    // Lấy tất cả sản phẩm
    public List<ProductEntity> getAllProducts() {
        List<ProductEntity> productList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS, null);

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE));

                ProductEntity product = new ProductEntity(id, name, price, description, imageUrl, type);
                productList.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return productList;
    }

    // [MỚI] Lấy sản phẩm theo loại (Category) từ SQLite
    public List<ProductEntity> getProductsByCategory(String categoryType) {
        List<ProductEntity> productList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Sử dụng câu lệnh WHERE để lọc
        String query = "SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_TYPE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{categoryType});

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE));

                ProductEntity product = new ProductEntity(id, name, price, description, imageUrl, type);
                productList.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return productList;
    }

    // Xóa tất cả dữ liệu (dùng khi muốn refresh hoàn toàn từ server)
    public void deleteAllProducts() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PRODUCTS);
        db.close();
    }
}