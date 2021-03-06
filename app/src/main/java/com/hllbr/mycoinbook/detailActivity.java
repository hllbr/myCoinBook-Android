package com.hllbr.mycoinbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

public class detailActivity extends AppCompatActivity {
    EditText coinName,ceoName,yearName,orName;
    ImageView imageView;
    Button savebutton;
    Bitmap selectedImage;
    SQLiteDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        coinName = findViewById(R.id.coinNameText);
        ceoName = findViewById(R.id.ceoNameText);
        yearName = findViewById(R.id.yearText);
        orName = findViewById(R.id.orNameText);
        savebutton = findViewById(R.id.button);
        imageView = findViewById(R.id.imageView);
        database= this.openOrCreateDatabase("Coins",MODE_PRIVATE,null);
        Intent intent = getIntent();
        String info = intent.getStringExtra("info");
        if(info.matches("new")){
            coinName.setText("");
            ceoName.setText("");
            yearName.setText("");
            orName.setText("");
            savebutton.setVisibility(View.VISIBLE);
            Bitmap bitmap2 = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.addimage);
            imageView.setImageBitmap(bitmap2);
        }else{
            int coinId = intent.getIntExtra("CoinId",1);
            savebutton.setVisibility(View.INVISIBLE);
            try{
                Cursor cursor = database.rawQuery("SELECT * FROM coins WHERE id = ?",new String[]{String.valueOf(coinId)});
                int coinNameIx = cursor.getColumnIndex("coinname");
                int ceoIx = cursor.getColumnIndex("ceo");
                int yearIx = cursor.getColumnIndex("year");
                int ornpIx = cursor.getColumnIndex("ornp");

                int imageIx = cursor.getColumnIndex("image");
                while(cursor.moveToNext()){
                    coinName.setText(cursor.getString(coinNameIx));
                    ceoName.setText(cursor.getString(ceoIx));
                    yearName.setText(cursor.getString(yearIx));
                    orName.setText(cursor.getString(ornpIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap1 = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    imageView.setImageBitmap(bitmap1);
                }
                cursor.close();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
    public void selectImage(View view){

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //not allowed operation
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                //requestcode = izin kodu olarak ifade edebiliriz.hangi istek i??in hangi cevap geldi bunun kontrol edilmesi i??in verilmi?? bir id olarak ifade edebiliriz.
            }else{
                //allowed operation
                Intent intontToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //URI sadece web adresinin de??il dosya adresinide belirtmek amac??yla kullan??l??yor
                startActivityForResult(intontToGallery,2);//iki numaral?? yap?? i??in ger??ekle??tirilecek i??lem olarak ifade edebiliriz.
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //izin istendi??inde bunun sonucunda ne olaca????n?? yazd??????m alan
        if(requestCode == 1){
            //e??er selectImage t??kland??????nda kullan??c?? izin vermemi??se yapaca????m i??lemler
            if(grantResults.length> 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                //sonu?? i??erisinde en az bir eleman bar??nd??r??yorsa ve i??erisindeki eleman izin onay elemean?? ise
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentToGallery,2);
            }
        }

        //buradan sonraki i??lem ki??i g??sreli se??tikten sorna ger??ekle??tirilecek i??lemler

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //izinleri kontrol ettikten sonra kullan??c??n??n neyi se??ti??ini ve kullan??c??ya d??n??lecek veriyi ifade etti??imiz yap??m??z
        if(requestCode == 2 && resultCode == RESULT_OK && data != null){
            //izin okey ,bir??eyler se??ilmi?? okeyi,d??nd??r??lecek veri bo?? ifadeden farkl?? okey ise ...
            Uri imageData = data.getData();//bu yap?? bize Uri veriyor nereye kay??tl??  oldu??unun yolunu almak i??in kulal??yorum
            //yolu ald??m

            //versionlar aras??ndaki farkl??l??klardan dolay?? bu k??s??mda de??i??iklikler oluyor

            try{
                if(Build.VERSION.SDK_INT >=28){
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(),imageData);
                    selectedImage = ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(selectedImage);
                }else{
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData);
                    imageView.setImageBitmap(selectedImage);
                }
            }catch (Exception ex){

                ex.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void saveCoin(View view){
        String coinname = coinName.getText().toString();
        String ceoname = ceoName.getText().toString();
        String year = yearName.getText().toString();
        String oroperation = orName.getText().toString();

        Bitmap smallImage = makeSmallerImage(selectedImage,300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();//G??rsel veriye d??n????t??rlm???? oldu

        //Veri Taban?? i??lemleri =
        try{
            database = this.openOrCreateDatabase("Coins",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EX??STS coins(id INTEGER PRIMARY KEY,coinname VARCHAR,ceo VARCHAR,year VARCHAR,ornp VARCHAR,image BLOB)");
            String sqlString = "INSERT INTO coins(coinname,ceo,year,ornp,image) VALUES(?,?,?,?,?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,coinname);
            sqLiteStatement.bindString(2,ceoname);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindString(4,oroperation);
            sqLiteStatement.bindBlob(5,byteArray);
            sqLiteStatement.execute();
        }catch (Exception eX){
            eX.printStackTrace();
        }
        Intent intent = new Intent(detailActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    public Bitmap makeSmallerImage(Bitmap image,int maximumSize){
        //bu metodun bana bir adet bitmap d??nmesinini istedi??im i??in bitmap s??n??f??ndan yararlanarak metodu olu??turuyorum
        //orant??l?? olarak bir k??????ltme i??lemi ger??ekle??tirmek istiyorum.Bunu sorgularla yapabilirim

        int width = image.getWidth();
        int height = image.getHeight();
        //hassas bir sonu?? elde etmek i??in double/float ile i??lemlerime devam ediyorum

        float bitmapRatio = (float)(width/height);
        //bitmapRatio e??er 1 den b??y??kse widht daha b??y??k demektir.Geni??lik daha b??y??k resim yatay b??y??k demek
        if(bitmapRatio > 1){
            width = maximumSize;
            height = (int)(width/bitmapRatio);
        }else{
            height = maximumSize;
            width = (int)(height*bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image,width,height,true);


    }
}