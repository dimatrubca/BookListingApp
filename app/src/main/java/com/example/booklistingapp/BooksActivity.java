package com.example.booklistingapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BooksActivity extends AppCompatActivity {

    private static final String TAG = "BooksActivity";
    private static final String BASE_URL = "https://www.googleapis.com/books/v1/volumes?key=AIzaSyB_sjSTZDsnSQWkVtWTGZ90qhltvj6Uhr4";
    private static final int LOAD_BOOKS_PER_QUERY = 10;

    RecyclerView recyclerView;
    BooksAdapter adapter;
    TextView emptyView;
    ProgressBar progressBar;
    EditText searchBar;

    String initialQueryUrl;
    int booksRetrieved;

    TextView.OnEditorActionListener searchBarActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (!isNetworkAvailable()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText("NO INTERNET CONNECTION");
                return false;
            }
            booksRetrieved = 0;

            Uri buildUri = Uri.parse(BASE_URL)
                    .buildUpon()
                    .appendQueryParameter("q", searchBar.getText().toString())
                    .appendQueryParameter("maxResults", String.valueOf(LOAD_BOOKS_PER_QUERY))
                    .build();
            initialQueryUrl = buildUri.toString();
            new BookDownloader().execute(initialQueryUrl);
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);
        recyclerView = findViewById(R.id.recycle_view);
        emptyView = findViewById(R.id.text_empty_list);
        progressBar = findViewById(R.id.progressBar);

        searchBar = findViewById(R.id.search_bar);
        searchBar.setOnEditorActionListener(searchBarActionListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(BooksActivity.this));
        adapter = new BooksAdapter(new ArrayList<Book>(), recyclerView);
        recyclerView.setAdapter(adapter);

        //add divider between items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                adapter.addItem(null);

                Uri buildUri = Uri.parse(initialQueryUrl)
                        .buildUpon()
                        .appendQueryParameter("startIndex", String.valueOf(booksRetrieved))
                        .build();
                new BookDownloader().execute(buildUri.toString());
            }
        });
    }

    private class BookDownloader extends AsyncTask<String, Void, ArrayList<Book>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (adapter.isLoadingMore()) {
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);

            if (adapter != null) {
                adapter.clear();
            }
        }

        @Override
        protected ArrayList<Book> doInBackground(String... strings) {
            return QueryUtils.extractBooksFromURL(strings[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<Book> books) {
            progressBar.setVisibility(View.GONE);

            if (books == null) {
                books = new ArrayList<>();
            }

            if (adapter.isLoadingMore()) {
                adapter.removeLastItem();
                adapter.addItems(books);
                adapter.setLoaded();
            } else {
                adapter.swapList(books);
                if (books.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText("No results found");
                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            if (books.isEmpty()) {
                adapter.setIsLoadedAllData();
            } else {
                booksRetrieved += books.size(); //use it
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
