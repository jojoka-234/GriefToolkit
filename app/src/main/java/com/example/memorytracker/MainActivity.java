package com.example.memorytracker;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.memorytracker.books.Books;
import com.example.memorytracker.dayCounter.DayCounter;
import com.example.memorytracker.memorabilia.Memorabilia;
import com.example.memorytracker.memories.Memories;
import com.example.memorytracker.mood.MoodTracker;
import com.example.memorytracker.quotes.Quotes;
import com.example.memorytracker.source.Sources;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> exportBackup = registerForActivityResult(
            new ActivityResultContracts.CreateDocument(), uri -> {
                if (uri == null) {
                    return;
                }
                Toast.makeText(this, getString(R.string.export_running), Toast.LENGTH_SHORT).show();
                new Thread(() -> {
                    boolean success = ZipUtils.zipFilesDir(this, uri);
                    runOnUiThread(() -> Toast.makeText(this,
                            getString(success ? R.string.export_success : R.string.export_failure),
                            Toast.LENGTH_LONG).show());
                }).start();
            });

    private final ActivityResultLauncher<String[]> importBackup = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) {
                    return;
                }
                Toast.makeText(this, getString(R.string.import_running), Toast.LENGTH_SHORT).show();
                new Thread(() -> {
                    boolean success = ZipUtils.unzipToFilesDir(this, uri);
                    runOnUiThread(() -> Toast.makeText(this,
                            getString(success ? R.string.import_success : R.string.import_failure),
                            Toast.LENGTH_LONG).show());
                }).start();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button moodTracker = findViewById(R.id.moodTracker);
        moodTracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMoodTracker(view);
            }
        });

        Button memories = findViewById(R.id.memories);
        memories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMemories(view);
            }
        });

        Button sources = findViewById(R.id.sources);
        sources.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSources(view);
            }
        });

        Button books = findViewById(R.id.books);
        books.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBooks(view);
            }
        });

        Button dayCounter = findViewById(R.id.dayCounter);
        dayCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDayCounter(view);
            }
        });

        Button quotes = findViewById(R.id.quotes);
        quotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openQuotes(view);
            }
        });

        Button memorabilia = findViewById(R.id.memorabilia);
        memorabilia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMemorabilia(view);
            }
        });
    }

    public void openMoodTracker(View view) {
        Intent intent = new Intent(this, MoodTracker.class);
        startActivity(intent);
    }

    public void openMemories(View view) {
        Intent intent = new Intent(this, Memories.class);
        startActivity(intent);
    }

    public void openBooks(View view) {
        Intent intent = new Intent(this, Books.class);
        startActivity(intent);
    }

    public void openSources(View view) {
        Intent intent = new Intent(this, Sources.class);
        startActivity(intent);
    }

    public void openDayCounter(View view) {
        Intent intent = new Intent(this, DayCounter.class);
        startActivity(intent);
    }

    public void openQuotes(View view) {
        Intent intent = new Intent(this, Quotes.class);
        startActivity(intent);
    }

    public void openMemorabilia(View view) {
        Intent intent = new Intent(this, Memorabilia.class);
        startActivity(intent);
    }

    //AppBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
               getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.export:
                exportBackup.launch(String.format("grief_toolkit_backup_%s.zip", new SimpleDateFormat("yyyyMMddHHmm").format(new Date())));
                break;
            case R.id.imp:
                importBackup.launch(new String[]{"application/zip", "application/x-zip-compressed", "application/octet-stream"});
                break;
        }
        return true;
    }

}