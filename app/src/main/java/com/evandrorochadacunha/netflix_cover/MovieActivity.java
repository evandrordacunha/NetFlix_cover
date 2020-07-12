package com.evandrorochadacunha.netflix_cover;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import com.evandrorochadacunha.netflix_cover.Model.Movie;
import com.evandrorochadacunha.netflix_cover.Model.MovieDetail;
import com.evandrorochadacunha.netflix_cover.Util.ImageDownloaderTask;
import com.evandrorochadacunha.netflix_cover.Util.MovieDetailTask;

import java.util.ArrayList;
import java.util.List;

public class MovieActivity extends AppCompatActivity implements MovieDetailTask.MovieDetailLoader {

        /*VARIAVEIS*/
        private TextView txtTitulo;
        private TextView txtDescricao;
        private TextView txtCast;
        private MovieAdapter movieAdapter;
        private ImageView img_cover;
        private RecyclerView recyclerView;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_movie);

            /*INICIALIZANDO VARIAVEIS */
            txtDescricao = findViewById(R.id.descricao);
            txtCast = findViewById(R.id.elenco);
            txtTitulo = findViewById(R.id.titulo_filme);
            recyclerView = findViewById(R.id.recycler_view_similar);
            img_cover = findViewById(R.id.image_movie);

        /*
        ADICIONANDO A TOOLBAR CRIADA A ACTIVITY
         */
            //Referenciando a Toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            /*MODIFICANDO PROPRIEDADES DA TOOLBAR*/
            //Se existir uma toolbar ativa e action declarado, manipula ela fazendo:
            if (getSupportActionBar() != null) {
                //menu Home
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                //botão voltar [criar em File>>New>>VectorAsset
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
                getSupportActionBar().setTitle(null);
            }
            //MUDAR DINAMICAMENTE AS CAPAS DA DESCRIÇÃO DOS FILMES
            LayerDrawable drawable = (LayerDrawable) ContextCompat.getDrawable(this, R.drawable.shadows);
            List<Movie> movies = new ArrayList<>();
//        RECEBENDO DADOS REAIS PELA DAPTER
            movieAdapter = new MovieAdapter(movies);
            /*POPULANDO FILMES SIMILARES */
            recyclerView.setAdapter(movieAdapter);
            /*cria a grade com 3 colunas */
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));


            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                int id = extras.getInt("id");
                MovieDetailTask movieDetailTask = new MovieDetailTask(this);
                movieDetailTask.setMovieDetailLoader(this); //listener é a propria activity
                movieDetailTask.execute("https://tiagoaguiar.co/api/netflix/" + id);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == android.R.id.home)
                finish();
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onResult(MovieDetail movieDetail) {

            txtTitulo.setText(movieDetail.getMovie().getTitulo());
            txtDescricao.setText(movieDetail.getMovie().getDesc());
            txtCast.setText(movieDetail.getMovie().getCast());
            movieAdapter.setMovies(movieDetail.getMoviesSimilar());
            movieAdapter.notifyDataSetChanged();
//      ALTERANDO CAPA DO FILME DENTRO DE DETALHES
            ImageDownloaderTask imageDownloaderTask = new ImageDownloaderTask(img_cover);
            imageDownloaderTask.setShadowEnabled(true);
            imageDownloaderTask.execute(movieDetail.getMovie().getCoverURL());
        }

        private static class MovieHolder extends RecyclerView.ViewHolder {

            // MANIPULA OBJETO TEXT VIEW DA LISTA
            final ImageView capa;

            public MovieHolder(@NonNull View itemView) {
                super(itemView);
                capa = itemView.findViewById(R.id.capa);
            }
        }

        /**
         * ADAPTER PARA VIDEOS
         */
        private class MovieAdapter extends RecyclerView.Adapter<MovieHolder> {

            private final List<Movie> movies;

            public void setMovies(List<Movie> movies) {
                this.movies.clear();
                this.movies.addAll(movies);
            }

            private MovieAdapter(List<Movie> filmes) {

                this.movies = filmes;
            }


            // DEFININDO O LAYOUT XML SERÁ MANIPULADO RETORNANDO UM VIEW HOLDER (MOVIEVIEWHOLDER)
            @NonNull
            @Override
            public MovieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new MovieHolder(getLayoutInflater()
                        .inflate(R.layout.movie_item_similar, parent, false));
            }

            // DEVOLVE UM ITEM  E SUA POSIÇÃO, TROCANDO UM USADO POR OUTRO QUANDO NECESSÁRIO
            @Override
            public void onBindViewHolder(@NonNull MovieHolder holder, int position) {
                Movie movie = movies.get(position);
                new ImageDownloaderTask(holder.capa).execute(movie.getCoverURL());

                //PEGANDO A CAPA DO FILME
                //holder.capa.setImageResource(movie.getCoverURL());
            }

            // QUANTIDADE DE ITENS DA COLEÇÃO
            @Override
            public int getItemCount() {
                return movies.size();
            }
        }
    }
