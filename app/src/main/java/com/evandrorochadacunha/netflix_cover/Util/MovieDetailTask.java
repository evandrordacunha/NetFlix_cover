package com.evandrorochadacunha.netflix_cover.Util;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.ProgressBar;

import com.evandrorochadacunha.netflix_cover.Model.Movie;
import com.evandrorochadacunha.netflix_cover.Model.MovieDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MovieDetailTask extends AsyncTask<String, Void, MovieDetail> {
    //    Adicionando um contexto
    private final WeakReference<Context> context;
    private ProgressDialog dialog;
    private MovieDetailLoader movieDetailLoader;

    public MovieDetailTask(Context context) {
        this.context = new WeakReference<>(context);
    }

    public void setMovieDetailLoader(MovieDetailLoader movieDetailLoader) {
        this.movieDetailLoader = movieDetailLoader;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Context context = this.context.get();
        if (context != null) {
            dialog = ProgressDialog.show(context, "Carregando...", "", true);
        }
    }

    @Override
    protected MovieDetail doInBackground(String... parametro) {

        //recebe a URL passa na MainActivity onde contem o JSON de Filmes.
        String url = parametro[0];

        try {
            URL requestUrl = new URL(url);
            HttpsURLConnection conexao = (HttpsURLConnection) requestUrl.openConnection();
            conexao.setReadTimeout(2000); //tempo de espera de leitura
            conexao.setReadTimeout(2000); //tempo até mostrar mensagem de erro

            //STATUS CODE - CÓDIGO DE RETORNO DE CONEXÃO
            int responseCode = conexao.getResponseCode();

            //SE O RETORNO FOR >400 ENTÃO ALGO DEU ERRADO
            if (responseCode > 400) {
                throw new IOException("Erro de comunicação com o servidor!!!");
            }

            InputStream inputStream = conexao.getInputStream();
            BufferedInputStream in = new BufferedInputStream(inputStream);

            String jsonTraduzido = toString(in);
            try {
                MovieDetail movieDetail = getMovieDetail(new JSONObject(jsonTraduzido));
                in.close(); //FECHANDO A CONEXÃO
                return movieDetail;
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    //    Pega todas propriedades do Json
    private MovieDetail getMovieDetail(JSONObject jsonObject) throws JSONException {
        int id = jsonObject.getInt("id");
        String title = jsonObject.getString("title");
        String desc = jsonObject.getString("desc");
        String cast = jsonObject.getString("cast");
        String coverUrl = jsonObject.getString("cover_url");

//        Criando lista de filmes similares
        List<Movie> movies = new ArrayList<>();
        JSONArray movieArray = jsonObject.getJSONArray("movie");

        for (int i = 0; i < movieArray.length(); i++) {
            JSONObject movieObj = movieArray.getJSONObject(i);
            String cover_url = movieObj.getString("cover_url");
            int movieId = movieObj.getInt("id");

            Movie similar = new Movie();
            similar.setId(movieId);
            similar.setCoverURL(cover_url);
            similar.setDesc(desc);
            similar.setCast(cast);
            movies.add(similar);
        }
        Movie movie = new Movie();
        movie.setId(id);
        movie.setCoverURL(coverUrl);
        movie.setTitulo(title);
        movie.setDesc(desc);
        movie.setCast(cast);
        return new MovieDetail(movie, movies);
    }

    @Override
    protected void onPostExecute(MovieDetail movieDetail) {
        super.onPostExecute(movieDetail);
        dialog.dismiss();
        if (movieDetailLoader != null) {
            movieDetailLoader.onResult(movieDetail);
        }
    }

    public interface MovieDetailLoader {
        void onResult(MovieDetail movieDetail);
    }


    /*CONVERSOR DE BYTES PARA STRING*/
    private String toString(InputStream is) throws IOException {
        byte[] bytes = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int lidos;
        while ((lidos = is.read(bytes)) > 0) {
            baos.write(bytes, 0, lidos);
        }
        return new String(baos.toByteArray());
    }

}
