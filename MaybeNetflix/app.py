import datetime
import os
import json
import random
import pandas as pd
import requests
from fastapi import FastAPI, HTTPException
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from typing import List
from pydantic import BaseModel

app = FastAPI()

# Load environment variable for TMDB API key
TMDB_API_KEY = os.getenv("TMDB_API_KEY")

# Load movie metadata
metadata = pd.read_csv("tmdb_5000_movies.csv")
metadata.fillna("", inplace=True)

# Create tags for TF-IDF
def clean_metadata(row):
    return f"{row['genres']} {row['keywords']} {row['overview']} {row['tagline']}"

metadata['tags'] = metadata.apply(clean_metadata, axis=1)
tfidf = TfidfVectorizer(stop_words='english')
tfidf_matrix = tfidf.fit_transform(metadata['tags'])
cosine_sim = cosine_similarity(tfidf_matrix)

# Fetch poster using TMDB API
def fetch_poster(movie_id: int) -> str:
    url = f"https://api.themoviedb.org/3/movie/{movie_id}?api_key={TMDB_API_KEY}&language=en-US"
    try:
        headers = {"User-Agent": "Mozilla/5.0"}
        response = requests.get(url, headers=headers, timeout=5)
        if response.status_code == 200:
            data = response.json()
            poster_path = data.get('poster_path')
            if poster_path:
                return "https://image.tmdb.org/t/p/w500" + poster_path
        return "https://via.placeholder.com/500"
    except Exception as e:
        print(f"[ERROR] Poster fetch failed for movie_id {movie_id}: {e}")
        return "https://via.placeholder.com/500"

# Models
class LikeData(BaseModel):
    user_id: str
    movie_title: str

class Feedback(BaseModel):
    user_id: str
    movie_id: int
    movie_title: str
    liked: bool = False
    star_rating: int = 0
    watch_duration: float = 0.0
    timestamp: str = datetime.datetime.utcnow().isoformat()
class Movie(BaseModel):
    movie_id: int
    title: str
    overview: str
    poster: str
    vote_average: float
    genres: str

class SectionResponse(BaseModel):
    title: str
    movies: List[Movie]

@app.get("/random_recommendations", response_model=List[SectionResponse])
def random_recommendations():
    titles = metadata['title'].dropna().tolist()

    # Pick 3 random titles
    seed_titles = random.sample(titles, 3)

    sections = []
    for title in seed_titles:
        try:
            rec = recommend(title)  # returns {"recommendations": [...]}
            sections.append({
                "title": f"Because you watched '{title}'",
                "movies": rec["recommendations"]
            })
        except Exception as e:
            print(f"Skipping {title} due to error: {e}")
            continue

    return sections

# Routes
@app.get("/recommend/{movie_title}")
def recommend(movie_title: str):
    movie_title = movie_title.lower().strip()
    index = metadata[metadata['title'].str.lower().str.strip() == movie_title].index
    if len(index) == 0:
        raise HTTPException(status_code=404, detail="Movie not found")

    index = index[0]
    distances = list(enumerate(cosine_sim[index]))
    distances = sorted(distances, key=lambda x: x[1], reverse=True)[1:6]

    results = []
    for i in distances:
        movie = metadata.iloc[i[0]]
        results.append({
            "movie_id": int(movie['id']) if str(movie['id']).isdigit() else -1,
            "title": movie['title'],
            "poster": fetch_poster(int(movie['id'])) if str(movie['id']).isdigit() else "",
            "overview": movie['overview'],
            "genres": movie['genres'],
            "vote_average": movie['vote_average'],
        })
    return {"recommendations": results}

@app.get("/movie/{movie_id}")
def get_movie_detail(movie_id: int):
    m = metadata[metadata['id'] == movie_id]
    if m.empty:
        raise HTTPException(status_code=404, detail="Movie not found")
    m = m.iloc[0]
    return {
        "id": movie_id,
        "title": m['title'],
        "overview": m['overview'],
        "genres": m['genres'],
        "tagline": m['tagline'],
        "release_date": m['release_date'],
        "vote_average": m['vote_average'],
        "runtime": m['runtime'],
        "poster": fetch_poster(movie_id)
    }

@app.post("/like")
def like_movie(data: LikeData):
    with open("likes.json", "a") as f:
        f.write(json.dumps(data.dict()) + "\n")
    return {"status": "saved"}

DATA_FILE = "user_feedback.json"

@app.post("/submit_feedback")
def submit_feedback(feedback: Feedback):
    entry = feedback.dict()
    if os.path.exists(DATA_FILE):
        with open(DATA_FILE, "r") as f:
            data = json.load(f)
    else:
        data = []

    data.append(entry)

    with open(DATA_FILE, "w") as f:
        json.dump(data, f, indent=4)

    return {"message": "Feedback saved!"}

from fastapi import Query

@app.get("/search/{query}")
def search_movies(query: str):
    query = query.lower()
    filtered = metadata[metadata['title'].str.lower().str.contains(query, na=False)]

    if filtered.empty:
        return []

    results = []
    for _, movie in filtered.iterrows():
        movie_id = int(movie['id']) if str(movie['id']).isdigit() else -1
        results.append({
            "movie_id": movie_id,
            "title": movie['title'],
            "poster": fetch_poster(movie_id),
            "overview": movie['overview'],
            "genres": movie['genres'],
            "vote_average": movie['vote_average'],
        })

    return results

import json
from fastapi import HTTPException

@app.get("/genre/{genre_name}")
def recommend_by_genre(genre_name: str):
    genre_name = genre_name.strip().lower()

    def genre_match(genre_json_str):
        try:
            genre_list = json.loads(genre_json_str)
            for genre_dict in genre_list:
                if genre_dict.get('name', '').lower() == genre_name:
                    return True
        except json.JSONDecodeError:
            return False
        return False

    filtered = metadata[metadata['genres'].apply(genre_match)]

    if filtered.empty:
        raise HTTPException(status_code=404, detail=f"No results found for genre: {genre_name}")

    results = []
    for _, movie in filtered.sample(min(10, len(filtered))).iterrows():
        movie_id = int(movie['id']) if str(movie['id']).isdigit() else -1
        try:
            poster = fetch_poster(movie_id)
        except:
            poster = "https://via.placeholder.com/300x450?text=No+Image"

        results.append({
            "movie_id": movie_id,
            "title": movie['title'],
            "poster": poster,
            "overview": movie['overview'],
            "genres": movie['genres'],
            "vote_average": movie['vote_average'],
        })

    return results
