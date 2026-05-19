CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  login VARCHAR(100) UNIQUE NOT NULL,
  name VARCHAR(255),
  birthday DATE
);

CREATE TABLE IF NOT EXISTS mpa_rating (
  id SERIAL PRIMARY KEY,
  name VARCHAR(10) UNIQUE NOT NULL,
  description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS genre (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS film (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  release_date DATE,
  duration INTEGER,
  mpa_rating_id INTEGER,
  CONSTRAINT chk_duration_positive CHECK (duration > 0)
);

CREATE TABLE IF NOT EXISTS film_genre (
  film_id INTEGER NOT NULL,
  genre_id INTEGER NOT NULL,
  PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS friendship (
  user_id INTEGER NOT NULL,
  friend_id INTEGER NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS film_like (
  film_id INTEGER NOT NULL,
  user_id INTEGER NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (film_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_login ON users (login);
CREATE INDEX IF NOT EXISTS idx_film_mpa ON film (mpa_rating_id);
CREATE INDEX IF NOT EXISTS idx_film_genre_genre_id ON film_genre (genre_id);
CREATE INDEX IF NOT EXISTS idx_friendship_user_id ON friendship (user_id);
CREATE INDEX IF NOT EXISTS idx_friendship_friend_id ON friendship (friend_id);
CREATE INDEX IF NOT EXISTS idx_film_like_film_id ON film_like (film_id);
CREATE INDEX IF NOT EXISTS idx_film_like_user_id ON film_like (user_id);

ALTER TABLE film ADD CONSTRAINT IF NOT EXISTS fk_film_mpa_rating FOREIGN KEY (mpa_rating_id) REFERENCES mpa_rating (id);
ALTER TABLE film_genre ADD CONSTRAINT IF NOT EXISTS fk_film_genre_film FOREIGN KEY (film_id) REFERENCES film (id) ON DELETE CASCADE;
ALTER TABLE film_genre ADD CONSTRAINT IF NOT EXISTS fk_film_genre_genre FOREIGN KEY (genre_id) REFERENCES genre (id) ON DELETE CASCADE;
ALTER TABLE friendship ADD CONSTRAINT IF NOT EXISTS fk_friendship_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE friendship ADD CONSTRAINT IF NOT EXISTS fk_friendship_friend FOREIGN KEY (friend_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE film_like ADD CONSTRAINT IF NOT EXISTS fk_film_like_film FOREIGN KEY (film_id) REFERENCES film (id) ON DELETE CASCADE;
ALTER TABLE film_like ADD CONSTRAINT IF NOT EXISTS fk_film_like_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;