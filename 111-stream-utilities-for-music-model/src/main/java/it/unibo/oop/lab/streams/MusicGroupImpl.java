package it.unibo.oop.lab.streams;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public final class MusicGroupImpl implements MusicGroup {

    private final Map<String, Integer> albums = new HashMap<>();
    private final Set<Song> songs = new HashSet<>();

    @Override
    public void addAlbum(final String albumName, final int year) {
        this.albums.put(albumName, year);
    }

    @Override
    public void addSong(final String songName, final Optional<String> albumName, final double duration) {
        if (albumName.isPresent() && !this.albums.containsKey(albumName.get())) {
            throw new IllegalArgumentException("invalid album name");
        }
        this.songs.add(new MusicGroupImpl.Song(songName, albumName, duration));
    }

    @Override
    public Stream<String> orderedSongNames() {
        return songs.stream().map(a -> a.getSongName()).sorted();
    }

    @Override
    public Stream<String> albumNames() {
        return albums.keySet().stream();
    }

    @Override
    public Stream<String> albumInYear(final int year) {
        return albums.entrySet().stream()
            .filter(a -> a.getValue().equals(year))
            .map(a -> a.getKey());
    }

    @Override
    public int countSongs(final String albumName) {
        return songs.stream().filter(a -> a.getAlbumName().isPresent())
            .filter(a->a.getAlbumName().get().equals(albumName))
            .toList()
            .size();
    }

    @Override
    public int countSongsInNoAlbum() {
        return songs.stream().filter(a -> a.getAlbumName().isEmpty()).toList().size();
    }

    @Override
    public OptionalDouble averageDurationOfSongs(final String albumName) {
        return OptionalDouble.of(songs.stream()
        .filter(a -> a.getAlbumName().isPresent())
        .filter(a -> a.getAlbumName().get().equals(albumName))
        .map(a -> a.getDuration())
        .reduce((a,b) -> a+b).get() / countSongs(albumName));
    }

    @Override
    public Optional<String> longestSong() {
        return Optional.of(
            songs.stream()
            .max((a,b) -> Double.compare(a.getDuration(), b.getDuration()))
            .get()
            .getSongName());
    }

    @Override
    public Optional<String> longestAlbum() {
        return Optional.of(
            /*albums.entrySet().stream()
                .collect(groupingBy(a -> songs.stream()
                        .filter(b -> b.getAlbumName().isPresent())
                        .filter(b -> b.getAlbumName().get().equals(a.getKey())),minBy((f,g) -> Double.compare(f.getValue().doubleValue(), g.getValue().doubleValue())))).entrySet().stream().sorted((l,p) -> Double.compare(l.getValue().get().getValue().doubleValue(), p.getValue().get().getValue().doubleValue())).findFirst().get().getKey().sorted().findFirst().get().getSongName()*/
            this.songs.stream()
                .filter(a -> a.getAlbumName().isPresent())
                .collect(Collectors.groupingBy(a -> a.getAlbumName(), Collectors.summingDouble(Song::getDuration)))
                .entrySet().stream()
                .max((k,l) -> k.getValue().compareTo(l.getValue())).get().getKey()).get();
    }

    private static final class Song {

        private final String songName;
        private final Optional<String> albumName;
        private final double duration;
        private int hash;

        Song(final String name, final Optional<String> album, final double len) {
            super();
            this.songName = name;
            this.albumName = album;
            this.duration = len;
        }

        public String getSongName() {
            return songName;
        }

        public Optional<String> getAlbumName() {
            return albumName;
        }

        public double getDuration() {
            return duration;
        }

        @Override
        public int hashCode() {
            if (hash == 0) {
                hash = songName.hashCode() ^ albumName.hashCode() ^ Double.hashCode(duration);
            }
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Song) {
                final Song other = (Song) obj;
                return albumName.equals(other.albumName) && songName.equals(other.songName)
                        && duration == other.duration;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Song [songName=" + songName + ", albumName=" + albumName + ", duration=" + duration + "]";
        }

    }

}
