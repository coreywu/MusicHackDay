package com.example.musichackday;


public class TrackLyrics {
    public Message message;
    
    public class Message {
        public Body body;
        public Header header;
    }
    
    public class Header {
        public int status_code;
        public double execute_time;
        public int maintenance_id;
    }

    public class Body {
        public Subtitle subtitle;
    }
    
    public class Subtitle {
        public String subtitle_body;
    }
    

}

