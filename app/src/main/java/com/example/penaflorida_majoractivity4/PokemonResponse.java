package com.example.penaflorida_majoractivity4;

import com.google.gson.annotations.SerializedName;

public class PokemonResponse {
    @SerializedName("name")
    private String name;

    @SerializedName("sprites")
    private Sprites sprites;

    public String getName() {
        return name;
    }

    public Sprites getSprites() {
        return sprites;
    }

    public static class Sprites {
        @SerializedName("front_default")
        private String frontDefault;

        @SerializedName("other")
        private Other other;

        public String getFrontDefault() {
            return frontDefault;
        }

        public Other getOther() {
            return other;
        }
    }

    public static class Other {
        @SerializedName("official-artwork")
        private OfficialArtwork officialArtwork;

        public OfficialArtwork getOfficialArtwork() {
            return officialArtwork;
        }
    }

    public static class OfficialArtwork {
        @SerializedName("front_default")
        private String frontDefault;

        public String getFrontDefault() {
            return frontDefault;
        }
    }
}
