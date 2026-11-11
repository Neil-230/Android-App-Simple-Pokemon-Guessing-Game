package com.example.penaflorida_majoractivity4;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PokeApiService {
    @GET("pokemon/{id}")
    Call<PokemonResponse> getPokemon(@Path("id") int id);
}
