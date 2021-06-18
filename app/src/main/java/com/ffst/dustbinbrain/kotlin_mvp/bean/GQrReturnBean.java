package com.ffst.dustbinbrain.kotlin_mvp.bean;

public class GQrReturnBean {


    /**
     * face_image_state : true
     * featrue_state : true
     * info : {"token":"160beef7-1a9f-4c25-b426-c082509cdb96","user_id":63,"user_type":1}
     */

    private boolean face_image_state;
    private boolean featrue_state;
    /**
     * token : 160beef7-1a9f-4c25-b426-c082509cdb96
     * user_id : 63
     * user_type : 1
     */

    private InfoBean info;

    public boolean isFace_image_state() {
        return face_image_state;
    }

    public void setFace_image_state(boolean face_image_state) {
        this.face_image_state = face_image_state;
    }

    public boolean isFeatrue_state() {
        return featrue_state;
    }

    public void setFeatrue_state(boolean featrue_state) {
        this.featrue_state = featrue_state;
    }

    public InfoBean getInfo() {
        return info;
    }

    public void setInfo(InfoBean info) {
        this.info = info;
    }

    public static class InfoBean {
        private String token;
        private int user_id;
        private int user_type;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public int getUser_id() {
            return user_id;
        }

        public void setUser_id(int user_id) {
            this.user_id = user_id;
        }

        public int getUser_type() {
            return user_type;
        }

        public void setUser_type(int user_type) {
            this.user_type = user_type;
        }
    }
}
