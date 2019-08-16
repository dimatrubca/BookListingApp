package com.example.booklistingapp;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BooksAdapter extends RecyclerView.Adapter {
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    private static final String TAG = "BooksAdapter";
    private List<Book> books;

    // The minimum amount of items to have below your current scroll position
    // before loadingMore more.
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private boolean loadingMore;
    private boolean loadedAllData;
    private OnLoadMoreListener onLoadMoreListener;

    public BooksAdapter(List<Book> books, RecyclerView recyclerView) {
        this.books = books;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (loadedAllData) {
                        return;
                    }

                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                    if (!loadingMore && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        // End has been reached
                        if (onLoadMoreListener != null) {
                            loadingMore = true;
                            onLoadMoreListener.onLoadMore();
                        }
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return books.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;

        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
            vh = new BookViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.progressbar, parent, false);
            vh = new ProgressViewHolder(v);
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof BookViewHolder) {
            BookViewHolder holder = (BookViewHolder) viewHolder;
            Book currentBook = books.get(position);

            holder.title.setText(currentBook.getTitle());
            if (currentBook.getAuthors() != null) {
                holder.authors.setVisibility(View.VISIBLE);
                holder.authors.setText("Author: " + extractAuthors(currentBook.getAuthors()));
            } else {
                holder.authors.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(currentBook.getImageURL())) {
                Picasso.get().load(currentBook.getImageURL()).into(holder.image);
            } else {
                Log.i(TAG, "getView: no image");
                //set default image
            }

            if (currentBook.getPageCount() != Book.INT_FIELD_NOT_PROVIDED) {
                holder.pageCount.setVisibility(View.VISIBLE);
                holder.pageCount.setText("Pages: " + currentBook.getPageCount());
            } else {
                holder.pageCount.setVisibility(View.GONE);
            }

            if (currentBook.getPublishedDate() != null) {
                holder.publishedDate.setVisibility(View.VISIBLE);
                String date = dateToString(currentBook.getPublishedDate(), "dd/MM/yyyy");
                holder.publishedDate.setText("Published: " + date);
            } else {
                holder.publishedDate.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(currentBook.getDescription())) {
                holder.description.setVisibility(View.VISIBLE);
                holder.description.setText(currentBook.getDescription());
            } else {
                holder.description.setVisibility(View.GONE);
            }
        } else {
            ((ProgressViewHolder) viewHolder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public void setLoaded() {
        loadingMore = false;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void clear() {
        books.clear();
        notifyDataSetChanged();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView authors;
        private TextView description;
        private TextView pageCount;
        private TextView publishedDate;
        private ImageView image;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.text_title);
            authors = itemView.findViewById(R.id.text_authors);
            description = itemView.findViewById(R.id.text_description);
            pageCount = itemView.findViewById(R.id.text_page_count);
            publishedDate = itemView.findViewById(R.id.text_published_date);
            image = itemView.findViewById(R.id.image_thumbnail);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            this.progressBar = itemView.findViewById(R.id.progressBar1);
        }
    }

    private String dateToString(Date date, String pattern) {
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }

    private String extractAuthors(List<String> authors) {
        StringBuilder authorsString = new StringBuilder();
        for (int i = 0; i < authors.size(); i++) {
            authorsString.append(authors.get(i));
            if (i != authors.size() - 1) {
                authorsString.append(", ");
            }
        }
        return authorsString.toString();
    }

    public void addItems(List<Book> list) {
        books.addAll(list);
        notifyDataSetChanged();
    }

    public void addItem(Book book) {
        books.add(book);
        notifyItemInserted(books.size() - 1);
    }

    public void removeLastItem() {
        books.remove(books.size() - 1);
        notifyItemRemoved(books.size());
    }

    public void swapList(ArrayList<Book> books) {
        this.books.clear();
        this.books.addAll(books);
        notifyDataSetChanged();
        loadedAllData = false;
    }

    public boolean isLoadingMore() {
        return loadingMore;
    }

    public void setIsLoadedAllData() {
        loadedAllData = true;
    }
}
