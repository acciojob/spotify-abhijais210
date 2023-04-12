package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name,mobile);
        users.add(user);
        userPlaylistMap.put(user,new ArrayList<>());
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        boolean flag = false;
        Artist artist = null;

        for(Artist a : artists){
            if(a.getName().equals(artistName)){
                flag = true;
                 artist = a;
                 break;
            }
        }
        //if artist not created then first create artist
        if(!flag){
            artist = createArtist(artistName);
        }
        Album album = new Album(title);
        albums.add(album);
        //and also map artist album database
        List<Album> albumList = artistAlbumMap.get(artist);
        if(albumList == null)
            albumList = new ArrayList<>();

        albumList.add(album);
        artistAlbumMap.put(artist,albumList);
        albumSongMap.put(album,new ArrayList<>());

        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        boolean flag = false;
        Album album = null;
        //if this album doesn't exist in DataBase return Exception
        for(Album a: albums) {
            if(a.getTitle().equals(albumName)){
                album = a;
                flag = true;
                break;
            }
        }
        if(!flag)
            throw new Exception("Album does not exist");

        //now album exists in database
        Song song = new Song(title,length);
        songs.add(song);

        List<Song> songList = albumSongMap.get(album);
        songList.add(song);
        albumSongMap.put(album,songList);
        songLikeMap.put(song,new ArrayList<>());

        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        //first we check if this user exists or not
        User user = findUserByMobile(mobile);

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);

        //now we add song in this playlist
        //find list of all song of given length
        //and add it to Database
        List<Song> songList = new ArrayList<>();
        for(Song s : songs){
            if(s.getLength() == length)
                songList.add(s);
        }
        playlistSongMap.put(playlist,songList);
        creatorPlaylistMap.put(user,playlist);

        //now add this user in list of user for playList
        List<User> userList = playlistListenerMap.get(playlist);
        if(userList == null)
            userList = new ArrayList<>();

        userList.add(user);
        playlistListenerMap.put(playlist,userList);

        //now also update the list of playList for current user
        List<Playlist> playlistList = userPlaylistMap.get(user);
        if(playlistList == null)
            playlistList = new ArrayList<>();

        playlistList.add(playlist);
        userPlaylistMap.put(user,playlistList);

        return playlist;
    }

    public User findUserByMobile(String mobile) throws Exception {
        User user = null;
        boolean flag = false;
        for(User u : users){
            if(u.getMobile().equals(mobile)){
                user = u;
                flag = true;
                break;
            }
        }
        if(!flag)
            throw new Exception("User does not exist");

        return user;
    }
    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        //first we check if this user exists or not
        User user = findUserByMobile(mobile);

        //now create playlist and add it to Database
        Playlist playlist = new Playlist(title);
        playlists.add(playlist);

        //now we add song in this playlist
        //find list of all song of given length
        List<Song> songList = new ArrayList<>();
        for(String songTitle : songTitles) {
            for (Song s : songs) {
                if (s.getTitle().equals(songTitle))
                    songList.add(s);
            }
        }
        playlistSongMap.put(playlist,songList);
        creatorPlaylistMap.put(user,playlist);

        //now add this user in list of user for playList
        List<User> userList = playlistListenerMap.get(playlist);
        if(userList == null)
            userList = new ArrayList<>();

        userList.add(user);
        playlistListenerMap.put(playlist,userList);

        //now also update the list of playList for current user
        List<Playlist> playlistList = userPlaylistMap.get(user);
        if(playlistList == null)
            playlistList = new ArrayList<>();

        playlistList.add(playlist);
        userPlaylistMap.put(user,playlistList);

        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        //check if user and playList exists in DB
        User user = findUserByMobile(mobile);

        boolean flag = false;
        Playlist playlist = null;
        for(Playlist p : playlists){
            if(p.getTitle().equals(playlistTitle)) {
                playlist = p;
                flag = true;
                break;
            }
        }
        if(!flag)
            throw new Exception("Playlist does not exist");

        //if user is the creator of playList then just return
        if(creatorPlaylistMap.containsKey(user))
            return playlist;

        //now if user already a listener of this playList then just return
        List<User> userList = playlistListenerMap.get(playlist);
        for(User u : userList){
            if(u.equals(user))
                return playlist;
        }
        //otherwise update the listener and userPlayList
        userList.add(user);
        playlistListenerMap.put(playlist,userList);

        List<Playlist> playlistList = userPlaylistMap.get(user);
        if(playlistList == null)
            playlistList = new ArrayList<>();

        playlistList.add(playlist);
        userPlaylistMap.put(user,playlistList);

        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = findUserByMobile(mobile);
        //song validation in DB
        Song song = null;
        boolean flag = false;
        for(Song s : songs){
            if(s.getTitle().equals(songTitle)){
                flag = true;
                song = s;
                break;
            }
        }
        if(!flag)
            throw  new Exception("Song does not exist");

        //if this song already liked by this user then just return
        List<User> userList = songLikeMap.get(song);
        for(User u : userList){
            if(u.equals(user))
                return song;
        }
        userList.add(user);
        songLikeMap.put(song,userList);
        //now get the artist of this song
        //1. get the album in which this song present
        Album album = null;
        flag = false;
        for(Map.Entry<Album,List<Song>> map : albumSongMap.entrySet()){
            List<Song> songList = map.getValue();
            for(Song s : songList){
                if(s.equals(song)){
                    album = map.getKey();
                    flag = true;
                    break;
                }
            }
            if(flag)
                break;
        }
        //2. get the artist of this album
        Artist artist = null;
        flag = false;
        for(Map.Entry<Artist,List<Album>> map : artistAlbumMap.entrySet()){
            List<Album> albumList = map.getValue();
            for(Album a : albumList){
                if(a.equals(album)){
                    artist = map.getKey();
                    flag = true;
                    break;
                }
            }
            if(flag)
                break;
        }
        //updated the likes for this song and artist also
        song.setLikes(song.getLikes()+1);
        if (artist != null) {
            artist.setLikes(artist.getLikes()+1);
        }

        return song;
    }

    public String mostPopularArtist() {
        Artist artist = null;
        int mostLikes = 0;
        for(Artist a : artists){
            if(a.getLikes() > mostLikes){
                mostLikes = a.getLikes();
                artist = a;
            }
        }
        return (artist != null ? artist.getName()+mostLikes : "");
    }

    public String mostPopularSong() {
        Song song = null;
        int mostLikes = 0;
        for(Song s : songs){
            if(s.getLikes() > mostLikes){
                mostLikes = s.getLikes();
                song = s;
            }
        }
        return (song != null ? song.getTitle()+mostLikes : "");
    }
}
