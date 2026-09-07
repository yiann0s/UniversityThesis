# UniversityThesis
University thesis of an Android App for a Movie Recommendation System using a Collaborative Filtering Algorithm.
For a full description of the Theoretical background and the Code of the App, please consult Thesis.pdf.

![navigation and rating example](https://media.giphy.com/media/cdjNrpSVT5Xmse0AVn/giphy.gif)

## Configuration

Create `local.properties` in the project root and add your TMDB API key:

```properties
TMDB_API_KEY=your_tmdb_api_key
```

`local.properties` is ignored by Git. For CI builds, set the `TMDB_API_KEY`
environment variable instead.
