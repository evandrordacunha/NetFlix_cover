package com.evandrorochadacunha.netflix_cover.Util;

import android.app.ProgressDialog;
import android.content.Context;

import com.evandrorochadacunha.netflix_cover.Model.Category;
import com.evandrorochadacunha.netflix_cover.Model.Movie;

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

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class CategoryTask extends AsyncTask<String, Void, List<Category>> {

    private final WeakReference<Context> context;
    private ProgressDialog barra;
    private CategoryLoader categoryLoader;

    public CategoryTask(Context context) {
        this.context = new WeakReference<>(context);
    }

    //BARRA DE PROGRESSO
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Context context = this.context.get();
        if (context != null) {
            barra = ProgressDialog.show(context, "Carregando...", "", true);
        }
    }

    //THREAD DE CONEXÃO COM SERVIDOR
    @Override
    protected List<Category> doInBackground(String... parametro) {

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
                List<Category> categories = getCategories(new JSONObject(jsonTraduzido));
                in.close(); //FECHANDO A CONEXÃO
                return categories;
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

    /*MANIPULANDO JSON - GET - MAPEAMENTO DO JSON PARA UMA LISTA DE CATEGORIAS */
    private List<Category> getCategories(JSONObject json) throws JSONException {
        List<Category> categories = new ArrayList<Category>();
        //Dando um get em tudo que está dentro da array Category do JSON
        JSONArray categoryArray = json.getJSONArray("category");
        for (int i = 0; i < categoryArray.length(); i++) {
            //Retorna um objeto categoria na posição i ao percorrer a lista
            JSONObject category = categoryArray.getJSONObject(i);
            String descricaoCategoria = category.getString("title");

            //Lista de filmes de uma categoria
            List<Movie> movies = new ArrayList<>();
            JSONArray movieArray = category.getJSONArray("movie");
            for (int j = 0; j < movieArray.length(); j++) {
                //Pega o filme pertecente a esta categoria na posição J
                JSONObject movieObjetc = movieArray.getJSONObject(j);
                //Pega a url do filme
                String coverUrl = movieObjetc.getString("cover_url");
                Movie movie = new Movie();
//                 Pega  o id do Json
                int id = movieObjetc.getInt("id");

                movie.setId(id);
                movie.setCoverURL(coverUrl);
//                 Adiciona na lista de filme
                movies.add(movie);
            }
//            Adiciona o objeto categoria na lista de categorias com seu nome e lista de filmes
            Category categoryObj = new Category();
            categoryObj.setNome(descricaoCategoria);
            categoryObj.setMovies(movies);
            categories.add(categoryObj);
        }
        return categories;
    }

    //EXIBE INFORMAÇÃO APÓS A EXECUÇÃO
    @Override
    protected void onPostExecute(List<Category> categories) {
        super.onPostExecute(categories);
        //sumir a barra quando dados forem carregados.
        barra.dismiss();
//        chamando o listener
        if (categoryLoader != null) {
            categoryLoader.onResult(categories);
        }
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

    /*IMPLEMENTANDO O LISTENER */
    public interface CategoryLoader {
        void onResult(List<Category> categories);
    }

    //    Método SetCategoryLoader
    public void setCategoryLoader(CategoryLoader categoryLoader) {
        this.categoryLoader = categoryLoader;
    }
}
