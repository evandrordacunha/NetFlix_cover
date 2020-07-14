package com.evandrorochadacunha.netflix_cover;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.evandrorochadacunha.netflix_cover.Model.Category;
import com.evandrorochadacunha.netflix_cover.Model.Movie;
import com.evandrorochadacunha.netflix_cover.Util.CategoryTask;
import com.evandrorochadacunha.netflix_cover.Util.ImageDownloaderTask;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CategoryTask.CategoryLoader{

    private MainAdapter mainAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_main);

        //CRIANDO LISTA DE CATEGORIAS
        List<Category> listaCategorias = new ArrayList<>();


        // ADAPTER RESPONSAVEL POR RECEBER DADOS DA LISTA PARA PREENCHER A RECYCLER VIEW
        mainAdapter = new MainAdapter(listaCategorias);
        //DEFININDO O TIPO DE LAYOUT DE EXIBIÇÃO DOS ITENS (VERTICAL / HORIZONTAL)
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        // DEFININDO QUEM GERENCIARA A IMPORTAÇÃO DOS ITENS PARA A RECYCLER
        recyclerView.setAdapter(mainAdapter);

        //BARRA DE CARREGAMENTO
        CategoryTask categoryTask = new CategoryTask(this);
        categoryTask.setCategoryLoader(this);
        categoryTask.execute("https://tiagoaguiar.co/api/netflix/home");
    }

    @Override
    public void onResult(List<Category> categories) {
        mainAdapter.setCategories(categories);
        mainAdapter.notifyDataSetChanged();
    }

    /**
     * MANIPULANDO OBJETOS DA LISTA DE RECYCLER VIEW
     */
    private static class MovieHolder extends RecyclerView.ViewHolder {

        // MANIPULA OBJETO TEXT VIEW DA LISTA
        final ImageView capa;

        public MovieHolder(@NonNull View itemView, final OnItemClickListener onItemClickListener) {
            super(itemView);
            capa = itemView.findViewById(R.id.capa);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onClick(getAdapterPosition());
                }
            });
        }
    }

    /**
     * MANIPULANDO OBJETOS DA LISTA DE RECYCLER VIEW
     */
    private static class CategoryHolder extends RecyclerView.ViewHolder {

        // MANIPULA OBJETO TEXT VIEW DA LISTA
        TextView titulo;
        //MANIPULA O CARROSSEL DE FILMES
        RecyclerView rv_categorias;

        public CategoryHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.title_category);
            rv_categorias = itemView.findViewById(R.id.rv_carrosel);
        }
    }

    private class MainAdapter extends RecyclerView.Adapter<CategoryHolder> {

        private List<Category> categorias;

        private MainAdapter(List<Category> categories) {
            this.categorias = categories;
        }

        // DEFININDO O LAYOUT XML SERÁ MANIPULADO RETORNANDO UM VIEW HOLDER (MOVIEVIEWHOLDER)
        @NonNull
        @Override
        public CategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CategoryHolder(getLayoutInflater()
                    .inflate(R.layout.category_item, parent, false));
        }

        // DEVOLVE UM ITEM  E SUA POSIÇÃO, TROCANDO UM USADO POR OUTRO QUANDO NECESSÁRIO
        @Override
        public void onBindViewHolder(@NonNull CategoryHolder holder, int position) {
            Category category = categorias.get(position);
            // MANIPULANDO PROPRIEDADES DE UMA CATEGORIA
            holder.titulo.setText(category.getNome());
            holder.rv_categorias.setAdapter(new MovieAdapter(category.getMovies()));
            holder.rv_categorias.setLayoutManager(new LinearLayoutManager(getBaseContext(), RecyclerView.HORIZONTAL, false
            ));
        }

        // QUANTIDADE DE ITENS DA COLEÇÃO
        @Override
        public int getItemCount() {
            return categorias.size();
        }

        public void setCategories(List<Category> categories) {
            this.categorias.clear();
            this.categorias.addAll(categories);
        }
    }

    /**
     * ADAPTER PARA VIDEOS
     */
    private class MovieAdapter extends RecyclerView.Adapter<MovieHolder> implements OnItemClickListener {

        private final List<Movie> movies;

        private MovieAdapter(List<Movie> filmes) {
            this.movies = filmes;
        }

        @Override
        public void onClick(int position) {
            if (movies.get(position).getId() <= 3) { //TRATEI AQUI PARA EXIBIR SO 3 FILMES POIS SÓ TENHO 3 DETALHES NO SERVIDOR
                Intent intent = new Intent(MainActivity.this, MovieActivity.class);
                intent.putExtra("id", movies.get(position).getId());
                startActivity(intent);
            }
        }

        // DEFININDO O LAYOUT XML SERÁ MANIPULADO RETORNANDO UM VIEW HOLDER (MOVIEVIEWHOLDER)
        @NonNull
        @Override
        public MovieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.movie_item, parent, false);
            return new MovieHolder(view, this);

        }

        // DEVOLVE UM ITEM  E SUA POSIÇÃO, TROCANDO UM USADO POR OUTRO QUANDO NECESSÁRIO
        @Override
        public void onBindViewHolder(@NonNull MovieHolder holder, int position) {
            Movie movie = movies.get(position);
            // MANIPULANDO PROPRIEDADES DE UM FILME

            //PEGANDO A CAPA DO FILME

            new ImageDownloaderTask(holder.capa).execute(movie.getCoverURL());
        }

        // QUANTIDADE DE ITENS DA COLEÇÃO
        @Override
        public int getItemCount() {
            return movies.size();
        }
    }

    /*INTERFACE DO EVENTO DE CLICK*/
    interface OnItemClickListener {
        void onClick(int position);
    }
}