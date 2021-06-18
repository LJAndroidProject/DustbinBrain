package com.ffst.dustbinbrain.kotlin_mvp.bean;

public class VXLoginCall {


    /**
     * face_image_state : true
     * featrue_state : true
     * info : {"token":"6442d742-7f44-4a0e-a8c6-8c99fe171507","user_id":34,"user_type":1,"face_image":"http://123123.png","featrue":"123212skdSD21MKMDAKDSJAMSKDMksdkdmaskmd"}
     */

    private boolean face_image_state;
    private boolean featrue_state;
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
        /**
         * token : 6442d742-7f44-4a0e-a8c6-8c99fe171507
         * user_id : 34
         * user_type : 1
         * face_image : http://123123.png
         * featrue : 123212skdSD21MKMDAKDSJAMSKDMksdkdmaskmd
         */

        private String token;
        private int user_id;
        private int user_type;
        private String face_image;
        private String featrue;

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

        public String getFace_image() {
            return face_image;
        }

        public void setFace_image(String face_image) {
            this.face_image = face_image;
        }

        public String getFeatrue() {
            return featrue;
        }

        public void setFeatrue(String featrue) {
            this.featrue = featrue;
        }

        @Override
        public String toString() {
            return "InfoBean{" +
                    "token='" + token + '\'' +
                    ", user_id=" + user_id +
                    ", user_type=" + user_type +
                    ", face_image='" + face_image + '\'' +
                    ", featrue='" + featrue + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "VXLoginCall{" +
                "face_image_state=" + face_image_state +
                ", featrue_state=" + featrue_state +
                ", info=" + info +
                '}';
    }
}
