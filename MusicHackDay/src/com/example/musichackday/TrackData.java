package com.example.musichackday;

import java.util.List;
import java.util.Map;

public class TrackData {
    public Message message;
    
    public class Message {
        public Body body;
        public Header header;
    }
    
    public class Header {
        public int status_code;
        public double execute_time;
        public int available;
    }

    public class Body {
        public List<Track> track_list;
    }
    
    public class TrackList {
        public List<Track> track_list;
    }
    
    public class Track {
        public int track_id;
        public int has_lyrics;
    }


}

