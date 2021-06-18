package com.ffst.dustbinbrain.kotlin_mvp.bean;

public class NfcActivityBean {

    /**
     * type : nfcActivity
     * data : {"code":1,"info":{"user_id":59,"user_type":1,"card_code":"121"}}
     */

    private String type;
    private DataBean data;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * code : 1
         * info : {"user_id":59,"user_type":1,"card_code":"121"}
         */

        private int code;
        private InfoBean info;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public InfoBean getInfo() {
            return info;
        }

        public void setInfo(InfoBean info) {
            this.info = info;
        }

        public static class InfoBean {
            /**
             * user_id : 59
             * user_type : 1
             * card_code : 121
             */

            private int user_id;
            private int user_type;
            private String card_code;

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

            public String getCard_code() {
                return card_code;
            }

            public void setCard_code(String card_code) {
                this.card_code = card_code;
            }
        }
    }
}
